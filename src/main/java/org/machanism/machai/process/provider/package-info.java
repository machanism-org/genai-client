/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Public contracts and reusable infrastructure for application-facing process
 * providers.
 *
 * <p>
 * A process provider is an adapter between an application and a generative-AI
 * backend, local model, or deliberately disabled implementation. This package
 * keeps the lifecycle contract independent of vendor SDKs: callers initialize a
 * provider with a model identifier and a {@link org.machanism.macha.core.commons.configurator.Configurator},
 * supply instructions and prompts, register optional capabilities, execute the
 * request, and inspect the result through the same API. Provider instances are
 * generally stateful and should be treated as non-thread-safe unless an
 * implementation documents otherwise.
 * </p>
 *
 * <h2>Public contracts</h2>
 * <ul>
 * <li>
 * {@link ProcessProvider} defines the conversational lifecycle. It includes
 * initialization, prompts and instructions, request clearing, reflective tool,
 * prompt, and resource registration, project-directory context, registered-tool
 * inspection, execution, and tool-error handling policy.
 * </li>
 * <li>
 * {@link EmbeddingProvider} is an independent contract for generating a numeric
 * vector from text. An embedding implementation does not need to support
 * conversational prompts or function tools, although one implementation may
 * also implement {@link ProcessProvider}.
 * </li>
 * </ul>
 *
 * <h2>Shared implementation services</h2>
 * <ul>
 * <li>
 * {@link AbstractAIProvider} is the extension point for backend integrations. It
 * stores model, configurator, instruction, project-directory, timeout, and output
 * limit state; reads optional MCP and web-search settings; discovers annotated
 * capabilities by reflection; converts arguments; logs activity; and provides
 * guarded tool invocation. Concrete subclasses supply backend-specific request
 * construction and registration hooks.
 * </li>
 * <li>
 * {@link ProcessProviderAdapter} is a delegating decorator for cross-cutting
 * concerns such as logging, metrics, retries, or request transformation. Set a
 * delegate with {@link ProcessProviderAdapter#setProvider(ProcessProvider)}
 * before using it; otherwise calls cannot be forwarded. The adapter is not
 * thread-safe unless the delegate and calling arrangement provide that guarantee.
 * </li>
 * <li>
 * {@link TypeConverter} is a non-instantiable utility used by reflective
 * handlers. It maps supported Java parameter classes to simple schema names and
 * converts textual values into primitive wrappers, strings, files, collections,
 * maps, and JSON-backed objects.
 * </li>
 * <li>
 * {@link ToolLogger} is package-private logging infrastructure for tool, prompt,
 * and resource activity. Informational messages abbreviate payloads, while debug
 * messages retain serialized payloads and failure details.
 * </li>
 * </ul>
 *
 * <h2>Concrete implementations</h2>
 * <p>
 * Implementations are organized in subpackages. The {@code impl} package
 * provides {@link org.machanism.machai.process.provider.impl.NoneProvider}, a
 * no-op provider whose execution result is always {@code null}, and
 * {@link org.machanism.machai.process.provider.impl.ToolsProvider}, a local
 * provider that dispatches a YAML-described call to a registered Java tool when
 * initialized with the {@code "yaml"} model. Other adapters can live in their
 * own subpackages while implementing the contracts defined here.
 * </p>
 *
 * <h2>Request lifecycle</h2>
 * <p>
 * A typical conversational request initializes a provider, optionally sets
 * instructions and a project directory, registers capabilities, adds one or
 * more prompts, and calls {@link ProcessProvider#perform()}. The meaning of
 * {@link ProcessProvider#clear()} and whether state is retained after execution
 * are implementation-specific. In particular, callers should consult the
 * selected implementation before assuming that {@code clear()} removes every
 * kind of registered capability. Configure
 * {@link ProcessProvider#setErrorHandling(boolean)} according to whether tool
 * failures should be returned to the model as text or propagated to the caller.
 * </p>
 *
 * <h2>Conversational usage</h2>
 * <pre>
 * Configurator configurator = ...;
 * ProcessProvider provider = ...;
 * provider.init("model-id", configurator);
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize the project architecture.");
 * String response = provider.perform();
 * provider.clear();
 * </pre>
 *
 * <h2>Embedding usage</h2>
 * <pre>
 * Configurator configurator = ...;
 * EmbeddingProvider provider = ...;
 * provider.init("embedding-model", configurator);
 * java.util.List&lt;Double&gt; vector = provider.embedding("example text", 384);
 * </pre>
 *
 * <p>
 * Reflective capabilities are supplied by application classes implementing
 * {@link org.machanism.machai.process.tools.FunctionTools}. Methods annotated
 * with the package's tool, prompt, or resource annotations are discovered by
 * {@link AbstractAIProvider}; their names, descriptions, parameter descriptors,
 * and invocation callbacks are then exposed through the corresponding
 * registration methods on {@link ProcessProvider}.
 * </p>
 *
 * @see ProcessProvider
 * @see EmbeddingProvider
 * @see AbstractAIProvider
 * @see ProcessProviderAdapter
 * @see TypeConverter
 * @since 1.2.0
 */
package org.machanism.machai.process.provider;
