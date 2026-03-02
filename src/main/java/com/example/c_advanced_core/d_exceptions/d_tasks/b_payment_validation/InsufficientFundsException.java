package com.example.c_advanced_core.d_exceptions.d_tasks.b_payment_validation;

public class InsufficientFundsException extends PaymentException {
    private final String accountId;

    public InsufficientFundsException(String accountId, String message) {
        super(message);
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
