package com.name.app.auth;

import com.name.app.db.DatabaseManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // Hardcoded credentials -> role
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin";
    private static final String USER_USER = "user";
    private static final String USER_PASS = "user";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // If already logged in, skip straight to home
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String role = authenticate(username, password);

        if (role != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("username", username);
            session.setAttribute("role", role);
            recordLogin(username, role);
            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            request.setAttribute("error", "Invalid username or password");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }

    private String authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        if (ADMIN_USER.equals(username) && ADMIN_PASS.equals(password)) {
            return "admin";
        }
        if (USER_USER.equals(username) && USER_PASS.equals(password)) {
            return "user";
        }
        return null;
    }

    private void recordLogin(String username, String role) {
        String sql = "INSERT INTO login_history (username, role) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, role);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Don't block login if logging fails — just log the error server-side
            e.printStackTrace();
        }
    }
}
