package org.machanism.machai.ai.provider;

import java.io.File;
import java.util.List;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.tools.FunctionTools;
import org.machanism.machai.ai.tools.Prompt;
import org.machanism.machai.ai.tools.Resource;

/**
 * Contract for a generative-AI provider integration.
 * <p>
 * A {@code Genai} represents a concrete implementation (for example OpenAI,
 * Gemini, a local model, etc.) capable of:
 * </p>
 * <ul>
 * <li>Collecting prompts and system instructions for a conversation,</li>
 * <li>Attaching local or remote files for provider-side processing,</li>
 * <li>Registering tool functions that may be invoked during a run.</li>
 * </ul>
 *
 * <p>
 * Implementations may keep session state between calls. Use {@link #clear()} to
 * reset conversation state.
 * </p>
 *
 * <h2>Typical usage</h2>
 * 
 * <pre>
 * Configurator conf = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Hello!");
 * String response = provider.perform();
 *
 * provider.clear();
 * </pre>
 *
 * @author Viktor Tovstyi
 */
public interface Genai {

	/**
	 * Initializes the provider with application configuration.
	 *
	 * @param model the model identifier or name to use
	 * @param conf  configuration source used to initialize the provider
	 * @throws IllegalArgumentException if the model or configuration is invalid
	 */
	void init(String model, Configurator conf);

	/**
	 * Adds a user prompt to the current session.
	 * <p>
	 * Prompts are accumulated until {@link #perform()} or {@link #clear()} is
	 * called. The exact message format sent to the provider is implementation
	 * specific.
	 * </p>
	 *
	 * @param text the prompt text
	 */
	void prompt(String text);

	/**
	 * Clears any stored files and session/provider state.
	 * <p>
	 * This resets the conversation and any accumulated context.
	 * </p>
	 */
	void clear();

	/**
	 * Sets system/session instructions for the current conversation.
	 * <p>
	 * Calling this method replaces any previously configured instructions. A
	 * {@code null} value may be used to clear them when supported by the
	 * implementation.
	 * </p>
	 *
	 * @param instructions instruction text
	 */
	void instructions(String instructions);

	/**
	 * Executes the provider to produce a response based on the accumulated prompts,
	 * instructions, files, and registered capabilities.
	 * <p>
	 * Whether the accumulated session is retained after execution is provider
	 * specific. Call {@link #clear()} when a new independent conversation is
	 * required.
	 * </p>
	 *
	 * @return the provider response as a string
	 */
	String perform();

	/**
	 * Registers the tools exposed by the given {@link FunctionTools} implementation,
	 * optionally restricting registration to a filtered subset of tools.
	 * <p>
	 * Implementations are expected to discover tool definitions on the provided {@code tools}
	 * instance (typically via annotated methods) and make them available for use, applying the
	 * enablement filter described below.
	 *
	 * @param tools        the {@link FunctionTools} implementation containing the tool definitions
	 *                     to register; must not be {@code null}.
	 * @param enabledTools an optional array of regular expression patterns controlling which tools
	 *                     are registered.
	 *                     <p>
	 *                     Each tool is identified internally by a fully qualified name built in the
	 *                     format {@code <ClassName>:<toolName>}, where:
	 *                     <ul>
	 *                         <li>{@code <ClassName>} is the fully qualified class name of the
	 *                             {@code tools} implementation, and</li>
	 *                         <li>{@code <toolName>} is the tool's declared name (or the method name
	 *                             if no explicit name is provided).</li>
	 *                     </ul>
	 *                     A tool is registered only if its fully qualified name matches at least one
	 *                     of the given patterns. If {@code enabledTools} is {@code null}, all
	 *                     available tools are registered without filtering.
	 */
	void addTools(FunctionTools tools, String[] enabledTools);

	/**
	 * Returns the names of tools currently registered with the provider.
	 * <p>
	 * The returned names may be used for diagnostics, filtering, request
	 * construction, or tool-invocation processing. The ordering and mutability of
	 * the returned list are provider-specific.
	 * </p>
	 *
	 * @return the names of registered tools, or an empty list when no tools are
	 *         registered
	 */
	List<String> getToolNames();
	
	/**
	 * Scans the provided {@link FunctionTools} instance for methods annotated with
	 * {@link Prompt} and registers each prompt for use during a run.
	 * <p>
	 * The prompt name, description, role, and parameters are obtained from the
	 * annotation and the method signature.
	 * </p>
	 *
	 * @param tools the {@link FunctionTools} instance whose methods will be scanned
	 *              for {@link Prompt} annotations
	 */
	void addPrompts(FunctionTools tools);

	/**
	 * Scans the provided {@link FunctionTools} instance for methods annotated with
	 * {@link Resource}, and registers each resource for use during a run.
	 * <p>
	 * This method inspects the given class instance to register resource utilities
	 * that can be dynamically called by the AI model during generation processes.
	 * </p>
	 *
	 * @param tools the {@link FunctionTools} instance whose methods will be scanned
	 *              for resource annotations
	 */
	void addResources(FunctionTools tools);

	/**
	 * Sets the working directory for the provider, which may be supplied to tool,
	 * prompt, and resource handlers.
	 *
	 * @param projectDir the project directory, or {@code null} to clear the current
	 *         directory
	 */
	void setProjectDir(File projectDir);

	/**
	 * Configures whether tool invocation errors should be returned to the model for
	 * conversational recovery or propagated as exceptions. This setting does not
	 * suppress provider or configuration errors unrelated to tool invocation.
	 *
	 * @param errorHandling {@code true} to return tool errors as response text;
	 *                      {@code false} to propagate them immediately
	 */
	void setErrorHandling(boolean errorHandling);

}
