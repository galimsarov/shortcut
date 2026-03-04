package com.example.c_advanced_core.h_serialization.b_tasks.a_basic;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProfileSerializer {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Path dir = Path.of("tmp/serialization");
        Files.createDirectories(dir);

        Path file = dir.resolve("user-profile.bin");

        UserProfile userProfile = new UserProfile("crazyFrog", "crazeFrog@gmail.com", 42);
        System.out.println("Before serialization: " + userProfile);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            oos.writeObject(userProfile);
        }

        UserProfile restored;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toFile()))) {
            restored = (UserProfile) ois.readObject();
        }

        System.out.println("After deserialization: " + restored);
    }
}
