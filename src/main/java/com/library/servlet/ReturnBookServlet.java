package com.library.servlet;
import com.library.service.IssueService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/librarian/issues/return") public class ReturnBookServlet extends BaseServlet {
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{try{service(IssueService.class).returnBook(paramId(q,"issueId"));flash(q,"Return recorded and any late fine calculated.");s.sendRedirect(q.getContextPath()+"/librarian/issues");}catch(RuntimeException e){try{error(q,s,e);}catch(Exception ignored){s.sendError(500);}}}
}
