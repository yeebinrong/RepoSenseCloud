package com.hamburger.batch.job;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testRunWithArgs() {
        // Capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String[] args = {"arg1", "arg2"};
        Main.run(args);

        String output = outContent.toString();
        assertTrue(output.contains("Hello and welcome!"));
        assertTrue(output.contains("arg1"));
        assertTrue(output.contains("arg2"));
    }

    @Test
    void testRunWithoutArgs() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        String[] args = {};
        Main.run(args);

        String output = outContent.toString();
        assertTrue(output.contains("Hello and welcome!"));
        assertFalse(output.contains("arg1"));
    }
}
