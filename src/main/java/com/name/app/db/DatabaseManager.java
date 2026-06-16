package com.name.app.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    // File-based H2 DB — stored at ~/myapp-db/data (outside the project)
    private static final String DB_URL = "jdbc:h2:~/myapp-db/data;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    static {
        try {
            Class.forName("org.h2.Driver");  // explicitly load the driver
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("H2 Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void main(String[] args) throws  Exception{
        DatabaseManager.getConnection();
    }
}
