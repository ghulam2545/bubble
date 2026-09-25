package com.ghulam.nova.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Logs every inbound HTTP request with method, URL, status code, and duration.
 * Skips static assets (js, css, images) to keep the console clean.
 */
@Slf4j
@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Skip noisy static-asset requests
        if (isStaticAsset(uri)) {
            chain.doFilter(req, res);
            return;
        }

        long start = System.currentTimeMillis();
        log.info("--> {} {}", method, uri);

        chain.doFilter(req, res);

        long duration = System.currentTimeMillis() - start;
        int status = response.getStatus();

        if (status >= 500) {
            log.error("<-- {} {} | {} | {}ms", method, uri, status, duration);
        } else if (status >= 400) {
            log.warn("<-- {} {} | {} | {}ms", method, uri, status, duration);
        } else {
            log.info("<-- {} {} | {} | {}ms", method, uri, status, duration);
        }
    }

    private boolean isStaticAsset(String uri) {
        return uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/favicon")
                || uri.endsWith(".png")
                || uri.endsWith(".ico");
    }
}
