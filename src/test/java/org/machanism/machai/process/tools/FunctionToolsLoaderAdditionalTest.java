package org.machanism.machai.process.tools;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.machanism.machai.process.provider.ProcessProvider;

/** Additional compatibility and filtering coverage for the tool loader. */
class FunctionToolsLoaderAdditionalTest {

    @Test
    void applyTools_exclusionTakesPrecedenceOverSupportedSuperclass() throws Exception {
        // Arrange
        FunctionToolsLoader loader = new FunctionToolsLoader();
        List<FunctionTools> discovered = discoveredTools(loader);
        discovered.clear();
        ExcludesChild tools = new ExcludesChild();
        discovered.add(tools);
        ProcessProvider provider = mock(ProcessProvider.class);

        // Act
        loader.applyTools(provider, new String[] { "ignored" }, ChildApplication.class);

        // Assert
        verify(provider, never()).addTools(tools, new String[] { "ignored" });
        verify(provider, never()).addPrompts(tools);
        verify(provider, never()).addResources(tools);
    }

    @Test
    void applyTools_emptySupportedListMeansAnyApplicationUnlessExcluded() throws Exception {
        // Arrange
        FunctionToolsLoader loader = new FunctionToolsLoader();
        List<FunctionTools> discovered = discoveredTools(loader);
        discovered.clear();
        AnyApplication tools = new AnyApplication();
        discovered.add(tools);
        ProcessProvider provider = mock(ProcessProvider.class);
        String[] requested = { "tool" };

        // Act
        loader.applyTools(provider, requested, UnrelatedApplication.class);

        // Assert
        verify(provider).addTools(tools, requested);
        verify(provider).addPrompts(tools);
        verify(provider).addResources(tools);
    }

    @Test
    void constructor_handlesMissingLegacyServiceDescriptorWithoutFailing() {
        // Arrange and Act
        FunctionToolsLoader loader = new FunctionToolsLoader();

        // Assert
        assertDoesNotThrow(() -> loader.applyTools(mock(ProcessProvider.class), null, Object.class));
    }

    @Test
    void constructor_ignoresMalformedLegacyServiceDescriptor() throws Exception {
        // Arrange
        Path servicesDirectory = Files.createTempDirectory("function-tools-services");
        Path descriptor = servicesDirectory.resolve(
                "META-INF/services/org.machanism.machai.ai.tools.FunctionTools");
        Files.createDirectories(descriptor.getParent());
        Files.write(descriptor, "not.a.RealFunctionTools\n".getBytes(StandardCharsets.UTF_8));
        ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();

        try (URLClassLoader contextLoader = new URLClassLoader(
                new URL[] { servicesDirectory.toUri().toURL() }, previousLoader)) {
            Thread.currentThread().setContextClassLoader(contextLoader);

            // Act
            FunctionToolsLoader loader = new FunctionToolsLoader();

            // Assert
            assertDoesNotThrow(() -> loader.applyTools(mock(ProcessProvider.class), null, Object.class));
        } finally {
            Thread.currentThread().setContextClassLoader(previousLoader);
            deleteRecursively(servicesDirectory);
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (java.nio.file.DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteRecursively(entry);
                }
            }
        }
        Files.deleteIfExists(path);
    }

    @SuppressWarnings("unchecked")
    private static List<FunctionTools> discoveredTools(FunctionToolsLoader loader) throws Exception {
        // Arrange
        Field field = FunctionToolsLoader.class.getDeclaredField("functionTools");
        field.setAccessible(true);

        // Act
        return (List<FunctionTools>) field.get(loader);
    }

    static class ParentApplication { }
    static class ChildApplication extends ParentApplication { }
    static class UnrelatedApplication { }

    @SupportedFor(value = { ParentApplication.class }, excludes = { ChildApplication.class })
    static class ExcludesChild implements FunctionTools { }

    @SupportedFor
    static class AnyApplication implements FunctionTools { }
}
