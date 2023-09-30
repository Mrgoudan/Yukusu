package android.support.test.espresso.search;

import android.support.test.espresso.UiController;

import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public abstract class Action {

    protected Selector selector;

    public abstract JSONObject toJSON();

    public Selector getSelector(){
        return selector;
    }

    public abstract Action copy();
}
