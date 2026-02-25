package com.example.a_core.b_oop.a_encapsulationinheritancepolymorphism;

/**
 * Пакет 1: базовые принципы ООП.
 *
 * Здесь показаны:
 * - Инкапсуляция: поля приватные, доступ через методы.
 * - Наследование: Cat наследуется от Animal.
 * - Полиморфизм: переменная типа Animal хранит Cat и вызывает переопределённый метод.
 */
public final class BasicOopExample {

    private BasicOopExample() {
    }

    public static void demo() {
        // Инкапсуляция: внутреннее состояние меняется только через методы класса.
        BankAccount account = new BankAccount("A-001", 1_000);
        account.deposit(250);
        account.withdraw(100);
        System.out.println("Баланс счёта " + account.getNumber() + ": " + account.getBalance());

        // Наследование + полиморфизм.
        Animal animal = new Cat("Tom"); // ссылка базового типа
        System.out.println("Animal: " + animal.getName());
        System.out.println("Sound: " + animal.makeSound()); // вызов переопределённого метода Cat
    }

    static class BankAccount {
        private final String number;
        private int balance;

        BankAccount(String number, int initialBalance) {
            this.number = number;
            this.balance = Math.max(initialBalance, 0);
        }

        public String getNumber() {
            return number;
        }

        public int getBalance() {
            return balance;
        }

        public void deposit(int amount) {
            if (amount > 0) {
                balance += amount;
            }
        }

        public boolean withdraw(int amount) {
            if (amount <= 0 || amount > balance) {
                return false;
            }
            balance -= amount;
            return true;
        }
    }

    static class Animal {
        private final String name;

        Animal(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String makeSound() {
            return "...";
        }
    }

    static class Cat extends Animal {
        Cat(String name) {
            super(name);
        }

        @Override
        public String makeSound() {
            return "Meow";
        }
    }
}
