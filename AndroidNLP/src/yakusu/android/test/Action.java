package yakusu.android.test;

import java.util.HashMap;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

/**
 * Created by mattia on 7/12/17.
 */

public abstract class Action {

    protected Selector selector;

    public Selector getSelector(){
        return selector;
    }
    
    public abstract void generateEspressoCode(MethodSpec.Builder testMethodBuilder, HashMap<String, ClassName> classNameMap);
}
