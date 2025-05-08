package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            logger.info("Registering user: {}", user.getEmail());
            User registeredUser = authService.register(user);
            logger.info("User registered successfully: {}", registeredUser.getEmail());
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Error registering user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Logging in user: {}", loginRequest.getEmail());
            String jwt = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
            logger.info("User logged in successfully: {}", loginRequest.getEmail());
            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (Exception e) {
            logger.error("Error logging in user: {}", e.getMessage(), e);
            return ResponseEntity.status(401).body("Invalid credentials: " + e.getMessage());
        }
    }

    @PostMapping("/hash-passwords")
    public ResponseEntity<?> hashExistingPasswords() {
        try {
            logger.info("Hashing existing passwords");
            authService.hashExistingPasswords();
            logger.info("Passwords hashed successfully");
            return ResponseEntity.ok("Passwords hashed successfully");
        } catch (Exception e) {
            logger.error("Error hashing passwords: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error hashing passwords: " + e.getMessage());
        }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class JwtResponse {
        private String jwt;

        public JwtResponse(String jwt) {
            this.jwt = jwt;
        }

        public String getJwt() {
            return jwt;
        }

        public void setJwt(String jwt) {
            this.jwt = jwt;
        }
    }
}