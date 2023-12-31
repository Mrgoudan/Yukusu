/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.test.espresso.action;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.util.ActivityLifecycles.hasForegroundActivities;
import static android.support.test.espresso.util.ActivityLifecycles.hasTransitioningActivities;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getOnlyElement;

import android.support.test.runner.lifecycle.ActivityLifecycleMonitor;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.util.HumanReadables;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.Collection;

/**
 * Enables pressing KeyEvents on views.
 */
public final class KeyEventAction implements ViewAction {
  private static final String TAG = KeyEventAction.class.getSimpleName();
  public static final int BACK_ACTIVITY_TRANSITION_MILLIS_DELAY = 150;
  public static final int CLEAR_TRANSITIONING_ACTIVITIES_ATTEMPTS = 4;
  public static final int CLEAR_TRANSITIONING_ACTIVITIES_MILLIS_DELAY = 150;

  private final EspressoKey key;

  public KeyEventAction(EspressoKey key) {
    this.key = checkNotNull(key);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Matcher<View> getConstraints() {
    return isDisplayed();
  }

  @Override
  public void perform(UiController uiController, View view) {
    if(this.key.getKeyCode()==KeyEvent.KEYCODE_MENU){
      uiController.loopMainThreadForAtLeast(3000);
    }
    try {
      if (!sendKeyEvent(uiController)) {
        Log.e(TAG, "Failed to inject key event: " + this.key);
        throw new PerformException.Builder()
          .withActionDescription(this.getDescription())
          .withViewDescription(HumanReadables.describe(view))
          .withCause(new RuntimeException("Failed to inject key event " + this.key))
          .build();
      }
    } catch (InjectEventSecurityException e) {
      Log.e(TAG, "Failed to inject key event: " + this.key);
      throw new PerformException.Builder()
        .withActionDescription(this.getDescription())
        .withViewDescription(HumanReadables.describe(view))
        .withCause(e)
        .build();
    }
  }

  private static boolean isActivityResumed(Activity activity) {
    return ActivityLifecycleMonitorRegistry.getInstance().getLifecycleStageOf(activity)
            == Stage.RESUMED;
  }

  private static Activity getCurrentActivity() {
    Collection<Activity> resumedActivities =
            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
    return getOnlyElement(resumedActivities);
  }

  private boolean sendKeyEvent(UiController controller)
          throws InjectEventSecurityException {

    Activity initialActivity = getCurrentActivity();

    boolean injected = false;
    long eventTime = SystemClock.uptimeMillis();
    for (int attempts = 0; !injected && attempts < 4; attempts++) {
      injected = controller.injectKeyEvent(new KeyEvent(eventTime,
          eventTime,
          KeyEvent.ACTION_DOWN,
          this.key.getKeyCode(),
          0,
          this.key.getMetaState()));
    }

    if (!injected) {
      // it is not a transient failure... :(
      return false;
    }

    injected = false;
    eventTime = SystemClock.uptimeMillis();
    for (int attempts = 0; !injected && attempts < 4; attempts++) {
      injected = controller.injectKeyEvent(
          new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, this.key.getKeyCode(), 0));
    }

    if (this.key.getKeyCode() == KeyEvent.KEYCODE_BACK) {
      // Wait for a Stage change of the initial activity.
      waitForStageChangeInitialActivity(controller, initialActivity);

      // Wait until there are no other pending activities in a foreground stage.
      waitForPendingForegroundActivities(controller);
    }

    return injected;
  }

  private void waitForStageChangeInitialActivity(
      UiController controller, Activity initialActivity) {
    if (isActivityResumed(initialActivity)) {
      // The activity transition hasn't happened yet, wait for it.
      controller.loopMainThreadForAtLeast(BACK_ACTIVITY_TRANSITION_MILLIS_DELAY);
      if (isActivityResumed(initialActivity)) {
        Log.e(TAG, "Back was pressed but there was no Activity stage transition in "
                + BACK_ACTIVITY_TRANSITION_MILLIS_DELAY
                + "ms, possibly due to a delay calling super.onBackPressed() from your Activity.");
      }
    }
  }

  private void waitForPendingForegroundActivities(UiController controller) {
    ActivityLifecycleMonitor activityLifecycleMonitor =
            ActivityLifecycleMonitorRegistry.getInstance();
    boolean pendingForegroundActivities = false;
    for (int attempts = 0; attempts < CLEAR_TRANSITIONING_ACTIVITIES_ATTEMPTS; attempts++) {
      controller.loopMainThreadUntilIdle();
      pendingForegroundActivities = hasTransitioningActivities(activityLifecycleMonitor);
      if (pendingForegroundActivities) {
        controller.loopMainThreadForAtLeast(CLEAR_TRANSITIONING_ACTIVITIES_MILLIS_DELAY);
      } else {
        break;
      }
    }

    // Pressing back can kill the app: throw exception.
    if (!hasForegroundActivities(activityLifecycleMonitor)) {
      throw new NoActivityResumedException("Pressed back and killed the app");
    }

    if (pendingForegroundActivities) {
      Log.e(TAG, "Back was pressed and left the application in an inconsistent state even after "
        + (CLEAR_TRANSITIONING_ACTIVITIES_MILLIS_DELAY * CLEAR_TRANSITIONING_ACTIVITIES_ATTEMPTS)
        +  "ms.");
      }
  }


  @Override
  public String getDescription() {
    return String.format("send %s key event", this.key);
  }
}
