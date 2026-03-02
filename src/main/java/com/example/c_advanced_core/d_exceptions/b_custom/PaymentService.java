package com.example.c_advanced_core.d_exceptions.b_custom;

public class PaymentService {
    public void withdraw(String accountId, int balance, int amount) {
        if (amount > balance) {
            throw new InsufficientFundsException(
                    accountId,
                    "Insufficient funds: balance=" + balance + ", amount=" + amount
            );
        }
        // списание
    }
}
