package com.library.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/** HTTPS enforcement at the application boundary plus request timing without query strings or identities. */
@WebFilter("/*")
public class OperationalFilter implements Filter {
    private static final Logger LOG=LoggerFactory.getLogger(OperationalFilter.class);
    @Override public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException,ServletException {
        HttpServletRequest req=(HttpServletRequest)request; HttpServletResponse res=(HttpServletResponse)response;
        if("production".equalsIgnoreCase(setting("APP_ENV","development"))&&!isSecure(req)) {
            String port=setting("HTTPS_PORT","");
            String host=setting("PUBLIC_HTTPS_HOST",req.getServerName())+(port.isBlank()||"443".equals(port)?"":":"+port);
            res.setStatus(308); res.setHeader("Location","https://"+host+req.getRequestURI()+(req.getQueryString()==null?"":"?"+req.getQueryString())); return;
        }
        long start=System.nanoTime(); int status=500;
        try { chain.doFilter(request,response); status=res.getStatus(); }
        catch(IOException|ServletException|RuntimeException e) { LOG.error("http_request_failed method={} path={} exception={}",req.getMethod(),req.getRequestURI(),e.getClass().getSimpleName()); throw e; }
        finally { long ms=(System.nanoTime()-start)/1_000_000; LOG.info("http_request method={} path={} status={} duration_ms={}",req.getMethod(),req.getRequestURI(),status,ms); }
    }
    private boolean isSecure(HttpServletRequest req) {
        if(req.isSecure()) return true;
        return Boolean.parseBoolean(setting("TRUST_PROXY_TLS","false"))&&"https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
    }
    private String setting(String name,String fallback){String v=System.getenv(name);return v==null||v.isBlank()?fallback:v;}
}

