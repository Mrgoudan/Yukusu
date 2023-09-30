package android.support.test.espresso.search;

/**
 * Created by mattia on 8/17/17.
 */

public class NodeWindow extends Node {

    private String relatedActivity;
    private String type;

    public NodeWindow (int nodeId, String relatedActivity, String type){
        this.nodeId=nodeId;
        this.relatedActivity=relatedActivity;
        this.type=type;
    }

    public String getRelatedActivity() {
        return relatedActivity;
    }

    public String getType() { return type;}

}
