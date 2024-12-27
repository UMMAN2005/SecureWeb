package com.secure_web.servlets;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;
import java.sql.*;

public class ProfileAddAPIServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(ProfileAddAPIServlet.class);
    private static final String UPLOAD_DIR = "profile_pics";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/multipart-form-data");
        response.setCharacterEncoding("UTF-8");

        try {
            // Validate file upload
            Part filePart = request.getPart("profile_picture");
            String username = request.getAttribute("username") == null ? "admin" : request.getParameter("username");

            if (username == null || username.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Username parameter is required.");
                return;
            }

            if (filePart == null || filePart.getSize() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"No file uploaded. Please select a file.\"}");
                return;
            }

            if (filePart.getSize() > MAX_FILE_SIZE) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"File size exceeds the limit of 5MB.\"}");
                return;
            }

            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            if (!fileName.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid file type. Only .png, .jpg, and .jpeg are allowed.\"}");
                return;
            }

            if (scanForMalware(filePart)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"File contains malware.\"}");
                return;
            }

            // Save the file
            String newFileName = UUID.randomUUID() + fileName.substring(fileName.lastIndexOf('.'));
            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdir();

            File newFile = new File(uploadDir, newFileName);
            filePart.write(newFile.getAbsolutePath());

            // Update database
            updateProfilePicture(newFileName, username, uploadDir);

            // Respond with success
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"message\":\"Profile picture uploaded successfully.\"}");

        } catch (Exception e) {
            logger.error("Error processing profile picture upload.", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Internal server error.\"}");
        }
    }

    private boolean scanForMalware(Part filePart) throws IOException {
        String apiKey = "6cc519a4174088fa74339339cd362529c2825a2a86b73bda12e00c87a29553d3";
        URL url = new URL("https://www.virustotal.com/api/v3/files");

        try (InputStream fileInputStream = filePart.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("x-apikey", apiKey);
            connection.setDoOutput(true);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            try (OutputStream os = connection.getOutputStream()) {
                os.write(baos.toByteArray());
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (InputStream responseStream = connection.getInputStream()) {
                    String response = new String(responseStream.readAllBytes());
                    return response.contains("\"malicious\":true");
                }
            }
        }

        return false;
    }

    private void updateProfilePicture(String newFileName, String user, File uploadDir) throws SQLException, ClassNotFoundException {
        Dotenv dotenv = Dotenv.load();
        String dbUrl = dotenv.get("DB_URL");
        String dbUsername = dotenv.get("DB_USERNAME");
        String dbPassword = dotenv.get("DB_PASSWORD");

        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String updateQuery = "UPDATE users SET profile_picture = ? WHERE username = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setString(1, newFileName);
                updateStmt.setString(2, user);
                updateStmt.executeUpdate();
            }
        }
    }
}
