package yakusu.android.nlp;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by mattia on 11/9/17.
 */

//mf: decided not to use lemmas for now
public class TargetViewModel {

    private String text;

    private String resourceId;
    private String resourceIdReference;
    private List<String> resourceIdReferenceKeywordsList;

    private String imageReference;
    private List<String> imageReferenceKeywordsList;
    
    private String xPath;
    
    private boolean precise;
    private boolean fromText;
    private boolean canBePreference;

    public TargetViewModel(String text,
        String resourceId, String resourceIdReference, List<String> resourceIdReferenceKeywordsList,
        String imageReference, List<String> imageReferenceKeywordsList,
        String xpath,
        boolean precise, boolean fromText, boolean canBePreference){

        this.text=text;

        this.resourceId=resourceId;
        this.resourceIdReference=resourceIdReference;
        this.resourceIdReferenceKeywordsList=resourceIdReferenceKeywordsList;

        this.imageReference=imageReference;
        this.imageReferenceKeywordsList=imageReferenceKeywordsList;
        
        this.xPath=xpath;
        
        this.fromText=fromText;
        this.precise=precise;
        this.canBePreference=canBePreference;
    }

    public JSONObject toJSON(){
    	JSONObject result = new JSONObject();
    	try{
	    	result.put("text", text);
	    	
	    	result.put("resource_id", "");
	    	result.put("resource_id_reference", "");
	    	result.put("resource_id_reference_keywords", new JSONArray());
	    	
	    	result.put("image_reference", "");
	    	result.put("image_reference_keywords", new JSONArray());
	    	
	    	result.put("xpath", xPath);
	    	
	    	result.put("precise", precise);
	    	result.put("from_text",fromText);
	    	result.put("can_be_preference",canBePreference);
        }
        catch(Exception e){
            throw new RuntimeException("could not create JSON for target view model", e);
        }
    	return result;
    }
    
    public String toString(){
    	return "(text:"+text+"#precise:"+precise+"#from_text:"+fromText+"#preference:"+canBePreference+")";
    }

}
