package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class GoalClickGeneral extends Goal {

    //text information about goal
    private TargetViewModel target;
    //click == 0, long click == 1, double == 2
    private int duration;

    public GoalClickGeneral(boolean satisfied, boolean fromGraph, TargetViewModel target, int duration){
        this.satisfied=satisfied;
        this.fromGraph=fromGraph;
        this.target=target;
        this.duration=duration;
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

    public GoalClickGeneral copy(){
        GoalClickGeneral result = new GoalClickGeneral(this.satisfied, this.fromGraph, this.target.copy(), this.duration);
        return result;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "click_general");
            result.put("satisfied", this.satisfied);
            result.put("from_graph", this.fromGraph);
            result.put("target", this.target.toJSON());
            result.put("duration", this.duration);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for goal click general");
            throw new RuntimeException("could not create JSON for goal click general", e);
        }
        return result;
    }
}
