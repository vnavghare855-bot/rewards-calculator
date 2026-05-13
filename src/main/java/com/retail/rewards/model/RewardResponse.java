package com.retail.rewards.model;

import lombok.Data;
import java.util.Map;

/**
 * Represents the reward points response for a customer.
 */
@Data
public class RewardResponse {
    private Long customerId;
    private Map<String, Integer> monthlyPoints;
    private int totalPoints;

    public RewardResponse(Long customerId, Map<String, Integer> monthlyPoints, int totalPoints) {
        this.customerId = customerId;
        this.monthlyPoints = monthlyPoints;
        this.totalPoints = totalPoints;
    }
}
