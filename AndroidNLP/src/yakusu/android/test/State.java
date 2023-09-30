package yakusu.android.test;

import org.json.JSONArray;
import org.json.JSONObject;

import yakusu.android.nlp.Goal;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mattia on 7/13/17.
 */

public class State {

    private int stateId;
    private int score;
    private List<Goal> goalsList;
    private List<Action> actionsList;
    //counts how many random goals were generated
    private int randomCount;
    private int skippedGoalCount;
    private String message;
    private boolean finished;
    private int drawerHeuristicCount;
    private int scrollHeuristicCount;

    public State(int stateId, int score, List<Goal> goalsList, List<Action> actionsList, int randomCount, int skippedGoalCount, String message, boolean finished, int drawerHeuristicCount, int scrollHeuristicCount) {
        this.stateId=stateId;
        this.score=score;
        this.goalsList=goalsList;
        this.actionsList=actionsList;
        this.randomCount=randomCount;
        this.skippedGoalCount=skippedGoalCount;
        this.message=message;
        this.finished=finished;
        this.drawerHeuristicCount=drawerHeuristicCount;
        this.scrollHeuristicCount=scrollHeuristicCount;
    }

    public int getStateId() {return stateId; }

    public int getScore(){
        return score;
    }

    public int getRandomCount() {return randomCount; }

    public void setRandomCount(int randomCount) { this.randomCount=randomCount; }

    public List<Action> getActionsList(){
        return actionsList;
    }

    public List<Goal> getGoalsList(){
        return goalsList;
    }

    public void setScore(int score){
        this.score=score;
    }

    public void setGoalsList(List<Goal> goalsList){
        this.goalsList=goalsList;
    }
    
    public int getSkippedGoalCount(){
        return skippedGoalCount;
    }

    public void setSkippedGoalCount(int skippedGoalCount){
        this.skippedGoalCount=skippedGoalCount;
    }

    public void setMessage(String message){
        this.message=message;
    }

    public boolean isFinished(){
        return finished;
    }

    public void setFinished(boolean finished){
        this.finished=finished;
    }
    
    public int getDrawerHeuristicCount(){
        return drawerHeuristicCount;
    }

    public void setDrawerHeuristicCount(int drawerHeuristicCount){
        this.drawerHeuristicCount=drawerHeuristicCount;
    }

    public int getScrollHeuristicCount(){
        return scrollHeuristicCount;
    }

    public void setScrollHeuristicCount(int scrollHeuristicCount){
        this.scrollHeuristicCount=scrollHeuristicCount;
    }
}
