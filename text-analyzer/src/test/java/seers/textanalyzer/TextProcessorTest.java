package seers.textanalyzer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import seers.textanalyzer.entity.Sentence;
import seers.textanalyzer.entity.Token;

public class TextProcessorTest {

	@Test
	public void testProcessTextStringBoolean() {
		String text = "instead, the CWD command can be used to test if the entity is directory or not.               Reducing the number of network transactions required for a directory listing.";
		List<Sentence> sentences = TextProcessor.processText(text, false);

		assertEquals(2, sentences.size());

		assertEquals("instead, the CWD command can be used to test if the entity is directory or not.",
				sentences.get(0).getText());
		assertEquals("Reducing the number of network transactions required for a directory listing.",
				sentences.get(1).getText());
	}

	// @Test
	public void testProcessTextFullPipelineAndQuotes() {
		String text = "App crashes with the following stack traces when \"Maximum Score\" is the character \"-\" or is a big number.";

		List<Sentence> sentences = TextProcessor.processTextFullPipelineAndQuotes(text, true);
		assertEquals(1, sentences.size());
		Sentence sentence = sentences.get(0);
		assertEquals(2, sentence.getQuotes().size());

		System.out.println(sentence.getText());
		System.out.println(sentence.getTokens());
		System.out.println(sentence.getQuotes());
	}

	@Test
	public void testPreprocessText() {
		String text = "App crashes with the following stack traces when \"Maximum Score\" is the character \"-\" or is a big number.";

		List<Sentence> sentences = TextProcessor.preprocessText(text, null);

		List<Token> tokens = new ArrayList<>();
		for (Sentence sentence : sentences) {
			tokens.addAll(sentence.getTokens());
		}

		String tokensString = tokens.stream().map(Token::getLemma).collect(Collectors.joining(", "));

		System.out.println(tokensString);

		assertEquals(14, tokens.size());

	}

	@Test
	public void testPreprocessText2() {
		String text = "App crashes with the following stack traces when \"Maximum Score\" is the character \"-\" or is a big number.";

		String options = "-" + PreprocessingOptionsParser.SHORT_TOKENS_REMOVAL + " 3";
		List<Sentence> sentences = TextProcessor.preprocessText(text, null, options);

		List<Token> tokens = new ArrayList<>();
		for (Sentence sentence : sentences) {
			tokens.addAll(sentence.getTokens());
		}

		String tokensString = tokens.stream().map(Token::getLemma).collect(Collectors.joining(", "));

		System.out.println(text);
		System.out.println(tokensString);

		assertEquals(10, tokens.size());

	}

	@Test
	public void testPreprocessText3() {
		String text = "camelCase This is a number: 23234.2342 4544 456454,121";

		String options = "-" + PreprocessingOptionsParser.NUMBERS_REMOVAL;
		List<Sentence> sentences = TextProcessor.preprocessText(text, null, options);

		List<Token> tokens = new ArrayList<>();
		for (Sentence sentence : sentences) {
			tokens.addAll(sentence.getTokens());
		}

		String tokensString = tokens.stream().map(Token::getLemma).collect(Collectors.joining(" - "));

		System.out.println(text);
		System.out.println(tokensString);

		assertEquals(7, tokens.size());

	}

	@Test
	public void testPreprocessText4() {
		String text = "process_text camelCase NewCamelCase This is a number: 23234.2342 4544 456454,121";

		String options = "-" + PreprocessingOptionsParser.NUMBERS_REMOVAL + " -"
				+ PreprocessingOptionsParser.CAMEL_CASE_SPLITTING + " -"
				+ PreprocessingOptionsParser.SPECIAL_CHARS_REMOVAL;
		List<Sentence> sentences = TextProcessor.preprocessText(text, null, options);

		List<Token> tokens = new ArrayList<>();
		for (Sentence sentence : sentences) {
			tokens.addAll(sentence.getTokens());
		}

		String tokensString = tokens.stream().map(Token::getLemma).collect(Collectors.joining(" - "));

		System.out.println(text);
		System.out.println(tokensString);

		assertEquals(11, tokens.size());

	}

	@Test
	public void testPreprocessText5() {
		String text = "When I create an entry for a purchase, the autocomplete list shows up";

		List<Sentence> sentences = TextProcessor.processTextFullPipeline(text, false);

		System.out.println(sentences.get(0).getDependencies());

		// --------------------

		text = "the autocomplete list shows up, when I create an entry for a purchase";

		sentences = TextProcessor.processTextFullPipeline(text, false);

		System.out.println(sentences.get(0).getDependencies());

	}
}
