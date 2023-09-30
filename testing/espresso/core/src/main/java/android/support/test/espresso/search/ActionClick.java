package android.support.test.espresso.search;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.PrecisionDescriber;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.action.Tapper;
import android.support.test.espresso.util.HumanReadables;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;

import com.google.common.base.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by mattia on 7/12/17.
 */

public class ActionClick extends Action {

    public ActionClick(Selector selector){
        this.selector=selector;
    }

    @Override
    public JSONObject toJSON(){
        JSONObject result = new JSONObject();
        try {
            result.put("type", "click");
            result.put("selector", this.selector.toJSON());
        }
        catch(Exception e){
            Log.d("Espresso", "could not create JSON for action click");
            throw new RuntimeException("could not create JSON for action click", e);
        }
        return result;
    }

    public ActionClick copy(){
        ActionClick result = new ActionClick(this.selector.copy());
        return result;
    }

}
