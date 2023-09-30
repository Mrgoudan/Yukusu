package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class GoalClick extends Goal {


    //TODO remove generated walk and selector

    //information about target for this goal
    private TargetViewModel target;
    //duration whether it is normal (0) or long click (1)
    private int duration;
    //random count
    private int randomCount;

    public GoalClick(boolean satisfied, boolean fromGraph, TargetViewModel target, int duration, int randomCount){
        this.satisfied=satisfied;
        this.fromGraph=fromGraph;
        this.target=target;
        this.duration=duration;
        this.randomCount=randomCount;
    }

    public TargetViewModel getTarget(){
        return this.target;
    }

    public int getDuration(){
        return this.duration;
    }

    public void setTargetViewModel(TargetViewModel targetViewModel){
        this.target=targetViewModel;
    }

    public int getRandomCount(){
        return this.randomCount;
    }

    public void setRandomCount(int randomCount){
        this.randomCount=randomCount;
    }

    public GoalClick copy(){
        GoalClick result = new GoalClick(this.satisfied, this.fromGraph, this.target.copy(), this.duration, this.randomCount);
        return result;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "click");
            result.put("satisfied", this.satisfied);
            result.put("from_graph", this.fromGraph);
            result.put("target", this.target.toJSON());
            result.put("duration", this.duration);
            result.put("random_count", this.randomCount);

        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for goal click");
            throw new RuntimeException("could not create JSON for goal click", e);
        }
        return result;
    }
}
