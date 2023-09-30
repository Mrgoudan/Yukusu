package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionClickDouble extends Action {

    public ActionClickDouble(Selector selector){
        this.selector=selector;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "click_double");
            result.put("selector", this.selector.toJSON());
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action click double");
            throw new RuntimeException("could not create JSON for action click double", e);
        }
        return result;
    }

    public ActionClickDouble copy(){
        ActionClickDouble result = new ActionClickDouble(this.selector.copy());
        return result;
    }

}
