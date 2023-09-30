package yakusu.android.nlp;

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
            throw new RuntimeException("could not create JSON for goal click general", e);
        }
        return result;
    }
    
	@Override
	public String toString() {
		return "+++++++++++++++++++++click general:(duration:"+duration+"#target:"+target+")+++++++++++++++++++++";
	}
}
