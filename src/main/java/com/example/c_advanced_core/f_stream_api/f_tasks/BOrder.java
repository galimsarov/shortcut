package com.example.c_advanced_core.f_stream_api.f_tasks;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BOrder {
    private int id;
    private String customer;
    private double amount;
    private String status;
    private LocalDateTime createdAt;

    public BOrder(int id, String customer, double amount, String status, LocalDateTime createdAt) {
        this.id = id;
        this.customer = customer;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "BOrder{" +
                "id=" + id +
                ", customer='" + customer + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
