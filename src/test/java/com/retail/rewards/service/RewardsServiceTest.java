package com.retail.rewards.service;

import com.retail.rewards.model.RewardResponse;
import com.retail.rewards.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void testCalculatePoints_Exactly50() {
        assertEquals(0, rewardsService.calculatePoints(50.0));
    }

    @Test
    public void testCalculatePoints_Negative() {
        assertEquals(0, rewardsService.calculatePoints(-10.0));
    }

    @Test
    public void testCalculatePoints_PrecisionLoss() {
        // 120.5: (120.5 - 100) * 2 + 50 = 41.0 + 50 = 91
        assertEquals(91, rewardsService.calculatePoints(120.5));
        // 80.5: 80.5 - 50 = 30.5 => 30
        assertEquals(30, rewardsService.calculatePoints(80.5));
    }

    @Test
    public void testGetRewards_MultipleCustomersAndMonths() {
        LocalDate now = LocalDate.now();
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 1L, 120.0, now.minusMonths(2)),
                new Transaction(2L, 1L, 80.0, now.minusMonths(2)),
                new Transaction(3L, 1L, 150.0, now.minusMonths(1)),
                new Transaction(4L, 2L, 200.0, now.minusMonths(2))
        );

        List<RewardResponse> responses = rewardsService.getRewards(transactions);

        assertEquals(2, responses.size());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
        String monthMinus2 = now.minusMonths(2).format(formatter);
        String monthMinus1 = now.minusMonths(1).format(formatter);

        RewardResponse cust1 = responses.stream().filter(r -> r.getCustomerId() == 1L).findFirst().get();
        assertEquals(120, cust1.getMonthlyPoints().get(monthMinus2)); // 90 (from 120) + 30 (from 80)
        assertEquals(150, cust1.getMonthlyPoints().get(monthMinus1)); // 150 (from 150)
        assertEquals(270, cust1.getTotalPoints());

        RewardResponse cust2 = responses.stream().filter(r -> r.getCustomerId() == 2L).findFirst().get();
        assertEquals(250, cust2.getMonthlyPoints().get(monthMinus2)); // 250 (from 200)
        assertEquals(250, cust2.getTotalPoints());
    }

    @Test
    public void testGetRewards_NullTransaction() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 1L, 120.0, LocalDate.now()),
                null
        );
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            rewardsService.getRewards(transactions);
        });
        assertEquals("Transaction cannot be null", exception.getMessage());
    }

    @Test
    public void testGetRewards_NullCustomerId() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, null, 120.0, LocalDate.now())
        );
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            rewardsService.getRewards(transactions);
        });
        assertEquals("Customer ID cannot be null", exception.getMessage());
    }

    @Test
    public void testGetRewards_NullAmount() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 1L, null, LocalDate.now())
        );
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            rewardsService.getRewards(transactions);
        });
        assertEquals("Transaction amount cannot be null", exception.getMessage());
    }

    @Test
    public void testGetRewards_NegativeAmount() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 1L, -120.0, LocalDate.now())
        );
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            rewardsService.getRewards(transactions);
        });
        assertEquals("Transaction amount cannot be negative", exception.getMessage());
    }

    @Test
    public void testGetRewards_NullTransactionDate() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 1L, 120.0, null)
        );
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            rewardsService.getRewards(transactions);
        });
        assertEquals("Transaction date cannot be null", exception.getMessage());
    }

    @Test
    public void testGetRewards_OutsideThreeMonthWindow() {
        LocalDate now = LocalDate.now();
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 1L, 120.0, now),
                new Transaction(2L, 1L, 80.0, now.minusMonths(2)),
                new Transaction(3L, 1L, 150.0, now.minusMonths(3).minusDays(1)) // outside window
        );
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            rewardsService.getRewards(transactions);
        });
        assertTrue(exception.getMessage().contains("outside the three-month window"));
    }

    @Test
    public void testGetRewards_InsideThreeMonthWindowBoundary() {
        LocalDate now = LocalDate.now();
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 1L, 120.0, now),
                new Transaction(2L, 1L, 80.0, now.minusMonths(3)) // exactly at boundary
        );
        List<RewardResponse> responses = rewardsService.getRewards(transactions);
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    public void testGetRewards_ExpandedMultiCustomer() {
        LocalDate now = LocalDate.now();
        List<Transaction> transactions = Arrays.asList(
                // Customer 1
                new Transaction(1L, 1L, 120.5, now),
                new Transaction(2L, 1L, 99.9, now.minusMonths(1)),
                new Transaction(3L, 1L, 50.1, now.minusMonths(2)),
                // Customer 2
                new Transaction(4L, 2L, 200.0, now),
                new Transaction(5L, 2L, 100.0, now.minusMonths(1)),
                // Customer 3
                new Transaction(6L, 3L, 45.0, now.minusMonths(2))
        );

        List<RewardResponse> responses = rewardsService.getRewards(transactions);
        assertEquals(3, responses.size());

        // Customer 1 verification (120.5 -> 91, 99.9 -> 49, 50.1 -> 0. Total = 140)
        RewardResponse cust1 = responses.stream().filter(r -> r.getCustomerId() == 1L).findFirst().orElseThrow();
        assertEquals(140, cust1.getTotalPoints());

        // Customer 2 verification (200 -> 250, 100 -> 50. Total = 300)
        RewardResponse cust2 = responses.stream().filter(r -> r.getCustomerId() == 2L).findFirst().orElseThrow();
        assertEquals(300, cust2.getTotalPoints());

        // Customer 3 verification (45 -> 0. Total = 0)
        RewardResponse cust3 = responses.stream().filter(r -> r.getCustomerId() == 3L).findFirst().orElseThrow();
        assertEquals(0, cust3.getTotalPoints());
    }
}
