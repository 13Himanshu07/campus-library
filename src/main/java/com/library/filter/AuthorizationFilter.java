package com.library.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter(urlPatterns={"/librarian/*","/admin/*"})
public class AuthorizationFilter implements Filter {
    @Override public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException{
        HttpServletRequest request=(HttpServletRequest)req;HttpServletResponse response=(HttpServletResponse)res;
        HttpSession session=request.getSession(false);String role=session==null?null:(String)session.getAttribute("role");
        boolean admin=request.getRequestURI().contains("/admin/");
        boolean allowed="ADMIN".equals(role)||(!admin&&"LIBRARIAN".equals(role));
        if(!allowed){response.sendError(HttpServletResponse.SC_FORBIDDEN,"You do not have permission to access this page.");return;}
        chain.doFilter(req,res);
    }
}
