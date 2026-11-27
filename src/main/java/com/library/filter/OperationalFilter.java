package com.library.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** HTTPS enforcement at the application boundary plus request timing without query strings or identities. */
@WebFilter("/*")
public class OperationalFilter implements Filter {
    private static final Logger LOG=LoggerFactory.getLogger(OperationalFilter.class);
    @Override public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException,ServletException {
        HttpServletRequest req=(HttpServletRequest)request; HttpServletResponse res=(HttpServletResponse)response;
        boolean production="production".equalsIgnoreCase(setting("APP_ENV","development"));
        boolean secure=isSecure(req);
        applySecurityHeaders(res,production&&secure);
        if(production&&!secure) {
            String port=setting("HTTPS_PORT","");
            String host=setting("PUBLIC_HTTPS_HOST",req.getServerName())+(port.isBlank()||"443".equals(port)?"":":"+port);
            res.setStatus(308); res.setHeader("Location","https://"+host+req.getRequestURI()+(req.getQueryString()==null?"":"?"+req.getQueryString())); return;
        }
        long start=System.nanoTime(); int status=500;
        try { chain.doFilter(request,response); status=res.getStatus(); }
        catch(IOException|ServletException|RuntimeException e) { LOG.error("http_request_failed method={} path={} exception={}",req.getMethod(),req.getRequestURI(),e.getClass().getSimpleName()); throw e; }
        finally { long ms=(System.nanoTime()-start)/1_000_000; LOG.info("http_request method={} path={} status={} duration_ms={}",req.getMethod(),req.getRequestURI(),status,ms); }
    }
    static void applySecurityHeaders(HttpServletResponse response,boolean productionHttps) {
        securityHeaders(productionHttps).forEach(response::setHeader);
    }
    static Map<String,String> securityHeaders(boolean productionHttps) {
        Map<String,String> h=new LinkedHashMap<>();
        h.put("Content-Security-Policy","default-src 'self'; base-uri 'self'; object-src 'none'; frame-ancestors 'none'; form-action 'self'; img-src 'self' data:; style-src 'self' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; script-src 'self'");
        h.put("X-Content-Type-Options","nosniff");h.put("Referrer-Policy","strict-origin-when-cross-origin");
        if(productionHttps)h.put("Strict-Transport-Security","max-age=31536000");
        return Map.copyOf(h);
    }
    private boolean isSecure(HttpServletRequest req) {
        if(req.isSecure()) return true;
        return Boolean.parseBoolean(setting("TRUST_PROXY_TLS","false"))&&"https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
    }
    private String setting(String name,String fallback){String v=System.getenv(name);return v==null||v.isBlank()?fallback:v;}
}

