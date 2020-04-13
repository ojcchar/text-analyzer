package seers.textanalyzer.entity;

import java.util.Objects;

public class Token {

    private String word;
	private String generalPos;
	private String pos;
	private String lemma;
	private String stem;
	private final int index; //stanford corenlp index in the semantic graph
    private final int beginPosition;
    private final int endPosition;

    public Token(String word, String generalPos, String pos, String lemma, String stem,
                 int beginPosition, int endPosition, int index) {
		this.word = word;
		this.generalPos = generalPos;
		this.pos = pos;
		this.lemma = lemma;
		this.stem = stem;
		this.beginPosition = beginPosition;
		this.endPosition = endPosition;
		this.index = index;
	}

	public Token(String word, String generalPos, String pos, String lemma, String stem,
				 int beginPosition, int endPosition){
    	this(word, generalPos, pos, lemma, stem, beginPosition, endPosition, -1);
	}

	public int getIndex() {
		return index;
	}

	public int getBeginPosition() {
        return beginPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getLemma() {
		return lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	@Override
	public String toString() {
		return "(w=" + word + ", gp=" + generalPos + ", p=" + pos + ", l=" + lemma + ", b=" + beginPosition +
                ", e=" + endPosition +")";
	}

	public String getGeneralPos() {
		return generalPos;
	}

	public void setGeneralPos(String generalPos) {
		this.generalPos = generalPos;
	}

	public String getStem() {
		return stem;
	}

	public void setStem(String stem) {
		this.stem = stem;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Token token = (Token) o;
		return Objects.equals(lemma, token.lemma);
	}

	@Override
	public int hashCode() {

		return Objects.hash(lemma);
	}
}
