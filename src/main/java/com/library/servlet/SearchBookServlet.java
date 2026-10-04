package com.library.servlet;
import com.library.service.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/app/search") public class SearchBookServlet extends BaseServlet {
 @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{try{String term=q.getParameter("q");q.setAttribute("books",service(BookService.class).search(term));q.setAttribute("categories",service(CategoryService.class).all());q.setAttribute("query",term==null?"":term);takeFlash(q);q.getRequestDispatcher("/WEB-INF/views/student/books.jsp").forward(q,s);}catch(RuntimeException e){error(q,s,e);}}
}
