package com.library.servlet;
import com.library.model.Book;
import com.library.service.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/librarian/books/edit") public class EditBookServlet extends AddBookServlet {
 @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{try{q.setAttribute("book",service(BookService.class).get(paramId(q,"id")));q.setAttribute("categories",service(CategoryService.class).all());q.getRequestDispatcher("/WEB-INF/views/librarian/book-form.jsp").forward(q,s);}catch(RuntimeException e){error(q,s,e);}}
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{try{Book b=read(q);b.setId(paramId(q,"id"));service(BookService.class).update(b);flash(q,"Book details updated.");s.sendRedirect(q.getContextPath()+"/app/books");}catch(RuntimeException e){error(q,s,e);}}
}
