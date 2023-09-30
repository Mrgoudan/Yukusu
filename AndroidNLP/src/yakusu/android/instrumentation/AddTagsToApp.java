package yakusu.android.instrumentation;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AddTagsToApp {

	public static void main(String[] args) {
		if(args.length!=1){
			System.out.println("usage: java -jar AddTagsToApp.jar source_root");
			System.exit(-1);
		}
		
		String sourceRoot = args[0];
		
		File sourceRootFolder = new File(sourceRoot);
		List<File> workList = new ArrayList<File>();
		workList.add(sourceRootFolder);
		while(!workList.isEmpty()){
			File currFile = workList.remove(0);
			if(currFile.isDirectory()){
				File[] containedFilesArray = currFile.listFiles();
				for(File containedFile:containedFilesArray){
					workList.add(containedFile);
				}
			}
			else{
				//check if it is xml file
				if(currFile.getAbsolutePath().endsWith(".xml")){
					if(currFile.getAbsolutePath().endsWith("values"+File.separator+"strings.xml")){
						addIdsForTags(currFile);
					}
					else{
						if(currFile.getAbsolutePath().endsWith("AndroidManifest.xml")){
							continue;
						}
						addTags(currFile);
					}
				}
			}
		}
	}
	
	private static void addTags(File file){
		Document document;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			document = dBuilder.parse(file);
			
			boolean documentChanged = false;

			Element rootElement = document.getDocumentElement();
			List<Element> workList = new ArrayList<Element>();
			workList.add(rootElement);
			while(!workList.isEmpty()){
				Element currElement = workList.remove(0);
				if(currElement.getTagName().equals("PreferenceScreen")){
					prettyPrint(document);
					continue;
				}
				
				//take care of text reference
				if(currElement.hasAttribute("android:text")){
					documentChanged = true;
					String textReference = currElement.getAttribute("android:text");
					Element textReferenceTagElement = document.createElement("tag");
					textReferenceTagElement.setAttribute("android:id", "@id/TAG_TEXT_REFERENCE");
					textReferenceTagElement.setAttribute("android:value", "referenceprefix"+textReference);
					currElement.appendChild(textReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:hint")){
					documentChanged = true;
					String textReference = currElement.getAttribute("android:hint");
					Element textReferenceTagElement = document.createElement("tag");
					textReferenceTagElement.setAttribute("android:id", "@id/TAG_TEXT_REFERENCE");
					textReferenceTagElement.setAttribute("android:value", "referenceprefix"+textReference);
					currElement.appendChild(textReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:label")){
					documentChanged = true;
					String textReference = currElement.getAttribute("android:label");
					Element textReferenceTagElement = document.createElement("tag");
					textReferenceTagElement.setAttribute("android:id", "@id/TAG_TEXT_REFERENCE");
					textReferenceTagElement.setAttribute("android:value", "referenceprefix"+textReference);
					currElement.appendChild(textReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:title")){
					documentChanged = true;
					String textReference = currElement.getAttribute("android:title");
					Element textReferenceTagElement = document.createElement("tag");
					textReferenceTagElement.setAttribute("android:id", "@id/TAG_TEXT_REFERENCE");
					textReferenceTagElement.setAttribute("android:value", "referenceprefix"+textReference);
					currElement.appendChild(textReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:contentDescription")){
					documentChanged = true;
					String textReference = currElement.getAttribute("android:contentDescription");
					Element textReferenceTagElement = document.createElement("tag");
					textReferenceTagElement.setAttribute("android:id", "@id/TAG_TEXT_REFERENCE");
					textReferenceTagElement.setAttribute("android:value", "referenceprefix"+textReference);
					currElement.appendChild(textReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:textOn")){
					documentChanged = true;
					String textReference = currElement.getAttribute("android:textOn");
					Element textReferenceTagElement = document.createElement("tag");
					textReferenceTagElement.setAttribute("android:id", "@id/TAG_TEXT_REFERENCE");
					textReferenceTagElement.setAttribute("android:value", "referenceprefix"+textReference);
					currElement.appendChild(textReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:textOff")){
					documentChanged = true;
					String textReference = currElement.getAttribute("android:textOff");
					Element textReferenceTagElement = document.createElement("tag");
					textReferenceTagElement.setAttribute("android:id", "@id/TAG_TEXT_REFERENCE");
					textReferenceTagElement.setAttribute("android:value", "referenceprefix"+textReference);
					currElement.appendChild(textReferenceTagElement);
				}
				
				//take care of image reference
				if(currElement.hasAttribute("android:src")){
					documentChanged = true;
					String imageReference = currElement.getAttribute("android:src");
					Element imageReferenceTagElement = document.createElement("tag");
					imageReferenceTagElement.setAttribute("android:id", "@id/TAG_IMAGE_REFERENCE");
					imageReferenceTagElement.setAttribute("android:value", "referenceprefix"+imageReference);
					currElement.appendChild(imageReferenceTagElement);
				}
				else if(currElement.hasAttribute("app:srcCompat")){
					documentChanged = true;
					String imageReference = currElement.getAttribute("app:srcCompat");
					Element imageReferenceTagElement = document.createElement("tag");
					imageReferenceTagElement.setAttribute("android:id", "@id/TAG_IMAGE_REFERENCE");
					imageReferenceTagElement.setAttribute("android:value", "referenceprefix"+imageReference);
					currElement.appendChild(imageReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:background")){
					documentChanged = true;
					String imageReference = currElement.getAttribute("android:background");
					Element imageReferenceTagElement = document.createElement("tag");
					imageReferenceTagElement.setAttribute("android:id", "@id/TAG_IMAGE_REFERENCE");
					imageReferenceTagElement.setAttribute("android:value", "referenceprefix"+imageReference);
					currElement.appendChild(imageReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:drawableLeft")){
					documentChanged = true;
					String imageReference = currElement.getAttribute("android:drawableLeft");
					Element imageReferenceTagElement = document.createElement("tag");
					imageReferenceTagElement.setAttribute("android:id", "@id/TAG_IMAGE_REFERENCE");
					imageReferenceTagElement.setAttribute("android:value", "referenceprefix"+imageReference);
					currElement.appendChild(imageReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:drawableRight")){
					documentChanged = true;
					String imageReference = currElement.getAttribute("android:drawableRight");
					Element imageReferenceTagElement = document.createElement("tag");
					imageReferenceTagElement.setAttribute("android:id", "@id/TAG_IMAGE_REFERENCE");
					imageReferenceTagElement.setAttribute("android:value", "referenceprefix"+imageReference);
					currElement.appendChild(imageReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:drawableTop")){
					documentChanged = true;
					String imageReference = currElement.getAttribute("android:drawableTop");
					Element imageReferenceTagElement = document.createElement("tag");
					imageReferenceTagElement.setAttribute("android:id", "@id/TAG_IMAGE_REFERENCE");
					imageReferenceTagElement.setAttribute("android:value", "referenceprefix"+imageReference);
					currElement.appendChild(imageReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:drawableBottom")){
					documentChanged = true;
					String imageReference = currElement.getAttribute("android:drawableBottom");
					Element imageReferenceTagElement = document.createElement("tag");
					imageReferenceTagElement.setAttribute("android:id", "@id/TAG_IMAGE_REFERENCE");
					imageReferenceTagElement.setAttribute("android:value", "referenceprefix"+imageReference);
					currElement.appendChild(imageReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:drawableStart")){
					documentChanged = true;
					String imageReference = currElement.getAttribute("android:drawableStart");
					Element imageReferenceTagElement = document.createElement("tag");
					imageReferenceTagElement.setAttribute("android:id", "@id/TAG_IMAGE_REFERENCE");
					imageReferenceTagElement.setAttribute("android:value", "referenceprefix"+imageReference);
					currElement.appendChild(imageReferenceTagElement);
				}
				else if(currElement.hasAttribute("android:drawableEnd")){
					documentChanged = true;
					String imageReference = currElement.getAttribute("android:drawableEnd");
					Element imageReferenceTagElement = document.createElement("tag");
					imageReferenceTagElement.setAttribute("android:id", "@id/TAG_IMAGE_REFERENCE");
					imageReferenceTagElement.setAttribute("android:value", "referenceprefix"+imageReference);
					currElement.appendChild(imageReferenceTagElement);
				}
				
				//add child nodes to worklist
				NodeList currElementChildNodeList = currElement.getChildNodes();
				for(int i=0; i<currElementChildNodeList.getLength(); ++i){
					Node childNode = currElementChildNodeList.item(i);
					if(childNode instanceof Element){
						Element childElement = (Element) childNode;
						workList.add(childElement);
					}
				}
			}
			
			if(documentChanged){
				saveToFile(document, file.getAbsolutePath());
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	    
	}
	
	private static void addIdsForTags(File file){
		Document document;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			document = dBuilder.parse(file);

			Element rootElement = document.getDocumentElement();
			Element imageReferenceItemElement = document.createElement("item");
			imageReferenceItemElement.setAttribute("type", "id");
			imageReferenceItemElement.setAttribute("name", "TAG_IMAGE_REFERENCE");
			rootElement.appendChild(imageReferenceItemElement);

			Element textReferenceItemElement = document.createElement("item");
			textReferenceItemElement.setAttribute("type", "id");
			textReferenceItemElement.setAttribute("name", "TAG_TEXT_REFERENCE");
			rootElement.appendChild(textReferenceItemElement);

			saveToFile(document, file.getAbsolutePath());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	    
	}
	
	private static void saveToFile(Document document, String fileName) throws Exception{
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		Result output = new StreamResult(new File(fileName));
		Source input = new DOMSource(document);
		transformer.transform(input, output);
	}
	
	private static void prettyPrint(Document document) throws Exception {
	    Transformer tf = TransformerFactory.newInstance().newTransformer();
	    tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    tf.setOutputProperty(OutputKeys.INDENT, "yes");
	    Writer out = new StringWriter();
	    tf.transform(new DOMSource(document), new StreamResult(out));
	    System.out.println(out.toString());
    }


}
