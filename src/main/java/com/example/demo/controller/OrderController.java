package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.OrderRequest;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            logger.info("Creating order for product ID: {}", orderRequest.getProductId());

            // Get authenticated user (buyer)
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String email = (String) principal;
            User buyer = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            // Validate product
            Product product = productRepository.findById(orderRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            // Add business logic (e.g., check if product is available)
            if (orderRepository.existsByProduct_Id(orderRequest.getProductId())) {
                return ResponseEntity.badRequest().body("Product is already ordered");
            }

            // Create and save order
            Order order = new Order(product, buyer);
            Order savedOrder = orderRepository.save(order);

            // Prepare response
            OrderDTO orderDTO = new OrderDTO(
                    savedOrder.getId(),
                    savedOrder.getProduct().getId(),
                    savedOrder.getBuyer().getEmail(),
                    savedOrder.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    savedOrder.getStatus().name()
            );
            return ResponseEntity.ok(orderDTO);
        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error creating order: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserOrders() {
        try {
            logger.info("Fetching orders for authenticated user");

            // Get authenticated user (buyer)
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String email = (String) principal;
            User buyer = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            // Fetch all orders for the user
            List<Order> orders = orderRepository.findByBuyer_Id(buyer.getId());
            if (orders.isEmpty()) {
                return ResponseEntity.ok("No orders found for this user");
            }

            // Map orders to DTOs
            List<OrderDTO> orderDTOs = orders.stream().map(order -> new OrderDTO(
                    order.getId(),
                    order.getProduct().getId(),
                    order.getBuyer().getEmail(),
                    order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    order.getStatus().name()
            )).collect(Collectors.toList());

            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            logger.error("Error fetching user orders: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching user orders: " + e.getMessage());
        }
    }
}