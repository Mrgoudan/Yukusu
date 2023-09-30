package yakusu.android.nlp;

import edu.stanford.nlp.naturalli.SentenceFragment;

public class IndexedClause implements Comparable<IndexedClause>{

	private int index;
	private SentenceFragment clause;
	
	public IndexedClause(int index, SentenceFragment clause){
		this.index=index;
		this.clause=clause;
	}

	public int getIndex() {
		return index;
	}

	public SentenceFragment getClause() {
		return clause;
	}
	
	@Override
	public int compareTo(IndexedClause indexedClause) {
		if(this.index<indexedClause.getIndex()){
			return -1;
		}
		else if(this.index==indexedClause.getIndex()){
			return 0;
		}
		else{
			return 1;
		}
	}
	
	
}
