package seers.textanalyzer;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import seers.textanalyzer.QuoteProcessor.Quotes;

public class QuoteProcessorTest {

	@Test
	public void test() {
		String[] txts = {
				"Now again selected \" Export view to csv\"",
				"You'll be presented with a chance to enter a ZIP and mileage radius: enter 87110 and leave the default radius at '50'.",
//				"if(getip(`document.referrer`)==\"www.'eg.'com\" || getip('document.referrer')==\"192.57.42.11\"",
				"App crashes with the following stack traces when \"Maximum Score\" is the character \"-\" or is a big number.",
				"This is a test", "\"This is a test\"", 
				"\" not end quote"

				};

		for (String txt : txts) {
			System.out.println(txt);
			
			QuoteProcessor processor = new QuoteProcessor();
			Quotes quotes = processor.processSentence(txt);
			
			assertNotNull(quotes);

			System.out.println(quotes);
			System.out.println();

		}
	}

}
