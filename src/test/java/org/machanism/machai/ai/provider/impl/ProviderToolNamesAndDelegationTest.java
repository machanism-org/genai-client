package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.machanism.machai.genai.provider.AnthropicProvider;
import org.machanism.machai.genai.provider.OpenAIProvider;
import org.machanism.machai.process.tools.ParamDescriptor;

/** Covers tool registration identity and CodeMie delegation edge cases. */
class ProviderToolNamesAndDelegationTest {

    private static final ParamDescriptor REQUIRED_QUERY =
            new ParamDescriptor("query", "string", true, "query text", null);

    private static final class ExposedAnthropicProvider extends AnthropicProvider {
        void register(String name) {
            addTool(name, "description", (parameters, projectDir) -> "result", REQUIRED_QUERY);
        }
    }

    private static final class ExposedOpenAIProvider extends OpenAIProvider {
        void register(String name) {
            addTool(name, "description", (parameters, projectDir) -> "result", REQUIRED_QUERY);
        }
    }

    @Test
    void openAiReturnsRegisteredFunctionNamesInRegistrationOrderAndDoesNotDuplicateTools() {
        // Arrange
        ExposedOpenAIProvider provider = new ExposedOpenAIProvider();

        // Act
        provider.register("first");
        provider.register("second");
        provider.register("first");

        // Assert
        assertEquals(Arrays.asList("first", "second"), provider.getToolNames());
    }

    @Test
    void anthropicReturnsRegisteredFunctionNamesInRegistrationOrderAndDoesNotDuplicateTools() {
        // Arrange
        ExposedAnthropicProvider provider = new ExposedAnthropicProvider();

        // Act
        provider.register("lookup");
        provider.register("summarize");
        provider.register("lookup");

        // Assert
        assertEquals(Arrays.asList("lookup", "summarize"), provider.getToolNames());
    }

}
