# Chennai Craft (Backend)

REST API for a Chennai-only marketplace where local makers sell handmade products. Customers can browse and order, sellers manage their own shop's products and orders, and an admin approves sellers.

**Status:** backend is complete and unit-tested. React frontend and deployment are planned.

## Features

- Register and login with JWT authentication
- Role-based access: `CUSTOMER`, `SELLER`, `ADMIN`
- Seller onboarding: a customer applies, an admin approves or rejects, and approval upgrades the user to `SELLER`
- Sellers create and list their own products
- Public product browsing, search by name, and product detail (only in-stock items are listed)
- Order placement with stock check and stock reduction
- Orders are split per seller: each order line has its own status
- Sellers update their own order lines: `PLACED`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, or `CANCELLED`
- Cancelling an order line puts the stock back
- Delivery limited to Chennai (pincode `600xxx`) and Indian mobile number validation

## Tech stack

- Java 17, Spring Boot 3
- Spring Security with JWT (jjwt), BCrypt password hashing
- Spring Data JPA (Hibernate), MySQL 8
- Maven, Lombok, Bean Validation

## API endpoints

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create a customer account |
| POST | `/api/auth/login` | Public | Login and get a JWT |
| GET | `/api/me` | Logged in | Current user |
| GET | `/api/products?q=` | Public | List and search in-stock products |
| GET | `/api/products/{id}` | Public | Product detail |
| POST | `/api/seller/apply` | Customer | Apply to become a seller |
| GET | `/api/seller/application` | Logged in | Check my seller application |
| POST | `/api/seller/products` | Seller | Add a product |
| GET | `/api/seller/products` | Seller | List my products |
| POST | `/api/orders` | Customer, Seller | Place an order |
| GET | `/api/orders/my` | Customer, Seller | My orders |
| GET | `/api/seller/orders` | Seller | Order lines for my shop |
| PATCH | `/api/seller/orders/{itemId}/status` | Seller | Update an order line's status |
| GET | `/api/admin/sellers?status=PENDING` | Admin | List seller applications |
| POST | `/api/admin/sellers/{id}/approve` | Admin | Approve a seller |
| POST | `/api/admin/sellers/{id}/reject` | Admin | Reject a seller |
| PUT | `/api/seller/products/{id}` | Seller | Edit my product |
| DELETE | `/api/seller/products/{id}` | Seller | Delete my product (only if it has no orders) |

Send the token as `Authorization: Bearer <token>`.

### Order line status rules

- `PLACED` to `CONFIRMED` or `CANCELLED`
- `CONFIRMED` to `SHIPPED` or `CANCELLED`
- `SHIPPED` to `DELIVERED`
- `DELIVERED` and `CANCELLED` are final

## Design decisions

- **Identity comes from the token, not the request body.** A seller cannot add products or update orders on behalf of another seller.
- **Order placement is transactional.** If any item is out of stock, nothing is saved.
- **Stock rows are locked** (pessimistic write lock) while an order is placed, so two buyers cannot take the last item together.
- **Price snapshot.** Each order line stores the product name and unit price at purchase time.
- **Roles are read from the database on every request**, so a role change (for example seller approval) applies immediately.
- A seller cannot buy their own product.

## Running tests

```
./mvnw test "-Dtest=OrderServiceTest,SellerOrderServiceTest"
```

## Getting started

### Prerequisites

- Java 17
- MySQL 8

### Setup

1. Clone the repository

   ```
   git clone https://github.com/gokul2910/chennai-craft-backend.git
   cd chennai-craft-backend
   ```

2. Create the database

   ```sql
   CREATE DATABASE chennai_craft;
   ```

3. Create `src/main/resources/application-local.properties` (this file is git-ignored, so your secrets stay local)

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/chennai_craft
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   jwt.secret=USE_A_RANDOM_STRING_OF_AT_LEAST_32_CHARACTERS
   ```

4. Run the app

   ```
   ./mvnw spring-boot:run
   ```

   On Windows PowerShell use `.\mvnw spring-boot:run`. The API starts on `http://localhost:8081`. Tables are created automatically on first run.

### Creating the first admin

Admins cannot be created through the API (on purpose). Register a user, then promote it in MySQL:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

### Example: place an order

`POST /api/orders` with a customer's token:

```json
{
  "address": "12, Usman Road, T. Nagar, Chennai",
  "phone": "9876543210",
  "pincode": "600017",
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
```

## Project structure

```
src/main/java/chennai_craft_backend
  config/       security config and JWT filter
  controller/   REST controllers
  dto/          request and response objects
  model/        JPA entities and enums
  repository/   Spring Data repositories
  service/      business logic
```

## Roadmap

- [x] Product edit and delete for sellers
- [x] CORS configuration
- [x] Global error handling
- [x] Unit and integration tests
- [ ] Pagination for product list
- [ ] Customer-side order cancel
- [ ] React frontend
- [ ] Deployment

## Author

Gokul ([GitHub](https://github.com/gokul2910) | [LinkedIn](https://www.linkedin.com/in/gokulgandhimyd72))