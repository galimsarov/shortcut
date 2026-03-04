package com.example.c_advanced_core.h_serialization.b_tasks.b_transient;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Сделать класс Account, где часть данных не должна сохраняться напрямую.
 * Требования:
 * - Поля: login, passwordHash, sessionToken.
 * - sessionToken сделать transient.
 * - Реализовать writeObject/readObject для дополнительной валидации (например, проверка формата login).
 * Показать, какие поля сохранились/восстановились после десериализации.
 */
@Getter
@Setter
@ToString
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    private String login;
    private String passwordHash;
    private transient String sessionToken;

    public Account(String login, String passwordHash, String sessionToken) {
        if (login.length() < 4) {
            throw new IllegalArgumentException("Login length must be at least 4 characters");
        }
        this.login = login;
        this.passwordHash = passwordHash;
        this.sessionToken = sessionToken;
    }
}
