/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.test.espresso.action;

import android.content.Context;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteractionModule;
import android.support.test.espresso.matcher.PreferenceMatchers;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.search.Action;
import android.support.test.espresso.search.ActionBack;
import android.support.test.espresso.search.ActionClick;
import android.support.test.espresso.search.ActionClickDouble;
import android.support.test.espresso.search.ActionClickLong;
import android.support.test.espresso.search.ActionDrawer;
import android.support.test.espresso.search.ActionKeyboardClose;
import android.support.test.espresso.search.ActionOpenOptions;
import android.support.test.espresso.search.ActionPreferenceClick;
import android.support.test.espresso.search.ActionPreferenceClickDouble;
import android.support.test.espresso.search.ActionPreferenceClickLong;
import android.support.test.espresso.search.ActionRotate;
import android.support.test.espresso.search.ActionScroll;
import android.support.test.espresso.search.ActionSleep;
import android.support.test.espresso.search.ActionSwipe;
import android.support.test.espresso.search.ActionType;
import android.support.test.espresso.search.ActivityUtils;
import android.support.test.espresso.search.Goal;
import android.support.test.espresso.search.GoalBack;
import android.support.test.espresso.search.GoalClick;
import android.support.test.espresso.search.GoalClickGeneral;
import android.support.test.espresso.search.GoalClickPosition;
import android.support.test.espresso.search.GoalOpenOptions;
import android.support.test.espresso.search.GoalRotate;
import android.support.test.espresso.search.GoalScroll;
import android.support.test.espresso.search.GoalScrollComplete;
import android.support.test.espresso.search.GoalSwipe;
import android.support.test.espresso.search.GoalType;
import android.support.test.espresso.search.GraphUtils;
import android.support.test.espresso.search.NodeView;
import android.support.test.espresso.search.NodeWindow;
import android.support.test.espresso.search.ScoredGraphTarget;
import android.support.test.espresso.search.ScoredHierarchyTarget;
import android.support.test.espresso.search.Selector;
import android.support.test.espresso.search.SelectorCoordinate;
import android.support.test.espresso.search.SelectorResourceId;
import android.support.test.espresso.search.SelectorText;
import android.support.test.espresso.search.SelectorXPath;
import android.support.test.espresso.search.State;
import android.support.test.espresso.search.TargetViewModel;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static org.hamcrest.Matchers.any;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;


/**
 * Enables pressing KeyEvents on views.
 */
public final class Search implements ViewAction {

  public static boolean KEYBOARD_IS_CLOSED = true;
  private static boolean KEYBOARD_CHECK_ENABLED = false;

  private final String CONFIG_FILE_NAME = "/sdcard/search_config.json";
  private final String GOALS_FILE_NAME = "/sdcard/goals.json";
  private final String STATES_FILE_NAME = "/sdcard/states.json";
  private final String STEPS_FILE_NAME = "/sdcard/steps.json";
  private final String INPUTS_FILE_NAME = "/sdcard/inputs.json";

  private final String CONSTRAINT_ANY = "any";

  private String graphAddress;
  private String serverAddress;
  private String graphToken;
  private String packageName;
  //private String firstActivityName;
  private int tagImageReferenceId = 0;

  private RequestQueue requestQueue;
  private final int requestTimeout = 120;//some graph queries require long time to execute
  private boolean requestCompleted = false;
  private JSONObject requestResult = null;
  private Random random;

  //search state bookkeeping
  private int nextStateId = 0;
  private List<Action> stepsList = null;
  private List<Goal> goalsList = null;
  private List<State> statesList = null;
  private List<State> statesProcessedList = null;
  private State currState = null;
  List<State> remainingStatesList = null;
  List<Goal> satisfiedGoalsList = null;
  List<Goal> remainingGoalsList = null;
  private Map<String, String> inputsMap = null;
  private boolean finished = false;
  private boolean crash = true;
  private boolean savedExecutionState = false;
  //not clean but works
  private Selector drawerViewSelector = null;
  private Selector scrollViewSelector = null;
  private int scrollCount = 0;
  private Selector optionsMenuSelector = null;

  //search parameters that can be changed
  private int RANDOM_COUNT_LIMIT = 10;
  private int SCROLL_COUNT_LIMIT = 20;
  private double MIN_SIMILARITY = 0.5;//used 0.7 in the past maybe 0.65 would work
  private double SKIP_SIMILARITY = 0.8;
  private int RANDOM_STRING_LENGTH = 8;
  private int GRAPH_MAX_DISTANCE = 10;

  private Context appContext;

  public Search(Context appContext) {
    this.appContext=appContext;
    this.requestQueue = Volley.newRequestQueue(this.appContext);
    this.random=new Random(0);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Matcher<View> getConstraints() {
    return any(View.class);
  }

  private int getNextStateId(){
    int nextStateIdTmp=this.nextStateId;
    this.nextStateId=this.nextStateId+1;
    return nextStateIdTmp;
  }

  private void readConfig(){
    try {
      FileInputStream fis = new FileInputStream(CONFIG_FILE_NAME);
      DataInputStream dis = new DataInputStream(fis);
      BufferedReader br = new BufferedReader(new InputStreamReader(dis));
      String content = "";
      String line = "";
      while ((line = br.readLine()) != null) {
        content = content + line;
      }
      JSONObject configJSON = new JSONObject(content);
      this.graphAddress = configJSON.getString("address");
      this.serverAddress = configJSON.getString("address");
      this.graphToken = configJSON.getString("token");
      this.packageName = configJSON.getString("package");
      this.crash = configJSON.getBoolean("crash");
      //search parameters that can be changed
      this.RANDOM_COUNT_LIMIT = configJSON.getInt("random_count_limit");
      this.SCROLL_COUNT_LIMIT = configJSON.getInt("scroll_count_limit");
      this.MIN_SIMILARITY = configJSON.getDouble("min_similarity");
      this.SKIP_SIMILARITY = configJSON.getDouble("skip_similarity");
      this.RANDOM_STRING_LENGTH = configJSON.getInt("random_string_length");
      this.GRAPH_MAX_DISTANCE = configJSON.getInt("graph_max_distance");
      //logging
      Log.d("Espresso", "graph address:"+this.graphAddress);
      Log.d("Espresso", "server address:"+this.serverAddress);
      Log.d("Espresso", "graph token:"+this.graphToken);
      Log.d("Espresso", "package name:"+this.packageName);
      Log.d("Espresso", "crash:"+this.crash);
      Log.d("Espresso", "random goal count limit:"+this.RANDOM_COUNT_LIMIT);
      Log.d("Espresso", "scroll heuristic count limit:"+this.SCROLL_COUNT_LIMIT);
      Log.d("Espresso", "min similarity:"+this.MIN_SIMILARITY);
      Log.d("Espresso", "similarity that allow to skip:"+this.SKIP_SIMILARITY);
      Log.d("Espresso", "random string length:"+this.RANDOM_STRING_LENGTH);
      Log.d("Espresso", "graph max distance:"+this.GRAPH_MAX_DISTANCE);

      try {
        this.tagImageReferenceId = ActivityUtils.getCurrentActivity().getResources().getIdentifier("TAG_IMAGE_REFERENCE", "id", ActivityUtils.getCurrentActivity().getPackageName());
      }
      catch(Exception e){
        Log.d("Espresso", "exception while getting id value for TAG_IMAGE_REFERENCE");
        throw new RuntimeException("exception while getting id value for TAG_IMAGE_REFERENCE", e);
      }
      if(tagImageReferenceId==0){
        Log.d("Espresso", "could not get id value for TAG_IMAGE_REFERENCE");
        throw new RuntimeException("could not get id value for TAG_IMAGE_REFERENCE");
      }
    }
    catch(Exception e){
      Log.d("Espresso", "could not read config");
      throw new RuntimeException("could not read config", e);
    }
  }

  private List<Goal> readGoals(){
    List<Goal> goalsList = new ArrayList<Goal>();
    try {
      FileInputStream fis = new FileInputStream(GOALS_FILE_NAME);
      DataInputStream dis = new DataInputStream(fis);
      BufferedReader br = new BufferedReader(new InputStreamReader(dis));
      String content = "";
      String line = "";
      while ((line = br.readLine()) != null) {
        content = content + line;
      }
      JSONObject goalsJSON = new JSONObject(content);
      JSONArray goalsArrayJSON = goalsJSON.getJSONArray("goals");
      for(int i=0; i<goalsArrayJSON.length(); ++i){
        JSONObject goalJSON = goalsArrayJSON.getJSONObject(i);
        goalsList.add(getGoalFromJSONObject(goalJSON));
      }
    }
    catch(Exception e){
      Log.d("Espresso", "could not read goals");
      throw new RuntimeException("could not read goals", e);
    }
    return goalsList;
  }

  private List<Action> readSteps(){
    List<Action> stepsList = new ArrayList<Action>();
    try {
      FileInputStream fis = new FileInputStream(STEPS_FILE_NAME);
      DataInputStream dis = new DataInputStream(fis);
      BufferedReader br = new BufferedReader(new InputStreamReader(dis));
      String content = "";
      String line = "";
      while ((line = br.readLine()) != null) {
        content = content + line;
      }
      JSONObject stepsJSON = new JSONObject(content);
      JSONArray stepsArrayJSON = stepsJSON.getJSONArray("steps");
      for(int i=0; i<stepsArrayJSON.length(); ++i){
        JSONObject stepJSON = stepsArrayJSON.getJSONObject(i);
        stepsList.add(getActionFromJSONObject(stepJSON));
      }
      br.close();
    }
    catch(Exception e){
      Log.d("Espresso", "could not read steps");
      throw new RuntimeException("could not read steps", e);
    }
    return stepsList;
  }

  private Map<String, String> readInputs(){
    Map<String, String> inputsMap = new HashMap<String, String>();
    try {
      FileInputStream fis = new FileInputStream(INPUTS_FILE_NAME);
      DataInputStream dis = new DataInputStream(fis);
      BufferedReader br = new BufferedReader(new InputStreamReader(dis));
      String content = "";
      String line = "";
      while ((line = br.readLine()) != null) {
        content = content + line;
      }
      JSONObject inputsJSON = new JSONObject(content);
      JSONArray inputsArrayJSON = inputsJSON.getJSONArray("inputs");
      for(int i=0; i<inputsArrayJSON.length(); ++i){
        JSONObject inputJSON = inputsArrayJSON.getJSONObject(i);
        String key = inputJSON.getString("key");
        String value = inputJSON.getString("value");
        Log.d("Espresso", "key:"+key+"#"+"value:"+value);
        inputsMap.put(key,value);
      }
    }
    catch(Exception e){
      Log.d("Espresso", "could not read inputs");
      throw new RuntimeException("could not read inputs", e);
    }
    return inputsMap;
  }

  private List<State> readStates(){
    List<State> statesList = new ArrayList<State>();
    try {
      FileInputStream fis = new FileInputStream(STATES_FILE_NAME);
      DataInputStream dis = new DataInputStream(fis);
      BufferedReader br = new BufferedReader(new InputStreamReader(dis));
      String content = "";
      String line = "";
      while ((line = br.readLine()) != null) {
        content = content + line;
      }
      JSONObject statesJSON = new JSONObject(content);
      JSONArray statesArrayJSON = statesJSON.getJSONArray("states");
      for(int i=0; i<statesArrayJSON.length(); ++i){
        JSONObject stateJSON = statesArrayJSON.getJSONObject(i);
        int id = stateJSON.getInt("id");
        int score = stateJSON.getInt("score");
        int randomCount = stateJSON.getInt("random_count");
        List<Goal> goalsList = new ArrayList<Goal>();
        JSONArray goalsArrayJSON = stateJSON.getJSONArray("goals");
        for(int j=0; j<goalsArrayJSON.length(); ++j){
          Goal goal = getGoalFromJSONObject(goalsArrayJSON.getJSONObject(j));
          goalsList.add(goal);
        }
        List<Action> actionsList = new ArrayList<Action>();
        JSONArray actionsArrayJSON = stateJSON.getJSONArray("actions");
        for(int j=0; j<actionsArrayJSON.length(); ++j){
          Action action = getActionFromJSONObject(actionsArrayJSON.getJSONObject(j));
          actionsList.add(action);
        }
        int skippedGoalCount = stateJSON.getInt("skipped_goal_count");
        String message = stateJSON.getString("message");
        boolean finished = stateJSON.getBoolean("finished");
        int drawerHeuristicCount = stateJSON.getInt("drawer_heuristic_count");
        int scrollHeuristicCount = stateJSON.getInt("scroll_heuristic_count");
        State state = new State(id, score, goalsList, actionsList, randomCount, skippedGoalCount, message, finished, drawerHeuristicCount, scrollHeuristicCount);
        statesList.add(state);
      }
    }
    catch(Exception e){
      Log.d("Espresso", "could not read states");
      throw new RuntimeException("could not read states", e);
    }
    return statesList;
  }

  private List<State> readStatesProcessed(){
    List<State> statesList = new ArrayList<State>();
    try {
      FileInputStream fis = new FileInputStream(STATES_FILE_NAME);
      DataInputStream dis = new DataInputStream(fis);
      BufferedReader br = new BufferedReader(new InputStreamReader(dis));
      String content = "";
      String line = "";
      while ((line = br.readLine()) != null) {
        content = content + line;
      }
      JSONObject statesJSON = new JSONObject(content);
      JSONArray statesArrayJSON = statesJSON.getJSONArray("states_processed");
      for(int i=0; i<statesArrayJSON.length(); ++i){
        JSONObject stateJSON = statesArrayJSON.getJSONObject(i);
        int id = stateJSON.getInt("id");
        int score = stateJSON.getInt("score");
        int randomCount = stateJSON.getInt("random_count");
        List<Goal> goalsList = new ArrayList<Goal>();
        JSONArray goalsArrayJSON = stateJSON.getJSONArray("goals");
        for(int j=0; j<goalsArrayJSON.length(); ++j){
          Goal goal = getGoalFromJSONObject(goalsArrayJSON.getJSONObject(j));
          goalsList.add(goal);
        }
        List<Action> actionsList = new ArrayList<Action>();
        JSONArray actionsArrayJSON = stateJSON.getJSONArray("actions");
        for(int j=0; j<actionsArrayJSON.length(); ++j){
          Action action = getActionFromJSONObject(actionsArrayJSON.getJSONObject(j));
          actionsList.add(action);
        }
        int skippedGoalCount = stateJSON.getInt("skipped_goal_count");
        String message = stateJSON.getString("message");
        boolean finished = stateJSON.getBoolean("finished");
        int drawerHeuristicCount = stateJSON.getInt("drawer_heuristic_count");
        int scrollHeuristicCount = stateJSON.getInt("scroll_heuristic_count");
        State state = new State(id, score, goalsList, actionsList, randomCount, skippedGoalCount, message, finished, drawerHeuristicCount, scrollHeuristicCount);
        statesList.add(state);
      }
      br.close();
    }
    catch(Exception e){
      Log.d("Espresso", "could not read states");
      throw new RuntimeException("could not read states", e);
    }
    return statesList;
  }

  private Action getActionFromJSONObject(JSONObject actionJSON){
    Action action = null;
    try {
      String actionType = actionJSON.getString("type");
      if(actionType.equals("click")){
        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
        action = new ActionClick(getSelectorFromJSONObject(selectorJSON));
      }
      else if(actionType.equals("click_long")){
        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
        action = new ActionClickLong(getSelectorFromJSONObject(selectorJSON));
      }
      else if(actionType.equals("click_double")){
        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
        action = new ActionClickDouble(getSelectorFromJSONObject(selectorJSON));
      }
      else if(actionType.equals("scroll")){
        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
        int direction = actionJSON.getInt("direction");
        action = new ActionScroll(getSelectorFromJSONObject(selectorJSON), direction);
      }
      else if(actionType.equals("swipe")){
        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
        int direction = actionJSON.getInt("direction");
        action = new ActionSwipe(getSelectorFromJSONObject(selectorJSON), direction);
      }
      else if(actionType.equals("drawer")){
        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
        int direction = actionJSON.getInt("direction");
        action = new ActionDrawer(getSelectorFromJSONObject(selectorJSON), direction);
      }
      else if(actionType.equals("type")){
        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
        String text = actionJSON.getString("text");
        action = new ActionType(getSelectorFromJSONObject(selectorJSON), text);
      }
      else if(actionType.equals("preference_click")){
        String text = actionJSON.getString("text");
        action = new ActionPreferenceClick(text);
      }
      else if(actionType.equals("preference_click_long")){
        String text = actionJSON.getString("text");
        action = new ActionPreferenceClickLong(text);
      }
      else if(actionType.equals("preference_click_double")){
        String text = actionJSON.getString("text");
        action = new ActionPreferenceClickDouble(text);
      }
      else if(actionType.equals("open_options")){
        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
        action = new ActionOpenOptions(getSelectorFromJSONObject(selectorJSON));
      }
      else if(actionType.equals("rotate")){
        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
        action = new ActionRotate(getSelectorFromJSONObject(selectorJSON));
      }
      else if(actionType.equals("back")){
        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
        action = new ActionBack(getSelectorFromJSONObject(selectorJSON));
      }
      else if(actionType.equals("sleep")){
        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
        long time = actionJSON.getLong("time");
        action = new ActionSleep(getSelectorFromJSONObject(selectorJSON), time);
      }
      else if(actionType.equals("keyboard_close")){
        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
        action = new ActionKeyboardClose(getSelectorFromJSONObject(selectorJSON));
      }
      else{
        Log.d("Espresso", "handle action type while getting action from JSON object");
        throw new RuntimeException("handle action type while getting action from JSON object");
      }
    }
    catch(Exception e){
      Log.d("Espresso", "could not get action from JSON object");
      throw new RuntimeException("could not get action from JSON object", e);
    }
    return action;
  }

  private Selector getSelectorFromJSONObject(JSONObject selectorJSON){
    Selector selector = null;
    try {
      String selectorType = selectorJSON.getString("type");
      if (selectorType.equals("coordinate")) {
        int x = selectorJSON.getInt("x");
        int y = selectorJSON.getInt("y");
        selector = new SelectorCoordinate(x, y);
      }
      else if (selectorType.equals("resource_id")){
        String resourceId = selectorJSON.getString("resource_id");
        selector = new SelectorResourceId(resourceId);
      }
      else if (selectorType.equals("text")){
        String text = selectorJSON.getString("text");
        selector = new SelectorText(text);
      }
      else if (selectorType.equals("xpath")){
        String xPath = selectorJSON.getString("xpath");
        selector = new SelectorXPath(xPath);
      }
      else {
        Log.d("Espresso", "handle selector type while getting selector from JSON object");
        throw new RuntimeException("handle selector type while getting selector from JSON object");
      }
    }
    catch(Exception e){
      Log.d("Espresso", "could not get selector from JSON object");
      throw new RuntimeException("could not get selector from JSON object", e);
    }
    return selector;
  }

  private TargetViewModel getTargetViewModelFromJSONObject(JSONObject targetViewModelJSON){
    TargetViewModel targetViewModel = null;
    try {
      String text = targetViewModelJSON.getString("text");
      String resourceId = targetViewModelJSON.getString("resource_id");
      String resourceIdReference = targetViewModelJSON.getString("resource_id_reference");
      List<String> resourceIdReferenceKeywordsList = new ArrayList<String>();
      for(int i=0;i<targetViewModelJSON.getJSONArray("resource_id_reference_keywords").length(); ++i){
        resourceIdReferenceKeywordsList.add(targetViewModelJSON.getJSONArray("resource_id_reference_keywords").getString(i));
      }
      String imageReference = targetViewModelJSON.getString("image_reference");
      List<String> imageReferenceKeywordsList = new ArrayList<String>();
      for(int i=0;i<targetViewModelJSON.getJSONArray("image_reference_keywords").length(); ++i){
        imageReferenceKeywordsList.add(targetViewModelJSON.getJSONArray("image_reference_keywords").getString(i));
      }
      String xPath = targetViewModelJSON.getString("xpath");

      boolean precise = targetViewModelJSON.getBoolean("precise");
      boolean fromText = targetViewModelJSON.getBoolean("from_text");
      boolean canBePreference = targetViewModelJSON.getBoolean("can_be_preference");
      targetViewModel = new TargetViewModel(text, resourceId, resourceIdReference, resourceIdReferenceKeywordsList, imageReference, imageReferenceKeywordsList, xPath, precise, fromText, canBePreference);
    }
    catch(Exception e){
      Log.d("Espresso", "could not get target view model from JSON object");
      throw new RuntimeException("could not get target view model from JSON object", e);
    }
    return targetViewModel;
  }

  private Goal getGoalFromJSONObject(JSONObject goalJSON){
    Goal goal = null;
    try {
      String goalType = goalJSON.getString("type");
      if(goalType.equals("click")){
        boolean satisfied = goalJSON.getBoolean("satisfied");
        boolean fromGraph = goalJSON.getBoolean("from_graph");
        TargetViewModel targetViewModel = getTargetViewModelFromJSONObject(goalJSON.getJSONObject("target"));
        int duration = goalJSON.getInt("duration");
        int randomCount = goalJSON.getInt("random_count");
        goal = new GoalClick(satisfied, fromGraph, targetViewModel, duration, randomCount);
      }
      else if(goalType.equals("click_general")){
        boolean satisfied = goalJSON.getBoolean("satisfied");
        boolean fromGraph = goalJSON.getBoolean("from_graph");
        TargetViewModel targetViewModel = getTargetViewModelFromJSONObject(goalJSON.getJSONObject("target"));
        int duration = goalJSON.getInt("duration");
        goal = new GoalClickGeneral(satisfied, fromGraph, targetViewModel, duration);
      }
      else if(goalType.equals("click_position")){
        boolean satisfied = goalJSON.getBoolean("satisfied");
        boolean fromGraph = goalJSON.getBoolean("from_graph");
        int duration = goalJSON.getInt("duration");
        int position = goalJSON.getInt("position");
        String container = goalJSON.getString("container");
        goal = new GoalClickPosition(satisfied, fromGraph, position, container, duration);
      }
      else if(goalType.equals("scroll")){
        boolean satisfied = goalJSON.getBoolean("satisfied");
        boolean fromGraph = goalJSON.getBoolean("from_graph");
        int direction = goalJSON.getInt("direction");
        goal = new GoalScroll(satisfied, fromGraph, direction);
      }
      else if(goalType.equals("scroll_complete")){
        boolean satisfied = goalJSON.getBoolean("satisfied");
        boolean fromGraph = goalJSON.getBoolean("from_graph");
        int direction = goalJSON.getInt("direction");
        goal = new GoalScrollComplete(satisfied, fromGraph, direction);
      }
      else if(goalType.equals("swipe")){
        boolean satisfied = goalJSON.getBoolean("satisfied");
        boolean fromGraph = goalJSON.getBoolean("from_graph");
        int direction = goalJSON.getInt("direction");
        goal = new GoalSwipe(satisfied, fromGraph, direction);
      }
      else if(goalType.equals("open_options")){
        boolean satisfied = goalJSON.getBoolean("satisfied");
        boolean fromGraph = goalJSON.getBoolean("from_graph");
        goal = new GoalOpenOptions(satisfied, fromGraph);
      }
      else if(goalType.equals("rotate")){
        boolean satisfied = goalJSON.getBoolean("satisfied");
        boolean fromGraph = goalJSON.getBoolean("from_graph");
        goal = new GoalRotate(satisfied, fromGraph);
      }
      else if(goalType.equals("back")){
        boolean satisfied = goalJSON.getBoolean("satisfied");
        boolean fromGraph = goalJSON.getBoolean("from_graph");
        goal = new GoalBack(satisfied, fromGraph);
      }
      else if(goalType.equals("type")){
        boolean satisfied = goalJSON.getBoolean("satisfied");
        boolean fromGraph = goalJSON.getBoolean("from_graph");
        TargetViewModel targetViewModel = getTargetViewModelFromJSONObject(goalJSON.getJSONObject("target"));
        String text = goalJSON.getString("text");
        int randomCount = goalJSON.getInt("random_count");
        goal = new GoalType(satisfied, fromGraph, targetViewModel, text, randomCount);
      }
      else{
        Log.d("Espresso", "handle goal type while getting goal from JSON object");
        throw new RuntimeException("handle goal type while getting goal from JSON object");
      }
    }
    catch(Exception e){
      Log.d("Espresso", "could not get goal from JSON object");
      throw new RuntimeException("could not get goal from JSON object", e);
    }
    return goal;
  }

  private State selectCurrState(List<State> statesList){
    State selectedState = null;
    int maxScore = Integer.MIN_VALUE;
    for(State state:statesList){
      if(state.getScore()>maxScore){
        maxScore = state.getScore();
      }
    }
    List<State> maxStatesList = new ArrayList<State>();
    for(State state:statesList){
      if(state.getScore()==maxScore){
        maxStatesList.add(state);
      }
    }
    int selectedStateIndex = random.nextInt(maxStatesList.size());
    selectedState = maxStatesList.get(selectedStateIndex);
    return selectedState;
  }

  private void restoreState(UiController uiController, List<Action> actionsToRestoreState){
    while(!actionsToRestoreState.isEmpty()){
      Action currAction = actionsToRestoreState.remove(0);
      performAction(uiController, currAction, false);
    }
  }

  private void saveExecutionState(){
    Log.d("Espresso", "saving execution state");
    try {
      JSONObject statesJSON = new JSONObject();
      JSONArray statesJSONArray = new JSONArray();
      for (State remainingState : this.remainingStatesList) {
        statesJSONArray.put(remainingState.toJSON());
      }
      //save all processed states and current state in processed state
      JSONArray statesProcessedJSONArray = new JSONArray();
      for (State processedState : this.statesProcessedList) {
        statesProcessedJSONArray.put(processedState.toJSON());
      }
      //set if current state has finished
      currState.setFinished(this.finished);
      statesProcessedJSONArray.put(currState.toJSON());
      statesJSON.put("states_processed", statesProcessedJSONArray);
      statesJSON.put("finished", this.finished);
      statesJSON.put("states", statesJSONArray);
      FileWriter statesFileWriter = new FileWriter(STATES_FILE_NAME);
      statesFileWriter.write(statesJSON.toString());
      statesFileWriter.close();
    }
    catch(Exception e){
      Log.d("Espresso", "could not save execution state");
      throw new RuntimeException("could not save execution state", e);
    }
    this.savedExecutionState=true;
  }

  private void processStackTraceToCheckCrashInApp(Exception e) {
    Log.d("Espresso", "processStackTraceToCheckCrashInApp");
    boolean isExceptionFromApp = false;
    StringWriter errors = new StringWriter();
    PrintWriter pw = new PrintWriter(errors);
    e.printStackTrace(pw);
    //mf: this should not be needed but there is some problem when there is an exception in jni
    if(e.getCause()!=null){
      e.getCause().printStackTrace(pw);
    }
    if(errors.toString().contains(this.packageName)
      || errors.toString().contains("android.support.v7")){
      isExceptionFromApp = true;
    }
    Log.d("Espresso", "exception:"+errors.toString());
    Log.d("Espresso", "exception from app:"+isExceptionFromApp);
    int remainingSize = remainingGoalsList.size();
    boolean allGeneralLeft = true;
    for(Goal goal:remainingGoalsList){
      if(!(goal instanceof GoalClickGeneral)){
        allGeneralLeft = false;
        break;
      }
    }
    if(allGeneralLeft){
      remainingSize = 0;
    }
    Log.d("Espresso", "remaining size:"+remainingSize);
    //stop search only if no goals left and crash from app
    if(isExceptionFromApp && remainingSize==0) {
      this.finished = true;
    }
    this.currState.setMessage(errors.toString());
    saveExecutionState();
    //throw runtime exception so that exploration stops
    throw new RuntimeException(e);
  }


  private void processStackTraceToCheckOutOfApp(Exception e) {
    Log.d("Espresso", "processStackTraceToCheckOutOfApp");
    boolean isOutOfApp = false;
    StringWriter errors = new StringWriter();
    PrintWriter pw = new PrintWriter(errors);
    e.printStackTrace(pw);
    //mf: this should not be needed but there is some problem when there is an exception in jni
    if(e.getCause()!=null){
      e.getCause().printStackTrace(pw);
    }
    if(errors.toString().contains("InjectEventSecurityException")){
      isOutOfApp = true;
    }
    Log.d("Espresso", "exception:"+errors.toString());
    Log.d("Espresso", "exception because out of app:"+isOutOfApp);
    //order of the next statements is important
    if(isOutOfApp){
      //duplicate and remove
      duplicateStateRandomException();
    }
    this.currState.setMessage(errors.toString());
    saveExecutionState();
    //throw runtime exception so that exploration stops
    throw new RuntimeException(e);
  }

  private void performAction(UiController uiController, Action action, boolean isRandom) {
    try {
      if (action instanceof ActionClick) {
        ActionClick actionClick = (ActionClick) action;
        Log.d("Espresso", "performing click on:"+actionClick.getSelector());
        View view = findViewFromSelector(actionClick.getSelector());
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.click());
        //new GeneralClickAction(Tap.SINGLE, GeneralLocation.CENTER, Press.FINGER).perform(uiController, view);
      }
      else if (action instanceof ActionClickLong) {
        ActionClickLong actionClickLong = (ActionClickLong) action;
        Log.d("Espresso", "performing click long on:"+actionClickLong.getSelector());
        View view = findViewFromSelector(actionClickLong.getSelector());
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.longClick());
        //new GeneralClickAction(Tap.LONG, GeneralLocation.CENTER, Press.FINGER).perform(uiController, view);
      }
      else if (action instanceof ActionClickDouble) {
        ActionClickDouble actionClickDouble = (ActionClickDouble) action;
        Log.d("Espresso", "performing click double on:"+actionClickDouble.getSelector());
        View view = findViewFromSelector(actionClickDouble.getSelector());
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.doubleClick());
        //new GeneralClickAction(Tap.LONG, GeneralLocation.CENTER, Press.FINGER).perform(uiController, view);
      }
      else if (action instanceof ActionScroll){
        ActionScroll actionScroll = (ActionScroll) action;
        Log.d("Espresso", "performing scroll on:"+actionScroll.getSelector());
        View view = findViewFromSelector(actionScroll.getSelector());
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.scroll(actionScroll.getDirection()));
      }
      else if (action instanceof ActionSwipe){
        ActionSwipe actionSwipe = (ActionSwipe) action;
        Log.d("Espresso", "performing swipe on:"+actionSwipe.getSelector());
        View view = findViewFromSelector(actionSwipe.getSelector());
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.swipe(actionSwipe.getDirection()));
      }
      else if (action instanceof ActionDrawer){
        ActionDrawer actionDrawer = (ActionDrawer) action;
        Log.d("Espresso", "performing drawer on:"+actionDrawer.getSelector());
        View view = findViewFromSelector(actionDrawer.getSelector());
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.drawer(actionDrawer.getDirection()));
      }
      else if (action instanceof ActionType) {
        ActionType actionType = (ActionType) action;
        Log.d("Espresso", "performing type on:"+actionType.getSelector());
        View view = findViewFromSelector(actionType.getSelector());
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.typeText(actionType.getText()));
        //new TypeTextAction(actionType.getText()).perform(uiController, view);
      }
      else if (action instanceof ActionPreferenceClick) {
        Log.d("Espresso", "performing preference click");
        ActionPreferenceClick actionPreferenceClick = (ActionPreferenceClick) action;
        new DataInteraction(PreferenceMatchers.withTitleText(actionPreferenceClick.getText())).performNew(uiController, ViewActions.click());
      }
      else if (action instanceof ActionPreferenceClickLong) {
        Log.d("Espresso", "performing preference click long");
        ActionPreferenceClickLong actionPreferenceClickLong = (ActionPreferenceClickLong) action;
        new DataInteraction(PreferenceMatchers.withTitleText(actionPreferenceClickLong.getText())).performNew(uiController, ViewActions.longClick());
      }
      else if (action instanceof ActionPreferenceClickDouble) {
        Log.d("Espresso", "performing preference click double");
        ActionPreferenceClickDouble actionPreferenceClickDouble = (ActionPreferenceClickDouble) action;
        new DataInteraction(PreferenceMatchers.withTitleText(actionPreferenceClickDouble.getText())).performNew(uiController, ViewActions.doubleClick());
      }
      else if (action instanceof ActionOpenOptions){
        Log.d("Espresso", "performing open options");
        ActionOpenOptions actionOpenOptions = (ActionOpenOptions) action;
        View view = findViewFromSelector(actionOpenOptions.getSelector());
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.pressMenuKey());
        //new KeyEventAction(new EspressoKey.Builder().withKeyCode(KeyEvent.KEYCODE_MENU).build()).perform(uiController, view);
        //Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.openOptions());
        //new OpenOptions().perform(uiController, view);
      }
      else if (action instanceof ActionRotate) {
        ActionRotate actionRotate = (ActionRotate) action;
        Log.d("Espresso", "performing action rotate:"+actionRotate.getSelector());
        View view = findViewFromSelector(actionRotate.getSelector());
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.rotate());
        //new Rotate().perform(uiController, view);
      }
      else if (action instanceof ActionBack) {
        ActionBack actionBack = (ActionBack) action;
        Log.d("Espresso", "performing action back:"+actionBack.getSelector());
        View view = findViewFromSelector(actionBack.getSelector());
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.pressBack());
        //new KeyEventAction(new EspressoKey.Builder().withKeyCode(KeyEvent.KEYCODE_BACK).build()).performNew(uiController, view);
      }
      //this action does not have goal yet
      else if (action instanceof ActionSleep) {
        ActionSleep actionSleep = (ActionSleep) action;
        Log.d("Espresso", "performing action sleep:"+actionSleep.getSelector());
        View view = findViewFromSelector(actionSleep.getSelector());
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.sleep(actionSleep.getTime()));
        //new Sleep(actionSleep.getTime()).perform(uiController, view);
      }
      else if (action instanceof ActionKeyboardClose) {
        ActionKeyboardClose actionKeyboardClose = (ActionKeyboardClose) action;
        Log.d("Espresso", "performing action keyboard close:"+actionKeyboardClose.getSelector());
        View view = findViewFromSelector(actionKeyboardClose.getSelector());
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(view))).viewInteraction().performNew(ViewActions.closeSoftKeyboard());
        //new CloseKeyboardAction().perform(uiController, view);
        Search.KEYBOARD_IS_CLOSED=true;
      }
      else {
        Log.d("Espresso", "handle action type while performing action");
        throw new RuntimeException("handle action type while performing action");
      }
    }
    catch(Exception e){
      if(isRandom) {
        processStackTraceToCheckOutOfApp(e);
      }
      processStackTraceToCheckCrashInApp(e);
    }
    //try to check if keyboard was opened
    closeKeyboard();
  }


  private String computeXPathFromView(View view){
    String result = "";
    //mf: changed
    //int[] locationOnScreen = new int[2];
    //view.getLocationOnScreen(locationOnScreen);
    //int viewLeft = locationOnScreen[0];
    //int viewTop = locationOnScreen[1];
    //int viewWidth = view.getWidth();
    //int viewHeight = view.getHeight();
    Rect viewGlobalVisibleRect = new Rect();
    view.getGlobalVisibleRect(viewGlobalVisibleRect);
    int viewLeft = viewGlobalVisibleRect.left;
    int viewTop = viewGlobalVisibleRect.top;
    int viewWidth = viewGlobalVisibleRect.right-viewGlobalVisibleRect.left;
    int viewHeight = viewGlobalVisibleRect.bottom-viewGlobalVisibleRect.top;
    String viewClassName = view.getClass().getName();

    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when computing xpath");
      throw new RuntimeException("root view is not visible when computing xpath");
    }
    ArrayList<View> workList = new ArrayList<View>();
    ArrayList<String> xpathWorkList = new ArrayList<String>();
    workList.add(rootView);
    String rootXPath = "/" + rootView.getClass().getName() + "[1]";
    xpathWorkList.add(rootXPath);
    while (!workList.isEmpty()) {
      //update view
      viewGlobalVisibleRect = new Rect();
      view.getGlobalVisibleRect(viewGlobalVisibleRect);
      viewLeft = viewGlobalVisibleRect.left;
      viewTop = viewGlobalVisibleRect.top;
      viewWidth = viewGlobalVisibleRect.right-viewGlobalVisibleRect.left;
      viewHeight = viewGlobalVisibleRect.bottom-viewGlobalVisibleRect.top;
      viewClassName = view.getClass().getName();
      //Log.d("Espresso", "view#left:"+viewLeft+"#top:"+viewTop+"#width:"+viewWidth+"#height:"+viewHeight+"#class:"+viewClassName);
      //get curr view
      View currView = workList.remove(0);
      String currViewXPath = xpathWorkList.remove(0);
      //mf: changed
      //int[] currViewLocationOnScreen = new int[2];
      //currView.getLocationOnScreen(currViewLocationOnScreen);
      //int currViewLeft = currViewLocationOnScreen[0];
      //int currViewTop = currViewLocationOnScreen[1];
      //int currViewWidth = currView.getWidth();
      //int currViewHeight = currView.getHeight();
      Rect currViewGlobalVisibleRect = new Rect();
      currView.getGlobalVisibleRect(currViewGlobalVisibleRect);
      int currViewLeft = currViewGlobalVisibleRect.left;
      int currViewTop = currViewGlobalVisibleRect.top;
      int currViewWidth = currViewGlobalVisibleRect.right-currViewGlobalVisibleRect.left;
      int currViewHeight = currViewGlobalVisibleRect.bottom-currViewGlobalVisibleRect.top;
      String currViewClassName = currView.getClass().getName();
      //Log.d("Espresso", "currView#left:"+currViewLeft+"#top:"+currViewTop+"#width:"+currViewWidth+"#height:"+currViewHeight+"#class:"+currViewClassName);
      if((currViewLeft==viewLeft) && (currViewTop==viewTop) && (currViewWidth==viewWidth) && (currViewHeight==viewHeight) && (currViewClassName.equals(viewClassName))){
          result = currViewXPath;
          break;
      }
      if(currView instanceof ViewGroup) {
        ViewGroup currViewGroup = (ViewGroup) currView;
        ArrayList<View> childViewList = new ArrayList<View>();
        for (int i = 0; i < currViewGroup.getChildCount(); ++i) {
          if (currViewGroup.getChildAt(i)!=null && currViewGroup.getChildAt(i).getVisibility()==View.VISIBLE && currViewGroup.getChildAt(i).getGlobalVisibleRect(new Rect())) {
            childViewList.add(currViewGroup.getChildAt(i));
          }
        }
        if(childViewList.size() > 1) {
          for (int i=0; i<childViewList.size(); ++i) {
            int childViewXPathIndex = i + 1;
            String childViewXPath = currViewXPath + "/" + childViewList.get(i).getClass().getName() + "[" + childViewXPathIndex + "]";
            workList.add(childViewList.get(i));
            xpathWorkList.add(childViewXPath);
          }
        }
        else if (childViewList.size() == 1) {
          String childViewXPath = currViewXPath + "/" + childViewList.get(0).getClass().getName() + "[1]";
          workList.add(childViewList.get(0));
          xpathWorkList.add(childViewXPath);
        }
      }
    }
    if(result.equals("")){
      Log.d("Espresso", "did not compute xpath correctly#left:"+viewLeft+"#top:"+viewTop+"#width:"+viewWidth+"#height:"+viewHeight+"#class:"+viewClassName);
      throw new RuntimeException("did not compute xpath correctly");
    }
    return result;
  }

  //mf: finds a view based on the selector
  private View findViewFromSelector(Selector selector){
    View result = null;
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when finding selector");
      throw new RuntimeException("root view is not visible when finding selector");
    }
    if(selector instanceof SelectorCoordinate){
      //mf: choosing the leaf view that contains the coordinates
      SelectorCoordinate selectorCoordinate = (SelectorCoordinate) selector;
      List<View> workList = new ArrayList<View>();
      workList.add(rootView);
      List<View> resultsList = new ArrayList<View>();
      while(!workList.isEmpty()) {
        View currView = workList.remove(0);
        if(currView instanceof ViewGroup){
          ViewGroup currViewGroup = (ViewGroup) currView;
          for(int i=0; i<currViewGroup.getChildCount(); ++i){
            View currChildView = currViewGroup.getChildAt(i);
            if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
              workList.add(currChildView);
            }
          }
        }
        else{
          //the curr view is a child
          Rect viewBoundaries = new Rect();
          currView.getGlobalVisibleRect(viewBoundaries);
          if(viewBoundaries.left<=selectorCoordinate.getX() &&
                  viewBoundaries.right>=selectorCoordinate.getX() &&
                  viewBoundaries.top<=selectorCoordinate.getY() &&
                  viewBoundaries.bottom>=selectorCoordinate.getY()){
            resultsList.add(currView);
          }
        }
      }
      if(resultsList.size()==1){
        result = resultsList.get(0);
      }
      else if(resultsList.size()>1){
        Log.d("Espresso", "handle case in which more than one leaf view contains coordinates");
        throw new RuntimeException("handle case in which more than one leaf view contains coordinates");
      }
      else{
        Log.d("Espresso", "handle case in which no leaf contains coordinates");
        throw new RuntimeException("handle case in which no leaf contains coordinates");
      }
    }
    else if(selector instanceof SelectorResourceId){
      SelectorResourceId selectorResourceId = (SelectorResourceId) selector;
      List<View> workList = new ArrayList<View>();
      workList.add(rootView);
      List<View> resultsList = new ArrayList<View>();
      while(!workList.isEmpty()) {
        View currView = workList.remove(0);
        String resourceId = null;
        try {
          resourceId = currView.getResources().getResourceName(currView.getId());
          if(resourceId.startsWith(this.packageName)){
            resourceId = this.packageName + ".R.id." + resourceId.substring(resourceId.indexOf(":id/")+4);
          }
          else{
            resourceId = resourceId.replace(":id/", ".R.id.");
          }
        }
        catch(Exception e){

        }
        if(resourceId!=null && resourceId.equals(selectorResourceId.getResourceId())){
          resultsList.add(currView);
        }
        if(currView instanceof ViewGroup){
          ViewGroup currViewGroup = (ViewGroup) currView;
          for(int i=0; i<currViewGroup.getChildCount(); ++i){
            View currChildView = currViewGroup.getChildAt(i);
            if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
              workList.add(currChildView);
            }
          }
        }
      }
      if(resultsList.size()==1){
        result = resultsList.get(0);
      }
      else if(resultsList.size()>1){
        Log.d("Espresso", "handle case in which more than one view is equal to the resource id:"+selectorResourceId.getResourceId());
        throw new RuntimeException("handle case in which more than one view is equal to the resource id:"+selectorResourceId.getResourceId());
      }
      else{
        Log.d("Espresso", "handle case in which more no view is equal to the resource id"+selectorResourceId.getResourceId());
        throw new RuntimeException("handle case in which more no view is equal to the resource id"+selectorResourceId.getResourceId());
      }
    }
    else if(selector instanceof SelectorText){
      SelectorText selectorText = (SelectorText) selector;
      List<View> workList = new ArrayList<View>();
      workList.add(rootView);
      List<View> resultsList = new ArrayList<View>();
      while(!workList.isEmpty()) {
        View currView = workList.remove(0);
        String text = "";
        if(currView instanceof TextView){
          TextView textView = (TextView) currView;
          text = textView.getText() != null ? textView.getText().toString() : "";
        }
        if(!text.equals("") && text.equals(selectorText.getText())){
          resultsList.add(currView);
        }
        if(currView instanceof ViewGroup){
          ViewGroup currViewGroup = (ViewGroup) currView;
          for(int i=0; i<currViewGroup.getChildCount(); ++i){
            View currChildView = currViewGroup.getChildAt(i);
            if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
              workList.add(currChildView);
            }
          }
        }
      }
      if(resultsList.size()==1){
        result = resultsList.get(0);
      }
      else if(resultsList.size()>1){
        Log.d("Espresso", "handle case in which more than one view is equal to the text:"+selectorText.getText());
        throw new RuntimeException("handle case in which more than one view is equal to the text:"+selectorText.getText());
      }
      else{
        Log.d("Espresso", "handle case in which more no view is equal to the text:"+selectorText.getText());
        throw new RuntimeException("handle case in which more no view is equal to the text:"+selectorText.getText());
      }
    }
    else if(selector instanceof SelectorXPath){
      SelectorXPath selectorXPath = (SelectorXPath) selector;
      List<View> workList = new ArrayList<View>();
      workList.add(rootView);
      List<View> resultsList = new ArrayList<View>();
      while(!workList.isEmpty()) {
        View currView = workList.remove(0);
        String currViewXPath = computeXPathFromView(currView);
        if(currViewXPath.equals(selectorXPath.getXPath())){
          resultsList.add(currView);
        }
        if(currView instanceof ViewGroup){
          ViewGroup currViewGroup = (ViewGroup) currView;
          for(int i=0; i<currViewGroup.getChildCount(); ++i){
            View currChildView = currViewGroup.getChildAt(i);
            if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
              workList.add(currChildView);
            }
          }
        }
      }
      if(resultsList.size()==1){
        result = resultsList.get(0);
      }
      else if(resultsList.size()>1){
        Log.d("Espresso", "handle case in which more than one view is equal to xpath:"+selectorXPath.getXPath());
        throw new RuntimeException("handle case in which more than one view is equal to xpath:"+selectorXPath.getXPath());
      }
      else{
        Log.d("Espresso", "handle case in which more no view is equal to xpath:"+selectorXPath.getXPath());
        throw new RuntimeException("handle case in which more no view is equal to xpath:"+selectorXPath.getXPath());
      }
    }
    else{
      Log.d("Espresso", "handle selector type while finding view from selector");
      throw new RuntimeException("handle selector type while finding view from selector");
    }
    return result;
  }

  private Selector findSelectorForView(View view){
    Selector result = null;
    //check for resource id selector
    String viewResourceId = "";
    try{
      viewResourceId = view.getResources().getResourceName(view.getId());
      if(viewResourceId.startsWith(this.packageName)){
        viewResourceId = this.packageName + ".R.id." + viewResourceId.substring(viewResourceId.indexOf(":id/")+4);
      }
      else{
        viewResourceId = viewResourceId.replace(":id/", ".R.id.");
      }
    }
    catch(Exception e){

    }
    if(!viewResourceId.equals("")) {
      View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
      if (!(rootView != null && rootView.getVisibility() == View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))) {
        Log.d("Espresso", "root view is not visible when finding selector based on view for resource id");
        throw new RuntimeException("root view is not visible when finding selector based on view for resource id");
      }
      List<View> workList = new ArrayList<View>();
      workList.add(rootView);
      int resourceIdMatchesCount = 0;
      while (!workList.isEmpty()) {
        View currView = workList.remove(0);
        String currViewResourceId = "";
        try{
          currViewResourceId = currView.getResources().getResourceName(currView.getId());
          if(currViewResourceId.startsWith(this.packageName)){
            currViewResourceId = this.packageName + ".R.id." + currViewResourceId.substring(currViewResourceId.indexOf(":id/")+4);
          }
          else{
            currViewResourceId = currViewResourceId.replace(":id/", ".R.id.");
          }
        }
        catch(Exception e){

        }
        if(currViewResourceId.equals(viewResourceId)){
          resourceIdMatchesCount++;
        }
        if(currView instanceof ViewGroup){
          ViewGroup currViewGroup = (ViewGroup) currView;
          for(int i=0; i<currViewGroup.getChildCount(); ++i){
            View currChildView = currViewGroup.getChildAt(i);
            if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
              workList.add(currChildView);
            }
          }
        }
      }
      if(resourceIdMatchesCount==1){
        //selected resource id result
        result = new SelectorResourceId(viewResourceId);
        return result;
      }
    }
    String text = "";
    if(view instanceof TextView){
      TextView textView = (TextView) view;
      text = textView.getText() !=null ? textView.getText().toString() : "";
    }
    if(!text.equals("")){
      View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
      if (!(rootView != null && rootView.getVisibility() == View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))) {
        Log.d("Espresso", "root view is not visible when finding selector based on view for text");
        throw new RuntimeException("root view is not visible when finding selector based on view for text");
      }
      List<View> workList = new ArrayList<View>();
      workList.add(rootView);
      int textMatchesCount = 0;
      while (!workList.isEmpty()) {
        View currView = workList.remove(0);
        String currViewText = "";
        if(currView instanceof TextView){
          TextView currViewTextView = (TextView) currView;
          currViewText = currViewTextView.getText() !=null ? currViewTextView.getText().toString() : "";
        }
        if(currViewText.equals(text)){
          textMatchesCount++;
        }
        if(currView instanceof ViewGroup){
          ViewGroup currViewGroup = (ViewGroup) currView;
          for(int i=0; i<currViewGroup.getChildCount(); ++i){
            View currChildView = currViewGroup.getChildAt(i);
            if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
              workList.add(currChildView);
            }
          }
        }
      }
      if(textMatchesCount==1){
        //selected text resource id
        result = new SelectorText(text);
        return result;
      }
    }
    //did not select resource id as selector therefore compute xpath
    String xPath = computeXPathFromView(view);
    result = new SelectorXPath(xPath);
    return result;
  }

  private static List<String> getKeywordsFromReference(String reference){
    List<String> resultList = new ArrayList<String>();
    if(reference.equals("")){
      return resultList;
    }
    String[] underscoreComponents = StringUtils.split(reference, "_");
    for(String underscoreComponent:underscoreComponents){
      String[] spaceComponents = StringUtils.split(underscoreComponent, StringUtils.SPACE);
      for(String spaceComponent:spaceComponents){
        String[] components = StringUtils.splitByCharacterTypeCamelCase(spaceComponent);
        for(String component:components){
          resultList.add(component);
        }
      }
    }
    return resultList;
  }

  private String convertDrawableReference(String reference){
    if(reference.equals("")){
      return reference;
    }
    if(reference.startsWith("@drawable/")){
      String convertedReference = reference.substring("@drawable/".length());
      return convertedReference;
    }
    if(reference.startsWith("@null")){
      String convertedReference = "null";
      return convertedReference;
    }
    if(reference.startsWith("@android:drawable/")){
      String convertedReference = reference.substring("@android:drawable/".length());
      return convertedReference;
    }
    if(reference.startsWith("@*android:drawable/")){
      String convertedReference = reference.substring("@*android:drawable/".length());
      return convertedReference;
    }
    if(reference.startsWith("@color/")){
      String convertedReference = reference.substring("@color/".length());
      return convertedReference;
    }
    if(reference.startsWith("@android:color/")){
      String convertedReference = reference.substring("@android:color/".length());
      return convertedReference;
    }
    if(reference.startsWith("@mipmap/")){
      String convertedReference = reference.substring("@mipmap/".length());
      return convertedReference;
    }
    if(reference.startsWith("?android:")){
      String convertedReference = reference.substring("?android:".length());
      return convertedReference;
    }
    if(reference.startsWith("?attr/")){
      String convertedReference = reference.substring("?attr/".length());
      return convertedReference;
    }
    if(reference.startsWith("?")){
      String convertedReference = reference.substring("?".length());
      return convertedReference;
    }
    if(reference.startsWith("#")){
      String convertedReference = reference.substring("#".length());
      return convertedReference;
    }
    Log.d("Espresso", "could not convert drawable reference:"+reference);
    throw new RuntimeException("could not convert drawable reference:"+reference);
  }

  private TargetViewModel getTargetViewModelFromView(View view){
    String text="";
    if(view instanceof TextView){
      TextView textView = (TextView) view;
      text = textView.getText() == null ? "" : textView.getText().toString();
      if(text.equals("")){
        CharSequence hint = textView.getHint();
        if(hint!=null){
          text = hint.toString();
        }
      }
    }
    if(text.equals("")){
      CharSequence contentDescription = view.getContentDescription();
      if(contentDescription!=null){
        text = contentDescription.toString();
      }
    }

    String resourceId = "";
    if(view.getId()!=-1){
      resourceId=view.getId()+"";
    }
    String resourceIdReference = "";
    try{
      resourceIdReference = view.getResources().getResourceName(view.getId());
      if(resourceIdReference.indexOf(":id/")==-1){
        Log.d("Espresso", "can not parse resource id:"+resourceIdReference);
        throw new RuntimeException("can not parse resource id:"+resourceIdReference);
      }
      resourceIdReference = resourceIdReference.substring(resourceIdReference.indexOf(":id/")+4);
    }
    catch(Exception e){

    }
    List<String> resourceIdReferenceKeywordsList = getKeywordsFromReference(resourceIdReference);

    String imageReference="";
    Object tagImageReference = view.getTag(this.tagImageReferenceId);
    if(tagImageReference!=null){
      String tagImageReferenceString = tagImageReference.toString();
      tagImageReferenceString = tagImageReferenceString.substring(tagImageReferenceString.indexOf("referenceprefix")+"referenceprefix".length());
      imageReference = convertDrawableReference(tagImageReferenceString);
    }
    List<String> imageReferenceKeywordsList = getKeywordsFromReference(imageReference);
    String xPath = computeXPathFromView(view);

    //the last two booleans must be precise=true from text=false
    TargetViewModel result=new TargetViewModel(text, resourceId, resourceIdReference, resourceIdReferenceKeywordsList, imageReference, imageReferenceKeywordsList, xPath, true, false, false);
    return result;
  }

  private double getCandidateSimilarityToTarget(UiController uiController, TargetViewModel targetViewModel, TargetViewModel candidateViewModel){
    double result=0;
    if(targetViewModel.isFromText()) {
      //this target was created from nlp of text
      if(targetViewModel.isPrecise()){
        //this target had camel case and was recognized to be part of the ontology
        if(targetViewModel.getText().equals(candidateViewModel.getText())){
          result=1;
          return result;
        }
        else{
          result=0;
          return result;
        }
      }
      else{
        //find best matching
        try {
          String sentence1 = "";
          String sentence2 = "";
          JSONObject serverRequestResult = null;

          //find similarity based on text
          double textSimilarity = 0;
          sentence1 = targetViewModel.getText();
          sentence2 = candidateViewModel.textToSentence();
          if(!sentence1.equals("") && !sentence2.equals("")) {
            Log.d("Espresso", "sentence1:" + sentence1 + "#" + "sentence2:" + sentence2);
            serverRequestResult = sendServerRequest(uiController, sentence1, sentence2);
            if(serverRequestResult.has("score")) {
              Log.d("Espresso", serverRequestResult.toString());
              textSimilarity = Double.parseDouble(serverRequestResult.getString("score"));
              if (textSimilarity > result) {
                result = textSimilarity;
              }
            }
            else{
              Log.d("Espresso", "could not compute score for sentence1:"+sentence1+"#sentence2:"+sentence2);
              Log.d("Espresso", serverRequestResult.toString());
            }
          }

          //find similarity based on image reference
          double imageReferenceSimilarity = 0;
          sentence1 = targetViewModel.getText();
          sentence2 = candidateViewModel.imageReferenceKeywordsListToSentence();
          if(!sentence1.equals("") && !sentence2.equals("")) {
            Log.d("Espresso", "sentence1:" + sentence1 + "#" + "sentence2:" + sentence2);
            serverRequestResult = sendServerRequest(uiController, sentence1, sentence2);
            Log.d("Espresso", serverRequestResult.toString());
            if(serverRequestResult.has("score")) {
              imageReferenceSimilarity = Double.parseDouble(serverRequestResult.getString("score"));
              if (imageReferenceSimilarity > result) {
                result = imageReferenceSimilarity;
              }
            }
            else{
              Log.d("Espresso", "could not compute score for sentence1:"+sentence1+"#sentence2:"+sentence2);
              Log.d("Espresso", serverRequestResult.toString());
            }
          }

          //find similarity based on resource id reference
          double resourceIdReferenceSimilarity = 0;
          sentence1 = targetViewModel.getText();
          sentence2 = candidateViewModel.resourceIdReferenceKeywordsListToSentence();
          if(!sentence1.equals("") && !sentence2.equals("")) {
            Log.d("Espresso", "sentence1:" + sentence1 + "#" + "sentence2:" + sentence2);
            serverRequestResult = sendServerRequest(uiController, sentence1, sentence2);
            Log.d("Espresso", serverRequestResult.toString());
            if(serverRequestResult.has("score")) {
              resourceIdReferenceSimilarity = Double.parseDouble(serverRequestResult.getString("score"));
              if (resourceIdReferenceSimilarity > result) {
                result = resourceIdReferenceSimilarity;
              }
            }
            else{
              Log.d("Espresso", "could not compute score for sentence1:"+sentence1+"#sentence2:"+sentence2);
              Log.d("Espresso", serverRequestResult.toString());
            }
          }
          return result;
        }
        catch (JSONException je) {
          Log.d("Espresso", "exception when extracting score for sentence comparison");
          throw new RuntimeException("exception when extracting score for sentence comparison", je);
        }
      }
    }
    else{
      if(targetViewModel.isPrecise()) {
        //precondition: target view should never have all values empty
        //all info present in target must match information in candidate
        boolean same = true;
        if (!targetViewModel.getText().equals("")) {
          same = same && targetViewModel.getText().equals(candidateViewModel.getText());
        }
        if (!targetViewModel.getResourceId().equals("")) {
          same = same && targetViewModel.getResourceId().equals(candidateViewModel.getResourceId());
        }
        if (!targetViewModel.getImageReference().equals("")) {
          same = same && targetViewModel.getImageReference().equals(candidateViewModel.getImageReference());
        }
        if (!targetViewModel.getResourceIdReference().equals("")) {
          same = same && targetViewModel.getResourceIdReference().equals(candidateViewModel.getResourceIdReference());
        }
        if (!targetViewModel.getXPath().equals("")) {
          same = same && targetViewModel.getXPath().equals(candidateViewModel.getXPath());
        }
        if (same) {
          result = 1;
          return result;
        } else {
          result = 0;
          return result;
        }
      }
      else{
        //this case happens when generating a random goal and then we need to reprocess the text
        //find best matching
        try {
          String sentence1 = "";
          String sentence2 = "";
          JSONObject serverRequestResult = null;

          //find similarity based on text
          double textSimilarity = 0;
          sentence1 = targetViewModel.getText();
          sentence2 = candidateViewModel.textToSentence();
          if(!sentence1.equals("") && !sentence2.equals("")) {
            Log.d("Espresso", "sentence1:" + sentence1 + "#" + "sentence2:" + sentence2);
            serverRequestResult = sendServerRequest(uiController, sentence1, sentence2);
            if(serverRequestResult.has("score")) {
              Log.d("Espresso", serverRequestResult.toString());
              textSimilarity = Double.parseDouble(serverRequestResult.getString("score"));
              if (textSimilarity > result) {
                result = textSimilarity;
              }
            }
            else{
              Log.d("Espresso", "could not compute score for sentence1:"+sentence1+"#sentence2:"+sentence2);
              Log.d("Espresso", serverRequestResult.toString());
            }
          }

          //find similarity based on image reference
          double imageReferenceSimilarity = 0;
          sentence1 = targetViewModel.getText();
          sentence2 = candidateViewModel.imageReferenceKeywordsListToSentence();
          if(!sentence1.equals("") && !sentence2.equals("")) {
            Log.d("Espresso", "sentence1:" + sentence1 + "#" + "sentence2:" + sentence2);
            serverRequestResult = sendServerRequest(uiController, sentence1, sentence2);
            Log.d("Espresso", serverRequestResult.toString());
            if(serverRequestResult.has("score")) {
              imageReferenceSimilarity = Double.parseDouble(serverRequestResult.getString("score"));
              if (imageReferenceSimilarity > result) {
                result = imageReferenceSimilarity;
              }
            }
            else{
              Log.d("Espresso", "could not compute score for sentence1:"+sentence1+"#sentence2:"+sentence2);
              Log.d("Espresso", serverRequestResult.toString());
            }
          }

          //find similarity based on resource id reference
          double resourceIdReferenceSimilarity = 0;
          sentence1 = targetViewModel.getText();
          sentence2 = candidateViewModel.resourceIdReferenceKeywordsListToSentence();
          if(!sentence1.equals("") && !sentence2.equals("")) {
            Log.d("Espresso", "sentence1:" + sentence1 + "#" + "sentence2:" + sentence2);
            serverRequestResult = sendServerRequest(uiController, sentence1, sentence2);
            Log.d("Espresso", serverRequestResult.toString());
            if(serverRequestResult.has("score")) {
              resourceIdReferenceSimilarity = Double.parseDouble(serverRequestResult.getString("score"));
              if (resourceIdReferenceSimilarity > result) {
                result = resourceIdReferenceSimilarity;
              }
            }
            else{
              Log.d("Espresso", "could not compute score for sentence1:"+sentence1+"#sentence2:"+sentence2);
              Log.d("Espresso", serverRequestResult.toString());
            }
          }
          return result;
        }
        catch (JSONException je) {
          Log.d("Espresso", "exception when extracting score for sentence comparison");
          throw new RuntimeException("exception when extracting score for sentence comparison", je);
        }
      }
    }
  }

  private List<ScoredHierarchyTarget> findViewInUiHierarchyBasedOnTarget(UiController uiController, TargetViewModel targetViewModel, Class superClass){
    Log.d("Espresso","findViewInUiHierarchyBasedOnTarget fromText:"+targetViewModel.isFromText()+"#precise:"+targetViewModel.isPrecise());
    List<ScoredHierarchyTarget> resultList = new ArrayList<ScoredHierarchyTarget>();
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when finding view based on parameters");
      throw new RuntimeException("root view is not visible when finding view based on parameters");
    }
    //only allowed to use text or resourceId value as precise
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while (!workList.isEmpty()) {
      View currView = workList.remove(0);
      if(isDisplayingAtLeast(90).matches(currView) && superClass.isAssignableFrom(currView.getClass())){
        TargetViewModel candidateViewModel = getTargetViewModelFromView(currView);
        double similarityScore = getCandidateSimilarityToTarget(uiController, targetViewModel, candidateViewModel);
        if(similarityScore>MIN_SIMILARITY){
          Log.d("Espresso","hierarchy candidate:"+candidateViewModel.getText()+"#"+candidateViewModel.getImageReference()+"#"+candidateViewModel.getResourceIdReference());
          ScoredHierarchyTarget scoredHierarchyTarget = new ScoredHierarchyTarget(similarityScore, currView, candidateViewModel);
          resultList.add(scoredHierarchyTarget);
        }
      }
      if (currView instanceof ViewGroup) {
        ViewGroup currViewGroup = (ViewGroup) currView;
        for (int i = 0; i < currViewGroup.getChildCount(); ++i) {
          View currChildView = currViewGroup.getChildAt(i);
          if (currChildView != null && currChildView.getVisibility() == View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())) {
            workList.add(currChildView);
          }
        }
      }
    }
    return resultList;
  }


  public List<ScoredGraphTarget> findNodeInGraphBasedOnTarget(UiController uiController, TargetViewModel targetViewModel){
    List<ScoredGraphTarget> nodeViewList = new ArrayList<ScoredGraphTarget>();
    //optimization not to get all nodes in case it is from text (should always be) and it is precise
    if(targetViewModel.isFromText()){
      if(targetViewModel.isPrecise()){
        Log.d("Espresso", "findNodeInGraphBasedOnTarget:precise");
        //run query that search by text
        String graphQuery = GraphUtils.getViewFromText(targetViewModel.getText());
        JSONObject graphQueryResult = sendGraphRequest(uiController, graphQuery);
        if(!graphQueryResult.has("results")){
          Log.d("Espresso", "could not get view from text: "+graphQuery);
          Log.d("Espresso", graphQueryResult.toString());
          return nodeViewList;
        }
        List<Integer> viewsNodeIdList = new ArrayList<Integer>();
        List<TargetViewModel> viewNodeModelList = new ArrayList<TargetViewModel>();
        List<String> viewNodeFullClassNameList = new ArrayList<String>();
        try {
          //mf: example {"results":[{"columns":["v"],"data":[{"row":[{"resourceId":"","keywords":"settings","name":"android.widget.TextView","class":"android.widget.TextView","nodeId":55602}],"meta":[{"id":22874,"type":"node","deleted":false}]},{"row":[{"resourceId":"","keywords":"settings","name":"android.widget.TextView","class":"android.widget.TextView","nodeId":56977}],"meta":[{"id":22884,"type":"node","deleted":false}]},{"row":[{"resourceId":"","keywords":"settings","name":"android.widget.TextView","class":"android.widget.TextView","nodeId":56476}],"meta":[{"id":22887,"type":"node","deleted":false}]},{"row":[{"resourceId":"redditPrefsHeader","keywords":"open reddit settings in browser","name":"android.widget.TextView","class":"android.widget.TextView","nodeId":56638}],"meta":[{"id":23432,"type":"node","deleted":false}]},{"row":[{"resourceId":"","keywords":"enable datasaving settings","name":"android.widget.TextView","class":"android.widget.TextView","nodeId":57386}],"meta":[{"id":23781,"type":"node","deleted":false}]}]}],"errors":[]}
          JSONArray dataArrayJSON = graphQueryResult.getJSONArray("results").getJSONObject(0).getJSONArray("data");
          for (int i = 0; i < dataArrayJSON.length(); ++i) {
            JSONObject viewJSON = dataArrayJSON.getJSONObject(i).getJSONArray("row").getJSONObject(0);
            int viewNodeId = viewJSON.getInt("nodeId");
            String fullClassName = viewJSON.getString("fullClassName");
            String text = viewJSON.getString("text");
            String resourceId = viewJSON.getString("resourceId");
            String resourceIdReference = viewJSON.getString("resourceIdReference");
            List<String> resourceIdReferenceKeywordsList = getKeywordsFromReference(resourceIdReference);
            //mf: added
            if(fullClassName.equals("android.view.MenuItem")){
              Log.d("Espresso", "menu item change:"+text);
              resourceId = "";
              resourceIdReference = "";
              resourceIdReferenceKeywordsList = new ArrayList<String>();
            }
            String imageReference = viewJSON.getString("imageReference");
            List<String> imageReferenceKeywordsList = getKeywordsFromReference(imageReference);
            String xPath="";
            boolean precise = true;
            boolean fromText = false;
            String className = viewJSON.getString("className");
            boolean canBePreference = false;
            if(className.toLowerCase().contains("preference")){
              canBePreference = true;
            }
            TargetViewModel viewNodeModel = new TargetViewModel(text, resourceId, resourceIdReference, resourceIdReferenceKeywordsList, imageReference, imageReferenceKeywordsList, xPath, precise, fromText, canBePreference);
            //Log.d("Espresso", viewNodeId+"#"+viewNodeModel.getText());
            viewsNodeIdList.add(new Integer(viewNodeId));
            //mf: do not need to check score because it is going to be 1
            viewNodeModelList.add(viewNodeModel);
            //mf: put full class name
            viewNodeFullClassNameList.add(fullClassName);
          }
        } catch (JSONException je) {
          Log.d("Espresso", "exception when extracting views from graph");
          throw new RuntimeException("exception when extracting views from graph", je);
        }
        //get windows for views
        for (int i = 0; i < viewsNodeIdList.size(); ++i) {
          graphQuery = GraphUtils.getWindowFromNodeId(viewsNodeIdList.get(i).intValue());
          graphQueryResult = sendGraphRequest(uiController, graphQuery);
          if(!graphQueryResult.has("results")){
            Log.d("Espresso", "could not get window from node id: "+graphQuery);
            Log.d("Espresso", graphQueryResult.toString());
            continue;
          }
          try {
            //mf: example {"results":[{"columns":["last(collect(b))"],"data":[{"row":[{"keywords":"me.ccrama.redditslide.activities.mainactivity","name":"me.ccrama.redditslide.Activities.MainActivity","class":"me.ccrama.redditslide.Activities.MainActivity","nodeId":2295}],"meta":[{"id":22773,"type":"node","deleted":false}]}]}],"errors":[]}
            JSONArray dataArrayJSON = graphQueryResult.getJSONArray("results").getJSONObject(0).getJSONArray("data");
            for (int j = 0; j < dataArrayJSON.length(); ++j) {
              JSONObject windowJSON = dataArrayJSON.getJSONObject(j).getJSONArray("row").getJSONObject(0);
              int windowNodeId = windowJSON.getInt("nodeId");
              String relatedActivity = windowJSON.getString("relatedActivity");
              String type = windowJSON.getString("type");
              NodeWindow windowNode = new NodeWindow(windowNodeId, relatedActivity, type);
              int viewNodeId = viewsNodeIdList.get(i).intValue();
              String fullClassName = viewNodeFullClassNameList.get(i);
              TargetViewModel viewNodeModel = viewNodeModelList.get(i);
              NodeView nodeView = new NodeView(viewNodeId, fullClassName, windowNode, viewNodeModel);
              Log.d("Espresso", "graph candidate:"+viewNodeId+"#"+fullClassName+"#"+windowNode.getRelatedActivity()+"#"+viewNodeModel.getText()+"#"+viewNodeModel.getImageReference()+"#"+viewNodeModel.getResourceIdReference());
              nodeViewList.add(new ScoredGraphTarget(1,nodeView));
            }
          } catch (JSONException je) {
            Log.d("Espresso", "exception when getting window for view");
            throw new RuntimeException("exception when getting window for view", je);
          }
        }
        return nodeViewList;
      }
      else{
        Log.d("Espresso", "findNodeInGraphBasedOnTarget:not precise");
        //run query that get all view nodes
        String graphQuery = GraphUtils.getView();
        JSONObject graphQueryResult = sendGraphRequest(uiController, graphQuery);
        if(!graphQueryResult.has("results")){
          Log.d("Espresso", "could not get view: "+graphQuery);
          Log.d("Espresso", graphQueryResult.toString());
          return nodeViewList;
        }
        List<Integer> viewsNodeIdList = new ArrayList<Integer>();
        List<TargetViewModel> viewNodeModelList = new ArrayList<TargetViewModel>();
        List<String> viewNodeFullClassNameList = new ArrayList<String>();
        try {
          //mf: example {"results":[{"columns":["v"],"data":[{"row":[{"resourceId":"","keywords":"settings","name":"android.widget.TextView","class":"android.widget.TextView","nodeId":55602}],"meta":[{"id":22874,"type":"node","deleted":false}]},{"row":[{"resourceId":"","keywords":"settings","name":"android.widget.TextView","class":"android.widget.TextView","nodeId":56977}],"meta":[{"id":22884,"type":"node","deleted":false}]},{"row":[{"resourceId":"","keywords":"settings","name":"android.widget.TextView","class":"android.widget.TextView","nodeId":56476}],"meta":[{"id":22887,"type":"node","deleted":false}]},{"row":[{"resourceId":"redditPrefsHeader","keywords":"open reddit settings in browser","name":"android.widget.TextView","class":"android.widget.TextView","nodeId":56638}],"meta":[{"id":23432,"type":"node","deleted":false}]},{"row":[{"resourceId":"","keywords":"enable datasaving settings","name":"android.widget.TextView","class":"android.widget.TextView","nodeId":57386}],"meta":[{"id":23781,"type":"node","deleted":false}]}]}],"errors":[]}
          JSONArray dataArrayJSON = graphQueryResult.getJSONArray("results").getJSONObject(0).getJSONArray("data");
          for (int i = 0; i < dataArrayJSON.length(); ++i) {
            JSONObject viewJSON = dataArrayJSON.getJSONObject(i).getJSONArray("row").getJSONObject(0);
            int viewNodeId = viewJSON.getInt("nodeId");
            String fullClassName = viewJSON.getString("fullClassName");
            String text = viewJSON.getString("text");
            String resourceId = viewJSON.getString("resourceId");
            String resourceIdReference = viewJSON.getString("resourceIdReference");
            List<String> resourceIdReferenceKeywordsList = getKeywordsFromReference(resourceIdReference);
            //mf: added
            if(fullClassName.equals("android.view.MenuItem")){
              Log.d("Espresso", "menu item change:"+text);
              resourceId = "";
              resourceIdReference = "";
              resourceIdReferenceKeywordsList = new ArrayList<String>();
            }
            String imageReference = viewJSON.getString("imageReference");
            List<String> imageReferenceKeywordsList = getKeywordsFromReference(imageReference);
            String xPath="";
            boolean precise = true;
            boolean fromText = false;
            String className = viewJSON.getString("className");
            boolean canBePreference = false;
            if(className.toLowerCase().contains("preference")){
              canBePreference = true;
            }
            TargetViewModel viewNodeModel = new TargetViewModel(text, resourceId, resourceIdReference, resourceIdReferenceKeywordsList, imageReference, imageReferenceKeywordsList, xPath, precise, fromText, canBePreference);
            double similarityScore = getCandidateSimilarityToTarget(uiController, targetViewModel, viewNodeModel);
            if(similarityScore>MIN_SIMILARITY){
              //Log.d("Espresso", viewNodeId+"#"+viewNodeModel.getText());
              viewsNodeIdList.add(new Integer(viewNodeId));
              viewNodeModelList.add(viewNodeModel);
              viewNodeFullClassNameList.add(fullClassName);
            }
          }
        } catch (JSONException je) {
          Log.d("Espresso", "exception when extracting views from graph");
          throw new RuntimeException("exception when extracting views from graph", je);
        }
        //get windows for views
        for (int i = 0; i < viewsNodeIdList.size(); ++i) {
          graphQuery = GraphUtils.getWindowFromNodeId(viewsNodeIdList.get(i).intValue());
          graphQueryResult = sendGraphRequest(uiController, graphQuery);
          if(!graphQueryResult.has("results")){
            Log.d("Espresso", "could not get window from node id: "+graphQuery);
            Log.d("Espresso", graphQueryResult.toString());
            continue;
          }
          try {
            //mf: example {"results":[{"columns":["last(collect(b))"],"data":[{"row":[{"keywords":"me.ccrama.redditslide.activities.mainactivity","name":"me.ccrama.redditslide.Activities.MainActivity","class":"me.ccrama.redditslide.Activities.MainActivity","nodeId":2295}],"meta":[{"id":22773,"type":"node","deleted":false}]}]}],"errors":[]}
            JSONArray dataArrayJSON = graphQueryResult.getJSONArray("results").getJSONObject(0).getJSONArray("data");
            for (int j = 0; j < dataArrayJSON.length(); ++j) {
              JSONObject windowJSON = dataArrayJSON.getJSONObject(j).getJSONArray("row").getJSONObject(0);
              int windowNodeId = windowJSON.getInt("nodeId");

              String relatedActivity = windowJSON.getString("relatedActivity");
              String type = windowJSON.getString("type");
              NodeWindow windowNode = new NodeWindow(windowNodeId, relatedActivity, type);
              int viewNodeId = viewsNodeIdList.get(i).intValue();
              String fullClassName = viewNodeFullClassNameList.get(i);
              TargetViewModel viewNodeModel = viewNodeModelList.get(i);
              NodeView nodeView = new NodeView(viewNodeId, fullClassName, windowNode, viewNodeModel);
              Log.d("Espresso", "graph candidate:"+viewNodeId+"#"+fullClassName+"#"+windowNode.getRelatedActivity()+"#"+viewNodeModel.getText()+"#"+viewNodeModel.getImageReference()+"#"+viewNodeModel.getResourceIdReference());
              nodeViewList.add(new ScoredGraphTarget(1,nodeView));
            }
          } catch (JSONException je) {
            Log.d("Espresso", "exception when getting window for view");
            throw new RuntimeException("exception when getting window for view", je);
          }
        }
        return nodeViewList;
      }
    }
    else{
      Log.d("Espresso", "I should never query the graph for a state created by the search");
      throw new RuntimeException("I should never query the graph for a state created by the search");
    }
  }

  private List<List<Goal>> getListOfGoalsListToReachView(UiController uiController, String currentActivity, String windowType, NodeView nodeView){
    //get paths from current activity to target view
    List<List<Goal>> listOfNodeViewGoalsList = new ArrayList<List<Goal>>();
    String graphQuery = GraphUtils.getPathsFromWindowToView(currentActivity, windowType, nodeView.getNodeId());
    Log.d("Espresso", "get paths for:"+nodeView.getNodeId()+"#"+nodeView.getNodeViewModel().getText()+"#"+nodeView.getNodeViewModel().getImageReference()+"#"+nodeView.getNodeViewModel().getResourceIdReference()+" in:"+currentActivity+"#"+windowType);
    JSONObject graphQueryResult = sendGraphRequest(uiController, graphQuery);
    if(!graphQueryResult.has("results")){
      Log.d("Espresso", "could not get paths from window to view: "+graphQuery);
      Log.d("Espresso", graphQueryResult.toString());
      return listOfNodeViewGoalsList;
    }
    try {
      //each row is a different path
      //example {"row":[[{"relatedActivity":"me.ccrama.redditslide.Activities.MainActivity","keywords":"","name":"me.ccrama.redditslide.Activities.MainActivity","type":"Activity","class":"me.ccrama.redditslide.Activities.MainActivity","nodeId":2493},{},{"resourceId":2131689668,"keywords":"","name":"me.ccrama.redditslide.Views.SidebarLayout","text":"","class":"me.ccrama.redditslide.Views.SidebarLayout","nodeId":56795},{},{"resourceId":2131689964,"keywords":"","name":"android.widget.TextView","text":"comments","class":"android.widget.TextView","nodeId":56659},{},{"relatedActivity":"me.ccrama.redditslide.Activities.SettingsData","keywords":"","name":"me.ccrama.redditslide.Activities.SettingsData","type":"Activity","class":"me.ccrama.redditslide.Activities.SettingsData","nodeId":2533},{},{"resourceId":-1,"keywords":"","name":"android.widget.ScrollView","text":"","class":"android.widget.ScrollView","nodeId":55480},{},{"resourceId":-1,"keywords":"","name":"android.widget.TextView","text":"enable datasaving settings","class":"android.widget.TextView","nodeId":55506}],["hasRoot","hasDescendant","click","hasRoot","hasDescendant"]]...
      JSONArray dataArrayJSON = graphQueryResult.getJSONArray("results").getJSONObject(0).getJSONArray("data");
      for (int i = 0; i < dataArrayJSON.length(); ++i) {
        //get path
        JSONArray pathJSONArray = dataArrayJSON.getJSONObject(i).getJSONArray("row").getJSONArray(0);//first column
        //get relationships
        JSONArray relJSONArray = dataArrayJSON.getJSONObject(i).getJSONArray("row").getJSONArray(1);//second column
        //goals for this path
        List<Goal> pathGoalsList = new ArrayList<Goal>();
        int relIndex = 0;
        for(int j= 0; j<pathJSONArray.length(); j=j+2){
          if(relIndex>(relJSONArray.length()-1)){
            //reached the end
            break;
          }
          JSONObject srcJSON = pathJSONArray.getJSONObject(j);
          String rel = relJSONArray.getString(relIndex);
          if(rel.equals("click")){
            String text = srcJSON.getString("text");
            String resourceId = srcJSON.getString("resourceId");
            String resourceIdReference = srcJSON.getString("resourceIdReference");
            List<String> resourceIdReferenceKeywordsList = getKeywordsFromReference(resourceIdReference);
            String imageReference = srcJSON.getString("imageReference");
            List<String> imageReferenceKeywordsList = getKeywordsFromReference(imageReference);
            String xPath="";
            boolean precise = true;
            boolean fromText = false;
            String className = srcJSON.getString("className");
            boolean canBePreference = false;
            if(className.toLowerCase().contains("preference")){
              canBePreference = true;
            }
            TargetViewModel viewNodeModel = new TargetViewModel(text, resourceId, resourceIdReference, resourceIdReferenceKeywordsList, imageReference, imageReferenceKeywordsList, xPath, precise, fromText, canBePreference);
            if(text.equals("") && resourceId.equals("") && resourceIdReference.equals("") && imageReference.equals("")){
              //can not identify view
              pathGoalsList.clear();
              break;
            }
            GoalClick goalClick = new GoalClick(false, true, viewNodeModel, 0, 0);
            pathGoalsList.add(goalClick);
          }
          else if (rel.equals("longClick")){
            String text = srcJSON.getString("text");
            String resourceId = srcJSON.getString("resourceId");
            String resourceIdReference = srcJSON.getString("resourceIdReference");
            List<String> resourceIdReferenceKeywordsList = getKeywordsFromReference(resourceIdReference);
            String imageReference = srcJSON.getString("imageReference");
            List<String> imageReferenceKeywordsList = getKeywordsFromReference(imageReference);
            String xPath="";
            boolean precise = true;
            boolean fromText = false;
            String className = srcJSON.getString("className");
            boolean canBePreference = false;
            if(className.toLowerCase().contains("preference")){
              canBePreference = true;
            }
            TargetViewModel viewNodeModel = new TargetViewModel(text, resourceId, resourceIdReference, resourceIdReferenceKeywordsList, imageReference, imageReferenceKeywordsList, xPath, precise, fromText, canBePreference);
            if(text.equals("") && resourceId.equals("") && resourceIdReference.equals("") && imageReference.equals("")){
              //can not identify view
              pathGoalsList.clear();
              break;
            }
            GoalClick goalClick = new GoalClick(false, true, viewNodeModel, 1, 0);
            pathGoalsList.add(goalClick);
          }
          else if(rel.equals("openOptions")){
            GoalOpenOptions goalOpenOptions = new GoalOpenOptions(false, true);
            pathGoalsList.add(goalOpenOptions);
          }
          relIndex++;
        }
        if(pathGoalsList.size()>0){
          Log.d("Espresso", "found path for node and path is of size:"+pathGoalsList.size());
          listOfNodeViewGoalsList.add(pathGoalsList);
        }
      }
    } catch (JSONException je) {
      Log.d("Espresso", "exception in path generation");
      throw new RuntimeException("exception in path generation", je);
    }
    return listOfNodeViewGoalsList;
  }

  private String getCurrWindowType(){
    String result = "";
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view not visible in sleep");
      throw new RuntimeException("root view not visible in sleep");
    }
    boolean isDialog = false;
    try{
      Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().inRoot(RootMatchers.isDialog()).performNew(ViewActions.nothing());
      isDialog = true;
    }
    catch(Exception e){

    }
    if(isDialog){
      result = "Dialog";
      return result;
    }
//    //mf: should not need this
//    boolean isPopup = false;
//    try{
//      Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().inRoot(RootMatchers.isPlatformPopup()).performNew(ViewActions.nothing());
//      isPopup = true;
//    }
//    catch(Exception e){
//
//    }
    boolean isRecycleListView = false;
    boolean isMenu = false;
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while (!workList.isEmpty()) {
      View currView = workList.remove(0);
      try {
        if(Class.forName("com.android.internal.app.AlertController$RecycleListView").isAssignableFrom(currView.getClass())){
          isRecycleListView = true;
        }
      }
      catch(Exception e){
        Log.d("Espresso", "Could not get RecycleListView");
      }
      try{
        if(Class.forName("com.android.internal.view.menu.ListMenuItemView").isAssignableFrom(currView.getClass())){
          //android.support.v7.view.menu.ListMenuItemView
          isMenu = true;
        }
      }
      catch(Exception e){
        //Log.d("Espresso", "Could not get com.android.internal.view.menu.ListMenuItemView");
      }
      try{
        if(Class.forName("android.support.v7.view.menu.ListMenuItemView").isAssignableFrom(currView.getClass())){
          isMenu = true;
        }
      }
      catch(Exception e){
        //Log.d("Espresso", "Could not get android.support.v7.view.menu.ListMenuItemView");
      }
      if (currView instanceof ViewGroup) {
        ViewGroup currViewGroup = (ViewGroup) currView;
        for (int i = 0; i < currViewGroup.getChildCount(); ++i) {
          View currChildView = currViewGroup.getChildAt(i);
          if (currChildView != null && currChildView.getVisibility() == View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())) {
            workList.add(currChildView);
          }
        }
      }
    }
    if(isMenu && isRecycleListView){
      result = "ContextMenu";
      return result;
    }
    else if(isMenu){
      result = "OptionsMenu";
      return result;
    }
    else {
      result = "Activity";
      return result;
    }
  }


  private double findBetterMatch(UiController uiController, Goal goal, TargetViewModel target, double currMaxScore, String constraints){
    double graphMaxSimilarity=0;
    List<ScoredGraphTarget> scoredGraphTargetList = findNodeInGraphBasedOnTarget(uiController, target);
    scoredGraphTargetList.clear();
    List<NodeView> scoreList = new ArrayList<NodeView>();
    List<Double> scoreValueList = new ArrayList<Double>();
    //keep only scores that are higher of already matched score
    for(ScoredGraphTarget scoredGraphTarget:scoredGraphTargetList){
      if(scoredGraphTarget.getScore()>=currMaxScore){
        scoreList.add(scoredGraphTarget.getNodeView());
        scoreValueList.add(scoredGraphTarget.getScore());
      }
    }
    //try to find better match only in other activities
    Log.d("Espresso", "curr window type:"+getCurrWindowType());
    List<NodeView> resultList = new ArrayList<NodeView>();
    List<Double> resultValueList = new ArrayList<Double>();
    for(int i=0; i<scoreList.size(); ++i){
      //discarding views that are in the same activity
      boolean considerBasedOnClass = true;
      if(!constraints.equals(CONSTRAINT_ANY)){
        try{
          considerBasedOnClass = Class.forName(constraints).isAssignableFrom(Class.forName(scoreList.get(i).getFullClassName()));
        }
        catch(Exception e){
          Log.d("Espresso", "Could not get class from string "+scoreList.get(i).getFullClassName()+"#"+constraints);
        }
      }
      boolean considerBasedOnWindow = true;
      if(scoreList.get(i).getContainingWindow().getRelatedActivity().equals(ActivityUtils.getCurrentActivity().getClass().getName()) && scoreList.get(i).getContainingWindow().getType().equals(getCurrWindowType())){
        considerBasedOnWindow=false;
      }
      if(considerBasedOnClass && considerBasedOnWindow){
        resultList.add(scoreList.get(i));
        resultValueList.add(scoreValueList.get(i));
      }
      else {
        Log.d("Espresso", "discarded candidate (consider based on class:"+considerBasedOnClass+"#consider based on window:"+considerBasedOnWindow+") " + scoreList.get(i).getNodeId() + "#" + scoreList.get(i).getFullClassName() + "#" + scoreList.get(i).getContainingWindow().getRelatedActivity() + "#" + scoreList.get(i).getNodeViewModel().getText() + "#" + scoreList.get(i).getNodeViewModel().getImageReference() + "#" + scoreList.get(i).getNodeViewModel().getResourceIdReference());
      }
    }
    List<Double> finalValueList = new ArrayList<Double>();
    for (int i=0; i<resultList.size(); ++i) {
      List<List<Goal>> listOfNodeViewGoalsList = getListOfGoalsListToReachView(uiController, ActivityUtils.getCurrentActivity().getClass().getName(), getCurrWindowType(), resultList.get(i));
      boolean alreadyConsidered = false;
      for (List<Goal> nodeViewGoalsList : listOfNodeViewGoalsList) {
        if(nodeViewGoalsList.size()>GRAPH_MAX_DISTANCE) {
          continue;
        }
        if(!alreadyConsidered){
          alreadyConsidered=true;
          finalValueList.add(resultValueList.get(i));
        }
        //copy state
        State duplicatedState = this.currState.copy(getNextStateId());
        //we need to take care of goals because we want to have an updated information of current goals
        //prepare goals for duplicated state
        List<Goal> goalsForDuplicatedStateList = new ArrayList<Goal>();
        //add satisfied goals
        for (Goal satisfiedGoal : this.satisfiedGoalsList) {
          goalsForDuplicatedStateList.add(satisfiedGoal.copy());
        }
        //add goals to reach node view
        goalsForDuplicatedStateList.addAll(nodeViewGoalsList);
        //add current goal
        Goal currGoalForDuplicatedState = goal.copy();
        //check type of goal and perform additional actions
        if (currGoalForDuplicatedState instanceof GoalClick){
          GoalClick currGoalClickForDuplicatedState = (GoalClick) currGoalForDuplicatedState;
          //set target from this nodeview
          currGoalClickForDuplicatedState.setTargetViewModel(resultList.get(i).getNodeViewModel());
        }
        else if (currGoalForDuplicatedState instanceof GoalType){
          GoalType currGoalTypeForDuplicatedState = (GoalType) currGoalForDuplicatedState;
          //set target from this nodeview
          currGoalTypeForDuplicatedState.setTargetViewModel(resultList.get(i).getNodeViewModel());
        }
        //add current goal
        goalsForDuplicatedStateList.add(currGoalForDuplicatedState);
        //add remaining goals
        for (Goal remainingGoal : this.remainingGoalsList) {
          goalsForDuplicatedStateList.add(remainingGoal.copy());
        }
        duplicatedState.setGoalsList(goalsForDuplicatedStateList);
        //set score
        int newScore = duplicatedState.getScore() - nodeViewGoalsList.size();
        duplicatedState.setScore(newScore);
        //add state to remaining states
        this.remainingStatesList.add(duplicatedState);
      }
    }
    if(finalValueList.size()>0){
      //mf: find max
      for(Double newSimilarity:finalValueList){
        if(newSimilarity.doubleValue()>graphMaxSimilarity){
          graphMaxSimilarity = newSimilarity.doubleValue();
        }
      }
      return graphMaxSimilarity;
    }
    else{
      return graphMaxSimilarity;
    }
  }


  private void duplicateState(Goal goal, List<ScoredHierarchyTarget> otherList){
    if(goal instanceof GoalClick) {
      GoalClick goalClick = (GoalClick) goal;
      //create a state for other views
      for (ScoredHierarchyTarget otherTarget : otherList) {
        Log.d("Espresso", "goal click: duplicating state");
        //copy state
        State duplicatedState = this.currState.copy(getNextStateId());
        //we need to take care of goals because we want to have an updated information of current goals
        //prepare goals for duplicated state
        List<Goal> goalsForDuplicatedStateList = new ArrayList<Goal>();
        //add satisfied goals
        for (Goal satisfiedGoal : this.satisfiedGoalsList) {
          goalsForDuplicatedStateList.add(satisfiedGoal.copy());
        }
        //add current goal
        GoalClick currGoalClickForDuplicatedState = goalClick.copy();
        //set target for current goal
        currGoalClickForDuplicatedState.setTargetViewModel(otherTarget.getViewModel());
        //add current goal
        goalsForDuplicatedStateList.add(currGoalClickForDuplicatedState);
        //add remaining goals
        for (Goal remainingGoal : this.remainingGoalsList) {
          goalsForDuplicatedStateList.add(remainingGoal.copy());
        }
        duplicatedState.setGoalsList(goalsForDuplicatedStateList);
        //add state to remaining states
        this.remainingStatesList.add(duplicatedState);
      }
    }
    else if(goal instanceof GoalType) {
      GoalType goalType = (GoalType) goal;
      //create a state for other views
      for (ScoredHierarchyTarget otherTarget : otherList) {
        Log.d("Espresso", "goal type: duplicating state");
        //copy state
        State duplicatedState = this.currState.copy(getNextStateId());
        //we need to take care of goals because we want to have an updated information of current goals
        //prepare goals for duplicated state
        List<Goal> goalsForDuplicatedStateList = new ArrayList<Goal>();
        //add satisfied goals
        for (Goal satisfiedGoal : this.satisfiedGoalsList) {
          goalsForDuplicatedStateList.add(satisfiedGoal.copy());
        }
        //add current goal
        GoalType currGoalTypeForDuplicatedState = goalType.copy();
        //set target for current goal
        currGoalTypeForDuplicatedState.setTargetViewModel(otherTarget.getViewModel());
        //add current goal
        goalsForDuplicatedStateList.add(currGoalTypeForDuplicatedState);
        //add remaining goals
        for (Goal remainingGoal : this.remainingGoalsList) {
          goalsForDuplicatedStateList.add(remainingGoal.copy());
        }
        duplicatedState.setGoalsList(goalsForDuplicatedStateList);
        //add state to remaining states
        this.remainingStatesList.add(duplicatedState);
      }
    }
    else if(goal instanceof GoalClickGeneral) {
      Log.d("Espresso", "goal click general: duplicating state");
      //copy state by excluding the click general goal
      State duplicatedState = this.currState.copy(getNextStateId());
      //increase score as if it was solved
      duplicatedState.setScore(duplicatedState.getScore()+1);
      //prepare goals for duplicated state
      List<Goal> goalsForDuplicatedStateList = new ArrayList<Goal>();
      //add satisfied goals
      for (Goal satisfiedGoal : this.satisfiedGoalsList) {
        goalsForDuplicatedStateList.add(satisfiedGoal.copy());
      }
      //add remaining goals
      for (Goal remainingGoal : this.remainingGoalsList) {
        goalsForDuplicatedStateList.add(remainingGoal.copy());
      }
      duplicatedState.setGoalsList(goalsForDuplicatedStateList);
      //add state to remaining states
      this.remainingStatesList.add(duplicatedState);
    }
    else{
      Log.d("Espresso", "handle goal type when duplicating state");
      throw new RuntimeException("handle goal type when duplicating state");
    }
  }

  private void duplicateStateRandomException(){
    //copy state
    State duplicatedState = this.currState.copy(getNextStateId());
    //remove the action that caused the exception for the duplicated state
    duplicatedState.getActionsList().remove(duplicatedState.getActionsList().size()-1);
    //prepare goals for duplicated state
    List<Goal> goalsForDuplicatedStateList = new ArrayList<Goal>();
    //add satisfied goals
    for (Goal satisfiedGoal : this.satisfiedGoalsList) {
      goalsForDuplicatedStateList.add(satisfiedGoal.copy());
    }
    //add remaining goals
    for (Goal remainingGoal : this.remainingGoalsList) {
      goalsForDuplicatedStateList.add(remainingGoal.copy());
    }
    //add goals to duplicated state
    duplicatedState.setGoalsList(goalsForDuplicatedStateList);
    //decrease random count for duplicated state
    duplicatedState.setRandomCount(duplicatedState.getRandomCount()-1);
    //add state to remaining states
    this.remainingStatesList.add(duplicatedState);
  }

  private void updateCurrState(Goal goal, Action action){
    //add action to state
    this.currState.getActionsList().add(action);
    //set goal as satisfied
    goal.setSatisfied(true);
    //add curr goal to satisfied goals list
    this.satisfiedGoalsList.add(goal);
    //increase score because action was satisfied
    int newScore = this.currState.getScore() + 1;
    this.currState.setScore(newScore);
  }

  private void updateCurrState(Goal goal, List<Action> actionList){
    //add action to state
    this.currState.getActionsList().addAll(actionList);
    //set goal as satisfied
    goal.setSatisfied(true);
    //add curr goal to satisfied goals list
    this.satisfiedGoalsList.add(goal);
    //increase score because action was satisfied
    int newScore = this.currState.getScore() + 1;
    this.currState.setScore(newScore);
  }

  private Action generateRandomAction(){
    WindowManager wm = (WindowManager) ActivityUtils.getCurrentActivity().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    int currDisplayWidth = display.getWidth();
    int currDisplayHeight = display.getHeight();
    //Log.d("Espresso", "width:"+currDisplayWidth+"#height:"+currDisplayHeight);
    Action result = null;
    List<Action> actionsList = new ArrayList<Action>();
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when generating random action");
      throw new RuntimeException("root view is not visible when generating random action");
    }
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while(!workList.isEmpty()) {
      View currView = workList.remove(0);
      //check whether we should consider this view
      boolean consider = true;
      Rect rect = new Rect();
      currView.getGlobalVisibleRect(rect);
      boolean outOfDisplayArea = false;
      if(rect.bottom>currDisplayHeight || rect.right>currDisplayWidth || rect.top==0){//checking top==0 for system top bar
        outOfDisplayArea = true;
      }
      //Log.d("Espresso", "xpath"+computeXPathFromView(currView));
      //Log.d("Espresso", "outOfDisplayArea:"+outOfDisplayArea+"#:"+isDisplayingAtLeast(90).matches(currView));
      if(outOfDisplayArea || !isDisplayingAtLeast(90).matches(currView)){//90 is needed because espresso clicks only on things that are visible at least 90
        consider = false;
      }
      if(consider) {
        //if view is clicklable or has on click listeners
        if (currView.isClickable() || currView.hasOnClickListeners()) {
          Selector selector = findSelectorForView(currView);
          Action actionClick = new ActionClick(selector);
          actionsList.add(actionClick);

        }
        if (!(currView instanceof ViewGroup) && !actionsList.contains(currView)) {
          //add leaves as possible candidates for click if they were not already added
          Selector selector = findSelectorForView(currView);
          Action actionClick = new ActionClick(selector);
          actionsList.add(actionClick);
        }
      }
      if(currView instanceof ViewGroup){
        ViewGroup currViewGroup = (ViewGroup) currView;
        for(int i=0; i<currViewGroup.getChildCount(); ++i){
          View currChildView = currViewGroup.getChildAt(i);
          if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
            workList.add(currChildView);
          }
        }
      }
    }
    if(actionsList.size()==0){
      Log.d("Espresso", "state in which zero random actions can be performed");
      throw new RuntimeException("state in which zero random actions can be performed");
    }
    else {
      int selectedActionIndex = random.nextInt(actionsList.size());
      result = actionsList.get(selectedActionIndex);
    }
    return result;
  }

  private View getRandomEditor(){
    WindowManager wm = (WindowManager) ActivityUtils.getCurrentActivity().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    int currDisplayWidth = display.getWidth();
    int currDisplayHeight = display.getHeight();
    View result = null;
    List<View> resultList = new ArrayList<View>();
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when getting random editor");
      throw new RuntimeException("root view is not visible when getting random editor");
    }
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while(!workList.isEmpty()) {
      View currView = workList.remove(0);
      //check whether we should consider this view
      boolean consider = true;
      Rect rect = new Rect();
      currView.getGlobalVisibleRect(rect);
      boolean outOfDisplayArea = false;
      if(rect.bottom>currDisplayHeight || rect.right>currDisplayWidth || rect.top==0){//checking top==0 for system top bar
        outOfDisplayArea = true;
      }
      //Log.d("Espresso", "xpath"+computeXPathFromView(currView));
      //Log.d("Espresso", "outOfDisplayArea:"+outOfDisplayArea+"#:"+isDisplayingAtLeast(90).matches(currView));
      if(outOfDisplayArea || !isDisplayingAtLeast(90).matches(currView)){//90 is needed because espresso clicks only on things that are visible at least 90
        consider = false;
      }
      if(consider) {
        //if view is clicklable or has on click listeners
        if (currView instanceof EditText){
          resultList.add(currView);
        }
      }
      if(currView instanceof ViewGroup){
        ViewGroup currViewGroup = (ViewGroup) currView;
        for(int i=0; i<currViewGroup.getChildCount(); ++i){
          View currChildView = currViewGroup.getChildAt(i);
          if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
            workList.add(currChildView);
          }
        }
      }
    }
    if(resultList.size()==0){
      Log.d("Espresso", "state in which zero random editor can be get");
      return result;
    }
    else {
      int selectedActionIndex = random.nextInt(resultList.size());
      result = resultList.get(selectedActionIndex);
    }
    return result;
  }

  private boolean hierarchyHasDrawer(){
    boolean result = false;
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when checking if it has drawer");
      throw new RuntimeException("root view is not visible when checking if it has drawer");
    }
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while(!workList.isEmpty()){
      View currView = workList.remove(0);
      if(currView instanceof DrawerLayout){
        result = true;
        return result;
      }
      if(currView instanceof ViewGroup){
        ViewGroup currViewGroup = (ViewGroup) currView;
        for(int i=0; i<currViewGroup.getChildCount(); ++i){
          View currChildView = currViewGroup.getChildAt(i);
          if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
            workList.add(currChildView);
          }
        }
      }
    }
    return result;
  }

  private List<ScoredHierarchyTarget> applyDrawerHeuristic(UiController uiController, TargetViewModel targetViewModel){
    List<ScoredHierarchyTarget> resultList = new ArrayList<ScoredHierarchyTarget>();
    View drawerView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(drawerView!=null && drawerView.getVisibility()==View.VISIBLE && drawerView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when applying drawer heuristic");
      //we do not create an exception because this is an heuristic
      return resultList;
    }
    this.drawerViewSelector = findSelectorForView(drawerView);
    //open drawer
    Log.d("Espresso", "open drawer");
    //ActionDrawer actionOpenDrawer = new ActionDrawer(findSelectorForView(drawerView), 1);
    //performAction(uiController, actionOpenDrawer, false);
    Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(drawerView))).viewInteraction().performNew(ViewActions.drawer(1));
    //new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER_LEFT, GeneralLocation.CENTER_RIGHT, Press.FINGER).perform(uiController, drawerView);
    //check if view corresponding to keyword is present
    resultList = findViewInUiHierarchyBasedOnTarget(uiController, targetViewModel, View.class);
    if(resultList.size()>0){
      return resultList;
    }
    else{
      Log.d("Espresso", "close drawer");
      //invalidate open drawer
      //View newRoot = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
      //ActionDrawer actionCloseDrawer = new ActionDrawer(findSelectorForView(newRoot), 0);
      //performAction(uiController, actionCloseDrawer, false);
      Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView()))).viewInteraction().performNew(ViewActions.drawer(0));
      //new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER_LEFT_WITH_OFFSET, GeneralLocation.CENTER_LEFT, Press.FINGER).perform(uiController, Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView()); //using root view and not drawer view because it might have changed after opening the drawer
      this.drawerViewSelector = null;
    }
    return resultList;
  }

  private boolean hierarchyHasScrollableView(){
    boolean result = false;
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when checking if it has scrollable view");
      throw new RuntimeException("root view is not visible when checking if it has scrollable view");
    }
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while(!workList.isEmpty()){
      View currView = workList.remove(0);
      if(currView instanceof ScrollView || currView instanceof AbsListView || currView instanceof RecyclerView){//abs list view is fine
        result = true;
        return result;
      }
      if(currView instanceof ViewGroup){
        ViewGroup currViewGroup = (ViewGroup) currView;
        for(int i=0; i<currViewGroup.getChildCount(); ++i){
          View currChildView = currViewGroup.getChildAt(i);
          if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
            workList.add(currChildView);
          }
        }
      }
    }
    return result;
  }

  private boolean hierarchyHasOptionsMenu(){
    boolean result = false;
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when checking if it has options menu");
      throw new RuntimeException("root view is not visible when checking if it has options menu");
    }
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while(!workList.isEmpty()){
      View currView = workList.remove(0);
      if(currView.getClass().getName().contains("OverflowMenuButton")){
        result = true;
        return result;
      }
      if(currView instanceof ViewGroup){
        ViewGroup currViewGroup = (ViewGroup) currView;
        for(int i=0; i<currViewGroup.getChildCount(); ++i){
          View currChildView = currViewGroup.getChildAt(i);
          if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
            workList.add(currChildView);
          }
        }
      }
    }
    return result;
  }

  private List<ScoredHierarchyTarget> applyScrollHeuristic(UiController uiController, TargetViewModel targetViewModel){
    List<ScoredHierarchyTarget> resultList = new ArrayList<ScoredHierarchyTarget>();
    //find scrollable views
    List<View> scrollableViewList = new ArrayList<View>();
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when applying scroll heuristic");
      return resultList;
    }
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while (!workList.isEmpty()) {
      View currView = workList.remove(0);
      if(currView instanceof ScrollView || currView instanceof AbsListView || currView instanceof RecyclerView){//abs list view is fine
        scrollableViewList.add(currView);
      }
      if (currView instanceof ViewGroup) {
        ViewGroup currViewGroup = (ViewGroup) currView;
        for (int i = 0; i < currViewGroup.getChildCount(); ++i) {
          View currChildView = currViewGroup.getChildAt(i);
          if (currChildView != null && currChildView.getVisibility() == View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())) {
            workList.add(currChildView);
          }
        }
      }
    }
    View scrollView = null;
    if(scrollableViewList.size()==0){
      Log.d("Espresso", "no scrollable views");
      return resultList;
    }
    else if(scrollableViewList.size()==1){
      Log.d("Espresso", "one scrollable view");
      scrollView = scrollableViewList.get(0);
    }
    else {
      Log.d("Espresso", "more than one scrollable view");
      //throw new RuntimeException("handle more than one scrollable view");
      int selectedIndex = random.nextInt(scrollableViewList.size());
      scrollView = scrollableViewList.get(selectedIndex);
    }
    //View scrollView = scrollableViewList.get(0);
    this.scrollViewSelector = findSelectorForView(scrollView);
    Log.d("Espresso", "scroll view selector:"+scrollViewSelector.toString());
    this.scrollCount = 0;

    for(int i=0; i<SCROLL_COUNT_LIMIT; ++i){
      Log.d("Espresso", "scrolling down");
      //ActionScroll actionScrollDown = new ActionScroll(findSelectorForView(scrollView), 1);
      //performAction(uiController, actionScrollDown, false);
      Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(scrollView))).viewInteraction().performNew(ViewActions.scroll(1));
      //new GeneralSwipeAction(Swipe.FAST, GeneralLocation.TOP_CENTER_WITH_OFFSET, GeneralLocation.TOP_CENTER, Press.FINGER).perform(uiController, scrollView);
      scrollCount++;
      resultList = findViewInUiHierarchyBasedOnTarget(uiController, targetViewModel, View.class);
      if(resultList.size()>0){
        return  resultList;
      }
    }
    if(resultList.size()==0){
      Log.d("Espresso", "scrolling up");
      //scroll in the opposite way as before
      for(int i=0; i<SCROLL_COUNT_LIMIT; ++i) {
        //ActionScroll actionScrollUp = new ActionScroll(findSelectorForView(scrollView), 0);
        //performAction(uiController, actionScrollUp, false);
        Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(scrollView))).viewInteraction().performNew(ViewActions.scroll(0));
        //new GeneralSwipeAction(Swipe.FAST, GeneralLocation.TOP_CENTER, GeneralLocation.TOP_CENTER_WITH_OFFSET, Press.FINGER).perform(uiController, scrollView);
      }
      //reinitialize bookkeeping
      this.scrollCount = 0;
      this.scrollViewSelector=null;
    }
    return resultList;
  }

  private List<ScoredHierarchyTarget> applyOptionsMenuHeuristic(UiController uiController, TargetViewModel targetViewModel){
    List<ScoredHierarchyTarget> resultList = new ArrayList<ScoredHierarchyTarget>();
    //find root view
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when applying options menu heuristic");
      return resultList;
    }
    this.optionsMenuSelector = findSelectorForView(rootView);
    Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(rootView))).viewInteraction().performNew(ViewActions.pressMenuKey());
    resultList = findViewInUiHierarchyBasedOnTarget(uiController, targetViewModel, View.class);
    if(resultList.size()>0){
      return  resultList;
    }
    if(resultList.size()==0){
      rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
      if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
        Log.d("Espresso", "root view is not visible when closing options menu");
        return resultList;
      }
      Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(rootView))).viewInteraction().performNew(ViewActions.pressMenuKey());
      this.optionsMenuSelector = null;
    }
    return resultList;
  }

  private String getTextFromHierarchy(){
    String result="";
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "could not get text from hierarchy");
      return result;
    }
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while (!workList.isEmpty()) {
      View currView = workList.remove(0);
      if(currView instanceof TextView){
        TextView textView = (TextView) currView;
        result = result + "#" + textView.getText();
      }
      if(currView instanceof ViewGroup){
        ViewGroup currViewGroup = (ViewGroup) currView;
        for(int i=0; i<currViewGroup.getChildCount(); ++i){
          View currChildView = currViewGroup.getChildAt(i);
          if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
            workList.add(currChildView);
          }
        }
      }
    }
    return result;
  }

  private View findScrollElement(){
    //find scrollable views
    List<View> scrollableViewList = new ArrayList<View>();
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when trying to find scrollable element");
      throw new RuntimeException("root view is not visible when trying to find scrollable element");
    }
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while (!workList.isEmpty()) {
      View currView = workList.remove(0);
      if(currView instanceof ScrollView || currView instanceof AbsListView || currView instanceof RecyclerView || currView instanceof WebView){//abs list view is fine
        scrollableViewList.add(currView);
      }
      if (currView instanceof ViewGroup) {
        ViewGroup currViewGroup = (ViewGroup) currView;
        for (int i = 0; i < currViewGroup.getChildCount(); ++i) {
          View currChildView = currViewGroup.getChildAt(i);
          if (currChildView != null && currChildView.getVisibility() == View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())) {
            workList.add(currChildView);
          }
        }
      }
    }
    View scrollView = null;
    if(scrollableViewList.size()==0){
      Log.d("Espresso", "no scrollable views");
      throw new RuntimeException("no scrollable views");
    }
    else if(scrollableViewList.size()==1){
      Log.d("Espresso", "one scrollable view");
      scrollView = scrollableViewList.get(0);
    }
    else {
      Log.d("Espresso", "more than one scrollable view");
      //throw new RuntimeException("handle more than one scrollable view");
      int selectedIndex = random.nextInt(scrollableViewList.size());
      scrollView = scrollableViewList.get(selectedIndex);
    }
    //View scrollView = scrollableViewList.get(0);
    return scrollView;
  }

  private View findSwipeElement(){
    //find swipable views
    List<View> swipableViewList = new ArrayList<View>();
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when trying to find swipable element");
      throw new RuntimeException("root view is not visible when trying to find swipable element");
    }
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while (!workList.isEmpty()) {
      View currView = workList.remove(0);
      if(currView instanceof SwipeRefreshLayout){//abs list view is fine
        swipableViewList.add(currView);
      }
      if (currView instanceof ViewGroup) {
        ViewGroup currViewGroup = (ViewGroup) currView;
        for (int i = 0; i < currViewGroup.getChildCount(); ++i) {
          View currChildView = currViewGroup.getChildAt(i);
          if (currChildView != null && currChildView.getVisibility() == View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())) {
            workList.add(currChildView);
          }
        }
      }
    }
    if(swipableViewList.size()==0){
      Log.d("Espresso", "no swipable views");
      throw new RuntimeException("no swipable views");
    }
    else if(swipableViewList.size()==1){
      Log.d("Espresso", "one swipable view");
    }
    else {
      Log.d("Espresso", "more than one swipable view");
      throw new RuntimeException("handle more than one swipable view");
    }
    View swipeView = swipableViewList.get(0);
    return swipeView;
  }

  private void scrollComplete(UiController uiController, int direction){
    //find scrollable views
    List<View> scrollableViewList = new ArrayList<View>();
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "root view is not visible when trying to scroll complete");
      throw new RuntimeException("root view is not visible when trying to scroll complete");
    }
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while (!workList.isEmpty()) {
      View currView = workList.remove(0);
      if(currView instanceof ScrollView || currView instanceof AbsListView || currView instanceof RecyclerView){//abs list view is fine
        scrollableViewList.add(currView);
      }
      if (currView instanceof ViewGroup) {
        ViewGroup currViewGroup = (ViewGroup) currView;
        for (int i = 0; i < currViewGroup.getChildCount(); ++i) {
          View currChildView = currViewGroup.getChildAt(i);
          if (currChildView != null && currChildView.getVisibility() == View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())) {
            workList.add(currChildView);
          }
        }
      }
    }
    View scrollView = null;
    if(scrollableViewList.size()==0){
      Log.d("Espresso", "no scrollable views");
      throw new RuntimeException("no scrollable views");
    }
    else if(scrollableViewList.size()==1){
      Log.d("Espresso", "one scrollable view");
      scrollView = scrollableViewList.get(0);
    }
    else {
      Log.d("Espresso", "more than one scrollable view");
      //throw new RuntimeException("handle more than one scrollable view");
      int selectedIndex = random.nextInt(scrollableViewList.size());
      scrollView = scrollableViewList.get(selectedIndex);
    }
    //View scrollView = scrollableViewList.get(0);
    this.scrollViewSelector = findSelectorForView(scrollView);
    this.scrollCount = 0;
    boolean iterate = true;
    while(iterate){
      this.scrollCount++;
      String displayedText = getTextFromHierarchy();
      //ActionScroll actionScroll = new ActionScroll(findSelectorForView(scrollView), direction);
      //performAction(uiController, actionScroll, false);
      Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(scrollView))).viewInteraction().performNew(ViewActions.scroll(direction));
//      if(direction==1){
//        //scroll down
//        new GeneralSwipeAction(Swipe.FAST, GeneralLocation.TOP_CENTER_WITH_OFFSET, GeneralLocation.TOP_CENTER, Press.FINGER).perform(uiController, scrollView);
//      }
//      else {
//        //scroll up
//        new GeneralSwipeAction(Swipe.FAST, GeneralLocation.TOP_CENTER, GeneralLocation.TOP_CENTER_WITH_OFFSET, Press.FINGER).perform(uiController, scrollView);
//      }
      String newDisplayedText = getTextFromHierarchy();
      if(displayedText.equals(newDisplayedText)){
        iterate=false;
      }
    }
  }

  private View findElementInContainerBasedOnPosition(String container, int position){
    View result = null;
    if(container.equals("list")){
      List<View> containerList = new ArrayList<View>();
      View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
      if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
        Log.d("Espresso", "root view is not visible when trying find element in container");
        throw new RuntimeException("root view is not visible when trying find element in container");
      }
      List<View> workList = new ArrayList<View>();
      workList.add(rootView);
      while (!workList.isEmpty()) {
        View currView = workList.remove(0);
        if(currView instanceof ScrollView || currView instanceof AbsListView || currView instanceof RecyclerView){//abs list view is fine
          containerList.add(currView);
        }
        if (currView instanceof ViewGroup) {
          ViewGroup currViewGroup = (ViewGroup) currView;
          for (int i = 0; i < currViewGroup.getChildCount(); ++i) {
            View currChildView = currViewGroup.getChildAt(i);
            if (currChildView != null && currChildView.getVisibility() == View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())) {
              workList.add(currChildView);
            }
          }
        }
      }
      View containerView = null;
      if(containerList.size()==0){
        Log.d("Espresso", "no scrollable views");
        throw new RuntimeException("no scrollable views");
      }
      else if(containerList.size()==1){
        Log.d("Espresso", "one scrollable view");
        containerView = containerList.get(0);
      }
      else {
        Log.d("Espresso", "more than one scrollable view");
        //throw new RuntimeException("handle more than one container view");
        int selectedIndex = random.nextInt(containerList.size());
        containerView = containerList.get(selectedIndex);
      }
      //View containerView = containerList.get(0);
      List<View> positionWorkList = new ArrayList<View>();
      positionWorkList.add(containerView);
      while(!positionWorkList.isEmpty()){
        View currView = positionWorkList.remove(0);
        if(currView instanceof ViewGroup) {
          ViewGroup currViewGroup = (ViewGroup) currView;
          List<View> childrenList = new ArrayList<View>();
          for (int i = 0; i < currViewGroup.getChildCount(); ++i) {
            View currChildView = currViewGroup.getChildAt(i);
            if (currChildView != null && currChildView.getVisibility() == View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())) {
              childrenList.add(currChildView);
            }
          }
          //analyze children list
          if (childrenList.size() == 0) {
            //no children
            if(position==1){
              //fine if position is first
              result = currView;
              return result;
            }
            else{
              //not good if other position
              Log.d("Espresso", "the list does not have the position I am looking for");
              throw new RuntimeException("the list does not have the position I am looking for");
            }
          } else if (childrenList.size() == 1){
            //check only child
            View childView = childrenList.get(0);
            if(childView instanceof ViewGroup){
              //if it is a view group iterate on it
              workList.add(childView);
            }
            else{
              //not a view group
              if(position==1){
                //fine if position is first
                result = childView;
                return result;
              }
              else{
                Log.d("Espresso", "the list does not have the position I am looking for");
                throw new RuntimeException("the list does not have the position I am looking for");
              }
            }
          } else {
            //more than one child
            if (position == 0) {
              //get last
              result = childrenList.get(childrenList.size() - 1);
              return result;
            } else {
              //get position
              int index = position - 1;
              result = childrenList.get(index);
              return result;
            }
          }
        }
      }
    }
    else{
      Log.d("Espresso", "handle type of container");
      throw new RuntimeException("handle type of container");
    }
    return result;
  }

  private void printHierarchy(){
    View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
    if(!(rootView!=null && rootView.getVisibility()==View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))){
      Log.d("Espresso", "could not print hierarchy");
      return;
    }
    Log.d("Espresso", "hierarchy");
    List<View> workList = new ArrayList<View>();
    workList.add(rootView);
    while (!workList.isEmpty()) {
      View currView = workList.remove(0);
      String result = "";
      result = result + computeXPathFromView(currView);
      result = result + "#" + currView.isClickable();
      if(currView instanceof TextView){
        TextView textView = (TextView) currView;
        result = result + "#" + textView.getText();
      }
      else{
        result = result + "#*";
      }
      String resourceId = "";
      try {
        resourceId = currView.getResources().getResourceName(currView.getId());
        if(resourceId.startsWith(this.packageName)){
          resourceId = this.packageName + ".R.id." + resourceId.substring(resourceId.indexOf(":id/")+4);
        }
        else{
          resourceId = resourceId.replace(":id/", ".R.id.");
        }
        result = result + "#" + resourceId;
      }
      catch (Exception e){
        result = result + "#*";
      }
      Log.d("Espresso", result);
      if(currView instanceof ViewGroup){
        ViewGroup currViewGroup = (ViewGroup) currView;
        for(int i=0; i<currViewGroup.getChildCount(); ++i){
          View currChildView = currViewGroup.getChildAt(i);
          if(currChildView!=null && currChildView.getVisibility()==View.VISIBLE && currChildView.getGlobalVisibleRect(new Rect())){
            workList.add(currChildView);
          }
        }
      }
    }
  }


  public void closeKeyboard() {
    if(KEYBOARD_CHECK_ENABLED) {
      View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
      if (!(rootView != null && rootView.getVisibility() == View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))) {
        Log.d("Espresso", "could not find root when trying to close keyboard");
        throw new RuntimeException("could not find root when trying to close keyboard");
      }
      ActionKeyboardClose actionKeyboardClose = new ActionKeyboardClose(findSelectorForView(rootView));
      Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.withView(rootView))).viewInteraction().performNew(ViewActions.closeSoftKeyboard());
      if (!KEYBOARD_IS_CLOSED) {
        Log.d("Espresso", "Adding keyboard close action to state");
        this.currState.getActionsList().add(actionKeyboardClose);
        KEYBOARD_IS_CLOSED = true;
      }
    }
  }

  private String findTextBasedOnInputs(UiController uiController, String target){
    String result = "";
    double maxSimilarityScore = 0;
    try {
      for (String key : inputsMap.keySet()) {
        String sentence1 = "";
        String sentence2 = "";
        JSONObject serverRequestResult = null;
        double currSimilarityScore = 0;
        sentence1 = key;
        sentence2 = target;
        Log.d("Espresso", "input request:"+sentence1+"#"+sentence2);
        if (!sentence1.equals("") && !sentence2.equals("")) {
          serverRequestResult = sendServerRequest(uiController, sentence1, sentence2);
          Log.d("Espresso", "result:"+serverRequestResult.toString());
          if (serverRequestResult.has("score")) {
            currSimilarityScore = Double.parseDouble(serverRequestResult.getString("score"));
            if (currSimilarityScore > maxSimilarityScore && currSimilarityScore > MIN_SIMILARITY) {
              maxSimilarityScore = currSimilarityScore;
              result = inputsMap.get(key);
            }
          } else {
            Log.d("Espresso", "could not compute score for sentence1:" + sentence1 + "#sentence2:" + sentence2);
            Log.d("Espresso", serverRequestResult.toString());
          }
        }
      }
    }
    catch (JSONException je) {
      Log.d("Espresso", "exception when extracting score for sentence comparison");
    }
    if(result.equals("")){
      //could not find any input, generate random string
      result = RandomStringUtils.randomAlphanumeric(RANDOM_STRING_LENGTH);
    }
    Log.d("Espresso", "Using as input:"+result);
    return result;
  }

  private void duplicateStateSkippingGoal(Goal goal){
    Log.d("Espresso", "goal: hard to satisfy therefore creating a state that can skip this goal");
    State duplicatedState = this.currState.copy(getNextStateId());
    //increase the number of goals skipped
    duplicatedState.setSkippedGoalCount(duplicatedState.getSkippedGoalCount()+1);
    //prepare goals for duplicated state
    List<Goal> goalsForDuplicatedStateList = new ArrayList<Goal>();
    //add satisfied goals
    for (Goal satisfiedGoal : this.satisfiedGoalsList) {
      goalsForDuplicatedStateList.add(satisfiedGoal.copy());
    }
    //add remaining goals
    for (Goal remainingGoal : this.remainingGoalsList) {
      goalsForDuplicatedStateList.add(remainingGoal.copy());
    }
    duplicatedState.setGoalsList(goalsForDuplicatedStateList);
    //add state to remaining states
    this.remainingStatesList.add(duplicatedState);
  }

  private boolean isGenericTarget(String lemma){
    boolean result = false;
    if(lemma.toLowerCase().equals("something")){
      result = true;
      return result;
    }
    if(lemma.toLowerCase().equals("anything")){
      result = true;
      return result;
    }
    return result;
  }

  @Override
  public void perform(UiController uiController, View view) {
    //mf: logging
    Log.d("Espresso", "search started");

    try {

      //save the activity name of the activity launched by espresso
      //this.firstActivityName = ActivityUtils.getCurrentActivity().getClass().getName();

      //initialization
      //read configuration
      readConfig();
      //load initial steps
      this.stepsList = readSteps();
      //load list of goals
      this.goalsList = readGoals();
      //create list of states
      this.statesList = readStates();
      //create list of processed states
      this.statesProcessedList = readStatesProcessed();
      //create map of inputs
      this.inputsMap = readInputs();

      //search bookkeeping and updated durign search process
      this.currState = null;
      this.remainingStatesList = new ArrayList<State>();
      this.satisfiedGoalsList = new ArrayList<Goal>();
      this.remainingGoalsList = new ArrayList<Goal>();

      //check if the search is at the beginning
      if (this.statesList.size() == 0) {
        //initial execution of search
        this.currState = new State(getNextStateId(), 0, this.goalsList, new ArrayList<Action>(), 0, 0, "", false, 0, 0);
        //extracts goals to be satisfied
        this.remainingGoalsList.addAll(this.goalsList);
        //add non selected goal

      } else {
        int maxStateId = Integer.MIN_VALUE;
        for (State state : this.statesList) {
          if (state.getStateId() > maxStateId) {
            maxStateId = state.getStateId();
          }
        }
        for (State state : this.statesProcessedList) {
          if (state.getStateId() > maxStateId) {
            maxStateId = state.getStateId();
          }
        }
        this.nextStateId = maxStateId + 1;
        //already started search
        this.currState = selectCurrState(this.statesList);
        //add all nodes with id different from curr state
        for (State state : this.statesList) {
          if (state.getStateId() != this.currState.getStateId()) {
            this.remainingStatesList.add(state);
          }
        }
        //extract goals to be satisfied
        List<Goal> currStateGoalsList = this.currState.getGoalsList();
        for (Goal goal : currStateGoalsList) {
          if (goal.isSatisfied()) {
            this.satisfiedGoalsList.add(goal);
          } else {
            this.remainingGoalsList.add(goal);
          }
        }
      }

      //execute steps and actions of currState
      List<Action> actionsToRestoreState = new ArrayList<Action>();
      for (Action action : this.stepsList) {
        actionsToRestoreState.add(action);
      }
      for (Action action : this.currState.getActionsList()) {
        actionsToRestoreState.add(action);
      }

      //do not check for close keyboard here because it can be done by human
      restoreState(uiController, actionsToRestoreState);
      KEYBOARD_CHECK_ENABLED=true;

      while (!this.remainingGoalsList.isEmpty()) {
        Log.d("Espresso", "search iteration");
        //mf: get curr goal
        Goal currGoal = this.remainingGoalsList.remove(0);
        //***************************handling goal click******************************//
        if (currGoal instanceof GoalClick) {
          //printHierarchy();
          GoalClick goalClick = (GoalClick) currGoal;
          if (goalClick.getTarget().isFromText()) {
            Log.d("Espresso", "goal click: processing goal from text:" + goalClick.getTarget().getText());
            //##############click goal generated by nlp processing########################
            if (goalClick.getTarget().getCanBePreference() && !goalClick.getTarget().getText().equals("")) {
              //try to load and click preference
              Log.d("Espresso", "goal click: trying preference heuristic");
              try {
                if (goalClick.getDuration() == 0) {
                  new DataInteraction(PreferenceMatchers.withTitleText(goalClick.getTarget().getText())).performNew(uiController, ViewActions.click());
                } else if (goalClick.getDuration() == 1) {
                  new DataInteraction(PreferenceMatchers.withTitleText(goalClick.getTarget().getText())).performNew(uiController, ViewActions.longClick());
                } else {
                  new DataInteraction(PreferenceMatchers.withTitleText(goalClick.getTarget().getText())).performNew(uiController, ViewActions.doubleClick());
                }
                Action action = null;
                if (goalClick.getDuration() == 0) {
                  action = new ActionPreferenceClick(goalClick.getTarget().getText());
                } else if (goalClick.getDuration() == 1) {
                  action = new ActionPreferenceClickLong(goalClick.getTarget().getText());
                } else {
                  action = new ActionPreferenceClickDouble(goalClick.getTarget().getText());
                }
                //not updating goal with selected target because it is identity and this action is superfluous
                //goalClick.setTargetViewModel(goalClick.getTarget());
                updateCurrState(goalClick, action);
                //try to close keyboard after preference heuristic was successfull
                closeKeyboard();
                continue;
              } catch (Exception e) {
                //check if exception was caused because of crash in app
                boolean isExceptionFromApp = false;
                StringWriter errors = new StringWriter();
                PrintWriter pw = new PrintWriter(errors);
                e.printStackTrace(pw);
                //mf: this should not be needed but there is some problem when there is an exception in jni
                if(e.getCause()!=null){
                  e.getCause().printStackTrace(pw);
                }
                if(errors.toString().contains(this.packageName)
                        || errors.toString().contains("android.support.v7")){
                  isExceptionFromApp = true;
                }
                if(isExceptionFromApp) {
                  //stop search only if remaining goals list is empty
                  if(remainingGoalsList.size()==0) {
                    this.finished = true;
                  }
                  //save actions and update state
                  Action action = null;
                  if (goalClick.getDuration() == 0) {
                    action = new ActionPreferenceClick(goalClick.getTarget().getText());
                  } else if (goalClick.getDuration() == 1) {
                    action = new ActionPreferenceClickLong(goalClick.getTarget().getText());
                  } else {
                    action = new ActionPreferenceClickDouble(goalClick.getTarget().getText());
                  }
                  //not updating goal with selected target because it is identity and this action is superfluous
                  //goalClick.setTargetViewModel(goalClick.getTarget());
                  updateCurrState(goalClick, action);
                  this.currState.setMessage(errors.toString());
                  saveExecutionState();
                  throw new RuntimeException(e);
                }
                else {
                  Log.d("Espresso", "preference heuristic did not work");
                }
              }
            }
            Log.d("Espresso", "goal click: checking hierarchy for target");
            List<ScoredHierarchyTarget> candidateViewList = findViewInUiHierarchyBasedOnTarget(uiController, goalClick.getTarget(), View.class);
            //find max score
            double maxScore = 0;
            for (ScoredHierarchyTarget scoredHierarchyTarget : candidateViewList) {
              if (scoredHierarchyTarget.getScore() > maxScore) {
                maxScore = scoredHierarchyTarget.getScore();
              }
            }
            //find if there would be another activity with a better match
            Log.d("Espresso", "goal click: checking graph for better match");
            double graphMaxScore = findBetterMatch(uiController, goalClick, goalClick.getTarget(), maxScore, CONSTRAINT_ANY);
            //process results based on current hierarchy
            if (candidateViewList.size() > 0) {
              Log.d("Espresso", "goal click: hierarchy has target");
              //select candidate
              int selectedIndex = -1;
              for (int i = 0; i < candidateViewList.size(); ++i) {
                if (Double.compare(candidateViewList.get(i).getScore(), maxScore) == 0) {
                  selectedIndex = i;
                  break;
                }
              }
              if (selectedIndex == -1) {
                Log.d("Espresso", "double comparison failed");
                throw new RuntimeException("double comparison failed");
              }
              //check if we should consider to skip this gaol
              if(maxScore<SKIP_SIMILARITY && graphMaxScore<SKIP_SIMILARITY){
                duplicateStateSkippingGoal(goalClick);
              }
              ScoredHierarchyTarget selectedTarget = candidateViewList.remove(selectedIndex);
              //duplicate state non selected views, this function makes sure of making target of click goal as specific as possible using xpath and marking them as search generated
              duplicateState(goalClick, candidateViewList);
              //get selected view
              View selectedView = selectedTarget.getView();
              Selector selectedSelector = findSelectorForView(selectedView);
              //create action
              Action action = null;
              if (goalClick.getDuration() == 0) {
                action = new ActionClick(selectedSelector);
              } else if (goalClick.getDuration() == 1) {
                action = new ActionClickLong(selectedSelector);
              } else {
                action = new ActionClickDouble(selectedSelector);
              }
              //update goal with selected target (this action is superfluous but using to keep it consistent)
              goalClick.setTargetViewModel(selectedTarget.getViewModel());
              //
              updateCurrState(goalClick, action);
              //perform action
              performAction(uiController, action, false);
              //move to next goal
              continue;
            } else {
              //##############################try drawer heuristic########################################
              Log.d("Espresso", "goal click: trying drawer heuristic");
              if (hierarchyHasDrawer()) {
                Log.d("Espresso", "goal click: has drawer");
                List<ScoredHierarchyTarget> drawerTargetList = applyDrawerHeuristic(uiController, goalClick.getTarget());
                if (drawerTargetList.size() > 0) {
                  Log.d("Espresso", "goal click: drawer heuristic was successful");
                  //find the target with max similarity
                  double drawerMaxScore = 0;
                  for (ScoredHierarchyTarget scoredHierarchyTarget : drawerTargetList) {
                    if (scoredHierarchyTarget.getScore() > drawerMaxScore) {
                      drawerMaxScore = scoredHierarchyTarget.getScore();
                    }
                  }
                  //selecting index
                  int selectedIndex = -1;
                  for (int i = 0; i < drawerTargetList.size(); ++i) {
                    if (Double.compare(drawerTargetList.get(i).getScore(), drawerMaxScore) == 0) {
                      selectedIndex = i;
                      break;
                    }
                  }
                  if (selectedIndex == -1) {
                    Log.d("Espresso", "double comparison failed");
                    throw new RuntimeException("double comparison failed");
                  }
                  //check if we should consider to skip this gaol
                  if(drawerMaxScore<SKIP_SIMILARITY && graphMaxScore<SKIP_SIMILARITY){
                    duplicateStateSkippingGoal(goalClick);
                  }
                  //this must happen after we duplicate the state
                  this.currState.setDrawerHeuristicCount(this.currState.getDrawerHeuristicCount()+1);
                  //doing the drawer heuristic returned a result
                  //add open drawer to the list of actions because it was successful in finding the target
                  ActionDrawer actionDrawer = new ActionDrawer(this.drawerViewSelector.copy(), 1);
                  this.currState.getActionsList().add(actionDrawer);
                  //reset selector
                  this.drawerViewSelector = null;
                  //select target for this state
                  ScoredHierarchyTarget selectedTarget = drawerTargetList.remove(selectedIndex);
                  //duplicate state non selected views,  this function makes sure of making target of click goal as specific as possible using xpath and marking them as search generated
                  duplicateState(goalClick, drawerTargetList);
                  //get selected view
                  View selectedView = selectedTarget.getView();
                  Selector selectedSelector = findSelectorForView(selectedView);
                  //create action
                  Action action = null;
                  if (goalClick.getDuration() == 0) {
                    action = new ActionClick(selectedSelector);
                  } else if (goalClick.getDuration() == 1) {
                    action = new ActionClickLong(selectedSelector);
                  } else {
                    action = new ActionClickDouble(selectedSelector);
                  }

                  //update goal with selected target (this action is superfluous but using to keep it consistent)
                  goalClick.setTargetViewModel(selectedTarget.getViewModel());
                  //update current state
                  updateCurrState(goalClick, action);
                  //perform action
                  performAction(uiController, action, false);
                  //move to next goal
                  continue;
                }
              }
              //##############################try scroll heuristic########################################
              Log.d("Espresso", "goal click: trying scroll heuristic");
              if (hierarchyHasScrollableView()) {
                Log.d("Espresso", "goal click: has scrollable view");
                List<ScoredHierarchyTarget> scrollableTargetList = applyScrollHeuristic(uiController, goalClick.getTarget());
                if (scrollableTargetList.size() > 0) {
                  Log.d("Espresso", "goal click: scroll heuristic was successful");
                  //find the target with max similairty
                  double scrollMaxScore = 0;
                  for (ScoredHierarchyTarget scoredHierarchyTarget : scrollableTargetList) {
                    if (scoredHierarchyTarget.getScore() > scrollMaxScore) {
                      scrollMaxScore = scoredHierarchyTarget.getScore();
                    }
                  }
                  //select index
                  int selectedIndex = -1;
                  for (int i = 0; i < scrollableTargetList.size(); ++i) {
                    if (Double.compare(scrollableTargetList.get(i).getScore(), scrollMaxScore) == 0) {
                      selectedIndex = i;
                      break;
                    }
                  }
                  if (selectedIndex == -1) {
                    Log.d("Espresso", "double comparison failed");
                    throw new RuntimeException("double comparison failed");
                  }
                  //check if we should consider to skip this gaol
                  if(scrollMaxScore<SKIP_SIMILARITY && graphMaxScore<SKIP_SIMILARITY){
                    duplicateStateSkippingGoal(goalClick);
                  }
                  //this must happen after duplicating state
                  this.currState.setScrollHeuristicCount(this.currState.getScrollHeuristicCount()+1);
                  //doing the scroll heuristic returned a result
                  //add as many scroll actions as scroll count
                  List<ActionScroll> scrollActionsList = new ArrayList<ActionScroll>();
                  for (int i = 0; i < this.scrollCount; ++i) {
                    scrollActionsList.add(new ActionScroll(this.scrollViewSelector.copy(), 1));//scrolling down because the heuristic scrolls down
                  }
                  //add actions to the current state
                  this.currState.getActionsList().addAll(scrollActionsList);
                  //reinitialize bookkeeping
                  this.scrollViewSelector = null;
                  this.scrollCount = 0;
                  //select target for this state
                  ScoredHierarchyTarget selectedTarget = scrollableTargetList.remove(selectedIndex);
                  //duplicate state non selected views,  this function makes sure of making target of click goal as specific as possible using xpath and marking them as search generated
                  duplicateState(goalClick, scrollableTargetList);
                  //get selected view
                  View selectedView = selectedTarget.getView();
                  Selector selectedSelector = findSelectorForView(selectedView);
                  //create action
                  Action action = null;
                  if (goalClick.getDuration() == 0) {
                    action = new ActionClick(selectedSelector);
                  } else if (goalClick.getDuration() == 1) {
                    action = new ActionClickLong(selectedSelector);
                  } else {
                    action = new ActionClickDouble(selectedSelector);
                  }
                  //update goal with selected target (this action is superfluous but using to keep it consistent)
                  goalClick.setTargetViewModel(selectedTarget.getViewModel());
                  //update current state
                  updateCurrState(goalClick, action);
                  //perform action
                  performAction(uiController, action, false);
                  //move to next goal
                  continue;
                }
              }
              //##############################try scroll heuristic########################################
              Log.d("Espresso", "goal click: try menu heuristic");
              if (hierarchyHasOptionsMenu()) {
                Log.d("Espresso", "goal click: has options menu");
                List<ScoredHierarchyTarget> optionsMenuTargetList = applyOptionsMenuHeuristic(uiController, goalClick.getTarget());
                if (optionsMenuTargetList.size() > 0) {
                  Log.d("Espresso", "goal click: options menu heuristic was successful");
                  //find the target with max similairty
                  double scrollMaxScore = 0;
                  for (ScoredHierarchyTarget scoredHierarchyTarget : optionsMenuTargetList) {
                    if (scoredHierarchyTarget.getScore() > scrollMaxScore) {
                      scrollMaxScore = scoredHierarchyTarget.getScore();
                    }
                  }
                  //select index
                  int selectedIndex = -1;
                  for (int i = 0; i < optionsMenuTargetList.size(); ++i) {
                    if (Double.compare(optionsMenuTargetList.get(i).getScore(), scrollMaxScore) == 0) {
                      selectedIndex = i;
                      break;
                    }
                  }
                  if (selectedIndex == -1) {
                    Log.d("Espresso", "double comparison failed");
                    throw new RuntimeException("double comparison failed");
                  }
                  //check if we should consider to skip this gaol
                  if(scrollMaxScore<SKIP_SIMILARITY && graphMaxScore<SKIP_SIMILARITY){
                    duplicateStateSkippingGoal(goalClick);
                  }
                  //this must happen after duplicating state
                  this.currState.setDrawerHeuristicCount(this.currState.getScrollHeuristicCount()+1);
                  //doing the options menu heuristic heuristic returned a result
                  //add actions to the current state
                  this.currState.getActionsList().add(new ActionOpenOptions(this.optionsMenuSelector.copy()));
                  //reinitialize bookkeeping
                  this.optionsMenuSelector = null;
                  //select target for this state
                  ScoredHierarchyTarget selectedTarget = optionsMenuTargetList.remove(selectedIndex);
                  //duplicate state non selected views,  this function makes sure of making target of click goal as specific as possible using xpath and marking them as search generated
                  duplicateState(goalClick, optionsMenuTargetList);
                  //get selected view
                  View selectedView = selectedTarget.getView();
                  Selector selectedSelector = findSelectorForView(selectedView);
                  //create action
                  Action action = null;
                  if (goalClick.getDuration() == 0) {
                    action = new ActionClick(selectedSelector);
                  } else if (goalClick.getDuration() == 1) {
                    action = new ActionClickLong(selectedSelector);
                  } else {
                    action = new ActionClickDouble(selectedSelector);
                  }
                  //update goal with selected target (this action is superfluous but using to keep it consistent)
                  goalClick.setTargetViewModel(selectedTarget.getViewModel());
                  //update current state
                  updateCurrState(goalClick, action);
                  //perform action
                  performAction(uiController, action, false);
                  //move to next goal
                  continue;
                }
              }

              //##############################check how likely is to find a match for this action########################
              //check if we should consider to skip this goal (no heuristic worked therefore checking only graph max score)
              if(graphMaxScore<SKIP_SIMILARITY){
                duplicateStateSkippingGoal(goalClick);
              }

              //#############################try to find the target by generating a random action###############################
              //change this target as being search generated because I do not want to generate walks again
              goalClick.getTarget().setFromText(false);
              //generated random action if allowed
              if (goalClick.getRandomCount() < RANDOM_COUNT_LIMIT) {
                Log.d("Espresso", "goal click: generate random action");
                //add curr goal in front
                this.remainingGoalsList.add(0, goalClick);
                //increase random count
                goalClick.setRandomCount(goalClick.getRandomCount()+1);
                this.currState.setRandomCount(this.currState.getRandomCount()+1);
                //generate random action
                Action randomAction = generateRandomAction();
                //add action to state
                this.currState.getActionsList().add(randomAction);
                //perform action
                performAction(uiController, randomAction, true);
                continue;
              } else {
                //#############################stop search###############################
                Log.d("Espresso", "goal click: reached max for random action");
                break;
              }
            }
          } else {
            Log.d("Espresso", "goal click: processing goal from search");
            //##############click goal generated by the search###########################
            if (goalClick.getTarget().getCanBePreference() && !goalClick.getTarget().getText().equals("")) {
              //try to load and click preference
              Log.d("Espresso", "goal click: trying preference heuristic");
              try {
                if (goalClick.getDuration() == 0) {
                  new DataInteraction(PreferenceMatchers.withTitleText(goalClick.getTarget().getText())).performNew(uiController, ViewActions.click());
                } else if (goalClick.getDuration() == 1) {
                  new DataInteraction(PreferenceMatchers.withTitleText(goalClick.getTarget().getText())).performNew(uiController, ViewActions.longClick());
                } else {
                  new DataInteraction(PreferenceMatchers.withTitleText(goalClick.getTarget().getText())).performNew(uiController, ViewActions.doubleClick());
                }
                Action action = null;
                if (goalClick.getDuration() == 0) {
                  action = new ActionPreferenceClick(goalClick.getTarget().getText());
                } else if (goalClick.getDuration() == 1) {
                  action = new ActionPreferenceClickLong(goalClick.getTarget().getText());
                } else {
                  action = new ActionPreferenceClickDouble(goalClick.getTarget().getText());
                }
                //not updating goal with selected target because it is identity and this action is superfluous
                //goalClick.setTargetViewModel(goalClick.getTarget());
                updateCurrState(goalClick, action);
                //try to close keyboard after preference heuristic was successfull
                closeKeyboard();
                continue;
              } catch (Exception e) {
                //check if exception was caused because of crash in app
                boolean isExceptionFromApp = false;
                StringWriter errors = new StringWriter();
                PrintWriter pw = new PrintWriter(errors);
                e.printStackTrace(pw);
                //mf: this should not be needed but there is some problem when there is an exception in jni
                if(e.getCause()!=null){
                  e.getCause().printStackTrace(pw);
                }
                if(errors.toString().contains(this.packageName)
                        || errors.toString().contains("android.support.v7")){
                  isExceptionFromApp = true;
                }
                if(isExceptionFromApp) {
                  //stop search only if remaining goal list is empty
                  if(remainingGoalsList.size()==0) {
                    this.finished = true;
                  }
                  //save actions and update state
                  Action action = null;
                  if (goalClick.getDuration() == 0) {
                    action = new ActionPreferenceClick(goalClick.getTarget().getText());
                  } else if (goalClick.getDuration() == 1) {
                    action = new ActionPreferenceClickLong(goalClick.getTarget().getText());
                  } else {
                    action = new ActionPreferenceClickDouble(goalClick.getTarget().getText());
                  }
                  //not updating goal with selected target because it is identity and this action is superfluous
                  //goalClick.setTargetViewModel(goalClick.getTarget());
                  updateCurrState(goalClick, action);
                  this.currState.setMessage(errors.toString());
                  saveExecutionState();
                  throw new RuntimeException(e);
                }
                else {
                  Log.d("Espresso", "preference heuristic did not work");
                }
              }
            }
            Log.d("Espresso", "goal click: checking hierarchy for target");
            List<ScoredHierarchyTarget> candidateViewList = findViewInUiHierarchyBasedOnTarget(uiController, goalClick.getTarget(), View.class);
            if (candidateViewList.size() > 0) {
              //more than one candidate can happen when in from text we generated a random action
              //select view with max score for current state, randomly pick one and make the selected one and others as specific as possible
              Log.d("Espresso", "goal click: hierarchy has target");
              //find max score
              double maxScore = 0;
              for (ScoredHierarchyTarget scoredHierarchyTarget : candidateViewList) {
                if (scoredHierarchyTarget.getScore() > maxScore) {
                  maxScore = scoredHierarchyTarget.getScore();
                }
              }
              //select candidate
              int selectedIndex = -1;
              for (int i = 0; i < candidateViewList.size(); ++i) {
                if (Double.compare(candidateViewList.get(i).getScore(), maxScore) == 0) {
                  selectedIndex = i;
                  break;
                }
              }
              if (selectedIndex == -1) {
                Log.d("Espresso", "double comparison failed");
                throw new RuntimeException("double comparison failed");
              }
              ScoredHierarchyTarget selectedTarget = candidateViewList.remove(selectedIndex);
              //duplicate state non selected views, this function makes sure of making target of click goal as specific as possible using xpath and marking them as search generated
              duplicateState(goalClick, candidateViewList);
              //get selected view
              View selectedView = selectedTarget.getView();
              Selector selectedSelector = findSelectorForView(selectedView);
              //create action
              Action action = null;
              if (goalClick.getDuration() == 0) {
                action = new ActionClick(selectedSelector);
              } else  if (goalClick.getDuration() == 1) {
                action = new ActionClickLong(selectedSelector);
              } else {
                action = new ActionClickDouble(selectedSelector);
              }
              //update goal with selected target (this action is superfluous but using to keep it consistent)
              goalClick.setTargetViewModel(selectedTarget.getViewModel());
              //update current state
              updateCurrState(goalClick, action);
              //perform action
              performAction(uiController, action, false);
              //move to next goal
              continue;
            } else {
              //##############################try drawer heuristic########################################
              Log.d("Espresso", "goal click: trying drawer heuristic");
              if (hierarchyHasDrawer()) {
                Log.d("Espresso", "goal click: has drawer");
                List<ScoredHierarchyTarget> drawerTargetList = applyDrawerHeuristic(uiController, goalClick.getTarget());
                if (drawerTargetList.size() > 0) {
                  Log.d("Espresso", "goal click: drawer heuristic was successful");
                  this.currState.setDrawerHeuristicCount(this.currState.getDrawerHeuristicCount()+1);
                  //doing the drawer heuristic returned a result
                  //add open drawer to the list of actions because it was successful in finding the target
                  ActionDrawer actionDrawer = new ActionDrawer(this.drawerViewSelector.copy(), 1);
                  this.currState.getActionsList().add(actionDrawer);
                  //reset selector
                  this.drawerViewSelector = null;
                  //find the target with max similarity
                  double drawerMaxScore = 0;
                  for (ScoredHierarchyTarget scoredHierarchyTarget : drawerTargetList) {
                    if (scoredHierarchyTarget.getScore() > drawerMaxScore) {
                      drawerMaxScore = scoredHierarchyTarget.getScore();
                    }
                  }
                  int selectedIndex = -1;
                  for (int i = 0; i < drawerTargetList.size(); ++i) {
                    if (Double.compare(drawerTargetList.get(i).getScore(), drawerMaxScore) == 0) {
                      selectedIndex = i;
                      break;
                    }
                  }
                  if (selectedIndex == -1) {
                    Log.d("Espresso", "double comparison failed");
                    throw new RuntimeException("double comparison failed");
                  }
                  //select target for this state
                  ScoredHierarchyTarget selectedTarget = drawerTargetList.remove(selectedIndex);
                  //duplicate state non selected views,  this function makes sure of making target of click goal as specific as possible using xpath and marking them as search generated
                  duplicateState(goalClick, drawerTargetList);
                  //get selected view
                  View selectedView = selectedTarget.getView();
                  Selector selectedSelector = findSelectorForView(selectedView);
                  //create action
                  Action action = null;
                  if (goalClick.getDuration() == 0) {
                    action = new ActionClick(selectedSelector);
                  } else if (goalClick.getDuration() == 1) {
                    action = new ActionClickLong(selectedSelector);
                  } else {
                    action = new ActionClickDouble(selectedSelector);
                  }
                  //update goal with selected target (this action is superfluous but using to keep it consistent)
                  goalClick.setTargetViewModel(selectedTarget.getViewModel());
                  //update current state
                  updateCurrState(goalClick, action);
                  //perform action
                  performAction(uiController, action, false);
                  //move to next goal
                  continue;
                }
              }
              //##############################try scroll heuristic########################################
              Log.d("Espresso", "goal click: trying scroll heuristic");
              if (hierarchyHasScrollableView()) {
                Log.d("Espresso", "goal click: has scrollable view");
                List<ScoredHierarchyTarget> scrollableTargetList = applyScrollHeuristic(uiController, goalClick.getTarget());
                if (scrollableTargetList.size() > 0) {
                  Log.d("Espresso", "goal click: scroll heuristic was successful");
                  this.currState.setScrollHeuristicCount(this.currState.getScrollHeuristicCount()+1);
                  //doing the scroll heuristic returned a result
                  //add as many scroll actions as scroll count
                  List<ActionScroll> scrollActionsList = new ArrayList<ActionScroll>();
                  for (int i = 0; i < this.scrollCount; ++i) {
                    scrollActionsList.add(new ActionScroll(this.scrollViewSelector.copy(), 1));//scrolling down because the heuristic scrolls down
                  }
                  //add actions to the current state
                  this.currState.getActionsList().addAll(scrollActionsList);
                  //reinitialize bookkeeping
                  this.scrollViewSelector = null;
                  this.scrollCount = 0;
                  //find the target with max similairty
                  double scrollMaxScore = 0;
                  for (ScoredHierarchyTarget scoredHierarchyTarget : scrollableTargetList) {
                    if (scoredHierarchyTarget.getScore() > scrollMaxScore) {
                      scrollMaxScore = scoredHierarchyTarget.getScore();
                    }
                  }
                  int selectedIndex = -1;
                  for (int i = 0; i < scrollableTargetList.size(); ++i) {
                    if (Double.compare(scrollableTargetList.get(i).getScore(), scrollMaxScore) == 0) {
                      selectedIndex = i;
                      break;
                    }
                  }
                  if (selectedIndex == -1) {
                    Log.d("Espresso", "double comparison failed");
                    throw new RuntimeException("double comparison failed");
                  }
                  //select target for this state
                  ScoredHierarchyTarget selectedTarget = scrollableTargetList.remove(selectedIndex);
                  //duplicate state non selected views,  this function makes sure of making target of click goal as specific as possible using xpath and marking them as search generated
                  duplicateState(goalClick, scrollableTargetList);
                  //get selected view
                  View selectedView = selectedTarget.getView();
                  Selector selectedSelector = findSelectorForView(selectedView);
                  //create action
                  Action action = null;
                  if (goalClick.getDuration() == 0) {
                    action = new ActionClick(selectedSelector);
                  } else if (goalClick.getDuration() == 1) {
                    action = new ActionClickLong(selectedSelector);
                  } else {
                    action = new ActionClickDouble(selectedSelector);
                  }
                  //update goal with selected target (this action is superfluous but using to keep it consistent)
                  goalClick.setTargetViewModel(selectedTarget.getViewModel());
                  //update current state
                  updateCurrState(goalClick, action);
                  //perform action
                  performAction(uiController, action, false);
                  //move to next goal
                  continue;
                }
              }
              Log.d("Espresso", "goal click: try menu heuristic");
              if (hierarchyHasOptionsMenu()) {
                Log.d("Espresso", "goal click: has options menu");
                List<ScoredHierarchyTarget> optionsMenuTargetList = applyOptionsMenuHeuristic(uiController, goalClick.getTarget());
                if (optionsMenuTargetList.size() > 0) {
                  Log.d("Espresso", "goal click: options menu heuristic was successful");
                  //find the target with max similairty
                  double scrollMaxScore = 0;
                  for (ScoredHierarchyTarget scoredHierarchyTarget : optionsMenuTargetList) {
                    if (scoredHierarchyTarget.getScore() > scrollMaxScore) {
                      scrollMaxScore = scoredHierarchyTarget.getScore();
                    }
                  }
                  //select index
                  int selectedIndex = -1;
                  for (int i = 0; i < optionsMenuTargetList.size(); ++i) {
                    if (Double.compare(optionsMenuTargetList.get(i).getScore(), scrollMaxScore) == 0) {
                      selectedIndex = i;
                      break;
                    }
                  }
                  if (selectedIndex == -1) {
                    Log.d("Espresso", "double comparison failed");
                    throw new RuntimeException("double comparison failed");
                  }
                  //this must happen after duplicating state
                  this.currState.setDrawerHeuristicCount(this.currState.getScrollHeuristicCount()+1);
                  //doing the options menu heuristic heuristic returned a result
                  //add actions to the current state
                  this.currState.getActionsList().add(new ActionOpenOptions(this.optionsMenuSelector.copy()));
                  //reinitialize bookkeeping
                  this.optionsMenuSelector = null;
                  //select target for this state
                  ScoredHierarchyTarget selectedTarget = optionsMenuTargetList.remove(selectedIndex);
                  //duplicate state non selected views,  this function makes sure of making target of click goal as specific as possible using xpath and marking them as search generated
                  duplicateState(goalClick, optionsMenuTargetList);
                  //get selected view
                  View selectedView = selectedTarget.getView();
                  Selector selectedSelector = findSelectorForView(selectedView);
                  //create action
                  Action action = null;
                  if (goalClick.getDuration() == 0) {
                    action = new ActionClick(selectedSelector);
                  } else if (goalClick.getDuration() == 1) {
                    action = new ActionClickLong(selectedSelector);
                  } else {
                    action = new ActionClickDouble(selectedSelector);
                  }
                  //update goal with selected target (this action is superfluous but using to keep it consistent)
                  goalClick.setTargetViewModel(selectedTarget.getViewModel());
                  //update current state
                  updateCurrState(goalClick, action);
                  //perform action
                  performAction(uiController, action, false);
                  //move to next goal
                  continue;
                }
              }

              //#############################try to find the target by generating a random action###############################
              //generated random action if allowed
              if (goalClick.getRandomCount() < RANDOM_COUNT_LIMIT) {
                Log.d("Espresso", "goal click: generate random action");
                //add curr goal in front
                this.remainingGoalsList.add(0, goalClick);
                //increase random count
                goalClick.setRandomCount(goalClick.getRandomCount()+1);
                this.currState.setRandomCount(this.currState.getRandomCount()+1);
                //generate random action
                Action randomAction = generateRandomAction();
                //add action to state
                this.currState.getActionsList().add(randomAction);
                //perform action
                performAction(uiController, randomAction, true);
                continue;
              } else {
                //#############################stop search###############################
                Log.d("Espresso", "goal click: reached max for random action");
                break;
              }
            }
          }
        } else if (currGoal instanceof GoalScroll) {
          //printHierarchy();
          GoalScroll goalScroll = (GoalScroll) currGoal;
          Log.d("Espresso", "goal scroll: scrolling in direction " + goalScroll.getDirection());
          View scrollView = findScrollElement();
          ActionScroll actionScroll = new ActionScroll(findSelectorForView(scrollView), goalScroll.getDirection());
          //update current state
          updateCurrState(goalScroll, actionScroll);
          //perform action
          performAction(uiController, actionScroll, false);
          //move to next goal
          continue;
        } else if (currGoal instanceof GoalScrollComplete) {
          //printHierarchy();
          GoalScrollComplete goalScrollComplete = (GoalScrollComplete) currGoal;
          Log.d("Espresso", "goal scroll complete: scrolling in direction " + goalScrollComplete.getDirection());
          scrollComplete(uiController, goalScrollComplete.getDirection());
          List<Action> scrollActionsList = new ArrayList<Action>();
          for (int i = 0; i < this.scrollCount; ++i) {
            scrollActionsList.add(new ActionScroll(this.scrollViewSelector.copy(), goalScrollComplete.getDirection()));
          }
          //reinitialize bookkeeping
          this.scrollViewSelector = null;
          this.scrollCount = 0;
          //update current state
          updateCurrState(goalScrollComplete, scrollActionsList);
          continue;
        } else if (currGoal instanceof GoalSwipe) {
          //printHierarchy();
          GoalSwipe goalSwipe = (GoalSwipe) currGoal;
          Log.d("Espresso", "goal swipe: swiping in direction " + goalSwipe.getDirection());
          View swipeView = findScrollElement();
          ActionSwipe actionSwipe = new ActionSwipe(findSelectorForView(swipeView), goalSwipe.getDirection());
          //update current state
          updateCurrState(goalSwipe, actionSwipe);
          //perform action
          performAction(uiController, actionSwipe, false);
          //move to next goal
          continue;
        } else if (currGoal instanceof GoalClickPosition) {
          //printHierarchy();
          GoalClickPosition goalClickPosition = (GoalClickPosition) currGoal;
          Log.d("Espresso", "goal click position: clicking element " + goalClickPosition.getPosition() + " in " + goalClickPosition.getContainer());
          //assumption both container and positions are present
          View selectedView = findElementInContainerBasedOnPosition(goalClickPosition.getContainer(), goalClickPosition.getPosition());
          Selector selectedSelector = findSelectorForView(selectedView);
          //create action
          Action action = null;
          if (goalClickPosition.getDuration() == 0) {
            action = new ActionClick(selectedSelector);
          } else if (goalClickPosition.getDuration() == 1) {
            action = new ActionClickLong(selectedSelector);
          } else {
            action = new ActionClickDouble(selectedSelector);
          }
          //update current state
          updateCurrState(goalClickPosition, action);
          //perform action
          performAction(uiController, action, false);
          //move to next goal
          continue;
        } else if (currGoal instanceof GoalOpenOptions) {
          //printHierarchy();
          GoalOpenOptions goalOpenOptions = (GoalOpenOptions) currGoal;
          Log.d("Espresso", "goal open options");
          View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
          if (!(rootView != null && rootView.getVisibility() == View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))) {
            Log.d("Espresso", "root view is not visible for open options");
            throw new RuntimeException("root view is not visible for open options");
          }
          Selector rootSelector = findSelectorForView(rootView);
          //create action
          Action action = new ActionOpenOptions(rootSelector);
          //update current state
          updateCurrState(goalOpenOptions, action);
          //perform action
          performAction(uiController, action, false);
          //move to next goal
          continue;
        } else if (currGoal instanceof GoalRotate) {
          //printHierarchy();
          GoalRotate goalRotate = (GoalRotate) currGoal;
          Log.d("Espresso", "goal rotate");
          View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
          if (!(rootView != null && rootView.getVisibility() == View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))) {
            Log.d("Espresso", "root view is not visible for rotate");
            throw new RuntimeException("root view is not visible for rotate");
          }
          Selector rootSelector = findSelectorForView(rootView);
          //create action
          Action action = new ActionRotate(rootSelector);
          //update current state
          updateCurrState(goalRotate, action);
          //perform action
          performAction(uiController, action, false);
          //move to next goal
          continue;
        } else if (currGoal instanceof GoalBack) {
          //printHierarchy();
          GoalBack goalBack = (GoalBack) currGoal;
          Log.d("Espresso", "goal back");
          View rootView = Espresso.BASE.plus(new ViewInteractionModule(ViewMatchers.isRoot())).viewInteraction().getView();
          if (!(rootView != null && rootView.getVisibility() == View.VISIBLE && rootView.getGlobalVisibleRect(new Rect()))) {
            Log.d("Espresso", "root view is not visible for back");
            throw new RuntimeException("root view is not visible for back");
          }
          Selector rootSelector = findSelectorForView(rootView);
          //create action
          Action action = new ActionBack(rootSelector);
          //update current state
          updateCurrState(goalBack, action);
          //perform action
          performAction(uiController, action, false);
          //move to next goal
          continue;
        } else if (currGoal instanceof GoalType){
          //printHierarchy();
          GoalType goalType = (GoalType) currGoal;
          if (goalType.getTarget().isFromText()) {
            Log.d("Espresso", "goal type: processing goal from text:" + goalType.getTarget().getText());
            //##############type goal generated by nlp processing########################
            Log.d("Espresso", "goal type: checking hierarchy for target");
            if(isGenericTarget(goalType.getTarget().getText())){
              View selectedView = getRandomEditor();
              if(selectedView!=null){
                Log.d("Espresso", "goal type: generic view found");
                String text = findTextBasedOnInputs(uiController, goalType.getTarget().getText());
                Selector selectedSelector = findSelectorForView(selectedView);
                Action action = new ActionType(selectedSelector, text);
                //update state
                updateCurrState(goalType, action);
                //perform action
                performAction(uiController, action, false);
                //move to next goal
                continue;
              }
            }

            List<ScoredHierarchyTarget> candidateViewList = findViewInUiHierarchyBasedOnTarget(uiController, goalType.getTarget(), EditText.class);
            //find max score
            double maxScore = 0;
            for (ScoredHierarchyTarget scoredHierarchyTarget : candidateViewList) {
              if (scoredHierarchyTarget.getScore() > maxScore) {
                maxScore = scoredHierarchyTarget.getScore();
              }
            }
            //find if there would be another activity with a better match
            Log.d("Espresso", "goal type: checking graph for better match");
            double graphMaxScore = findBetterMatch(uiController, goalType, goalType.getTarget(), maxScore, "android.widget.EditText");
            //process results based on current hierarchy
            if (candidateViewList.size() > 0) {
              Log.d("Espresso", "goal type: hierarchy has target");
              //select candidate
              int selectedIndex = -1;
              for (int i = 0; i < candidateViewList.size(); ++i) {
                if (Double.compare(candidateViewList.get(i).getScore(), maxScore) == 0) {
                  selectedIndex = i;
                  break;
                }
              }
              if (selectedIndex == -1) {
                Log.d("Espresso", "double comparison failed");
                throw new RuntimeException("double comparison failed");
              }
              //check if we should consider to skip this gaol
              if(maxScore<SKIP_SIMILARITY && graphMaxScore<SKIP_SIMILARITY){
                duplicateStateSkippingGoal(goalType);
              }
              ScoredHierarchyTarget selectedTarget = candidateViewList.remove(selectedIndex);
              //duplicate state non selected views, this function makes sure of making target of click goal as specific as possible using xpath and marking them as search generated
              duplicateState(goalType, candidateViewList);
              //get selected view
              View selectedView = selectedTarget.getView();
              Selector selectedSelector = findSelectorForView(selectedView);
              //create action
              String text=goalType.getText();
              if(text.equals("")){
                text = findTextBasedOnInputs(uiController, goalType.getTarget().getText());
              }
              if(text.equals("empty")){
                text = "";
              }
              Action action = new ActionType(selectedSelector, text);
              //update goal with selected target (this action is superfluous but using to keep it consistent)
              goalType.setTargetViewModel(selectedTarget.getViewModel());
              //update state
              updateCurrState(goalType, action);
              //perform action
              performAction(uiController, action, false);
              //move to next goal
              continue;
            } else {
              //using only scroll heuristic because never found an edit text in a drawer
              //##############################try scroll heuristic########################################
              Log.d("Espresso", "goal type: trying scroll heuristic");
              if (hierarchyHasScrollableView()) {
                Log.d("Espresso", "goal type: has scrollable view");
                List<ScoredHierarchyTarget> scrollableTargetList = applyScrollHeuristic(uiController, goalType.getTarget());
                if (scrollableTargetList.size() > 0) {
                  Log.d("Espresso", "goal type: scroll heuristic was successful");
                  //find the target with max similairty
                  double scrollMaxScore = 0;
                  for (ScoredHierarchyTarget scoredHierarchyTarget : scrollableTargetList) {
                    if (scoredHierarchyTarget.getScore() > scrollMaxScore) {
                      scrollMaxScore = scoredHierarchyTarget.getScore();
                    }
                  }
                  int selectedIndex = -1;
                  for (int i = 0; i < scrollableTargetList.size(); ++i) {
                    if (Double.compare(scrollableTargetList.get(i).getScore(), scrollMaxScore) == 0) {
                      selectedIndex = i;
                      break;
                    }
                  }
                  if (selectedIndex == -1) {
                    Log.d("Espresso", "double comparison failed");
                    throw new RuntimeException("double comparison failed");
                  }
                  //check if we should consider to skip this gaol
                  if(scrollMaxScore<SKIP_SIMILARITY && graphMaxScore<SKIP_SIMILARITY){
                    duplicateStateSkippingGoal(goalType);
                  }
                  //this must happen after
                  this.currState.setScrollHeuristicCount(this.currState.getScrollHeuristicCount()+1);
                  //doing the scroll heuristic returned a result
                  //add as many scroll actions as scroll count
                  List<ActionScroll> scrollActionsList = new ArrayList<ActionScroll>();
                  for (int i = 0; i < this.scrollCount; ++i) {
                    scrollActionsList.add(new ActionScroll(this.scrollViewSelector.copy(), 1));//scrolling down because the heuristic scrolls down
                  }
                  //add actions to the current state
                  this.currState.getActionsList().addAll(scrollActionsList);
                  //reinitialize bookkeeping
                  this.scrollViewSelector = null;
                  this.scrollCount = 0;
                  //select target for this state
                  ScoredHierarchyTarget selectedTarget = scrollableTargetList.remove(selectedIndex);
                  //duplicate state non selected views,  this function makes sure of making target of click goal as specific as possible using xpath and marking them as search generated
                  duplicateState(goalType, scrollableTargetList);
                  //get selected view
                  View selectedView = selectedTarget.getView();
                  Selector selectedSelector = findSelectorForView(selectedView);
                  //find text
                  String text=goalType.getText();
                  if(text.equals("")){
                    text = findTextBasedOnInputs(uiController, goalType.getTarget().getText());
                  }
                  if(text.equals("empty")){
                    text = "";
                  }
                  Action action = new ActionType(selectedSelector, text);
                  //update goal with selected target (this action is superfluous but using to keep it consistent)
                  goalType.setTargetViewModel(selectedTarget.getViewModel());
                  //update current state
                  updateCurrState(goalType, action);
                  //perform action
                  performAction(uiController, action, false);
                  //move to next goal
                  continue;
                }
              }
              //##############################check how likely is to find a match for this action########################
              //check if we should consider to skip this goal
              if(graphMaxScore<SKIP_SIMILARITY) {
                duplicateStateSkippingGoal(goalType);
              }
              //#############################try to find the target by generating a random action###############################
              //change this target as being search generated because I do not want to generate walks again
              goalType.getTarget().setFromText(false);
              //generated random action if allowed
              if (goalType.getRandomCount() < RANDOM_COUNT_LIMIT) {
                Log.d("Espresso", "goal type: generate random action");
                //add curr goal in front
                goalType.setRandomCount(goalType.getRandomCount()+1);
                this.remainingGoalsList.add(0, goalType);
                //increase random count
                this.currState.setRandomCount(this.currState.getRandomCount()+1);
                //generate random action
                Action randomAction = generateRandomAction();
                //add action to state
                this.currState.getActionsList().add(randomAction);
                //perform action
                performAction(uiController, randomAction, true);
                continue;
              } else {
                //#############################stop search###############################
                Log.d("Espresso", "goal type: reached max for random action");
                break;
              }
            }
          }
          else{
            Log.d("Espresso", "goal type: processing goal from search");
            //##############click goal generated by the search###########################
            Log.d("Espresso", "goal type: checking hierarchy for target");
            if(isGenericTarget(goalType.getTarget().getText())){
              View selectedView = getRandomEditor();
              if(selectedView!=null){
                Log.d("Espresso", "goal type: generic view found");
                String text = findTextBasedOnInputs(uiController, goalType.getTarget().getText());
                Selector selectedSelector = findSelectorForView(selectedView);
                Action action = new ActionType(selectedSelector, text);
                //update state
                updateCurrState(goalType, action);
                //perform action
                performAction(uiController, action, false);
                //move to next goal
                continue;
              }
            }
            List<ScoredHierarchyTarget> candidateViewList = findViewInUiHierarchyBasedOnTarget(uiController, goalType.getTarget(), EditText.class);
            //process results based on current hierarchy
            if (candidateViewList.size() > 0) {
              Log.d("Espresso", "goal type: hierarchy has target");
              //find max score
              double maxScore = 0;
              for (ScoredHierarchyTarget scoredHierarchyTarget : candidateViewList) {
                if (scoredHierarchyTarget.getScore() > maxScore) {
                  maxScore = scoredHierarchyTarget.getScore();
                }
              }
              //select candidate
              int selectedIndex = -1;
              for (int i = 0; i < candidateViewList.size(); ++i) {
                if (Double.compare(candidateViewList.get(i).getScore(), maxScore) == 0) {
                  selectedIndex = i;
                  break;
                }
              }
              if (selectedIndex == -1) {
                Log.d("Espresso", "double comparison failed");
                throw new RuntimeException("double comparison failed");
              }
              ScoredHierarchyTarget selectedTarget = candidateViewList.remove(selectedIndex);
              //duplicate state non selected views, this function makes sure of making target of click goal as specific as possible using xpath and marking them as search generated
              duplicateState(goalType, candidateViewList);
              //get selected view
              View selectedView = selectedTarget.getView();
              Selector selectedSelector = findSelectorForView(selectedView);
              //create action
              String text=goalType.getText();
              if(text.equals("")){
                text = findTextBasedOnInputs(uiController, goalType.getTarget().getText());
              }
              if(text.equals("empty")){
                text = "";
              }
              Action action = new ActionType(selectedSelector, text);
              //update goal with selected target (this action is superfluous but using to keep it consistent)
              goalType.setTargetViewModel(selectedTarget.getViewModel());
              //update state
              updateCurrState(goalType, action);
              //perform action
              performAction(uiController, action, false);
              //move to next goal
              continue;
            } else {
              //using only scroll heuristic because never found an edit text in a drawer
              //##############################try scroll heuristic########################################
              Log.d("Espresso", "goal type: trying scroll heuristic");
              if (hierarchyHasScrollableView()) {
                Log.d("Espresso", "goal type: has scrollable view");
                List<ScoredHierarchyTarget> scrollableTargetList = applyScrollHeuristic(uiController, goalType.getTarget());
                if (scrollableTargetList.size() > 0) {
                  Log.d("Espresso", "goal type: scroll heuristic was successful");
                  this.currState.setScrollHeuristicCount(this.currState.getScrollHeuristicCount()+1);
                  //doing the scroll heuristic returned a result
                  //add as many scroll actions as scroll count
                  List<ActionScroll> scrollActionsList = new ArrayList<ActionScroll>();
                  for (int i = 0; i < this.scrollCount; ++i) {
                    scrollActionsList.add(new ActionScroll(this.scrollViewSelector.copy(), 1));//scrolling down because the heuristic scrolls down
                  }
                  //add actions to the current state
                  this.currState.getActionsList().addAll(scrollActionsList);
                  //reinitialize bookkeeping
                  this.scrollViewSelector = null;
                  this.scrollCount = 0;
                  //find the target with max similarity
                  double scrollMaxScore = 0;
                  for (ScoredHierarchyTarget scoredHierarchyTarget : scrollableTargetList) {
                    if (scoredHierarchyTarget.getScore() > scrollMaxScore) {
                      scrollMaxScore = scoredHierarchyTarget.getScore();
                    }
                  }
                  int selectedIndex = -1;
                  for (int i = 0; i < scrollableTargetList.size(); ++i) {
                    if (Double.compare(scrollableTargetList.get(i).getScore(), scrollMaxScore) == 0) {
                      selectedIndex = i;
                      break;
                    }
                  }
                  if (selectedIndex == -1) {
                    Log.d("Espresso", "double comparison failed");
                    throw new RuntimeException("double comparison failed");
                  }
                  //select target for this state
                  ScoredHierarchyTarget selectedTarget = scrollableTargetList.remove(selectedIndex);
                  //duplicate state non selected views,  this function makes sure of making target of click goal as specific as possible using xpath and marking them as search generated
                  duplicateState(goalType, scrollableTargetList);
                  //get selected view
                  View selectedView = selectedTarget.getView();
                  Selector selectedSelector = findSelectorForView(selectedView);
                  //find text
                  String text = goalType.getText();
                  if (text.equals("")) {
                    text = findTextBasedOnInputs(uiController, goalType.getTarget().getText());
                  }
                  if(text.equals("empty")){
                    text = "";
                  }
                  Action action = new ActionType(selectedSelector, text);
                  //update goal with selected target (this action is superfluous but using to keep it consistent)
                  goalType.setTargetViewModel(selectedTarget.getViewModel());
                  //update current state
                  updateCurrState(goalType, action);
                  //perform action
                  performAction(uiController, action, false);
                  //move to next goal
                  continue;
                }
              }
              //#############################try to find the target by generating a random action###############################
              //generated random action if allowed
              if (goalType.getRandomCount() < RANDOM_COUNT_LIMIT) {
                Log.d("Espresso", "goal type: generate random action");
                //add curr goal in front
                this.remainingGoalsList.add(0, goalType);
                //increase random count
                goalType.setRandomCount(goalType.getRandomCount()+1);
                this.currState.setRandomCount(this.currState.getRandomCount() + 1);
                //generate random action
                Action randomAction = generateRandomAction();
                //add action to state
                this.currState.getActionsList().add(randomAction);
                //perform action
                performAction(uiController, randomAction, true);
                continue;
              } else {
                //#############################stop search###############################
                Log.d("Espresso", "goal type: reached max for random action");
                break;
              }
            }
          }
        } else if (currGoal instanceof GoalClickGeneral) {
          Log.d("Espresso", "goal click general");
          //printHierarchy();
          GoalClickGeneral goalClickGeneral = (GoalClickGeneral) currGoal;
          duplicateState(goalClickGeneral, new ArrayList<ScoredHierarchyTarget>());
          //transform this goal into a click goal
          GoalClick newGoal = new GoalClick(goalClickGeneral.isSatisfied(), goalClickGeneral.isFromGraph(), goalClickGeneral.getTarget().copy(), goalClickGeneral.getDuration(), 0);
          //ad new goal to remaining goal list
          this.remainingGoalsList.add(0, newGoal);
          //move to next goal
          continue;
        } else {
          Log.d("Espresso", "handle new type of goal while doing search");
          throw new RuntimeException("handle new type of goal while doing search");
        }
      }

      if(this.crash){
        //try to generate MAX LIMIT RANDOM ACTIONS
        for(int i=0; i<RANDOM_COUNT_LIMIT; i++) {
          Log.d("Espresso", "generating random action to try to crash");
          this.currState.setRandomCount(this.currState.getRandomCount() + 1);
          //generate random action
          Action randomAction = generateRandomAction();
          //add action to state
          this.currState.getActionsList().add(randomAction);
          //perform action (do not consider this as random action of a goal)
          performAction(uiController, randomAction, false);
        }
        //we finished but crash was not generated
        this.currState.setMessage("Terminated but did not cause crash");
        this.finished=false;
      }
      else{
        boolean allSatisfied = true;
        for (int i = 0; i < this.satisfiedGoalsList.size(); ++i) {
          if(!this.satisfiedGoalsList.get(i).isSatisfied()){
            allSatisfied = false;
            break;
          }
        }

        for (int i = 0; i < this.satisfiedGoalsList.size(); ++i) {
          Log.d("Espresso", this.satisfiedGoalsList.get(i).toJSON().toString());
        }
        Log.d("Espresso", "allSatisfied:"+allSatisfied);
        Log.d("Espresso", "remainingGoalsList:"+remainingGoalsList.size());
        Log.d("Espresso", "skipped:"+this.currState.getSkippedGoalCount());

        //first check for checking all goals were satisfied, second check in case max number of random actions was reached, third check no skipped gaols
        if(allSatisfied && this.remainingGoalsList.size()==0 && this.currState.getSkippedGoalCount()==0){
          this.currState.setMessage("Terminated satisfying all goals without crash as expressed.");
          Log.d("Espresso", "Terminated satisfying all goals without crash as expressed.");
          this.finished=true;
        }
        else{
          this.currState.setMessage("Terminated but check");
          Log.d("Espresso", "Terminated but check");
          this.finished=false;
        }
      }
      //save execution state if not done already
      if(!this.savedExecutionState) {
        saveExecutionState();
      }
    }
    catch(Exception e){
      if(!this.savedExecutionState){
        StringWriter errors = new StringWriter();
        PrintWriter pw = new PrintWriter(errors);
        e.printStackTrace(pw);
        //mf: this should not be needed but there is some problem when there is an exception in jni
        if(e.getCause()!=null){
          e.getCause().printStackTrace(pw);
        }
        Log.d("Espresso", errors.toString());
        this.currState.setMessage(errors.toString());
        //this is a bug in the search (e.g., did not find root view at some point)
        saveExecutionState();
      }
    }

    Log.d("Espresso", "search ended");
  }

  //******************************************graph and server requests************************************************************

  private JSONObject sendGraphRequest(UiController uiController, String graphQuery){
    JSONObject result = new JSONObject();
    try {
      JSONObject graphQueryStmt = new JSONObject();
      graphQueryStmt.put("statement", graphQuery);
      JSONArray graphQueryStmts = new JSONArray();
      graphQueryStmts.put(graphQueryStmt);
      JSONObject graphQueryParams = new JSONObject();
      graphQueryParams.put("statements", graphQueryStmts);
      new GraphRequest().execute(graphQueryParams);
      while (!requestCompleted) {
        uiController.loopMainThreadForAtLeast(100);
      }
      //return a copy of the object as result
      result = new JSONObject(requestResult.toString());
      if(result.has("exception")){
        Log.d("Espresso", "send graph request exception");
        //allow the graph db to catch up
        uiController.loopMainThreadForAtLeast(1000);
      }
    }
    catch(JSONException je){
      Log.d("Espresso", "Exception when running graph query:"+graphQuery);
      result = new JSONObject();
      //allow the graph db to catch up
      uiController.loopMainThreadForAtLeast(1000);
    }
    requestCompleted=false;
    requestResult=null;
    return result;
  }

  private class GraphRequest extends AsyncTask<JSONObject, Void, Boolean> {
    @Override
    protected Boolean doInBackground(JSONObject...params) {
      boolean exception = false;
      try {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "http://"+graphAddress+":7474" + "/db/data/transaction/commit", params[0], future, future) {
          @Override
          public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("accept", "application/json");
            headers.put("content-type", "application/json");
            headers.put("Authorization", "Basic " + graphToken);
            return headers;
          }
        };
        request.setShouldCache(false);
        //requestQueue.getCache().clear();
        requestQueue.add(request);
        JSONObject response = future.get(requestTimeout, TimeUnit.SECONDS);
        requestResult=response;

      } catch (Exception e) {
        StringWriter errors = new StringWriter();
        PrintWriter pw = new PrintWriter(errors);
        e.printStackTrace(pw);
        Log.d("Espresso", "error with volley:"+errors.toString());
        //requestQueue = Volley.newRequestQueue(appContext);
        exception = true;
      }
      if(exception){
        try {
          requestResult = new JSONObject();
          requestResult.put("exception", "exception");
          return new Boolean(false);
        }
        catch(Exception e){
          return new Boolean(false);
        }
      }
      return new Boolean(true);
    }

    @Override
    protected void onPostExecute(Boolean result) {
      requestCompleted = true;
    }
  }


  private JSONObject sendServerRequest(UiController uiController, String sentence1, String sentence2){
    JSONObject result = new JSONObject();
    try {
      JSONObject serverRequestJSON = new JSONObject();
      serverRequestJSON.put("sentence1", sentence1);
      serverRequestJSON.put("sentence2", sentence2);
      new ServerRequest().execute(serverRequestJSON);
      while (!requestCompleted) {
        uiController.loopMainThreadForAtLeast(100);
      }
      //return a copy of the object as result
      result = new JSONObject(requestResult.toString());
      if(result.has("exception")){
        //allow server to catch up
        Log.d("Espresso", "send server request exception");
        uiController.loopMainThreadForAtLeast(1000);
      }
    }
    catch(JSONException je){
      Log.d("Espresso", "Exception when running server request:"+sentence1+"#"+sentence2);
      result = new JSONObject();
      //allow server to catch up
      uiController.loopMainThreadForAtLeast(1000);
    }
    requestCompleted=false;
    requestResult=null;
    return result;
  }

  private class ServerRequest extends AsyncTask<JSONObject, Void, Boolean> {
    @Override
    protected Boolean doInBackground(JSONObject...params) {
      boolean exception = false;
      try {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "http://"+serverAddress+":9000", params[0], future, future) {
          @Override
          public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("accept", "application/json");
            headers.put("content-type", "application/json");
            return headers;
          }
        };
        request.setShouldCache(false);
        //requestQueue.getCache().clear();
        requestQueue.add(request);
        JSONObject response = future.get(requestTimeout, TimeUnit.SECONDS);
        requestResult=response;

      } catch (Exception e) {
        StringWriter errors = new StringWriter();
        PrintWriter pw = new PrintWriter(errors);
        e.printStackTrace(pw);
        Log.d("Espresso", "error with volley:"+errors.toString());
        //requestQueue = Volley.newRequestQueue(appContext);
        exception = true;
      }
      if(exception){
        try {
          requestResult = new JSONObject();
          requestResult.put("exception", "exception");
          return new Boolean(false);
        }
        catch(Exception e){
          return new Boolean(false);
        }
      }
      return new Boolean(true);
    }

    @Override
    protected void onPostExecute(Boolean result) {
      requestCompleted = true;
    }
  }


  @Override
  public String getDescription() {
    return String.format("Search action");
  }
}
