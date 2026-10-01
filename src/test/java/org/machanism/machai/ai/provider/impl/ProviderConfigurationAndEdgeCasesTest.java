package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.machanism.machai.TestConfigurators;
import org.machanism.machai.genai.provider.AnthropicProvider;
import org.machanism.machai.genai.provider.CodeMieProvider;
import org.machanism.machai.genai.provider.OpenAIProvider;
import org.machanism.machai.process.provider.AbstractAIProvider;
import org.machanism.machai.process.tools.ParamDescriptor;

import com.openai.models.responses.Tool;

/** Additional isolated tests for provider configuration and local state. */
class ProviderConfigurationAndEdgeCasesTest {

    private static final class ExposedOpenAIProvider extends OpenAIProvider {
        int inputCount() { return getInputs().size(); }
        boolean hasInputs() { return !getInputs().isEmpty(); }
        int toolCount() { return getToolMap().size(); }
        java.util.Set<Tool> tools() { return getToolMap().keySet(); }
        private java.util.List<?> getInputs() {
            return (java.util.List<?>) field("inputs");
        }
        private java.util.Map<Tool, ?> getToolMap() {
            return (java.util.Map<Tool, ?>) field("toolMap");
        }
        private Object field(String name) {
            try {
                Field f = OpenAIProvider.class.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(this);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }
        void registerTool(String name, ParamDescriptor... descriptors) {
            addTool(name, "description", (params, context) -> "ok", descriptors);
        }
        void registerWebSearch(String type, String city, String country, String region) {
            addWebSearch(type, city, country, region);
        }
        void registerMcp(String name, String url, String authorization, String description) {
            addMcpServer(name, url, authorization, description);
        }
    }

    private static final class ExposedAnthropicProvider extends AnthropicProvider {
        void registerWebSearch(String type, String city, String country, String region) {
            addWebSearch(type, city, country, region);
        }
        void registerMcp(String name, String url, String authorization, String description) {
            addMcpServer(name, url, authorization, description);
        }
        void registerTool(String name, ParamDescriptor... descriptors) {
            addTool(name, "description", (params, context) -> "ok", descriptors);
        }
    }

    @Test
    void openAiPromptAndClearMaintainConversationState() {
        // Arrange
        ExposedOpenAIProvider provider = new ExposedOpenAIProvider();

        // Act
        provider.prompt("hello");

        // Assert
        assertEquals(1, provider.inputCount());
        provider.clear();
        assertTrue(!provider.hasInputs());
    }

    @Test
    void openAiRegistersWebSearchMcpAndFunctionTools() {
        // Arrange
        ExposedOpenAIProvider provider = new ExposedOpenAIProvider();
        ParamDescriptor required = new ParamDescriptor("query", "string", true, "search text", null);
        ParamDescriptor optional = new ParamDescriptor("limit", "integer", false, "maximum results", 10);

        // Act
        provider.registerWebSearch(OpenAIProvider.DEFAULT_WEBSEARCH_TYPE_NAME, "Paris", "FR", "IDF");
        provider.registerMcp("docs", "https://example.test/mcp", "secret", "documentation");
        provider.registerTool("search", required, optional);

        // Assert
        assertEquals(3, provider.toolCount());
        assertTrue(provider.tools().stream().anyMatch(Tool::isFunction));
        assertTrue(provider.tools().stream().anyMatch(Tool::isWebSearch));
        assertTrue(provider.tools().stream().anyMatch(Tool::isMcp));
    }

    @Test
    void anthropicRejectsUnknownWebSearchVersionAndAcceptsSupportedVersions() {
        // Arrange
        ExposedAnthropicProvider provider = new ExposedAnthropicProvider();

        // Act and assert
        assertThrows(IllegalArgumentException.class,
                () -> provider.registerWebSearch("unsupported", null, null, null));
        provider.registerWebSearch(AnthropicProvider.DEFAULT_WEBSEARCH_TYPE_NAME, "Paris", null, "IDF");
        provider.registerWebSearch("20250305", null, "FR", null);
        provider.registerMcp("docs", "https://example.test/mcp", null, null);
        provider.registerTool("lookup", new ParamDescriptor("q", "string", true, "query", null));
		assertEquals(java.util.Collections.singletonList("lookup"), provider.getToolNames());
    }

    @Test
    void codeMieRejectsUnsupportedModelBeforeDelegation() {
        // Arrange
        CodeMieProvider provider = new CodeMieProvider();
        TestConfigurators.MapBackedConfigurator config = TestConfigurators.mapBacked();
        config.put(AbstractAIProvider.USERNAME_PROP_NAME, "client");
        config.put(AbstractAIProvider.PASSWORD_PROP_NAME, "secret");

        // Act and assert
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> provider.init("unsupported-model", config));
        assertNotNull(error.getMessage());
    }

    @Test
    void codeMieEmbeddingWithoutInitializedProviderFailsClearly() {
        // Arrange
        CodeMieProvider provider = new CodeMieProvider();

        // Act
        assertThrows(NullPointerException.class, () -> provider.embedding(null, 3));
    }

    @Test
    void providerConfigurationPropertiesCanBeChangedAndRead() {
        // Arrange
        ExposedOpenAIProvider provider = new ExposedOpenAIProvider();
        provider.init("gpt-test", TestConfigurators.mapBacked());

        // Act
        provider.instructions("system");
        provider.setTimeout(12);

        // Assert
        assertEquals(12, provider.getTimeout());
        assertEquals("system", getField(provider, "instructions"));
    }

    private static Object getField(Object target, String name) {
        try {
            Field field = target.getClass().getSuperclass().getSuperclass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
