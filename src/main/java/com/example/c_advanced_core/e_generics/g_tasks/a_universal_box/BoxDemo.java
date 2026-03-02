package com.example.c_advanced_core.e_generics.g_tasks.a_universal_box;

public class BoxDemo {
    public static void main(String[] args) {
        stringBox();
        System.out.println();
        intBox();
        System.out.println();
        catBox();
        System.out.println();
        rawBox();
    }

    private static void rawBox() {
        Box box = new Box();
        System.out.println("Putting a string into a raw box");
        box.set("1");
        System.out.println("Hello from raw box!");
        System.out.println(box.get());

        System.out.println("Putting int into a raw box");
        box.set(1);
        System.out.println("Hello from raw box!");
        System.out.println(box.get());

        System.out.println("Putting a cat into a raw box");
        box.set(new Cat("Tom"));
        System.out.println("Hello from raw box!");
        System.out.println(box.get());
    }

    private static void catBox() {
        Box<Cat> catBox = new Box<>();
        Cat cat = new Cat("Tom");
        catBox.set(cat);
        System.out.println("Hello from CatBox!");
        System.out.println(catBox.get());
        System.out.println("Hello from empty box:");
        System.out.println(catBox.get());
    }

    private static void intBox() {
        Box<Integer> integerBox = new Box<>();
        integerBox.set(1);
        System.out.println("Hello from intBox!");
        System.out.println(integerBox.get());
        System.out.println("Hello from empty box:");
        System.out.println(integerBox.get());
    }

    private static void stringBox() {
        Box<String> stringBox = new Box<>();
        stringBox.set("Hello from StringBox!");
        System.out.println(stringBox.get());
        System.out.println("Hello from empty box:");
        System.out.println(stringBox.get());
    }
}
