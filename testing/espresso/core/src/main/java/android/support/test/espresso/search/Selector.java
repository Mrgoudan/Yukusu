package android.support.test.espresso.search;

import org.json.JSONObject;

/**
 * Created by mattia on 8/11/17.
 */

public abstract class Selector {

    public abstract Selector copy();

    public abstract JSONObject toJSON();

    @Override
    public abstract String toString();
}
