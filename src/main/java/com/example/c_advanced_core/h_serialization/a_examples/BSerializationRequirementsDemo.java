package com.example.c_advanced_core.h_serialization.a_examples;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class BSerializationRequirementsDemo {

    public static void main(String[] args) throws Exception {
        Path dir = Path.of("tmp/serialization");
        Files.createDirectories(dir);

        Path badFile = dir.resolve("order-bad.bin");
        Path goodFile = dir.resolve("order-good.bin");

        OrderBad bad = new OrderBad("ORD-1", new AuditTrail("created-by-system"));
        try {
            writeToFile(bad, badFile);
        } catch (NotSerializableException e) {
            System.out.println("As expected, we received a NotSerializableException: " + e.getMessage());
        }

        OrderGood good = new OrderGood("ORD-2", new AuditTrail("approved-by-admin"));
        writeToFile(good, goodFile);
        OrderGood restored = (OrderGood) readFromFile(goodFile);

        System.out.println("After correct serialization: " + restored);
    }

    private static void writeToFile(Object obj, Path file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            out.writeObject(obj);
        }
    }

    private static Object readFromFile(Path file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file.toFile()))) {
            return in.readObject();
        }
    }

    private static final class OrderBad implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String number;
        private final AuditTrail auditTrail; // Не Serializable -> ошибка

        private OrderBad(String number, AuditTrail auditTrail) {
            this.number = number;
            this.auditTrail = auditTrail;
        }
    }

    private static final class OrderGood implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String number;
        private transient AuditTrail auditTrail; // исключили из состояния

        private OrderGood(String number, AuditTrail auditTrail) {
            this.number = number;
            this.auditTrail = auditTrail;
        }

        @Override
        public String toString() {
            return "OrderGood{" +
                    "number='" + number + '\'' +
                    ", auditTrail=" + auditTrail +
                    '}';
        }
    }

    private static final class AuditTrail {
        private final String comment;

        private AuditTrail(String comment) {
            this.comment = comment;
        }

        @Override
        public String toString() {
            return "AuditTrail{" +
                    "comment='" + comment + '\'' +
                    '}';
        }
    }
}
