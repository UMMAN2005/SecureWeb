package com.secure_web.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;

public class ProfileRemoveServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Ensure the user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Get the username from the form
        String usernameToDelete = request.getParameter("username");

        try {
            // Load environment variables for database credentials
            Dotenv dotenv = Dotenv.load();
            String dbUrl = dotenv.get("DB_URL");
            String dbUsername = dotenv.get("DB_USERNAME");
            String dbPassword = dotenv.get("DB_PASSWORD");

            // Establish a connection to the database
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
                // SQL query to update the profile picture to default
                String sql = "UPDATE users SET profile_picture = ? WHERE username = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, "default.jpg");  // Set the profile picture to the default
                stmt.setString(2, usernameToDelete);  // Set the username for which we are deleting the profile picture

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    // Profile picture was successfully updated to the default
                    response.sendRedirect("welcomeAdmin");  // Redirect back to the admin page or any other confirmation page
                } else {
                    // Handle the case where no rows were updated
                    response.getWriter().println("Error: User not found.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.getWriter().println("Database error. Please try again later.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error processing the request.");
        }
    }
}
