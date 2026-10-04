package com.library.servlet;
import com.library.service.IssueService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/app/my-books") public class MyBooksServlet extends BaseServlet {
 @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{try{long userId=(long)q.getSession().getAttribute("userId");q.setAttribute("issues",service(IssueService.class).activeFor(userId));takeFlash(q);q.getRequestDispatcher("/WEB-INF/views/student/my-books.jsp").forward(q,s);}catch(RuntimeException e){error(q,s,e);}}
}
