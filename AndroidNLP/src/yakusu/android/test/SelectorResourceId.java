package yakusu.android.test;

/**
 * Created by mattia on 8/11/17.
 */

public class SelectorResourceId extends Selector {

    private String resourceId;

    public SelectorResourceId(String resourceId){
        this.resourceId=resourceId;
    }

    public String getResourceId(){
        return resourceId;
    }
    
	@Override
	public String getEspressoCode() {
		//$T == ViewMatchers
		String statement = "$T.withId($L)";
		return statement;
	}

}
