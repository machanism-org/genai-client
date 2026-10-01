package org.machanism.machai.ai.provider.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.machanism.machai.TestConfigurators;
import org.machanism.machai.genai.provider.AnthropicProvider;
import org.machanism.machai.genai.provider.CodeMieProvider;
import org.machanism.machai.genai.provider.OpenAIProvider;
import org.machanism.machai.process.tools.ParamDescriptor;

import com.anthropic.models.beta.messages.BetaMessageParam;
import com.anthropic.models.beta.messages.MessageCreateParams;

/** Focused coverage of provider request construction and local state. */
class ProviderCoreCoverageTest {

	@Test
	void openAiEmbeddingWithNullInputReturnsAnEmptyVectorWithoutCreatingAClient() {
		// Arrange
		OpenAIProvider provider = new OpenAIProvider();

		// Act
		java.util.List<Double> result = provider.embedding(null, 3);

		// Assert
		assertTrue(result.isEmpty());
	}

	@Test
	void anthropicBuildsRequestAndIgnoresBlankPrompts() throws Exception {
		ExposedAnthropic p = new ExposedAnthropic();
		p.init("claude-test", TestConfigurators.mapBacked());
		p.instructions("system");
		p.prompt(" ");
		p.prompt("question");
		p.register("lookup", new ParamDescriptor("q", "string", true, "query", null));
		p.register("lookup", new ParamDescriptor("ignored", "string", false, "duplicate", null));
		MessageCreateParams request = p.request();
		assertTrue(request.model().toString().contains("claude-test"));
		assertTrue(request.system().get().toString().contains("system"));
		assertEquals(1, request.messages().size());
		assertEquals(1, request.tools().get().size());
		assertEquals(java.util.Collections.singletonList("lookup"), p.getToolNames());
		p.clear();
		assertTrue(p.inputs().isEmpty());
	}

	@Test
	void codeMieRejectsUnsupportedModelAndUninitializedEmbedding() {
		CodeMieProvider p = new CodeMieProvider();
		assertThrows(IllegalArgumentException.class, () -> p.init("unsupported", TestConfigurators.mapBacked()));
		assertThrows(NullPointerException.class, () -> p.embedding(null, 3));
	}

	private static final class ExposedAnthropic extends AnthropicProvider {
		void register(String name, ParamDescriptor... d) {
			addTool(name, "tool", (p, c) -> "ok", d);
		}

		MessageCreateParams request() throws Exception {
			Method m = AnthropicProvider.class.getDeclaredMethod("createResponseBuilder", java.util.List.class);
			m.setAccessible(true);
			return (MessageCreateParams) m.invoke(this, inputs());
		}

		@SuppressWarnings("unchecked")
		java.util.List<BetaMessageParam> inputs() throws Exception {
			java.lang.reflect.Field f = AnthropicProvider.class.getDeclaredField("inputs");
			f.setAccessible(true);
			return (java.util.List<BetaMessageParam>) f.get(this);
		}
	}
}
