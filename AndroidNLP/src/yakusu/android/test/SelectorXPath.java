package yakusu.android.test;
/**
 * Created by mattia on 8/11/17.
 */

public class SelectorXPath extends Selector {

    private String xPath;

    public SelectorXPath(String xPath){
        this.xPath=xPath;
    }

    public String getXPath(){
        return xPath;
    }
    
	@Override
	public String getEspressoCode() {
		//$T == ViewMatchers
		String statement = "$T.withXPath($S)";
		return statement;
	}

}
