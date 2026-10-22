<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
# Page Structure: 
1. Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge ([![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
   - Bindex Badge [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/[artifactId]/refs/heads/main/bindex.json)
# Overview
   - Full description the project based on package-info.java files in source folder..
   - Use the project structure diagram by the path: `./images/c4-diagram.png` (`src/site/puml/c4-diagram.puml`).
# Supported AI providers
   - Describe all supported AP providers with configurations.
   - Table of common configuration parameters, their descriptions, and default values.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# GenAI Client

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg)](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/genai-client/refs/heads/main/bindex.json)

## Overview

GenAI Client is a Java library designed for integrating Machai applications with generative AI providers through a consistent provider abstraction. It provides provider resolution, prompt and instruction handling, runtime configuration, optional embedding generation, usage tracking, and Java function-tool, prompt, and resource registration for AI-powered workflows.

The library is organized around the common `ProcessProvider` lifecycle contract and shared provider infrastructure. Applications can resolve a configured provider from identifiers such as `OpenAI:gpt-4o-mini`, attach prompts, system instructions, tools, resources, web-search support, or MCP servers, and execute requests without depending directly on vendor-specific SDK details. Token usage is captured in immutable usage records and can be aggregated per model for reporting and diagnostics.

`ProcessProviderManager` resolves `Provider:Model` identifiers by reflection, initializes public no-argument provider implementations with a `Configurator`, and separately resolves embedding providers. `Usage` records input, cached-input, and output token counts, while `UsageStatistics` keeps in-memory records keyed by the exact model identifier and exposes model-specific or aggregate reporting. Provider instances retain their conversation and registration state until `clear()` is called and should not be shared concurrently without external synchronization.

GenAI Client also includes a lightweight tool metadata layer. Java methods annotated as tools, prompts, or resources can be discovered through `ServiceLoader`, described with parameter metadata, filtered for supported application classes, and registered with providers as AI-callable functions or resource callbacks. This enables advanced use cases such as semantic search, automated content generation, intelligent project assembly, structured tool execution, local tool orchestration, and provider-independent prompt workflows.

The `org.machanism.machai.process.tools` package also provides provider-neutral callback and failure contracts. `ToolFunction` supports programmatic JSON-based invocation, `ParamDescriptor` represents programmatic parameter metadata, and `ErrorResultException`, `ToolExecutionException`, and `SpecialException` distinguish structured results, checked tool failures, and intentional task completion. Service descriptors are loaded in discovery order; `SupportedFor` can restrict a capability bundle to application classes and its exclusions can veto an otherwise compatible match.

![GenAI Client structure](./images/c4-diagram.png)

The main package areas are:

- `org.machanism.machai.process.manager` resolves configured provider identifiers, initializes provider instances, and records token usage by model.
- `org.machanism.machai.process.provider` defines the common provider contracts and reusable base behavior for conversational, embedding, tool-enabled, web-search-enabled, and MCP-enabled AI integrations.
- `org.machanism.machai.genai.provider` contains concrete provider implementations for OpenAI-compatible APIs, Anthropic Claude, and CodeMie, while `org.machanism.machai.process.provider` contains the direct host-side Tools and None providers.
- `org.machanism.machai.process.tools` defines tool, prompt, resource, parameter, role, and supported-application metadata, plus service loading and callback contracts used to expose Java methods as AI-accessible capabilities.

## Supported AI providers

### OpenAI

The OpenAI provider adapts the Machai `ProcessProvider` and `EmbeddingProvider` APIs to the OpenAI Java SDK Responses API and Embeddings API. It supports conversational text generation, iterative function-tool calling, optional OpenAI web search, MCP server tools, embeddings, request input logging, and OpenAI usage conversion.

Typical configuration includes an OpenAI API key, a chat or embedding model name, and optional values such as a custom OpenAI-compatible base URL, timeout, maximum output tokens, and tool-call limits. It can also be used with OpenAI-compatible endpoints by overriding the base URL.

Configure it with an identifier such as `OpenAI:gpt-4o-mini` or `OpenAI:text-embedding-3-small`, `OPENAI_API_KEY`, and, for a compatible gateway, `OPENAI_BASE_URL`. `GENAI_TIMEOUT`, `MAX_OUTPUT_TOKENS`, `MAX_TOOL_CALLS`, `WebSearchTool.*`, and `MCP*` settings are optional.

`WebSearchTool.type` is provider-specific: OpenAI uses its supported search tool type, while Anthropic accepts `default`, `20260209`, or `20250305`. MCP groups are read as `MCP.*`, then sequentially as `MCP_1.*`, `MCP_2.*`, and so on; a group is registered when its name is present.

### Anthropic

The Anthropic provider adapts the Machai `ProcessProvider` API to Anthropic Claude models through the Anthropic Java SDK Beta Messages API. It supports prompt execution, system instructions, custom function tools, automatic tool-use loops, optional web search, MCP server forwarding, prompt-cache control on the final registered tool, and token-usage capture.

Typical configuration includes an Anthropic API key or authorization token, a Claude model identifier, and optional values such as a custom base URL, timeout, output-token limits, web-search settings, and MCP server definitions. When local function tools are registered, the provider applies Anthropic ephemeral prompt-cache control to the final registered tool.

Configure it with an identifier such as `Anthropic:claude-3-5-sonnet`, `ANTHROPIC_API_KEY`, and optionally `ANTHROPIC_BASE_URL`, `GENAI_TIMEOUT`, `MAX_OUTPUT_TOKENS`, `WebSearchTool.*`, and `MCP*` settings. `MAX_TOOL_CALLS` is an OpenAI Responses API setting and does not limit Anthropic tool-use loops.

Anthropic treats a key beginning with `sk-` as an API key and other non-null credentials as authorization tokens. Local tools are sent in registration order; the final local tool receives ephemeral cache-control metadata when tools are present.

### CodeMie

The CodeMie provider integrates with EPAM CodeMie Code Assistant endpoints. It authenticates with a CodeMie OpenID Connect token endpoint, obtains OAuth 2.0 bearer tokens, configures the delegated AI provider with the CodeMie API base URL, and routes supported model families to the appropriate implementation.

Models beginning with `gpt-`, `gemini-`, `text-embedding-`, `codemie-text-embedding-`, or `amazon.titan-embed-text-` are delegated to the OpenAI provider configured for CodeMie endpoints. Models beginning with `claude-` are delegated to the Anthropic provider. The provider supports password-grant and client-credentials authentication flows based on `GENAI_USERNAME` and `GENAI_PASSWORD`, with an optional `AUTH_URL` token-endpoint override.

Blank model names are also routed to the OpenAI-compatible implementation. Unsupported model prefixes fail during initialization. For OpenAI-compatible delegation, CodeMie sets `OPENAI_BASE_URL` to its Code Assistant API and refreshes the bearer token when the client is created; for Claude delegation it sets the corresponding Anthropic base URL and refreshes the Anthropic authorization token.

Use an identifier such as `CodeMie:gpt-4o-mini` or `CodeMie:claude-3-5-sonnet` together with `GENAI_USERNAME` and `GENAI_PASSWORD`. A username containing `@` selects the password grant; any other username selects the client-credentials grant. CodeMie supplies the delegated provider's base URL and bearer token, so callers do not configure `OPENAI_API_KEY` or `ANTHROPIC_API_KEY` directly.

### Tools provider

The Tools provider exposes registered application functions for structured invocation. It collects prompts, registers `ToolFunction` callbacks, and executes those callbacks from YAML-based tool-call descriptions containing a tool name and parameter payload. This provider is useful for internal orchestration when host-defined tools need to be invoked through the same lifecycle as other AI providers without sending the request to an external model service.

Configure it with the `Tools:yaml` identifier. Register the local callbacks before calling `perform()` and supply the final prompt as the YAML tool-call descriptor; no external credentials or endpoint settings are used.

The descriptor must contain a `tool` name and may contain `params`. The provider invokes the matching registered function, serializes non-string results as JSON, preserves tool registration order, and uses fail-fast error handling. At least one prompt and a matching tool are required for a YAML execution.

### None provider

The None provider is a disabled, no-op implementation for configurations that must not make AI requests. It accepts the standard provider lifecycle calls, discards submitted state, and returns `null` from execution. Initialize it with the model name `log` to emit its implemented lifecycle activity at INFO level; other model names remain silent. It is useful as a safe default and in tests.

Configure it with `None:log` for INFO-level lifecycle diagnostics, or with another model value such as `None:disabled` for silent no-op behavior. It requires no credentials or endpoint settings.

## Common configuration parameters

| Parameter | Description | Default value |
| --- | --- | --- |
| Provider/model identifier | Model identifier used by the selected provider. `ProcessProviderManager` commonly resolves identifiers in the `Provider:Model` form, such as `OpenAI:gpt-4o-mini`; the provider itself receives the model portion. | Required |
| `OPENAI_API_KEY` | API key for OpenAI or OpenAI-compatible endpoints. CodeMie sets this to the retrieved OAuth 2.0 bearer token for delegated OpenAI-compatible requests. | Required for OpenAI-compatible providers |
| `OPENAI_BASE_URL` | Optional base URL override for OpenAI-compatible APIs. | OpenAI SDK default |
| `ANTHROPIC_API_KEY` | API key or authorization token for Anthropic Claude requests. | Required for Anthropic |
| `ANTHROPIC_BASE_URL` | Optional base URL override for Anthropic-compatible APIs. | Anthropic SDK default |
| `GENAI_TIMEOUT` | Request timeout in seconds. A value of `0` or an absent value leaves SDK defaults in effect. | `0` |
| `MAX_OUTPUT_TOKENS` | Maximum number of tokens the model may generate. | `18000` |
| `MAX_TOOL_CALLS` | Maximum number of tool calls the OpenAI Responses API may issue in a response loop. A value of `0` leaves the limit unset. | `0` |
| `WebSearchTool.type` | Enables provider-specific web search when present. The value `default` maps to the provider default web-search tool type where supported. | Not set |
| `WebSearchTool.city` | Optional city hint for web-search user location. | Not set |
| `WebSearchTool.country` | Optional country hint for web-search user location. | Not set |
| `WebSearchTool.region` | Optional region hint for web-search user location. | Not set |
| `MCP.url` | URL for the first MCP server tool. Additional servers can be configured with numbered groups such as `MCP_1.url`, `MCP_2.url`, and so on. | Not set |
| `MCP.name` | Provider-visible MCP server name (passed to OpenAI as its server label). Additional servers can use `MCP_1.name`, `MCP_2.name`, and so on. A name is required for a configured group to be registered. | Not set |
| `MCP.authorization` | Optional authorization value for the MCP server. Additional servers can use numbered variants. | Not set |
| `MCP.description` | Optional MCP server description. Additional servers can use numbered variants. | Not set |
| `GENAI_USERNAME` | Generic username used by provider authentication flows. CodeMie can use it for password-grant authentication. | Provider-specific |
| `GENAI_PASSWORD` | Generic password or secret used by provider authentication flows. CodeMie can use it as a password or client secret. | Provider-specific |
| `AUTH_URL` | Optional CodeMie OpenID Connect token endpoint override. | CodeMie default endpoint |

## Resources

- [Machai GenAI Client site](https://machai.machanism.org/genai-client/index.html)
- [Machanism platform](https://machanism.org/)
- [Machai project documentation](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai.git)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
- [API documentation](https://machai.machanism.org/genai-client/apidocs/)
