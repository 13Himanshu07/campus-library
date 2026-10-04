package com.library.servlet;
import com.library.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/register") public class RegisterServlet extends BaseServlet {
 @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{q.getRequestDispatcher("/register.jsp").forward(q,s);}
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{try{boolean application="on".equals(q.getParameter("librarianApplication"));service(UserService.class).register(q.getParameter("name"),q.getParameter("email"),q.getParameter("password"),q.getParameter("phone"),q.getParameter("membershipId"),application);q.getSession().setAttribute("flashMessage",application?"Librarian application submitted for administrator approval.":"Your student account is ready. Sign in to continue.");s.sendRedirect(q.getContextPath()+"/login");}catch(RuntimeException e){error(q,s,e);}}
}
