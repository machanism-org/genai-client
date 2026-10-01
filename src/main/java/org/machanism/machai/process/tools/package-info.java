/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Declares the provider-neutral metadata and execution contracts used to expose
 * application methods as tools, prompts, and resources.
 *
 * <p>This package separates capability declaration from provider-specific
 * integration. Runtime-retained annotations describe reflective methods and
 * parameters, while the interfaces and exceptions support programmatic
 * registration and invocation. A provider is responsible for interpreting the
 * metadata, validating and converting arguments, invoking methods, and
 * serializing results.</p>
 *
 * <h2>Capability declarations</h2>
 * <ul>
 *   <li>{@link Tool} marks a method as an invokable operation. Its required
 *       description documents the operation; its optional name defaults to
 *       {@link Tool#NOT_DEFINED}.</li>
 *   <li>{@link Prompt} marks a method that produces a prompt and supplies its
 *       name, description, and {@link Role}. The default role is
 *       {@link Role#USER}.</li>
 *   <li>{@link Resource} marks a method that supplies content under one or more
 *       URI identifiers. Its description and optional MIME type help a provider
 *       decide how to publish and interpret that content.</li>
 *   <li>{@link Param} adds a name, description, and optional default value to a
 *       method parameter. {@link ParamDescriptor} provides equivalent metadata
 *       for programmatic registration and translates the {@link Param#NULL} and
 *       {@link Param#NOT_DEFINED} string sentinels to {@code null} where
 *       applicable.</li>
 * </ul>
 *
 * <p>Annotation metadata is available at runtime for provider integrations.
 * The {@code NOT_DEFINED} constants in {@link Tool}, {@link Prompt},
 * {@link Resource}, and {@link Param} represent omitted metadata; integrations
 * should apply their own schema rules instead of presenting those sentinel
 * values as user-facing names, descriptions, or MIME types.</p>
 *
 * <h2>Discovery and registration</h2>
 * <p>{@link FunctionTools} is a marker service-provider interface for a class
 * that groups related tools, prompts, and resources. Publish an implementation's
 * fully qualified class name in
 * {@code META-INF/services/org.machanism.machai.process.tools.FunctionTools}
 * for discovery through {@link java.util.ServiceLoader}.
 * {@link FunctionToolsLoader} also reads the legacy descriptor name used by
 * earlier releases. When {@link FunctionToolsLoader#applyTools(
 * org.machanism.machai.process.provider.ProcessProvider, String[], Class)} is
 * called, each discovered compatible implementation is passed to the provider
 * for tool, prompt, and resource registration in discovery order. Implementations
 * are not deduplicated.</p>
 *
 * <p>{@link SupportedFor} controls compatibility with the requested application
 * class. An absent annotation, or an annotation whose included-class list is
 * empty, permits every class. Otherwise, the application class must be
 * assignable to at least one included class. A matching class in
 * {@link SupportedFor#excludes()} always vetoes the match.</p>
 *
 * <h2>Execution and failure handling</h2>
 * <p>{@link ToolFunction} is the functional callback for programmatic tool
 * execution. Its {@link ToolFunction#apply(com.fasterxml.jackson.databind.JsonNode,
 * Object[])} method receives a JSON parameter tree and optional context objects,
 * such as a working-directory {@link java.io.File} or a
 * {@link org.machanism.macha.core.commons.configurator.Configurator}, and may
 * throw an exception. {@link ErrorResultException} carries a structured payload
 * in its message and serializes non-string payloads as JSON when possible.
 * {@link ToolExecutionException} represents a checked tool failure, whereas
 * {@link SpecialException} signals completion of the current task without
 * requiring the host application to terminate. Providers remain responsible for
 * catching, presenting, and mapping these failures to their own protocols.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public final class ProjectTools implements FunctionTools {
 *     @Tool(description = "Reads a project resource by relative path.")
 *     public String readResource(
 *             @Param(description = "Path relative to the project root.") String path) {
 *         return loadProjectResource(path);
 *     }
 * }
 * }</pre>
 *
 * <p>Keep published tool names, resource URI identifiers, and descriptions
 * stable when consumers depend on them. Document the context object types that
 * each {@link ToolFunction} callback expects, and ensure service descriptors are
 * packaged with their implementations.</p>
 */
package org.machanism.machai.process.tools;
