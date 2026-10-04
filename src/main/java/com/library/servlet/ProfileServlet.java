package com.library.servlet;
import com.library.model.User;
import com.library.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/app/profile") public class ProfileServlet extends BaseServlet {
 @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{try{q.setAttribute("profile",service(UserService.class).getById((long)q.getSession().getAttribute("userId")));q.getRequestDispatcher("/WEB-INF/views/student/profile.jsp").forward(q,s);}catch(RuntimeException e){error(q,s,e);}}
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{try{User u=service(UserService.class).getById((long)q.getSession().getAttribute("userId"));u.setName(q.getParameter("name"));u.setEmail(q.getParameter("email"));u.setPhone(q.getParameter("phone"));service(UserService.class).update(u);q.getSession().setAttribute("userName",u.getName());flash(q,"Profile saved.");s.sendRedirect(q.getContextPath()+"/app/profile");}catch(RuntimeException e){try{error(q,s,e);}catch(Exception ignored){s.sendError(500);}}}
}
