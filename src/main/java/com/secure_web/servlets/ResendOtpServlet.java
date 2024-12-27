package com.secure_web.servlets;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

public class ResendOtpServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(LoginServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = "ummanmemmedov2005@gmail.com";
        // Generate OTP
        String otp = generateOTP();
        try {
            sendEmail(otp, email);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        session = req.getSession(true);
        session.setAttribute("otp", otp);
        session.setAttribute("username", "admin");

        logger.info("OTP sent to {}", email);
        resp.sendRedirect("verifyOtp.jsp");
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
