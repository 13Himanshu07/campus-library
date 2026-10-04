package com.library.servlet;
import com.library.model.User;
import com.library.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/librarian/users") public class UserListServlet extends BaseServlet {
 @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{try{q.setAttribute("users",service(UserService.class).getStudents());q.getRequestDispatcher("/WEB-INF/views/librarian/users.jsp").forward(q,s);}catch(RuntimeException e){error(q,s,e);}}
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{try{User u=service(UserService.class).getById(paramId(q,"id"));if(u.getRole()!=User.Role.STUDENT)throw new IllegalArgumentException("Only student accounts can be disabled here.");u.setStatus(User.Status.DISABLED);service(UserService.class).update(u);flash(q,"Student account disabled.");s.sendRedirect(q.getContextPath()+"/librarian/users");}catch(RuntimeException e){try{error(q,s,e);}catch(Exception ignored){s.sendError(500);}}}
}
