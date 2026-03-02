package com.example.c_advanced_core.i_copy.a_shallow_deep;

import java.util.ArrayList;
import java.util.List;

public class CopyDemo {
    public static void main(String[] args) {
        User original = new User("Alice", new Address("Berlin"), new ArrayList<>(List.of("vip")));

        User shallow = original.shallowCopy();
        User deep = original.deepCopy();

        original.address.city = "Paris";
        original.tags.add("new");

        System.out.println(shallow.address.city); // Paris  (общий Address)
        System.out.println(shallow.tags);         // [vip, new] (общий List)

        System.out.println(deep.address.city);    // Berlin (независимая копия)
        System.out.println(deep.tags);            // [vip]
    }
}
