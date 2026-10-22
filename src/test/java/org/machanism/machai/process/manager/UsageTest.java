package org.machanism.machai.process.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UsageTest {

    @Test
    void exposesAllTokenCountsWithoutMutation() {
        // Arrange
        Usage usage = new Usage(120L, 30L, 45L);

        // Act
        long input = usage.getInputTokens();
        long cached = usage.getInputCachedTokens();
        long output = usage.getOutputTokens();

        // Assert
        assertEquals(120L, input);
        assertEquals(30L, cached);
        assertEquals(45L, output);
    }

    @Test
    void preservesZeroAndNegativeValuesAsReported() {
        // Arrange and Act
        Usage usage = new Usage(0L, -1L, Long.MIN_VALUE);

        // Assert
        assertEquals(0L, usage.getInputTokens());
        assertEquals(-1L, usage.getInputCachedTokens());
        assertEquals(Long.MIN_VALUE, usage.getOutputTokens());
    }
}
