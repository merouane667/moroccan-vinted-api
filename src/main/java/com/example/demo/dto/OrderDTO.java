package com.example.demo.dto;

public class OrderDTO {
    private Long id;
    private Long productId;
    private String buyerEmail;
    private String orderDate;
    private String status;

    public OrderDTO(Long id, Long productId, String buyerEmail, String orderDate, String status) {
        this.id = id;
        this.productId = productId;
        this.buyerEmail = buyerEmail;
        this.orderDate = orderDate;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}