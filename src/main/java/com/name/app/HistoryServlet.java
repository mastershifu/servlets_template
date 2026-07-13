package com.name.app;

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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/history")
public class HistoryServlet extends HttpServlet {

    public static class LoginRecord {
        public final String username;
        public final String role;
        public final String loginTime;

        public LoginRecord(String username, String role, String loginTime) {
            this.username = username;
            this.role = role;
            this.loginTime = loginTime;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Extra guard on top of AuthFilter: only admins may view this page
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        if (!"admin".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admins only");
            return;
        }

        List<LoginRecord> records = new ArrayList<LoginRecord>();
        String sql = "SELECT username, role, login_time FROM login_history ORDER BY login_time DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                records.add(new LoginRecord(
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("login_time")
                ));
            }
        } catch (SQLException e) {
            throw new ServletException("Failed to load login history", e);
        }

        request.setAttribute("records", records);
        request.getRequestDispatcher("/WEB-INF/views/history.jsp").forward(request, response);
    }
}
