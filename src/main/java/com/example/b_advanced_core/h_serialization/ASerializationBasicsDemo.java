package com.example.b_advanced_core.h_serialization;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ASerializationBasicsDemo {

    public static void main(String[] args) throws Exception {
        Path dir = Path.of("tmp/serialization");
        Files.createDirectories(dir);

        Path file = dir.resolve("user.bin");

        User user = new User(42L, "alice", "secret-token");
        System.out.println("До сериализации: " + user);

        serialize(user, file);

        User restored = deserialize(file);
        System.out.println("После десериализации: " + restored);
        System.out.println("token после десериализации = " + restored.getSessionToken()); // null (transient)
    }

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
