package android.support.test.espresso.search;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
                           String xPath,
                           boolean precise, boolean fromText, boolean canBePreference){

        this.text=text;

        this.resourceId=resourceId;
        this.resourceIdReference=resourceIdReference;
        this.resourceIdReferenceKeywordsList=resourceIdReferenceKeywordsList;

        this.imageReference=imageReference;
        this.imageReferenceKeywordsList=imageReferenceKeywordsList;

        this.xPath=xPath;

        this.precise=precise;
        this.fromText=fromText;
        this.canBePreference=canBePreference;
    }

    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try{
            result.put("text", text);

            result.put("resource_id", resourceId);
            result.put("resource_id_reference", resourceIdReference);
            JSONArray resourceIdReferenceKeywordsArray = new JSONArray();
            for(String item:resourceIdReferenceKeywordsList){
                resourceIdReferenceKeywordsArray.put(item);
            }
            result.put("resource_id_reference_keywords", resourceIdReferenceKeywordsArray);

            result.put("image_reference", imageReference);
            JSONArray imageReferenceKeywordsArray = new JSONArray();
            for(String item:imageReferenceKeywordsList){
                imageReferenceKeywordsArray.put(item);
            }
            result.put("image_reference_keywords", imageReferenceKeywordsArray);

            result.put("xpath", xPath);

            result.put("precise", precise);
            result.put("from_text", fromText);
            result.put("can_be_preference", canBePreference);
        }
        catch(Exception e){
            throw new RuntimeException("could not create JSON for target view model", e);
        }
        return result;
    }

    public TargetViewModel copy(){
        List<String> newResourceIdReferenceKeywordsList = new ArrayList<String>();
        for(String item:resourceIdReferenceKeywordsList){
            newResourceIdReferenceKeywordsList.add(item);
        }
        List<String> newImageReferenceKeywordsList = new ArrayList<String>();
        for(String item:imageReferenceKeywordsList){
            newImageReferenceKeywordsList.add(item);
        }

        TargetViewModel result = new TargetViewModel(this.text, this.resourceId, this.resourceIdReference, newResourceIdReferenceKeywordsList, this.imageReference, newImageReferenceKeywordsList, this.xPath, this.precise, this.fromText, this.canBePreference);
        return result;
    }


    public String getText() {
        return text;
    }

    public String textToSentence(){
        return text;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String resourceIdToSentence() {
        return resourceId;
    }

    public String getResourceIdReference() {
        return resourceIdReference;
    }

    public String resourceIdReferenceToSentence(){
        return resourceIdReference;
    }

    public List<String> getResourceIdReferenceKeywordsList() {
        return resourceIdReferenceKeywordsList;
    }

    public String resourceIdReferenceKeywordsListToSentence(){
        String result = "";
        for(String item:resourceIdReferenceKeywordsList){
            result = result + item + " ";
        }
        result = result.trim();
        return result;
    }

    public String getImageReference() {
        return imageReference;
    }

    public String imageReferenceToSentence(){
        return imageReference;
    }

    public List<String> getImageReferenceKeywordsList() {
        return imageReferenceKeywordsList;
    }

    public String imageReferenceKeywordsListToSentence(){
        String result = "";
        for(String item:imageReferenceKeywordsList){
            result = result + item + " ";
        }
        result = result.trim();
        return result;
    }

    public String getXPath() {
        return xPath;
    }

    public boolean isPrecise(){
        return precise;
    }

    public boolean isFromText(){
        return fromText;
    }
    public boolean getCanBePreference(){
        return canBePreference;
    }



    public void setFromText(boolean fromText){
        this.fromText=fromText;
    }

}
