package com.example.c_advanced_core.d_exceptions.d_tasks.b_payment_validation;

public class CardExpiredException extends PaymentException {
    String cardId;

    public CardExpiredException(String cardId, String message) {
        super(message);
        this.cardId = cardId;
    }

    public String getCardId() {
        return cardId;
    }
}
