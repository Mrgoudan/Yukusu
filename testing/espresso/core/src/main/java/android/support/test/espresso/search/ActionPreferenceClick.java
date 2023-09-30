package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionPreferenceClick extends Action {

    private String text;

    public ActionPreferenceClick(String text){
        //no selector for preference
        this.selector=null;
        this.text=text;
    }

    public String getText(){
        return text;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "preference_click");
            result.put("selector", JSONObject.NULL);
            result.put("text", text);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action click preference");
            throw new RuntimeException("could not create JSON for action click preference", e);
        }
        return result;
    }

    public ActionPreferenceClick copy(){
        ActionPreferenceClick result = new ActionPreferenceClick(text);
        return result;
    }

}
