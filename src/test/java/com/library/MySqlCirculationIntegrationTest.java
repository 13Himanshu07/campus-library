package com.library;

import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.IssueDAOImpl;
import com.library.service.impl.IssueServiceImpl;
import com.library.util.DBConnection;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/** Real MySQL integration coverage. Docker-less machines skip this class explicitly. */
@Testcontainers(disabledWithoutDocker = true)
class MySqlCirculationIntegrationTest {
    @Container static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
        .withDatabaseName("library_management_test").withUsername("library_test").withPassword("integration-only-password");
    static final IssueDAOImpl DAO = new IssueDAOImpl();

    @BeforeAll static void initializeDatabase() {
        Flyway.configure().dataSource(MYSQL.getJdbcUrl(),MYSQL.getUsername(),MYSQL.getPassword())
            .locations("classpath:db/migration").load().migrate();
        DBConnection.initialize(MYSQL.getJdbcUrl(),MYSQL.getUsername(),MYSQL.getPassword());
    }
    @AfterAll static void closePool(){DBConnection.shutdown();}
    @BeforeEach void seed() throws Exception {
        try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()) {
            s.executeUpdate("DELETE FROM audit_log");s.executeUpdate("DELETE FROM fines");s.executeUpdate("DELETE FROM book_issues");s.executeUpdate("DELETE FROM books");s.executeUpdate("DELETE FROM categories");s.executeUpdate("DELETE FROM users");
            s.executeUpdate("INSERT INTO users(id,name,email,password_hash,membership_id,role,status) VALUES (1,'Student One','s1@test.local','x','M1','STUDENT','ACTIVE'),(2,'Student Two','s2@test.local','x','M2','STUDENT','ACTIVE'),(3,'Librarian','lib@test.local','x','M3','LIBRARIAN','ACTIVE')");
            s.executeUpdate("INSERT INTO books(id,title,author,isbn,total_copies,available_copies) VALUES (1,'Concurrency','Author','T-1',1,1),(2,'Returns','Author','T-2',4,4)");
        }
    }

    @Test void concurrentBorrowingNeverIssuesOneCopyTwice() throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2); CountDownLatch ready=new CountDownLatch(2), start=new CountDownLatch(1);
        try {
            Callable<Boolean> a=()->{ready.countDown();start.await();try{DAO.issue(1,1,14,5);return true;}catch(RuntimeException e){return false;}};
            Callable<Boolean> b=()->{ready.countDown();start.await();try{DAO.issue(1,2,14,5);return true;}catch(RuntimeException e){return false;}};
            Future<Boolean> fa=pool.submit(a),fb=pool.submit(b);assertTrue(ready.await(10,TimeUnit.SECONDS));start.countDown();
            assertEquals(1,(int)(fa.get(20,TimeUnit.SECONDS)?1:0)+(fb.get(20,TimeUnit.SECONDS)?1:0));
            assertEquals(0,scalar("SELECT available_copies FROM books WHERE id=1"));assertEquals(1,scalar("SELECT COUNT(*) FROM book_issues WHERE book_id=1 AND status='ISSUED'"));
        } finally {pool.shutdownNow();}
    }

    @Test void returnRestoresInventoryAndRejectsDuplicateReturn() {
        long id=DAO.issue(2,1,14,5);DAO.returnBook(id,BigDecimal.ZERO);
        assertEquals(4,scalar("SELECT available_copies FROM books WHERE id=2"));assertEquals("RETURNED",value("SELECT status FROM book_issues WHERE id="+id));
        assertThrows(RuntimeException.class,()->DAO.returnBook(id,BigDecimal.ZERO));assertEquals(4,scalar("SELECT available_copies FROM books WHERE id=2"));
    }

    @Test void borrowAndReturnFailuresRollBackEarlierWrites() throws Exception {
        try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()) {
            s.execute("CREATE TRIGGER fail_inventory_update BEFORE UPDATE ON books FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='injected inventory failure'");
        }
        try { assertThrows(RuntimeException.class,()->DAO.issue(2,1,14,5));assertEquals(0,scalar("SELECT COUNT(*) FROM book_issues"));assertEquals(4,scalar("SELECT available_copies FROM books WHERE id=2")); }
        finally {try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()){s.execute("DROP TRIGGER fail_inventory_update");}}
        long id=DAO.issue(2,1,14,5);
        try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()){s.execute("CREATE TRIGGER fail_inventory_update BEFORE UPDATE ON books FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='injected inventory failure'");}
        try {assertThrows(RuntimeException.class,()->DAO.returnBook(id,new BigDecimal("10.00")));assertNull(value("SELECT return_date FROM book_issues WHERE id="+id));assertEquals(3,scalar("SELECT available_copies FROM books WHERE id=2"));assertEquals(0,scalar("SELECT COUNT(*) FROM fines WHERE issue_id="+id));}
        finally {try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()){s.execute("DROP TRIGGER fail_inventory_update");}}
    }

    @Test void fineRulesCoverSameDayAndSeveralOverdueDurations() throws Exception {
        IssueServiceImpl service=new IssueServiceImpl(DAO,new com.library.service.impl.BookServiceImpl(new BookDAOImpl()),14,5,new BigDecimal("5.00"));
        long same=insertIssue(2,1,LocalDate.now());service.returnBook(same);assertEquals(0,scalar("SELECT COUNT(*) FROM fines WHERE issue_id="+same));
        long three=insertIssue(2,1,LocalDate.now().minusDays(3));service.returnBook(three);assertEquals(new BigDecimal("15.00"),decimal("SELECT amount FROM fines WHERE issue_id="+three));
        long ten=insertIssue(2,2,LocalDate.now().minusDays(10));service.returnBook(ten);assertEquals(new BigDecimal("50.00"),decimal("SELECT amount FROM fines WHERE issue_id="+ten));
        assertThrows(RuntimeException.class,()->service.returnBook(three));assertEquals(1,scalar("SELECT COUNT(*) FROM fines WHERE issue_id="+three));
    }

    private long insertIssue(long book,long user,LocalDate due)throws Exception {try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO book_issues(book_id,user_id,issue_date,due_date,status) VALUES(?,?,?,?, 'ISSUED')",Statement.RETURN_GENERATED_KEYS)){p.setLong(1,book);p.setLong(2,user);p.setDate(3,Date.valueOf(due.minusDays(14)));p.setDate(4,Date.valueOf(due));p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();return r.getLong(1);}}}
    private int scalar(String sql){return Integer.parseInt(value(sql));}
    private String value(String sql){try(Connection c=DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getString(1);}catch(Exception e){throw new RuntimeException(e);}}
    private BigDecimal decimal(String sql){try(Connection c=DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getBigDecimal(1);}catch(Exception e){throw new RuntimeException(e);}}
}


