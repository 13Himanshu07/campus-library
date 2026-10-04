package com.library.servlet;

import com.library.exception.AuthenticationException;
import com.library.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;

public class LoginServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{req.getRequestDispatcher("/login.jsp").forward(req,res);}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
        try{var user=service(UserService.class).authenticate(req.getParameter("email"),req.getParameter("password"));HttpSession session=req.getSession(true);req.changeSessionId();session.setAttribute("userId",user.getId());session.setAttribute("role",user.getRole().name());session.setAttribute("userName",user.getName());session.setAttribute("dashboardType",user.getDashboardType());String target="ADMIN".equals(user.getRole().name())?"/admin/dashboard":"/app/dashboard";res.sendRedirect(req.getContextPath()+target);}
        catch(RuntimeException e){if(e instanceof AuthenticationException){req.setAttribute("errorMessage",e.getMessage());req.getRequestDispatcher("/login.jsp").forward(req,res);}else error(req,res,e);}
    }
}
