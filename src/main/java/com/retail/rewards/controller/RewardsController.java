package com.retail.rewards.controller;

import com.retail.rewards.model.RewardResponse;
import com.retail.rewards.model.Transaction;
import com.retail.rewards.service.RewardsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Helper endpoint to get static mock transaction data and its reward calculation.
     * Useful for demonstration purposes to show a proper three-month period.
     * 
     * @return Reward points summary for mock transactions
     */
    @GetMapping("/mock-demo")
    public ResponseEntity<List<RewardResponse>> getMockRewards() {
        List<Transaction> mockTransactions = rewardsService.getMockTransactions();
        return ResponseEntity.ok(rewardsService.getRewards(mockTransactions));
    }
}
