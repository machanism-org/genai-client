/* @guidance: >>> ${guidances}/package-info-javadoc.md */

/**
 * Concrete and host-side provider implementations for the Machai process API.
 *
 * <p>
 * This package contains provider implementations that can be selected by an
 * application when it needs either a no-op processing mode or deterministic
 * invocation of functions implemented by the host application. The classes
 * implement {@link org.machanism.machai.process.provider.ProcessProvider}, while
 * {@link ToolsProvider} additionally inherits common tool-discovery and
 * invocation behavior from
 * {@link org.machanism.machai.process.provider.AbstractAIProvider}.
 * </p>
 *
 * <h2>Implementations</h2>
 * <ul>
 * <li>
 * {@link NoneProvider} is a deliberately inactive provider. It accepts the
 * common lifecycle calls, discards their input, returns an empty tool-name list,
 * and always returns {@code null} from {@link NoneProvider#perform()}. Selecting
 * the {@code log} model enables informational lifecycle logging, which is useful
 * for disabled-provider defaults and tests.
 * </li>
 * <li>
 * {@link ToolsProvider} is a deterministic function-tool provider. It registers
 * annotated functions through the inherited
 * {@link org.machanism.machai.process.provider.AbstractAIProvider#addTools(
 * org.machanism.machai.process.tools.FunctionTools, String[])} mechanism and,
 * when initialized with the {@code yaml} model, interprets its last prompt as a
 * YAML object containing a {@code tool} name and optional {@code params} object.
 * It invokes the selected function and serializes non-string results as JSON.
 * </li>
 * </ul>
 *
 * <h2>Relationships and lifecycle</h2>
 * <p>
 * {@code NoneProvider} implements the contract directly because it has no
 * request state or backend. {@code ToolsProvider} uses the abstract provider
 * infrastructure for reflective discovery of methods annotated with
 * {@link org.machanism.machai.process.tools.Tool}, prompt and resource
 * registration hooks, project-directory context, and configurable tool-error
 * handling. Tool registration is performed through
 * {@link org.machanism.machai.process.provider.ProcessProvider#addTools(
 * org.machanism.machai.process.tools.FunctionTools, String[])}, and the
 * resulting names are available from
 * {@link org.machanism.machai.process.provider.ProcessProvider#getToolNames()}.
 * </p>
 *
 * <h2>Usage</h2>
 * <p>
 * A deterministic tool invocation can be performed as follows. The configured
 * tool object must expose a method annotated with {@code @Tool}; the prompt is
 * YAML whose {@code tool} value matches the registered tool name.
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
 * Use {@code NoneProvider} when processing should be intentionally disabled:
 * </p>
 * <pre>
 * ProcessProvider provider = new NoneProvider();
 * provider.init("log", configurator);
 * provider.prompt("This input is accepted and discarded.");
 * String result = provider.perform(); // always null
 * </pre>
 *
 * <p>
 * Concrete providers retain only the state defined by their implementation.
 * Call {@link org.machanism.machai.process.provider.ProcessProvider#clear()}
 * before starting an independent request, and configure error handling according
 * to whether tool failures should be returned to the caller or propagated.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.process.provider.impl;
