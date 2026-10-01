package org.machanism.machai.process.provider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Covers concise logging and serialization fallback paths. */
class ToolLoggerCoverageTest {
    private final Logger original = ToolLogger.logger;

    @AfterEach
    void restoreLogger() {
        ToolLogger.logger = original;
    }

    @Test
    void infoLoggingAbbreviatesMultilineAndLargePayloads() throws Exception {
        // Arrange
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(false);
        when(logger.isInfoEnabled()).thenReturn(true);
        ToolLogger.logger = logger;
        String longValue = new String(new char[220]).replace('\0', 'x') + " line";
        File directory = new File("logs");

        // Act
        ToolLogger subject = new ToolLogger(ToolLogger.Type.PROMPT, null);
        subject.logInput("prompt", new ObjectMapper().readTree("{\"value\":\"" + longValue + "\"}"), directory);
        subject.logResult("prompt", directory, longValue);
        subject.logError("prompt", directory, new IllegalArgumentException("invalid"));

        // Assert
        verify(logger).info(org.mockito.ArgumentMatchers.eq(ToolLogger.CALL_MSG),
                org.mockito.ArgumentMatchers.eq(ToolLogger.Type.PROMPT), org.mockito.ArgumentMatchers.eq("prompt"),
                org.mockito.ArgumentMatchers.argThat(value -> String.valueOf(value).length() <= AbstractAIProvider.LOG_LINE_LENG),
                org.mockito.ArgumentMatchers.eq(directory));
        verify(logger).info(org.mockito.ArgumentMatchers.eq(ToolLogger.RETURNS_MSG),
                org.mockito.ArgumentMatchers.eq(ToolLogger.Type.PROMPT), org.mockito.ArgumentMatchers.eq("prompt"),
                org.mockito.ArgumentMatchers.eq(longValue.length()),
                org.mockito.ArgumentMatchers.argThat(value -> String.valueOf(value).length() <= AbstractAIProvider.LOG_LINE_LENG),
                org.mockito.ArgumentMatchers.eq(directory));
        verify(logger).error(ToolLogger.ERROR_MSG, ToolLogger.Type.PROMPT, "prompt", "failed: invalid", directory);
    }

    @Test
    void nonStringResultIsSerializedAndUnserializableResultUsesFallback() {
        // Arrange
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        ToolLogger.logger = logger;
        ToolLogger subject = new ToolLogger(ToolLogger.Type.TOOL, null);

        // Act
        subject.logResult("object", null, new ObjectMapper().createObjectNode().put("ok", true));
        subject.logResult("bad", null, new Object() { public Object self() { return this; } });

        // Assert
        verify(logger).debug(ToolLogger.RETURNS_MSG, ToolLogger.Type.TOOL, "object", 11, "{\"ok\":true}", null);
        verify(logger).debug(org.mockito.ArgumentMatchers.eq(ToolLogger.RETURNS_MSG),
                org.mockito.ArgumentMatchers.eq(ToolLogger.Type.TOOL), org.mockito.ArgumentMatchers.eq("bad"),
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.contains("ToolLoggerCoverageTest"),
                org.mockito.ArgumentMatchers.isNull());
    }
}
