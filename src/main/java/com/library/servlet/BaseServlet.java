package com.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.library.util.LogFields;

public abstract class BaseServlet extends HttpServlet {
    private static final Logger LOG=LoggerFactory.getLogger(BaseServlet.class);
    protected <T> T service(Class<T> type){return type.cast(getServletContext().getAttribute(type.getName()));}
    protected void error(HttpServletRequest request,HttpServletResponse response,RuntimeException ex)throws ServletException,IOException{
        LOG.warn("request_failed path={} exception_type={}",LogFields.quoted(request.getRequestURI()),LogFields.quoted(ex.getClass().getSimpleName()));
        String message=ex instanceof com.library.exception.ValidationException||ex instanceof IllegalArgumentException||ex instanceof com.library.exception.AuthenticationException||ex instanceof com.library.exception.BookNotAvailableException?ex.getMessage():"The request could not be completed. Please try again.";
        request.setAttribute("errorMessage",message);
        request.getRequestDispatcher("/WEB-INF/views/error/error.jsp").forward(request,response);
    }
    protected long paramId(HttpServletRequest request,String name){return com.library.util.ValidationUtil.id(request.getParameter(name),name);}
    protected void flash(HttpServletRequest request,String message){request.getSession().setAttribute("flashMessage",message);}
    protected void takeFlash(HttpServletRequest request){HttpSession session=request.getSession(false);if(session!=null){request.setAttribute("flashMessage",session.getAttribute("flashMessage"));session.removeAttribute("flashMessage");}}
}
