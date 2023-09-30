package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class GoalOpenOptions extends Goal {

    public GoalOpenOptions(boolean satisfied, boolean fromGraph){
        this.satisfied=satisfied;
        this.fromGraph=fromGraph;
    }

    public GoalOpenOptions copy(){
        GoalOpenOptions result = new GoalOpenOptions(this.satisfied, this.fromGraph);
        return result;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "open_options");
            result.put("satisfied", this.satisfied);
            result.put("from_graph", this.fromGraph);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for goal open options");
            throw new RuntimeException("could not create JSON for goal open options", e);
        }
        return result;
    }
}
