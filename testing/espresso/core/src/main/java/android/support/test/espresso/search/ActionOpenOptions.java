package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionOpenOptions extends Action {

    public ActionOpenOptions(Selector selector){
        this.selector=selector;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "open_options");
            result.put("selector", this.selector.toJSON());
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action open options");
            throw new RuntimeException("could not create JSON for action open options", e);
        }
        return result;
    }

    public ActionOpenOptions copy(){
        ActionOpenOptions result = new ActionOpenOptions(this.selector.copy());
        return result;
    }

}
