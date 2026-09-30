package org.machanism.machai.process.provider;

import java.io.File;
import java.util.List;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.process.tools.FunctionTools;

/**
 * Delegating {@link ProcessProvider} implementation.
 *
 * <p>
 * This adapter forwards all {@link ProcessProvider} calls to an underlying provider
 * instance configured via {@link #setProvider(ProcessProvider)}.
 * </p>
 *
 * <p>
 * Intended use cases include decorating providers (for example, adding
 * cross-cutting concerns like logging, metrics, retries, or request shaping)
 * while preserving the {@link ProcessProvider} contract.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> Instances are not thread-safe unless the
 * delegated provider is thread-safe and access is externally synchronized.
 * </p>
 */
public class ProcessProviderAdapter implements ProcessProvider {

	/**
	 * Delegate provider.
	 *
	 * <p>
	 * Subclasses typically set this value during construction or initialization
	 * using {@link #setProvider(ProcessProvider)}.
	 * </p>
	 */
	protected ProcessProvider provider;

	/**
	 * Creates an adapter without a delegate.
	 *
	 * <p>
	 * Call {@link #setProvider(ProcessProvider)} before invoking any other methods.
	 * </p>
	 */
	public ProcessProviderAdapter() {
		super();
	}

	/**
	 * Sets the delegate provider.
	 *
	 * @param provider the provider to delegate to
	 * @throws IllegalArgumentException if {@code provider} is {@code null}
	 */
	public void setProvider(ProcessProvider provider) {
		if (provider == null) {
			throw new IllegalArgumentException("provider must not be null");
		}
		this.provider = provider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(String model, Configurator conf) {
		provider.init(model, conf);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void prompt(String text) {
		provider.prompt(text);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		provider.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void instructions(String instructions) {
		provider.instructions(instructions);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String perform() {
		return provider.perform();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProjectDir(File projectDir) {
		provider.setProjectDir(projectDir);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addTools(FunctionTools tools, String[] enabledTools) {
		provider.addTools(tools, enabledTools);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPrompts(FunctionTools functionTool) {
		provider.addPrompts(functionTool);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addResources(FunctionTools tools) {
		provider.addPrompts(tools);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setErrorHandling(boolean errorHandling) {
		provider.setErrorHandling(errorHandling);
	}

	@Override
	public List<String> getToolNames() {
		return provider.getToolNames();
	}

}