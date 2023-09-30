package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionRotate extends Action {

    public ActionRotate(Selector selector){
        this.selector=selector;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "rotate");
            result.put("selector", this.selector.toJSON());
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action rotate");
            throw new RuntimeException("could not create JSON for action rotate", e);
        }
        return result;
    }

    public ActionRotate copy(){
        ActionRotate result = new ActionRotate(this.selector.copy());
        return result;
    }

}
