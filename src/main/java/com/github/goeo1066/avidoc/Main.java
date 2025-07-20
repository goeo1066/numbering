package com.github.goeo1066.avidoc;

import java.util.Scanner;

public class Main {
    private static final char[] char_map = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

    private static final char[] char_map_2 = new char[]{
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

    public static void main(String[] args) {
        try (Scanner s = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = s.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                String[] inputs = input.split(",");
                int num = 0;
                int max = 0;
                if (inputs.length == 2) {
                    try {
                        num = Integer.parseInt(inputs[0].trim());
                        max = Integer.parseInt(inputs[1].trim());
                        max = ((int) Math.pow(10, max)) - 1;
                    } catch (NumberFormatException ignored) {
                        System.out.println("Format Error");
                        continue;
                    }
                } else {
                    continue;
                }
                print2(num, max);
            }
        }
    }

    public static void print2(int num, int max) {
        System.out.printf("num = %d, max = %d\n", num, max);
        int digits = ((int) Math.log10(max)) + 1;
        int i;
        int minBound = 0;
        int lastMinBound = 1;
        int lastTens = (int) Math.pow(10, digits - 1);
        for (i = 0; i < digits; i++) {
            minBound = ((int) Math.pow(10, digits - i)) * ((int) Math.pow(char_map.length, i));
            int tens = (int) Math.pow(10, digits - i - 1);
            System.out.println(minBound + " " + tens);

            if (minBound > num) {
                int upperNum = num / lastTens;
                while (true) {
                    System.out.print(char_map[upperNum % char_map.length]);
                    upperNum = upperNum / char_map.length;
                    if (upperNum <= 0) {
                        break;
                    }
                }
                System.out.printf("%0" + (digits - 1) + "d\n", num % lastTens);
                break;
            }
            lastTens = tens;
            lastMinBound = minBound;
        }

        System.out.printf("Num of top A = %d\n", i);
        System.out.printf("Input num    = %d\n", num);
    }

    public static void print(int num) {
        int max = 999;
        int digits = ((int) Math.log10(max)) + 1;
        int i;
        int minBound = 0;
        int lastMinBound = 0;
        for (i = 0; i < digits; i++) {
            minBound = ((int) Math.pow(10, digits - i)) * ((int) Math.pow(char_map.length, i));
//            System.out.printf("Min Bound (%d) = %d\n", i , minBound);
            System.out.printf("10^%d * %d^%d  = %d (Min Bound)\n", digits - i, char_map.length, i, minBound);
            if (minBound > num) {
                if (lastMinBound == 0) {
                    System.out.println(num);
                    break;
                }
                System.out.printf("%d %% %d = %d\n", num, lastMinBound, num % lastMinBound);
                int c = i - 1;
                int z = num;
                for (; c >= 0; c--) {
                    int idx = z / (((int) Math.pow(10, digits - c)) * ((int) Math.pow(char_map.length, c)));
                    if (idx <= 0) {
                        break;
                    }
                    System.out.print(char_map_2[idx - 1]);
                    z = z % (((int) Math.pow(10, digits - c)) * ((int) Math.pow(char_map.length, c)));
                }
                System.out.printf("%0" + (digits - i) + "d", z);
                System.out.println();
                break;
            }
            lastMinBound = minBound;
        }

        System.out.printf("Num of top A = %d\n", i);
        System.out.printf("Input num    = %d\n", num);
    }
}