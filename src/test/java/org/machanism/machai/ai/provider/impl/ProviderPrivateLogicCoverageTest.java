package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.machanism.machai.genai.provider.CodeMieProvider;
import org.machanism.machai.genai.provider.OpenAIProvider;

import com.openai.models.responses.ResponseReasoningItem;

/** Covers isolated response helper edge cases without network calls. */
class ProviderPrivateLogicCoverageTest {

    @Test
    void openAiConfigurationSettersAndPromptStateAreObservable() {
        ExposedOpenAIProvider provider = new ExposedOpenAIProvider();
        provider.setProjectDir(new java.io.File("project"));
        provider.setErrorHandling(true);
        provider.setMaxOutputTokens(77L);
        provider.setMaxToolCalls(4L);
        provider.instructions("system");
        provider.prompt("hello");

        assertEquals("project", provider.getProjectDir().getPath());
        assertTrue(provider.isErrorHandling());
        assertEquals(77L, provider.getMaxOutputTokens());
        assertEquals(4L, provider.getMaxToolCalls());
        assertEquals("system", provider.getInstructions());
        assertTrue(provider.hasInputs());
        provider.clear();
        assertFalse(provider.hasInputs());
    }

    @Test
    void firstNonBlankReasoningReturnsFirstUsefulFragmentOrNull() throws Exception {
        OpenAIProvider provider = new OpenAIProvider();
        ResponseReasoningItem.Content blank = org.mockito.Mockito.mock(ResponseReasoningItem.Content.class);
        ResponseReasoningItem.Content useful = org.mockito.Mockito.mock(ResponseReasoningItem.Content.class);
        org.mockito.Mockito.when(blank.text()).thenReturn("  ");
        org.mockito.Mockito.when(useful.text()).thenReturn("reasoning");
        Method method = OpenAIProvider.class.getDeclaredMethod("firstNonBlankReasoning", java.util.List.class);
        method.setAccessible(true);

        String result = (String) method.invoke(provider, Arrays.asList(blank, useful));
        String noResult = (String) method.invoke(provider, Collections.singletonList(blank));

        assertEquals("reasoning", result);
        assertNull(noResult);
    }

    @Test
    void codeMieUrlEncodingEncodesSpacesAndReservedCharacters() throws Exception {
        Method method = CodeMieProvider.class.getDeclaredMethod("urlEncode", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, "a b+&");
        assertEquals("a+b%2B%26", result);
    }

    private static final class ExposedOpenAIProvider extends OpenAIProvider {
        boolean hasInputs() {
            try {
                java.lang.reflect.Field field = OpenAIProvider.class.getDeclaredField("inputs");
                field.setAccessible(true);
                return !((java.util.List<?>) field.get(this)).isEmpty();
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }
    }
}
