package android.support.test.espresso.search;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mattia on 8/11/17.
 */

public class SelectorCoordinate extends Selector {

    private int x;
    private int y;

    public SelectorCoordinate(int x, int y){
        this.x=x;
        this.y=y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public SelectorCoordinate copy(){
        SelectorCoordinate result = new SelectorCoordinate(this.x, this.y);
        return result;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "coordinate");
            result.put("x", this.x);
            result.put("y", this.y);
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for coordinate selector");
            throw new RuntimeException("could not create JSON for coordinate selector", e);
        }
        return result;
    }

    @Override
    public String toString() {
        String result = "x:"+x+"#y:"+y;
        return result;
    }

}
