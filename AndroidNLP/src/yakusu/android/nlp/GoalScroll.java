package yakusu.android.nlp;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class GoalScroll extends Goal {

    //direction 0==up, 1=down, 2=left, 3=right
    private int direction;
    

    public GoalScroll(boolean satisfied, boolean fromGraph, int direction){
        this.satisfied=satisfied;
        this.fromGraph=fromGraph;
        this.direction=direction;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
        	result.put("type", "scroll");
            result.put("satisfied", this.satisfied);
            result.put("from_graph", this.fromGraph);
            result.put("direction", this.direction);
        }
        catch(Exception e){
            throw new RuntimeException("could not create JSON for goal scroll", e);
        }
        return result;
    }
    
	@Override
	public String toString() {
		return "+++++++++++++++++++++scroll:(direction:"+direction+")+++++++++++++++++++++";
	}
}
