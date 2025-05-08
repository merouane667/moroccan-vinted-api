package com.example.demo.dto;

public class OrderRequest {
    private Long productId;

    public OrderRequest() {
    }

    public OrderRequest(Long productId) {
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
