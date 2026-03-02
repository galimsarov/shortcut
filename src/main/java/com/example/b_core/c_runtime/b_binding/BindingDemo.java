package com.example.b_core.c_runtime.b_binding;

public class BindingDemo {
    public static void main(final String[] args) {
        Parent p = new Child();

        // Раннее связывание: static-метод вызывается по типу ссылки Parent
        p.staticMethod(); // Parent.staticMethod

        // Позднее связывание: instance-метод выбирается по реальному типу Child
        p.instanceMethod(); // Child.instanceMethod

        Child c = new Child();

        // Раннее связывание: static-метод вызывается по типу ссылки Child
        c.staticMethod(); // Child.staticMethod
    }
}
