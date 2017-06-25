package seers.textanalyzer;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import seers.textanalyzer.QuoteProcessor.Quotes;

public class QuoteProcessorTest {

	@Test
	public void test() {
		String[] txts = {
				"if(getip(`document.referrer`)==\"www.'eg.'com\" || getip('document.referrer')==\"192.57.42.11\"",
				"App crashes with the following stack traces when \"Maximum Score\" is the character \"-\" or is a big number.",
				"This is a test", "\"This is a test\"", "\" not end quote" };

		for (String txt : txts) {
			
			QuoteProcessor processor = new QuoteProcessor();
			Quotes quotes = processor.processSentence(txt);
			
			assertNotNull(quotes);

			System.out.println(quotes);
			System.out.println();

		}
	}

}
