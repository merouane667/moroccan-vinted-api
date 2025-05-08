package com.example.demo.config;

import com.example.demo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.info("Processing request - URI: {}, ContextPath: {}", request.getRequestURI(), request.getContextPath());

        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        String normalizedURI = requestURI.startsWith(contextPath) ? requestURI.substring(contextPath.length()) : requestURI;

        logger.info("shouldNotFilter - URI: {}, ContextPath: {}, Normalized URI: {}, Skip: {}", requestURI, contextPath, normalizedURI, shouldNotFilter(request));

        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            logger.info("Extracted JWT token: {}", jwt);
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.info("Extracted username from token: {}", username);
            } catch (Exception e) {
                logger.error("Error extracting username from token: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("No JWT token found in Authorization header");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("Loading user details for username: {}", username);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            logger.info("User details loaded: {}, class: {}, authorities: {}", userDetails.getUsername(), userDetails.getClass().getName(), userDetails.getAuthorities());

            if (jwtUtil.validateToken(jwt, userDetails)) {
                logger.info("JWT token validated successfully for user: {}", username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Authentication set for user: {}, principal class: {}, principal value: {}", username, authentication.getPrincipal().getClass().getName(), authentication.getPrincipal());
            } else {
                logger.warn("JWT token validation failed for user: {}", username);
            }
        } else if (username == null && jwt != null) {
            logger.warn("No username extracted from JWT token");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        String normalizedPath = path.startsWith(contextPath) ? path.substring(contextPath.length()) : path;
        boolean skip = normalizedPath.startsWith("/api/auth") || normalizedPath.equals("/error");
        logger.info("shouldNotFilter - URI: {}, ContextPath: {}, Normalized URI: {}, Skip: {}", path, contextPath, normalizedPath, skip);
        return skip;
    }
}