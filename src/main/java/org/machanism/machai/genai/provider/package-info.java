/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Provider integrations that connect Machai conversations to generative-AI
 * model services.
 *
 * <p>This package is the SDK-facing integration layer. Its providers translate
 * Machai prompts, instructions, configuration, and reflected tool metadata into
 * provider-specific requests; submit those requests; execute matching local
 * tool functions when a model asks for them; append tool results to the
 * conversation; and record reported token usage. The public provider contract
 * is {@link org.machanism.machai.process.provider.ProcessProvider}; providers
 * that generate vectors additionally implement
 * {@link org.machanism.machai.process.provider.EmbeddingProvider}.</p>
 *
 * <h2>Provider implementations</h2>
 * <ul>
 * <li>{@link OpenAIProvider} uses the OpenAI Responses API. It supports text
 * prompts, function tools, configured web search and MCP tools, follow-up
 * requests for tool calls, usage accounting, and embeddings. Its client uses
 * {@code OPENAI_API_KEY}, the optional {@code OPENAI_BASE_URL}, and the common
 * timeout and output-limit settings described in its class documentation.</li>
 * <li>{@link AnthropicProvider} uses the Anthropic Beta Messages API. It keeps
 * user and assistant messages, forwards MCP definitions, supports the
 * supported Anthropic web-search tool versions, invokes local tools, applies
 * cache control to the final local tool, and records input and output usage.</li>
 * <li>{@link CodeMieProvider} is a routing adapter. Model names with supported
 * OpenAI-compatible prefixes, including blank model names and embedding
 * prefixes, delegate to {@link OpenAIProvider}; {@code claude-*} names
 * delegate to {@link AnthropicProvider}. It obtains an OAuth 2.0 token using a
 * password grant for an e-mail username or client credentials otherwise, and
 * supports an {@code AUTH_URL} token-endpoint override.</li>
 * </ul>
 *
 * <h2>Shared infrastructure</h2>
 * <ul>
 * <li>{@link AbstractAIProvider} supplies common configuration, model and
 * request limits, project-directory context, reflective registration of
 * annotated tools, prompts and resources, safe tool invocation, and MCP and
 * web-search configuration hooks.</li>
 * <li>{@link TypeConverter} converts reflected tool arguments from strings or
 * JSON into Java values and maps supported Java classes to schema type names.
 * It handles scalar values, files, lists, maps, and Jackson JSON nodes.</li>
 * <li>{@link ToolLogger} is the package-private execution logger. It emits
 * complete serialized payloads at debug level and abbreviated payloads at info
 * level, while recording tool failures with their project context.</li>
 * </ul>
 *
 * <h2>Lifecycle and conversation scope</h2>
 * <p>Initialize a provider with a model and a configured
 * {@link org.machanism.macha.core.commons.configurator.Configurator}, add
 * prompts and any annotated tool definitions, then call
 * {@code perform()}. A provider may issue additional backend requests while it
 * resolves model tool calls. Conversation inputs and registered provider state
 * remain until {@link org.machanism.machai.process.provider.ProcessProvider#clear()}
 * is called, so an instance represents one conversation and must not be shared
 * concurrently without external synchronization.</p>
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
 * <p>Credentials, endpoint selection, timeouts, search, MCP servers, tool
 * execution, and output limits are backend-specific. Consult the class-level
 * documentation of the concrete provider selected for the accepted keys and
 * delegation behavior. Providers are service adapters rather than a general
 * purpose tool registry; deterministic host-side workflows belong in the
 * separate tools-provider implementation.</p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.genai.provider;
