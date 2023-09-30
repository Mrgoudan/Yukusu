package android.support.test.espresso.search;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class GoalSwipe extends Goal {

    //position 0==up, 1=down, 2=left, 3=right
    private int direction;


    public GoalSwipe(boolean satisfied, boolean fromGraph, int direction){
        this.satisfied=satisfied;
        this.direction=direction;
        this.fromGraph=fromGraph;
    }

    @Override
    public GoalSwipe copy() {
        GoalSwipe result = new GoalSwipe(this.satisfied, this.fromGraph, this.direction);
        return result;
    }

    public int getDirection(){
        return direction;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "swipe");
            result.put("satisfied", this.satisfied);
            result.put("direction", this.direction);
            result.put("from_graph", this.fromGraph);
        }
        catch(Exception e){
            throw new RuntimeException("could not create JSON for goal swipe", e);
        }
        return result;
    }
}
