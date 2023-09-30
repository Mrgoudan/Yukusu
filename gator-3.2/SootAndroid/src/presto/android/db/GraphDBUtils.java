package presto.android.db;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.google.gson.Gson;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import presto.android.gui.graph.NActivityNode;
import presto.android.gui.graph.NContextMenuNode;
import presto.android.gui.graph.NDialogNode;
import presto.android.gui.graph.NInflNode;
import presto.android.gui.graph.NMenuItemInflNode;
import presto.android.gui.graph.NNode;
import presto.android.gui.graph.NOptionsMenuNode;
import presto.android.gui.graph.NStringConstantNode;
import presto.android.gui.graph.NStringIdNode;
import presto.android.gui.graph.NViewAllocNode;
import presto.android.xml.XMLParser;
import presto.android.xml.XMLParser.Factory;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class GraphDBUtils {

	private static Driver DRIVER;
	private static Session SESSION;
	private static JSONArray ontologyJSONArray;
	public static int viewNum = 0;
	public static int windowNum = 0;
	public static int actionNum = 0;
	
	private static int NODE_ID = 1000000;
	
	public static int getNextNodeId(){
		int result = NODE_ID;
		NODE_ID++;
		return result;
	}
	
	public static void initializeDB(JSONArray ontologyJSONArray){
		GraphDBUtils.ontologyJSONArray=ontologyJSONArray;
		GraphDBUtils.DRIVER = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "android" ) );
	    GraphDBUtils.SESSION = DRIVER.session();
	    //delete existing content
	    SESSION.run("MATCH (n) DETACH DELETE n");
	}
	
	public static void finalizeDB(){
		SESSION.close();
	    DRIVER.close();
	}
	
	private static void checkResult(StatementResult result){
//		while (result.hasNext() ) {
//		    Record record = result.next();
//		    Gson gson = new Gson();
//		    System.out.println("Result:"+gson.toJson(record.asMap()).toString());
//		}
	}
	
	//mf: fake root view for options menu and context menu
	//mf: this should not have image reference and text reference
	public static void addFakeRoot(NNode windowNode, int nodeId){
		viewNum++;
		if(windowNode instanceof NOptionsMenuNode){
			NOptionsMenuNode optionsMenuNode = (NOptionsMenuNode) windowNode;
			//handle class name and name
			String fullClassName = optionsMenuNode.getClassType().getName();
			String className = getClassNameFromFullClassName(fullClassName);
			String name = className;
			List<String> classNameKeywordsList = getKeywordsFromClassName(className);
			List<String> classNameLemmasList = keywordsToLemmas(classNameKeywordsList);
			//handle text
			String text = "";
			List<String> textKeywordsList = getKeywordsFromText(text);
			List<String> textLemmasList = keywordsToLemmas(textKeywordsList);
			//handle text reference
			String textReference = "";
			List<String> textReferenceKeywordsList = getKeywordsFromReference(textReference);
			List<String> textReferenceLemmasList = keywordsToLemmas(textReferenceKeywordsList);
			//handle resource id
			String resourceId = "";
			String resourceIdReference = "";
			List<String> resourceIdReferenceKeywordsList = getKeywordsFromReference(resourceIdReference);
			List<String> resourceIdReferenceLemmasList = keywordsToLemmas(resourceIdReferenceKeywordsList);
			//handle image reference
			String imageReference = "";
			List<String> imageReferenceKeywordsList = getKeywordsFromReference(imageReference);
			List<String> imageReferenceLemmasList = keywordsToLemmas(imageReferenceKeywordsList);
			
			StatementResult result = SESSION.run( "MERGE (v:View {nodeId:{nodeId}, name:{name}, "
					+ "fullClassName:{fullClassName}, className:{className}, classNameKeywords:{classNameKeywords}, classNameLemmas:{classNameLemmas}, "
					+ "text:{text}, textKeywords:{textKeywords}, textLemmas:{textLemmas}, textReference:{textReference}, textReferenceKeywords:{textReferenceKeywords}, textReferenceLemmas:{textReferenceLemmas},"
					+ "resourceId:{resourceId}, resourceIdReference:{resourceIdReference}, resourceIdReferenceKeywords:{resourceIdReferenceKeywords}, resourceIdReferenceLemmas:{resourceIdReferenceLemmas},"
					+ "imageReference:{imageReference}, imageReferenceKeywords:{imageReferenceKeywords}, imageReferenceLemmas:{imageReferenceLemmas}}) RETURN v",
					parameters("nodeId", nodeId, "name", name,
							"fullClassName", fullClassName, "className", className, "classNameKeywords", classNameKeywordsList, "classNameLemmas", classNameLemmasList,
							"text", text, "textKeywords", textKeywordsList, "textLemmas", textLemmasList, "textReference", textReference, "textReferenceKeywords", textReferenceKeywordsList, "textReferenceLemmas", textReferenceLemmasList,
							"resourceId", resourceId, "resourceIdReference", resourceIdReference,"resourceIdReferenceKeywords", resourceIdReferenceKeywordsList, "resourceIdReferenceLemmas", resourceIdReferenceLemmasList,
							"imageReference", imageReference, "imageReferenceKeywords", imageReferenceKeywordsList, "imageReferenceLemmas", imageReferenceLemmasList));
			checkResult(result);
			//save ontology to json object
			saveOntology(fullClassName, className, classNameKeywordsList, classNameLemmasList,
					text, textKeywordsList, textLemmasList, textReference, textReferenceKeywordsList, textReferenceLemmasList,
					resourceId, resourceIdReference, resourceIdReferenceKeywordsList, resourceIdReferenceLemmasList,
					imageReference, imageReferenceKeywordsList, imageReferenceLemmasList);
		}
		else if(windowNode instanceof NContextMenuNode){
			NContextMenuNode contextMenuNode = (NContextMenuNode) windowNode;
			//handle class name and name
			String fullClassName = contextMenuNode.getClassType().getName();
			String className = getClassNameFromFullClassName(fullClassName);
			String name = className;
			List<String> classNameKeywordsList = getKeywordsFromClassName(className);
			List<String> classNameLemmasList = keywordsToLemmas(classNameKeywordsList);
			//handle text
			String text = "";
			List<String> textKeywordsList = getKeywordsFromText(text);
			List<String> textLemmasList = keywordsToLemmas(textKeywordsList);
			//handle text reference
			String textReference = "";
			List<String> textReferenceKeywordsList = getKeywordsFromReference(textReference);
			List<String> textReferenceLemmasList = keywordsToLemmas(textReferenceKeywordsList);
			//handle resource id
			String resourceId = "";
			String resourceIdReference = "";
			List<String> resourceIdReferenceKeywordsList = getKeywordsFromReference(resourceIdReference);
			List<String> resourceIdReferenceLemmasList = keywordsToLemmas(resourceIdReferenceKeywordsList);
			//handle image reference
			String imageReference = "";
			List<String> imageReferenceKeywordsList = getKeywordsFromReference(imageReference);
			List<String> imageReferenceLemmasList = keywordsToLemmas(imageReferenceKeywordsList);
			
			StatementResult result = SESSION.run( "MERGE (v:View {nodeId:{nodeId}, name:{name}, "
					+ "fullClassName:{fullClassName}, className:{className}, classNameKeywords:{classNameKeywords}, classNameLemmas:{classNameLemmas}, "
					+ "text:{text}, textKeywords:{textKeywords}, textLemmas:{textLemmas}, textReference:{textReference}, textReferenceKeywords:{textReferenceKeywords}, textReferenceLemmas:{textReferenceLemmas},"
					+ "resourceId:{resourceId}, resourceIdReference:{resourceIdReference}, resourceIdReferenceKeywords:{resourceIdReferenceKeywords}, resourceIdReferenceLemmas:{resourceIdReferenceLemmas},"
					+ "imageReference:{imageReference}, imageReferenceKeywords:{imageReferenceKeywords}, imageReferenceLemmas:{imageReferenceLemmas}}) RETURN v",
					parameters("nodeId", nodeId, "name", name,
							"fullClassName", fullClassName, "className", className, "classNameKeywords", classNameKeywordsList, "classNameLemmas", classNameLemmasList,
							"text", text, "textKeywords", textKeywordsList, "textLemmas", textLemmasList, "textReference", textReference, "textReferenceKeywords", textReferenceKeywordsList, "textReferenceLemmas", textReferenceLemmasList,
							"resourceId", resourceId, "resourceIdReference", resourceIdReference, "resourceIdReferenceKeywords", resourceIdReferenceKeywordsList, "resourceIdReferenceLemmas", resourceIdReferenceLemmasList,
							"imageReference", imageReference, "imageReferenceKeywords", imageReferenceKeywordsList, "imageReferenceLemmas", imageReferenceLemmasList));
			checkResult(result);
			//save ontology to json object
			saveOntology(fullClassName, className, classNameKeywordsList, classNameLemmasList,
					text, textKeywordsList, textLemmasList, textReference, textReferenceKeywordsList, textReferenceLemmasList,
					resourceId, resourceIdReference, resourceIdReferenceKeywordsList, resourceIdReferenceLemmasList,
					imageReference, imageReferenceKeywordsList, imageReferenceLemmasList);
		}
		else if(windowNode instanceof NActivityNode){
			NActivityNode activityNode = (NActivityNode) windowNode;
			//handle class name and name
			String fullClassName = activityNode.getClassType().getName();
			String className = getClassNameFromFullClassName(fullClassName);
			String name = className;
			List<String> classNameKeywordsList = getKeywordsFromClassName(className);
			List<String> classNameLemmasList = keywordsToLemmas(classNameKeywordsList);
			//handle text
			String text = "";
			List<String> textKeywordsList = getKeywordsFromText(text);
			List<String> textLemmasList = keywordsToLemmas(textKeywordsList);
			//handle text reference
			String textReference = "";
			List<String> textReferenceKeywordsList = getKeywordsFromReference(textReference);
			List<String> textReferenceLemmasList = keywordsToLemmas(textReferenceKeywordsList);
			//handle resource id
			String resourceId = "";
			String resourceIdReference = "";
			List<String> resourceIdReferenceKeywordsList = getKeywordsFromReference(resourceIdReference);
			List<String> resourceIdReferenceLemmasList = keywordsToLemmas(resourceIdReferenceKeywordsList);
			//handle image reference
			String imageReference = "";
			List<String> imageReferenceKeywordsList = getKeywordsFromReference(imageReference);
			List<String> imageReferenceLemmasList = keywordsToLemmas(imageReferenceKeywordsList);
			
			StatementResult result = SESSION.run( "MERGE (v:View {nodeId:{nodeId}, name:{name}, "
					+ "fullClassName:{fullClassName}, className:{className}, classNameKeywords:{classNameKeywords}, classNameLemmas:{classNameLemmas}, "
					+ "text:{text}, textKeywords:{textKeywords}, textLemmas:{textLemmas}, textReference:{textReference}, textReferenceKeywords:{textReferenceKeywords}, textReferenceLemmas:{textReferenceLemmas},"
					+ "resourceId:{resourceId}, resourceIdReference:{resourceIdReference}, resourceIdReferenceKeywords:{resourceIdReferenceKeywords}, resourceIdReferenceLemmas:{resourceIdReferenceLemmas},"
					+ "imageReference:{imageReference}, imageReferenceKeywords:{imageReferenceKeywords}, imageReferenceLemmas:{imageReferenceLemmas}}) RETURN v",
					parameters("nodeId", nodeId, "name", name,
							"fullClassName", fullClassName, "className", className, "classNameKeywords", classNameKeywordsList, "classNameLemmas", classNameLemmasList,
							"text", text, "textKeywords", textKeywordsList, "textLemmas", textLemmasList, "textReference", textReference, "textReferenceKeywords", textReferenceKeywordsList, "textReferenceLemmas", textReferenceLemmasList,
							"resourceId", resourceId, "resourceIdReference", resourceIdReference, "resourceIdReferenceKeywords", resourceIdReferenceKeywordsList, "resourceIdReferenceLemmas", resourceIdReferenceLemmasList,
							"imageReference", imageReference, "imageReferenceKeywords", imageReferenceKeywordsList, "imageReferenceLemmas", imageReferenceLemmasList));
			checkResult(result);
			//save ontology to json object
			saveOntology(fullClassName, className, classNameKeywordsList, classNameLemmasList,
					text, textKeywordsList, textLemmasList, textReference, textReferenceKeywordsList, textReferenceLemmasList,
					resourceId, resourceIdReference, resourceIdReferenceKeywordsList, resourceIdReferenceLemmasList,
					imageReference, imageReferenceKeywordsList, imageReferenceLemmasList);
		}
		else{
			System.err.println("addFakeRoot: node type not handled "+windowNode.getClass().toString()+" "+windowNode.toString());
			System.exit(-1);
		}
	}
	
	//mf: represent window in graph
	//mf: these nodes should not have image reference and text reference
	public static void addWindow(String type, NNode windowNode, String relatedActivity){
		windowNum++;
		if(windowNode instanceof NActivityNode){
			NActivityNode activityNode = (NActivityNode) windowNode;
			//full class name
			String fullClassName = activityNode.getClassType().getName();
			//class name
			String className = getClassNameFromFullClassName(fullClassName);
			//name
			String name = className;
			//node id
			int nodeId = activityNode.id;
			StatementResult result = SESSION.run( "MERGE (w:Window {name:{name}, fullClassName:{fullClassName}, className:{className}, type:{type}, relatedActivity:{relatedActivity}, nodeId:{nodeId}}) RETURN w",
					parameters("name", name, "fullClassName", ""+fullClassName+"", "className", ""+className+"", "type", type, "relatedActivity", relatedActivity, "nodeId", nodeId));
			checkResult(result);
		}
		else if(windowNode instanceof NOptionsMenuNode){
			NOptionsMenuNode optionsMenuNode = (NOptionsMenuNode) windowNode;
			//full class name
			String fullClassName = optionsMenuNode.getClassType().getName();
			//class name
			String className = getClassNameFromFullClassName(fullClassName);
			//name
			String name = className;
			//node id
			int nodeId = optionsMenuNode.id;
			StatementResult result = SESSION.run( "MERGE (w:Window {name:{name}, fullClassName:{fullClassName}, className:{className}, type:{type}, relatedActivity:{relatedActivity}, nodeId:{nodeId}}) RETURN w",
					parameters("name", name, "fullClassName", ""+fullClassName+"", "className", ""+className+"", "type", type, "relatedActivity", relatedActivity, "nodeId", nodeId));
			checkResult(result);
		}
		else if(windowNode instanceof NContextMenuNode){
			NContextMenuNode contextMenuNode = (NContextMenuNode) windowNode;
			//full class name
			String fullClassName = contextMenuNode.getClassType().getName();
			//class name
			String className = getClassNameFromFullClassName(fullClassName);
			//name
			String name = className;
			//node id
			int nodeId = contextMenuNode.id;
			StatementResult result = SESSION.run( "MERGE (w:Window {name:{name}, fullClassName:{fullClassName}, className:{className}, type:{type}, relatedActivity:{relatedActivity}, nodeId:{nodeId}}) RETURN w",
					parameters("name", name, "fullClassName", ""+fullClassName+"", "className", ""+className+"", "type", type, "relatedActivity", relatedActivity, "nodeId", nodeId));
			checkResult(result);
		}
		else if(windowNode instanceof NDialogNode){
			NDialogNode dialogNode = (NDialogNode) windowNode;
			//full class name
			String fullClassName = dialogNode.getClassType().getName();
			//class name
			String className = getClassNameFromFullClassName(fullClassName);
			//name
			String name = className;
			//node id
			int nodeId = dialogNode.id;
			StatementResult result = SESSION.run( "MERGE (w:Window {name:{name}, fullClassName:{fullClassName}, className:{className}, type:{type}, relatedActivity:{relatedActivity}, nodeId:{nodeId}}) RETURN w",
					parameters("name", name, "fullClassName", ""+fullClassName+"", "className", ""+className+"", "type", type, "relatedActivity", relatedActivity, "nodeId", nodeId));
			checkResult(result);
		}
		else{
			System.err.println("addWindow: node type not handled "+windowNode.getClass().toString()+" "+windowNode.toString());
			System.exit(-1);
		}
	}
	
	//mf: handle view nodes
	//mf: in this one for sure we could have resourceId or text or whatever
	public static void addView(NNode viewNode){		
		viewNum++;
		//get gator node id 
		int nodeId = viewNode.id;
		//handle class name and name
		String fullClassName = getClassNameForView(viewNode);
		String className = getClassNameFromFullClassName(fullClassName);
		String name = className;
		List<String> classNameKeywordsList = getKeywordsFromClassName(className);
		List<String> classNameLemmasList = keywordsToLemmas(classNameKeywordsList);
		//handle text
		String text = viewNode.getTextValue() == null ? "" : viewNode.getTextValue();
		List<String> textKeywordsList = getKeywordsFromText(text);
		List<String> textLemmasList = keywordsToLemmas(textKeywordsList);
		//handle text reference
		String textReference = viewNode.getTextReference() == null ? "" : viewNode.getTextReference();
		List<String> textReferenceKeywordsList = getKeywordsFromReference(textReference);
		List<String> textReferenceLemmasList = keywordsToLemmas(textReferenceKeywordsList);
		//handle resource id
		String resourceId = viewNode.getResourceIdValue()  == null ? "" : viewNode.getResourceIdValue();
		String resourceIdReference = viewNode.getResourceIdReference() == null ? "" : viewNode.getResourceIdReference();
		List<String> resourceIdReferenceKeywordsList = getKeywordsFromReference(resourceIdReference);
		List<String> resourceIdReferenceLemmasList = keywordsToLemmas(resourceIdReferenceKeywordsList);
		//handle image reference
		String imageReference = viewNode.getImageReference() == null ? "" : viewNode.getImageReference();//using this in the node because the user could say "click on the arrow"
		List<String> imageReferenceKeywordsList = getKeywordsFromReference(imageReference);
		List<String> imageReferenceLemmasList = keywordsToLemmas(imageReferenceKeywordsList);
		
		StatementResult result = SESSION.run( "MERGE (v:View {nodeId:{nodeId}, name:{name}, "
				+ "fullClassName:{fullClassName}, className:{className}, classNameKeywords:{classNameKeywords}, classNameLemmas:{classNameLemmas}, "
				+ "text:{text}, textKeywords:{textKeywords}, textLemmas:{textLemmas}, textReference:{textReference}, textReferenceKeywords:{textReferenceKeywords}, textReferenceLemmas:{textReferenceLemmas},"
				+ "resourceId:{resourceId}, resourceIdReference:{resourceIdReference}, resourceIdReferenceKeywords:{resourceIdReferenceKeywords}, resourceIdReferenceLemmas:{resourceIdReferenceLemmas},"
				+ "imageReference:{imageReference}, imageReferenceKeywords:{imageReferenceKeywords}, imageReferenceLemmas:{imageReferenceLemmas}}) RETURN v",
				parameters("nodeId", nodeId, "name", name,
						"fullClassName", fullClassName, "className", className, "classNameKeywords", classNameKeywordsList, "classNameLemmas", classNameLemmasList,
						"text", text, "textKeywords", textKeywordsList, "textLemmas", textLemmasList, "textReference", textReference, "textReferenceKeywords", textReferenceKeywordsList, "textReferenceLemmas", textReferenceLemmasList,
						"resourceId", resourceId, "resourceIdReference", resourceIdReference, "resourceIdReferenceKeywords", resourceIdReferenceKeywordsList, "resourceIdReferenceLemmas", resourceIdReferenceLemmasList,
						"imageReference", imageReference, "imageReferenceKeywords", imageReferenceKeywordsList, "imageReferenceLemmas", imageReferenceLemmasList));
		checkResult(result);
		//save ontology to json object
		saveOntology(fullClassName, className, classNameKeywordsList, classNameLemmasList,
				text, textKeywordsList, textLemmasList, textReference, textReferenceKeywordsList, textReferenceLemmasList,
				resourceId, resourceIdReference, resourceIdReferenceKeywordsList, resourceIdReferenceLemmasList,
				imageReference, imageReferenceKeywordsList, imageReferenceLemmasList);
	}
	
	public static void addWindowRootRelation(int windowId, int rootId){
		StatementResult result = SESSION.run("MATCH (w:Window),(v:View) WHERE w.nodeId = "+windowId+" AND v.nodeId = "+rootId+" MERGE (w)-[r:hasRoot]->(v) RETURN r");
		checkResult(result);
	}
	
	public static void addDescendantRelation(int rootId, int viewId){
		StatementResult result = SESSION.run("MATCH (v1:View),(v2:View) WHERE v1.nodeId = "+rootId+" AND v2.nodeId = "+viewId+" MERGE (v1)-[r:hasDescendant]->(v2) RETURN r");
		checkResult(result);
	}
	
	public static void addParentRelation(int parentId, int viewId){
		StatementResult result = SESSION.run("MATCH (v1:View),(v2:View) WHERE v1.nodeId = "+parentId+" AND v2.nodeId = "+viewId+" MERGE (v1)-[r:hasChild]->(v2) RETURN r");
		checkResult(result);
	}
	
	public static void addActionRelation(String action, int sourceId, int targetId){
		actionNum++;
		StatementResult result = SESSION.run("MATCH (v:View),(w:Window) WHERE v.nodeId = "+sourceId+" AND w.nodeId = "+targetId+" MERGE (v)-[r:"+action+"]->(w) RETURN r");
		checkResult(result);
	}
	
	private static String getClassNameForView(NNode viewNode){
		String className = "";
		if(viewNode instanceof NInflNode){
			NInflNode inflNode = (NInflNode) viewNode;
			className = inflNode.getClassType().getName();			
		}
		else if(viewNode instanceof NMenuItemInflNode){
			NMenuItemInflNode menuIteminflNode = (NMenuItemInflNode) viewNode;
			className = menuIteminflNode.getClassType().getName();
		}
		else if(viewNode instanceof NViewAllocNode){
			//new View case
			NViewAllocNode allocNode = (NViewAllocNode) viewNode;
			className = allocNode.getClassType().getName();
		}
		else{
			System.err.println("getClassNameForView: node type not handled "+viewNode.getClass().toString()+" "+viewNode.toString());
			System.exit(-1);
		}
		return className;
	}
	
	private static void saveOntology(String fullClassName, String className, List<String> classNameKeywordsList, List<String> classNameLemmasList,
			String text, List<String> textKeywordsList, List<String> textLemmasList, String textReference, List<String> textReferenceKeywordsList, List<String> textReferenceLemmasList,
			String resourceId, String resourceIdReference, List<String> resourceIdReferenceKeywordsList, List<String> resourceIdReferenceLemmasList,
			String imageReference, List<String> imageReferenceKeywordsList, List<String> imageReferenceLemmasList){
			try{
				JSONObject nodeOntologyJSON = new JSONObject();
				//full class name
				nodeOntologyJSON.put("full_class_name", fullClassName);
				//class name
				nodeOntologyJSON.put("class_name", className);
				//class name keywords
				JSONArray classNameKeywords = new JSONArray();
				for(String keyword:classNameKeywordsList){
					classNameKeywords.put(keyword);
				}
				nodeOntologyJSON.put("class_name_keywords", classNameKeywords);
				//class name lemmas
				JSONArray classNameLemmas = new JSONArray();
				for(String lemmas:classNameLemmasList){
					classNameLemmas.put(lemmas);
				}
				nodeOntologyJSON.put("class_name_lemmas", classNameLemmas);
				//text
				nodeOntologyJSON.put("text", text);
				//text keywords
				JSONArray textKeywords = new JSONArray();
				for(String keyword:textKeywordsList){
					textKeywords.put(keyword);
				}
				nodeOntologyJSON.put("text_keywords", textKeywords);
				//text lemmas
				JSONArray textLemmas = new JSONArray();
				for(String lemmas:textLemmasList){
					textLemmas.put(lemmas);
				}
				nodeOntologyJSON.put("text_lemmas", textLemmas);
				//text reference
				nodeOntologyJSON.put("text_reference", textReference);
				//text reference keywords
				JSONArray textReferenceKeywords = new JSONArray();
				for(String keyword:textReferenceKeywordsList){
					textReferenceKeywords.put(keyword);
				}
				nodeOntologyJSON.put("text_reference_keywords", textReferenceKeywords);
				//text reference lemmas
				JSONArray textReferenceLemmas = new JSONArray();
				for(String lemmas:textReferenceLemmasList){
					textReferenceLemmas.put(lemmas);
				}
				nodeOntologyJSON.put("text_reference_lemmas", textReferenceLemmas);
				//resource id
				nodeOntologyJSON.put("resource_id", resourceId);
				//resource id reference
				nodeOntologyJSON.put("resource_id_reference", resourceIdReference);
				//resource id reference keywords
				JSONArray resourceIdReferenceKeywords = new JSONArray();
				for(String keyword:resourceIdReferenceKeywordsList){
					resourceIdReferenceKeywords.put(keyword);
				}
				nodeOntologyJSON.put("resource_id_reference_keywords", resourceIdReferenceKeywords);
				//resource id reference lemmas
				JSONArray resourceIdReferenceLemmas = new JSONArray();
				for(String lemmas:resourceIdReferenceLemmasList){
					resourceIdReferenceLemmas.put(lemmas);
				}
				nodeOntologyJSON.put("resource_id_reference_lemmas", resourceIdReferenceLemmas);
				//image reference
				nodeOntologyJSON.put("image_reference", imageReference);
				//image reference keywords
				JSONArray imageReferenceKeywords = new JSONArray();
				for(String keyword:imageReferenceKeywordsList){
					imageReferenceKeywords.put(keyword);
				}
				nodeOntologyJSON.put("image_reference_keywords", imageReferenceKeywords);
				//image reference lemmas
				JSONArray imageReferenceLemmas = new JSONArray();
				for(String lemmas:imageReferenceLemmasList){
					imageReferenceLemmas.put(lemmas);
				}
				nodeOntologyJSON.put("image_reference_lemmas", imageReferenceLemmas);
				//add node ontology to array of ontologies
				GraphDBUtils.ontologyJSONArray.put(nodeOntologyJSON);
			}
			catch(JSONException je){
				System.err.println("exception while saving ontology");
				throw new RuntimeException("exception while saving ontology", je);
			}
	}
	
	
/////////////mf: operations on attributes////////////////////////////////////////
	private static String getClassNameFromFullClassName(String fullClassName){
		String result = "";
		if(fullClassName.equals("")){
			return result;
		}
		//pre process class name
		if(fullClassName.contains("$")){
			fullClassName = fullClassName.substring(fullClassName.lastIndexOf("$")+1);
		}
		if(fullClassName.contains(".")){
			fullClassName = fullClassName.substring(fullClassName.lastIndexOf(".")+1);
		}
		result=fullClassName;
		return result;
	}
	
	private static List<String> getKeywordsFromClassName(String className){
		List<String> resultList = new ArrayList<String>();
		if(className.equals("")){
			return resultList;
		}
		//use stringutils to split on camel case
		String[] components = StringUtils.splitByCharacterTypeCamelCase(className);
		for(String component:components){
			resultList.add(component);
		}
		return resultList;
	}
	
	private static List<String> getKeywordsFromText(String text){
		List<String> resultList = new ArrayList<String>();
		if(text.equals("")){
			return resultList;
		}
		String[] components = StringUtils.split(text, StringUtils.SPACE);
		for(String component:components){
			resultList.add(component);
		}
		return resultList;
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
	
	private static List<String> keywordsToLemmas(List<String> keywordsList){
		List<String> resultList = new ArrayList<String>();
		for(String keyword:keywordsList){
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma");
			RedwoodConfiguration.current().clear().apply();
			StanfordCoreNLP core = new StanfordCoreNLP(props);
			Annotation annotation = new Annotation(keyword);
			core.annotate(annotation);
			//get sentences
			List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
			//process each sentence
			for(CoreMap sentence : sentences){
				//process each token in sentence
				for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
					//get lemma of token
					String lemma = token.get(LemmaAnnotation.class);
					resultList.add(lemma);
				}
			}
		}
		return resultList;
	}
	
	
//////////////////////mf: old methods that might be useful//////////////////////////////////
//	private static int getResourceIdForView(NNode viewNode){
//		int resourceId = -1;
//		if(viewNode instanceof NInflNode){
//			NInflNode inflNode = (NInflNode) viewNode;
//			if(inflNode.idNode!=null){
//				resourceId = inflNode.idNode.getIdValue();
//			}			
//		}
//		else if(viewNode instanceof NMenuItemInflNode){
//			NMenuItemInflNode menuIteminflNode = (NMenuItemInflNode) viewNode;
//			if(menuIteminflNode.idNode!=null){
//				resourceId = menuIteminflNode.idNode.getIdValue();
//			}
//		}
//		else if(viewNode instanceof NViewAllocNode){
//			//new View case
//			NViewAllocNode allocNode = (NViewAllocNode) viewNode;
//			if(allocNode.idNode!=null){
//				resourceId = allocNode.idNode.getIdValue();
//			}
//		}
//		else{
//			System.err.println("getResourceIdForView: node type not handled "+viewNode.getClass().toString()+" "+viewNode.toString());
//			System.exit(-1);
//		}
//		return resourceId;
//	}
//	
//	//mf: get text as computed by gator
//	private static String getTextForView(NNode viewNode){
//		String text = "";
//		if(viewNode instanceof NInflNode){
//			NInflNode inflNode = (NInflNode) viewNode;
//			Iterator<NNode> textNodesIterator = inflNode.getTextNodes();
//			while(textNodesIterator.hasNext()){
//				NNode textNode = textNodesIterator.next();
//				if(textNode instanceof NStringConstantNode){
//					NStringConstantNode stringConstantNode = (NStringConstantNode) textNode;
//					text = text + " "+ stringConstantNode.value;
//				}
//				else if(textNode instanceof NStringIdNode){
//					//new View case
//					NStringIdNode stringIdNode = (NStringIdNode) textNode;
//					String idName = stringIdNode.getIdName();
//					idName = idName.replace("string_", "");
//					HashMap<String, String> stringIdValueMapping = Factory.getXMLParser().getStringIdValueMapping();
//					if(stringIdValueMapping!=null && stringIdValueMapping.containsKey(idName)){
//						text = text + " " + stringIdValueMapping.get(idName);
//					}
//					else{
//						System.out.println("getTextForView: can not find text for "+idName);
//					}
//				}
//				else{
//					System.err.println("getTextForView: text node type not handled "+textNode.getClass().toString());
//					System.exit(-1);
//				}
//			}
//			
//		}
//		else if(viewNode instanceof NMenuItemInflNode){
//			NMenuItemInflNode menuIteminflNode = (NMenuItemInflNode) viewNode;
//			Iterator<NNode> textNodesIterator = menuIteminflNode.getTextNodes();
//			while(textNodesIterator.hasNext()){
//				NNode textNode = textNodesIterator.next();
//				if(textNode instanceof NStringConstantNode){
//					NStringConstantNode stringConstantNode = (NStringConstantNode) textNode;
//					text = text + " "+ stringConstantNode.value;
//				}
//				else if(textNode instanceof NStringIdNode){
//					//new View case
//					NStringIdNode stringIdNode = (NStringIdNode) textNode;
//					String idName = stringIdNode.getIdName();
//					idName = idName.replace("string_", "");
//					HashMap<String, String> stringIdValueMapping = Factory.getXMLParser().getStringIdValueMapping();
//					if(stringIdValueMapping!=null && stringIdValueMapping.containsKey(idName)){
//						text = text + " " + stringIdValueMapping.get(idName);
//					}
//					else{
//						System.out.println("getTextForView: can not find text for "+idName);
//					}
//				}
//				else{
//					System.err.println("getTextForView: text node type not handled "+textNode.getClass().toString());
//					System.exit(-1);
//				}
//			}
//		}
//		else if(viewNode instanceof NViewAllocNode){
//			//new View case
//			NViewAllocNode allocNode = (NViewAllocNode) viewNode;
//			Iterator<NNode> textNodesIterator = allocNode.getTextNodes();
//			while(textNodesIterator.hasNext()){
//				NNode textNode = textNodesIterator.next();
//				if(textNode instanceof NStringConstantNode){
//					NStringConstantNode stringConstantNode = (NStringConstantNode) textNode;
//					text = text + " "+ stringConstantNode.value;
//				}
//				else if(textNode instanceof NStringIdNode){
//					//new View case
//					NStringIdNode stringIdNode = (NStringIdNode) textNode;
//					String idName = stringIdNode.getIdName();
//					idName = idName.replace("string_", "");
//					HashMap<String, String> stringIdValueMapping = Factory.getXMLParser().getStringIdValueMapping();
//					if(stringIdValueMapping!=null && stringIdValueMapping.containsKey(idName)){
//						text = text + " " + stringIdValueMapping.get(idName);
//					}
//					else{
//						System.out.println("getTextForView: can not find text for "+idName);
//					}
//				}
//				else{
//					System.err.println("getTextForView: text node type not handled "+textNode.getClass().toString());
//					System.exit(-1);
//				}
//			}
//		}
//		else{
//			System.err.println("getTextForView: node type not handled "+viewNode.getClass().toString()+" "+viewNode.toString());
//			System.exit(-1);
//		}
//		text = text.trim();
//		return text;
//	}
//	
//	//this saves the ontology
//	private static void saveOntology(NNode viewNode){
//		try{
//			JSONObject nodeOntologyJSON = new JSONObject();
//			String className = getClassNameForView(viewNode);
//			nodeOntologyJSON.put("class", className);
//			JSONArray textJSONArray = new JSONArray();
//			if(viewNode instanceof NInflNode){
//				NInflNode inflNode = (NInflNode) viewNode;
//				Iterator<NNode> textNodesIterator = inflNode.getTextNodes();
//				while(textNodesIterator.hasNext()){
//					NNode textNode = textNodesIterator.next();
//					if(textNode instanceof NStringConstantNode){
//						NStringConstantNode stringConstantNode = (NStringConstantNode) textNode;
//						textJSONArray.put(stringConstantNode.value);
//					}
//					else if(textNode instanceof NStringIdNode){
//						//new View case
//						NStringIdNode stringIdNode = (NStringIdNode) textNode;
//						String idName = stringIdNode.getIdName();
//						idName = idName.replace("string_", "");
//						HashMap<String, String> stringIdValueMapping = Factory.getXMLParser().getStringIdValueMapping();
//						if(stringIdValueMapping!=null && stringIdValueMapping.containsKey(idName)){
//							textJSONArray.put(stringIdValueMapping.get(idName));
//						}
//						else{
//							System.out.println("saveOntology: can not find text for "+idName);
//						}
//					}
//					else{
//						System.err.println("saveOntology: text node type not handled "+textNode.getClass().toString());
//						System.exit(-1);
//					}
//				}
//				
//			}
//			else if(viewNode instanceof NMenuItemInflNode){
//				NMenuItemInflNode menuIteminflNode = (NMenuItemInflNode) viewNode;
//				Iterator<NNode> textNodesIterator = menuIteminflNode.getTextNodes();
//				while(textNodesIterator.hasNext()){
//					NNode textNode = textNodesIterator.next();
//					if(textNode instanceof NStringConstantNode){
//						NStringConstantNode stringConstantNode = (NStringConstantNode) textNode;
//						textJSONArray.put(stringConstantNode.value);
//					}
//					else if(textNode instanceof NStringIdNode){
//						//new View case
//						NStringIdNode stringIdNode = (NStringIdNode) textNode;
//						String idName = stringIdNode.getIdName();
//						idName = idName.replace("string_", "");
//						HashMap<String, String> stringIdValueMapping = Factory.getXMLParser().getStringIdValueMapping();
//						if(stringIdValueMapping!=null && stringIdValueMapping.containsKey(idName)){
//							textJSONArray.put(stringIdValueMapping.get(idName));
//						}
//						else{
//							System.out.println("saveOntology: can not find text for "+idName);
//						}
//					}
//					else{
//						System.err.println("saveOntology: text node type not handled "+textNode.getClass().toString());
//						System.exit(-1);
//					}
//				}
//			}
//			else if(viewNode instanceof NViewAllocNode){
//				//new View case
//				NViewAllocNode allocNode = (NViewAllocNode) viewNode;
//				Iterator<NNode> textNodesIterator = allocNode.getTextNodes();
//				while(textNodesIterator.hasNext()){
//					NNode textNode = textNodesIterator.next();
//					if(textNode instanceof NStringConstantNode){
//						NStringConstantNode stringConstantNode = (NStringConstantNode) textNode;
//						textJSONArray.put(stringConstantNode.value);
//					}
//					else if(textNode instanceof NStringIdNode){
//						//new View case
//						NStringIdNode stringIdNode = (NStringIdNode) textNode;
//						String idName = stringIdNode.getIdName();
//						idName = idName.replace("string_", "");
//						HashMap<String, String> stringIdValueMapping = Factory.getXMLParser().getStringIdValueMapping();
//						if(stringIdValueMapping!=null && stringIdValueMapping.containsKey(idName)){
//							textJSONArray.put(stringIdValueMapping.get(idName));
//						}
//						else{
//							System.out.println("saveOntology: can not find text for "+idName);
//						}
//					}
//					else{
//						System.err.println("saveOntology: text node type not handled "+textNode.getClass().toString());
//						System.exit(-1);
//					}
//				}
//			}
//			else{
//				System.err.println("saveOntology: node type not handled "+viewNode.getClass().toString()+" "+viewNode.toString());
//				System.exit(-1);
//			}
//			nodeOntologyJSON.put("text", textJSONArray);
//			GraphDBUtils.ontologyJSONArray.put(nodeOntologyJSON);
//		}
//		catch(JSONException je){
//			System.err.println("exception while saving ontology");
//			throw new RuntimeException("exception while saving ontology",je);
//		}
//		
//	}
}
