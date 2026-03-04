package com.example.c_advanced_core.g_input_output.a_examples;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;

import static java.nio.file.StandardOpenOption.*;

public class DNioChannelCopyDemo {
    public static void main(String[] args) throws IOException {
        Path src = Paths.get("tmp/io-demo/notes.txt");
        Path dst = Paths.get("tmp/io-demo/notes-channel-copy.txt");

        Files.createDirectories(src.getParent());
        if (Files.notExists(src)) {
            Files.writeString(src, "NIO channel demo\n");
        }

        try (FileChannel in = FileChannel.open(src, READ);
             FileChannel out = FileChannel.open(dst, CREATE, WRITE, TRUNCATE_EXISTING)) {

            long size = in.size();
            long transferred = 0;
            while (transferred < size) {
                transferred += in.transferTo(transferred, size - transferred, out);
            }
        }

        System.out.println("Copied with FileChannel: " + dst);
    }
}