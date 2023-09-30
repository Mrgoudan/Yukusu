package yakusu.android.nlp;

public class WordUtils {
	
	public static String ELEMENT_STRING = "element";
	public static int ELEMENT_INDEX = 0;
	//use quote to better parse sentence but content of quote was not in ontology
	public static String QUOTE_STRING = "quoatation";
	public static int QUOTE_INDEX = 0;
	
	public static String replaceToken(String lemma, String pos){
		String result = "";
		lemma = lemma.toLowerCase();
		if(lemma.equals("tap")){//should stay
			if(!pos.startsWith("V")){
				result = "click";
			}
			else{
				if(pos.equals("VB")){
					result = "click";
				}
				else if(pos.equals("VBD")){
					result = "clicked";
				}
				else if(pos.equals("VBG")){
					result = "clicking";
				}
				else if(pos.equals("VBN")){
					result = "clicked";
				}
				else if(pos.equals("VBP")){
					result = "click";
				}
				else {
					result = "clicks";
				}
			}
			return result;
		}
		if(lemma.equals("press")){//should stay
			if(!pos.startsWith("V")){
				result = "click";
			}
			else{
				if(pos.equals("VB")){
					result = "click";
				}
				else if(pos.equals("VBD")){
					result = "clicked";
				}
				else if(pos.equals("VBG")){
					result = "clicking";
				}
				else if(pos.equals("VBN")){
					result = "clicked";
				}
				else if(pos.equals("VBP")){
					result = "click";
				}
				else {
					result = "clicks";
				}
			}
			return result;
		}
		if(lemma.equals("select")){//should stay
			if(!pos.startsWith("V")){
				result = "click";
			}
			else{
				if(pos.equals("VB")){
					result = "click";
				}
				else if(pos.equals("VBD")){
					result = "clicked";
				}
				else if(pos.equals("VBG")){
					result = "clicking";
				}
				else if(pos.equals("VBN")){
					result = "clicked";
				}
				else if(pos.equals("VBP")){
					result = "click";
				}
				else {
					result = "clicks";
				}
			}
			return result;
		}
		if(lemma.equals("choose")){//should stay
			if(!pos.startsWith("V")){
				result = "click";
			}
			else{
				if(pos.equals("VB")){
					result = "click";
				}
				else if(pos.equals("VBD")){
					result = "clicked";
				}
				else if(pos.equals("VBG")){
					result = "clicking";
				}
				else if(pos.equals("VBN")){
					result = "clicked";
				}
				else if(pos.equals("VBP")){
					result = "click";
				}
				else {
					result = "clicks";
				}
			}
			return result;
		}
		if(lemma.equals("expand")){//should stay
			if(!pos.startsWith("V")){
				result = "click";
			}
			else{
				if(pos.equals("VB")){
					result = "click";
				}
				else if(pos.equals("VBD")){
					result = "clicked";
				}
				else if(pos.equals("VBG")){
					result = "clicking";
				}
				else if(pos.equals("VBN")){
					result = "clicked";
				}
				else if(pos.equals("VBP")){
					result = "click";
				}
				else {
					result = "clicks";
				}
			}
			return result;
		}
//		if(lemma.equals("use")){//should stay
//			if(!pos.startsWith("V")){
//				result = "click";
//			}
//			else{
//				if(pos.equals("VB")){
//					result = "click";
//				}
//				else if(pos.equals("VBD")){
//					result = "clicked";
//				}
//				else if(pos.equals("VBG")){
//					result = "clicking";
//				}
//				else if(pos.equals("VBN")){
//					result = "clicked";
//				}
//				else if(pos.equals("VBP")){
//					result = "click";
//				}
//				else {
//					result = "clicks";
//				}
//			}
//			return result;
//		}
		if(lemma.equals("push")){//should stay
			if(!pos.startsWith("V")){
				result = "click";
			}
			else{
				if(pos.equals("VB")){
					result = "click";
				}
				else if(pos.equals("VBD")){
					result = "clicked";
				}
				else if(pos.equals("VBG")){
					result = "clicking";
				}
				else if(pos.equals("VBN")){
					result = "clicked";
				}
				else if(pos.equals("VBP")){
					result = "click";
				}
				else {
					result = "clicks";
				}
			}
			return result;
		}
		if(lemma.equals("long-press")){
			result = "long click";
			return result;
		}	
		if(lemma.equals("longpress")){
			result = "long click";
			return result;
		}
		if(lemma.equals("enter")){
			if(!pos.startsWith("V")){
				result = "write";
			}
			else{
				if(pos.equals("VB")){
					result = "write";
				}
				else if(pos.equals("VBD")){
					result = "wrote";
				}
				else if(pos.equals("VBG")){
					result = "writing";
				}
				else if(pos.equals("VBN")){
					result = "written";
				}
				else if(pos.equals("VBP")){
					result = "write";
				}
				else {
					result = "writes";
				}
			}
			return result;
		}
		if(lemma.equals("input")){
			if(!pos.startsWith("V")){
				result = "write";
			}
			else{
				if(pos.equals("VB")){
					result = "write";
				}
				else if(pos.equals("VBD")){
					result = "wrote";
				}
				else if(pos.equals("VBG")){
					result = "writing";
				}
				else if(pos.equals("VBN")){
					result = "written";
				}
				else if(pos.equals("VBP")){
					result = "write";
				}
				else {
					result = "writes";
				}
			}
			return result;
		}
		if(lemma.equals("insert")){
			if(!pos.startsWith("V")){
				result = "write";
			}
			else{
				if(pos.equals("VB")){
					result = "write";
				}
				else if(pos.equals("VBD")){
					result = "wrote";
				}
				else if(pos.equals("VBG")){
					result = "writing";
				}
				else if(pos.equals("VBN")){
					result = "written";
				}
				else if(pos.equals("VBP")){
					result = "write";
				}
				else {
					result = "writes";
				}
			}
			return result;
		}
		if(lemma.equals("typing")){
			result = "writing";
			return result;
		}
		if(lemma.equals("swipe")){//navigate aids the parsing
			if(!pos.startsWith("V")){
				result = "navigate";
			}
			else{
				if(pos.equals("VB")){
					result = "navigate";
				}
				else if(pos.equals("VBD")){
					result = "navigated";
				}
				else if(pos.equals("VBG")){
					result = "navigating";
				}
				else if(pos.equals("VBN")){
					result = "navigated";
				}
				else if(pos.equals("VBP")){
					result = "navigate";
				}
				else {
					result = "navigates";
				}
			}
			return result;
		}
//		if(lemma.equals("scroll")){
//			result = "fling";
//			return result;
//		}
		//app specific
		//40
		if(lemma.equals("...")){//navigate aids the parsing
			result = "ic";
		}
		if(lemma.equals("+")){//navigate aids the parsing
			result = "Add";
		}
		return result;
	}
	
	public static boolean isBeforeLemma(String lemma){
		lemma = lemma.toLowerCase();
		if(lemma.equals("before")){
			return true;
		}
		return false;
	}
	
	public static boolean isAfterLemma(String lemma){
		lemma = lemma.toLowerCase();
		if(lemma.equals("after")){
			return true;
		}
		return false;
	}
	
	public static boolean isContainerLemma(String lemma){
		boolean result = false;
		lemma = lemma.toLowerCase();
		if(lemma.equals("list")){
			result = true;
		}
		return result;
	}
	
	public static String getContainerForLemma(String lemma){
		String result = "";
		lemma = lemma.toLowerCase();
		if(lemma.equals("item")){
			result = "list";
		}
		return result;
	}
	
	//compute statistic for this
	public static boolean isGenericLemma(String lemma){
		boolean result = false;
		lemma = lemma.toLowerCase();
		if(lemma.equals("item") ||
				lemma.equals("button") || 
				lemma.equals("tab") ||
				lemma.equals("option") ||
				lemma.equals("section") ||
				lemma.equals("dialog") || lemma.equals("dialogue") ||
				lemma.equals("icon") ||
				lemma.equals("drawer") || lemma.equals("navigation") ||
				//lemma.equals("app") ||
				lemma.equals("list") ||
				lemma.equals("activity")
				){
			result = true;
		}
		return result;
	}
	
	public static boolean isCompleteDirectionLemma(String lemma){
		boolean result = false;
		lemma = lemma.toLowerCase();
		if(lemma.equals("bottom") || lemma.equals("top")){
			result = true;
		}
		return result;
	}
	
	public static int getCompleteDirectionForLemma(String lemma){	
		int result = -1;
		lemma = lemma.toLowerCase();
		if(lemma.equals("top")){
			result = 0;
		}
		if(lemma.equals("bottom")){
			result = 1;
		}
		return result;
	}
	
	public static boolean isDirectionLemma(String lemma){
		boolean result = false;
		lemma = lemma.toLowerCase();
		if(lemma.equals("up") || lemma.equals("down") || lemma.equals("left") || lemma.equals("right")){
			result = true;
		}
		return result;
	}
	
	public static int getDirectionForLemma(String lemma){	
		int result = -1;
		lemma = lemma.toLowerCase();
		if(lemma.equals("up")){
			result = 0;
		}
		if(lemma.equals("down")){
			result = 1;
		}
		if(lemma.equals("left")){
			result = 2;
		}
		if(lemma.equals("right")){
			result = 3;
		}
		return result;
	}
	
	public static boolean isMultipleLemma(String lemma){
		boolean result = false;
		lemma = lemma.toLowerCase();
		if(lemma.equals("multiple")){
			result = true;
		}
		if(lemma.equals("several")){
			result = true;
		}
		return result;
	}
	
	public static boolean isInstallLemma(String lemma){
		boolean result = false;
		lemma = lemma.toLowerCase();
		if(lemma.equals("install")){
			result = true;
		}
		if(lemma.equals("reinstall")){
			result = true;
		}
		return result;
	}
	
	public static boolean isStartLemma(String lemma){
		boolean result = false;
		lemma = lemma.toLowerCase();
		if(lemma.equals("click")){
			result = true;
		}
		if(lemma.equals("open")){
			result = true;
		}
		if(lemma.equals("launch")){
			result = true;
		}
		if(lemma.equals("run")){
			result = true;
		}
		if(lemma.equals("load")){
			result = true;
		}
		if(lemma.equals("go")){
			result = true;
		}
		return result;
	}
	
	public static boolean isErrorLemma(String lemma){
		boolean result = false;
		lemma = lemma.toLowerCase();
		if(lemma.equals("crash")){
			result = true;
		}
		if(lemma.equals("error")){
			result = true;
		}
		if(lemma.equals("hang")){
			result = true;
		}
		if(lemma.equals("unusable")){
			result = true;
		}
		return result;
	}
	
	public static boolean isWaitLemma(String lemma){
		boolean result = false;
		lemma = lemma.toLowerCase();
		if(lemma.equals("wait")){
			result = true;
		}
		return result;
	}
	
	public static boolean isNothingLemma(String lemma){
		boolean result = false;
		lemma = lemma.toLowerCase();
		if(lemma.equals("empty")){
			result = true;
		}
		return result;
	}
}
