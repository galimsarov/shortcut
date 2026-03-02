package com.example.c_advanced_core.g_input_output;

import java.io.*;
import java.nio.file.*;

/**
 * Небуферизированный поток чаще ходит в ОС/диск небольшими порциями.
 * Буферизированный (BufferedInputStream, BufferedOutputStream, BufferedReader, BufferedWriter)
 * уменьшает число системных операций и обычно быстрее.
 * Важный момент: даже без Buffered* можно частично компенсировать это,
 * если читать/писать массивами (byte[]), а не по 1 байту.
 */
public class CBufferedVsUnbufferedDemo {
    public static void main(String[] args) throws Exception {
        Path src = Paths.get("tmp/io-demo/big.bin");
        Path dst1 = Paths.get("tmp/io-demo/copy-unbuffered.bin");
        Path dst2 = Paths.get("tmp/io-demo/copy-buffered.bin");

        Files.createDirectories(src.getParent());
        if (Files.notExists(src)) {
            // Генерируем ~10 MB
            byte[] data = new byte[10 * 1024 * 1024];
            for (int i = 0; i < data.length; i++) data[i] = (byte) (i % 256);
            Files.write(src, data);
        }

        long t1 = System.nanoTime();
        copyUnbuffered(src, dst1);
        long t2 = System.nanoTime();

        copyBuffered(src, dst2);
        long t3 = System.nanoTime();

        System.out.printf("Unbuffered: %.2f ms%n", (t2 - t1) / 1_000_000.0);
        System.out.printf("Buffered:   %.2f ms%n", (t3 - t2) / 1_000_000.0);
    }

    static void copyUnbuffered(Path src, Path dst) throws IOException {
        try (InputStream in = new FileInputStream(src.toFile());
             OutputStream out = new FileOutputStream(dst.toFile())) {
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
        }
    }

    static void copyBuffered(Path src, Path dst) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(src.toFile()));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(dst.toFile()))) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}
