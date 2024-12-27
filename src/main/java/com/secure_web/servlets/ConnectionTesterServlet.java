package com.secure_web.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class ConnectionTesterServlet extends HttpServlet {
    // Whitelisted domains
    private static final List<String> WHITELIST = Arrays.asList(
            "yahoo.com",
            "google.com",
            "yandex.com"
    );

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String urlString = request.getParameter("url");

        try {
            URL url = new URL(urlString);
            String host = url.getHost();

            // Normalize host by removing "www." if present
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            // Check if the normalized domain is in the whitelist
            if (!WHITELIST.contains(host)) {
                response.getWriter().println("URL is not allowed. Please use a whitelisted domain.");
                return;
            }

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int statusCode = connection.getResponseCode();

            response.getWriter().println("Connection successful! Status code: " + statusCode);
        } catch (Exception e) {
            response.getWriter().println("Error connecting to the URL: " + e.getMessage());
        }
    }
}
