package com.secure_web.servlets;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class LoginServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(LoginServlet.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_TIME = 15 * 60 * 1000;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe"); // Checkbox value

        try {
            Dotenv dotenv = Dotenv.load();
            String dbUrl = dotenv.get("DB_URL");
            String dbUsername = dotenv.get("DB_USERNAME");
            String dbPassword = dotenv.get("DB_PASSWORD");

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

            if (isUserLockedOut(conn, username)) {
                logger.warn("User {} is locked out.", username);
                response.getWriter().println("Your account is locked. Please try again later.");
                return;
            }

            String sql = "SELECT username, role FROM Users WHERE username = ? AND password = SHA2(?, 256)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String email = "ummanmemmedov2005@gmail.com";
                String role = rs.getString("role");

                // Generate OTP
                String otp = generateOTP();
                sendEmail(otp, email);

                resetFailedAttempts(conn, username);

                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                session = request.getSession(true);
                session.setAttribute("username", username);
                session.setAttribute("role", role);
                session.setAttribute("otp", otp);
                session.setMaxInactiveInterval(5 * 60);

                if ("on".equals(rememberMe)) {
                    String authToken = UUID.randomUUID().toString();
                    storeAuthToken(conn, username, authToken); // Save token to DB

                    Cookie userCookie = new Cookie("authToken", authToken);
                    userCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                    userCookie.setHttpOnly(true);
                    response.addCookie(userCookie);
                }
                logger.info("OTP sent to {}", username);
                response.sendRedirect("verifyOtp.jsp");
            } else {
                logger.warn("Failed login attempt for username: {}", username);
                incrementFailedAttempts(conn, username);
                response.getWriter().println("Invalid credentials.");
            }
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Error during login process.", e);
            response.getWriter().println("An error occurred.");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }



    private boolean isUserLockedOut(Connection conn, String username) throws SQLException {
        String sql = "SELECT failed_attempts, last_failed_attempt FROM LoginAttempts WHERE username = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            int failedAttempts = rs.getInt("failed_attempts");
            long lastFailedAttempt = rs.getLong("last_failed_attempt");
            long currentTime = System.currentTimeMillis();

            return failedAttempts >= MAX_FAILED_ATTEMPTS && (currentTime - lastFailedAttempt) < LOCKOUT_TIME;
        }
        return false;
    }

    private void incrementFailedAttempts(Connection conn, String username) throws SQLException {
        String sql = "INSERT INTO LoginAttempts (username, failed_attempts, last_failed_attempt) VALUES (?, 1, ?) " +
                "ON DUPLICATE KEY UPDATE failed_attempts = failed_attempts + 1, last_failed_attempt = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setLong(2, System.currentTimeMillis());
        stmt.setLong(3, System.currentTimeMillis());
        stmt.executeUpdate();
    }

    private void resetFailedAttempts(Connection conn, String username) throws SQLException {
        String sql = "UPDATE LoginAttempts SET failed_attempts = 0 WHERE username = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.executeUpdate();
    }

    private void storeAuthToken(Connection conn, String username, String authToken) throws SQLException {
        String sql = "UPDATE Users SET auth_token = ? WHERE username = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, authToken);
        stmt.setString(2, username);
        stmt.executeUpdate();
    }

    private String generateOTP() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000)); // Generate 6-digit OTP
    }

//    private void sendEmail(String otp, String recipientEmail) throws MessagingException {
//        Dotenv dotenv = Dotenv.load();
//        String host = "smtp-relay.brevo.com";
//        String port = "587";
//        String senderEmail = dotenv.get("EMAIL_USERNAME");
//        String senderPassword = dotenv.get("EMAIL_PASSWORD");
//
//        Properties properties = new Properties();
//        properties.put("mail.smtp.auth", "true");
//        properties.put("mail.smtp.starttls.enable", "true");
//        properties.put("mail.smtp.host", host);
//        properties.put("mail.smtp.port", port);
//
//        Session session = Session.getInstance(properties, new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(senderEmail, senderPassword);
//            }
//        });
//
//        Message message = new MimeMessage(session);
//        message.setFrom(new InternetAddress(senderEmail));
//        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
//        message.setSubject("Your OTP Code");
//        message.setText("Your OTP code is: " + otp);
//
//        Transport.send(message);
//        logger.info("OTP sent to {}", recipientEmail);
//    }

    private void sendEmail(String otp, String recipientEmail) throws MessagingException {
        Dotenv dotenv = Dotenv.load();
        String host = "smtp.gmail.com"; // Gmail's SMTP server
        String port = "587"; // SMTP port for TLS
        String senderEmail = dotenv.get("GMAIL_USERNAME"); // Update environment variable
        String senderPassword = dotenv.get("GMAIL_PASSWORD"); // Update environment variable

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        // Create session with Gmail SMTP server
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        // Compose email
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + otp);

        // Send email
        Transport.send(message);
        logger.info("OTP sent to {}", recipientEmail);
    }
}
