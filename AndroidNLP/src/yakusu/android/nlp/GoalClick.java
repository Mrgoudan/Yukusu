package yakusu.android.nlp;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class GoalClick extends Goal {

    //text information about goal
    private TargetViewModel target;
    //click == 0, long click == 1, double == 2
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
            throw new RuntimeException("could not create JSON for goal click", e);
        }
        return result;
    }
    
	@Override
	public String toString() {
		return "+++++++++++++++++++++click:(duration:"+duration+"#target:"+target+")+++++++++++++++++++++";
	}
}
