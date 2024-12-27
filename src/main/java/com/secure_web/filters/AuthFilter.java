package com.secure_web.filters;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization (if necessary)
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Check if the user is logged in by verifying the session attribute
        HttpSession session = httpRequest.getSession(false); // false means no session is created if it doesn't exist
        String username = (session != null) ? (String) session.getAttribute("username") : null;

        // If the user is not authenticated, redirect to the login page
        if (username == null) {
            httpResponse.sendRedirect("login.jsp"); // Redirect to login page if not authenticated
            return;
        }

        // If authenticated, continue processing the request
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup code (if necessary)
    }
}
