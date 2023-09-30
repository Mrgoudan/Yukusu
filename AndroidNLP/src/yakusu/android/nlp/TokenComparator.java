package yakusu.android.nlp;

import java.util.Comparator;

public class TokenComparator implements Comparator<Token>{

	@Override
	public int compare(Token t1, Token t2) {
		return t1.getPosition() < t2.getPosition() ? -1 : t1.getPosition()==t2.getPosition() ? 0 : 1;
	}

}
