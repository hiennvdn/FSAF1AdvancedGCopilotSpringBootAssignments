# E-Commerce Spring Boot API

## Overview
This project provides a RESTful API for managing products, categories, and orders in an e-commerce system. It is built with Spring Boot, Spring Data JPA, and MySQL.

## Product API

### Endpoints
- `POST /api/products` - Create a new product
- `PUT /api/products/{id}` - Update an existing product
- `DELETE /api/products/{id}` - Delete a product
- `GET /api/products/{id}` - Get product details by ID
- `GET /api/products` - Search products with filters:
  - `keyword` (name/description, optional)
  - `categoryId` (optional)
  - `minPrice`, `maxPrice` (optional)
  - Pagination supported (Spring Data Pageable)

#### Example: Search Products
```
GET /api/products?keyword=laptop&categoryId=2&minPrice=500&maxPrice=2000&page=0&size=10
```

## Order API

### Endpoints
- `POST /api/orders` - Place a new order
  - Request body: userId, shippingAddress, items (productId, quantity)
- `GET /api/orders` - Get all orders
- `GET /api/orders/user/{userId}` - Get all orders for a specific user

#### Example: Place Order
```
POST /api/orders
{
  "userId": 1,
  "shippingAddress": "123 Main St",
  "items": [
    { "productId": 2, "quantity": 1 },
    { "productId": 3, "quantity": 2 }
  ]
}
```

## Database
- MySQL is used as the database.
- Connection settings are in `src/main/resources/application.yml`.

## Running the Project
1. Ensure MySQL is running and the database exists.
2. Update `application.yml` with your DB credentials if needed.
3. Run:
   ```
   mvn clean spring-boot:run
   ```
4. Access the APIs at `http://localhost:8080/api/`

## Testing
- Run all tests and generate code coverage with:
  ```
  mvn clean verify
  ```
- JaCoCo coverage report: `target/site/jacoco/index.html`

## Notes
- All endpoints return JSON.
- Error handling follows standard Spring Boot conventions.
