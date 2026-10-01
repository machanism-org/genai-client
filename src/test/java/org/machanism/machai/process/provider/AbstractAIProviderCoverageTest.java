package org.machanism.machai.process.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.process.tools.ToolFunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Focused tests for provider state, configuration hooks, and utility branches. */
class AbstractAIProviderCoverageTest {
    @Test
    void initLoadsStateAndRegistersConfiguredSearchAndMcpServices() {
        // Arrange
        HookProvider provider = new HookProvider();
        Configurator config = mock(Configurator.class);
        when(config.getLong("MAX_OUTPUT_TOKENS", AbstractAIProvider.MAX_OUTPUT_TOKENS)).thenReturn(99L);
        when(config.getLong("MAX_TOOL_CALLS", 0L)).thenReturn(3L);
        when(config.get("WebSearchTool.type", null)).thenReturn("search");
        when(config.get("WebSearchTool.city", null)).thenReturn("Paris");
        when(config.get("MCP.url", null)).thenReturn("https://mcp");
        when(config.get("MCP.name", null)).thenReturn("primary");
        when(config.get("MCP.authorization", null)).thenReturn("token");
        when(config.get("MCP.description", null)).thenReturn("desc");

        // Act
        provider.init("gpt-test", config);

        // Assert
        assertEquals("gpt-test", provider.getModel());
        assertSame(config, provider.getConfigurator());
        assertEquals(99L, provider.getMaxOutputTokens());
        assertEquals(3L, provider.getMaxToolCalls());
        assertEquals("search:Paris", provider.searches.get(0));
        assertEquals("primary:https://mcp:token:desc", provider.mcps.get(0));
    }

    @Test
    void stateAccessorsAndNoOpLifecycleMethodsRemainSafe() {
        // Arrange
        HookProvider provider = new HookProvider();
        File directory = new File("workspace");

        // Act
        provider.setModel("changed");
        provider.setMaxOutputTokens(7L);
        provider.setMaxToolCalls(8L);
        provider.setInstructions("instructions");
        provider.setTimeout(12L);
        provider.setProjectDir(directory);
        provider.instructions("new instructions");
        provider.prompt("prompt");
        provider.clear();

        // Assert
        assertEquals("changed", provider.getModel());
        assertEquals(7L, provider.getMaxOutputTokens());
        assertEquals(8L, provider.getMaxToolCalls());
        assertEquals("new instructions", provider.getInstructions());
        assertEquals(12L, provider.getTimeout());
        assertSame(directory, provider.getProjectDir());
        assertTrue(provider.isErrorHandling());
        provider.setErrorHandling(false);
        assertFalse(provider.isErrorHandling());
    }

    @Test
    void utilityMethodsHandleNullValuesAndCommonNamingStyles() {
        // Arrange
        HookProvider provider = new HookProvider();

        // Act / Assert
        assertEquals("", provider.normalizeValue(null));
        assertEquals("mixed value", provider.normalizeValue("MiXeD Value"));
        assertEquals("", AbstractAIProvider.toSnakeCase(null));
        assertEquals("", AbstractAIProvider.toSnakeCase(""));
        assertEquals("already_snake", AbstractAIProvider.toSnakeCase(" Already-Snake "));
        assertEquals("get_http_response", AbstractAIProvider.toSnakeCase("getHTTPResponse"));
        assertEquals("one_two", AbstractAIProvider.toSnakeCase("one__two"));
    }

    @Test
    void safelyInvokedSpecialExceptionRootCauseIsNeverConvertedToText() {
        // Arrange
        HookProvider provider = new HookProvider();
        provider.init("model", emptyConfiguration());
        ToolFunction function = (props, context) -> {
            throw new RuntimeException(new org.machanism.machai.process.tools.SpecialException("stop"));
        };

        // Act / Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                org.machanism.machai.process.tools.SpecialException.class,
                () -> provider.invoke(function));
    }

    private static Configurator emptyConfiguration() {
        Configurator config = mock(Configurator.class);
        when(config.getLong("MAX_OUTPUT_TOKENS", AbstractAIProvider.MAX_OUTPUT_TOKENS))
                .thenReturn(AbstractAIProvider.MAX_OUTPUT_TOKENS);
        when(config.getLong("MAX_TOOL_CALLS", 0L)).thenReturn(0L);
        return config;
    }

    static final class HookProvider extends AbstractAIProvider {
        final List<String> searches = new ArrayList<>();
        final List<String> mcps = new ArrayList<>();

        @Override
        protected void addTool(String name, String description, ToolFunction function,
                org.machanism.machai.process.tools.ParamDescriptor... params) { }

        @Override
        protected void addWebSearch(String type, String city, String country, String region) {
            searches.add(type + ":" + city);
        }

        @Override
        protected void addMcpServer(String label, String url, String authorization, String description) {
            mcps.add(label + ":" + url + ":" + authorization + ":" + description);
        }

        String normalizeValue(String value) { return normalize(value); }

        Object invoke(ToolFunction function) {
            return safelyInvokeTool("test", function, new ObjectMapper().createObjectNode(), null);
        }

        @Override public String perform() { return null; }
        @Override public List<String> getToolNames() { return Collections.emptyList(); }
    }
}
