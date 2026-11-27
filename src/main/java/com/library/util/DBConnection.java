package com.library.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/** Single process-wide JDBC pool. DAO callers keep using getConnection() and close it normally. */
public final class DBConnection {
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
        config.setMaximumPoolSize(integer("DB_POOL_MAX_SIZE", 10));
        config.setMinimumIdle(integer("DB_POOL_MIN_IDLE", 2));
        config.setConnectionTimeout(integer("DB_POOL_CONNECTION_TIMEOUT_MS", 30000));
        config.setIdleTimeout(integer("DB_POOL_IDLE_TIMEOUT_MS", 600000));
        config.setMaxLifetime(integer("DB_POOL_MAX_LIFETIME_MS", 1800000));
        config.setPoolName(setting("DB_POOL_NAME", "CampusLibraryPool"));
        config.setInitializationFailTimeout(integer("DB_POOL_INIT_FAIL_TIMEOUT_MS", 10000));
        dataSource = new HikariDataSource(config);
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
        String normalized=url.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("usessl=false") || normalized.contains("sslmode=disable"))
            throw new IllegalStateException("Production DB_URL must enable MySQL TLS and certificate verification.");
        if (!normalized.contains("sslmode=verify"))
            throw new IllegalStateException("Production DB_URL must configure TLS certificate verification.");
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
    private static int integer(String name, int fallback) {
        try { int value=Integer.parseInt(setting(name,Integer.toString(fallback))); if(value<=0) throw new NumberFormatException(); return value; }
        catch(NumberFormatException e) { throw new IllegalStateException(name+" must be a positive integer.",e); }
    }
}
