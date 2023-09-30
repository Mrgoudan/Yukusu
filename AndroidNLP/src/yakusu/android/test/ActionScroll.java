package yakusu.android.test;

import java.util.HashMap;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec.Builder;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionScroll extends Action {

    private int direction;//direction 0==up, 1=down, 2=left, 3=right

    public ActionScroll(Selector selector, int direction){
        this.selector=selector;
        this.direction=direction;
    }

    public int getDirection(){
        return direction;
    }
    
	@Override
	public void generateEspressoCode(Builder testMethodBuilder, HashMap<String, ClassName> classNameMap) {
		testMethodBuilder.addComment("scroll action");
		String statement = "onView("+selector.getEspressoCode()+").perform($T.scroll("+direction+"))";
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
