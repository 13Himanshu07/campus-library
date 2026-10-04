package com.library.servlet;
import com.library.service.IssueService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/librarian/issues") public class LibrarianIssueListServlet extends BaseServlet {
 @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{try{q.setAttribute("issues",service(IssueService.class).all());q.setAttribute("overdue",service(IssueService.class).overdue());q.getRequestDispatcher("/WEB-INF/views/librarian/issues.jsp").forward(q,s);}catch(RuntimeException e){error(q,s,e);}}
}
