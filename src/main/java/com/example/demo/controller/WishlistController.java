package com.example.demo.controller;

import com.example.demo.dto.WishlistDTO;
import com.example.demo.service.WishlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private static final Logger logger = LoggerFactory.getLogger(WishlistController.class);

    @Autowired
    private WishlistService wishlistService;

    @PostMapping("/{productId}")
    public ResponseEntity<?> addToWishlist(@PathVariable Long productId) {
        try {
            logger.info("Adding product ID {} to wishlist", productId);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                logger.error("Principal is not a String (email): {}", principal);
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String email = (String) principal;
            wishlistService.addToWishlist(email, productId);
            return ResponseEntity.ok("Product added to wishlist successfully");
        } catch (RuntimeException e) {
            logger.error("Error adding product ID {} to wishlist: {}", productId, e.getMessage(), e);
            if (e.getMessage().equals("Product not found")) {
                return ResponseEntity.status(404).body("Product not found");
            } else if (e.getMessage().equals("Product is already in your wishlist")) {
                return ResponseEntity.status(400).body("Product is already in your wishlist");
            }
            return ResponseEntity.status(500).body("Error adding product to wishlist: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getWishlist() {
        try {
            logger.info("Fetching wishlist for authenticated user");
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String email = (String) principal;
            List<WishlistDTO> wishlist = wishlistService.getWishlist(email);
            return ResponseEntity.ok(wishlist);
        } catch (RuntimeException e) {
            logger.error("Error fetching wishlist: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching wishlist: " + e.getMessage());
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable Long productId) {
        try {
            logger.info("Removing product ID {} from wishlist", productId);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                logger.error("Principal is not a String (email): {}", principal);
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String email = (String) principal;
            wishlistService.removeFromWishlist(email, productId);
            return ResponseEntity.ok("Product removed from wishlist successfully");
        } catch (RuntimeException e) {
            logger.error("Error removing product ID {} from wishlist: {}", productId, e.getMessage(), e);
            if (e.getMessage().equals("Product not found in your wishlist")) {
                return ResponseEntity.status(404).body("Product not found in your wishlist");
            }
            return ResponseEntity.status(500).body("Error removing product from wishlist: " + e.getMessage());
        }
    }
}