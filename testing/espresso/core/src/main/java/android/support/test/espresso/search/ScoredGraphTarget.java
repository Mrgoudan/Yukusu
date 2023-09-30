package android.support.test.espresso.search;

/**
 * Created by mattia on 11/10/17.
 */

public class ScoredGraphTarget {

    private double score = 0;
    private NodeView nodeView;

    public ScoredGraphTarget(double score, NodeView nodeView){
        this.score=score;
        this.nodeView=nodeView;
    }

    public double getScore(){
        return score;
    }

    public NodeView getNodeView(){
        return nodeView;
    }
}
