package com.library.servlet;
import com.library.service.CategoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/librarian/categories") public class CategoryServlet extends BaseServlet {
 @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{q.setAttribute("categories",service(CategoryService.class).all());q.getRequestDispatcher("/WEB-INF/views/librarian/categories.jsp").forward(q,s);}
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{try{service(CategoryService.class).add(q.getParameter("name"),q.getParameter("description"));flash(q,"Category added.");s.sendRedirect(q.getContextPath()+"/librarian/categories");}catch(RuntimeException e){try{error(q,s,e);}catch(Exception ignored){s.sendError(500);}}}
}
