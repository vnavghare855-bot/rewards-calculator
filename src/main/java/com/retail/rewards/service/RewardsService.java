package com.retail.rewards.service;

import com.retail.rewards.model.RewardResponse;
import com.retail.rewards.model.Transaction;
import org.springframework.stereotype.Service;

import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to calculate reward points based on customer transactions.
 */
@Service
public class RewardsService {

    /**
     * Calculates reward points for a single transaction amount.
     * 
     * @param amount Transaction amount
     * @return Calculated reward points
     */
    public int calculatePoints(Double amount) {
        if (amount == null || amount <= 50) {
            return 0;
        }
        int points = 0;
        if (amount > 100) {
            points += (int) (amount - 100) * 2;
            points += 50; // Points for amount between 50 and 100
        } else {
            points += (int) (amount - 50);
        }
        return points;
    }

    /**
     * Aggregates transactions and calculates monthly and total reward points for each customer.
     * 
     * @param transactions List of customer transactions
     * @return List of RewardResponse objects
     */
    public List<RewardResponse> getRewards(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<Transaction>> transactionsByCustomer = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCustomerId));

        List<RewardResponse> responses = new ArrayList<>();

        for (Map.Entry<Long, List<Transaction>> entry : transactionsByCustomer.entrySet()) {
            Long customerId = entry.getKey();
            List<Transaction> customerTransactions = entry.getValue();

            Map<String, Integer> monthlyPoints = new LinkedHashMap<>();
            int totalPoints = 0;

            for (Transaction transaction : customerTransactions) {
                String month = transaction.getTransactionDate().getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                int points = calculatePoints(transaction.getAmount());

                monthlyPoints.put(month, monthlyPoints.getOrDefault(month, 0) + points);
                totalPoints += points;
            }

            responses.add(new RewardResponse(customerId, monthlyPoints, totalPoints));
        }

        return responses;
    }
}
