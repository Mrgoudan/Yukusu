package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionType extends Action {

    private String text;

    public ActionType(Selector selector, String text){
        this.selector=selector;
        this.text=text;
    }

    public String getText(){
        return text;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "type");
            result.put("selector", this.selector.toJSON());
            result.put("text", this.text);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action type");
            throw new RuntimeException("could not create JSON for action type", e);
        }
        return result;
    }

    public ActionType copy(){
        ActionType result = new ActionType(this.selector.copy(), this.text);
        return result;
    }

}
