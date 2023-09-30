package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionPreferenceClickDouble extends Action {

    private String text;

    public ActionPreferenceClickDouble(String text){
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
            result.put("type", "preference_click_double");
            result.put("selector", JSONObject.NULL);
            result.put("text", text);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action click preference double");
            throw new RuntimeException("could not create JSON for action click preference double", e);
        }
        return result;
    }

    public ActionPreferenceClickDouble copy(){
        ActionPreferenceClickDouble result = new ActionPreferenceClickDouble(text);
        return result;
    }

}
