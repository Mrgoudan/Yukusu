package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionPreferenceClickLong extends Action {

    private String text;

    public ActionPreferenceClickLong(String text){
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
            result.put("type", "preference_click_long");
            result.put("selector", JSONObject.NULL);
            result.put("text", text);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action click preference long");
            throw new RuntimeException("could not create JSON for action click preference long", e);
        }
        return result;
    }

    public ActionPreferenceClickLong copy(){
        ActionPreferenceClickLong result = new ActionPreferenceClickLong(text);
        return result;
    }

}
