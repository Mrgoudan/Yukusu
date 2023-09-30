package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class GoalType extends Goal {

    private TargetViewModel target;
    private String text;
    //random count
    private int randomCount;

    public GoalType(boolean satisfied, boolean fromGraph, TargetViewModel target, String text, int randomCount){
        this.satisfied=satisfied;
        this.fromGraph=fromGraph;
        this.target=target;
        this.text=text;
        this.randomCount=randomCount;
    }

    public TargetViewModel getTarget(){
        return this.target;
    }

    public String getText(){
        return this.text;
    }

    public void setTargetViewModel(TargetViewModel targetViewModel){
        this.target=targetViewModel;
    }

    public GoalType copy(){
        GoalType result = new GoalType(this.satisfied, this.fromGraph, this.target.copy(), this.text, this.randomCount);
        return result;
    }

    public int getRandomCount(){
        return this.randomCount;
    }

    public void setRandomCount(int randomCount){
        this.randomCount=randomCount;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "type");
            result.put("satisfied", this.satisfied);
            result.put("from_graph", this.fromGraph);
            result.put("target", this.target.toJSON());
            result.put("text", this.text);
            result.put("random_count", this.randomCount);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for goal type");
            throw new RuntimeException("could not create JSON for goal type", e);
        }
        return result;
    }
}
