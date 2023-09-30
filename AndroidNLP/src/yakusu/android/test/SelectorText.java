package yakusu.android.test;
/**
 * Created by mattia on 8/11/17.
 */

public class SelectorText extends Selector {

    private String text;

    public SelectorText(String text){
        this.text=text;
    }

    public String getText(){
        return text;
    }
    
	@Override
	public String getEspressoCode() {
		//$T == ViewMatchers
		String statement = "$T.withText($S)";
		return statement;
	}

}
