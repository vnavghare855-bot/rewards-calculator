package com.retail.rewards.controller;

import com.retail.rewards.model.RewardResponse;
import com.retail.rewards.model.Transaction;
import com.retail.rewards.service.RewardsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * REST Controller for reward points API.
 */
@RestController
@RequestMapping("/api/rewards")
public class RewardsController {

    @Autowired
    private RewardsService rewardsService;

    /**
     * Endpoint to calculate reward points from a list of transactions.
     * 
     * @param transactions List of transactions
     * @return Reward points summary for each customer
     */
    @PostMapping("/calculate")
    public ResponseEntity<List<RewardResponse>> calculateRewards(@RequestBody List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(rewardsService.getRewards(transactions));
    }

    /**
     * Helper endpoint to get sample data and its reward calculation.
     * Useful for demonstration purposes.
     * 
     * @return Reward points summary for mock transactions
     */
    @GetMapping("/mock-demo")
    public ResponseEntity<List<RewardResponse>> getMockRewards() {
        List<Transaction> mockTransactions = Arrays.asList(
                // Customer 1: Month 1
                new Transaction(1L, 1L, 120.0, LocalDate.now().minusMonths(2)),
                new Transaction(2L, 1L, 80.0, LocalDate.now().minusMonths(2)),
                // Customer 1: Month 2
                new Transaction(3L, 1L, 150.0, LocalDate.now().minusMonths(1)),
                // Customer 1: Month 3
                new Transaction(4L, 1L, 60.0, LocalDate.now()),

                // Customer 2: Month 1
                new Transaction(5L, 2L, 200.0, LocalDate.now().minusMonths(2)),
                // Customer 2: Month 2
                new Transaction(6L, 2L, 40.0, LocalDate.now().minusMonths(1))
        );
        return ResponseEntity.ok(rewardsService.getRewards(mockTransactions));
    }
}
