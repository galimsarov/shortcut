package com.example.c_advanced_core.d_exceptions.d_tasks.b_payment_validation;

public class PaymentGatewayException extends PaymentException {
    private final String errorCode;

    public PaymentGatewayException(String errorCode,String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
