package com.secure_web.servlets;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;
import java.sql.*;

public class ProfileAddServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(ProfileAddServlet.class);
    private static final String UPLOAD_DIR = "profile_pics";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Ensure the user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Get the uploaded file
        Part filePart = request.getPart("profile_picture");
        if (filePart == null || filePart.getSize() == 0) {
            response.getWriter().println("No file uploaded. Please select a file.");
            logger.warn("No file uploaded for username: {}", session.getAttribute("username"));
            return;
        }

        // Check file size
        if (filePart.getSize() > MAX_FILE_SIZE) {
            response.getWriter().println("File size exceeds the limit of 5MB.");
            logger.warn("File size exceeded limit for username: {}. File size: {}", session.getAttribute("username"), filePart.getSize());
            return;
        }

        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

        // validate file type (only .jpg, .jpeg, .png)
        if (!fileName.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
            response.getWriter().println("Invalid file type. Only .png, .jpg, and .jpeg are allowed.");
            logger.warn("Invalid file type uploaded by user: {}", session.getAttribute("username"));
            return;
        }

        // Scan the uploaded file for malware
        if (scanForMalware(filePart)) {
            response.getWriter().println("File contains malware. Please upload a clean file.");
            logger.warn("Malware detected in file uploaded by user: {}", session.getAttribute("username"));
            return;
        }

        // Get the username from session
        String username = (String) session.getAttribute("username");

        // Generate a random unique file name for the new profile picture
        String newFileName = UUID.randomUUID() + fileName.substring(fileName.lastIndexOf('.'));

        // Prepare the file path to save the uploaded image
        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdir(); // Create directory if it doesn't exist

        File newFile = new File(uploadDir, newFileName);
        filePart.write(newFile.getAbsolutePath());

        // Log the successful upload
        logger.info("User {} uploaded a new profile picture: {}", username, newFileName);

        // Secure Version: Using environment variables for database credentials
        try {
            Dotenv dotenv = Dotenv.load();  // Load the .env file securely
            String dbUrl = dotenv.get("DB_URL");
            String dbUsername = dotenv.get("DB_USERNAME");
            String dbPassword = dotenv.get("DB_PASSWORD");

            // Secure database connection
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
                // Retrieve the current profile picture from the database
                String currentPicture = null;
                String selectQuery = "SELECT profile_picture FROM users WHERE username = ?";
                try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                    selectStmt.setString(1, username);
                    ResultSet rs = selectStmt.executeQuery();
                    if (rs.next()) {
                        currentPicture = rs.getString("profile_picture");
                    }
                }

                // Update the profile picture in the database
                String updateQuery = "UPDATE users SET profile_picture = ? WHERE username = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, newFileName);
                    updateStmt.setString(2, username);
                    updateStmt.executeUpdate();
                }

                // Delete the old profile picture from the server if it's not the default
                if (currentPicture != null && !currentPicture.equals("default.jpg")) {
                    File oldFile = new File(uploadDir, currentPicture);
                    if (oldFile.exists()) oldFile.delete();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                response.getWriter().println("Error updating profile picture in the database.");
                logger.error("Error updating profile picture for user {} in the database.", username, e);
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error loading environment variables.");
            logger.error("Error loading environment variables during profile picture upload for user: {}", session.getAttribute("username"), e);
            return;
        }

        // Redirect to the welcome page after successful upload
        response.sendRedirect("welcome");
    }

    private boolean scanForMalware(Part filePart) throws IOException {
        String apiKey = "6cc519a4174088fa74339339cd362529c2825a2a86b73bda12e00c87a29553d3";
        URL url = new URL("https://www.virustotal.com/api/v3/files");

        // Convert the uploaded file to InputStream
        InputStream fileInputStream = filePart.getInputStream();

        // Create connection to VirusTotal API
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("x-apikey", apiKey);
        connection.setDoOutput(true);

        // Send the file to VirusTotal API
        try (OutputStream os = connection.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        // Get the response
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            // Read the response
            try (InputStream responseStream = connection.getInputStream()) {
                String response = new String(responseStream.readAllBytes());
                // Parse response to check if the file is clean or contains malware
                return response.contains("\"malicious\":true");
            }
        }

        return false;
    }
}
