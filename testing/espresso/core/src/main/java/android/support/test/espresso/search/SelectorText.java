package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 8/11/17.
 */

public class SelectorText extends Selector {

    private String text;

    public SelectorText(String text){
        this.text=text;
    }

    public String getText(){
        return text;
    }

    public SelectorText copy(){
        SelectorText result = new SelectorText(this.text);
        return result;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "text");
            result.put("text", this.text);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for text selector");
            throw new RuntimeException("could not create JSON for text selector", e);
        }
        return result;
    }

    @Override
    public String toString() {
        return text;
    }

}
