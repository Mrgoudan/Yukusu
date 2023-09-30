package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

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

    public SelectorResourceId copy(){
        SelectorResourceId result = new SelectorResourceId(this.resourceId);
        return result;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "resource_id");
            result.put("resource_id", this.resourceId);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for ResourceId selector");
            throw new RuntimeException("could not create JSON for ResourceId selector", e);
        }
        return result;
    }

    @Override
    public String toString() {
        return resourceId;
    }

}
