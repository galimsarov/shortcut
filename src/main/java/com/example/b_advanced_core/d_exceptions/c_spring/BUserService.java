package com.example.b_advanced_core.d_exceptions.c_spring;

/**
 * Сервис
 */
//@Service
public class BUserService {
    public String findUserName(Long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id должен быть положительным");
        }
        if (id == 404L) {
            throw new AUserNotFoundException(id);
        }
        return "Alice";
    }
}
