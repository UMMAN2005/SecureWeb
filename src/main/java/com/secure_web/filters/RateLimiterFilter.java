package com.secure_web.filters;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.github.bucket4j.Bandwidth;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiterFilter implements Filter {

    private final ConcurrentHashMap<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
    private final int capacity = 10; // Max requests
    private final int refillTokens = 10; // Refill tokens
    private final Duration refillDuration = Duration.ofMinutes(1); // Refill period

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Optional: Any initialization logic
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String clientIp = request.getRemoteAddr(); // Identify client by IP
        Bucket bucket = ipBuckets.computeIfAbsent(clientIp, k -> createNewBucket());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response); // Proceed if token available
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpResponse.getWriter().write("Rate limit exceeded. Try again later.");
        }
    }

    private Bucket createNewBucket() {
        return Bucket4j.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.greedy(refillTokens, refillDuration)))
                .build();
    }

    @Override
    public void destroy() {
        // Optional: Any cleanup logic
    }
}
