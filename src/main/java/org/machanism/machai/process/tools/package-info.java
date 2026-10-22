/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Defines the provider-neutral API for declaring, discovering, registering, and
 * executing application capabilities as tools, prompts, and resources.
 *
 * <p>The package is intentionally divided into three cooperating layers:</p>
 * <ul>
 *   <li><strong>Declarations:</strong> {@link Tool}, {@link Prompt}, and
 *       {@link Resource} are runtime-retained method annotations. {@link Param}
 *       supplies runtime-retained parameter metadata, and {@link Role} identifies
 *       the participant associated with a prompt.</li>
 *   <li><strong>Registration:</strong> {@link FunctionTools} is the marker
 *       service-provider interface for a capability bundle. Implementations are
 *       discovered by {@link FunctionToolsLoader} through
 *       {@link java.util.ServiceLoader} and registered with a
 *       {@link org.machanism.machai.process.provider.ProcessProvider}.</li>
 *   <li><strong>Execution and failures:</strong> {@link ToolFunction} is the
 *       callback contract for programmatic invocation. {@link ParamDescriptor}
 *       represents equivalent parameter metadata outside annotations, while
 *       {@link ErrorResultException}, {@link ToolExecutionException}, and
 *       {@link SpecialException} communicate structured errors, checked tool
 *       failures, and intentional task completion respectively.</li>
 * </ul>
 *
 * <h2>Declaring capabilities</h2>
 * <p>Annotate public or otherwise discoverable methods with {@link Tool} for
 * invokable operations, {@link Prompt} for prompt-producing methods, or
 * {@link Resource} for content identified by one or more URIs. Each annotation
 * requires a description. Names and MIME types use a package-defined
 * {@code NOT_DEFINED} sentinel when omitted; integrations should interpret that
 * sentinel according to their own naming and schema rules rather than exposing it
 * to consumers. A {@link Prompt} also declares a {@link Role}, defaulting to
 * {@link Role#USER}. Annotate method parameters with {@link Param} when their
 * names, descriptions, or default values must be published as input metadata.</p>
 *
 * <p>{@link ParamDescriptor} is the programmatic counterpart to {@link Param}.
 * Its {@link ParamDescriptor#getDescription()} and
 * {@link ParamDescriptor#getDefaultValue()} methods translate the
 * {@link Param#NULL} and {@link Param#NOT_DEFINED} string sentinels to
 * {@code null} where applicable. Providers remain responsible for validating
 * declarations, converting input values, invoking methods, and serializing
 * returned values.</p>
 *
 * <h2>Discovery and compatibility</h2>
 * <p>Implement {@link FunctionTools} in a class that groups related annotated
 * methods, then publish the implementation's fully qualified class name in
 * {@code META-INF/services/org.machanism.machai.process.tools.FunctionTools}.
 * {@link FunctionToolsLoader} also accepts the legacy descriptor name used by
 * earlier releases. When {@link FunctionToolsLoader#applyTools(
 * org.machanism.machai.process.provider.ProcessProvider, String[], Class)} is
 * called, compatible implementations are passed to the provider for tool,
 * prompt, and resource registration in discovery order. The loader does not
 * deduplicate implementations.</p>
 *
 * <p>{@link SupportedFor} narrows registration to application classes. With no
 * annotation, or with an empty {@link SupportedFor#value()} array, an
 * implementation is compatible with every application class. Otherwise, the
 * requested application class must be assignable to at least one included class.
 * A class assignable from the requested application class in
 * {@link SupportedFor#excludes()} vetoes an otherwise valid match. The
 * {@code value} and {@code excludes} arrays therefore allow both positive and
 * negative compatibility rules.</p>
 *
 * <h2>Executing tools and handling outcomes</h2>
 * <p>Use {@link ToolFunction} when a provider needs a programmatic callback
 * rather than reflective method invocation. Its
 * {@link ToolFunction#apply(com.fasterxml.jackson.databind.JsonNode, Object[])}
 * method receives a JSON parameter tree and optional context objects, such as a
 * working-directory {@link java.io.File} or a
 * {@link org.machanism.macha.core.commons.configurator.Configurator}, and may
 * throw an exception. {@link ToolFunction#SESSION_ID_PARAM_NAME} defines the
 * standard session identifier key.</p>
 *
 * <p>Throw {@link ErrorResultException} when a structured payload should be
 * returned through an exception message; non-string payloads are serialized as
 * JSON when possible. Use {@link ToolExecutionException} for a checked failure
 * from tool execution. Use {@link SpecialException} when the current task should
 * finish while the host application remains running. The provider integration is
 * responsible for catching these exceptions and mapping them to its protocol.</p>
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
 * stable when clients depend on them. Document the context object types expected
 * by each {@link ToolFunction}, and package every service descriptor with its
 * implementation.</p>
 */
package org.machanism.machai.process.tools;
