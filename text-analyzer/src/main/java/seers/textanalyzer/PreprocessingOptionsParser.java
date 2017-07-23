package seers.textanalyzer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class PreprocessingOptionsParser {

	public static final String OPTION_PREFIX = "-";

	private static final String DEFAULT_MININUM_TOKEN_LENGTH = "3";

	public static final String PUNCTUATION_REMOVAL = "p";
	public static final String NUMBERS_REMOVAL = "n";
	public static final String SHORT_TOKENS_REMOVAL = "s";
	public static final String SPECIAL_CHARS_REMOVAL = "c";
	public static final String CAMEL_CASE_SPLITTING = "cc";

	public static String getDefaultOptions() {
		StringBuilder builder = new StringBuilder();

		builder.append(getDefaultOptionsNoCamelCase());
		builder.append(" ");

		builder.append(OPTION_PREFIX);
		builder.append(CAMEL_CASE_SPLITTING);
		builder.append(" ");

		return builder.toString().trim();
	}

	private static final Options options = new Options();
	static {
		options.addOption(PUNCTUATION_REMOVAL, "remove punctuation?");
		options.addOption(NUMBERS_REMOVAL, "remove numbers?");
		options.addOption(SHORT_TOKENS_REMOVAL, "remove short tokens? If so, what is the minimum lenght?");
		options.addOption(Option.builder(SHORT_TOKENS_REMOVAL).argName("min_length").hasArg()
				.desc("remove short tokens? If so, what is the minimum lenght?").build());
		options.addOption(SPECIAL_CHARS_REMOVAL, "remove tokens with special characters?");
		options.addOption(CAMEL_CASE_SPLITTING, "split in camel case?");
	}

	private CommandLine cmd;

	public PreprocessingOptionsParser(String preprocessingOptions) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		String[] args = preprocessingOptions.split(" ");
		cmd = parser.parse(options, args);
	}

	public boolean removePunctuation() {
		return cmd.hasOption(PUNCTUATION_REMOVAL);
	}

	public boolean removeNumbers() {
		return cmd.hasOption(NUMBERS_REMOVAL);
	}

	public boolean removeShortTokens() {
		return cmd.hasOption(SHORT_TOKENS_REMOVAL);
	}

	public int getTokenMinLength() {

		String val = cmd.getOptionValue(SHORT_TOKENS_REMOVAL, DEFAULT_MININUM_TOKEN_LENGTH);

		Integer minLength = Integer.valueOf(val);

		if (minLength <= 0) {
			throw new RuntimeException("The minimum value shold be greater than 0");
		}

		return minLength;
	}

	public boolean removeSpecialCharTokens() {
		return cmd.hasOption(SPECIAL_CHARS_REMOVAL);
	}

	public boolean splitCamelCase() {
		return cmd.hasOption(CAMEL_CASE_SPLITTING);
	}

	public static String getDefaultOptionsNoCamelCase() {
		StringBuilder builder = new StringBuilder();

		builder.append(OPTION_PREFIX);
		builder.append(PUNCTUATION_REMOVAL);
		builder.append(" ");

		builder.append(OPTION_PREFIX);
		builder.append(NUMBERS_REMOVAL);
		builder.append(" ");

		builder.append(OPTION_PREFIX);
		builder.append(SHORT_TOKENS_REMOVAL);
		builder.append(" ");
		builder.append(DEFAULT_MININUM_TOKEN_LENGTH);
		builder.append(" ");

		builder.append(OPTION_PREFIX);
		builder.append(SPECIAL_CHARS_REMOVAL);

		return builder.toString().trim();
	}

	public static String buildStringOptions(String... options) {
		StringBuilder builder = new StringBuilder();

		for (String opt : options) {
			builder.append(OPTION_PREFIX);
			builder.append(opt);
			builder.append(" ");
		}

		return builder.toString().trim();
	}

}
