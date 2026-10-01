/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Coordinates provider creation and in-memory token-usage reporting for
 * generative-AI integrations.
 *
 * <p>This package is the management boundary between application configuration,
 * provider implementations, and callers that need an initialized
 * {@link org.machanism.machai.process.provider.ProcessProvider} or
 * {@link org.machanism.machai.process.provider.EmbeddingProvider}. The
 * {@link ProcessProviderManager} is a reflection-based factory. It parses a
 * provider/model expression, resolves the provider class, creates it through a
 * public no-argument constructor, and invokes its initialization method with a
 * {@link org.machanism.macha.core.commons.configurator.Configurator}.</p>
 *
 * <h2>Provider resolution</h2>
 * <p>Use
 * {@link ProcessProviderManager#getProvider(String, org.machanism.macha.core.commons.configurator.Configurator)}
 * for process providers and
 * {@link ProcessProviderManager#getEmbeddingProvider(String, org.machanism.macha.core.commons.configurator.Configurator)}
 * for embedding providers. Both methods accept a {@code Provider:Model}
 * expression and pass the text after the first colon to the provider. A blank
 * provider segment returns {@code null}. Process-provider names must be valid
 * Java identifiers and are resolved through the supported conventional provider
 * packages, with a nested-provider fallback when no conventional class is
 * available.</p>
 *
 * <p>Embedding providers use the same conventional resolution rules, except that
 * a provider segment containing a dot is treated as a fully qualified class name.
 * The resolved class must implement
 * {@link org.machanism.machai.process.provider.EmbeddingProvider}. A provider
 * must expose a public no-argument constructor. Invalid names, missing classes,
 * incompatible embedding classes, and most reflective failures are reported as
 * {@link IllegalArgumentException}; a runtime exception thrown by an embedding
 * provider during initialization is propagated unchanged.</p>
 *
 * <h2>Usage registry</h2>
 * <p>{@link Usage} records input, cached-input, and output token counts for one
 * interaction. {@link UsageStatistics} keeps those records in a process-local
 * static map keyed by the exact model identifier supplied to
 * {@link UsageStatistics#addUsage(String, Usage)}. The registry is not persisted
 * between application runs. Adding records and the model-specific accessors
 * synchronize on the registry, while logging reports summaries through the
 * package's SLF4J logger. The single-model accessor returns a defensive list
 * copy. {@link UsageStatistics#getAllModelUsages()} returns a shallow map copy,
 * so its value lists remain shared and should be treated as read-only.</p>
 *
 * <h2>Example</h2>
 * <pre>
 * UsageStatistics.init();
 * Configurator conf = ...;
 * ProcessProvider chat = ProcessProviderManager.getProvider("OpenAI:gpt-4o", conf);
 * EmbeddingProvider embeddings = ProcessProviderManager.getEmbeddingProvider(
 *         "OpenAI:text-embedding-3-small", conf);
 * UsageStatistics.addUsage("OpenAI:gpt-4o", new Usage(500, 100, 200));
 * UsageStatistics.logUsage();
 * </pre>
 *
 * <p>{@link UsageStatistics#init()} can be called during startup to force utility
 * class initialization. {@link UsageStatistics#logUsage()} reports each model
 * currently visible to the registry, and
 * {@link UsageStatistics#logUsageForModel(String)} reports one model.</p>
 *
 * @see org.machanism.machai.process.provider.ProcessProvider
 * @see org.machanism.machai.process.provider.EmbeddingProvider
 * @see org.machanism.macha.core.commons.configurator.Configurator
 */
package org.machanism.machai.process.manager;
