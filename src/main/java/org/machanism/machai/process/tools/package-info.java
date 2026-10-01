/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Provider-neutral declarations and runtime support for exposing application
 * methods as AI tools, prompts, and resources.
 *
 * <p>The package deliberately separates the reflective declaration of a
 * capability from its provider-specific execution. {@link Tool}, {@link Prompt},
 * and {@link Resource} describe methods, while {@link Param} describes their
 * inputs. A provider decides how to validate and convert arguments, invoke the
 * method, and serialize its result.</p>
 *
 * <h2>Declaring capabilities</h2>
 * <ul>
 *   <li>{@link Tool} marks an invokable operation and requires a description;
 *       its name is optional and defaults to {@link Tool#NOT_DEFINED}.</li>
 *   <li>{@link Prompt} marks a prompt-producing method, with a name,
 *       description, and {@link Role} (defaulting to {@link Role#USER}).</li>
 *   <li>{@link Resource} marks a method that supplies content identified by one
 *       or more URI strings and optionally declares a MIME type.</li>
 *   <li>{@link Param} supplies a parameter name, description, and optional
 *       default value. {@link ParamDescriptor} offers the same metadata for
 *       programmatic registration and maps its {@link Param#NULL} and
 *       {@link Param#NOT_DEFINED} string sentinels to {@code null}.</li>
 * </ul>
 * <p>These annotations have runtime retention so provider integrations can
 * inspect them reflectively. The {@code NOT_DEFINED} constants in
 * {@link Tool}, {@link Prompt}, {@link Resource}, and {@link Param} indicate
 * omitted metadata; integrations should apply their own schema rules rather
 * than expose those sentinel strings to users.</p>
 *
 * <h2>Grouping and registration</h2>
 * <p>{@link FunctionTools} is a marker service-provider interface. Implement a
 * class, publish its fully qualified name in
 * {@code META-INF/services/org.machanism.machai.process.tools.FunctionTools},
 * and let {@link FunctionToolsLoader} discover it with
 * {@link java.util.ServiceLoader}. The loader also reads the legacy descriptor
 * name used by earlier releases. For each discovered implementation compatible
 * with the requested application class, it calls the supplied
 * {@link org.machanism.machai.process.provider.ProcessProvider} to register
 * tools, prompts, and resources in discovery order.</p>
 *
 * <p>{@link SupportedFor} controls that compatibility check: no annotation or
 * an annotation with an empty included-class list permits every class; a
 * non-empty list requires an assignable match; and a matching class in
 * {@link SupportedFor#excludes()} always vetoes the match. The loader does not
 * deduplicate discovered implementations.</p>
 *
 * <h2>Execution and failures</h2>
 * <p>{@link ToolFunction} is the functional callback used for programmatic
 * execution. Its {@link ToolFunction#apply(com.fasterxml.jackson.databind.JsonNode,
 * Object[])} method receives a JSON parameter tree and optional context objects,
 * such as a working-directory {@link java.io.File} or a
 * {@link org.machanism.macha.core.commons.configurator.Configurator}, and may
 * throw an exception. {@link ErrorResultException} carries a structured payload
 * in its message and serializes non-string payloads as JSON when possible.
 * {@link ToolExecutionException} represents a checked execution failure, while
 * {@link SpecialException} signals completion of a task without requiring the
 * host application to terminate. Providers remain responsible for handling and
 * presenting these failures.</p>
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
 * <p>Keep published names, URI identifiers, and descriptions stable when
 * consumers depend on them, and document the context object types expected by
 * each {@link ToolFunction} callback.</p>
 */
package org.machanism.machai.process.tools;
