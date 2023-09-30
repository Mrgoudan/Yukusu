package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionKeyboardClose extends Action {

    public ActionKeyboardClose(Selector selector){
        this.selector=selector;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "keyboard_close");
            result.put("selector", this.selector.toJSON());
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action keyboard close");
            throw new RuntimeException("could not create JSON for keyboard close", e);
        }
        return result;
    }

    public ActionKeyboardClose copy(){
        ActionKeyboardClose result = new ActionKeyboardClose(this.selector.copy());
        return result;
    }

}
