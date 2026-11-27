package com.library;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class DeploymentSecurityConfigTest {
    @Test void composeRequiresProductionTlsAndDedicatedCredentials() throws Exception {
        String compose=Files.readString(Path.of("deployment/oracle/compose.yaml"));
        assertTrue(compose.contains("APP_ENV: production"));
        assertTrue(compose.contains("PUBLIC_HTTPS_HOST:"));
        assertTrue(compose.contains("sslMode=VERIFY_CA"));
        assertTrue(compose.contains("DB_MIGRATION_USERNAME: library_migrator"));
        assertTrue(compose.contains("DB_USERNAME: library_app"));
        assertTrue(compose.contains("--require-secure-transport=ON"));
        assertTrue(compose.contains("MYSQL_TLS_CA_FILE:"));
        assertFalse(compose.contains("useSSL=false"));
        assertFalse(compose.contains("MYSQL_USER:"));
        String database = section(compose, "  database:", "  api:");
        String api = section(compose, "  api:", "  proxy:");
        assertFalse(database.contains("\n    ports:"), "MySQL must not publish a host port");
        assertFalse(api.contains("\n    ports:"), "Tomcat must not publish a host port");
        assertTrue(compose.contains("TRUST_PROXY_TLS: \"true\""));
        assertTrue(Files.readString(Path.of("deployment/oracle/Caddyfile")).contains("reverse_proxy api:8080"));
        assertTrue(Files.readString(Path.of("deployment/oracle/Caddyfile")).contains("header_up X-Forwarded-Proto"));
        assertTrue(Files.readString(Path.of("deployment/oracle/entrypoint.sh")).contains("-importcert"));
        String envExample = Files.readString(Path.of("deployment/oracle/.env.example"));
        assertTrue(envExample.contains("DB_MIGRATION_PASSWORD="));
        assertTrue(envExample.contains("MYSQL_ROOT_PASSWORD="));
        assertTrue(envExample.contains("DB_PASSWORD="));
    }

    private String section(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        int endIndex = text.indexOf(end, startIndex + start.length());
        assertTrue(startIndex >= 0 && endIndex > startIndex);
        return text.substring(startIndex, endIndex);
    }
}
