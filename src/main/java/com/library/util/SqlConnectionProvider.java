package com.library.util;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlConnectionProvider {
    Connection getConnection() throws SQLException;
}
