package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 11/08/17.
 */

public class GoalBack extends Goal {

    public GoalBack(boolean satisfied, boolean fromGraph){
        this.satisfied=satisfied;
        this.fromGraph=fromGraph;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
        	result.put("type", "back");
            result.put("satisfied", this.satisfied);
            result.put("from_graph", this.fromGraph);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for goal back");
            throw new RuntimeException("could not create JSON for goal back", e);
        }
        return result;
    }

    public GoalBack copy(){
        GoalBack result = new GoalBack(this.satisfied, this.fromGraph);
        return result;
    }
}
