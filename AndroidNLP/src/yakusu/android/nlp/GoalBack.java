package yakusu.android.nlp;

import org.json.JSONObject;

/**
 * Created by mattia on 11/08/17.
 */

public class GoalBack extends Goal {

    public GoalBack(boolean satisfied, boolean fromGraph){
        this.satisfied=satisfied;
        this.fromGraph=fromGraph;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
        	result.put("type", "back");
            result.put("satisfied", this.satisfied);
            result.put("from_graph", this.fromGraph);
        }
        catch(Exception e){
            throw new RuntimeException("could not create JSON for goal back", e);
        }
        return result;
    }

	@Override
	public String toString() {
		return "+++++++++++++++++++++back+++++++++++++++++++++";
	}
}
