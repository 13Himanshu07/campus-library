package com.library.servlet;

import com.library.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/applications")
public class AdminApplicationsServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        request.setAttribute("applications",service(UserService.class).pendingLibrarians());
        takeFlash(request);request.getRequestDispatcher("/WEB-INF/views/admin/applications.jsp").forward(request,response);
    }
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response)throws IOException{
        try{boolean approve="approve".equals(request.getParameter("decision"));service(UserService.class).decideLibrarianApplication(paramId(request,"id"),approve);flash(request,approve?"Librarian account approved.":"Application declined.");response.sendRedirect(request.getContextPath()+"/admin/applications");}
        catch(RuntimeException e){try{error(request,response,e);}catch(Exception ignored){response.sendError(500);}}
    }
}
