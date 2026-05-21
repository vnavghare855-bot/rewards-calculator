package com.retail.rewards.service;

import com.retail.rewards.model.RewardResponse;
import com.retail.rewards.model.Transaction;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to calculate reward points based on customer transactions.
 */
@Service
public class RewardsService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    /**
     * Calculates reward points for a single transaction amount.
     * Performs double-precision calculations to prevent precision loss.
     * 
     * @param amount Transaction amount
     * @return Calculated reward points
     */
    public int calculatePoints(Double amount) {
        if (amount == null || amount <= 50) {
            return 0;
        }
        double points = 0;
        if (amount > 100) {
            points += (amount - 100) * 2;
            points += 50; // Points for amount between 50 and 100
        } else {
            points += (amount - 50);
        }
        return (int) points;
    }

    /**
     * Aggregates transactions and calculates monthly and total reward points for each customer.
     * Performs validations for null fields, negative amounts, and three-month periods.
     * 
     * @param transactions List of customer transactions
     * @return List of RewardResponse objects
     * @throws IllegalArgumentException if transaction data is invalid or outside the 3-month window
     */
    public List<RewardResponse> getRewards(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Collections.emptyList();
        }

        // Validate individual transaction fields
        validateTransactions(transactions);

        // Enforce three-month period requirement
        validateThreeMonthPeriod(transactions);

        Map<Long, List<Transaction>> transactionsByCustomer = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCustomerId));

        List<RewardResponse> responses = new ArrayList<>();

        for (Map.Entry<Long, List<Transaction>> entry : transactionsByCustomer.entrySet()) {
            Long customerId = entry.getKey();
            List<Transaction> customerTransactions = entry.getValue();

            Map<String, Integer> monthlyPoints = new LinkedHashMap<>();
            int totalPoints = 0;

            for (Transaction transaction : customerTransactions) {
                String month = transaction.getTransactionDate().format(MONTH_FORMATTER);
                int points = calculatePoints(transaction.getAmount());

                monthlyPoints.put(month, monthlyPoints.getOrDefault(month, 0) + points);
                totalPoints += points;
            }

            responses.add(new RewardResponse(customerId, monthlyPoints, totalPoints));
        }

        return responses;
    }

    /**
     * Validates that all fields in the transactions list are present and valid.
     * 
     * @param transactions List of transactions to validate
     */
    private void validateTransactions(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            if (transaction == null) {
                throw new IllegalArgumentException("Transaction cannot be null");
            }
            if (transaction.getCustomerId() == null) {
                throw new IllegalArgumentException("Customer ID cannot be null");
            }
            if (transaction.getAmount() == null) {
                throw new IllegalArgumentException("Transaction amount cannot be null");
            }
            if (transaction.getAmount() < 0) {
                throw new IllegalArgumentException("Transaction amount cannot be negative");
            }
            if (transaction.getTransactionDate() == null) {
                throw new IllegalArgumentException("Transaction date cannot be null");
            }
        }
    }

    /**
     * Enforces that all transactions in the list fall within a three-month window.
     * The window is relative to the latest transaction date in the dataset.
     * 
     * @param transactions List of transactions to validate
     */
    private void validateThreeMonthPeriod(List<Transaction> transactions) {
        LocalDate latestDate = transactions.stream()
                .map(Transaction::getTransactionDate)
                .max(LocalDate::compareTo)
                .orElse(null);

        if (latestDate != null) {
            LocalDate cutoffDate = latestDate.minusMonths(3);
            for (Transaction transaction : transactions) {
                if (transaction.getTransactionDate().isBefore(cutoffDate)) {
                    throw new IllegalArgumentException("Transaction date " + transaction.getTransactionDate() 
                            + " is outside the three-month window (cutoff: " + cutoffDate + ")");
                }
            }
        }
    }

    /**
     * Generates mock transactions for demonstrating a three-month period.
     * Uses static, fixed dates to avoid dynamic date shifts.
     * 
     * @return a list of mock Transaction objects
     */
    public List<Transaction> getMockTransactions() {
        return Arrays.asList(
                // Customer 1: March 2026
                new Transaction(1L, 1L, 120.0, LocalDate.of(2026, 3, 15)),
                new Transaction(2L, 1L, 80.0, LocalDate.of(2026, 3, 20)),
                // Customer 1: April 2026
                new Transaction(3L, 1L, 150.0, LocalDate.of(2026, 4, 10)),
                // Customer 1: May 2026
                new Transaction(4L, 1L, 60.0, LocalDate.of(2026, 5, 5)),

                // Customer 2: March 2026
                new Transaction(5L, 2L, 200.0, LocalDate.of(2026, 3, 15)),
                // Customer 2: April 2026
                new Transaction(6L, 2L, 40.0, LocalDate.of(2026, 4, 10))
        );
    }
}
