# ⚡ StockPulse — Concurrency-Safe Inventory & Reservation Engine

A full-stack flash-sale inventory reservation system built with **Java, Spring Boot, MySQL, and Vanilla JavaScript**. Designed to prevent overselling under high concurrency and automatically reclaim abandoned cart stock.

## 🛠️ Tech Stack
* **Backend:** Java, Spring Boot (Web, Data JPA, Validation, Scheduling)
* **Database:** MySQL (`@Version` Optimistic Locking, JPQL `JOIN FETCH`)
* **Frontend:** HTML5, CSS3, Vanilla JavaScript (Fetch API, `Promise.all` Race Simulator)

---

## 🧠 AI vs. Human: Engineering Bugs Caught & Fixed

Instead of blindly shipping AI-generated boilerplate, this project specifically catches and resolves three classic backend failure points:

### 1. Race Condition & Overselling (`409 Conflict` Handling)
* **AI's Flawed Approach:** Checked `if (product.getAvailableStock() >= qty)` in Java before saving. Under simultaneous clicks, two threads read the same stock count and oversell the last unit.
* **My Fix:** Added `@Version` on the `Product` entity for **Optimistic Locking** inside a `@Transactional` boundary, and caught `ObjectOptimisticLockingFailureException` in `@RestControllerAdvice` to return a clean `HTTP 409 Conflict`.

### 2. The N+1 Query Bottleneck
* **AI's Flawed Approach:** Used standard `orderRepository.findAll()` with `FetchType.LAZY`, triggering $1 + N$ separate SQL queries when loading order history.
* **My Fix:** Wrote a custom JPQL query using `JOIN FETCH o.product` to fetch all orders and product details in a **single SQL `INNER JOIN` query**.

### 3. Abandoned Stock & Floating-Point Precision
* **AI's Flawed Approach:** Used `double` for currency and left reserved stock permanently locked if a buyer closed the browser before paying.
* **My Fix:** Enforced `BigDecimal` for all monetary math and built a `@Scheduled` background worker that expires unconfirmed reservations after 2 minutes and restores available stock.

---

## 🚀 How to Run Locally
1. Create a MySQL database: `CREATE DATABASE stockpulse_db;`
2. Update your MySQL credentials in `src/main/resources/application.properties`.
3. Run the application: `./mvnw spring-boot:run`
4. Open `http://localhost:8080` in your browser and click **"Simulate Race Condition"** to test concurrent locking live.
