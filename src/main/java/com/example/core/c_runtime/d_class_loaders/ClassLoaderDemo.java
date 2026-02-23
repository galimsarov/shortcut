package com.example.core.c_runtime.d_class_loaders;

import lombok.extern.java.Log;

@Log
public class ClassLoaderDemo {
    public static void main(String[] args) {
        ClassLoader app = ClassLoaderDemo.class.getClassLoader();
        log.info("App loader: " + app);

        ClassLoader platform = app.getParent();
        log.info("Platform loader: " + platform);

        ClassLoader bootstrap =
                platform != null
                        ? platform.getParent()
                        : null;
        log.info("Bootstrap loader (обычно null в выводе): " + bootstrap);

        // Классы JDK обычно загружены bootstrap-загрузчиком
        log.info("String loader: " + String.class.getClassLoader()); // null
    }
}
