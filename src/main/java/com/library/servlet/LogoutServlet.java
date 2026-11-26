package com.library.servlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/logout") public class LogoutServlet extends BaseServlet { @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{HttpSession s=req.getSession(false);Long actor=s==null?null:(Long)s.getAttribute("userId");com.library.util.AuditLog.record(actor,"LOGOUT","USER",actor==null?null:actor.toString(),true);if(s!=null)s.invalidate();res.sendRedirect(req.getContextPath()+"/login?loggedOut=1");} }
