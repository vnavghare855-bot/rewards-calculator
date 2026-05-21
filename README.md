# Retail Reward Points Calculator

A production-ready Spring Boot RESTful API designed to ingest customer transactions, validate input parameters, enforce time-window constraints, and calculate monthly and total reward points.

---

## 🏗️ System Architecture

The application adheres to the standard three-tier enterprise architecture pattern:

```
[ Client / API Request ]
         │
         ▼
┌────────────────────────────────────────────────────────┐
│                  Presentation Layer                    │
│   - RewardsController (REST API Endpoints)             │
│   - GlobalExceptionHandler (API Error Formatter)       │
└────────────────────────┬───────────────────────────────┘
                         │ (Ingests Transaction DTOs)
                         ▼
┌────────────────────────────────────────────────────────┐
│                    Business Layer                      │
│   - RewardsService (Validations, Period Enforcement,   │
│     Points Calculation, and Aggregation)               │
└────────────────────────┬───────────────────────────────┘
                         │ (Maps to/from DTOs)
                         ▼
┌────────────────────────────────────────────────────────┐
│                     Domain Layer                       │
│   - Transaction (Input Model)                          │
│   - RewardResponse (Output DTO Model)                  │
└────────────────────────────────────────────────────────┘
```

- **Presentation Layer**: Handles incoming HTTP POST requests (`/api/rewards/calculate`) and GET requests (`/api/rewards/mock-demo`). It handles request serialization, maps validation failures to user-friendly JSON payloads via the global exception handler, and coordinates with the service layer.
- **Business Layer**: The core logical center of the application. It validates data fields, enforces the sliding 3-month window check, evaluates individual rewards, and aggregates results.
- **Domain Layer**: Contains immutable/data objects representing business models using Lombok to eliminate boilerplate code.

---

## 📁 Package Structure

The package directory layout is designed according to standard Java domain-driven package organization principles:

```
src/main/java/com/retail/rewards/
├── RewardsApplication.java        # Spring Boot main entry point
├── controller/
│   └── RewardsController.java     # Exposes REST endpoints
├── exception/
│   └── GlobalExceptionHandler.java# Intercepts exceptions, translates into HTTP status codes
├── model/
│   ├── RewardResponse.java        # Output data transfer object (monthly & total summary)
│   └── Transaction.java           # Input data object representing a transaction
└── service/
    └── RewardsService.java        # Business logic, point calculation, & validations
```

---

## 🧮 Reward Calculation Algorithm

Points are calculated on a per-transaction basis. The calculation maintains full decimal precision during calculation steps and truncates only at the final step to prevent premature casting precision loss.

### The Algorithm:
For each transaction with a purchase amount $A$:
1. If $A \le 50$, points earned = $0$.
2. If $50 < A \le 100$, points earned = $\lfloor A - 50 \rfloor \times 1$.
3. If $A > 100$, points earned = $(\lfloor A - 100 \rfloor \times 2) + (50 \times 1)$.

Mathematically, this can be formulated as:
$$\text{Points}(A) = \begin{cases} 
      0 & \text{if } A \le 50 \\
      \lfloor A - 50 \rfloor & \text{if } 50 < A \le 100 \\
      2 \times \lfloor A - 100 \rfloor + 50 & \text{if } A > 100 
   \end{cases}$$

### Step-by-Step Example Calculations:

*   **Example 1 (Below Threshold):** Purchase of **$45.00**
    *   $45.00 \le 50 \implies \mathbf{0 \text{ points}}$

*   **Example 2 (Exactly Threshold):** Purchase of **$50.00**
    *   $50.00 \le 50 \implies \mathbf{0 \text{ points}}$

*   **Example 3 (Middle Bracket):** Purchase of **$80.50**
    *   Amount over $50$: $80.50 - 50.00 = 30.50$
    *   Points: $\lfloor 30.50 \rfloor \times 1 = \mathbf{30 \text{ points}}$

*   **Example 4 (Upper Bracket with Precision Preservation):** Purchase of **$120.50**
    *   Amount over $100$: $120.50 - 100.00 = 20.50$
    *   Points from $100+$ bracket: $20.50 \times 2 = 41.00$
    *   Points from middle bracket ($50$ to $100$): $50 \times 1 = 50.00$
    *   Total raw points: $41.00 + 50.00 = 91.00$
    *   Final casted points: $\lfloor 91.00 \rfloor = \mathbf{91 \text{ points}}$
    *   *(Note: A premature cast of the intermediate value like `(int)(120.50 - 100.00) * 2` would yield `20 * 2 = 40` points, resulting in a wrong total of `90`. Our algorithm avoids this intermediate precision loss).*

---

## 🗓️ Three-Month Period Validation

The business rules dictate that we calculate rewards for transactions during a **three-month period**. To achieve this, the service enforces a **sliding 3-month window** relative to the dataset itself:

1. **Window Alignment**: The service scans the list of input transactions to find the most recent transaction date ($D_{\text{latest}}$).
2. **Cutoff Calculation**: The start boundary of the window is set exactly 3 months prior to this latest date:
   $$D_{\text{cutoff}} = D_{\text{latest}} - 3 \text{ months}$$
3. **Enforcement**: Every transaction in the input list is validated:
   $$D_{\text{transaction}} \ge D_{\text{cutoff}}$$
   If any transaction occurs before this cutoff date, the service immediately throws an `IllegalArgumentException` indicating that the transaction lies outside the valid three-month window.

This sliding window allows calculations on historical datasets (e.g., Q1 2023) or current real-time datasets without the code becoming obsolete due to hardcoded dates.

---

## 🚫 Input Validations & Exception Handling

The API validates all input fields at the service layer prior to processing. If any validation fails, the service throws an `IllegalArgumentException` which is mapped by `GlobalExceptionHandler` to a standard HTTP `400 Bad Request` JSON response:

| Constraint | Validation Checked | Action on Failure |
| :--- | :--- | :--- |
| **Transaction List** | Checks if list is null or empty | Returns `400 Bad Request` |
| **Transaction Object** | Checks if any element in the list is null | Throws `IllegalArgumentException("Transaction cannot be null")` |
| **Customer ID** | Checks if `customerId` is null | Throws `IllegalArgumentException("Customer ID cannot be null")` |
| **Amount** | Checks if `amount` is null | Throws `IllegalArgumentException("Transaction amount cannot be null")` |
| **Negative Amount** | Checks if `amount` is negative ($< 0$) | Throws `IllegalArgumentException("Transaction amount cannot be negative")` |
| **Transaction Date** | Checks if `transactionDate` is null | Throws `IllegalArgumentException("Transaction date cannot be null")` |
| **Three-Month Window** | Checks if any transaction date is before $D_{\text{cutoff}}$ | Throws `IllegalArgumentException("Transaction date ... is outside the three-month window...")` |

---

## 🛣️ API Endpoints

### 1. Calculate Rewards
*   **Method**: `POST`
*   **Path**: `/api/rewards/calculate`
*   **Headers**: `Content-Type: application/json`
*   **Request Body**:
    ```json
    [
      {
        "id": 1,
        "customerId": 1,
        "amount": 120.50,
        "transactionDate": "2026-05-15"
      },
      {
        "id": 2,
        "customerId": 1,
        "amount": 80.00,
        "transactionDate": "2026-04-10"
      }
    ]
    ```
*   **Response Body (`200 OK`)**:
    ```json
    [
      {
        "customerId": 1,
        "monthlyPoints": {
          "May 2026": 91,
          "April 2026": 30
        },
        "totalPoints": 121
      }
    ]
    ```

### 2. Mock Data Demo
Useful for quick demonstration and testing of the 3-month window calculations using static, fixed data.
*   **Method**: `GET`
*   **Path**: `/api/rewards/mock-demo`
*   **Response Body (`200 OK`)**:
    ```json
    [
      {
        "customerId": 1,
        "monthlyPoints": {
          "March 2026": 120,
          "April 2026": 150,
          "May 2026": 10
        },
        "totalPoints": 280
      },
      {
        "customerId": 2,
        "monthlyPoints": {
          "March 2026": 250,
          "April 2026": 0
        },
        "totalPoints": 250
      }
    ]
    ```

---

## ⚙️ How to Build and Run

### Prerequisites
- Java 17
- Maven 3.x

### Build the Application
Run Maven compiler and packager:
```bash
mvn clean install
```

### Run the Application
Start the Spring Boot application:
```bash
mvn spring-boot:run
```
The application will start on port `8080` by default. You can access it at `http://localhost:8080`.

### Run Automated Tests
Execute the comprehensive unit and integration test suite:
```bash
mvn test
```
The test suite validates:
- Positive points calculation with decimal precision preservation.
- Edge cases ($50 threshold boundary, negative amounts).
- Multi-customer aggregations.
- Custom month-year display grouping (`MMMM yyyy`).
- 3-month sliding window constraint.
- Input validations (null fields, empty payloads).
