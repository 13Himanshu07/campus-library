package com.library.servlet;
import com.library.service.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet(urlPatterns={"/app/dashboard","/librarian/dashboard","/admin/dashboard"}) public class DashboardServlet extends BaseServlet {
 @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{try{long userId=(long)q.getSession().getAttribute("userId");String role=(String)q.getSession().getAttribute("role");q.setAttribute("stats",service(DashboardService.class).statistics());if("STUDENT".equals(role)){q.setAttribute("activeIssues",service(IssueService.class).activeFor(userId));}else{q.setAttribute("recentIssues",service(IssueService.class).all().stream().limit(8).toList());}takeFlash(q);String dashboard=(String)q.getSession().getAttribute("dashboardType");if(dashboard==null)throw new IllegalStateException("Session dashboard type is missing.");q.getRequestDispatcher("/WEB-INF/views/"+dashboard+"/dashboard.jsp").forward(q,s);}catch(RuntimeException e){error(q,s,e);}}
}
