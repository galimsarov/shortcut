package com.example.c_advanced_core.e_generics.d_covariance;

import java.util.List;


public class CovarianceDemo {
    /**
     * Ковариантность в Java достигается через wildcard ? extends T.
     */
    public static double sumNumbers(List<? extends Number> numbers) {
        double sum = 0;
        for (Number n : numbers) {
            sum += n.doubleValue();
        }
        return sum;
    }

    /**
     * Теперь можно передавать List<Integer>, List<Double>, List<Long>.
     * @param args
     */
    public static void main(String[] args) {
        double a = sumNumbers(List.of(1, 2, 3));
        System.out.println("a: " + a);
        double b = sumNumbers(List.of(1.5, 2.5));
        System.out.println("b: " + b);
    }
}
