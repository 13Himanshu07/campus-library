package com.library.listener;

import com.library.dao.impl.*;
import com.library.service.*;
import com.library.service.impl.*;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class LibraryContextListener implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent event){
        var context=event.getServletContext();
        put(context,UserService.class,new UserServiceImpl(new UserDAOImpl()));
        BookService books=new BookServiceImpl(new BookDAOImpl());
        put(context,BookService.class,books);
        put(context,IssueService.class,new IssueServiceImpl(new IssueDAOImpl(),books,integer("LIBRARY_BORROW_DAYS",14),integer("LIBRARY_BORROW_LIMIT",5),new java.math.BigDecimal(setting("LIBRARY_FINE_PER_DAY","5.00"))));
        put(context,FineService.class,new FineServiceImpl(new FineDAOImpl()));
        put(context,CategoryService.class,new CategoryServiceImpl(new CategoryDAOImpl()));
        put(context,DashboardService.class,new DashboardServiceImpl(new DashboardDAOImpl()));
    }
    private <T> void put(jakarta.servlet.ServletContext context,Class<T> type,T value){context.setAttribute(type.getName(),value);}
    private int integer(String name,int fallback){try{return Integer.parseInt(setting(name,Integer.toString(fallback)));}catch(NumberFormatException e){throw new IllegalStateException(name+" must be an integer",e);}}
    private String setting(String name,String fallback){String value=System.getenv(name);return value==null||value.isBlank()?fallback:value;}
}
