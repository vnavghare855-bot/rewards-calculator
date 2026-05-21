package com.retail.rewards.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents a retail transaction for which reward points can be calculated.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    /** The unique identifier of the transaction. */
    private Long id;

    /** The identifier of the customer who made the transaction. */
    private Long customerId;

    /** The purchase amount of the transaction. */
    private Double amount;

    /** The date when the transaction occurred. */
    private LocalDate transactionDate;
}
