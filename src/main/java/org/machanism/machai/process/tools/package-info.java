/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Defines the provider-neutral contracts and runtime metadata used to expose
 * application capabilities as AI tools, prompts, and resources.
 *
 * <p>The package is organized around three layers:</p>
 * <ul>
 *   <li>Declaration annotations ({@link Tool}, {@link Prompt}, {@link Resource},
 *       and {@link Param}) describe methods and parameters for reflective
 *       discovery.</li>
 *   <li>Integration contracts ({@link FunctionTools} and {@link ToolFunction})
 *       allow an application to group capabilities and implement tool execution
 *       without coupling those capabilities to a particular AI provider.</li>
 *   <li>Discovery and supporting types ({@link FunctionToolsLoader},
 *       {@link SupportedFor}, {@link ParamDescriptor}, {@link Role}, and the
 *       package exception types) support registration, compatibility filtering,
 *       metadata construction, and failure reporting.</li>
 * </ul>
 *
 * <h2>Declaring capabilities</h2>
 * <p>Annotate public methods with {@link Tool}, {@link Prompt}, or {@link Resource}
 * according to the capability they expose. Each annotation is retained at runtime
 * and therefore can be inspected by a provider integration. A method parameter may
 * use {@link Param} to supply a stable name, description, and optional default value.
 * Descriptions should be specific because they may be shown both to people and to
 * models when a provider builds its capability catalog.</p>
 *
 * <ul>
 *   <li>{@link Tool} declares an invokable operation and requires a human-readable
 *       description. Its optional {@link Tool#name()} is the exposed name when one
 *       is explicitly supplied.</li>
 *   <li>{@link Prompt} declares a prompt-producing operation and associates it with
 *       a {@link Role}; its {@link Prompt#role()} defaults to {@link Role#USER}.</li>
 *   <li>{@link Resource} declares content available through one or more URI
 *       identifiers and optionally describes the content with a MIME type.</li>
 *   <li>{@link Param} describes a method parameter used by a tool or prompt. The
 *       sentinel values declared by the annotation represent unspecified metadata;
 *       integrations decide how to map that metadata to their schemas.</li>
 * </ul>
 *
 * <h2>Discovery and registration</h2>
 * <p>{@link FunctionTools} is a marker service-provider interface for a related set
 * of capabilities. Implementations can be registered with Java's
 * {@link java.util.ServiceLoader} using a service descriptor under
 * {@code META-INF/services}. {@link FunctionToolsLoader} discovers those
 * implementations, optionally applies {@link SupportedFor} compatibility rules,
 * and asks the configured {@link org.machanism.machai.process.provider.ProcessProvider}
 * to register their tools, prompts, and resources. An implementation without
 * {@code SupportedFor} is considered applicable to every requested application
 * class; exclusions take precedence when the annotation is present.</p>
 *
 * <p>The loader provides registration orchestration only. The provider remains
 * responsible for interpreting annotation metadata, validating arguments,
 * serializing results, invoking methods, and enforcing its own security and
 * lifecycle policies. This separation lets the same capability implementation be
 * reused with different provider implementations.</p>
 *
 * <h2>Executing tools</h2>
 * <p>{@link ToolFunction} is a functional callback for execution code that accepts
 * structured {@link com.fasterxml.jackson.databind.JsonNode} parameters and
 * optional runtime context objects. It returns the provider-facing result and may
 * throw an exception when execution fails. {@link ParamDescriptor} can represent
 * parameter metadata programmatically when annotations alone are insufficient.
 * {@link ErrorResultException} carries a structured error payload, while
 * {@link ToolExecutionException} represents an execution failure and
 * {@link SpecialException} can signal completion of a task without terminating the
 * host application.</p>
 *
 * <h2>Example</h2>
 * <pre>
 * public final class ProjectTools implements FunctionTools {
 *     {@code @Tool(description = "Reads a project resource by relative path.")}
 *     public String readResource(
 *             {@code @Param(description = "Path relative to the project root.")} String path) {
 *         return loadProjectResource(path);
 *     }
 *
 *     {@code @Prompt(description = "Creates a short project summary.", role = Role.USER)}
 *     public String summarizeProject() {
 *         return "Summarize the current project structure and key files.";
 *     }
 *
 *     {@code @Resource(uri = {"file:///schemas/project-schema.json"},
 *             description = "Validation schema for project descriptors.",
 *             mimeType = "application/json")}
 *     public String getProjectSchema() {
 *         return loadSchemaFile();
 *     }
 * }
 * </pre>
 *
 * <p>Applications should publish the implementation through the appropriate
 * service descriptor, ensure that declared URIs and descriptions remain stable,
 * and document any context objects expected by their {@link ToolFunction}
 * callbacks.</p>
 */
package org.machanism.machai.process.tools;
