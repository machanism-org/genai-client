package org.machanism.machai.process.tools;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Tests loading the backwards-compatible service descriptor format. */
class FunctionToolsLoaderLegacyDescriptorTest {

    @Test
    void constructor_loadsValidLegacyEntriesAndIgnoresCommentsAndBlankLines() throws Exception {
        // Arrange
        Path servicesRoot = Files.createTempDirectory("legacy-function-tools");
        Path descriptor = servicesRoot.resolve(
                "META-INF/services/org.machanism.machai.ai.tools.FunctionTools");
        Files.createDirectories(descriptor.getParent());
        String implementationName = FunctionToolsLoaderTest.DiscoveredFunctionTools.class.getName();
        String content = "# a comment\n\n  " + implementationName + "  \n";
        Files.write(descriptor, content.getBytes(StandardCharsets.UTF_8));
        ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();

        try (URLClassLoader contextLoader = new URLClassLoader(
                new URL[] { servicesRoot.toUri().toURL() }, previousLoader)) {
            Thread.currentThread().setContextClassLoader(contextLoader);

            // Act
            FunctionToolsLoader loader = new FunctionToolsLoader();

            // Assert
            List<FunctionTools> discovered = discoveredTools(loader);
            assertTrue(discovered.stream()
                    .anyMatch(FunctionToolsLoaderTest.DiscoveredFunctionTools.class::isInstance));
        } finally {
            Thread.currentThread().setContextClassLoader(previousLoader);
            deleteRecursively(servicesRoot);
        }
    }

    @Test
    void constructor_ignoresLegacyEntriesThatAreNotFunctionTools() throws Exception {
        // Arrange
        Path servicesRoot = Files.createTempDirectory("legacy-non-tool");
        Path descriptor = servicesRoot.resolve(
                "META-INF/services/org.machanism.machai.ai.tools.FunctionTools");
        Files.createDirectories(descriptor.getParent());
        Files.write(descriptor, "java.lang.String\n".getBytes(StandardCharsets.UTF_8));
        ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();

        try (URLClassLoader contextLoader = new URLClassLoader(
                new URL[] { servicesRoot.toUri().toURL() }, previousLoader)) {
            Thread.currentThread().setContextClassLoader(contextLoader);

            // Act
            FunctionToolsLoader loader = new FunctionToolsLoader();

            // Assert
            assertTrue(discoveredTools(loader).stream()
                    .noneMatch(tool -> tool.getClass().equals(String.class)));
        } finally {
            Thread.currentThread().setContextClassLoader(previousLoader);
            deleteRecursively(servicesRoot);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<FunctionTools> discoveredTools(FunctionToolsLoader loader) throws Exception {
        java.lang.reflect.Field field = FunctionToolsLoader.class.getDeclaredField("functionTools");
        field.setAccessible(true);
        return (List<FunctionTools>) field.get(loader);
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
}
