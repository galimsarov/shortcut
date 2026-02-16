package com.example.solid.isp;

/**
 * ISP: интерфейсы должны быть узкими и предметными.
 */
public class IspExample {

    // ❌ Нарушение ISP: принтер вынужден реализовывать scan/fax, которые ему не нужны.
    interface FatOfficeMachine {
        void print(String text);

        void scan(String text);

        void fax(String text);
    }

    static class OldPrinter implements FatOfficeMachine {
        @Override
        public void print(String text) {
            System.out.println("Print: " + text);
        }

        @Override
        public void scan(String text) {
            throw new UnsupportedOperationException("Scanner is not supported");
        }

        @Override
        public void fax(String text) {
            throw new UnsupportedOperationException("Fax is not supported");
        }
    }

    interface Printer {
        void print(String text);
    }

    interface Scanner {
        void scan(String text);
    }

    interface Fax {
        void fax(String text);
    }

    // ✅ ISP: класс реализует только нужные ему интерфейсы.
    static class ModernPrinter implements Printer {
        @Override
        public void print(String text) {
            System.out.println("Print: " + text);
        }
    }

    static class MultiFunctionDevice implements Printer, Scanner, Fax {
        @Override
        public void print(String text) {
            System.out.println("Print: " + text);
        }

        @Override
        public void scan(String text) {
            System.out.println("Scan: " + text);
        }

        @Override
        public void fax(String text) {
            System.out.println("Fax: " + text);
        }
    }

    public static void demo() {
        Printer printer = new ModernPrinter();
        printer.print("Hello ISP");

        MultiFunctionDevice mfd = new MultiFunctionDevice();
        mfd.print("Doc");
        mfd.scan("Doc");
        mfd.fax("Doc");
    }
}
