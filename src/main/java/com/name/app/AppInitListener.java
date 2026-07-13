package com.name.app;

import com.name.app.db.DatabaseManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@WebListener
public class AppInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initializing database...");
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS messages (" +
                            "  id    INT AUTO_INCREMENT PRIMARY KEY," +
                            "  text  VARCHAR(255) NOT NULL" +
                            ")"
            );

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS login_history (" +
                            "  id         INT AUTO_INCREMENT PRIMARY KEY," +
                            "  username   VARCHAR(50) NOT NULL," +
                            "  role       VARCHAR(20) NOT NULL," +
                            "  login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                            ")"
            );

            System.out.println("Database ready.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("App shutting down.");
    }
}
