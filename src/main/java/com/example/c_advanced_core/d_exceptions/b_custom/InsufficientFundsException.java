package com.example.c_advanced_core.d_exceptions.b_custom;

public class InsufficientFundsException extends RuntimeException {
    private final String accountId;

    public InsufficientFundsException(String accountId, String message) {
        super(message);
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
