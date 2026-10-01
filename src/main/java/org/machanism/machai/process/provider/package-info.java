/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Provider contracts, reusable lifecycle infrastructure, and local dispatch
 * implementations for Machai process integrations.
 *
 * <p>
 * The package separates provider-facing orchestration from provider-specific
 * transport code. {@link ProcessProvider} defines the common lifecycle for
 * initialization, instructions, prompts, tool and resource registration,
 * execution, project-directory context, error handling, and state clearing.
 * Implementations may retain conversation or request state between calls, so a
 * caller should use {@link ProcessProvider#clear()} when beginning an
 * independent request and should not assume that every implementation has the
 * same execution semantics.
 * </p>
 *
 * <h2>Provider contracts and decorators</h2>
 * <ul>
 * <li>
 * {@link ProcessProvider} is the primary abstraction for conversational or
 * process-oriented providers. {@link EmbeddingProvider} is the separate
 * contract for providers that turn text into embedding vectors.
 * </li>
 * <li>
 * {@link ProcessProviderAdapter} is a delegating implementation useful for
 * decorating a provider with application-level concerns such as logging,
 * metrics, retries, or request shaping. Set its delegate before forwarding
 * lifecycle calls.
 * </li>
 * <li>
 * {@link AbstractAIProvider} supplies shared state and reflection-based
 * registration for methods annotated with
 * {@link org.machanism.machai.process.tools.Tool},
 * {@link org.machanism.machai.process.tools.Prompt}, and
 * {@link org.machanism.machai.process.tools.Resource}. It also provides model,
 * configuration, project-directory, instruction, token-limit, and tool-error
 * handling state for concrete subclasses.
 * </li>
 * </ul>
 *
 * <h2>Included implementations</h2>
 * <ul>
 * <li>
 * {@link NoneProvider} is an intentional no-op provider. It accepts lifecycle
 * input without retaining or processing it, returns {@code null} from
 * {@link NoneProvider#perform()}, and always reports an immutable empty tool
 * list. Initializing it with model {@code "log"} enables diagnostic INFO
 * messages; other model values disable those messages.
 * </li>
 * <li>
 * {@link ToolsProvider} is a lightweight local dispatcher. It stores prompts,
 * keeps registered tools in registration order, and, when initialized with
 * model {@code "yaml"}, parses the most recently submitted prompt as a YAML
 * mapping. The mapping must contain a {@code tool} name and may contain
 * {@code params}; the named function is invoked, with non-string results
 * serialized as JSON. A YAML execution requires at least one prompt. Register
 * annotated functions through
 * {@link ProcessProvider#addTools(org.machanism.machai.process.tools.FunctionTools,
 * String[])} rather than calling the protected registration hook directly.
 * </li>
 * </ul>
 *
 * <h2>Supporting utilities</h2>
 * <p>
 * {@link ToolLogger} records tool, prompt, and resource invocation details while
 * abbreviating payloads at INFO level and retaining complete payloads at DEBUG
 * level. {@link TypeConverter} converts reflected tool parameters from string
 * or JSON representations and maps Java parameter types to simplified schema
 * type names. These utilities support the provider infrastructure and are not
 * provider-selection mechanisms.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <p>
 * The following example registers annotated host functions and dispatches a
 * YAML request. The YAML {@code tool} value must match a registered function
 * name; the {@code Configurator} setup is application-specific.
 * </p>
 * <pre>
 * ProcessProvider provider = new ToolsProvider();
 * provider.init("yaml", configurator);
 * provider.addTools(functionTools, null);
 * provider.prompt("tool: summarize\nparams:\n  path: README.md");
 * String result = provider.perform();
 * provider.clear();
 * </pre>
 *
 * <p>
 * Use the no-op implementation when processing must be deliberately disabled:
 * </p>
 * <pre>
 * ProcessProvider provider = new NoneProvider();
 * provider.init("log", configurator);
 * provider.prompt("This input is accepted and discarded.");
 * String result = provider.perform(); // always null
 * </pre>
 *
 * @see ProcessProvider
 * @see EmbeddingProvider
 * @see AbstractAIProvider
 * @see ProcessProviderAdapter
 * @see NoneProvider
 * @see ToolsProvider
 * @see ToolLogger
 * @see TypeConverter
 * @since 1.2.0
 */
package org.machanism.machai.process.provider;
