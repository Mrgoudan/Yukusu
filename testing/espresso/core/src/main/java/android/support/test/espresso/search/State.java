package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

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

    public State copy(int newStateId){
        List<Goal> goalsListCopy = new ArrayList<Goal>();
        for(Goal goal:goalsList){
            goalsListCopy.add(goal.copy());
        }
        List<Action> actionsListCopy = new ArrayList<Action>();
        for(Action action:actionsList){
            actionsListCopy.add(action.copy());
        }

        State result = new State(newStateId, this.score, goalsListCopy, actionsListCopy, this.randomCount, this.skippedGoalCount, this.message, this.finished, this.drawerHeuristicCount, this.scrollHeuristicCount);
        return result;
    }

    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("id", this.stateId);
            result.put("score", this.score);
            result.put("random_count", this.randomCount);
            result.put("skipped_goal_count", this.skippedGoalCount);
            result.put("message", this.message);
            JSONArray goalsJSONArray = new JSONArray();
            for(Goal goal:this.goalsList){
                goalsJSONArray.put(goal.toJSON());
            }
            result.put("goals", goalsJSONArray);
            JSONArray actionsJSONArray = new JSONArray();
            for(Action action:this.actionsList){
                actionsJSONArray.put(action.toJSON());
            }
            result.put("actions", actionsJSONArray);
            result.put("finished", this.finished);
            result.put("drawer_heuristic_count", this.drawerHeuristicCount);
            result.put("scroll_heuristic_count", this.scrollHeuristicCount);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for state");
            throw new RuntimeException("could not create JSON for state", e);
        }
        return result;
    }

    public int getSkippedGoalCount(){
        return skippedGoalCount;
    }

    public void setSkippedGoalCount(int skippedGoalCount){
        this.skippedGoalCount=skippedGoalCount;
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

    public void setMessage(String message){
        this.message=message;
    }

    public boolean isFinished(){
        return finished;
    }

    public void setFinished(boolean finished){
        this.finished=finished;
    }
}
