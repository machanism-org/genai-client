/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Coordinates provider construction and in-memory token-usage reporting for
 * generative-AI process integrations.
 *
 * <p>The package has two responsibilities. {@link ProcessProviderManager} is
 * the reflection-based entry point for creating configured process and
 * embedding providers. {@link Usage} represents the token counts reported by
 * one interaction, while {@link UsageStatistics} collects those records by
 * model identifier and writes aggregate summaries through SLF4J.</p>
 *
 * <h2>Provider resolution</h2>
 * <p>Provider requests use the form {@code Provider:Model}. The manager splits
 * at the first colon: the text before it identifies the provider and the text
 * after it is passed to the provider as its model name. A blank provider
 * segment returns {@code null}. Provider implementations are created through
 * a public no-argument constructor and initialized with the model name and a
 * {@link org.machanism.macha.core.commons.configurator.Configurator}.</p>
 *
 * <p>Process-provider names must be valid Java identifiers. Simple names are
 * resolved against the supported provider class-name conventions, with the
 * later convention taking precedence when both classes exist, then against
 * the manager's nested-provider fallback. Embedding-provider
 * names containing a dot are treated as fully qualified class names; other
 * names use the same convention lookup. An embedding class must implement
 * {@link org.machanism.machai.process.provider.EmbeddingProvider}. Missing
 * classes, unavailable public no-argument constructors, reflection failures,
 * invalid process-provider names, and incompatible embedding classes are
 * reported as {@link IllegalArgumentException}. Runtime exceptions thrown by
 * an embedding provider during initialization are propagated unchanged.</p>
 *
 * <h2>Usage collection and reporting</h2>
 * <p>{@link Usage} is an immutable value object containing input, cached-input,
 * and output token counts. {@link UsageStatistics} stores usage records in a
 * static, in-memory map keyed by the exact model identifier supplied to
 * {@link UsageStatistics#addUsage(String, Usage)}. The registry is not
 * persisted between application runs. Model-specific additions and reads are
 * synchronized. {@link UsageStatistics#getUsageForModel(String)} returns a
 * defensive list copy, whereas
 * {@link UsageStatistics#getAllModelUsages()} returns a shallow map copy whose
 * value lists remain shared.</p>
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
 * <p>Call {@link UsageStatistics#init()} during startup when eager utility
 * class initialization is useful. Use {@link UsageStatistics#logUsage()} to
 * report each currently registered model, or
 * {@link UsageStatistics#logUsageForModel(String)} to report one model.</p>
 *
 * @see org.machanism.machai.process.provider.ProcessProvider
 * @see org.machanism.machai.process.provider.EmbeddingProvider
 * @see org.machanism.macha.core.commons.configurator.Configurator
 */
package org.machanism.machai.process.manager;
