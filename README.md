<!-- @guidance: >>> ${guidances}/readme-content.md -->

# GenAI Client

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/genai-client.svg)](https://central.sonatype.com/artifact/org.machanism.machai/genai-client) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/genai-client/refs/heads/main/bindex.json)

## Cloning and Getting Started

To clone and set up this project locally, follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/machanism-org/genai-client.git
   cd genai-client
   ```
2. **Build the project using Maven:**
   ```bash
   mvn clean install
   ```

## Overview

GenAI Client is a Java library designed for integrating Machai applications with generative AI providers through a consistent provider abstraction. It provides provider resolution, prompt and instruction handling, runtime configuration, optional embedding generation, usage tracking, and Java function-tool, prompt, and resource registration for AI-powered workflows.

The library is organized around the common `ProcessProvider` lifecycle contract and shared provider infrastructure. Applications can resolve a configured provider from identifiers such as `OpenAI:gpt-4o-mini`, attach prompts, system instructions, tools, resources, web-search support, or MCP servers, and execute requests without depending directly on vendor-specific SDK details. Token usage is captured in immutable usage records and can be aggregated per model for reporting and diagnostics.

`ProcessProviderManager` resolves `Provider:Model` identifiers by reflection, initializes public no-argument provider implementations with a `Configurator`, and separately resolves embedding providers. `Usage` records input, cached-input, and output token counts, while `UsageStatistics` keeps in-memory records keyed by the exact model identifier and exposes model-specific or aggregate reporting. Provider instances retain their conversation and registration state until `clear()` is called and should not be shared concurrently without external synchronization.

GenAI Client also includes a lightweight tool metadata layer. Java methods annotated as tools, prompts, or resources can be discovered through `ServiceLoader`, described with parameter metadata, filtered for supported application classes, and registered with providers as AI-callable functions or resource callbacks. This enables semantic search, automated content generation, intelligent project assembly, structured tool execution, local tool orchestration, and provider-independent prompt workflows.

The `org.machanism.machai.process.tools` package provides provider-neutral callback and failure contracts. `ToolFunction` supports programmatic JSON-based invocation, `ParamDescriptor` represents programmatic parameter metadata, and `ErrorResultException`, `ToolExecutionException`, and `SpecialException` distinguish structured results, checked tool failures, and intentional task completion.

## Project Structure

A host application requests a configured `Provider:Model` implementation from `ProcessProviderManager`, then uses the common `ProcessProvider` contract to submit prompts, register local capabilities, execute requests, and, where supported, generate embeddings. Shared provider infrastructure handles configuration, local callback discovery and invocation, web-search and MCP setup, argument conversion, and error handling.

Concrete provider components integrate OpenAI-compatible Responses and Embeddings APIs, Anthropic Messages APIs, and CodeMie delegation. The CodeMie component obtains an OAuth 2.0 access token and routes supported model families to the appropriate compatible provider. Local YAML-described callbacks and a safe no-op provider support host-side execution and disabled environments without an external model request.

The principal library areas are:

- `org.machanism.machai.process.manager` resolves configured provider identifiers, initializes provider instances, and records token usage by model.
- `org.machanism.machai.process.provider` defines the common provider contracts and reusable base behavior for conversational, embedding, tool-enabled, web-search-enabled, and MCP-enabled AI integrations.
- `org.machanism.machai.genai.provider` contains concrete provider implementations for OpenAI-compatible APIs, Anthropic Claude, and CodeMie; the same provider area contains the direct host-side Tools and None providers.
- `org.machanism.machai.process.tools` defines tool, prompt, resource, parameter, role, and supported-application metadata, plus service loading and callback contracts used to expose Java methods as AI-accessible capabilities.

## Introduction

Applications follow the same lifecycle regardless of provider: resolve a configured provider with `ProcessProviderManager`, attach prompts or system instructions, register tools and resources as needed, then execute the request. Java methods exposed through function-tool metadata can be discovered with `ServiceLoader` and made available as AI-callable functions or resource callbacks.

This supports semantic search, automated content generation, intelligent project assembly, structured tool execution, local tool orchestration, and provider-independent prompt workflows. Token usage is captured in immutable records and can be aggregated by model for reporting and diagnostics.

### Supported providers

#### OpenAI

The OpenAI provider adapts the Machai `ProcessProvider` and `EmbeddingProvider` APIs to the OpenAI Java SDK Responses and Embeddings APIs. It supports conversational text generation, iterative function-tool calling, optional web search, MCP server tools, embeddings, request input logging, and token-usage capture.

Configure it with `OpenAI:gpt-4o-mini` or `OpenAI:text-embedding-3-small`, `OPENAI_API_KEY`, and optionally `OPENAI_BASE_URL`, `GENAI_TIMEOUT`, `MAX_OUTPUT_TOKENS`, `MAX_TOOL_CALLS`, `WebSearchTool.*`, and `MCP*` settings. It can also be used with OpenAI-compatible endpoints by overriding the base URL.

#### Anthropic

The Anthropic provider adapts the Machai `ProcessProvider` API to Claude models through the Anthropic Java SDK Beta Messages API. It supports system instructions, local function-tool loops, web search, MCP definitions, prompt-cache control on the final registered tool, and token-usage capture.

Configure it with `Anthropic:claude-3-5-sonnet`, `ANTHROPIC_API_KEY`, and optionally `ANTHROPIC_BASE_URL`, `GENAI_TIMEOUT`, `MAX_OUTPUT_TOKENS`, `WebSearchTool.*`, and `MCP*` settings. `MAX_TOOL_CALLS` applies to OpenAI Responses API loops and does not limit Anthropic tool-use loops. A key beginning with `sk-` is treated as an API key; other non-null credentials are authorization tokens.

#### CodeMie

The CodeMie provider integrates with EPAM CodeMie Code Assistant endpoints. It authenticates with a CodeMie OpenID Connect token endpoint, obtains OAuth 2.0 bearer tokens, configures the delegated provider with the CodeMie API base URL, and routes supported model families to the appropriate implementation.

Models beginning with `gpt-`, `gemini-`, `text-embedding-`, `codemie-text-embedding-`, or `amazon.titan-embed-text-` use the OpenAI-compatible implementation; models beginning with `claude-` use the Anthropic implementation. Configure it with `CodeMie:gpt-4o-mini` or `CodeMie:claude-3-5-sonnet`, `GENAI_USERNAME`, and `GENAI_PASSWORD`. A username containing `@` selects password-grant authentication; another username selects client-credentials authentication. `AUTH_URL` optionally overrides the token endpoint.

#### Tools

The Tools provider executes a registered host-side `ToolFunction` callback from a YAML tool-call description containing a tool name and parameter payload. Use `Tools:yaml`, register local callbacks before `perform()`, and supply the final prompt as the YAML descriptor. It requires no external credentials or endpoint configuration.

#### None

The None provider is a disabled no-op implementation for safe defaults and tests. It accepts the standard lifecycle calls, discards submitted state, and returns `null` from execution. Use `None:log` to emit lifecycle activity at INFO level, or another model value such as `None:disabled` for silent operation.

## Usage

Configure a provider with a `Provider:Model` identifier and the credentials appropriate to that provider. For example:

```java
ProcessProvider provider = ProcessProviderManager.getProvider("OpenAI:gpt-4o-mini", configurator);
// Add prompts, instructions, tools, or resources as required, then execute the request.
```

Register compatible Java tools, prompts, and resources when the request requires local capabilities; providers expose them to the model as supported functions or resource callbacks. The Tools provider instead accepts a YAML descriptor containing a `tool` name and optional `params`.

### Common configuration

| Parameter | Description | Default |
| --- | --- | --- |
| Provider/model identifier | Provider and model in `Provider:Model` form, for example `OpenAI:gpt-4o-mini`. | Required |
| `OPENAI_API_KEY` | API key for OpenAI or OpenAI-compatible requests; CodeMie supplies a bearer token for delegated requests. | Required for OpenAI-compatible providers |
| `OPENAI_BASE_URL` | Base-URL override for an OpenAI-compatible endpoint. | OpenAI SDK default |
| `ANTHROPIC_API_KEY` | API key or authorization token for Anthropic requests. | Required for Anthropic |
| `ANTHROPIC_BASE_URL` | Base-URL override for an Anthropic-compatible endpoint. | Anthropic SDK default |
| `GENAI_TIMEOUT` | Request timeout in seconds; `0` or no value retains SDK behavior. | `0` |
| `MAX_OUTPUT_TOKENS` | Maximum generated tokens. | `18000` |
| `MAX_TOOL_CALLS` | Maximum OpenAI Responses API tool calls in a response loop; it does not limit Anthropic tool-use loops. | `0` (unset) |
| `WebSearchTool.type` | Enables provider-specific web search when present; `default` uses the provider's default supported type. | Not set |
| `WebSearchTool.city`, `WebSearchTool.country`, `WebSearchTool.region` | Optional user-location hints for web search. | Not set |
| `MCP.url` | URL for the first MCP server; numbered groups, such as `MCP_1.url`, configure additional servers. | Not set |
| `MCP.name` | Provider-visible MCP server name; a configured group requires a name to be registered. | Not set |
| `MCP.authorization` | Optional authorization value for an MCP server. | Not set |
| `MCP.description` | Optional MCP server description. | Not set |
| `GENAI_USERNAME` / `GENAI_PASSWORD` | CodeMie credentials for password-grant or client-credentials authentication. | Provider-specific |
| `AUTH_URL` | CodeMie token-endpoint override. | CodeMie default endpoint |

## Resources

- [Machai GenAI Client site](https://machai.machanism.org/genai-client/index.html)
- [Machanism platform](https://machanism.org/)
- [Machai project documentation](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai.git)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/genai-client)
- [API documentation](https://machai.machanism.org/genai-client/apidocs/)
