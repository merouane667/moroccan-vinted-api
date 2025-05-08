package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository; // Ensure this is injected

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
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
                            product.getSeller() != null ? product.getSeller().getEmail() : null,
                            product.getImage() != null ? Base64.getEncoder().encodeToString(product.getImage()) : null
                    ))
                    .collect(Collectors.toList());
            logger.info("Found {} products", productDTOs.size());
            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            logger.error("Error fetching products: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestPart("product") Product product,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        try {
            logger.info("Creating product: {}", product.getTitle());

            // Validate image
            if (image != null) {
                String contentType = image.getContentType();
                if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
                    logger.error("Invalid image type: {}", contentType);
                    return ResponseEntity.badRequest().body("Image must be JPEG or PNG");
                }
                long maxSize = 5 * 1024 * 1024; // 5MB
                if (image.getSize() > maxSize) {
                    logger.error("Image size exceeds limit: {}", image.getSize());
                    return ResponseEntity.badRequest().body("Image size must not exceed 5MB");
                }
                product.setImage(image.getBytes());
            }

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            logger.info("Principal class: {}, value: {}", principal.getClass().getName(), principal.toString());

            if (!(principal instanceof String)) {
                logger.error("Principal is not a String (email): {}", principal);
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }

            String email = (String) principal;

            User authenticatedUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("User not found with email: {}", email);
                        return new RuntimeException("Authenticated user not found");
                    });

            logger.info("Authenticated user: {}", authenticatedUser.getEmail());

            product.setSeller(authenticatedUser);
            Product savedProduct = productRepository.save(product);
            logger.info("Product created successfully: {}", savedProduct.getId());

            ProductDTO productDTO = new ProductDTO(
                    savedProduct.getId(),
                    savedProduct.getTitle(),
                    savedProduct.getDescription(),
                    savedProduct.getPrice(),
                    savedProduct.getCategory(),
                    savedProduct.getItemCondition(),
                    savedProduct.getSeller() != null ? savedProduct.getSeller().getEmail() : null,
                    savedProduct.getImage() != null ? Base64.getEncoder().encodeToString(savedProduct.getImage()) : null
            );

            return ResponseEntity.ok(productDTO);
        } catch (Exception e) {
            logger.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error creating product: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            logger.info("Fetching product with id: {}", id);
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            logger.info("Product found: {}", product.getTitle());
            ProductDTO productDTO = new ProductDTO(
                    product.getId(),
                    product.getTitle(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getCategory(),
                    product.getItemCondition(),
                    product.getSeller() != null ? product.getSeller().getEmail() : null,
                    product.getImage() != null ? Base64.getEncoder().encodeToString(product.getImage()) : null
            );
            return ResponseEntity.ok(productDTO);
        } catch (Exception e) {
            logger.error("Error fetching product with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(404).body("Product not found: " + e.getMessage());
        }
    }

    @GetMapping("/my-products")
    public ResponseEntity<?> getMyProducts() {
        try {
            logger.info("Fetching products posted by authenticated user");

            // Get authenticated user (seller)
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String email = (String) principal;
            User seller = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            // Fetch all products posted by the user
            List<Product> products = productRepository.findBySeller_Id(seller.getId());
            if (products.isEmpty()) {
                return ResponseEntity.ok("No products found for this user");
            }

            // Map products to DTOs
            List<ProductDTO> productDTOs = products.stream()
                    .map(product -> new ProductDTO(
                            product.getId(),
                            product.getTitle(),
                            product.getDescription(),
                            product.getPrice(),
                            product.getCategory(),
                            product.getItemCondition(),
                            product.getSeller() != null ? product.getSeller().getEmail() : null,
                            product.getImage() != null ? Base64.getEncoder().encodeToString(product.getImage()) : null
                    ))
                    .collect(Collectors.toList());

            logger.info("Found {} products posted by user {}", productDTOs.size(), email);
            return ResponseEntity.ok(productDTOs);
        } catch (Exception e) {
            logger.error("Error fetching user's products: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching user's products: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            logger.info("Attempting to delete product with id: {}", id);

            // Get authenticated user (seller)
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String email = (String) principal;
            User seller = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            // Fetch the product
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Product not found with id: {}", id);
                        return new RuntimeException("Product not found");
                    });

            // Check if the authenticated user is the seller
            if (!product.getSeller().getId().equals(seller.getId())) {
                logger.error("Unauthorized attempt to delete product {} by user {}", id, email);
                return ResponseEntity.status(403).body("You are not authorized to delete this product");
            }

            // Check if the product has any orders
            boolean hasOrders = orderRepository.existsByProduct_Id(id);
            if (hasOrders) {
                logger.error("Cannot delete product {} as it is already ordered", id);
                return ResponseEntity.status(400).body("Can't delete your product, it is already ordered by someone");
            }

            // Delete the product
            productRepository.delete(product);
            logger.info("Product with id {} deleted successfully", id);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (RuntimeException e) {
            logger.error("Error deleting product with id {}: {}", id, e.getMessage(), e);
            if (e.getMessage().equals("Product not found")) {
                return ResponseEntity.status(404).body("Product not found");
            }
            return ResponseEntity.status(500).body("Error deleting product: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting product with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error deleting product: " + e.getMessage());
        }
    }

    @GetMapping("/debug/principal")
    public ResponseEntity<?> debugPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("Debug principal class: {}, value: {}", principal.getClass().getName(), principal.toString());
        return ResponseEntity.ok("Principal: " + principal.toString() + ", class: " + principal.getClass().getName());
    }
}