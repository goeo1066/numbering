package com.github.goeo1066.avidoc;

import java.util.ArrayList;
import java.util.List;

/**
 * FlowControlIndex is a utility class for generating fixed-length codes or identifiers
 * using a specified radix and character set.
 * It supports overflow handling beyond the numeric limit by mapping into
 * custom character arrays, enabling alphanumeric code sequences such as
 * 999 → A00 → Z99 → ZA0.
 */
public class FlowControlIndexFormatter {
    /**
     * Default character set for alphabetic overflow (A-Z).
     */
    private static final String BASE10_CHAR_ARRAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Default radix for numeric indexing.
     */
    private static final int BASE10_RADIX = 10;

    /**
     * Character array used for overflow representation.
     */
    private final char[] charArray;

    /**
     * Base used for numeric calculation (e.g., 10 for decimal).
     */
    private final int radix;

    /**
     * Cached last character in the character array, used for repeated prefixing.
     */
    private final String lastChar;

    /**
     * Cached last character in the character array, used for repeated prefixing.
     */
    private FlowControlIndexFormatter(char[] charArray, int radix) {
        this.charArray = charArray;
        this.radix = radix;
        // Set the last character based on the provided character array
        this.lastChar = String.valueOf(charArray[charArray.length - 1]);
    }

    /**
     * Creates a FlowControlIndex instance with a character set derived from the radix.
     * Optionally removes ambiguous characters ('O', 'I') for human-readability.
     *
     * @param radix         the numeric base
     * @param humanReadable if true, excludes visually similar characters
     * @return FlowControlIndex configured with inferred character set
     */
    public static FlowControlIndexFormatter ofBase(int radix, boolean humanReadable) {
        String charArray = humanReadable
                ? BASE10_CHAR_ARRAY.replace("O", "").replace("I", "")
                : BASE10_CHAR_ARRAY;

        if (radix > BASE10_RADIX && radix < charArray.length()) {
            return new FlowControlIndexFormatter(charArray.substring(radix - BASE10_RADIX).toCharArray(), radix);
        }

        if (radix > 0 && radix <= BASE10_RADIX) {
            return new FlowControlIndexFormatter(charArray.toCharArray(), radix);
        }

        throw new UnsupportedOperationException("Unsupported radix or character set size.");
    }

    /**
     * Creates a FlowControlIndex instance using a custom character array.
     *
     * @param radix     radix to use
     * @param charArray custom overflow character set
     * @return FlowControlIndex instance
     */
    public static FlowControlIndexFormatter ofCustom(int radix, char[] charArray) {
        return new FlowControlIndexFormatter(charArray, radix);
    }

    /**
     * Calculates the maximum numeric value representable with a given string length.
     * Includes extended capacity from overflow encoding via character set.
     *
     * @param length desired code length
     * @return maximum value representable
     */
    public long maxNumberByLength(int length) {
        if (length < 1) {
            return 0;
        }

        long baseRange = (long) Math.pow(radix, length); // 정수 자릿수 영역 (예: 999까지)
        long maxValue = baseRange - 1; // 기본 최대값 (예: 999)

        long segmentSize = baseRange; // 초기 세그먼트 크기

        while ((segmentSize /= radix) > 0) {
            maxValue += charArray.length * segmentSize;
        }

        return maxValue;
    }

    /**
     * Formats a number into a fixed-length code with overflow logic.
     * Uses the provided radix for standard values, and switches to the character set
     * when the numeric limit is exceeded.
     *
     * @param value  value to encode
     * @param length desired length of output string
     * @return formatted alphanumeric code
     * @throws IndexOutOfBoundsException if value exceeds representable range
     */
    public String formatIndex(long value, int length) {
        if (length < 1) throw new IndexOutOfBoundsException("Length must be at least 1.");

        long numericLimit = (long) Math.pow(radix, length);
        if (numericLimit > value) {
            return toRadixString(value, length);
        }

        long extendedLimit = numericLimit - 1;
        int overflowTier = 0;

        while ((numericLimit /= radix) > 0) {
            extendedLimit += charArray.length * numericLimit;
            if (extendedLimit >= value) {
                long previousTierStart = extendedLimit - (charArray.length * numericLimit);
                return assembleCode(value, length, overflowTier, previousTierStart, numericLimit);
            }
            overflowTier++;
        }

        throw new IndexOutOfBoundsException("Value too large for given length.");
    }

    /**
     * Builds the final encoded string using overflow logic.
     * Prefixes the code with repeated overflow characters if needed.
     *
     * @param value             original numeric value
     * @param length            target string length
     * @param overflowTier      number of overflow prefix characters
     * @param previousTierStart lower bound of previous overflow segment
     * @param numericLimit      segment width for current overflow range
     * @return formatted code string
     */
    private String assembleCode(long value, int length, int overflowTier, long previousTierStart, long numericLimit) {
        StringBuilder builder = new StringBuilder(length);
        builder.append(lastChar.repeat(overflowTier));

        char c = charArray[(int) ((value - (previousTierStart + 1)) / numericLimit)];
        builder.append(c);

        if (numericLimit > 1) {
            int pad = (int) (Math.log(numericLimit) / Math.log(radix));
            builder.append(toRadixString(value % numericLimit, pad));
        }

        return builder.toString();
    }

    /**
     * Converts a number to a radix string, left-padded with zeros if necessary.
     *
     * @param value  value to convert
     * @param length minimum string width
     * @return zero-padded string in specified radix
     */
    public String toRadixString(long value, int length) {
        String s = Long.toString(value, radix);
        if (s.length() >= length) {
            return s;
        }

        String pad = "0".repeat(length - s.length());
        return pad + s;
    }

    public static void main(String[] args) {
        FlowControlIndexFormatter fci = FlowControlIndexFormatter.ofBase(10, true);
        int len = 7;
        long max = fci.maxNumberByLength(len);
        List<String> codes = new ArrayList<>((int) max + 1);
        long startedAt = System.currentTimeMillis();
        for (int i = 1; i <= max; i++) {
            String code = fci.formatIndex(i, len);
//            System.out.println(code);
            codes.add(code);
        }
        long elapsed = System.currentTimeMillis() - startedAt;
        System.out.println("Generated " + codes.size() + " codes.");
        System.out.println("Created " + max + ", Elapsed time: " + elapsed + " ms / " + (elapsed / 1000.0) + " seconds");
    }

    /* *******************************
     * Test cases are in FlowControlIndexFormatterTest.java
     */

    /**
     * Tests for private accessible.
     * This method runs inline tests to validate the functionality of the formatter.
     * It includes basic padding, overflow handling, single digit behavior,
     */
    public static void runTests() {
        FlowControlIndexFormatter fci = FlowControlIndexFormatter.ofBase(10, true);

        // Basic padding
        assertEqual(fci.formatIndex(1, 3), "001", "Leading zero padding");
        assertEqual(fci.formatIndex(999, 3), "999", "Max numeric boundary");
        assertEqual(fci.formatIndex(1000, 3), "A00", "Overflow to alpha");

        // Overflow segment handling
        assertEqual(fci.formatIndex(2599, 3), "R99", "Last code in 1st alpha segment");
        assertEqual(fci.formatIndex(2600, 3), "S00", "Start of 2nd overflow tier");

        // Single digit behavior
        assertEqual(fci.formatIndex(9, 1), "9", "Single digit");
        assertEqual(fci.formatIndex(10, 1), "A", "1-digit overflow");

        // Edge cases
        try {
            fci.formatIndex(1, 0);
            throw new AssertionError("Expected exception not thrown for len=0");
        } catch (Exception e) {
            System.out.println("✅ Passed: Exception for len = 0");
        }

        // Test maximum value by length
        long max = fci.maxNumberByLength(3);
        String lastCode = fci.formatIndex(max, 3);
        System.out.println("✅ Last code (max): " + lastCode);

        System.out.println("✅ All inline tests passed.");
    }

    private static void assertEqual(String actual, String expected, String label) {
        if (!actual.equals(expected)) {
            throw new AssertionError(
                    "❌ Test failed [" + label + "] — Expected: " + expected + ", Got: " + actual
            );
        }
        System.out.println("✅ " + label + " → " + actual);
    }
}
