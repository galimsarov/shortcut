package com.example.b_advanced_core.d_exceptions.c_spring;

/**
 * Доменное исключение
 */
public class AUserNotFoundException extends RuntimeException {
    public AUserNotFoundException(Long id) {
        super("User with id=" + id + " not found");
    }
}
