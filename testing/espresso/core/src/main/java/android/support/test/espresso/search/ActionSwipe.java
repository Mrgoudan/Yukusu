package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionSwipe extends Action {

    private int direction;//1 is down, 0 is up

    public ActionSwipe(Selector selector, int direction){
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
            result.put("type", "swipe");
            result.put("selector", this.selector.toJSON());
            result.put("direction", this.direction);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action swipe");
            throw new RuntimeException("could not create JSON for action swipe", e);
        }
        return result;
    }

    public ActionSwipe copy(){
        ActionSwipe result = new ActionSwipe(this.selector.copy(), this.direction);
        return result;
    }

}
