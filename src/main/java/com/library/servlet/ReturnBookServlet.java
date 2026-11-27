package com.library.servlet;
import com.library.service.IssueService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/librarian/issues/return") public class ReturnBookServlet extends BaseServlet {
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{Long actor=q.getSession(false)==null?null:(Long)q.getSession().getAttribute("userId");long issueId=-1;try{issueId=paramId(q,"issueId");service(IssueService.class).returnBook(issueId,actor);flash(q,"Return recorded and any late fine calculated.");s.sendRedirect(q.getContextPath()+"/librarian/issues");}catch(RuntimeException e){com.library.util.AuditLog.record(actor,"BOOK_RETURN","BOOK_ISSUE",issueId<0?null:Long.toString(issueId),false);try{error(q,s,e);}catch(Exception ignored){s.sendError(500);}}}
}
