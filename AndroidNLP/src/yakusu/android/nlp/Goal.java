package yakusu.android.nlp;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public abstract class Goal {

    protected boolean satisfied;

    public boolean isSatisfied(){
        return satisfied;
    }

    public void setSatisfied(boolean satisfied){
        this.satisfied=satisfied;
    }
    
    protected boolean fromGraph;

    public boolean isFromGraph(){
        return fromGraph;
    }

    public void setFromGraph(boolean fromGraph){
        this.fromGraph=fromGraph;
    }

    public abstract JSONObject toJSON();
    
    public abstract String toString();
}
