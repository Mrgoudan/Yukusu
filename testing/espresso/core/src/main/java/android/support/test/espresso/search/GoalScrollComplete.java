package android.support.test.espresso.search;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class GoalScrollComplete extends Goal {

    //position 1==bottom, 0=top
    private int direction;
    

    public GoalScrollComplete(boolean satisfied, boolean fromGraph, int direction){
        this.satisfied=satisfied;
        this.fromGraph=fromGraph;
        this.direction=direction;
        this.fromGraph=fromGraph;
    }

    @Override
    public GoalScrollComplete copy() {
        GoalScrollComplete result = new GoalScrollComplete(this.satisfied, this.fromGraph, this.direction);
        return result;
    }

    public int getDirection(){
        return direction;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        try {
            result.put("type", "scroll_complete");
            result.put("satisfied", this.satisfied);
            result.put("direction", this.direction);
            result.put("from_graph", this.fromGraph);
        } catch (Exception e) {
            throw new RuntimeException("could not create JSON for goal scroll complete", e);
        }
        return result;
    }
}
