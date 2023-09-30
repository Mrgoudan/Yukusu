package yakusu.android.test;

import java.util.HashMap;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec.Builder;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionType extends Action {

    private String text;

    public ActionType(Selector selector, String text){
        this.selector=selector;
        this.text=text;
    }

    public String getText(){
        return text;
    }

	@Override
	public void generateEspressoCode(Builder testMethodBuilder, HashMap<String, ClassName> classNameMap) {
		testMethodBuilder.addComment("type action");
		String statement = "onView("+selector.getEspressoCode()+").perform($T.typeText(\""+this.text+"\"))";
		if(selector instanceof SelectorCoordinate){
			SelectorCoordinate selectorCoordinate = (SelectorCoordinate) selector;
			testMethodBuilder.addStatement(statement, classNameMap.get("viewMatchersClassName"), selectorCoordinate.getX(), selectorCoordinate.getY(), classNameMap.get("viewActionsClassName"));
		}
		else if(selector instanceof SelectorResourceId){
			SelectorResourceId selectorResourceId = (SelectorResourceId) selector;
			testMethodBuilder.addStatement(statement, classNameMap.get("viewMatchersClassName"), selectorResourceId.getResourceId(), classNameMap.get("viewActionsClassName"));
		}
		else if(selector instanceof SelectorText){
			SelectorText selectorText = (SelectorText) selector;
			testMethodBuilder.addStatement(statement, classNameMap.get("viewMatchersClassName"), selectorText.getText(), classNameMap.get("viewActionsClassName"));
		}
		else if(selector instanceof SelectorXPath){
			SelectorXPath selectorXPath = (SelectorXPath) selector;
			testMethodBuilder.addStatement(statement, classNameMap.get("viewMatchersClassName"), selectorXPath.getXPath(), classNameMap.get("viewActionsClassName"));
		}
	}

}
