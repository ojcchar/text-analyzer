package seers.textanalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.EnhancedPlusPlusDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;
import seers.appcore.utils.ExceptionUtils;
import seers.textanalyzer.QuoteProcessor.Quotes;
import seers.textanalyzer.entity.Sentence;
import seers.textanalyzer.entity.Token;

/**
 * @author ojcch
 *
 */
public class TextProcessor {

	private static StanfordCoreNLP defaultPipeline;
	private static StanfordCoreNLP fullPipeline;

	private synchronized static void initFullPipeline() {

		if (fullPipeline != null) {
			return;
		}
		Properties props2 = new Properties();
		props2.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse");
		// props2.setProperty("annotators", "tokenize, ssplit, pos, lemma,
		// depparse, ner, mention, coref");
		// props2.setProperty("coref.algorithm", "statistical");
		props2.setProperty("tokenize.options", "untokenizable=noneKeep,invertible=true");
		fullPipeline = new StanfordCoreNLP(props2);
	}

	private synchronized static void initDefaultPipeline() {

		if (defaultPipeline != null) {
			return;
		}

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
		props.setProperty("tokenize.options", "untokenizable=noneKeep,invertible=true");
		defaultPipeline = new StanfordCoreNLP(props);
	}

	public static final String[] PARENTHESIS = { "-LCB-", "-RCB-", "-LRB-", "-RRB-", "-LSB-", "-RSB-" };
	public static final String[] PARENTHESIS2 = { "LCB", "RCB", "LRB", "RRB", "LSB", "RSB" };
	private static final String SPACE = " ";
	private static HashMap<String, String> POS_TAGS = new HashMap<String, String>();

	static {

		POS_TAGS.put("JJ", "JJ");
		POS_TAGS.put("JJR", "JJ");
		POS_TAGS.put("JJS", "JJ");

		POS_TAGS.put("NN", "NN");
		POS_TAGS.put("NNS", "NN");
		POS_TAGS.put("NNP", "NN");
		POS_TAGS.put("NNPS", "NN");

		POS_TAGS.put("PRP", "PRP");
		POS_TAGS.put("PRP$", "PRP");

		POS_TAGS.put("RB", "RB");
		POS_TAGS.put("RBR", "RB");
		POS_TAGS.put("RBS", "RB");

		POS_TAGS.put("VB", "VB");
		POS_TAGS.put("VBD", "VB");
		POS_TAGS.put("VBG", "VB");
		POS_TAGS.put("VBN", "VB");
		POS_TAGS.put("VBP", "VB");
		POS_TAGS.put("VBZ", "VB");

		POS_TAGS.put("WDT", "WH");
		POS_TAGS.put("WP", "WH");
		POS_TAGS.put("WP$", "WH");
		POS_TAGS.put("WRB", "WH");
	}

	public static String getGeneralPos(String pos) {
		String tag = POS_TAGS.get(pos);
		if (tag != null) {
			return tag;
		}
		return pos;
	}

	/**
	 * This method does not check for identifies (i.e., identifiers maybe not be
	 * parsed as NN).
	 * 
	 * Method created for compatibility reasons.
	 * 
	 * @param text
	 * @return
	 */
	public static List<Sentence> processText(String text) {
		return processText(text, false);
	}

	public static List<Sentence> processText(String text, boolean checkForIdentifiers) {
		initDefaultPipeline();

		Annotation document = new Annotation(text);
		defaultPipeline.annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		List<Sentence> parsedSentences = new ArrayList<>();
		Integer id = 0;

		for (CoreMap sentence : sentences) {

			List<CoreLabel> tokenList = sentence.get(TokensAnnotation.class);

			String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
			Sentence parsedSentence = new Sentence(id.toString(), sentenceText);

			for (CoreLabel token : tokenList) {
				Token parsedToken = parseToken(token, checkForIdentifiers);

				parsedSentence.addToken(parsedToken);
			}

			parsedSentences.add(parsedSentence);
			id++;
		}

		return parsedSentences;

	}

	public static List<Sentence> preprocessText(String text, List<String> stopWords) {
		String options = PreprocessingOptionsParser.getDefaultOptionsNoCamelCase();
		return preprocessText(text, stopWords, options);
	}
	
	public static List<Sentence> preprocessText(String text, List<String> stopWords, String[] preprocessingOptions) {
		String options = PreprocessingOptionsParser.buildStringOptions(preprocessingOptions);
		return preprocessText(text, stopWords, options);
	}

	public static List<Sentence> preprocessText(String text, List<String> stopWords, String preprocessingOptions) {

		PreprocessingOptionsParser parser;
		try {
			parser = new PreprocessingOptionsParser(preprocessingOptions);
		} catch (ParseException e) {
			throw ExceptionUtils.getRuntimeException(e);
		}

		// -------------------------

		initDefaultPipeline();

		Annotation document = new Annotation(text);
		defaultPipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		List<Sentence> parsedSentences = new ArrayList<>();
		Integer id = 0;

		for (CoreMap sentence : sentences) {

			List<CoreLabel> tokenList = sentence.get(TokensAnnotation.class);

			String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
			Sentence parsedSentence = new Sentence(id.toString(), sentenceText);

			for (CoreLabel token : tokenList) {

				if (parser.splitCamelCase()) {
					splitCamelCaseAndAddTokens(parser, stopWords, parsedSentence, token);
				} else {
					addToken(parser, stopWords, parsedSentence, token);
				}
			}

			if (parsedSentence.isEmpty()) {
				continue;
			}

			parsedSentences.add(parsedSentence);
			id++;
		}

		return parsedSentences;

	}

	private static void splitCamelCaseAndAddTokens(PreprocessingOptionsParser parser, List<String> stopWords,
			Sentence parsedSentence, CoreLabel token) {
	
		String word = token.get(TextAnnotation.class);
		String[] ccTokens = StringUtils.splitByCharacterTypeCamelCase(word);
		String tokenCC = StringUtils.join(ccTokens, ' ');

		Annotation tokenAnnot = new Annotation(tokenCC);
		defaultPipeline.annotate(tokenAnnot);
		
		List<CoreMap> sentences = tokenAnnot.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			
			List<CoreLabel> tokenList = sentence.get(TokensAnnotation.class);
			for (CoreLabel newToken : tokenList) {
				addToken(parser, stopWords, parsedSentence, newToken);
			}
		}
	}

	private static void addToken(PreprocessingOptionsParser parser, List<String> stopWords, Sentence parsedSentence,
			CoreLabel token) {
		String word = token.get(TextAnnotation.class);
		String lemma = token.get(LemmaAnnotation.class).toLowerCase();
		String pos = token.get(PartOfSpeechAnnotation.class);

		if (parser.removePunctuation()) {
			if (isPunctuation(lemma)) {
				return;
			}

			if (matchesPOS(pos, "LS")) {
				return;
			}
		}

		if (parser.removeNumbers() && isNumber(lemma)) {
			return;
		}

		if (parser.removeShortTokens() && isShortTerm(lemma, pos, parser.getTokenMinLength())) {
			return;
		}

		if (parser.removeSpecialCharTokens() && containsSpecialChars(lemma, pos)) {
			return;
		}

		if (stopWords != null && stopWords.size() > 0 && isStopWord(stopWords, lemma, pos)) {
			return;
		}

		String generalPos = getGeneralPos(pos);
		String stem = GeneralStemmer.stemmingPorter(word).toLowerCase();

		Token parsedToken = new Token(word, generalPos, pos, lemma, stem);
		parsedSentence.addToken(parsedToken);
	}

	public static boolean isShortTerm(String lemma, String pos, int length) {
		if (matchesPOS(pos, "cd")) {
			return false;
		}
		return (lemma.length() < length);
	}

	public static boolean matchesPOS(String pos, String posToMatch) {
		return posToMatch.equalsIgnoreCase(pos);
	}

	public static boolean isStopWord(List<String> stopWords, String lemma, String pos) {
		return stopWords.contains(lemma);
	}

	public static boolean containsSpecialChars(String str, String pos) {

		if (matchesPOS(pos, "sym")) {
			return true;
		}

		if ("'s".equalsIgnoreCase(str)) {
			return false;
		}

		if ("'ll".equalsIgnoreCase(str)) {
			return false;
		}

		String[] split = str.split("/");
		if (split.length > 1) {

			for (String s : split) {
				if (checkSpecialChars(s)) {
					return true;
				}
			}

			if (pos.equalsIgnoreCase("cd")) {
				return true;
			}

			return false;
		}

		split = str.split("-");
		if (split.length > 1) {

			for (String s : split) {
				if (checkSpecialChars(s)) {
					return true;
				}
			}

			if (pos.equalsIgnoreCase("cd")) {
				return true;
			}

			return false;
		}

		boolean b = checkSpecialChars(str);
		return b;
	}

	public static boolean checkSpecialChars(String str) {
		String[] split = str.split("[^a-zA-Z0-9]");
		boolean b = split.length != 1;
		return b;
	}

	public static boolean isNumber(String token) {
		boolean isInt = token.matches("\\d+((\\h|\\s)+\\d+)+");
		if (isInt) {
			return true;
		}

		try {
			Double.valueOf(token);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}

	}

	public static boolean isPunctuation(String token) {
		return token.matches("[\\p{P}\\p{S}]") || isParenthesis(token);
	}

	public static boolean isParenthesis(String token) {
		for (String parenthesis : PARENTHESIS) {
			if (token.toLowerCase().contains(parenthesis.toLowerCase())) {
				return true;
			}
		}

		for (String parenthesis : PARENTHESIS2) {
			if (token.toLowerCase().equals(parenthesis.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static String getStringFromSentences(List<Sentence> sentences) {
		StringBuffer buffer = new StringBuffer();
		for (Sentence sentence : sentences) {
			List<Token> tokens = sentence.getTokens();
			for (Token token : tokens) {
				buffer.append(token.getLemma());
				buffer.append(SPACE);
			}
		}
		return buffer.toString().trim();
	}

	public static List<Token> getAllTokens(List<Sentence> sentences) {
		List<Token> tokens = new ArrayList<>();
		for (Sentence sentence : sentences) {
			tokens.addAll(sentence.getTokens());
		}
		return tokens;
	}

	public static List<Token> getUniqueTokensBy(List<Sentence> sentences, Function<Token, Object> fieldFn) {

		List<Token> tokens = TextProcessor.getAllTokens(sentences);
		// unique terms by lemma
		List<Token> uniqueTokens = getUniqueTokensBy(fieldFn, tokens);

		return uniqueTokens;
	}

	private static List<Token> getUniqueTokensBy(Function<Token, Object> fieldFn, List<Token> tokens) {
		List<Token> uniqueTokens = new ArrayList<>();
		tokens.stream().filter(distinctByField(fieldFn)).forEach(t -> {
			uniqueTokens.add(t);
		});
		return uniqueTokens;
	}

	private static <T> Predicate<T> distinctByField(Function<? super T, Object> fieldExtractor) {
		Map<Object, Boolean> elemsSeen = new ConcurrentHashMap<>();
		return t -> elemsSeen.putIfAbsent(fieldExtractor.apply(t), Boolean.TRUE) == null;
	}

	public static List<Sentence> getSentencesWithUniqueTerms(List<Sentence> sentences,
			Function<Token, Object> fieldFn) {

		List<Sentence> uniqueSentences = new ArrayList<>();
		sentences.stream().forEach(s -> {
			List<Token> unTkns = getUniqueTokensBy(fieldFn, s.getTokens());
			uniqueSentences.add(new Sentence(s.getId(), unTkns, s.getText()));
		});

		return uniqueSentences;

	}

	public static List<String> readStopWords(String stopWordsPath) throws IOException {
		List<String> lines = FileUtils.readLines(new File(stopWordsPath), Charset.defaultCharset());
		List<String> stopWords = new ArrayList<>();
		for (String line : lines) {
			stopWords.add(line.trim().toLowerCase());
		}
		return stopWords;
	}

	public static String getStringFromLemmas(Sentence sentence) {
		StringBuffer buffer = new StringBuffer();
		List<Token> tokens = sentence.getTokens();
		for (Token token : tokens) {
			buffer.append(token.getLemma());
			buffer.append(SPACE);
		}
		return buffer.toString().trim();
	}

	public static String getStringFromTerms(Sentence sentence) {
		StringBuffer buffer = new StringBuffer();
		List<Token> tokens = sentence.getTokens();
		for (Token token : tokens) {
			buffer.append(token.getWord());
			buffer.append(SPACE);
		}
		return buffer.toString().trim();
	}

	public static List<Sentence> processTextFullPipelineAndQuotes(String text, boolean checkForIdentifiers) {

		QuoteProcessor processor = new QuoteProcessor();

		Quotes quotes = processor.processSentence(text);

		List<Sentence> sentences = processSentencesWithQuotes(quotes, checkForIdentifiers);

		return sentences;

	}

	private static List<Sentence> processSentencesWithQuotes(Quotes quotes, boolean checkForIdentifiers) {
		initFullPipeline();

		String text = quotes.txt;
		Annotation document = new Annotation(text);
		fullPipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		List<Sentence> parsedSentences = new ArrayList<>();
		Integer id = 0;

		for (CoreMap sentence : sentences) {

			List<CoreLabel> tokenList = sentence.get(TokensAnnotation.class);

			String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
			Sentence parsedSentence = new Sentence(id.toString(), sentenceText);

			for (CoreLabel token : tokenList) {
				Token parsedToken = parseToken(token, checkForIdentifiers);
				parsedSentence.addToken(parsedToken);
			}

			SemanticGraph dependencies = sentence.get(EnhancedPlusPlusDependenciesAnnotation.class);
			parsedSentence.setDependencies(dependencies);

			Set<Entry<String, Quotes>> quotesMap = quotes.quotesMap.entrySet();
			for (Entry<String, Quotes> quote : quotesMap) {

				List<Sentence> qSentences = processSentencesWithQuotes(quote.getValue(), checkForIdentifiers);
				parsedSentence.addQuote(quote.getKey(), qSentences);
			}

			parsedSentences.add(parsedSentence);
			id++;
		}

		return parsedSentences;
	}

	public static List<Sentence> processTextFullPipeline(String text, boolean checkForIdentifiers) {
		initFullPipeline();

		Annotation document = new Annotation(text);
		fullPipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		List<Sentence> parsedSentences = new ArrayList<>();
		Integer id = 0;

		for (CoreMap sentence : sentences) {

			List<CoreLabel> tokenList = sentence.get(TokensAnnotation.class);

			String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
			Sentence parsedSentence = new Sentence(id.toString(), sentenceText);

			for (CoreLabel token : tokenList) {
				Token parsedToken = parseToken(token, checkForIdentifiers);
				parsedSentence.addToken(parsedToken);
			}

			SemanticGraph dependencies = sentence.get(EnhancedPlusPlusDependenciesAnnotation.class);
			parsedSentence.setDependencies(dependencies);
			//
			// IntTuple a = document.get(CorefDestAnnotation.class);
			// Integer b = document.get(CorefClusterIdAnnotation.class);
			// Set<CoreLabel> c = document.get(CorefClusterAnnotation.class);
			// List<Mention> d = document.get(CorefMentionsAnnotation.class);
			//
			//
			// IntTuple e = sentence.get(CorefDestAnnotation.class);
			// Integer f = sentence.get(CorefClusterIdAnnotation.class);
			// Set<CoreLabel> g = sentence.get(CorefClusterAnnotation.class);
			// List<Mention> h = sentence.get(CorefMentionsAnnotation.class);
			//
			// Map<Integer, CorefChain> corefChains =
			// document.get(CorefChainAnnotation.class);
			//
			// parsedSentence.setCorefChains(corefChains);

			parsedSentences.add(parsedSentence);
			id++;
		}

		return parsedSentences;
	}

	public static Token parseToken(CoreLabel token, boolean checkForIdentifiers) {
		String word = token.get(TextAnnotation.class);
		String lemma = token.get(LemmaAnnotation.class).toLowerCase();
		String pos = token.get(PartOfSpeechAnnotation.class);
		if (checkForIdentifiers) {
			// match identifiers like "org.Class"
			// match method calls such as "call(param1)"
			if ((word.contains(".") || (word.contains("(") && word.contains(")"))) && word.length() > 1) {
				pos = "NN";
			}
		}

		String generalPos = getGeneralPos(pos);
		String stem = GeneralStemmer.stemmingPorter(word).toLowerCase();

		Token parsedToken = new Token(word, generalPos, pos, lemma, stem);
		return parsedToken;
	}

	public static String getStringFromTermsAndPos(Sentence sentence, boolean lowercase
	// , boolean processParenthesis
	) {
		StringBuffer buffer = new StringBuffer();
		List<Token> tokens = sentence.getTokens();
		for (Token token : tokens) {
			String word = token.getWord();

			// ----------------------------------

			if (lowercase) {
				word = word.toLowerCase();
			}

			// ----------------------------------

			// if (processParenthesis) {
			// }

			// ----------------------------------

			buffer.append(word);
			buffer.append("$$");
			buffer.append(token.getPos());
			buffer.append(SPACE);
		}
		return buffer.toString().trim();
	}

	public static String getStringFromLemmasAndPos(Sentence sentence) {
		StringBuffer buffer = new StringBuffer();
		List<Token> tokens = sentence.getTokens();
		for (Token token : tokens) {
			buffer.append(token.getLemma());
			buffer.append("$$");
			buffer.append(token.getPos());
			buffer.append(SPACE);
		}
		return buffer.toString().trim();
	}

	public static boolean checkGeneralPos(String tag, String... tagsToAssert) {
		String gnrlPos = getGeneralPos(tag);
		return Arrays.stream(tagsToAssert).anyMatch(t -> t.equals(gnrlPos));
	}

}
