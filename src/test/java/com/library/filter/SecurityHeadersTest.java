package com.library.filter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SecurityHeadersTest {
    @Test void sendsBrowserSecurityHeadersInAllEnvironments() {
        var headers=OperationalFilter.securityHeaders(false);
        assertEquals("nosniff",headers.get("X-Content-Type-Options"));
        assertEquals("strict-origin-when-cross-origin",headers.get("Referrer-Policy"));
        assertTrue(headers.get("Content-Security-Policy").contains("frame-ancestors 'none'"));
        assertTrue(headers.get("Content-Security-Policy").contains("script-src 'self'"));
        assertFalse(headers.containsKey("Strict-Transport-Security"));
    }
    @Test void sendsHstsOnlyForProductionHttps() {
        assertEquals("max-age=31536000",OperationalFilter.securityHeaders(true).get("Strict-Transport-Security"));
    }
}
