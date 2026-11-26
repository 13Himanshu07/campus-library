package com.library.servlet;
import com.library.service.FineService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet(urlPatterns={"/app/fines","/librarian/fines"}) public class FineServlet extends BaseServlet {
 @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{try{String role=(String)q.getSession().getAttribute("role");long userId=(long)q.getSession().getAttribute("userId");q.setAttribute("fines","STUDENT".equals(role)?service(FineService.class).forUser(userId):service(FineService.class).all());q.getRequestDispatcher("/WEB-INF/views/librarian/fines.jsp").forward(q,s);}catch(RuntimeException e){error(q,s,e);}}
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{Long actor=q.getSession(false)==null?null:(Long)q.getSession().getAttribute("userId");long fineId=-1;try{if("STUDENT".equals(q.getSession().getAttribute("role"))){s.sendError(HttpServletResponse.SC_FORBIDDEN);return;}fineId=paramId(q,"fineId");service(FineService.class).markPaid(fineId);com.library.util.AuditLog.record(actor,"FINE_PAYMENT","FINE",Long.toString(fineId),true);flash(q,"Fine marked as paid.");s.sendRedirect(q.getContextPath()+"/librarian/fines");}catch(RuntimeException e){com.library.util.AuditLog.record(actor,"FINE_PAYMENT","FINE",fineId<0?null:Long.toString(fineId),false);try{error(q,s,e);}catch(Exception ignored){s.sendError(500);}}}
}
