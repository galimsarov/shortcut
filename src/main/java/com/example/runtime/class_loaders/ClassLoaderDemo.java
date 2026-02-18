package com.example.runtime.class_loaders;

public class ClassLoaderDemo {
    public static void main(String[] args) {
        ClassLoader app = ClassLoaderDemo.class.getClassLoader();
        System.out.println("App loader: " + app);

        ClassLoader platform = app.getParent();
        System.out.println("Platform loader: " + platform);

        ClassLoader bootstrap =
                platform != null
                        ? platform.getParent()
                        : null;
        System.out.println("Bootstrap loader (обычно null в выводе): " + bootstrap);

        // Классы JDK обычно загружены bootstrap-загрузчиком
        System.out.println("String loader: " + String.class.getClassLoader()); // null
    }
}
