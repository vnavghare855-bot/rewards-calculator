package com.retail.rewards.controller;

import com.retail.rewards.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
public class RewardsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCalculateRewards_Success() throws Exception {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 1L, 120.0, LocalDate.now())
        );

        mockMvc.perform(post("/api/rewards/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].totalPoints").value(90));
    }

    @Test
    public void testCalculateRewards_EmptyList() throws Exception {
        mockMvc.perform(post("/api/rewards/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCalculateRewards_NegativeAmount_ReturnsBadRequest() throws Exception {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 1L, -100.0, LocalDate.now())
        );

        mockMvc.perform(post("/api/rewards/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Transaction amount cannot be negative"));
    }

    @Test
    public void testCalculateRewards_NullDate_ReturnsBadRequest() throws Exception {
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 1L, 100.0, null)
        );

        mockMvc.perform(post("/api/rewards/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Transaction date cannot be null"));
    }

    @Test
    public void testCalculateRewards_OutsideWindow_ReturnsBadRequest() throws Exception {
        LocalDate now = LocalDate.now();
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, 1L, 100.0, now),
                new Transaction(2L, 1L, 80.0, now.minusMonths(4))
        );

        mockMvc.perform(post("/api/rewards/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactions)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("outside the three-month window")));
    }

    @Test
    public void testMockDemo_Success() throws Exception {
        mockMvc.perform(get("/api/rewards/mock-demo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].totalPoints").value(280))
                .andExpect(jsonPath("$[1].customerId").value(2))
                .andExpect(jsonPath("$[1].totalPoints").value(250));
    }
}
