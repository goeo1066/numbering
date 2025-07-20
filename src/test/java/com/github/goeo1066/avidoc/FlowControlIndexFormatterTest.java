package com.github.goeo1066.avidoc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlowControlIndexFormatterTest {

    private final FlowControlIndexFormatter fci = FlowControlIndexFormatter.ofBase(10, true);

    @Test
    void testBasicPadding() {
        assertEquals("001", fci.formatIndex(1, 3));
        assertEquals("999", fci.formatIndex(999, 3));
    }

    @Test
    void testOverflowTransition() {
        assertEquals("A00", fci.formatIndex(1000, 3));
        assertEquals("Z99", fci.formatIndex(3399, 3));
        assertEquals("ZA0", fci.formatIndex(3400, 3));
    }

    @Test
    void testSingleDigitBehavior() {
        assertEquals("9", fci.formatIndex(9, 1));
        assertEquals("A", fci.formatIndex(10, 1));
    }

    @Test
    void testInvalidLength() {
        assertEquals("1", fci.formatIndex(1, 0));
    }

    @Test
    void testMaximumValueByLength() {
        long max = fci.maxNumberByLength(3);
        String code = fci.formatIndex(max, 3);
        assertNotNull(code);
        assertTrue(code.length() <= 4); // Because of prefixing, 3-digit + 1 overflow char
    }

    @Test
    void testEdgeCaseFormatting() {
        assertEquals("ZZ9", fci.formatIndex(3639, 3)); // 마지막 예상 가능한 코드
    }

    @Test
    void testOutOfBoundsHandling() {
        long max = fci.maxNumberByLength(3);
        assertThrows(IndexOutOfBoundsException.class, () -> fci.formatIndex(max + 1, 3));
    }

    @Test
    void internalTest() {
        FlowControlIndexFormatter.runTests();
    }
}