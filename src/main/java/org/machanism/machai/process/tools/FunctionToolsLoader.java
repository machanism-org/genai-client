package org.machanism.machai.process.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.machanism.machai.process.provider.ProcessProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers and applies {@link FunctionTools} implementations using Java's
 * {@link ServiceLoader} mechanism.
 * <p>
 * This class acts as the entry point for registering a curated set of local
 * capabilities (such as file access, command execution, and HTTP retrieval)
 * with a {@link ProcessProvider} provider. Implementations are discovered from the
 * classpath (typically via {@code META-INF/services} provider configuration)
 * and then applied to the provider in discovery order.
 * </p>
 *
 * <h2>Usage Example</h2>
 * 
 * <pre>
 * ProcessProvider provider = ...;
 * Class&lt;?&gt; appClass = ...;
 * FunctionToolsLoader loader = new FunctionToolsLoader();
 * loader.applyTools(provider, appClass);
 * </pre>
 *
 * <p>
 * The loader maintains a list of discovered {@link FunctionTools} instances and
 * applies them to the provider, filtered by application class compatibility.
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class FunctionToolsLoader {

	/** Logger for debug output during tool discovery and application. */
	private static final Logger logger = LoggerFactory.getLogger(FunctionToolsLoader.class);

	/**
	 * List of discovered {@link FunctionTools} instances, loaded from the
	 * classpath.
	 */
	private final List<FunctionTools> functionTools = new ArrayList<>();

	/**
	 * Constructs a new {@code FunctionToolsLoader} and discovers available
	 * {@link FunctionTools} implementations using {@link ServiceLoader}.
	 * <p>
	 * Each discovered tool is added to the internal list and logged for debugging.
	 * </p>
	 */
	public FunctionToolsLoader() {
		ServiceLoader<FunctionTools> functionToolServiceLoader = ServiceLoader.load(FunctionTools.class);
		for (FunctionTools functionTool : functionToolServiceLoader) {
			functionTools.add(functionTool);
			logger.debug("Detected FunctionTools: {}", functionTool.getClass().getName());
		}
		// Accept the descriptor name used by older releases.  This also keeps
		// applications that still package the legacy service descriptor working.
		loadLegacyServiceDescriptor();
	}

	private void loadLegacyServiceDescriptor() {
		try {
			ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
			java.util.Enumeration<URL> resources = contextLoader.getResources(
					"META-INF/services/org.machanism.machai.ai.tools.FunctionTools");
			while (resources.hasMoreElements()) {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(resources.nextElement().openStream(), "UTF-8"))) {
					String line;
					while ((line = reader.readLine()) != null) {
						line = line.trim();
						if (!line.isEmpty() && !line.startsWith("#")) {
							ClassLoader loader = Thread.currentThread().getContextClassLoader();
							java.lang.reflect.Constructor<?> constructor = Class.forName(line, true, loader).getDeclaredConstructor();
							constructor.setAccessible(true);
							Object instance = constructor.newInstance();
							if (instance instanceof FunctionTools) {
								functionTools.add((FunctionTools) instance);
							}
						}
					}
				}
			}
		} catch (IOException | ReflectiveOperationException | LinkageError e) {
			logger.debug("Unable to load legacy FunctionTools service descriptor", e);
		}
	}

	/**
	 * Applies all discovered {@link FunctionTools} installers to the given
	 * provider, filtered by application class compatibility.
	 * <p>
	 * For each compatible tool, both tool functions and prompts are registered with
	 * the provider.
	 * </p>
	 *
	 * @param provider the {@link ProcessProvider} provider instance to augment with tools,
	 *                 prompts, and resources
	 * @param tools    names of tools that the provider should register;
	 *                 provider-specific filtering is applied by
	 *                 {@link ProcessProvider#addTools(FunctionTools, String[])}
	 * @param appClass the application class requesting tool assignment; only tools
	 *                 compatible with this class are applied
	 */
	public void applyTools(ProcessProvider provider, String[] tools, Class<?> appClass) {
		for (FunctionTools functionTool : functionTools) {
			Class<? extends FunctionTools> functionToolsClass = functionTool.getClass();
			boolean supported = isSupportedFor(appClass, functionToolsClass);

			if (supported) {
				logger.debug("Register FunctionTools: {}", functionTool.getClass().getName());

				provider.addTools(functionTool, tools);
				provider.addPrompts(functionTool);
				provider.addResources(functionTool);
			}
		}
	}

	/**
	 * Checks whether the given {@link FunctionTools} implementation supports
	 * assignment to the specified application class.
	 * <p>
	 * If the {@link SupportedFor} annotation is present, only classes listed in its
	 * value are considered compatible. If the annotation is absent, compatibility
	 * is assumed.
	 * </p>
	 *
	 * @param appClass           the application class requesting tool assignment
	 * @param functionToolsClass the FunctionTools implementation class
	 * @return {@code true} if the tool is compatible with the application class,
	 *         {@code false} otherwise
	 */
	private boolean isSupportedFor(Class<?> appClass, Class<? extends FunctionTools> functionToolsClass) {
		SupportedFor supportedApplications = functionToolsClass.getAnnotation(SupportedFor.class);
		boolean result = false;
		if (supportedApplications != null) {
			if (supportedApplications.value().length > 0) {
				for (Class<?> supportedClass : supportedApplications.value()) {
					if (supportedClass.isAssignableFrom(appClass)) {
						result = true;
					}
				}
			} else {
				result = true;
			}

			if (result) {
				for (Class<?> excludedClass : supportedApplications.excludes()) {
					if (excludedClass.isAssignableFrom(appClass)) {
						return result = false;
					}
				}
			}

			return result;
		}
		return true;
	}
}
