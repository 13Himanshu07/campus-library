package com.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseServlet extends HttpServlet {
    private static final Logger LOG=Logger.getLogger(BaseServlet.class.getName());
    protected <T> T service(Class<T> type){return type.cast(getServletContext().getAttribute(type.getName()));}
    protected void error(HttpServletRequest request,HttpServletResponse response,RuntimeException ex)throws ServletException,IOException{
        LOG.log(Level.WARNING,"Request failed: "+request.getRequestURI(),ex);
        String message=ex instanceof com.library.exception.ValidationException||ex instanceof IllegalArgumentException||ex instanceof com.library.exception.AuthenticationException||ex instanceof com.library.exception.BookNotAvailableException?ex.getMessage():"The request could not be completed. Please try again.";
        request.setAttribute("errorMessage",message);
        request.getRequestDispatcher("/WEB-INF/views/error/error.jsp").forward(request,response);
    }
    protected long paramId(HttpServletRequest request,String name){return com.library.util.ValidationUtil.id(request.getParameter(name),name);}
    protected void flash(HttpServletRequest request,String message){request.getSession().setAttribute("flashMessage",message);}
    protected void takeFlash(HttpServletRequest request){HttpSession session=request.getSession(false);if(session!=null){request.setAttribute("flashMessage",session.getAttribute("flashMessage"));session.removeAttribute("flashMessage");}}
}
