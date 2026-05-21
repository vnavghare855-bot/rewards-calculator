package com.retail.rewards.model;

import lombok.Data;
import java.util.Map;

/**
 * Represents the reward points summary response for a customer,
 * including monthly breakdown and total points accumulated.
 */
@Data
public class RewardResponse {
    /** The identifier of the customer. */
    private Long customerId;

    /** The breakdown of points earned in each month (formatted as 'Month Year'). */
    private Map<String, Integer> monthlyPoints;

    /** The total points earned across all months. */
    private int totalPoints;

    /**
     * Constructs a new RewardResponse.
     *
     * @param customerId the identifier of the customer
     * @param monthlyPoints the monthly points breakdown
     * @param totalPoints the total points earned
     */
    public RewardResponse(Long customerId, Map<String, Integer> monthlyPoints, int totalPoints) {
        this.customerId = customerId;
        this.monthlyPoints = monthlyPoints;
        this.totalPoints = totalPoints;
    }
}
