package com.library.servlet;

import com.library.util.DBConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/** Minimal public liveness and database readiness endpoints for a load balancer. */
@WebServlet(urlPatterns = {"/health/live", "/health/ready"})
public final class HealthServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(HealthServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        if ("/health/live".equals(request.getServletPath())) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\":\"live\"}");
            return;
        }
        try (var connection = DBConnection.getConnection();
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT 1")) {
            if (!result.next() || result.getInt(1) != 1) throw new SQLException("Readiness query returned no row.");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\":\"ready\"}");
        } catch (SQLException | RuntimeException e) {
            LOG.warn("readiness_check_failed exception_type=\"{}\"", com.library.util.LogFields.quoted(e.getClass().getSimpleName()));
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("{\"status\":\"not_ready\"}");
        }
    }
}
