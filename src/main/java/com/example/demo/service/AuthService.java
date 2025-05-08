package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    public User register(User user) {
        logger.info("Registering user: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        logger.info("User registered: {}", savedUser.getEmail());
        return savedUser;
    }

    public String login(String email, String password) throws AuthenticationException {
        logger.info("Authenticating user: {}", email);
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", email);
                    return new RuntimeException("User not found");
                });
        logger.info("Generating JWT for user: {}", email);
        return jwtUtil.generateToken(user.getEmail(), user.getAuthorities());
    }

    public void hashExistingPasswords() {
        logger.info("Hashing existing passwords");
        userRepository.findAll().forEach(user -> {
            if (!user.getPassword().startsWith("$2a$")) {
                logger.info("Hashing password for user: {}", user.getEmail());
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userRepository.save(user);
            }
        });
        logger.info("Password hashing complete");
    }
}