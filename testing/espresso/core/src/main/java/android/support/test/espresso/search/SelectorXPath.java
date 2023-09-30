package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 8/11/17.
 */

public class SelectorXPath extends Selector {

    private String xPath;

    public SelectorXPath(String xPath){
        this.xPath=xPath;
    }

    public String getXPath(){
        return xPath;
    }

    public SelectorXPath copy(){
        SelectorXPath result = new SelectorXPath(this.xPath);
        return result;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "xpath");
            result.put("xpath", this.xPath);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for XPath selector");
            throw new RuntimeException("could not create JSON for XPath selector", e);
        }
        return result;
    }

    @Override
    public String toString() {
        return xPath;
    }

}
