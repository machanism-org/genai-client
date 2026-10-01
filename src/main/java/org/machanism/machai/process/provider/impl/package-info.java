/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Concrete {@link org.machanism.machai.process.provider.ProcessProvider}
 * implementations for disabling processing or dispatching host-defined tools.
 *
 * <p>
 * This package contains providers for applications that need the common process
 * provider contract but do not necessarily need a remote AI service. A caller
 * can select {@link NoneProvider} for an intentional no-op implementation, or
 * {@link ToolsProvider} to execute registered Java functions from a structured
 * YAML request. Both providers expose the same lifecycle operations for
 * initialization, prompt submission, tool registration, execution, and state
 * management; their execution behavior is intentionally different.
 * </p>
 *
 * <h2>Provider implementations</h2>
 * <ul>
 * <li>
 * <p>
 * {@link NoneProvider} accepts lifecycle input and discards it. It retains no
 * prompts, instructions, tools, resources, project-directory information, or
 * error-handling configuration. Its {@link NoneProvider#perform()} method
 * always returns {@code null}, and {@link NoneProvider#getToolNames()} returns
 * an immutable empty list. Initializing it with the model {@code "log"}
 * enables INFO-level diagnostic messages for supported lifecycle calls; any
 * other model disables those messages.
 * </p>
 * </li>
 * <li>
 * <p>
 * {@link ToolsProvider} extends
 * {@link org.machanism.machai.process.provider.AbstractAIProvider}. It stores
 * submitted prompts and maintains registered tool functions in registration
 * order. When initialized with the model {@code "yaml"},
 * {@link ToolsProvider#perform()} parses the most recently submitted prompt as
 * a YAML mapping, obtains its required {@code tool} value and optional
 * {@code params} value, invokes the matching function, and returns a string
 * result. Non-string results are serialized as JSON. A missing tool name causes
 * an {@link IllegalArgumentException}; tool failures are propagated by default,
 * while the inherited error-handling setting can convert a failure message into
 * the returned result. A YAML-mode call must have at least one prompt.
 * </p>
 * </li>
 * </ul>
 *
 * <h2>Architecture and lifecycle</h2>
 * <p>
 * {@code NoneProvider} implements the provider interface directly because it
 * has no backend or request state. {@code ToolsProvider} specializes the
 * abstract provider's reflective tool-registration and invocation facilities.
 * Host functions are normally exposed by methods annotated with
 * {@link org.machanism.machai.process.tools.Tool}; callers register their
 * containing {@link org.machanism.machai.process.tools.FunctionTools} through
 * {@link org.machanism.machai.process.provider.ProcessProvider#addTools(
 * org.machanism.machai.process.tools.FunctionTools, String[])}. The abstract
 * provider also supplies project-directory context, prompt and resource hooks,
 * and configurable handling for exceptions raised while invoking tools.
 * </p>
 *
 * <p>
 * Provider instances can retain request-specific data. For an independent
 * request, register the required tools and prompts, then call
 * {@link org.machanism.machai.process.provider.ProcessProvider#perform()}.
 * Call {@link org.machanism.machai.process.provider.ProcessProvider#clear()}
 * before starting the next request when the selected implementation supports
 * clearing its state. Configure error handling according to whether invocation
 * failures should be returned as text or propagated to the caller.
 * </p>
 *
 * <h2>Usage</h2>
 * <p>
 * The following example registers annotated host functions and asks
 * {@code ToolsProvider} to dispatch the final prompt. The YAML {@code tool}
 * value must match a registered function name.
 * </p>
 * <pre>
 * ToolsProvider provider = new ToolsProvider();
 * provider.init("yaml", configurator);
 * provider.addTools(functionTools, null);
 * provider.prompt("tool: summarize\nparams:\n  path: README.md");
 * String result = provider.perform();
 * provider.clear();
 * </pre>
 *
 * <p>
 * Use {@code NoneProvider} when processing must be deliberately disabled:
 * </p>
 * <pre>
 * ProcessProvider provider = new NoneProvider();
 * provider.init("log", configurator);
 * provider.prompt("This input is accepted and discarded.");
 * String result = provider.perform(); // always null
 * </pre>
 *
 * @see org.machanism.machai.process.provider.ProcessProvider
 * @see org.machanism.machai.process.provider.AbstractAIProvider
 * @see NoneProvider
 * @see ToolsProvider
 * @since 1.2.0
 */
package org.machanism.machai.process.provider.impl;
