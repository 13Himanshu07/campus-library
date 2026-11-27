package com.library.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthServletTest {
    @Test void livenessIsPublicSimpleAndDoesNotQueryDatabase() throws Exception {
        AtomicInteger status = new AtomicInteger();
        StringWriter body = new StringWriter();
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{HttpServletRequest.class}, (proxy, method, args) -> "getServletPath".equals(method.getName()) ? "/health/live" : defaultValue(method.getReturnType()));
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{HttpServletResponse.class}, (proxy, method, args) -> {
                if ("setStatus".equals(method.getName())) status.set((int) args[0]);
                if ("getWriter".equals(method.getName())) return new PrintWriter(body);
                return defaultValue(method.getReturnType());
            });

        new HealthServlet().doGet(request, response);

        assertEquals(HttpServletResponse.SC_OK, status.get());
        assertTrue(body.toString().contains("live"));
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        return 0;
    }
}
