package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionSleep extends Action {

    private long time;

    public ActionSleep(Selector selector, long time) {
        this.selector = selector;
        this.time = time;
    }

    public long getTime() {
            return time;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "sleep");
            result.put("selector", this.selector.toJSON());
            result.put("time", this.time);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action sleep");
            throw new RuntimeException("could not create JSON for action sleep", e);
        }
        return result;
    }

    public ActionSleep copy(){
        ActionSleep result = new ActionSleep(this.selector.copy(), this.time);
        return result;
    }

}
