package org.machanism.machai.process.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.process.provider.EmbeddingProvider;
import org.machanism.machai.process.provider.ProcessProvider;

class ProcessProviderManagerTest {

    @Test
    void returnsNullWhenProcessProviderNameIsBlank() {
        assertNull(ProcessProviderManager.getProvider(":model", null));
        assertNull(ProcessProviderManager.getProvider(null, null));
    }

    @Test
    void propagatesRuntimeFailureThrownDuringEmbeddingInitialization() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ProcessProviderManager.getEmbeddingProvider(
                        ThrowingEmbeddingProvider.class.getName() + ":model", null));

        assertEquals("initialization failed", exception.getMessage());
    }

    @Test
    void reportsEmbeddingProviderWithoutPublicNoArgumentConstructor() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ProcessProviderManager.getEmbeddingProvider(
                        PrivateConstructorEmbeddingProvider.class.getName() + ":model", null));

        assertEquals("Failed to initialize EmbeddingProvider provider '"
                + PrivateConstructorEmbeddingProvider.class.getName()
                + "': provider is not supported or an error occurred during initialization.",
                exception.getMessage());
    }

    @Test
    void rejectsInvalidProcessProviderIdentifier() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ProcessProviderManager.getProvider("bad-name:model", null));

        assertEquals("Invalid provider name: `bad-name`. Expected format is `Provider:Model` (e.g., `OpenAI:gpt-4`). Please specify both provider and model separated by a colon.",
                exception.getMessage());
    }

    @Test
    void resolvesConventionalProcessProviderAndPassesModelAndConfiguration() {
        Configurator configuration = null;

        ProcessProvider provider = ProcessProviderManager.getProvider("None:log", configuration);

        assertEquals("org.machanism.machai.process.provider.NoneProvider", provider.getClass().getName());
        assertNull(provider.perform());
    }

    @Test
    void reportsUnsupportedProcessProvider() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ProcessProviderManager.getProvider("DoesNotExist:model", null));

        assertEquals("Failed to initialize GenAI provider 'DoesNotExist': provider is not supported or an error occurred during initialization.",
                exception.getMessage());
    }

    @Test
    void returnsNullWhenEmbeddingProviderNameIsBlank() {
        assertNull(ProcessProviderManager.getEmbeddingProvider(":embedding-model", null));
        assertNull(ProcessProviderManager.getEmbeddingProvider(null, null));
    }

    @Test
    void resolvesFullyQualifiedEmbeddingProviderAndInitializesIt() {
        Configurator configuration = null;
        String providerName = TestEmbeddingProvider.class.getName();

        EmbeddingProvider provider = ProcessProviderManager.getEmbeddingProvider(
                providerName + ":embedding-model", configuration);

        assertEquals(TestEmbeddingProvider.class, provider.getClass());
        assertEquals("embedding-model", TestEmbeddingProvider.model);
        assertSame(configuration, TestEmbeddingProvider.configuration);
        assertEquals(Collections.singletonList(0.5d), provider.embedding("text", 1));
    }

    @Test
    void rejectsClassThatDoesNotImplementEmbeddingProvider() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ProcessProviderManager.getEmbeddingProvider("java.lang.String:model", null));

        assertEquals("Class `java.lang.String` does not implement EmbeddingProvider. Please ensure the class is a valid provider implementation.",
                exception.getMessage());
    }

    @Test
    void reportsMissingEmbeddingProvider() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ProcessProviderManager.getEmbeddingProvider("missing.Provider:model", null));

        assertEquals("Failed to initialize EmbeddingProvider provider 'missing.Provider': provider is not supported or an error occurred during initialization.",
                exception.getMessage());
    }

    public static class TestEmbeddingProvider implements EmbeddingProvider {
        static String model;
        static Configurator configuration;

        @Override
        public void init(String model, Configurator conf) {
            TestEmbeddingProvider.model = model;
            TestEmbeddingProvider.configuration = conf;
        }

        @Override
        public List<Double> embedding(String text, long dimensions) {
            return Collections.singletonList(0.5d);
        }
    }

    public static class ThrowingEmbeddingProvider implements EmbeddingProvider {
        @Override
        public void init(String model, Configurator conf) {
            throw new IllegalStateException("initialization failed");
        }

        @Override
        public List<Double> embedding(String text, long dimensions) {
            return Collections.emptyList();
        }
    }

    public static class PrivateConstructorEmbeddingProvider implements EmbeddingProvider {
        private PrivateConstructorEmbeddingProvider() {
        }

        @Override
        public void init(String model, Configurator conf) {
        }

        @Override
        public List<Double> embedding(String text, long dimensions) {
            return Collections.emptyList();
        }
    }

}
