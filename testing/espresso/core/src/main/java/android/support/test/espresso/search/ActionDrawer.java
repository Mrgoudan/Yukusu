package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionDrawer extends Action {

    private int direction;//1 is open from left, 0 is close left

    public ActionDrawer(Selector selector, int direction){
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
            result.put("type", "drawer");
            result.put("selector", this.selector.toJSON());
            result.put("direction", this.direction);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action drawer");
            throw new RuntimeException("could not create JSON for action drawer", e);
        }
        return result;
    }

    public ActionDrawer copy(){
        ActionDrawer result = new ActionDrawer(this.selector.copy(), this.direction);
        return result;
    }

}
