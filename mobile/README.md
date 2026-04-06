# PhoneShop Web App

This folder contains the Spring Boot e-commerce application.

## Stack

- Java 17
- Spring Boot
- Spring MVC + Thymeleaf
- Spring Security
- Spring Data JPA
- MySQL

## Folder map

- `src/main/java/com/ecommerce/mobile/config`
  - security, seed, payment, shipping, and app configuration
- `src/main/java/com/ecommerce/mobile/controller`
  - HTTP routes
- `src/main/java/com/ecommerce/mobile/entity`
  - JPA entities
- `src/main/java/com/ecommerce/mobile/repository`
  - database access
- `src/main/java/com/ecommerce/mobile/service`
  - business logic
- `src/main/resources/templates`
  - Thymeleaf pages
- `src/main/resources/static`
  - CSS, JS, images, assets

## Main user flows

### Customer

- login
- browse products
- search products
- open product detail
- add variant to cart
- edit cart
- checkout
- choose address
- place order
- pay by COD or VNPAY sandbox
- cancel order if allowed
- review products
- send feedback
- edit profile and addresses
- track shipment

### Employee

- login
- receive and process orders
- move order through workflow:
  - `PENDING`
  - `CONFIRMED`
  - `PACKING`
  - `SHIPPING`
  - `DELIVERED`
- handle customer feedback

### Manager

- login
- manage products
- manage users
- view reports
- view orders
- manage the business side of the app

## Important routes

### Public

- `/`
- `/products`
- `/products/{id}`
- `/login`
- `/register`

### Customer

- `/cart`
- `/orders`
- `/orders/{id}`
- `/orders/{id}/cancel`
- `/profile`
- `/profile/addresses`
- `/profile/feedbacks`
- `/orders/{id}/tracking`

### Employee / Manager

- `/employee/dashboard`
- `/employee/orders`
- `/employee/feedbacks`
- `/admin/dashboard`
- `/admin/products`
- `/admin/orders`
- `/admin/reports`

## Core data concepts

- `User` is the abstract base class
- `Customer`, `Employee`, and `Manager` extend `User`
- `Cart` belongs to a customer
- `Order` is the finalized purchase
- `OrderItem` stores each purchased variant
- `Payment` stores payment attempts and status
- `Shipment` stores shipping status
- `Review` belongs to customer + product
- `Feedback` belongs to customer + employee

## How to run

From the repository root:

```powershell
cd mobile
.\mvnw.cmd spring-boot:run
```

If you only want to compile:

```powershell
.\mvnw.cmd -q -DskipTests clean compile
```

## Test accounts

The seeded accounts depend on the current mock data, but the common ones are:

- `manager1@gmail.com / 123456`
- `employee1@gmail.com / 123456`
- `customer0001@phoneshop.vn / 123456`

## Typical web test sequence

1. Login as customer
2. Search a product
3. Open product detail
4. Add variant to cart
5. Open cart
6. Checkout
7. Choose address
8. Place order
9. Pay with COD or VNPAY sandbox
10. Track order
11. Leave a review after delivery

## If something breaks

When debugging, check these layers in order:

1. Controller
2. Service
3. Repository
4. Entity mapping
5. Thymeleaf template
6. MySQL schema

That order usually finds the problem fastest.

