package com.library.servlet;
import com.library.service.IssueService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/app/issues/borrow") public class IssueBookServlet extends BaseServlet {
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{try{if(!"STUDENT".equals(q.getSession().getAttribute("role"))){s.sendError(HttpServletResponse.SC_FORBIDDEN);return;}long userId=(long)q.getSession().getAttribute("userId");service(IssueService.class).issue(paramId(q,"bookId"),userId);flash(q,"Book issued. Check your due date under My books.");s.sendRedirect(q.getContextPath()+"/app/my-books");}catch(RuntimeException e){try{error(q,s,e);}catch(Exception ignored){s.sendError(500);}}}
}
