package com.library.servlet;
import com.library.model.Book;
import com.library.service.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/librarian/books/add") public class AddBookServlet extends BaseServlet {
 @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{q.setAttribute("categories",service(CategoryService.class).all());q.setAttribute("book",new Book());q.getRequestDispatcher("/WEB-INF/views/librarian/book-form.jsp").forward(q,s);}
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{Long actor=q.getSession(false)==null?null:(Long)q.getSession().getAttribute("userId");Book b=read(q);try{Book created=service(BookService.class).add(b);com.library.util.AuditLog.record(actor,"BOOK_ADD","BOOK",Long.toString(created.getId()),true);flash(q,"Book added to the collection.");s.sendRedirect(q.getContextPath()+"/app/books");}catch(RuntimeException e){com.library.util.AuditLog.record(actor,"BOOK_ADD","BOOK",null,false);error(q,s,e);}}
 protected Book read(HttpServletRequest q){Book b=new Book();b.setTitle(q.getParameter("title"));b.setAuthor(q.getParameter("author"));b.setIsbn(q.getParameter("isbn"));b.setDescription(q.getParameter("description"));b.setPublisher(q.getParameter("publisher"));b.setCategoryId(number(q,"categoryId",0));b.setPublicationYear(nullableInt(q.getParameter("publicationYear")));b.setTotalCopies((int)number(q,"totalCopies",0));return b;}
 protected long number(HttpServletRequest q,String name,long fallback){String v=q.getParameter(name);return v==null||v.isBlank()?fallback:Long.parseLong(v);}
 protected Integer nullableInt(String value){return value==null||value.isBlank()?null:Integer.valueOf(value);}
}
