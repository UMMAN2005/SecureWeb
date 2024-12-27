package com.secure_web.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class LogoutServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the current session, if it exists
        HttpSession session = request.getSession(false); // Don't create a new session if it doesn't exist

        // Problematic code: Session fixation vulnerability.
        // The session ID remains the same after logout, allowing attackers to hijack the session.
        // session.invalidate();  // Invalidate the session

        if (session != null) {
            // Invalidate the session to clear all session attributes
            session.invalidate(); // Invalidate the session
        }

        // Create a new session to avoid session fixation (important for security)
        session = request.getSession(true); // Create a new session for the user

        // Optionally, set secure cookie flags for the new session (if you are using cookies explicitly)
        Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
        sessionCookie.setHttpOnly(true);  // Prevent client-side access to the cookie
        sessionCookie.setSecure(true);    // Only sent over HTTPS
        sessionCookie.setPath("/");       // Path for the cookie
        response.addCookie(sessionCookie); // Add the cookie to the response

        // Redirect the user to the login page after logout
        response.sendRedirect("login.jsp"); // Redirect to the login page after logout
    }
}
