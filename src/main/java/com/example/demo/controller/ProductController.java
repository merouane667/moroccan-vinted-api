package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ReviewDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.Product;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper; // Add this import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            logger.info("Fetching all products");
            List<Product> products = productRepository.findAll();
            List<ProductDTO> productDTOs = products.stream()
                    .map(product -> new ProductDTO(
                            product.getId(),
                            product.getTitle(),
                            product.getDescription(),
                            product.getPrice(),
                            product.getCategory(),
                            product.getItemCondition(),
                            product.getSeller().getEmail(),
                            product.getImage() != null ? Base64.getEncoder().encodeToString(product.getImage()) : null
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            logger.error("Error fetching all products: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching products: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            logger.info("Creating new product");

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                logger.error("Principal is not a String (email): {}", principal);
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String email = (String) principal;
            User seller = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            ObjectMapper objectMapper = new ObjectMapper();
            ProductDTO productDTO = objectMapper.readValue(productJson, ProductDTO.class);

            Product product = new Product();
            product.setTitle(productDTO.getTitle());
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());
            product.setCategory(productDTO.getCategory());
            product.setItemCondition(productDTO.getItemCondition());
            product.setSeller(seller);

            if (image != null && !image.isEmpty()) {
                if (image.getSize() > 5 * 1024 * 1024) {
                    return ResponseEntity.status(400).body("Image size exceeds 5MB limit");
                }
                String contentType = image.getContentType();
                if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
                    return ResponseEntity.status(400).body("Only JPEG and PNG images are allowed");
                }
                product.setImage(image.getBytes());
            }

            Product savedProduct = productRepository.save(product);

            ProductDTO responseDTO = new ProductDTO(
                    savedProduct.getId(),
                    savedProduct.getTitle(),
                    savedProduct.getDescription(),
                    savedProduct.getPrice(),
                    savedProduct.getCategory(),
                    savedProduct.getItemCondition(),
                    savedProduct.getSeller().getEmail(),
                    savedProduct.getImage() != null ? Base64.getEncoder().encodeToString(savedProduct.getImage()) : null
            );
            return ResponseEntity.ok(responseDTO);
        } catch (IOException e) {
            logger.error("Error parsing product JSON or processing image: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Error processing request: " + e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error creating product: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error creating product: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            logger.info("Fetching product with ID: {}", id);
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            ProductDTO productDTO = new ProductDTO(
                    product.getId(),
                    product.getTitle(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getCategory(),
                    product.getItemCondition(),
                    product.getSeller().getEmail(),
                    product.getImage() != null ? Base64.getEncoder().encodeToString(product.getImage()) : null
            );
            return ResponseEntity.ok(productDTO);
        } catch (RuntimeException e) {
            logger.error("Error fetching product with ID {}: {}", id, e.getMessage(), e);
            if (e.getMessage().equals("Product not found")) {
                return ResponseEntity.status(404).body("Product not found");
            }
            return ResponseEntity.status(500).body("Error fetching product: " + e.getMessage());
        }
    }

    @GetMapping("/my-products")
    public ResponseEntity<?> getMyProducts() {
        try {
            logger.info("Fetching products for authenticated user");
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String email = (String) principal;
            User seller = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
            List<Product> products = productRepository.findBySeller_Id(seller.getId());
            if (products.isEmpty()) {
                return ResponseEntity.ok("No products found for this user");
            }
            List<ProductDTO> productDTOs = products.stream()
                    .map(product -> new ProductDTO(
                            product.getId(),
                            product.getTitle(),
                            product.getDescription(),
                            product.getPrice(),
                            product.getCategory(),
                            product.getItemCondition(),
                            product.getSeller().getEmail(),
                            product.getImage() != null ? Base64.getEncoder().encodeToString(product.getImage()) : null
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            logger.error("Error fetching user's products: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching user's products: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            logger.info("Deleting product with ID: {}", id);
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String email = (String) principal;
            User seller = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            if (!product.getSeller().getId().equals(seller.getId())) {
                return ResponseEntity.status(403).body("You are not authorized to delete this product");
            }
            boolean hasOrders = orderRepository.existsByProduct_Id(id);
            if (hasOrders) {
                return ResponseEntity.status(400).body("Can't delete your product, it is already ordered by someone");
            }
            productRepository.delete(product);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (RuntimeException e) {
            logger.error("Error deleting product with ID {}: {}", id, e.getMessage(), e);
            if (e.getMessage().equals("Product not found")) {
                return ResponseEntity.status(404).body("Product not found");
            }
            return ResponseEntity.status(500).body("Error deleting product: " + e.getMessage());
        }
    }

    @GetMapping("/debug/principal")
    public ResponseEntity<?> debugPrincipal() {
        try {
            logger.info("Debugging principal");
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ResponseEntity.ok(principal.toString());
        } catch (Exception e) {
            logger.error("Error debugging principal: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error debugging principal: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> createReview(@PathVariable Long id, @RequestBody ReviewDTO reviewDTO) {
        try {
            logger.info("Creating review for product ID: {}", id);

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                logger.error("Principal is not a String (email): {}", principal);
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String reviewerEmail = (String) principal;

            Product product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Product not found with ID: {}", id);
                        return new RuntimeException("Product not found");
                    });

            // Validate rating (1 to 5)
            if (reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5) {
                logger.error("Invalid rating value: {}", reviewDTO.getRating());
                return ResponseEntity.status(400).body("Rating must be between 1 and 5");
            }

            Review review = new Review(
                    product,
                    reviewerEmail,
                    reviewDTO.getRating(),
                    reviewDTO.getComment()
            );
            Review savedReview = reviewRepository.save(review);
            logger.info("Review created successfully for product ID: {}", id);

            ReviewDTO responseDTO = new ReviewDTO(
                    savedReview.getId(),
                    savedReview.getProduct().getId(),
                    savedReview.getReviewerEmail(),
                    savedReview.getRating(),
                    savedReview.getComment()
            );
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            logger.error("Error creating review for product ID {}: {}", id, e.getMessage(), e);
            if (e.getMessage().equals("Product not found")) {
                return ResponseEntity.status(404).body("Product not found");
            }
            return ResponseEntity.status(500).body("Error creating review: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating review for product ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error creating review: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<?> getReviews(@PathVariable Long id) {
        try {
            logger.info("Fetching reviews for product ID: {}", id);
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Product not found with ID: {}", id);
                        return new RuntimeException("Product not found");
                    });

            List<Review> reviews = reviewRepository.findAll().stream()
                    .filter(review -> review.getProduct().getId().equals(id))
                    .collect(Collectors.toList());

            List<ReviewDTO> reviewDTOs = reviews.stream()
                    .map(review -> new ReviewDTO(
                            review.getId(),
                            review.getProduct().getId(),
                            review.getReviewerEmail(),
                            review.getRating(),
                            review.getComment()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(reviewDTOs);
        } catch (RuntimeException e) {
            logger.error("Error fetching reviews for product ID {}: {}", id, e.getMessage(), e);
            if (e.getMessage().equals("Product not found")) {
                return ResponseEntity.status(404).body("Product not found");
            }
            return ResponseEntity.status(500).body("Error fetching reviews: " + e.getMessage());
        }
    }
}