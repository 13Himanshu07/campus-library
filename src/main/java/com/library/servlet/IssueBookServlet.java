package com.library.servlet;
import com.library.service.IssueService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/app/issues/borrow") public class IssueBookServlet extends BaseServlet {
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{Long actor=q.getSession(false)==null?null:(Long)q.getSession().getAttribute("userId");long bookId=-1;try{if(!"STUDENT".equals(q.getSession().getAttribute("role"))){s.sendError(HttpServletResponse.SC_FORBIDDEN);return;}long userId=(long)q.getSession().getAttribute("userId");bookId=paramId(q,"bookId");long issueId=service(IssueService.class).issue(bookId,userId);com.library.util.AuditLog.record(actor,"BOOK_BORROW","BOOK_ISSUE",Long.toString(issueId),true);flash(q,"Book issued. Check your due date under My books.");s.sendRedirect(q.getContextPath()+"/app/my-books");}catch(RuntimeException e){com.library.util.AuditLog.record(actor,"BOOK_BORROW","BOOK",bookId<0?null:Long.toString(bookId),false);try{error(q,s,e);}catch(Exception ignored){s.sendError(500);}}}
}
