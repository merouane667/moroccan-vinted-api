package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.Order.OrderStatus;
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

import java.time.LocalDateTime;
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

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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

            Product product = productRepository.findById(orderRequest.getProductId())
                    .orElseThrow(() -> {
                        logger.error("Product not found with ID: {}", orderRequest.getProductId());
                        return new RuntimeException("Product not found");
                    });

            boolean alreadyOrdered = orderRepository.existsByProduct_Id(product.getId());
            if (alreadyOrdered) {
                logger.error("Product with ID {} is already ordered", product.getId());
                return ResponseEntity.status(400).body("Product is already ordered");
            }

            Order order = new Order(product, authenticatedUser); // Status defaults to PENDING
            Order savedOrder = orderRepository.save(order);
            logger.info("Order created successfully: {}", savedOrder.getId());

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

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String email = (String) principal;
            User buyer = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            List<Order> orders = orderRepository.findByBuyer_Id(buyer.getId());
            if (orders.isEmpty()) {
                return ResponseEntity.ok("No orders found for this user");
            }

            List<OrderDTO> orderDTOs = orders.stream()
                    .map(order -> new OrderDTO(
                            order.getId(),
                            order.getProduct().getId(),
                            order.getBuyer().getEmail(),
                            order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            order.getStatus().name()
                    ))
                    .collect(Collectors.toList());

            logger.info("Found {} orders for user {}", orderDTOs.size(), email);
            return ResponseEntity.ok(orderDTOs);
        } catch (Exception e) {
            logger.error("Error fetching user's orders: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching user's orders: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusUpdateRequest statusUpdateRequest) {
        try {
            logger.info("Updating status for order ID: {}", id);

            // Validate the status
            OrderStatus newStatus;
            try {
                newStatus = OrderStatus.valueOf(statusUpdateRequest.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.error("Invalid status value: {}", statusUpdateRequest.getStatus());
                return ResponseEntity.status(400).body("Invalid status value. Must be one of: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED");
            }

            // Get authenticated user (buyer)
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (!(principal instanceof String)) {
                return ResponseEntity.status(500).body("Authentication principal is not a valid email string");
            }
            String email = (String) principal;
            User buyer = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

            // Fetch the order
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Order not found with ID: {}", id);
                        return new RuntimeException("Order not found");
                    });

            // Check if the authenticated user is the buyer of the order
            if (!order.getBuyer().getId().equals(buyer.getId())) {
                logger.error("Unauthorized attempt to update order status for order {} by user {}", id, email);
                return ResponseEntity.status(403).body("You are not authorized to update this order");
            }

            // Validate status transition
            OrderStatus currentStatus = order.getStatus();
            if (!isValidStatusTransition(currentStatus, newStatus)) {
                logger.error("Invalid status transition from {} to {} for order ID: {}", currentStatus, newStatus, id);
                return ResponseEntity.status(400).body("Invalid status transition: Cannot move from " + currentStatus + " to " + newStatus);
            }

            // Update the status
            order.setStatus(newStatus);
            Order updatedOrder = orderRepository.save(order);
            logger.info("Order status updated successfully for order ID: {}", updatedOrder.getId());

            // Return the updated OrderDTO
            OrderDTO orderDTO = new OrderDTO(
                    updatedOrder.getId(),
                    updatedOrder.getProduct().getId(),
                    updatedOrder.getBuyer().getEmail(),
                    updatedOrder.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    updatedOrder.getStatus().name()
            );

            return ResponseEntity.ok(orderDTO);
        } catch (RuntimeException e) {
            logger.error("Error updating order status for order ID {}: {}", id, e.getMessage(), e);
            if (e.getMessage().equals("Order not found")) {
                return ResponseEntity.status(404).body("Order not found");
            }
            return ResponseEntity.status(500).body("Error updating order status: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating order status for order ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error updating order status: " + e.getMessage());
        }
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid transitions for the buyer
        switch (currentStatus) {
            case PENDING:
                return newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED:
                return newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED:
                return newStatus == OrderStatus.DELIVERED;
            case DELIVERED:
            case CANCELLED:
                return false; // No transitions allowed from DELIVERED or CANCELLED
            default:
                return false;
        }
    }
}

class OrderRequest {
    private Long productId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}

class OrderStatusUpdateRequest {
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}