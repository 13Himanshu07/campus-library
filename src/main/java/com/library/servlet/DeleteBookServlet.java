package com.library.servlet;
import com.library.service.BookService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/librarian/books/delete") public class DeleteBookServlet extends BaseServlet {
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{Long actor=q.getSession(false)==null?null:(Long)q.getSession().getAttribute("userId");long id=-1;try{id=paramId(q,"id");service(BookService.class).archive(id);com.library.util.AuditLog.record(actor,"BOOK_ARCHIVE","BOOK",Long.toString(id),true);flash(q,"Book removed from the catalog.");s.sendRedirect(q.getContextPath()+"/app/books");}catch(RuntimeException e){com.library.util.AuditLog.record(actor,"BOOK_ARCHIVE","BOOK",id<0?null:Long.toString(id),false);try{error(q,s,e);}catch(Exception ignored){s.sendError(500);}}}
}
