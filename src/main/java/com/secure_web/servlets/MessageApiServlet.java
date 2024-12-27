package com.secure_web.servlets;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/api/messages")
public class MessageApiServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(MessageApiServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Set response type to JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // Parse JSON input
            BufferedReader reader = request.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            JSONObject jsonInput = new JSONObject(jsonBuilder.toString());

            // Extract fields
            String username = jsonInput.optString("username", null);
            String message = jsonInput.optString("message", null);

            // Validate input
            if (username == null || username.isEmpty() || message == null || message.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JSONObject errorResponse = new JSONObject().put("error", "Username and message are required.");
                out.print(errorResponse);
                return;
            }

            // Database connection
            Dotenv dotenv = Dotenv.load();
            String dbUrl = dotenv.get("DB_URL");
            String dbUsername = dotenv.get("DB_USERNAME");
            String dbPassword = dotenv.get("DB_PASSWORD");

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            // Insert message into database
            String sql = "INSERT INTO Messages (username, message) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, message);
            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                JSONObject successResponse = new JSONObject().put("message", "Message posted successfully.");
                out.print(successResponse);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject errorResponse = new JSONObject().put("error", "Failed to post the message.");
                out.print(errorResponse);
            }

        } catch (Exception e) {
            logger.error("Error in message API", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject errorResponse = new JSONObject().put("error", "An error occurred.");
            out.print(errorResponse);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            Dotenv dotenv = Dotenv.load();
            String dbUrl = dotenv.get("DB_URL");
            String dbUsername = dotenv.get("DB_USERNAME");
            String dbPassword = dotenv.get("DB_PASSWORD");

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            String sql = "SELECT username, message, date_posted FROM Messages ORDER BY date_posted DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            JSONArray messages = new JSONArray();
            while (rs.next()) {
                JSONObject message = new JSONObject()
                        .put("username", rs.getString("username"))
                        .put("message", rs.getString("message"))
                        .put("date_posted", rs.getTimestamp("date_posted").toString());
                messages.put(message);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(messages);

        } catch (Exception e) {
            logger.error("Error retrieving messages", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject errorResponse = new JSONObject().put("error", "An error occurred.");
            out.print(errorResponse);
        }
    }

}
