package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 11/08/17.
 */

public class GoalRotate extends Goal {

    public GoalRotate(boolean satisfied, boolean fromGraph){
        this.satisfied=satisfied;
        this.fromGraph=fromGraph;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
        	result.put("type", "rotate");
            result.put("satisfied", this.satisfied);
            result.put("from_graph", this.fromGraph);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for goal rotate");
            throw new RuntimeException("could not create JSON for goal rotate", e);
        }
        return result;
    }

    public GoalRotate copy(){
        GoalRotate result = new GoalRotate(this.satisfied, this.fromGraph);
        return result;
    }
}
