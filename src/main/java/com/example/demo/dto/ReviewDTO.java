package com.example.demo.dto;

public class ReviewDTO {

    private Long id;
    private Long productId;
    private String reviewerEmail;
    private int rating;
    private String comment;

    // Constructors
    public ReviewDTO() {}

    public ReviewDTO(Long id, Long productId, String reviewerEmail, int rating, String comment) {
        this.id = id;
        this.productId = productId;
        this.reviewerEmail = reviewerEmail;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getReviewerEmail() { return reviewerEmail; }
    public void setReviewerEmail(String reviewerEmail) { this.reviewerEmail = reviewerEmail; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}