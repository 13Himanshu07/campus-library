package com.library.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter(urlPatterns={"/app/*","/librarian/*","/admin/*"})
public class AuthenticationFilter implements Filter {
    @Override public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException{
        HttpServletRequest request=(HttpServletRequest)req;HttpServletResponse response=(HttpServletResponse)res;
        HttpSession session=request.getSession(false);
        if(session==null||session.getAttribute("userId")==null){response.sendRedirect(request.getContextPath()+"/login?next="+java.net.URLEncoder.encode(request.getRequestURI(),java.nio.charset.StandardCharsets.UTF_8));return;}
        chain.doFilter(req,res);
    }
}
