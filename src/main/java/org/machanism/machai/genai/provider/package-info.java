/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Concrete generative-AI provider implementations and supporting utilities used
 * by Machai.
 *
 * <p>
 * The classes in this package form the integration layer between Machai's
 * provider contracts and remote model APIs. A provider accepts prompts,
 * translates Machai configuration and tool definitions into SDK-specific
 * request objects, submits requests, resolves model-issued tool calls, records
 * usage, and returns the final result through the common
 * {@link org.machanism.machai.process.provider.ProcessProvider} contract.
 * Conversation input and provider-specific tool registrations are retained
 * until {@link org.machanism.machai.process.provider.ProcessProvider#clear()}
 * is called. Provider instances are therefore conversation-scoped and should
 * not be shared by concurrent conversations without external synchronization.
 * </p>
 *
 * <h2>Implementations and relationships</h2>
 * <ul>
 * <li>{@link OpenAIProvider} adapts the OpenAI Java SDK Responses API. It
 * supports text prompts, function tools, OpenAI web search, MCP server tools,
 * response-usage accounting, and embeddings. Its client reads the API key and
 * optional compatible base URL from the configurator, applies the configured
 * timeout, output-token, and tool-call limits, and uses the configured model
 * when creating requests. A blank model is rejected after the client is
 * created, with the available model list included in the exception.</li>
 * <li>{@link AnthropicProvider} adapts the Anthropic Java SDK Beta Messages
 * API. It maintains user and assistant message history, executes registered
 * local tools, forwards MCP server definitions, supports the configured
 * Anthropic web-search tool versions, applies ephemeral cache control to the
 * final registered local tool, and records input and output usage. The API key,
 * optional base URL, and timeout are read from the configurator.</li>
 * <li>{@link CodeMieProvider} is a routing adapter around the OpenAI-compatible
 * and Anthropic-compatible implementations. It obtains an OAuth 2.0 access
 * token from EPAM CodeMie using a password grant for an e-mail username or
 * client credentials otherwise, then refreshes that token whenever the
 * delegated client is created. Blank, {@code gpt-*}, {@code gemini-*},
 * {@code text-embedding-*}, {@code codemie-text-embedding-*}, and
 * {@code amazon.titan-embed-text-*} model names select {@link OpenAIProvider};
 * {@code claude-*} names select {@link AnthropicProvider}; other prefixes are
 * rejected. The token endpoint can be overridden with {@code AUTH_URL}; the
 * downstream clients use the CodeMie API base URL and the retrieved token.</li>
 * </ul>
 *
 * <h2>Supporting utilities</h2>
 * <ul>
 * <li>{@link TypeConverter} converts reflected tool arguments from strings or
 * JSON into Java values and maps Java classes to the simplified schema types
 * used by tool metadata.</li>
 * <li>{@link ToolLogger} is the package-local logging helper used for tool
 * inputs, results, and failures. Debug logging retains complete serialized
 * payloads, while info logging abbreviates them.</li>
 * <li>{@link AbstractAIProvider} supplies shared provider configuration,
 * conversation limits, project-directory handling, tool invocation, and common
 * web-search and MCP configuration hooks used by the concrete providers.</li>
 * </ul>
 *
 * <h2>Common lifecycle</h2>
 * <p>
 * Create a provider, initialize it with a model and a configured
 * {@code Configurator}, add prompts through the provider API, optionally
 * register tools through the surrounding Machai provider or adapter API, and
 * call {@code perform()}. Concrete providers submit follow-up requests as
 * needed to resolve model-issued tool calls. Implementations that support
 * vector generation also implement
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
 * <h2>Configuration and boundaries</h2>
 * <p>
 * Credentials, endpoint selection, timeouts, web search, MCP servers, tool
 * execution, and output limits are backend-specific. The class-level
 * documentation of {@link OpenAIProvider}, {@link AnthropicProvider}, and
 * {@link CodeMieProvider} describes the supported configuration keys and
 * delegation behavior. These classes are service adapters, not a general-
 * purpose tool registry; host-side deterministic tool workflows should use the
 * separate tools-provider implementation. Providers should be initialized
 * before use and cleared before reuse so that prompts and tool results from a
 * previous conversation are not sent with the next request.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.genai.provider;
