package yakusu.android.nlp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Ontology implements Comparable<Ontology>{
	private String fullClassName;
	private String className;
	private List<String> classNameKeywords;
	private List<String> classNameLemmas;
	private String text;
	private List<String> textKeywords;
	private List<String> textLemmas;
	private String textReference;
	private List<String> textReferenceKeywords;
	private List<String> textReferenceLemmas;
	private String resourceId;
	private String resourceIdReference;
	private List<String> resourceIdReferenceKeywords;
	private List<String> resourceIdReferenceLemmas;
	private String imageReference;
	private List<String> imageReferenceKeywords;
	private List<String> imageReferenceLemmas;
	
	@Override
	public int compareTo(Ontology ontology) {
		if(this.text.length()<ontology.getText().length()){
			return -1;
		}
		else if(this.text.length()==ontology.getText().length()){
			return 0;
		}
		else{
			return 1;
		}
	}
	
	public Ontology(JSONObject ontologyJSON){
//		nodeOntologyJSON.put("full_class_name", fullClassName);
		this.fullClassName=ontologyJSON.getString("full_class_name");
//		nodeOntologyJSON.put("class_name", className);
		this.className=ontologyJSON.getString("class_name");
//		nodeOntologyJSON.put("class_name_keywords", classNameKeywords);
		JSONArray classNameKeywordsArray = ontologyJSON.getJSONArray("class_name_keywords");
		this.classNameKeywords = new ArrayList<String>();
		for(int i=0; i<classNameKeywordsArray.length(); ++i){
			this.classNameKeywords.add(classNameKeywordsArray.getString(i));
		}
//		nodeOntologyJSON.put("class_name_lemmas", classNameLemmas);
		JSONArray classNameLemmasArray = ontologyJSON.getJSONArray("class_name_lemmas");
		this.classNameLemmas = new ArrayList<String>();
		for(int i=0; i<classNameLemmasArray.length(); ++i){
			this.classNameLemmas.add(classNameLemmasArray.getString(i));
		}
//		nodeOntologyJSON.put("text", text);
		this.text=ontologyJSON.getString("text");
//		nodeOntologyJSON.put("text_keywords", textKeywords);
		JSONArray textKeywordsArray = ontologyJSON.getJSONArray("text_keywords");
		this.textKeywords = new ArrayList<String>();
		for(int i=0; i<textKeywordsArray.length(); ++i){
			this.textKeywords.add(textKeywordsArray.getString(i));
		}
//		nodeOntologyJSON.put("text_lemmas", textLemmas);
		JSONArray textLemmasArray = ontologyJSON.getJSONArray("text_lemmas");
		this.textLemmas = new ArrayList<String>();
		for(int i=0; i<textLemmasArray.length(); ++i){
			this.textLemmas.add(textLemmasArray.getString(i));
		}
//		nodeOntologyJSON.put("text_reference", textReference);
		this.textReference=ontologyJSON.getString("text_reference");
//		nodeOntologyJSON.put("text_reference_keywords", textReferenceKeywords);
		JSONArray textReferenceKeywordsArray = ontologyJSON.getJSONArray("text_reference_keywords");
		this.textReferenceKeywords = new ArrayList<String>();
		for(int i=0; i<textReferenceKeywordsArray.length(); ++i){
			this.textReferenceKeywords.add(textReferenceKeywordsArray.getString(i));
		}
//		nodeOntologyJSON.put("text_reference_lemmas", textReferenceLemmas);
		JSONArray textReferenceLemmasArray = ontologyJSON.getJSONArray("text_reference_lemmas");
		this.textReferenceLemmas = new ArrayList<String>();
		for(int i=0; i<textReferenceLemmasArray.length(); ++i){
			this.textReferenceLemmas.add(textReferenceLemmasArray.getString(i));
		}
//		nodeOntologyJSON.put("resource_id", resourceId);
		this.resourceId=ontologyJSON.getString("resource_id");
//		nodeOntologyJSON.put("resource_id_reference", resourceIdReference);
		this.resourceIdReference=ontologyJSON.getString("resource_id_reference");
//		nodeOntologyJSON.put("resource_id_reference_keywords", resourceIdReferenceKeywords);
		JSONArray resourceIdReferenceKeywordsArray = ontologyJSON.getJSONArray("resource_id_reference_keywords");
		this.resourceIdReferenceKeywords = new ArrayList<String>();
		for(int i=0; i<resourceIdReferenceKeywordsArray.length(); ++i){
			this.resourceIdReferenceKeywords.add(resourceIdReferenceKeywordsArray.getString(i));
		}
//		nodeOntologyJSON.put("resource_id_reference_lemmas", resourceIdReferenceLemmas);
		JSONArray resourceIdReferenceLemmasArray = ontologyJSON.getJSONArray("resource_id_reference_lemmas");
		this.resourceIdReferenceLemmas = new ArrayList<String>();
		for(int i=0; i<resourceIdReferenceLemmasArray.length(); ++i){
			this.resourceIdReferenceLemmas.add(resourceIdReferenceLemmasArray.getString(i));
		}
//		nodeOntologyJSON.put("image_reference", imageReference);
		this.imageReference=ontologyJSON.getString("image_reference");
//		nodeOntologyJSON.put("image_reference_keywords", imageReferenceKeywords);
		JSONArray imageReferenceKeywordsArray = ontologyJSON.getJSONArray("image_reference_keywords");
		this.imageReferenceKeywords = new ArrayList<String>();
		for(int i=0; i<imageReferenceKeywordsArray.length(); ++i){
			this.imageReferenceKeywords.add(imageReferenceKeywordsArray.getString(i));
		}
//		nodeOntologyJSON.put("image_reference_lemmas", imageReferenceLemmas);
		JSONArray imageReferenceLemmasArray = ontologyJSON.getJSONArray("image_reference_lemmas");
		this.imageReferenceLemmas = new ArrayList<String>();
		for(int i=0; i<imageReferenceLemmasArray.length(); ++i){
			this.imageReferenceLemmas.add(imageReferenceLemmasArray.getString(i));
		}
	}

	public String getFullClassName() {
		return fullClassName;
	}

	public String getClassName() {
		return className;
	}

	public List<String> getClassNameKeywords() {
		return classNameKeywords;
	}

	public List<String> getClassNameLemmas() {
		return classNameLemmas;
	}

	public String getText() {
		return text;
	}

	public List<String> getTextKeywords() {
		return textKeywords;
	}

	public List<String> getTextLemmas() {
		return textLemmas;
	}

	public String getTextReference() {
		return textReference;
	}

	public List<String> getTextReferenceKeywords() {
		return textReferenceKeywords;
	}

	public List<String> getTextReferenceLemmas() {
		return textReferenceLemmas;
	}

	public String getResourceId() {
		return resourceId;
	}

	public String getResourceIdReference() {
		return resourceIdReference;
	}

	public List<String> getResourceIdReferenceKeywords() {
		return resourceIdReferenceKeywords;
	}

	public List<String> getResourceIdReferenceLemmas() {
		return resourceIdReferenceLemmas;
	}

	public String getImageReference() {
		return imageReference;
	}

	public List<String> getImageReferenceKeywords() {
		return imageReferenceKeywords;
	}

	public List<String> getImageReferenceLemmas() {
		return imageReferenceLemmas;
	}
	
}
