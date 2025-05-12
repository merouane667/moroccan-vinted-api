package com.example.demo.service;

import com.example.demo.dto.WishlistDTO;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WishlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private static final Logger logger = LoggerFactory.getLogger(WishlistService.class);

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public void addToWishlist(String email, Long productId) {
        logger.info("Adding product ID {} to wishlist for user {}", productId, email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new RuntimeException("Authenticated user not found");
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", productId);
                    return new RuntimeException("Product not found");
                });

        if (wishlistRepository.existsByUser_IdAndProduct_Id(user.getId(), productId)) {
            logger.warn("Product ID {} is already in the wishlist for user {}", productId, email);
            throw new RuntimeException("Product is already in your wishlist");
        }

        Wishlist wishlistItem = new Wishlist(user, product);
        wishlistRepository.save(wishlistItem);
        logger.info("Product ID {} added to wishlist for user {}", productId, email);
    }

    @Transactional(readOnly = true)
    public List<WishlistDTO> getWishlist(String email) {
        logger.info("Fetching wishlist for user {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new RuntimeException("Authenticated user not found");
                });

        List<Wishlist> wishlistItems = wishlistRepository.findByUser_Id(user.getId());
        return wishlistItems.stream()
                .map(wishlist -> new WishlistDTO(
                        wishlist.getId(),
                        wishlist.getProduct().getId(),
                        wishlist.getProduct().getTitle(),
                        wishlist.getProduct().getPrice(),
                        wishlist.getProduct().getCategory()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFromWishlist(String email, Long productId) {
        logger.info("Removing product ID {} from wishlist for user {}", productId, email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new RuntimeException("Authenticated user not found");
                });

        if (!wishlistRepository.existsByUser_IdAndProduct_Id(user.getId(), productId)) {
            logger.warn("Product ID {} not found in wishlist for user {}", productId, email);
            throw new RuntimeException("Product not found in your wishlist");
        }

        wishlistRepository.deleteByUser_IdAndProduct_Id(user.getId(), productId);
        logger.info("Product ID {} removed from wishlist for user {}", productId, email);
    }
}