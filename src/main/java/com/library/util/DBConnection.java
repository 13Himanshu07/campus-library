package com.library.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Single process-wide JDBC pool. DAO callers keep using getConnection() and close it normally. */
public final class DBConnection {
    private static final Logger LOG = LoggerFactory.getLogger(DBConnection.class);
    private static volatile HikariDataSource dataSource;
    private DBConnection() { }

    public static Connection getConnection() throws SQLException { return pool().getConnection(); }

    public static synchronized void initialize() {
        if (dataSource != null && !dataSource.isClosed()) return;
        String url = setting("DB_URL", "jdbc:mysql://localhost:3306/library_management?serverTimezone=UTC");
        String user = setting("DB_USERNAME", "library_app");
        initialize(url,user,setting("DB_PASSWORD", ""));
    }

    public static synchronized void initialize(String url,String user,String password) {
        if (dataSource != null && !dataSource.isClosed()) return;
        validateProductionConfiguration(url,user);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        int maxPoolSize = positiveInteger("DB_POOL_MAX_SIZE", 10);
        int minIdle = nonNegativeInteger("DB_POOL_MIN_IDLE", 2);
        long connectionTimeout = positiveLong("DB_POOL_CONNECTION_TIMEOUT_MS", 30000);
        long idleTimeout = nonNegativeLong("DB_POOL_IDLE_TIMEOUT_MS", 600000);
        long maxLifetime = nonNegativeLong("DB_POOL_MAX_LIFETIME_MS", 1800000);
        String poolName = setting("DB_POOL_NAME", "CampusLibraryPool");
        validatePoolConfiguration(maxPoolSize, minIdle, connectionTimeout, idleTimeout, maxLifetime, poolName);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setPoolName(poolName);
        config.setInitializationFailTimeout(positiveInteger("DB_POOL_INIT_FAIL_TIMEOUT_MS", 10000));
        try {
            dataSource = new HikariDataSource(config);
        } catch (RuntimeException e) {
            LOG.error("db_pool_initialization_failed exception_type=\"{}\"", LogFields.quoted(e.getClass().getSimpleName()));
            throw e;
        }
    }

    public static synchronized void shutdown() {
        if (dataSource != null) { dataSource.close(); dataSource = null; }
    }

    public static void validateProductionConfiguration(String url,String user) {
        validateProductionConfiguration("production".equalsIgnoreCase(setting("APP_ENV", "development")),url,user);
    }
    public static void validateProductionConfiguration(boolean production,String url,String user) {
        if (!production) return;
        if ("root".equalsIgnoreCase(user)) throw new IllegalStateException("Production database user must not be root.");
        if (!hasVerifiedTls(url))
            throw new IllegalStateException("Production DB_URL must configure TLS certificate verification.");
    }

    private static boolean hasVerifiedTls(String url) {
        if (url == null) return false;
        int queryStart = url.indexOf('?');
        if (queryStart < 0) return false;
        String sslMode = null;
        boolean explicitlyDisabled = false;
        for (String parameter : url.substring(queryStart + 1).split("[&#]")) {
            String[] pair = parameter.split("=", 2);
            String key = pair[0].trim();
            String value = pair.length == 2 ? pair[1].trim() : "";
            if ("sslMode".equalsIgnoreCase(key)) sslMode = value;
            if ("useSSL".equalsIgnoreCase(key) && "false".equalsIgnoreCase(value)) explicitlyDisabled = true;
        }
        return !explicitlyDisabled && ("VERIFY_CA".equalsIgnoreCase(sslMode) || "VERIFY_IDENTITY".equalsIgnoreCase(sslMode));
    }

    static void validatePoolConfiguration(int maximumPoolSize, int minimumIdle,
                                           long connectionTimeoutMs, long idleTimeoutMs,
                                           long maximumLifetimeMs, String poolName) {
        if (maximumPoolSize < 1) throw new IllegalArgumentException("DB_POOL_MAX_SIZE must be at least 1.");
        if (minimumIdle < 0 || minimumIdle > maximumPoolSize)
            throw new IllegalArgumentException("DB_POOL_MIN_IDLE must be between 0 and DB_POOL_MAX_SIZE.");
        if (connectionTimeoutMs < 250)
            throw new IllegalArgumentException("DB_POOL_CONNECTION_TIMEOUT_MS must be at least 250.");
        if (idleTimeoutMs < 0) throw new IllegalArgumentException("DB_POOL_IDLE_TIMEOUT_MS cannot be negative.");
        if (maximumLifetimeMs < 0) throw new IllegalArgumentException("DB_POOL_MAX_LIFETIME_MS cannot be negative.");
        if (poolName == null || poolName.isBlank()) throw new IllegalArgumentException("DB_POOL_NAME must not be blank.");
    }

    public static HikariDataSource getDataSource() { return pool(); }

    private static HikariDataSource pool() {
        HikariDataSource current = dataSource;
        if (current == null || current.isClosed()) {
            synchronized (DBConnection.class) {
                current = dataSource;
                if (current == null || current.isClosed()) { initialize(); current = dataSource; }
            }
        }
        return current;
    }
    private static String setting(String name, String fallback) { String value=System.getenv(name); return value==null||value.isBlank()?fallback:value; }
    private static int positiveInteger(String name, int fallback) {
        long value = positiveLong(name, fallback);
        if (value > Integer.MAX_VALUE) throw new IllegalStateException(name + " exceeds the supported range.");
        return (int) value;
    }
    private static int nonNegativeInteger(String name, int fallback) {
        long value = nonNegativeLong(name, fallback);
        if (value > Integer.MAX_VALUE) throw new IllegalStateException(name + " exceeds the supported range.");
        return (int) value;
    }
    private static long positiveLong(String name, long fallback) {
        try { long value=Long.parseLong(setting(name,Long.toString(fallback))); if(value<=0) throw new NumberFormatException(); return value; }
        catch(NumberFormatException e) { throw new IllegalStateException(name+" must be a positive integer.",e); }
    }
    private static long nonNegativeLong(String name, long fallback) {
        try { long value=Long.parseLong(setting(name,Long.toString(fallback))); if(value<0) throw new NumberFormatException(); return value; }
        catch(NumberFormatException e) { throw new IllegalStateException(name+" must be a non-negative integer.",e); }
    }
}
