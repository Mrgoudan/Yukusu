package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionBack extends Action {

    public ActionBack(Selector selector){
        this.selector=selector;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "back");
            result.put("selector", this.selector.toJSON());
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action back");
            throw new RuntimeException("could not create JSON for action back", e);
        }
        return result;
    }

    public ActionBack copy(){
        ActionBack result = new ActionBack(this.selector.copy());
        return result;
    }

}
