package com.secure_web.servlets;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;

@WebServlet("/messageboard")
public class MessagePanelServlet extends HttpServlet {

    // Handles the GET request to display the message board and search functionality
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String keyword = request.getParameter("keyword");
        String sql = "SELECT username, message, date_posted FROM messages";
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += " WHERE message LIKE ?";
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html><body>");
        out.println("<h1>Message Board</h1>");

        // Search form
        out.println("<form method='get'>");
        out.println("Search: <input type='text' name='keyword'><button type='submit'>Search</button>");
        out.println("</form>");

        out.println("<h2>Messages</h2>");
        out.println("<ul>");

        // Secure database connection using environment variables
        try {
            Dotenv dotenv = Dotenv.load();  // Load environment variables from .env file
            String dbUrl = dotenv.get("DB_URL");
            String dbUsername = dotenv.get("DB_USERNAME");
            String dbPassword = dotenv.get("DB_PASSWORD");
            // Insecure Version (Only the changed part is commented out):
            // ====================================================
            //     // Insecure: Hardcoded database credentials
            //     String dbUrl = "jdbc:mysql://localhost:3306/secureweb";
            //     String dbUsername = "root";
            //     String dbPassword = "root";
            // ====================================================

            try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                if (keyword != null && !keyword.trim().isEmpty()) {
                    stmt.setString(1, "%" + keyword + "%");
                }

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String username = rs.getString("username");
                    String message = rs.getString("message");
                    String timestamp = rs.getString("date_posted");

                    // Display each message
                    out.println("<li><strong>" + username + "</strong>: " + message + " <em>(" + timestamp + ")</em></li>");
                }
            } catch (SQLException e) {
                throw new ServletException(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error loading environment variables.");
            return;
        }

        out.println("</ul>");

        // Post message form
        out.println("<h2>Post a Message</h2>");
        out.println("<form method='post'>");
        out.println("Message: <textarea name='message' required></textarea>");
        out.println("<button type='submit'>Post</button>");
        out.println("</form>");

        out.println("</body></html>");
    }

    // Handles the POST request to add a new message
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        String message = request.getParameter("message");

        try {
            Dotenv dotenv = Dotenv.load();  // Load environment variables from .env file
            String dbUrl = dotenv.get("DB_URL");
            String dbUsername = dotenv.get("DB_USERNAME");
            String dbPassword = dotenv.get("DB_PASSWORD");
            // Insecure Version (Only the changed part is commented out):
            // ====================================================
            //     // Insecure: Hardcoded database credentials
            //     String dbUrl = "jdbc:mysql://localhost:3306/secureweb";
            //     String dbUsername = "root";
            //     String dbPassword = "root";
            // ====================================================

            try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
                String sql = "INSERT INTO messages (username, message) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                // Sanitize the message before storing it in the database to prevent xss
                PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);
                String sanitizedMessage = policy.sanitize(message);
                stmt.setString(2, sanitizedMessage);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new ServletException(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error loading environment variables.");
            return;
        }

        // After posting, redirect back to the message board to show the new message
        response.sendRedirect("messageboard");
    }
}
