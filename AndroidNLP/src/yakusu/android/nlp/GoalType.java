package yakusu.android.nlp;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class GoalType extends Goal {

    //target element where to enter text
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
            throw new RuntimeException("could not create JSON for goal type", e);
        }
        return result;
    }
    
    public void setText(String text){
    	this.text=text;
    }
    
	@Override
	public String toString() {
		return "+++++++++++++++++++++type:(text:"+text+"#target:"+target+")+++++++++++++++++++++";
	}
}
