package org.machanism.machai.process.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.process.provider.EmbeddingProvider;
import org.machanism.machai.process.provider.ProcessProvider;

/**
 * Utility class for dynamically loading and initializing generative AI
 * providers and embedding providers.
 *
 * <p>
 * The {@code ProcessProviderManager} offers static methods to instantiate
 * {@link ProcessProvider} and {@link EmbeddingProvider} implementations based
 * on a provider/model string, using Java reflection. Chat providers must use
 * the conventional provider identifier, while embedding providers may
 * alternatively use a fully qualified implementation class name.
 * </p>
 *
 * <h2>Provider Naming Convention</h2>
 * <ul>
 * <li>Provider and model are specified as {@code Provider:Model} (e.g.,
 * {@code OpenAI:gpt-4}).</li>
 * <li>Chat-provider identifiers must be valid Java identifiers.
 * Embedding-provider identifiers containing a dot ({@code .}) are treated as
 * fully qualified class names.</li>
 * <li>Other provider identifiers are resolved using the pattern
 * {@code org.machanism.machai.genai.provider.impl.{provider}Provider}.</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * Configurator conf = ...;
 * ProcessProvider provider = ProcessProviderManager.getProvider("OpenAI:gpt-4", conf);
 * EmbeddingProvider embeddingProvider = ProcessProviderManager.getEmbeddingProvider("OpenAI:embedding-model", conf);
 * }</pre>
 *
 * <p>
 * If the provider cannot be found or instantiated, an
 * {@link IllegalArgumentException} is thrown.
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class ProcessProviderManager {

	/**
	 * Format used to derive the conventional fully qualified provider class name
	 * from a provider identifier.
	 */
	private static final String[] PROCESS_PROVIDER_CLASS_NAME_PATTERNS = {
			"org.machanism.machai.genai.provider.%sProvider", "org.machanism.machai.process.provider.%sProvider" };

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private ProcessProviderManager() {
		// Utility class.
	}

	/**
	 * Dynamically loads and initializes a {@link ProcessProvider} provider based on
	 * the specified provider/model string.
	 *
	 * <p>
	 * The provider name and model are parsed from the input string (format:
	 * {@code Provider:Model}). The provider class is resolved using a conventional
	 * naming pattern. The provider is instantiated and initialized with the
	 * specified model and configuration.
	 * </p>
	 *
	 * @param chatModel the provider/model string (e.g., {@code OpenAI:gpt-4})
	 * @param conf      the configuration object for provider initialization
	 * @return the initialized {@link ProcessProvider} provider instance, or
	 *         {@code null} if the provider name is blank
	 * @throws IllegalArgumentException if the provider name is invalid, or if the
	 *                                  provider cannot be found or instantiated
	 */
	public static ProcessProvider getProvider(String chatModel, Configurator conf) {
		String providerName = StringUtils.substringBefore(chatModel, ":");
		String chatModelName = StringUtils.substringAfter(chatModel, ":");

		if (StringUtils.isBlank(providerName)) {
			return null;
		}

		boolean isValid = providerName.matches("^[A-Za-z_$][A-Za-z\\d_$]*$");
		if (isValid) {
			try {
				Class<? extends ProcessProvider> providerClass = resolveClass(providerName);
				Constructor<? extends ProcessProvider> constructor = providerClass.getConstructor();
				ProcessProvider provider = constructor.newInstance();
				provider.init(chatModelName, conf);
				return provider;
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException
					| ClassNotFoundException
					| NoSuchMethodException | SecurityException e) {
				throw new IllegalArgumentException("Failed to initialize GenAI provider '" + providerName
						+ "': provider is not supported or an error occurred during initialization.", e);
			} catch (IllegalArgumentException e) {
				throw e;
			}
		} else {
			throw new IllegalArgumentException(
					"Invalid provider name: `" + providerName
							+ "`. Expected format is `Provider:Model` (e.g., `OpenAI:gpt-4`). Please specify both provider and model separated by a colon.");
		}
	}

	/**
	 * Dynamically loads and initializes an {@link EmbeddingProvider} based on the
	 * specified provider/model string.
	 *
	 * <p>
	 * The provider name and model are parsed from the input string (format:
	 * {@code Provider:Model}). The provider class is resolved using a conventional
	 * naming pattern or, when the provider segment contains a dot, as a fully
	 * qualified class name. The provider is instantiated and initialized with the
	 * specified model and configuration.
	 * </p>
	 *
	 * @param embeddingModel the provider/model string (e.g.,
	 *                       {@code OpenAI:embedding-model})
	 * @param conf           the configuration object for provider initialization
	 * @return the initialized {@link EmbeddingProvider} instance, or {@code null}
	 *         if the provider name is blank
	 * @throws IllegalArgumentException if the provider cannot be found, does not
	 *                                  implement {@link EmbeddingProvider}, or
	 *                                  cannot be instantiated
	 */
	public static EmbeddingProvider getEmbeddingProvider(String embeddingModel, Configurator conf) {
		String providerName = StringUtils.substringBefore(embeddingModel, ":");
		String model = StringUtils.substringAfter(embeddingModel, ":");

		if (StringUtils.isBlank(providerName)) {
			return null;
		}

		try {
			Class<?> forName = resolveClass(providerName);
			if (EmbeddingProvider.class.isAssignableFrom(forName)) {
				@SuppressWarnings("unchecked")
				Class<? extends EmbeddingProvider> providerClass = (Class<? extends EmbeddingProvider>) forName;
				Constructor<? extends EmbeddingProvider> constructor = providerClass.getConstructor();
				EmbeddingProvider provider = constructor.newInstance();
				provider.init(model, conf);
				return provider;
			}
			throw new IllegalArgumentException(
					"Class `" + forName.getName() + "` does not implement EmbeddingProvider. "
							+ "Please ensure the class is a valid provider implementation.");

		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException
				| NoSuchMethodException | SecurityException e) {
			if (e instanceof InvocationTargetException
					&& ((InvocationTargetException) e).getCause() instanceof RuntimeException) {
				throw (RuntimeException) ((InvocationTargetException) e).getCause();
			}
			throw new IllegalArgumentException("Failed to initialize EmbeddingProvider provider '" + providerName
					+ "': provider is not supported or an error occurred during initialization.", e);
		} catch (IllegalArgumentException e) {
			throw e;
		}
	}

	/**
	 * Resolves the provider class name based on the provider name and a
	 * conventional naming pattern.
	 *
	 * <p>
	 * If the provider name contains a dot ({@code .}), it is treated as a fully
	 * qualified class name. Otherwise, the provider is resolved using the specified
	 * pattern. If the class is not loadable, a fallback naming convention is used.
	 * </p>
	 *
	 * @param providerName the provider identifier (for example, {@code OpenAI}) or
	 *                     fully qualified embedding-provider class name. A simple
	 *                     identifier resolves using
	 *                     {@code org.machanism.machai.genai.provider.impl.%sProvider}.
	 * @return the resolved class name
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private static Class<? extends ProcessProvider> resolveClass(String providerName) throws ClassNotFoundException {
		String className = resolveClassName(providerName);
		return (Class<? extends ProcessProvider>) Class.forName(className);
	}

	/**
	 * Resolves the textual class name for a provider. Fully qualified names are
	 * returned unchanged; simple names are checked against the supported
	 * conventions. The nested fallback is retained for callers that use this
	 * resolver to inspect a potential provider class without loading it.
	 *
	 * @param providerName provider identifier or fully qualified class name
	 * @return the resolved class name
	 */
	private static String resolveClassName(String providerName) {
		if (StringUtils.contains(providerName, '.')) {
			return providerName;
		}

		String resolvedClassName = null;
		for (String pattern : PROCESS_PROVIDER_CLASS_NAME_PATTERNS) {
			String className = String.format(pattern, providerName);
			try {
				Class.forName(className);
				// Keep checking so the established provider package takes precedence
				// when both conventions expose the same short name.
				resolvedClassName = className;
			} catch (ClassNotFoundException e) {
				// Try the next supported package convention.
			}
		}

		if (resolvedClassName != null) {
			return resolvedClassName;
		}

		return ProcessProviderManager.class.getName() + "$" + providerName + "Provider";
	}

}
