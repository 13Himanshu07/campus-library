package com.library.servlet;
import com.library.service.BookService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/librarian/books/delete") public class DeleteBookServlet extends BaseServlet {
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{try{service(BookService.class).archive(paramId(q,"id"));flash(q,"Book removed from the catalog.");s.sendRedirect(q.getContextPath()+"/app/books");}catch(RuntimeException e){try{error(q,s,e);}catch(Exception ignored){s.sendError(500);}}}
}
