package com.library.servlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/logout") public class LogoutServlet extends BaseServlet { @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{HttpSession s=req.getSession(false);if(s!=null)s.invalidate();res.sendRedirect(req.getContextPath()+"/login?loggedOut=1");} }
