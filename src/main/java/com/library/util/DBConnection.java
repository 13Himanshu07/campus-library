package com.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Central direct-JDBC connection factory; credentials come from the process environment. */
public final class DBConnection {
    private DBConnection() { }
    public static Connection getConnection() throws SQLException {
        String url=setting("DB_URL","jdbc:mysql://localhost:3306/library_management?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true");
        String user=setting("DB_USERNAME","root");
        String password=setting("DB_PASSWORD","");
        return DriverManager.getConnection(url,user,password);
    }
    private static String setting(String key,String fallback){String value=System.getenv(key);return value==null?fallback:value;}
}
