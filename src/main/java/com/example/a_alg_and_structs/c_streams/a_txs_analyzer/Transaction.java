package com.example.a_alg_and_structs.c_streams.a_txs_analyzer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Transaction {
    private String category;
    private BigDecimal amount;
    private LocalDateTime date;

    public Transaction(String category, BigDecimal amount, LocalDateTime date) {
        this.category = category;
        this.amount = amount;
        this.date = date;
    }
}
