package seers.textanalyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class QuoteProcessor {

	public static final String QUOTE_PREFIX = "QUT_";
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

			List<String> valuesInQuotes = getValues(quoteMark, txtToProcess);

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

	private List<String> getValues(String quoteMark, String txtToProcess) {

		String[] tokens = txtToProcess.split(" ");

		List<String> values = new ArrayList<>();
		for (int i = 0; i < tokens.length;) {
			String token = tokens[i];

			if (token.startsWith(quoteMark)) {
				for (int j = i; j < tokens.length; j++) {
					String token2 = tokens[j];

					if (token2.endsWith(quoteMark) || token2.matches(".+\\" + quoteMark + "\\p{Punct}+$")) {
						String value = getSubstring(tokens, i, j + 1, quoteMark);
						if (value!=null) {
							values.add(value);
						}
						i = j;
						break;
					}

				}
			}

			i++;

		}

		return values;
	}

	private String getSubstring(String[] tokens, int ini, int end, String quoteMark) {
		String[] subArray = Arrays.copyOfRange(tokens, ini, end);
		String subString = StringUtils.join(subArray, " ").trim();
		// subString = subString.replaceFirst("\\" + quoteMark + "\\p{Punct}+$",
		// "");
		// subString = subString.replace(quoteMark, "");

		String[] subs = StringUtils.substringsBetween(subString, quoteMark, quoteMark);
		if (subs != null) {
			return subs[0];
		}
		return null;
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
