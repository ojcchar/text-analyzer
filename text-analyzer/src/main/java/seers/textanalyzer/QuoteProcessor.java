package seers.textanalyzer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class QuoteProcessor {

	private final String QUOTE_PREFIX = "QUT_";
	private int quotesIdx;

	public QuoteProcessor() {
		quotesIdx = 1;
	}

	public Quotes processSentence(String txt) {
		return processSentence2(txt, false);
	}

	private Quotes processSentence2(String originalTxt, boolean checkSurroundingQuotes) {
		String[] quoteMarks = { "'", "\"", "`" };

		String processedText = originalTxt;
		HashMap<String, Quotes> quotesMap = new LinkedHashMap<>();

		for (String quoteMark : quoteMarks) {

			String txtToProcess = originalTxt;
			if (checkSurroundingQuotes && originalTxt.startsWith(quoteMark) && originalTxt.endsWith(quoteMark)) {
				txtToProcess = originalTxt.substring(1, originalTxt.length() - 1);
			}

			String[] valuesInQuotes = StringUtils.substringsBetween(txtToProcess, quoteMark, quoteMark);

			if (valuesInQuotes == null) {
				continue;
			}

			for (String value : valuesInQuotes) {
				String quoteKey = QUOTE_PREFIX + quotesIdx++;
				String quote = quoteMark + value + quoteMark;

				processedText = processedText.replace(quote, quoteKey);

				Quotes childrenQuotes = processSentence2(quote, true);
				quotesMap.put(quoteKey, childrenQuotes);

			}
		}

		Quotes quotes = new Quotes(originalTxt, processedText, quotesMap);

		return quotes;
	}

	public class Quotes {

		String originalTxt;
		String txt;
		HashMap<String, Quotes> quotesMap;

		public Quotes(String originalTxt, String txt, HashMap<String, Quotes> quotesMap) {
			super();
			this.originalTxt = originalTxt;
			this.txt = txt;
			this.quotesMap = quotesMap;
		}

		@Override
		public String toString() {
			return "Q [\n\torTxt=" + originalTxt + ",\n\ttxt=" + txt + ",\n\tqm={" + extracted(quotesMap, 1) + "}\n]";
		}

		private String extracted(HashMap<String, Quotes> quotesMap2, int i) {
			StringBuffer b = new StringBuffer();

			Set<Entry<String, Quotes>> entrySet = quotesMap2.entrySet();
			for (Entry<String, Quotes> entry : entrySet) {
				b.append(entry.getKey());
				b.append("=");

				Quotes value = entry.getValue();

				b.append("Q [\n\t" + getTabs(i + 1) + "orTxt=" + value.originalTxt + ",\n\t" + getTabs(i + 1) + "txt="
						+ value.txt + ",\n\t" + getTabs(i + 1) + "qm={" + extracted(value.quotesMap, i + 1) + "}\n"
						+ getTabs(i + 1) + "]");
				b.append(", ");
				b.append("\n" + getTabs(i + 1));
			}

			if (b.length() != 0) {
				b.delete(b.length() - i - 1 - 3, b.length());
			}

			return b.toString();
		}

		private String getTabs(int i) {
			StringBuffer b = new StringBuffer();

			for (int j = 0; j < i; j++) {
				b.append("\t");
			}
			return b.toString();
		}

	}

}
