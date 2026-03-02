package com.example.c_advanced_core.d_exceptions.d_tasks.b_payment_validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

public class PaymentDemo {
    public static void main(String[] args) {
        payWithLogging(
                LocalDate.of(2026, Month.JANUARY, 31),
                "accountId",
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                null
        );
        payWithLogging(
                LocalDate.of(2026, Month.AUGUST, 31),
                "accountId",
                new BigDecimal("100.00"),
                new BigDecimal("110.00"),
                null
        );
        payWithLogging(
                LocalDate.of(2026, Month.AUGUST, 31),
                "accountId",
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                "Internal server error"
        );
        payWithLogging(
                LocalDate.of(2026, Month.AUGUST, 31),
                "accountId",
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                null
        );
    }

    private static void payWithLogging(
            LocalDate endCardDate, String accountId, BigDecimal balance, BigDecimal amount, String errorCode
    ) {
        try {
            pay(
                    "cardId",
                    endCardDate,
                    balance,
                    amount,
                    accountId,
                    errorCode
                    );
            System.out.println("Payment successful");
        } catch (CardExpiredException e) {
            System.out.println(e.getMessage() + " for cardId: " + e.getCardId());
        } catch (InsufficientFundsException e) {
            System.out.println(e.getMessage() + " for accountId: " + e.getAccountId());
        } catch (PaymentGatewayException e) {
            System.out.println(e.getMessage() + ": " + e.getErrorCode());
        } finally {
            System.out.println("Operation ended");
        }

    }

    private static void pay(
            String cardId,
            LocalDate endCardDate,
            BigDecimal accountBalance,
            BigDecimal amount,
            String accountId,
            String errorCode
    ) throws PaymentException {
        if (!endCardDate.isAfter(LocalDate.now())) {
            throw new CardExpiredException(cardId, "End Card Date");
        }
        if (amount.compareTo(accountBalance) > 0) {
            throw new InsufficientFundsException(accountId, "Insufficient Funds");
        }
        if (errorCode != null) {
            throw new PaymentGatewayException(errorCode, "Payment Gateway Error");
        }
    }
}
