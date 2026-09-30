/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Coordinates runtime provider construction and records token usage for the
 * application's generative-AI process integrations.
 *
 * <p>This package is the management layer between provider implementations and
 * callers that need either an initialized process provider or usage reporting.
 * {@link ProcessProviderManager} uses reflection and a
 * {@link org.machanism.macha.core.commons.configurator.Configurator} to create
 * providers. {@link Usage} represents the metrics returned for one interaction,
 * and {@link UsageStatistics} groups those records in an in-memory registry by
 * the model identifier supplied by the caller.</p>
 *
 * <h2>Provider construction</h2>
 * <p>Call
 * {@link ProcessProviderManager#getProvider(String, org.machanism.macha.core.commons.configurator.Configurator)}
 * or
 * {@link ProcessProviderManager#getEmbeddingProvider(String, org.machanism.macha.core.commons.configurator.Configurator)}
 * with an identifier in the form {@code Provider:Model}. The model portion is
 * passed unchanged to the provider's initialization method. A blank provider
 * portion returns {@code null}; otherwise, a provider must be constructible
 * through a public no-argument constructor.</p>
 *
 * <p>Chat providers use a Java-identifier provider name and are resolved against
 * the package conventions supported by {@code ProcessProviderManager}. The
 * manager checks its configured conventional implementation names and then its
 * nested-provider fallback. Embedding providers use the same conventions, but a
 * provider portion containing a dot is also accepted as a fully qualified class
 * name. The resolved embedding class must implement
 * {@link org.machanism.machai.process.provider.EmbeddingProvider}. Invalid names,
 * missing classes, incompatible embedding classes, and construction or
 * initialization failures are reported as {@link IllegalArgumentException}.</p>
 *
 * <h2>Usage collection</h2>
 * <p>{@link UsageStatistics} is a process-local static registry; it does not
 * persist records between application runs. Records are grouped under the exact
 * model identifier passed to
 * {@link UsageStatistics#addUsage(String, Usage)}. Each {@link Usage} instance
 * is immutable after construction. The single-model accessor returns a defensive
 * list copy, whereas the all-model accessor returns a shallow map copy whose
 * value lists are shared with the registry. Callers should therefore treat the
 * lists returned by the latter accessor as read-only, particularly while usage
 * is being logged.</p>
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
 * <p>The explicit {@link UsageStatistics#init()} call is optional and can be
 * used to force utility-class initialization during application startup.
 * {@link UsageStatistics#logUsage()} reports totals for the models currently
 * registered, while
 * {@link UsageStatistics#logUsageForModel(String)} reports one model.</p>
 *
 * @see org.machanism.machai.process.provider.ProcessProvider
 * @see org.machanism.machai.process.provider.EmbeddingProvider
 * @see org.machanism.macha.core.commons.configurator.Configurator
 */
package org.machanism.machai.process.manager;
