# Moroccan Vinted API (Version 1.0)

## Overview
This is a RESTful API for a Moroccan second-hand marketplace, built with Spring Boot. It allows users to manage products (list, create, delete) and orders (place, list), with JWT-based authentication.

## Base URL
`http://localhost:8080`

## Endpoints
### ProductController (`/api/products`)
- **GET `/api/products`**: List all products.
- **POST `/api/products`**: Create a new product (multipart form with `product` and optional `image`).
- **GET `/api/products/{id}`**: Get a product by ID.
- **GET `/api/products/my-products`**: List products posted by the authenticated user.
- **DELETE `/api/products/{id}`**: Delete a product (if no orders exist).
- **GET `/api/products/debug/principal`**: Debug the authenticated user's principal.

### OrderController (`/api/orders`)
- **POST `/api/orders`**: Place an order (`{"productId": <Long>}`).
- **GET `/api/orders`**: List orders placed by the authenticated user.

## Setup
1. **Prerequisites**:
   - Java 17+
   - Maven
   - MySQL (or your preferred database)
2. **Clone the repository**:
   ```bash
   git clone https://github.com/<your-username>/moroccan-vinted-api.git
   cd moroccan-vinted-api/demo
