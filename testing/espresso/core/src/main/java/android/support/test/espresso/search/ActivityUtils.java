package android.support.test.espresso.search;

import android.app.Activity;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import java.util.Collection;
import java.util.NoSuchElementException;

import static com.google.common.collect.Iterables.getOnlyElement;

/**
 * Created by mattia on 7/13/17.
 */

public class ActivityUtils {

    public static Activity getCurrentActivity() {
        Collection<Activity> resumedActivities =
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
        return getOnlyElement(resumedActivities);
    }

    public static boolean isActivityResumed(Activity activity) {
        return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity)
                == Stage.RESUMED;
    }
}
