package com.secure_web.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import io.github.cdimascio.dotenv.Dotenv;

public class WelcomeServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Ensure the user is logged in
        HttpSession session = request.getSession(false);
        // Check if session exists; if not, check for cookies
        if (session == null || session.getAttribute("username") == null) {
            Cookie[] cookies = request.getCookies();
            String username = null;

            // Check if the "username" cookie exists
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("username".equals(cookie.getName())) {
                        username = cookie.getValue();
                        break;  // If cookie found, exit the loop
                    }
                }
            }

            // If no username cookie is found, redirect to login
            if (username == null) {
                response.sendRedirect("login.jsp");
                return;
            }

            // Set session attributes from cookie
            session = request.getSession(true);  // Create a new session if it doesn't exist
            session.setAttribute("username", username);

        }

        String username = (String) session.getAttribute("username");
        String profilePicture = "profile_pics/default.jpg"; // Default profile picture
        String userRole = "user"; // Default role
        ArrayList<Map<String, String>> userProfiles = new ArrayList<>(); // For admins and moderators

        try {
            // Load environment variables for database credentials
            Dotenv dotenv = Dotenv.load();
            String dbUrl = dotenv.get("DB_URL");
            String dbUsername = dotenv.get("DB_USERNAME");
            String dbPassword = dotenv.get("DB_PASSWORD");

            // Establish database connection
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
                // Query to get user details
                String userQuery = "SELECT profile_picture, role FROM users WHERE username = ?";
                PreparedStatement userStmt = conn.prepareStatement(userQuery);
                userStmt.setString(1, username);
                ResultSet userRs = userStmt.executeQuery();

                if (userRs.next()) {
                    profilePicture = userRs.getString("profile_picture");
                    userRole = userRs.getString("role");

                    if (profilePicture == null || profilePicture.isEmpty()) {
                        profilePicture = "profile_pics/default.jpg"; // Fallback to default
                    } else {
                        profilePicture = "profile_pics/" + profilePicture; // Prepend 'profile_pics/' to the image file name
                    }
                }

                // If user is an admin or moderator, fetch all user profiles
                if ("moderator".equalsIgnoreCase(userRole) || "admin".equalsIgnoreCase(userRole)) {
                    String allUsersQuery = "SELECT username, profile_picture FROM users";
                    PreparedStatement allUsersStmt = conn.prepareStatement(allUsersQuery);
                    ResultSet allUsersRs = allUsersStmt.executeQuery();

                    while (allUsersRs.next()) {
                        Map<String, String> userProfile = new HashMap<>();
                        userProfile.put("username", allUsersRs.getString("username"));
                        String pic = allUsersRs.getString("profile_picture");
                        userProfile.put("profilePicture", pic != null && !pic.isEmpty() ? "profile_pics/" + pic : "profile_pics/default.jpg");
                        userProfiles.add(userProfile);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error processing request. Please try again later.");
            return;
        }

        // Set attributes for JSP
        request.setAttribute("username", username);
        request.setAttribute("profilePicture", profilePicture);

        if ("moderator".equalsIgnoreCase(userRole) || "admin".equalsIgnoreCase(userRole)) {
            request.setAttribute("userProfiles", userProfiles);
            // Forward to the appropriate JSP based on the role
            if ("admin".equalsIgnoreCase(userRole)) {
                RequestDispatcher dispatcher = request.getRequestDispatcher("welcomeAdmin.jsp");
                dispatcher.forward(request, response);
            } else {
                RequestDispatcher dispatcher = request.getRequestDispatcher("welcomeModerator.jsp");
                dispatcher.forward(request, response);
            }
        } else {
            RequestDispatcher dispatcher = request.getRequestDispatcher("welcomeUser.jsp");
            dispatcher.forward(request, response);
        }
    }
}
