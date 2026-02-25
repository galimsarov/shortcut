package com.example.a_core.e_abstractions.b_abstract_class;

class Dog extends Animal {
    public Dog(String name) {
        super(name);
    }

    @Override
    public void makeSound() {
        System.out.println(name + ": Bark!");
    }
}

