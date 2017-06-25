package seers.textanalyzer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import seers.textanalyzer.entity.Sentence;

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
	
	@Test
	public void testProcessTextFullPipelineAndQuotes(){
		String text = "App crashes with the following stack traces when \"Maximum Score\" is the character \"-\" or is a big number.";
		
		List<Sentence> sentences = TextProcessor.processTextFullPipelineAndQuotes(text, true);
		assertEquals(1, sentences.size());
		Sentence sentence = sentences.get(0);
		assertEquals(2, sentence.getQuotes().size());
		
		System.out.println(sentence.getText());
		System.out.println(sentence.getTokens());
		System.out.println(sentence.getQuotes());
	}
	

}
