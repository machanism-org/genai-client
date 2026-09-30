/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Concrete generative-AI provider implementations used by Machai.
 *
 * <p>
 * This package is the integration layer between Machai's provider contracts and
 * remote model APIs. Providers retain the inputs for the current conversation,
 * translate Machai tools and configuration into the SDK-specific request model,
 * submit requests, and translate the final response back to the common
 * {@link org.machanism.machai.process.provider.ProcessProvider} contract. Request-scoped state is retained until
 * {@link org.machanism.machai.process.provider.ProcessProvider#clear()} is called; provider instances should therefore
 * not be shared between concurrent conversations unless the caller supplies the
 * necessary synchronization.
 * </p>
 *
 * <h2>Implementations and relationships</h2>
 * <ul>
 * <li>{@link OpenAIProvider} uses the OpenAI Java SDK Responses API. It supports
 * text prompts, registered function tools, OpenAI web search, MCP server tools,
 * response-usage accounting, and embedding requests. Its API key and optional
 * base URL are read from the supplied
 * {@link org.machanism.macha.core.commons.configurator.Configurator}.</li>
 * <li>{@link AnthropicProvider} uses the Anthropic Java SDK Beta Messages API. It
 * builds user and assistant message history, executes registered local tools,
 * forwards MCP server definitions, supports the configured Anthropic web-search
 * tool versions, applies ephemeral cache control to the final registered local
 * tool, and records input and output usage.</li>
 * <li>{@link CodeMieProvider} is an adapter around the other two implementations.
 * It obtains an OAuth 2.0 access token from EPAM CodeMie using a password grant
 * for an e-mail username or client credentials otherwise, then delegates model
 * requests to {@code OpenAIProvider} for blank, {@code gpt-*}, {@code gemini-*},
 * {@code text-embedding-*}, {@code codemie-text-embedding-*}, and
 * {@code amazon.titan-embed-text-*} model names. {@code claude-*} names are
 * delegated to {@code AnthropicProvider}; other prefixes are rejected.</li>
 * </ul>
 *
 * <h2>Common lifecycle</h2>
 * <p>
 * Initialize a provider with its model and a configured
 * {@code Configurator},
 * add prompts through the provider API, optionally register tools through the
 * surrounding Machai provider or adapter API, and call {@code perform()}. The
 * concrete provider handles tool-call follow-up requests until a final response
 * is available. Embedding-capable implementations additionally implement
 * {@link org.machanism.machai.process.provider.EmbeddingProvider#embedding(String, long)}.
 * </p>
 *
 * <pre>
 * Configurator configurator = configuredCredentials();
 * ProcessProvider provider = new OpenAIProvider();
 * provider.init("gpt-4.1", configurator);
 * provider.prompt("Summarize the project architecture.");
 * String answer = provider.perform();
 * provider.clear();
 * </pre>
 *
 * <p>
 * Required credentials and optional endpoint, timeout, web-search, MCP, and
 * output-limit settings are provider-specific. See the class-level
 * documentation of {@link OpenAIProvider}, {@link AnthropicProvider}, and
 * {@link CodeMieProvider} for the supported configuration keys and backend
 * behavior. This package provides service adapters, not a general-purpose tool
 * registry; host-side deterministic tool workflows should use the separate tools
 * provider implementation.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.ai.provider.impl;
