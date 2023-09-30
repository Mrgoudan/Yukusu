package yakusu.android.test;

import java.util.HashMap;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec.Builder;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionPreferenceClickLong extends Action {

    private String text;

    public ActionPreferenceClickLong(String text){
        //no selector for preference
        this.selector=null;
        this.text=text;
    }

	@Override
	public void generateEspressoCode(Builder testMethodBuilder, HashMap<String, ClassName> classNameMap) {
		testMethodBuilder.addComment("preference click action");
		String statement = "onData($T.withTitleText(\""+this.text+"\")).perform($T.longClick())";
		testMethodBuilder.addStatement(statement, classNameMap.get("preferenceMatchersClassName"), classNameMap.get("viewActionsClassName"));
	}

}
