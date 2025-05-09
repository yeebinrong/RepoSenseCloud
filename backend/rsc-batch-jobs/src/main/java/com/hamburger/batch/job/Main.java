package com.hamburger.batch.job;

public class Main {
    public static void main(String[] args) {
        run(args);
        System.exit(0);
    }

    // Separated logic for testing
    public static void run(String[] args) {
        System.out.println("Hello and welcome!");
        if (args.length > 0) {
            for (String arg : args) {
                System.out.println(arg);
            }
        }
    }
}