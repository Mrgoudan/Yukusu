package yakusu.android.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import org.json.JSONArray;
import org.json.JSONObject;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import yakusu.android.nlp.Goal;
import yakusu.android.nlp.GoalBack;
import yakusu.android.nlp.GoalClick;
import yakusu.android.nlp.GoalClickGeneral;
import yakusu.android.nlp.GoalClickPosition;
import yakusu.android.nlp.GoalOpenOptions;
import yakusu.android.nlp.GoalRotate;
import yakusu.android.nlp.GoalScroll;
import yakusu.android.nlp.GoalScrollComplete;
import yakusu.android.nlp.GoalSwipe;
import yakusu.android.nlp.GoalType;
import yakusu.android.nlp.TargetViewModel;

public class TestGenerator {
	public static void main(String[] args) {
		if(args.length!=6){
			System.out.println("usage: java -jar TestGenerator.jar app_package_name ontology.json steps.json generate_states.json testpath testname");
			System.exit(-1);
		}
		
		TestGenerator generateGoals = new TestGenerator();
		generateGoals.generateTest(args[0], args[1], args[2], args[3], args[4], args[5]);
	}
	
	private void generateTest(String packageName, String ontologyFileName, String stepsFileName, String generatedStatesFileName, String testPath, String testName){
		try{
			
			//read ontology file
			String ontologyContent = "";
			FileInputStream ontologyFis = new FileInputStream(ontologyFileName);
		    DataInputStream ontologyDis = new DataInputStream(ontologyFis);
		    BufferedReader ontologyBr = new BufferedReader(new InputStreamReader(ontologyDis));
		    String line = "";
		    while ((line = ontologyBr.readLine()) != null) {
		    	ontologyContent = ontologyContent + line;
		    }		   
		    ontologyBr.close();
		    JSONObject ontologyJSON = new JSONObject(ontologyContent);
		    String firstActivityName = ontologyJSON.getString("main_activity");
		    
		    //mf: read steps
		    //List<Action> actionsList = new ArrayList<Action>();
		    List<Action> stepsList = readSteps(stepsFileName);
		    //computed actions
		    //List<List<Action>> actionsListList = new ArrayList<List<Action>>();
		    List<State> selectedStateList = new ArrayList<State>();
			//mf: read generated states file find best state
		    List<State> statesList = readStatesProcessed(generatedStatesFileName);
		    List<State> statesNotProcessedList = readStatesNotProcessed(generatedStatesFileName);
		    int totalStatesCount = statesList.size() + statesNotProcessedList.size();
		    System.out.println("states generated:"+totalStatesCount);
		    System.out.println("states processed:"+statesList.size());
		    boolean finished = false;
		    for(State state:statesList){
		    	if(state.isFinished()){
		    		finished = true;
		    		break;
		    	}
		    }
		    if(finished){
			    for(State state:statesList){
			    	if(state.isFinished()){
			    		//actionsListList.add(state.getActionsList());
			    		selectedStateList.add(state);
			    		break;
			    	}
			    }
		    }
		    else{

//		    	Set<Integer> idSet = new HashSet<Integer>();
//		    	 for(State state:statesList){
//		    		 idSet.add(state.getStateId());
//		    	 }
//		    	 System.out.println("states size:"+idSet.size());
		    	
		    	List<State> maxSatisfiedList = new ArrayList<State>();
		    	//find state with max number of satisfied goals
		    	int satisfiedCountMax = 0;
			    for(State state:statesList){
			    	int currSatisfiedCount = 0;
			    	for(Goal goal:state.getGoalsList()){
			    		if(goal.isSatisfied() && !goal.isFromGraph()){
			    			currSatisfiedCount++;
			    		}
			    	}
			    	if(currSatisfiedCount>satisfiedCountMax){
			    		satisfiedCountMax = currSatisfiedCount;
			    	}
			    }
			    
			    //add list of actions for states equal to max if count is greater than zero
			    if(satisfiedCountMax > 0){
				    for(State state:statesList){
				    	int currSatisfiedCount = 0;
				    	for(Goal goal:state.getGoalsList()){
				    		if(goal.isSatisfied()){
				    			currSatisfiedCount++;
				    		}
				    	}
				    	if(currSatisfiedCount==satisfiedCountMax){
//				    		System.out.println("state id:"+state.getStateId());
//				    		System.out.println("satisfied count:"+currSatisfiedCount);
//				    		for(Goal goal:state.getGoalsList()){
//				    			System.out.println("from graph:"+goal.isFromGraph());
//				    			System.out.println("goal:"+goal.toString());
//				    		}
				    		//actionsListList.add(state.getActionsList());
				    		//selectedStateList.add(state);
				    		maxSatisfiedList.add(state);
				    	}
				    }
			    }
			    
			    //find state with min number of actions
			    int minAction = Integer.MAX_VALUE;
			    for(State state:maxSatisfiedList){
			    	if(state.getActionsList().size()<minAction){
			    		minAction = state.getActionsList().size();
			    	}
			    }
			    //add list of actions for states equal to max if count is greater than zero
			    if(minAction > 0){
				    for(State state:maxSatisfiedList){
				    	if(state.getActionsList().size()==minAction){
				    		selectedStateList.add(state);
				    	}
				    }
			    }
		    }
		    //generate test case
		    int generationCount = 0;
		    for(State state:selectedStateList){
		    	List<Action> actionsFromStateList = state.getActionsList();
		    	//print states about state
		    	System.out.println("################################");
		    	//System.out.println("id:"+state.getStateId());
		    	System.out.println("random count:"+state.getRandomCount());
		    	System.out.println("skipped count:"+state.getSkippedGoalCount());
		    	System.out.println("drawer heuristic count:"+state.getDrawerHeuristicCount());
		    	System.out.println("scroll heuristic count:"+state.getScrollHeuristicCount());
		    	int graphGoalCount = 0;
		    	for(Goal goal:state.getGoalsList()){
		    		if(goal.isSatisfied() && goal.isFromGraph()){
		    			graphGoalCount++;
		    		}
		    	}
		    	System.out.println("graph action count:"+graphGoalCount);
		    	
		    	//get all actions
		    	List<Action> actionsList = new ArrayList<Action>();
		    	actionsList.addAll(stepsList);
		    	actionsList.addAll(actionsFromStateList);
		    	
		    	//generate code
			    HashMap<String, ClassName> classNameMap = new HashMap<String, ClassName>();
				//activity class name
				String startingActivityPackageString = firstActivityName.substring(0, firstActivityName.lastIndexOf("."));
				String startingActivityClassString = firstActivityName.substring(firstActivityName.lastIndexOf(".")+1);
				ClassName startingActivityClassName = ClassName.get(startingActivityPackageString, startingActivityClassString);
				classNameMap.put("startingActivityClassName", startingActivityClassName);
				//large test class name
				ClassName largeTestClassName = ClassName.get("android.test.suitebuilder.annotation", "LargeTest");
				classNameMap.put("largeTestClassName", largeTestClassName);
				//AndroidJUnit4 class name
				ClassName androidJUnit4ClassName = ClassName.get("android.support.test.runner", "AndroidJUnit4");
				classNameMap.put("androidJUnit4ClassName", androidJUnit4ClassName);
				//rule class name
				ClassName ruleClassName = ClassName.get("org.junit", "Rule");
				classNameMap.put("ruleClassName", ruleClassName);
				//test class name
				ClassName testClassName = ClassName.get("org.junit", "Test");
				classNameMap.put("testClassName", testClassName);
				//run with class name
				ClassName runWidthClassName = ClassName.get("org.junit.runner", "RunWith");
				classNameMap.put("runWidthClassName", runWidthClassName);
				//intents test rule class name
				ClassName activityTestRuleClassName = ClassName.get("android.support.test.rule", "ActivityTestRule");
				classNameMap.put("activityTestRuleClassName", activityTestRuleClassName);
				//view matchers class name
				ClassName viewMatchersClassName = ClassName.get("android.support.test.espresso.matcher", "ViewMatchers");
				classNameMap.put("viewMatchersClassName", viewMatchersClassName);
				//preference matcher class name
				ClassName preferenceMatchersClassName = ClassName.get("android.support.test.espresso.matcher", "PreferenceMatchers");
				classNameMap.put("preferenceMatchersClassName", preferenceMatchersClassName);
				//view actions class name
				ClassName viewActionsClassName = ClassName.get("android.support.test.espresso.action", "ViewActions");
				classNameMap.put("viewActionsClassName", viewActionsClassName);
				//espresso class name
				ClassName espressoClassName = ClassName.get("android.support.test.espresso", "Espresso");
				classNameMap.put("espressoClassName", espressoClassName);
				
				//field name
				FieldSpec.Builder activityTestRuleFieldBuilder = FieldSpec.builder(ParameterizedTypeName.get(activityTestRuleClassName, startingActivityClassName), "activityTestRule");
				activityTestRuleFieldBuilder.addModifiers(Modifier.PUBLIC);
				activityTestRuleFieldBuilder.addAnnotation(ruleClassName);
				activityTestRuleFieldBuilder.initializer("new $T($T.class)", activityTestRuleClassName, startingActivityClassName);
				FieldSpec activityTestRuleField = activityTestRuleFieldBuilder.build();
				
				//test method
	        	MethodSpec.Builder testMethodBuilder = MethodSpec.methodBuilder("test"+testName+generationCount); ;
	        	testMethodBuilder.addModifiers(Modifier.PUBLIC);
	        	testMethodBuilder.returns(void.class);
	        	testMethodBuilder.addAnnotation(testClassName);
	        	//generate comment for developer
	        	if(!finished){
	        		testMethodBuilder.addComment("double check test case");
	        	}
	        	//generate code
		    	for(Action action:actionsList){
		    		action.generateEspressoCode(testMethodBuilder, classNameMap);
				}
	        	MethodSpec testMethod = testMethodBuilder.build();
	        	
	        	//generate class
	        	AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(runWidthClassName);
	        	annotationSpecBuilder.addMember("value","$T.class", androidJUnit4ClassName);
	        	AnnotationSpec runWithAnnotation = annotationSpecBuilder.build();
	        	TypeSpec.Builder testTypeBuilder = TypeSpec.classBuilder("Test"+testName+generationCount);
	        	testTypeBuilder.addModifiers(Modifier.PUBLIC);
	        	testTypeBuilder.addAnnotation(largeTestClassName);
	        	testTypeBuilder.addAnnotation(runWithAnnotation);
	        	testTypeBuilder.addField(activityTestRuleField);
	        	testTypeBuilder.addMethod(testMethod);
	        	TypeSpec testType = testTypeBuilder.build();       	
	        	JavaFile.Builder javaFileBuilder = JavaFile.builder(packageName, testType);
	        	javaFileBuilder.addStaticImport(espressoClassName, "onView");
	        	javaFileBuilder.addStaticImport(espressoClassName, "onData");
	        	JavaFile javaFile = javaFileBuilder.build();
	        	
				//create file
				File javaSourceFile = new File(testPath+"/"+"Test"+testName+generationCount+".java");
	        	javaFile.writeTo(javaSourceFile);
	        	generationCount++;
		    }
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private List<Action> readSteps(String stepsFileName){
		List<Action> stepsList = new ArrayList<Action>();
		try {
			FileInputStream fis = new FileInputStream(stepsFileName);
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
			throw new RuntimeException("could not read steps", e);
		}
		return stepsList;
	}
	
	private List<State> readStatesProcessed(String generatedStatesFileName){
		List<State> statesList = new ArrayList<State>();
		try {
			FileInputStream fis = new FileInputStream(generatedStatesFileName);
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
				boolean discard=false;
				for(int j=0; j<goalsArrayJSON.length(); ++j){
					Goal goal = getGoalFromJSONObject(goalsArrayJSON.getJSONObject(j));
					if(goal.isFromGraph()){
						discard=true;
						break;
					}
					goalsList.add(goal);
				}
				if(discard){
					continue;
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
			throw new RuntimeException("could not read states", e);
		}
		return statesList;
	}
	
	private List<State> readStatesNotProcessed(String generatedStatesFileName){
		List<State> statesList = new ArrayList<State>();
		try {
			FileInputStream fis = new FileInputStream(generatedStatesFileName);
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
				boolean discard=false;
				for(int j=0; j<goalsArrayJSON.length(); ++j){
					Goal goal = getGoalFromJSONObject(goalsArrayJSON.getJSONObject(j));
					if(goal.isFromGraph()){
						discard=true;
						break;
					}
					goalsList.add(goal);
				}
				if(discard){
					continue;
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
			throw new RuntimeException("could not read states", e);
		}
		return statesList;
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
				goal = new GoalClick(satisfied, fromGraph,targetViewModel, duration, randomCount);
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
				throw new RuntimeException("handle goal type while getting goal from JSON object");
			}
		}
		catch(Exception e){
			throw new RuntimeException("could not get goal from JSON object", e);
		}
		return goal;
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
			throw new RuntimeException("could not get target view model from JSON object", e);
		}
		return targetViewModel;
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
	      else if(actionType.equals("type")){
		        JSONObject selectorJSON = actionJSON.getJSONObject("selector");
		        String text = actionJSON.getString("text");
		        action = new ActionType(getSelectorFromJSONObject(selectorJSON), text);
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
	        throw new RuntimeException("handle action type while getting action from JSON object");
	      }
	    }
	    catch(Exception e){
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
	        throw new RuntimeException("handle selector type while getting selector from JSON object");
	      }
	    }
	    catch(Exception e){
	      throw new RuntimeException("could not get selector from JSON object", e);
	    }
	    return selector;
	  }
}
