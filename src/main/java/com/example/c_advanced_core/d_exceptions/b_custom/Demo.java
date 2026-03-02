package com.example.c_advanced_core.d_exceptions.b_custom;

/**
 * Хорошая практика — вводить доменные исключения с понятным смыслом.
 * Советы:
 * - Называйте исключения по бизнес-смыслу (UserNotFoundException, OrderAlreadyPaidException).
 * - Не теряйте причину: используйте конструкторы с cause, если заворачиваете чужое исключение.
 */
public class Demo {
    public static void main(String[] args) {
        PaymentService paymentService = new PaymentService();
        try {
            paymentService.withdraw("id", 10, 20);
        } catch (InsufficientFundsException e) {
            System.out.println(e.getMessage());
        }
    }
}
