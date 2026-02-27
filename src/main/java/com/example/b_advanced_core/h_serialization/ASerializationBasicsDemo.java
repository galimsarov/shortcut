package com.example.b_advanced_core.h_serialization;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Сериализация — это преобразование объекта в поток байтов (например, для сохранения в файл или передачи по сети),
 * а десериализация — обратный процесс.
 * В Java это делается через ObjectOutputStream / ObjectInputStream.
 */
public class ASerializationBasicsDemo {

    public static void main(String[] args) throws Exception {
        Path dir = Path.of("tmp/serialization");
        Files.createDirectories(dir);

        Path file = dir.resolve("user.bin");

        User user = new User(42L, "alice", "secret-token");
        System.out.println("Before serialization: " + user);

        serialize(user, file);

        User restored = deserialize(file);
        System.out.println("After deserialization: " + restored);
        System.out.println("After deserialization token = " + restored.getSessionToken()); // null (transient)
    }

    /**
     * Пример базовой сериализации:
     * @param user
     * @param file
     * @throws IOException
     */
    private static void serialize(User user, Path file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            out.writeObject(user);
        }
    }

    private static User deserialize(Path file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file.toFile()))) {
            return (User) in.readObject();
        }
    }

    /**
     * Чтобы объект можно было сериализовать:
     * - класс должен реализовывать java.io.Serializable (маркерный интерфейс);
     * - все вложенные поля, которые должны попасть в поток, тоже должны быть сериализуемыми;
     * - поля, которые нельзя/не нужно сохранять, помечают transient;
     * - static-поля не сериализуются (это состояние класса, а не объекта).
     */
    private static final class User implements Serializable {
        private static final long serialVersionUID = 1L;

        private final long id;
        private final String login;
        private transient String sessionToken;

        private User(long id, String login, String sessionToken) {
            this.id = id;
            this.login = login;
            this.sessionToken = sessionToken;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", login='" + login + '\'' +
                    ", sessionToken='" + sessionToken + '\'' +
                    '}';
        }
    }
}
