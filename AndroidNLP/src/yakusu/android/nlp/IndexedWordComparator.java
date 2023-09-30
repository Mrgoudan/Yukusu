package yakusu.android.nlp;

import java.util.Comparator;

import edu.stanford.nlp.ling.IndexedWord;

public class IndexedWordComparator implements Comparator<IndexedWord>{

	@Override
	public int compare(IndexedWord o1, IndexedWord o2) {
		if(o1.index()<o2.index()){
			return -1;
		}
		else if(o1.index()==o2.index()){
			return 0;
		}
		else{
			return 1;
		}
	}

}
