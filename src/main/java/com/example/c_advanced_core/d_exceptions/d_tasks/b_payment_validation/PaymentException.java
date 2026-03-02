package com.example.c_advanced_core.d_exceptions.d_tasks.b_payment_validation;

public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}
