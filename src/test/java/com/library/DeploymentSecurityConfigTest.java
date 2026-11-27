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
        assertFalse(compose.contains("useSSL=false"));
        assertFalse(compose.contains("MYSQL_USER:"));
    }
}
