package com.library.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

@WebFilter(urlPatterns={"/app/*","/librarian/*","/admin/*","/logout"})
public class CsrfFilter implements Filter {
    private final SecureRandom random=new SecureRandom();
    @Override public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException{
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        res.setContentType("text/html; charset=UTF-8");
        HttpServletRequest request=(HttpServletRequest)req;HttpServletResponse response=(HttpServletResponse)res;HttpSession session=request.getSession();
        String token=(String)session.getAttribute("csrfToken");if(token==null){byte[] bytes=new byte[32];random.nextBytes(bytes);token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);session.setAttribute("csrfToken",token);}
        if("POST".equalsIgnoreCase(request.getMethod())&&!java.security.MessageDigest.isEqual(token.getBytes(java.nio.charset.StandardCharsets.UTF_8),String.valueOf(request.getParameter("csrfToken")).getBytes(java.nio.charset.StandardCharsets.UTF_8))){response.sendError(HttpServletResponse.SC_FORBIDDEN,"Invalid form token. Reload and try again.");return;}
        chain.doFilter(req,res);
    }
}
