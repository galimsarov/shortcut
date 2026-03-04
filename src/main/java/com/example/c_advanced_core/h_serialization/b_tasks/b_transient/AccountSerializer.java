package com.example.c_advanced_core.h_serialization.b_tasks.b_transient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class AccountSerializer {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Path dir = Path.of("tmp/serialization");
        Files.createDirectories(dir);

        Path file = dir.resolve("account.bin");

        try {
            Account account = new Account("asdf", "asdf@asdf.com", "asdf@asdf.com");
            System.out.println("Before serialization: " + account);

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
                oos.writeObject(account);
            }

            Account restored;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toFile()))) {
                restored = (Account) ois.readObject();
            }
            System.out.println("After deserialization: " + restored);
        } catch (IllegalArgumentException e) {
            System.out.println("Bad account: " + e);
        }
    }
}
