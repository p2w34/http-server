package com.pbroda.selfdevelopment.httpserver.db;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Singleton
public class DatabaseConnectionFactory {

    private final String jdbcUrl;

    @Inject
    DatabaseConnectionFactory(@Named("JDBC URL") String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

}
