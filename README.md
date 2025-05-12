# Moroccan Vinted API

![Version](https://img.shields.io/badge/version-1.0-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-green)
![JWT](https://img.shields.io/badge/JWT-Authentication-orange)
![License](https://img.shields.io/badge/license-MIT-brightgreen)

> A RESTful API for a Moroccan second-hand marketplace, built with Spring Boot

## 📑 Overview

This API allows users to manage products, orders, reviews, and wishlists in a second-hand marketplace platform. It handles various e-commerce operations with secure JWT authentication.

### Key Features

- **Products Management**: List, create, view, and delete products
- **Order Processing**: Place, list, update status, and cancel orders
- **Review System**: Create and retrieve reviews with average rating calculation
- **Wishlist Functionality**: Add, view, and remove items from wishlist
- **JWT Authentication**: Secure API access with JSON Web Tokens

## 🔗 Base URL

```
http://localhost:8080
```

## 🔐 Authentication

All endpoints (except registration and login) require JWT authentication.

### Getting a Token

1. Register via `POST /api/auth/register`
2. Login via `POST /api/auth/login` to receive your JWT token

### Using the Token

Include the token in the Authorization header as:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwic3ViIjoibWVyb3VhbmVAZXhhbXBsZS5jb20iLCJpYXQiOjE3NDY3MDg0NzIsImV4cCI6MTc0NjcyNjQ3Mn0.mrOy2axLFgs9PDtQimnJSaRPPfzjm-MQAzBzMBvjhTDk09ZzHH4S6zJRAIGbxrr_qvvz-j5-oWxs6tkhLq9uGg
```

## 📋 API Endpoints

### Auth Controller (`/api/auth`)

#### Register a New User

```
POST /api/auth/register
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password",
  "username": "user",
  "phone": "1234567890",
  "city": "Casablanca"
}
```

**Responses:**
- `200 OK`: User registered successfully
- `400 Bad Request`: Email already exists or invalid input

#### Login

```
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Responses:**
- `200 OK`: Login successful, returns JWT token
- `401 Unauthorized`: Invalid credentials

### Product Controller (`/api/products`)

#### List All Products

```
GET /api/products
```

**Responses:**
- `200 OK`: List of products
- `500 Internal Server Error`: Error fetching products

#### Create a New Product

```
POST /api/products
```

**Request:**
- Form Data:
   - `product`: JSON string of product details
   - `image`: Optional image file (JPEG/PNG, max 5MB)

**Responses:**
- `200 OK`: Product created successfully
- `400 Bad Request`: Invalid input or image constraints violated
- `500 Internal Server Error`: Error creating product

#### Get Product by ID

```
GET /api/products/{id}
```

**Responses:**
- `200 OK`: Product details
- `404 Not Found`: Product not found
- `500 Internal Server Error`: Error fetching product

#### List User's Products

```
GET /api/products/my-products
```

**Responses:**
- `200 OK`: List of user's products or "No products found"
- `500 Internal Server Error`: Error fetching products

#### Delete a Product

```
DELETE /api/products/{id}
```

**Responses:**
- `200 OK`: Product deleted successfully
- `400 Bad Request`: Cannot delete due to existing orders
- `403 Forbidden`: Not authorized to delete this product
- `404 Not Found`: Product not found
- `500 Internal Server Error`: Error deleting product

#### Add a Review

```
POST /api/products/{id}/reviews
```

**Request Body:**
```json
{
  "rating": 4,
  "comment": "Great product, fast delivery!"
}
```

**Responses:**
- `200 OK`: Review created successfully
- `400 Bad Request`: Invalid rating or user hasn't purchased the product
- `404 Not Found`: Product not found
- `500 Internal Server Error`: Error creating review

#### Get Product Reviews

```
GET /api/products/{id}/reviews
```

**Responses:**
- `200 OK`: List of reviews with average rating
- `404 Not Found`: Product not found
- `500 Internal Server Error`: Error fetching reviews

### Order Controller (`/api/orders`)

#### Place an Order

```
POST /api/orders
```

**Request Body:**
```json
{
  "productId": 1
}
```

**Responses:**
- `200 OK`: Order created successfully
- `400 Bad Request`: Product already ordered
- `404 Not Found`: Product not found
- `500 Internal Server Error`: Error creating order

#### List User's Orders

```
GET /api/orders
```

**Responses:**
- `200 OK`: List of user's orders or "No orders found"
- `500 Internal Server Error`: Error fetching orders

#### Update Order Status

```
PUT /api/orders/{id}/status
```

**Request Body:**
```json
{
  "status": "CONFIRMED"
}
```

**Responses:**
- `200 OK`: Order status updated successfully
- `400 Bad Request`: Invalid status transition
- `403 Forbidden`: Not authorized to update this order
- `404 Not Found`: Order not found
- `500 Internal Server Error`: Error updating order status

#### Cancel an Order

```
PUT /api/orders/{id}/cancel
```

**Responses:**
- `200 OK`: Order cancelled successfully
- `400 Bad Request`: Cannot cancel due to current status
- `403 Forbidden`: Not authorized to cancel this order
- `404 Not Found`: Order not found
- `500 Internal Server Error`: Error cancelling order

### Wishlist Controller (`/api/wishlist`)

#### Add to Wishlist

```
POST /api/wishlist/{productId}
```

**Responses:**
- `200 OK`: Product added successfully
- `400 Bad Request`: Product already in wishlist
- `404 Not Found`: Product not found
- `500 Internal Server Error`: Error adding product

#### Get Wishlist

```
GET /api/wishlist
```

**Responses:**
- `200 OK`: List of wishlist items
- `500 Internal Server Error`: Error fetching wishlist

#### Remove from Wishlist

```
DELETE /api/wishlist/{productId}
```

**Responses:**
- `200 OK`: Product removed successfully
- `404 Not Found`: Product not in wishlist
- `500 Internal Server Error`: Error removing product

## 🚀 Setup & Installation

### Prerequisites

- Java 17+
- Maven
- MySQL (or preferred database)
- Postman (for testing)

### Installation Steps

1. **Clone the Repository**

   ```bash
   git clone https://github.com/merouane667/moroccan-vinted-api.git
   cd moroccan-vinted-api/demo
   ```

2. **Configure the Database**

   Create a MySQL database:
   ```sql
   CREATE DATABASE moroccan_vinted_db;
   ```

   Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/moroccan_vinted_db
   spring.datasource.username=your-username
   spring.datasource.password=your-password
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
   ```

3. **Install Dependencies**

   ```bash
   mvn clean install
   ```

4. **Run the Application**

   ```bash
   mvn spring-boot:run
   ```

   The application will start on http://localhost:8080

5. **Test the API**

   - Use Postman to test the endpoints
   - Start by registering and logging in to get a JWT token
   - Include the token in the Authorization header for subsequent requests

## 📝 Additional Notes

- **Error Handling**: The API returns appropriate HTTP status codes and error messages
- **Security**: JWT tokens are used for authentication
- **Database**: Hibernate automatically manages the schema
- **Logging**: The application logs requests and errors for debugging

## 🔮 Future Improvements

- Add product search and filtering
- Implement email notifications for orders and reviews
- Add a messaging system between buyers and sellers

## 📄 License

This project is licensed under the MIT License

---

Created by [merouane667](https://github.com/merouane667)