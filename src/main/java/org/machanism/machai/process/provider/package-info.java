/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Core contracts and reusable infrastructure for Machai process providers.
 *
 * <p>A process provider builds and executes a request against a generative-AI
 * backend. The request may contain instructions, prompts, registered function
 * tools, prompt handlers, resource handlers, and an optional project-directory
 * context. Providers are initialized with a model identifier and a
 * {@link org.machanism.macha.core.commons.configurator.Configurator}; the
 * provider-specific implementation determines how that configuration is used
 * and how the response is produced.</p>
 *
 * <p>This package deliberately separates the application-facing contract from
 * backend SDKs. Applications normally depend on
 * {@link ProcessProvider} and use {@link ProcessProvider#init(String,
 * org.machanism.macha.core.commons.configurator.Configurator)},
 * {@link ProcessProvider#prompt(String)}, and {@link ProcessProvider#perform()}
 * without needing to know the selected backend. A provider instance can retain
 * request state between calls, so callers should invoke
 * {@link ProcessProvider#clear()} before beginning an independent request.</p>
 *
 * <h2>Public contracts</h2>
 * <ul>
 * <li>{@link ProcessProvider} defines initialization, request construction,
 * execution, tool and handler registration, project-directory propagation,
 * error-handling configuration, and inspection of registered tool names.</li>
 * <li>{@link EmbeddingProvider} defines the independent operation for generating
 * a vector from text. A provider may implement this contract in addition to
 * {@link ProcessProvider} when its backend supports embeddings.</li>
 * </ul>
 *
 * <h2>Reusable infrastructure</h2>
 * <ul>
 * <li>{@link AbstractAIProvider} supplies shared state and behavior for concrete
 * AI integrations, including configuration, request input logging, tool,
 * prompt, and resource discovery, project-directory handling, and guarded tool
 * invocation.</li>
 * <li>{@link ProcessProviderAdapter} forwards the
 * {@link ProcessProvider} contract to a delegate. It is suitable as a base for
 * decorators that add logging, metrics, retries, or request transformation.</li>
 * <li>{@link TypeConverter} maps Java types to simplified schema names and
 * converts string arguments received by reflected tool methods to Java values,
 * including primitive wrappers, collections, maps, and JSON-backed objects.</li>
 * <li>{@link ToolLogger} is the package-private logging helper used by provider
 * infrastructure to record tool, prompt, and resource activity. It abbreviates
 * payloads at INFO level and retains complete payloads and failure details for
 * DEBUG logging.</li>
 * </ul>
 *
 * <h2>Implementations</h2>
 * <p>Concrete implementations are organized in provider-specific subpackages.
 * The {@code impl} subpackage contains host-side implementations such as
 * {@link org.machanism.machai.process.provider.impl.NoneProvider}, which
 * intentionally performs no AI work, and
 * {@link org.machanism.machai.process.provider.impl.ToolsProvider}, which can
 * execute a locally registered tool from a YAML prompt. Other backend adapters
 * may live in separate provider packages while implementing these same core
 * contracts.</p>
 *
 * <h2>Request lifecycle</h2>
 * <p>The usual lifecycle is to create or resolve a provider, initialize it,
 * configure optional instructions and capabilities, add one or more prompts,
 * execute the request, and clear the accumulated state before reuse. Tool
 * failures can be configured either to become model-visible response data or to
 * propagate to the caller, depending on the implementation and the value passed
 * to {@link ProcessProvider#setErrorHandling(boolean)}.</p>
 *
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
 * <p>Embedding-capable implementations use the separate contract and do not
 * require conversational prompts:</p>
 *
 * <pre>
 * EmbeddingProvider provider = ...;
 * provider.init("embedding-model", configurator);
 * java.util.List&lt;Double&gt; vector = provider.embedding("example text", 384);
 * </pre>
 *
 * <p>Implementations are generally stateful and are not assumed to be safe for
 * concurrent request construction unless their own documentation explicitly
 * provides that guarantee.</p>
 */
package org.machanism.machai.process.provider;
