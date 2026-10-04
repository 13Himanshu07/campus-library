package com.library.servlet;

import com.library.service.UserService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/app/password")
public class PasswordServlet extends BaseServlet {
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response)throws IOException{
        try{long userId=(long)request.getSession().getAttribute("userId");service(UserService.class).changePassword(userId,request.getParameter("currentPassword"),request.getParameter("newPassword"),request.getParameter("confirmPassword"));flash(request,"Password updated.");response.sendRedirect(request.getContextPath()+"/app/profile");}
        catch(RuntimeException e){try{error(request,response,e);}catch(Exception ignored){response.sendError(500);}}
    }
}
