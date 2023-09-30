package yakusu.android.nlp;

public class Token {

	private int position;
	private String word;
	private String lemma;
	
	public Token(int position, String word, String lemma){
		this.position=position;
		this.word=word;
		this.lemma=lemma;
	}

	public int getPosition() {
		return position;
	}

	public String getWord() {
		return word;
	}

	public String getLemma() {
		return lemma;
	}
}
