package yakusu.android.test;

import java.util.HashMap;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec.Builder;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionSleep extends Action {
	
	private long time;

    public ActionSleep(Selector selector, long time){
        this.selector=selector;
        this.time=time;
    }

	@Override
	public void generateEspressoCode(Builder testMethodBuilder, HashMap<String, ClassName> classNameMap) {
		testMethodBuilder.addComment("sleep action");
		String statement = "onView("+selector.getEspressoCode()+").perform($T.sleep("+time+"))";
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
