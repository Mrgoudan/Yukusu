package presto.android.gui.clients;

import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import presto.android.Configs;
import presto.android.db.GraphDBUtils;
import presto.android.gui.GUIAnalysisClient;
import presto.android.gui.GUIAnalysisOutput;
import presto.android.gui.GraphUtil;
import presto.android.gui.graph.NActivityNode;
import presto.android.gui.graph.NContextMenuNode;
import presto.android.gui.graph.NDialogNode;
import presto.android.gui.graph.NNode;
import presto.android.gui.graph.NObjectNode;
import presto.android.gui.graph.NOptionsMenuNode;
import presto.android.gui.listener.EventType;
import presto.android.gui.wtg.WTGAnalysisOutput;
import presto.android.gui.wtg.WTGBuilder;
import presto.android.gui.wtg.ds.WTG;
import presto.android.gui.wtg.ds.WTGEdge;
import presto.android.gui.wtg.ds.WTGNode;
import presto.android.gui.wtg.flowgraph.NLauncherNode;
import soot.SootClass;

public class CreateStaticGraph implements GUIAnalysisClient {

	@Override
	public void run(GUIAnalysisOutput guiOutput) {
		JSONArray ontologyJSONArray = new JSONArray();

		System.out.println("##########################Initializing graph############################");
		
		//initialize graph db
		GraphDBUtils.initializeDB(ontologyJSONArray);
		//get graph util
		GraphUtil graphUtil = GraphUtil.v();


		//represent view elements in the graph
		Map<Integer, Integer> optionsMenuRootNodeIdMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> contextMenuRootNodeIdMap = new HashMap<Integer, Integer>();
		Set<NOptionsMenuNode> processedOptionsMenuSet = new HashSet<NOptionsMenuNode>();
		Set<NContextMenuNode> processedContextMenuSet = new HashSet<NContextMenuNode>();
		Map<NObjectNode, NObjectNode> optionsMenuWindowMap = new HashMap<NObjectNode, NObjectNode>();
		List<NObjectNode> windowNodeList = Lists.newArrayList();
		Set<NObjectNode> windowsSet = Sets.newHashSet();
		Set<NNode> viewList = Sets.newHashSet();
		for (SootClass c : guiOutput.getActivities()) {
			NActivityNode actNode = guiOutput.getFlowgraph().allNActivityNodes.get(c);
			windowNodeList.add(actNode);
		}
		for (NDialogNode dialogNode : guiOutput.getDialogs()) {
			windowNodeList.add(dialogNode);
		}
		
		System.out.println("##########################Processing windows############################");
		
		while (!windowNodeList.isEmpty()) {
			NObjectNode windowNode = windowNodeList.remove(0);
			windowsSet.add(windowNode);
			if (windowNode instanceof NActivityNode) {
				//add activity node
				GraphDBUtils.addWindow("Activity", windowNode, windowNode.getClassType().getName());

				SootClass actClass = ((NActivityNode) windowNode).c;

				//check if activity has option menu
				NOptionsMenuNode optionsMenu = guiOutput.getOptionsMenu(actClass);
				if (optionsMenu != null) { 
					if(!processedOptionsMenuSet.contains(optionsMenu)){
						//treat options menu as 
						GraphDBUtils.addWindow("OptionsMenu", optionsMenu, optionsMenu.ownerActivity.getName());
						int optionsMenuRootNodeId = GraphDBUtils.getNextNodeId();
						GraphDBUtils.addFakeRoot(optionsMenu, optionsMenuRootNodeId);
						GraphDBUtils.addWindowRootRelation(optionsMenu.id, optionsMenuRootNodeId);
						//
						optionsMenuRootNodeIdMap.put(new Integer(optionsMenu.id), new Integer(optionsMenuRootNodeId));
						// add it to list to resolve hierarchy
						windowNodeList.add(optionsMenu);
						processedOptionsMenuSet.add(optionsMenu);	
						optionsMenuWindowMap.put(optionsMenu, windowNode);
					}
				}

				Set<NNode> roots = guiOutput.getActivityRoots(actClass);
				for (NNode r : roots) {        	
					//add root view
					GraphDBUtils.addView(r);
					//add relation between activity and root view 
					GraphDBUtils.addWindowRootRelation(windowNode.id, r.id);

					//iterate over descendants
					for (NNode desc : graphUtil.descendantNodes(r)) {
						if(desc==r){
							continue;
						}
						//add desc view
						GraphDBUtils.addView(desc);
						GraphDBUtils.addDescendantRelation(r.id, desc.id);
						viewList.add(desc);
						NObjectNode v = (NObjectNode) desc;
						//check for context menus
						Set<NContextMenuNode> contextMenus = guiOutput.getContextMenus(v);
						if(contextMenus!=null){
							for (NContextMenuNode contextMenu : contextMenus) {
								if(!processedContextMenuSet.contains(contextMenu)){
									//add context menu
									GraphDBUtils.addWindow("ContextMenu",contextMenu, windowNode.getClassType().getName());
									int contextMenuRootNodeId = GraphDBUtils.getNextNodeId();
									GraphDBUtils.addFakeRoot(contextMenu, contextMenuRootNodeId);
									GraphDBUtils.addWindowRootRelation(contextMenu.id, contextMenuRootNodeId);
									contextMenuRootNodeIdMap.put(new Integer(contextMenu.id), new Integer(contextMenuRootNodeId));
									windowNodeList.add(contextMenu);
									processedContextMenuSet.add(contextMenu);
								}
							}
						}
					}
				}
			}
			else if(windowNode instanceof NDialogNode){
				//add dialog
				NDialogNode dialogNode = (NDialogNode) windowNode;
				String activityClass = dialogNode.allocMethod.getDeclaringClass().getName();
				if(activityClass.indexOf("$")!=-1){
					activityClass = activityClass.substring(0, activityClass.indexOf("$"));
				}
				GraphDBUtils.addWindow("Dialog",windowNode, activityClass);

				Set<NNode> roots = guiOutput.getDialogRoots((NDialogNode) windowNode);
				for (NNode r : roots) {
					GraphDBUtils.addView(r);
					//add relation between activity and root view 
					GraphDBUtils.addWindowRootRelation(windowNode.id, r.id);
					
					for (NNode desc : graphUtil.descendantNodes(r)) {
						if(desc==r){
							continue;
						}
						//add desc view
						GraphDBUtils.addView(desc);
						GraphDBUtils.addDescendantRelation(r.id, desc.id);
						viewList.add(desc);
						NObjectNode v = (NObjectNode) desc;
						//check for context menus
						Set<NContextMenuNode> contextMenus = guiOutput.getContextMenus(v);
						if (contextMenus != null) {
							for (NContextMenuNode contextMenu : contextMenus) {
								if(!processedContextMenuSet.contains(contextMenu)){
									//add context menu
									GraphDBUtils.addWindow("ContextMenu", contextMenu, activityClass);
									int contextMenuRootNodeId = GraphDBUtils.getNextNodeId();
									GraphDBUtils.addFakeRoot(contextMenu, contextMenuRootNodeId);
									GraphDBUtils.addWindowRootRelation(contextMenu.id, contextMenuRootNodeId);
									contextMenuRootNodeIdMap.put(new Integer(contextMenu.id), new Integer(contextMenuRootNodeId));
									windowNodeList.add(contextMenu);
									processedContextMenuSet.add(contextMenu);
								}
							}
						}
					}
				}
			}
			else if(windowNode instanceof NOptionsMenuNode){
				int rootId = optionsMenuRootNodeIdMap.get(new Integer(windowNode.id)).intValue();
				for (NNode desc : graphUtil.descendantNodes(windowNode)) {
					if(desc==windowNode){
						continue;
					}       		
					GraphDBUtils.addView(desc);
					GraphDBUtils.addDescendantRelation(rootId, desc.id);
					viewList.add(desc);
				}
			}
			else if(windowNode instanceof NContextMenuNode){
				int rootId = contextMenuRootNodeIdMap.get(new Integer(windowNode.id)).intValue();
				for (NNode desc : graphUtil.descendantNodes(windowNode)) {
					if(desc==windowNode){
						continue;
					}       		
					GraphDBUtils.addView(desc);
					GraphDBUtils.addDescendantRelation(rootId, desc.id);
					viewList.add(desc);
				}
			}
		}
		
		System.out.println("##########################Creating view-view relations############################");

		//represent relations between views
		for(NNode viewNode:viewList){
			Iterator<NNode> parentsNodeIterator = viewNode.getParents();
			while(parentsNodeIterator.hasNext()){
				NNode parentNode = parentsNodeIterator.next();
				int parentId = parentNode.id;
				if(contextMenuRootNodeIdMap.keySet().contains(new Integer(parentId))){
					parentId = contextMenuRootNodeIdMap.get(new Integer(parentId));
				}
				else if(optionsMenuRootNodeIdMap.keySet().contains(new Integer(parentId))){
					parentId = optionsMenuRootNodeIdMap.get(new Integer(parentId));
				}
				GraphDBUtils.addParentRelation(parentId, viewNode.id);
			}
		}
		
		System.out.println("##########################Creating view-window relations############################");

		//represent transition between windows
		WTGBuilder wtgBuilder = new WTGBuilder();
		wtgBuilder.build(guiOutput);
		WTGAnalysisOutput wtgAO = new WTGAnalysisOutput(guiOutput, wtgBuilder);
		WTG wtg = wtgAO.getWTG();
		Collection<WTGEdge> edges = wtg.getEdges();
		for(WTGEdge e:edges){
			if(windowsSet.contains(e.getGUIWidget()) || e.getGUIWidget() instanceof NLauncherNode){
				//skip events for widgets what are windows or launcher node
				continue;
			}
			int sourceViewId = e.getGUIWidget().id;
			int targetNodeId = e.getTargetNode().getWindow().id;
			if(e.getEventType()==EventType.click){
				GraphDBUtils.addActionRelation("click", sourceViewId, targetNodeId);
				continue;
			}
			else if(e.getEventType()==EventType.long_click){
				GraphDBUtils.addActionRelation("longClick", sourceViewId, targetNodeId);
				continue;
			}
			else if(e.getEventType()==EventType.select){
				//System.out.println("TODO handle select:"+e.toString());
				GraphDBUtils.addActionRelation("click", sourceViewId, targetNodeId);
			}
			else if(e.getEventType()==EventType.item_click){
				//System.out.println("TODO handle item click:"+e.toString());
				GraphDBUtils.addActionRelation("click", sourceViewId, targetNodeId);
			}
			else if(e.getEventType()==EventType.item_long_click){
				//System.out.println("TODO handle item long click:"+e.toString());
				GraphDBUtils.addActionRelation("longClick", sourceViewId, targetNodeId);
			}
			else if(e.getEventType()==EventType.item_selected){
				//System.out.println("TODO handle item selected:"+e.toString());
				GraphDBUtils.addActionRelation("click", sourceViewId, targetNodeId);
			}
			else if(e.getEventType()==EventType.dialog_negative_button){
				System.out.println("TODO handle dialog negative button:"+e.toString());
			}
			else if(e.getEventType()==EventType.dialog_neutral_button){
				System.out.println("TODO handle dialog neutral button:"+e.toString());
			}
			else if(e.getEventType()==EventType.dialog_cancel){
				System.out.println("TODO handle dialog cancel:"+e.toString());
			}
			else if(e.getEventType()==EventType.dialog_dismiss){
				System.out.println("TODO handle dialog dismiss:"+e.toString());
			}
			else if(e.getEventType()==EventType.dialog_positive_button){
				System.out.println("TODO handle dialog positive button:"+e.toString());
			}
		}
		
		System.out.println("##########################Creating window-options menu relations############################");
		for(NObjectNode optionsMenuNode:optionsMenuWindowMap.keySet()){
			NObjectNode windowNode = optionsMenuWindowMap.get(optionsMenuNode);
			int windowRootNodeId = GraphDBUtils.getNextNodeId();
			//add fake root to window
			GraphDBUtils.addFakeRoot(windowNode, windowRootNodeId);
			//connect window to root
			GraphDBUtils.addWindowRootRelation(windowNode.id, windowRootNodeId);
			//optionsMenu
			GraphDBUtils.addActionRelation("openOptions", windowRootNodeId, optionsMenuNode.id);
		}
		
		
		System.out.println("##########################Saving ontology file############################");
		
		try{
			JSONObject ontologyJSON = new JSONObject();
			SootClass mainClz = guiOutput.getMainActivity();
			String mainActivityName = "";
			if(mainClz!=null){
				mainActivityName = mainClz.getName();
			}
			ontologyJSON.put("main_activity", mainActivityName);
			ontologyJSON.put("ontology", ontologyJSONArray);
			FileWriter ontologyFileWriter = new FileWriter(Configs.ontologyFileName);
			ontologyFileWriter.write(ontologyJSON.toString());
			ontologyFileWriter.close();
		}
		catch(Exception e){
			System.err.println("exception while saving ontology file");
			throw new RuntimeException("exception while saving ontology file",e);
		}
		
		System.out.println("##########################Saving string file############################");
		
		try{
			JSONObject stringJSON = new JSONObject();
			Map<String, String> stringIdValueMap = guiOutput.getFlowgraph().getXMLParser().getStringIdValueMapping();
			Map<String, String> sysStringIdValueMap = guiOutput.getFlowgraph().getXMLParser().getSysStringIdValueMapping();
			JSONArray stringJSONArray = new JSONArray();
			for(String key:stringIdValueMap.keySet()){
				String value = stringIdValueMap.get(key);
				if(value!=null && !value.equals("")){
					stringJSONArray.put(value);
				}
			}
			for(String key:sysStringIdValueMap.keySet()){
				String value = sysStringIdValueMap.get(key);
				if(value!=null && !value.equals("")){
					stringJSONArray.put(value);
				}
			}
			stringJSON.put("string", stringJSONArray);
			FileWriter stringFileWriter = new FileWriter(Configs.stringFileName);
			stringFileWriter.write(stringJSON.toString());
			stringFileWriter.close();
		}
		catch(Exception e){
			System.err.println("exception while saving string file");
			throw new RuntimeException("exception while saving string file",e);
		}
		
		
		System.out.println("######################################################");
		for(WTGNode node:wtg.getNodes()){
			System.out.println(node.toString());
		}
		System.out.println("######################################################");
		for(WTGEdge edge:wtg.getEdges()){
			System.out.println(edge.toString());
		}
		
		System.out.println("######################################################");
		System.out.println("view num:"+GraphDBUtils.viewNum);
		System.out.println("window num:"+GraphDBUtils.windowNum);
		System.out.println("action num:"+GraphDBUtils.actionNum);
		
		//finalize graph db
		GraphDBUtils.finalizeDB();
	}
}
