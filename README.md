# Retail Reward Points Calculator

## Project Overview
This is a Spring Boot REST API that calculates reward points for customers based on their transaction history.

### Reward Calculation Rules:
- 2 points for every dollar spent over $100 in each transaction.
- 1 point for every dollar spent between $50 and $100 in each transaction.
- *Example:* A $120 purchase yields 90 points (2x$20 + 1x$50).

## Project Structure
- `src/main/java/com/retail/rewards`: Main application code.
  - `controller`: Contains REST endpoints.
  - `service`: Contains business logic for point calculation and aggregation.
  - `model`: Contains Data Transfer Objects (DTOs) for transactions and responses.
  - `exception`: Contains global error handling logic.
- `src/test/java/com/retail/rewards`: Unit and integration tests.

## API Endpoints

### 1. Calculate Rewards
**POST** `/api/rewards/calculate`

Request Body:
```json
[
  {
    "id": 1,
    "customerId": 1,
    "amount": 120.0,
    "transactionDate": "2023-05-01"
  }
]
```

### 2. Mock Data Demo
**GET** `/api/rewards/mock-demo`

Returns a pre-populated set of transactions and their reward calculations for demonstration.

## How to Run
1. Ensure you have Java 17 and Maven installed.
2. Run `mvn clean install` to build the project.
3. Run `mvn spring-boot:run` to start the application.
4. The API will be available at `http://localhost:8080`.

## Testing
Run `mvn test` to execute unit and integration tests.
Tests cover:
- Edge cases for reward calculation (below 50, exactly 100, etc.)
- Multi-customer and multi-month aggregation.
- API error handling (empty request).

## GitLab Submission Guide
Follow these steps to upload your project to GitLab:

1. **Initialize Git Repository:**
   ```bash
   git init
   ```

2. **Add .gitignore:**
   Create a `.gitignore` file and add `target/`, `.idea/`, `.vscode/`, etc. (Done for you in this project).

3. **Stage and Commit:**
   ```bash
   git add .
   git commit -m "Initial commit: Rewards Calculator Project"
   ```

4. **Create a GitLab Repository:**
   - Log in to [gitlab.com](https://gitlab.com).
   - Click "New project" -> "Create blank project".
   - Name it `rewards-calculator`.

5. **Push to GitLab:**
   ```bash
   git remote add origin https://gitlab.com/YOUR_USERNAME/rewards-calculator.git
   git branch -M main
   git push -u origin main
   ```
