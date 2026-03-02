package com.example.c_advanced_core.e_generics.g_tasks.b_pecs;

import java.util.ArrayList;
import java.util.List;

/**
 * Сделать generic-утилиты для копирования и агрегации чисел.
 *
 * Требования:

 * Метод copy(List<? extends T> src, List<? super T> dst).
 * Метод sumNumbers(List<? extends Number> numbers).
 * Продемонстрировать работу с List<Integer>, List<Double>, List<Number>.
 * Коротко объяснить в комментариях, где producer, а где consumer.
 */
public class PECSDemo<T> {
    private void copy(List<? extends T> src, List<? super T> dst) {
        dst.addAll(src);
    }

    private double sumNumbers(List<? extends Number> numbers) {
        double sum = 0;
        for (Number number : numbers) {
            sum += number.doubleValue();
        }
        return sum;
    }

    public static void main(String[] args) {
        intListDemo();
        doubleListDemo();
        numberListDemo();
        intSumDemo();
        doubleSumDemo();
        numberSumDemo();
    }

    private static void intSumDemo() {
        PECSDemo<Integer> integerPECSDemo = new PECSDemo<>();
        List<Integer> intSrc = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        int sum = (int) integerPECSDemo.sumNumbers(intSrc);
        System.out.println(sum);
    }

    private static void intListDemo() {
        PECSDemo<Integer> integerPECSDemo = new PECSDemo<>();
        List<Integer> intSrc = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        List<Integer> intDst = new ArrayList<>();
        System.out.println("intDst before copy: " + intDst);
        integerPECSDemo.copy(intSrc, intDst);
        System.out.println("intDst after copy: " + intDst);
    }

    private static void doubleSumDemo() {
        PECSDemo<Double> doublePECSDemo = new PECSDemo<>();
        List<Double> doubleSrc = new ArrayList<>(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0));
        double sum = doublePECSDemo.sumNumbers(doubleSrc);
        System.out.println(sum);
    }

    private static void doubleListDemo() {
        PECSDemo<Double> doublePECSDemo = new PECSDemo<>();
        List<Double> doubleSrc = new ArrayList<>(List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0));
        List<Double> doubleDst = new ArrayList<>();
        System.out.println("doubleDst before copy: " + doubleDst);
        doublePECSDemo.copy(doubleSrc, doubleDst);
        System.out.println("doubleDst after copy: " + doubleDst);
    }

    private static void numberSumDemo() {
        PECSDemo<Number> numberPECSDemo = new PECSDemo<>();
        List<Number> numberSrc = new ArrayList<>(List.of(1.0, 2, 3.0, 4F, 5.0, 6L));
        Number sum = numberPECSDemo.sumNumbers(numberSrc);
        System.out.println(sum);
    }

    private static void numberListDemo() {
        PECSDemo<Number> numberPECSDemo = new PECSDemo<>();
        List<Number> numberSrc = new ArrayList<>(List.of(1.0, 2, 3.0, 4F, 5.0, 6L));
        List<Number> numberDst = new ArrayList<>();
        System.out.println("numberDst before copy: " + numberDst);
        numberPECSDemo.copy(numberSrc, numberDst);
        System.out.println("numberDst after copy: " + numberDst);
    }
}
