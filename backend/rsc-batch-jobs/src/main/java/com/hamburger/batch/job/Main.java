package com.hamburger.batch.job;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello and welcome!");
        if (args.length > 0) {
            for (String arg : args) {
                System.out.println(arg);

            }
        }
        System.exit(0);
    }
}