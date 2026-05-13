package com.retail.rewards.service;

import com.retail.rewards.model.RewardResponse;
import com.retail.rewards.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RewardsServiceTest {

    @Autowired
    private RewardsService rewardsService;

    @Test
    public void testCalculatePoints_Above100() {
        assertEquals(90, rewardsService.calculatePoints(120.0));
    }

    @Test
    public void testCalculatePoints_Exactly100() {
        assertEquals(50, rewardsService.calculatePoints(100.0));
    }

    @Test
    public void testCalculatePoints_Between50And100() {
        assertEquals(30, rewardsService.calculatePoints(80.0));
    }

    @Test
    public void testCalculatePoints_Below50() {
        assertEquals(0, rewardsService.calculatePoints(40.0));
    }

    @Test
    public void testCalculatePoints_Negative() {
        assertEquals(0, rewardsService.calculatePoints(-10.0));
    }

    @Test
    public void testGetRewards_MultipleCustomersAndMonths() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 1L, 120.0, LocalDate.of(2023, 1, 15)),
                new Transaction(2L, 1L, 80.0, LocalDate.of(2023, 1, 20)),
                new Transaction(3L, 1L, 150.0, LocalDate.of(2023, 2, 10)),
                new Transaction(4L, 2L, 200.0, LocalDate.of(2023, 1, 5))
        );

        List<RewardResponse> responses = rewardsService.getRewards(transactions);

        assertEquals(2, responses.size());
        
        RewardResponse cust1 = responses.stream().filter(r -> r.getCustomerId() == 1L).findFirst().get();
        assertEquals(120, cust1.getMonthlyPoints().get("January")); // 90 (from 120) + 30 (from 80)
        assertEquals(150, cust1.getMonthlyPoints().get("February")); // 150 (from 150)
        assertEquals(270, cust1.getTotalPoints());

        RewardResponse cust2 = responses.stream().filter(r -> r.getCustomerId() == 2L).findFirst().get();
        assertEquals(250, cust2.getMonthlyPoints().get("January")); // 250 (from 200: (200-100)*2 + 50)
        assertEquals(250, cust2.getTotalPoints());
    }
}
