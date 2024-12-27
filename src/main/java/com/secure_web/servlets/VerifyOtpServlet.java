package com.secure_web.servlets;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class VerifyOtpServlet extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(VerifyOtpServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.getWriter().println("Session expired. Please log in again.");
            return;
        }

        String inputOtp = request.getParameter("otp");
        String sessionOtp = (String) session.getAttribute("otp");
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        if (inputOtp != null && inputOtp.equals(sessionOtp)) {
            logger.info("OTP verified successfully for user {}", username);

            // Redirect based on user role
            if ("admin".equals(role)) {
                response.sendRedirect("welcomeAdmin");
            } else if ("moderator".equals(role)) {
                response.sendRedirect("welcomeModerator");
            } else {
                response.sendRedirect("welcomeUser");
            }
        } else {
            logger.warn("Invalid OTP for user {}", username);
            response.getWriter().println("Invalid OTP. Please try again.");
        }
    }
}
