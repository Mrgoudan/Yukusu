package android.support.test.espresso.search;

/**
 * Created by mattia on 8/17/17.
 */

public class NodeView extends Node {

    private String fullClassName;
    private NodeWindow containingWindow;
    private TargetViewModel nodeViewModel;

    public NodeView (int nodeId, String fullClassName, NodeWindow containingWindow, TargetViewModel nodeViewModel){
        this.nodeId=nodeId;
        this.fullClassName=fullClassName;
        this.containingWindow=containingWindow;
        this.nodeViewModel=nodeViewModel;
    }

    public NodeWindow getContainingWindow() {
        return containingWindow;
    }

    public TargetViewModel getNodeViewModel(){
        return nodeViewModel;
    }

    public String getFullClassName() {return fullClassName;}
}
