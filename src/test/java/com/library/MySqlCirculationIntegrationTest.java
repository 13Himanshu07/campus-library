package com.library;

import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.FineDAOImpl;
import com.library.dao.impl.IssueDAOImpl;
import com.library.dao.impl.UserDAOImpl;
import com.library.concurrent.FineCalculationTask;
import com.library.service.impl.BookServiceImpl;
import com.library.service.impl.FineServiceImpl;
import com.library.service.impl.IssueServiceImpl;
import com.library.service.impl.UserServiceImpl;
import com.library.model.Book;
import com.library.util.DBConnection;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/** Real MySQL 8 tests; separate pools model independent application instances. */
@Testcontainers(disabledWithoutDocker = true)
class MySqlCirculationIntegrationTest {
    private static final String ROOT_PASSWORD="a19c70e8b4ac5ce9";
    private static final String APP_PASSWORD="a19c70e8b4ac5ce9";
    private static final String MIGRATOR_PASSWORD="c2f15c699dab0221";
    @Container static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
        .withDatabaseName("library_management_test").withUsername("bootstrap_user").withPassword(ROOT_PASSWORD)
        .withEnv("MYSQL_ROOT_PASSWORD",ROOT_PASSWORD).withEnv("MYSQL_ROOT_HOST","%");
    static final IssueDAOImpl DAO = new IssueDAOImpl();

    @BeforeAll static void initializeDatabase() throws Exception {
        bootstrapRestrictedAccounts();
        Flyway.configure().dataSource(jdbc("library_management_test"),"library_migrator",MIGRATOR_PASSWORD)
            .locations("classpath:db/migration").load().migrate();
        DBConnection.initialize(jdbc("library_management_test"),"library_app",APP_PASSWORD);
    }
    @AfterAll static void closePool(){HikariDataSource pool=DBConnection.getDataSource();DBConnection.shutdown();assertTrue(pool.isClosed());}
    @BeforeEach void seed() throws Exception {
        try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()) {
            s.executeUpdate("DELETE FROM audit_logs");s.executeUpdate("DELETE FROM fines");s.executeUpdate("DELETE FROM book_issues");s.executeUpdate("DELETE FROM books");s.executeUpdate("DELETE FROM categories");s.executeUpdate("DELETE FROM users");
            s.executeUpdate("INSERT INTO users(id,name,email,password_hash,membership_id,role,status) VALUES (1,'Student One','s1@test.local','x','M1','STUDENT','ACTIVE'),(2,'Student Two','s2@test.local','x','M2','STUDENT','ACTIVE'),(3,'Librarian','lib@test.local','x','M3','LIBRARIAN','ACTIVE')");
            s.executeUpdate("INSERT INTO books(id,title,author,isbn,total_copies,available_copies) VALUES (1,'Concurrency','Author','T-1',1,1),(2,'Returns','Author','T-2',4,4)");
        }
    }

    @Test void freshMigrationAndRestrictedApplicationGrantAreVerified() throws Exception {
        assertEquals(4,scalar("SELECT COUNT(*) FROM flyway_schema_history WHERE success=TRUE AND version IS NOT NULL"));
        assertEquals(1,scalar("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='audit_logs'"));
        String grant=String.join(" ",values("SHOW GRANTS")).toUpperCase();
        assertTrue(grant.contains("SELECT, INSERT, UPDATE, DELETE"));
        assertFalse(grant.contains("ALL PRIVILEGES"));assertFalse(grant.contains("GRANT OPTION"));assertFalse(grant.contains("CREATE USER"));
        for(String ddl:java.util.List.of("CREATE","ALTER","DROP","INDEX","REFERENCES"))assertFalse(grant.matches("(?s).*\\b"+ddl+"\\b.*"),"Application user must not receive "+ddl);
        String migrator=String.join(" ",adminValues("SHOW GRANTS FOR 'library_migrator'@'%' ")).toUpperCase();
        for(String privilege:java.util.List.of("CREATE","ALTER","INDEX","DROP","REFERENCES"))assertTrue(migrator.contains(privilege));
        assertFalse(migrator.contains("ALL PRIVILEGES ON *.*"));assertFalse(migrator.contains("CREATE ON *.*"));assertFalse(migrator.contains("GRANT OPTION"));
        assertEquals(1,DAO.issue(2,1,14,5)>0?1:0);
    }

    @Test void independentApplicationPoolsCannotBorrowLastCopyTwice() throws Exception {
        try(HikariDataSource appOne=pool();HikariDataSource appTwo=pool()) {
            IssueDAOImpl instanceOne=new IssueDAOImpl(appOne),instanceTwo=new IssueDAOImpl(appTwo);
            ExecutorService workers=Executors.newFixedThreadPool(2);CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);
            try {
                Future<Boolean> first=workers.submit(borrow(instanceOne,1,ready,start));Future<Boolean> second=workers.submit(borrow(instanceTwo,2,ready,start));
                assertTrue(ready.await(10,TimeUnit.SECONDS));start.countDown();
                assertEquals(1,(int)(first.get(20,TimeUnit.SECONDS)?1:0)+(second.get(20,TimeUnit.SECONDS)?1:0));
                assertEquals(0,scalar("SELECT available_copies FROM books WHERE id=1"));
                assertEquals(1,scalar("SELECT COUNT(*) FROM book_issues WHERE book_id=1 AND status='ISSUED'"));
                assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='BOOK_BORROW' AND outcome='SUCCESS'"));
            } finally {workers.shutdownNow();}
        }
    }

    @Test void independentApplicationPoolsCannotReturnTheSameLoanTwice() throws Exception {
        long issueId=insertIssue(2,1,LocalDate.now().minusDays(3));
        try(HikariDataSource appOne=pool();HikariDataSource appTwo=pool()) {
            IssueDAOImpl instanceOne=new IssueDAOImpl(appOne),instanceTwo=new IssueDAOImpl(appTwo);
            ExecutorService workers=Executors.newFixedThreadPool(2);CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);
            try {
                Future<Boolean> first=workers.submit(returnLoan(instanceOne,issueId,ready,start));
                Future<Boolean> second=workers.submit(returnLoan(instanceTwo,issueId,ready,start));
                assertTrue(ready.await(10,TimeUnit.SECONDS));start.countDown();
                assertEquals(1,(int)(first.get(20,TimeUnit.SECONDS)?1:0)+(second.get(20,TimeUnit.SECONDS)?1:0));
                assertEquals("RETURNED",value("SELECT status FROM book_issues WHERE id="+issueId));
                assertNotNull(value("SELECT return_date FROM book_issues WHERE id="+issueId));
                assertEquals(4,scalar("SELECT available_copies FROM books WHERE id=2"));
                assertEquals(1,scalar("SELECT COUNT(*) FROM fines WHERE issue_id="+issueId));
                assertEquals(new BigDecimal("15.00"),decimal("SELECT amount FROM fines WHERE issue_id="+issueId));
                assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='BOOK_RETURN' AND entity_id='"+issueId+"' AND outcome='SUCCESS'"));
            } finally {workers.shutdownNow();}
        }
    }

    @Test void applicationPoolReusesConnectionsAndCanBeClosed() throws Exception {
        try(HikariDataSource oneConnectionPool=pool(1)) {
            long firstConnectionId;
            try(Connection c=oneConnectionPool.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT CONNECTION_ID()")){assertTrue(r.next());firstConnectionId=r.getLong(1);}
            try(Connection c=oneConnectionPool.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT CONNECTION_ID()")){
                assertTrue(r.next());assertEquals(firstConnectionId,r.getLong(1));
            }
            assertEquals(1,oneConnectionPool.getHikariPoolMXBean().getTotalConnections());
            assertEquals(0,oneConnectionPool.getHikariPoolMXBean().getActiveConnections());
        }
        HikariDataSource closedPool=pool(1);closedPool.close();assertTrue(closedPool.isClosed());
        HikariDataSource original=DBConnection.getDataSource();
        DBConnection.initialize(jdbc("library_management_test"),"library_app",APP_PASSWORD);
        assertSame(original,DBConnection.getDataSource());
    }

    @Test void returnIsAuditedAndDuplicateReturnDoesNotChangeInventory() {
        long id=DAO.issue(2,1,14,5);DAO.returnBook(id,BigDecimal.ZERO,3L);
        assertEquals(4,scalar("SELECT available_copies FROM books WHERE id=2"));assertEquals("RETURNED",value("SELECT status FROM book_issues WHERE id="+id));
        assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='BOOK_RETURN' AND entity_id='"+id+"'"));
        assertThrows(RuntimeException.class,()->DAO.returnBook(id,BigDecimal.ZERO,3L));assertEquals(4,scalar("SELECT available_copies FROM books WHERE id=2"));
    }

    @Test void failedAuditInsertRollsBackBorrowAndInventoryTogether() throws Exception {
        adminExecute("CREATE TRIGGER fail_audit BEFORE INSERT ON library_management_test.audit_logs FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='injected audit failure'");
        try {
            assertThrows(RuntimeException.class,()->DAO.issue(2,1,14,5));
            assertEquals(0,scalar("SELECT COUNT(*) FROM book_issues"));assertEquals(4,scalar("SELECT available_copies FROM books WHERE id=2"));
        } finally {adminExecute("DROP TRIGGER library_management_test.fail_audit");}
    }

    @Test void businessSqlFailuresRollBackBorrowAndReturn() throws Exception {
        adminExecute("CREATE TRIGGER fail_inventory BEFORE UPDATE ON library_management_test.books FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='injected inventory failure'");
        try {assertThrows(RuntimeException.class,()->DAO.issue(2,1,14,5));assertEquals(0,scalar("SELECT COUNT(*) FROM book_issues"));assertEquals(4,scalar("SELECT available_copies FROM books WHERE id=2"));}
        finally {adminExecute("DROP TRIGGER library_management_test.fail_inventory");}
        long id=DAO.issue(2,1,14,5);
        adminExecute("CREATE TRIGGER fail_inventory BEFORE UPDATE ON library_management_test.books FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='injected inventory failure'");
        try {assertThrows(RuntimeException.class,()->DAO.returnBook(id,new BigDecimal("10.00"),3L));assertNull(value("SELECT return_date FROM book_issues WHERE id="+id));assertEquals(3,scalar("SELECT available_copies FROM books WHERE id=2"));assertEquals(0,scalar("SELECT COUNT(*) FROM fines WHERE issue_id="+id));assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='BOOK_BORROW' AND outcome='SUCCESS'"));}
        finally {adminExecute("DROP TRIGGER library_management_test.fail_inventory");}
    }

    @Test void returnFineAmountsCoverSameDayAndMultipleOverdueDurations() throws Exception {
        IssueServiceImpl service=new IssueServiceImpl(DAO,new BookServiceImpl(new BookDAOImpl()),14,5,new BigDecimal("5.00"));
        long same=insertIssue(2,1,LocalDate.now());service.returnBook(same,3L);assertEquals(0,scalar("SELECT COUNT(*) FROM fines WHERE issue_id="+same));
        long three=insertIssue(2,1,LocalDate.now().minusDays(3));service.returnBook(three,3L);assertEquals(new BigDecimal("15.00"),decimal("SELECT amount FROM fines WHERE issue_id="+three));
        long ten=insertIssue(2,2,LocalDate.now().minusDays(10));service.returnBook(ten,3L);assertEquals(new BigDecimal("50.00"),decimal("SELECT amount FROM fines WHERE issue_id="+ten));
        assertEquals(3,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='BOOK_RETURN' AND outcome='SUCCESS'"));
        assertThrows(RuntimeException.class,()->service.returnBook(three,3L));assertEquals(1,scalar("SELECT COUNT(*) FROM fines WHERE issue_id="+three));
        long rounded=insertIssue(2,1,LocalDate.now().minusDays(3));
        IssueServiceImpl roundingService=new IssueServiceImpl(DAO,new BookServiceImpl(new BookDAOImpl()),14,5,new BigDecimal("0.335"));
        roundingService.returnBook(rounded,3L);
        BigDecimal roundedFine=decimal("SELECT amount FROM fines WHERE issue_id="+rounded);
        assertEquals(new BigDecimal("1.01"),roundedFine);
        assertEquals(2,roundedFine.scale());
    }

    @Test void bookAndAdministrativeMutationsAreAuditedInTheirTransactions() throws Exception {
        BookServiceImpl books=new BookServiceImpl(new BookDAOImpl());Book created=new Book();created.setTitle("Audited title");created.setAuthor("Writer");created.setIsbn("AUD-1");created.setTotalCopies(2);
        books.add(created,3L);created.setTitle("Updated title");books.update(created,3L);books.archive(created.getId(),3L);
        assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='BOOK_ADD' AND actor_user_id=3"));
        assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='BOOK_UPDATE' AND actor_user_id=3"));
        assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='BOOK_ARCHIVE' AND actor_user_id=3"));
        try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()){s.executeUpdate("INSERT INTO users(id,name,email,password_hash,membership_id,role,status) VALUES(4,'Applicant','applicant@test.local','x','M4','LIBRARIAN','PENDING')");}
        new UserServiceImpl(new UserDAOImpl()).decideLibrarianApplication(4,true,3L);
        assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='LIBRARIAN_APPROVE' AND actor_user_id=3 AND entity_id='4'"));
        try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()){s.executeUpdate("INSERT INTO users(id,name,email,password_hash,membership_id,role,status) VALUES(5,'Rejected Applicant','reject@test.local','x','M5','LIBRARIAN','PENDING')");}
        new UserServiceImpl(new UserDAOImpl()).decideLibrarianApplication(5,false,3L);
        assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='LIBRARIAN_REJECT' AND actor_user_id=3 AND entity_id='5'"));
        new UserDAOImpl().updatePassword(1,"test-only-password-hash",1L);
        assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='USER_PASSWORD_CHANGE' AND actor_user_id=1 AND entity_id='1'"));
    }

    @Test void registrationIsAuditedAndAuditFailureRollsBackAccountCreation() throws Exception {
        UserServiceImpl users=new UserServiceImpl(new UserDAOImpl());
        var created=users.register("Registered Student","registered@test.local","TestPassword9!",null,"REG-1");
        assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='USER_REGISTER' AND entity_id='"+created.getId()+"' AND outcome='SUCCESS'"));
        assertEquals(1,scalar("SELECT COUNT(*) FROM users WHERE email='registered@test.local'"));
        adminExecute("CREATE TRIGGER fail_audit BEFORE INSERT ON library_management_test.audit_logs FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='injected audit failure'");
        try {
            assertThrows(RuntimeException.class,()->users.register("Rollback Student","rollback@test.local","TestPassword9!",null,"REG-2"));
            assertEquals(0,scalar("SELECT COUNT(*) FROM users WHERE email='rollback@test.local'"));
        } finally {adminExecute("DROP TRIGGER library_management_test.fail_audit");}
    }

    @Test void auditFailureRollsBackFinePayment() throws Exception {
        long issue=insertIssue(2,1,LocalDate.now().minusDays(1));
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO fines(issue_id,amount) VALUES(?,5.00)")){p.setLong(1,issue);p.executeUpdate();}
        long fineId=scalar("SELECT id FROM fines WHERE issue_id="+issue);
        adminExecute("CREATE TRIGGER fail_audit BEFORE INSERT ON library_management_test.audit_logs FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='injected audit failure'");
        try {assertThrows(RuntimeException.class,()->new FineDAOImpl().markPaid(fineId,3L));assertEquals(0,scalar("SELECT paid FROM fines WHERE id="+fineId));}
        finally {adminExecute("DROP TRIGGER library_management_test.fail_audit");}
    }

    @Test void finePaymentPersistsWithActorAuditAndBackgroundTaskPreservesPaidFine() throws Exception {
        long paidIssue=insertIssue(2,1,LocalDate.now().minusDays(4));
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO fines(issue_id,amount,paid,paid_at) VALUES(?,20.00,TRUE,CURRENT_TIMESTAMP)")){p.setLong(1,paidIssue);p.executeUpdate();}
        long unpaidIssue=insertIssue(2,2,LocalDate.now().minusDays(2));
        IssueServiceImpl issues=new IssueServiceImpl(DAO,new BookServiceImpl(new BookDAOImpl()),14,5,new BigDecimal("5.00"));
        FineDAOImpl fineDao=new FineDAOImpl();FineServiceImpl fines=new FineServiceImpl(fineDao);
        new FineCalculationTask(issues,fines,new BigDecimal("5.00")).run();
        assertEquals(new BigDecimal("20.00"),decimal("SELECT amount FROM fines WHERE issue_id="+paidIssue));assertEquals(1,scalar("SELECT paid FROM fines WHERE issue_id="+paidIssue));
        assertEquals(new BigDecimal("10.00"),decimal("SELECT amount FROM fines WHERE issue_id="+unpaidIssue));
        fineDao.markPaid(scalar("SELECT id FROM fines WHERE issue_id="+unpaidIssue),3L);
        assertEquals(1,scalar("SELECT paid FROM fines WHERE issue_id="+unpaidIssue));
        assertEquals(1,scalar("SELECT COUNT(*) FROM audit_logs WHERE action='FINE_PAYMENT' AND actor_user_id=3"));
        new FineCalculationTask(issues,fines,new BigDecimal("5.00")).run();
        assertEquals(new BigDecimal("10.00"),decimal("SELECT amount FROM fines WHERE issue_id="+unpaidIssue));
        assertEquals(1,scalar("SELECT COUNT(*) FROM fines WHERE issue_id="+unpaidIssue));
    }

    @Test void existingSchemaIsBaselinedWithoutLosingRows() throws Exception {
        String schema="library_existing_test";createDatabase(schema);
        Flyway.configure().dataSource(jdbc(schema),"library_migrator",MIGRATOR_PASSWORD).locations("classpath:db/migration").target("1").load().migrate();
        adminExecute("INSERT INTO "+schema+".users(name,email,password_hash,membership_id) VALUES('Preserved','keep@test.local','x','KEEP-1')");
        adminExecute("DROP TABLE "+schema+".flyway_schema_history");
        Flyway.configure().dataSource(jdbc(schema),"library_migrator",MIGRATOR_PASSWORD).locations("classpath:db/migration").baselineOnMigrate(true).baselineVersion("1").load().migrate();
        assertEquals("Preserved",adminValue("SELECT name FROM "+schema+".users WHERE email='keep@test.local'"));
        assertEquals(1,adminScalar("SELECT COUNT(*) FROM "+schema+".flyway_schema_history WHERE version='4' AND success=1"));
        assertEquals(1,adminScalar("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='"+schema+"' AND table_name='audit_logs'"));
    }

    @Test void migrationFailureLeavesExistingBusinessDataIntact() throws Exception {
        String schema="library_migration_fail_test";createDatabase(schema);
        Flyway.configure().dataSource(jdbc(schema),"library_migrator",MIGRATOR_PASSWORD).locations("classpath:db/migration").target("1").load().migrate();
        adminExecute("INSERT INTO "+schema+".users(name,email,password_hash,membership_id) VALUES('Still here','safe@test.local','x','SAFE-1')");
        adminExecute("CREATE INDEX idx_books_available_title ON "+schema+".books(available_copies,title)");
        adminExecute("DROP TABLE "+schema+".flyway_schema_history");
        assertThrows(FlywayException.class,()->Flyway.configure().dataSource(jdbc(schema),"library_migrator",MIGRATOR_PASSWORD).locations("classpath:db/migration").baselineOnMigrate(true).baselineVersion("1").load().migrate());
        assertEquals("Still here",adminValue("SELECT name FROM "+schema+".users WHERE email='safe@test.local'"));
        assertEquals(1,adminScalar("SELECT COUNT(*) FROM "+schema+".flyway_schema_history WHERE version='2' AND success=1"));
    }

    private Callable<Boolean> borrow(IssueDAOImpl dao,long user,CountDownLatch ready,CountDownLatch start){return ()->{ready.countDown();start.await();try{dao.issue(1,user,14,5);return true;}catch(RuntimeException e){return false;}};}
    private Callable<Boolean> returnLoan(IssueDAOImpl dao,long issueId,CountDownLatch ready,CountDownLatch start){return ()->{ready.countDown();start.await();try{dao.returnBook(issueId,new BigDecimal("15.00"),3L);return true;}catch(RuntimeException e){return false;}};}
    private long insertIssue(long book,long user,LocalDate due)throws Exception {
        try(Connection c=DBConnection.getConnection()){
            try(PreparedStatement stock=c.prepareStatement("UPDATE books SET available_copies=available_copies-1 WHERE id=? AND available_copies>0")){stock.setLong(1,book);assertEquals(1,stock.executeUpdate());}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO book_issues(book_id,user_id,issue_date,due_date,status) VALUES(?,?,?,?, 'ISSUED')",Statement.RETURN_GENERATED_KEYS)){p.setLong(1,book);p.setLong(2,user);p.setDate(3,Date.valueOf(due.minusDays(14)));p.setDate(4,Date.valueOf(due));p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();return r.getLong(1);}}
        }
    }
    private static HikariDataSource pool(){return pool(3);}
    private static HikariDataSource pool(int maximumSize){HikariConfig c=new HikariConfig();c.setJdbcUrl(jdbc("library_management_test"));c.setUsername("library_app");c.setPassword(APP_PASSWORD);c.setMaximumPoolSize(maximumSize);c.setMinimumIdle(1);c.setPoolName("test-app-instance-"+java.util.UUID.randomUUID());return new HikariDataSource(c);}
    private static String jdbc(String schema){return MYSQL.getJdbcUrl().replaceFirst("/[^/?]+(?=\\?|$)","/"+schema);}
    private static void bootstrapRestrictedAccounts() throws Exception {
        adminExecute("CREATE USER IF NOT EXISTS 'library_app'@'%' IDENTIFIED BY '"+APP_PASSWORD+"'");
        adminExecute("CREATE USER IF NOT EXISTS 'library_migrator'@'%' IDENTIFIED BY '"+MIGRATOR_PASSWORD+"'");
        adminExecute("REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'library_app'@'%' ");
        adminExecute("REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'library_migrator'@'%' ");
        adminExecute("GRANT SELECT,INSERT,UPDATE,DELETE ON library_management_test.* TO 'library_app'@'%' ");
        adminExecute("GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,ALTER,INDEX,DROP,REFERENCES ON library_management_test.* TO 'library_migrator'@'%' ");
    }
    private static Connection admin() throws SQLException{return DriverManager.getConnection(jdbc("mysql"),"root",ROOT_PASSWORD);}
    private static void adminExecute(String sql)throws Exception{try(Connection c=admin();Statement s=c.createStatement()){s.execute(sql);}}
    private static String adminValue(String sql)throws Exception{try(Connection c=admin();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){assertTrue(r.next());return r.getString(1);}}
    private static java.util.List<String> adminValues(String sql)throws Exception{try(Connection c=admin();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){java.util.List<String> all=new java.util.ArrayList<>();while(r.next())all.add(r.getString(1));return all;}}
    private static int adminScalar(String sql)throws Exception{return Integer.parseInt(adminValue(sql));}
    private static void createDatabase(String schema)throws Exception{adminExecute("CREATE DATABASE "+schema+" CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");adminExecute("GRANT SELECT,INSERT,UPDATE,DELETE ON "+schema+".* TO 'library_app'@'%' ");adminExecute("GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,ALTER,INDEX,DROP,REFERENCES ON "+schema+".* TO 'library_migrator'@'%' ");}
    private int scalar(String sql){return Integer.parseInt(value(sql));}
    private String value(String sql){try(Connection c=DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){assertTrue(r.next());return r.getString(1);}catch(Exception e){throw new RuntimeException(e);}}
    private java.util.List<String> values(String sql){try(Connection c=DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){java.util.List<String> all=new java.util.ArrayList<>();while(r.next())all.add(r.getString(1));return all;}catch(Exception e){throw new RuntimeException(e);}}
    private BigDecimal decimal(String sql){try(Connection c=DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){assertTrue(r.next());return r.getBigDecimal(1);}catch(Exception e){throw new RuntimeException(e);}}
}


