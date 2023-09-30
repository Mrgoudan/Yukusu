package yakusu.android.test;

import java.util.HashMap;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec.Builder;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionDrawer extends Action {

    private int direction;//1 is open from left, 0 is close left

    public ActionDrawer(Selector selector, int direction){
        this.selector=selector;
        this.direction=direction;
    }

    public int getDirection(){
        return direction;
    }

	@Override
	public void generateEspressoCode(Builder testMethodBuilder, HashMap<String, ClassName> classNameMap) {
		testMethodBuilder.addComment("drawer action");
		String statement = "onView("+selector.getEspressoCode()+").perform($T.drawer("+direction+"))";
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
