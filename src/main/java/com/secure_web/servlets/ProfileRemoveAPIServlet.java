package com.secure_web.servlets;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;

@WebServlet("/api/profile/remove")
public class ProfileRemoveAPIServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/multipart-form-data");
        response.setCharacterEncoding("UTF-8");

        String username = request.getAttribute("username") == null ? "admin" : request.getParameter("username");

        if (username == null || username.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Username parameter is required.");
            return;
        }

        try {
            // Load environment variables for database credentials
            Dotenv dotenv = Dotenv.load();
            String dbUrl = dotenv.get("DB_URL");
            String dbUsername = dotenv.get("DB_USERNAME");
            String dbPassword = dotenv.get("DB_PASSWORD");

            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish a connection to the database
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
                // SQL query to update the profile picture to default
                String sql = "UPDATE users SET profile_picture = ? WHERE username = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, "default.jpg");  // Set the profile picture to the default
                    stmt.setString(2, username);  // Set the username for which we are deleting the profile picture

                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        // Successfully updated the profile picture
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println("Profile picture reset was successful.");
                    } else {
                        // No matching user found
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.getWriter().println("Error: User not found.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Database error. Please try again later.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error processing the request.");
        }
    }
}
