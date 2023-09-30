package android.support.test.espresso.search;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class GoalClickPosition extends Goal {

    //click == 0, long click == 1
    private int duration;
    //position last==0 (this is 0 because postion start from 1)
    private int position;
    //Container type
    private String container;

    public GoalClickPosition(boolean satisfied, boolean fromGraph, int position, String container, int duration){
        this.satisfied=satisfied;
        this.fromGraph=fromGraph;
        this.position=position;
        this.container=container;
        this.duration=duration;
    }

    public int getDuration(){
        return duration;
    }

    public int getPosition(){
        return position;
    }

    public String getContainer(){
        return container;
    }

    @Override
    public GoalClickPosition copy() {
        GoalClickPosition result = new GoalClickPosition(this.satisfied, this.fromGraph, this.position, this.container, this.duration);
        return result;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
        	result.put("type", "click_position");
            result.put("satisfied", this.satisfied);
            result.put("from_graph", this.fromGraph);
            result.put("duration", this.duration);
            result.put("position", this.position);
            result.put("container", this.container);
        }
        catch(Exception e){
            throw new RuntimeException("could not create JSON for goal click position", e);
        }
        return result;
    }
}
