package seers.textanalyzer.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import edu.stanford.nlp.semgraph.SemanticGraph;

public class Sentence {

	private String id;
	private List<Token> tokens;
	private SemanticGraph dependencies;
	private String text;
	private HashMap<String, List<Sentence>> quotes;

	public Sentence(String id) {
		if (id == null) {
			throw new NullPointerException();
		}
		this.id = id;
		tokens = new ArrayList<>();
		quotes = new LinkedHashMap<>();
	}

	public Sentence(String id, String text) {
		this(id);
		this.text = text;
		quotes = new LinkedHashMap<>();
	}

	public Sentence(String id, List<Token> tokens) {
		this(id);
		if (tokens == null) {
			throw new NullPointerException();
		}
		this.tokens = tokens;
		quotes = new LinkedHashMap<>();
	}

	public Sentence(String id, List<Token> tokens, String text) {
		this(id, tokens);
		this.text = text;
	}

	public void addToken(Token token) {
		tokens.add(token);
	}

	public String getId() {
		return id;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sentence other = (Sentence) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public boolean isEmpty() {
		return tokens.isEmpty();
	}

	@Override
	public String toString() {
		return "s [id=" + id + ", tk=" + tokens + "]";
	}

	public SemanticGraph getDependencies() {
		return dependencies;
	}

	public void setDependencies(SemanticGraph dependencies) {
		this.dependencies = dependencies;
	}

	public String getText() {
		return text;
	}

	public HashMap<String, List<Sentence>> getQuotes() {
		return quotes;
	}

	public void addQuote(String quoteKey, List<Sentence> sentences) {
		this.quotes.put(quoteKey, sentences);
	}

}
