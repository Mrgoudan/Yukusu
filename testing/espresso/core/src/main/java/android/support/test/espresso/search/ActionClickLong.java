package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionClickLong extends Action {

    public ActionClickLong(Selector selector){
        this.selector=selector;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "click_long");
            result.put("selector", this.selector.toJSON());
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action click long");
            throw new RuntimeException("could not create JSON for action click long", e);
        }
        return result;
    }

    public ActionClickLong copy(){
        ActionClickLong result = new ActionClickLong(this.selector.copy());
        return result;
    }

}
