package yakusu.android.nlp;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class GoalOpenOptions extends Goal {

    public GoalOpenOptions(boolean satisfied, boolean fromGraph){
        this.satisfied=satisfied;
        this.fromGraph=fromGraph;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
        	result.put("type", "open_options");
            result.put("satisfied", this.satisfied);
            result.put("from_graph", this.fromGraph);
        }
        catch(Exception e){
            throw new RuntimeException("could not create JSON for goal open options", e);
        }
        return result;
    }
    
	@Override
	public String toString() {
		return "+++++++++++++++++++++open options+++++++++++++++++++++";
	}
}
