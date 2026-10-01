/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Provides provider factories and process-local token-usage reporting for
 * generative-AI integrations.
 *
 * <p>This package separates application configuration from provider
 * implementations. {@link ProcessProviderManager} parses a
 * {@code Provider:Model} expression, resolves a provider implementation, creates
 * it with a public no-argument constructor, and initializes it with a
 * {@link org.machanism.macha.core.commons.configurator.Configurator}. The
 * resulting object implements either
 * {@link org.machanism.machai.process.provider.ProcessProvider} or
 * {@link org.machanism.machai.process.provider.EmbeddingProvider}.</p>
 *
 * <h2>Provider creation</h2>
 * <p>Call
 * {@link ProcessProviderManager#getProvider(String, org.machanism.macha.core.commons.configurator.Configurator)}
 * for process providers and
 * {@link ProcessProviderManager#getEmbeddingProvider(String, org.machanism.macha.core.commons.configurator.Configurator)}
 * for embedding providers. Each method passes the text following the first
 * colon as the model name and returns {@code null} when the provider segment is
 * blank. Process-provider names must be valid Java identifiers. Simple names
 * are searched in the package conventions supported by the manager, followed by
 * its nested-provider fallback. For embeddings, a provider segment containing a
 * dot is used as a fully qualified class name; otherwise the same conventions
 * are used. The resolved embedding class must implement
 * {@link org.machanism.machai.process.provider.EmbeddingProvider}.</p>
 *
 * <p>Provider implementations must expose a public no-argument constructor.
 * Invalid names, missing classes, incompatible embedding classes, and reflective
 * construction failures are reported as {@link IllegalArgumentException}. A
 * runtime exception thrown by an embedding provider during initialization is
 * propagated unchanged.</p>
 *
 * <h2>Usage reporting</h2>
 * <p>{@link Usage} is an immutable record of input, cached-input, and output
 * token counts for one interaction. {@link UsageStatistics} stores these
 * records in a static, in-memory map keyed by the exact model identifier passed
 * to {@link UsageStatistics#addUsage(String, Usage)}. The registry is not
 * persisted between application runs. Model-specific additions and reads are
 * synchronized; logging writes summaries through the class's SLF4J logger.
 * {@link UsageStatistics#getUsageForModel(String)} returns a defensive list
 * copy, while {@link UsageStatistics#getAllModelUsages()} returns a shallow map
 * copy whose value lists remain shared.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * UsageStatistics.init();
 * Configurator conf = ...;
 * ProcessProvider chat = ProcessProviderManager.getProvider("OpenAI:gpt-4o", conf);
 * EmbeddingProvider embeddings = ProcessProviderManager.getEmbeddingProvider(
 *         "OpenAI:text-embedding-3-small", conf);
 * UsageStatistics.addUsage("OpenAI:gpt-4o", new Usage(500, 100, 200));
 * UsageStatistics.logUsage();
 * }</pre>
 *
 * <p>Call {@link UsageStatistics#init()} during startup if eager utility-class
 * initialization is desired. Use {@link UsageStatistics#logUsage()} to report
 * every currently registered model or
 * {@link UsageStatistics#logUsageForModel(String)} to report one model.</p>
 *
 * @see org.machanism.machai.process.provider.ProcessProvider
 * @see org.machanism.machai.process.provider.EmbeddingProvider
 * @see org.machanism.macha.core.commons.configurator.Configurator
 */
package org.machanism.machai.process.manager;
