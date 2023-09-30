package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionScroll extends Action {

    private int direction;//1 is down, 0 is up

    public ActionScroll(Selector selector, int direction){
        this.selector=selector;
        this.direction=direction;
    }

    public int getDirection(){
        return direction;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "scroll");
            result.put("selector", this.selector.toJSON());
            result.put("direction", this.direction);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action scroll");
            throw new RuntimeException("could not create JSON for action scroll", e);
        }
        return result;
    }

    public ActionScroll copy(){
        ActionScroll result = new ActionScroll(this.selector.copy(), this.direction);
        return result;
    }

}
