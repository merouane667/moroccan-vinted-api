package com.example.demo.dto;

public class WishlistDTO {

    private Long id;
    private Long productId;
    private String productTitle;
    private double productPrice;
    private String productCategory;

    // Constructors
    public WishlistDTO() {}

    public WishlistDTO(Long id, Long productId, String productTitle, double productPrice, String productCategory) {
        this.id = id;
        this.productId = productId;
        this.productTitle = productTitle;
        this.productPrice = productPrice;
        this.productCategory = productCategory;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }
    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }
}