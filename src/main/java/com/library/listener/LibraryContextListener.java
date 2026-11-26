package com.library.listener;

import com.library.dao.impl.*;
import com.library.concurrent.*;
import com.library.service.*;
import com.library.service.impl.*;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import com.library.util.DBConnection;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class LibraryContextListener implements ServletContextListener {
    private static final Logger LOG=LoggerFactory.getLogger(LibraryContextListener.class);
    private LibraryTaskExecutor taskExecutor;
    @Override public void contextInitialized(ServletContextEvent event){
        boolean production="production".equalsIgnoreCase(setting("APP_ENV","development"));
        if(production&&setting("PUBLIC_HTTPS_HOST","").isBlank())throw new IllegalStateException("PUBLIC_HTTPS_HOST must be configured in production.");
        var cookies=event.getServletContext().getSessionCookieConfig();
        cookies.setHttpOnly(true); cookies.setSecure(production); cookies.setAttribute("SameSite","Lax");
        cookies.setName("JSESSIONID");
        String url=setting("DB_URL","jdbc:mysql://localhost:3306/library_management?serverTimezone=UTC");
        String username=setting("DB_MIGRATION_USERNAME",setting("DB_USERNAME","library_app"));
        String password=setting("DB_MIGRATION_PASSWORD",setting("DB_PASSWORD",""));
        try {
            DBConnection.validateProductionConfiguration(url,setting("DB_USERNAME","library_app"));
            Flyway.configure().dataSource(url,username,password).locations("classpath:db/migration")
                .baselineOnMigrate(true).baselineVersion("1").load().migrate();
            DBConnection.initialize();
            LOG.info("application_startup database_migrations=complete db_pool={}",DBConnection.getDataSource().getPoolName());
        } catch (RuntimeException e) { LOG.error("application_startup_failed database initialization failed"); throw e; }
        var context=event.getServletContext();
        put(context,UserService.class,new UserServiceImpl(new UserDAOImpl()));
        BookService books=new BookServiceImpl(new BookDAOImpl());
        put(context,BookService.class,books);
        put(context,IssueService.class,new IssueServiceImpl(new IssueDAOImpl(),books,integer("LIBRARY_BORROW_DAYS",14),integer("LIBRARY_BORROW_LIMIT",5),new java.math.BigDecimal(setting("LIBRARY_FINE_PER_DAY","5.00"))));
        FineService fines=new FineServiceImpl(new FineDAOImpl());
        put(context,FineService.class,fines);
        taskExecutor=new LibraryTaskExecutor();
        taskExecutor.schedulePoolMetrics(DBConnection.getDataSource());
        taskExecutor.scheduleFineCalculation(new FineCalculationTask((IssueService)context.getAttribute(IssueService.class.getName()),fines,new java.math.BigDecimal(setting("LIBRARY_FINE_PER_DAY","5.00"))),java.time.Duration.ofDays(1));
        put(context,CategoryService.class,new CategoryServiceImpl(new CategoryDAOImpl()));
        put(context,DashboardService.class,new DashboardServiceImpl(new DashboardDAOImpl()));
    }
    @Override public void contextDestroyed(ServletContextEvent event){if(taskExecutor!=null)taskExecutor.close();DBConnection.shutdown();LOG.info("application_shutdown");}
    private <T> void put(jakarta.servlet.ServletContext context,Class<T> type,T value){context.setAttribute(type.getName(),value);}
    private int integer(String name,int fallback){try{return Integer.parseInt(setting(name,Integer.toString(fallback)));}catch(NumberFormatException e){throw new IllegalStateException(name+" must be an integer",e);}}
    private String setting(String name,String fallback){String value=System.getenv(name);return value==null||value.isBlank()?fallback:value;}
}
