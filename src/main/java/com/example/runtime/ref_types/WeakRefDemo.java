package com.example.runtime.ref_types;

import java.lang.ref.WeakReference;

public class WeakRefDemo {
    public static void main(String[] args) {
        Object obj = new Object();
        WeakReference<Object> weakRef = new WeakReference<>(obj);
        obj = null;
        System.gc();
        System.out.println(weakRef.get());
    }
}
