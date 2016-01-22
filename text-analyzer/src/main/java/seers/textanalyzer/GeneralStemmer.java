package seers.textanalyzer;

public class GeneralStemmer {

	public static String stemmingPorter(String token) {
		PorterStemmer stemmer = new PorterStemmer();
		for (int j = 0; j < token.length(); j++)
			stemmer.add(token.charAt(j));
		stemmer.stem();
		return stemmer.toString();
	}

}
