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

package android.support.test.espresso.base;

import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.RootMatchers.isFocusable;
import static com.google.common.base.Preconditions.checkState;

import android.support.test.runner.lifecycle.ActivityLifecycleMonitor;
import android.support.test.runner.lifecycle.Stage;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.espresso.NoMatchingRootException;
import android.support.test.espresso.Root;
import android.support.test.espresso.UiController;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import android.app.Activity;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Provides the root View of the top-most Window, with which the user can interact. View is
 * guaranteed to be in a stable state - i.e. not pending any updates from the application.
 *
 * This provider can only be accessed from the main thread.
 */
@Singleton
public final class RootViewPicker implements Provider<View> {
  private static final String TAG = RootViewPicker.class.getSimpleName();

  private final ActiveRootLister activeRootLister;
  private final UiController uiController;
  private final ActivityLifecycleMonitor activityLifecycleMonitor;
  private final AtomicReference<Matcher<Root>> rootMatcherRef;


  @Inject
  RootViewPicker(ActiveRootLister activeRootLister, UiController uiController,
      ActivityLifecycleMonitor activityLifecycleMonitor,
      AtomicReference<Matcher<Root>> rootMatcherRef) {
    this.activeRootLister = activeRootLister;
    this.uiController = uiController;
    this.activityLifecycleMonitor = activityLifecycleMonitor;
    this.rootMatcherRef = rootMatcherRef;
  }

  @Override
  public View get() {
    checkState(Looper.getMainLooper().equals(Looper.myLooper()), "must be called on main thread.");
    Matcher<Root> rootMatcher = rootMatcherRef.get();

    FindRootResult findResult = findRoot(rootMatcher);

    // we only want to propagate a root view that the user can interact with and is not
    // about to relay itself out. An app should be in this state the majority of the time,
    // if we happen not to be in this state at the moment, process the queue some more
    // we should come to it quickly enough.
    int loops = 0;

    while (!isReady(findResult.needle)) {
      if (loops < 3) {
        uiController.loopMainThreadUntilIdle();
      } else if (loops < 1001) {

        // loopUntil idle effectively is polling and pegs the CPU... if we don't have an update to
        // process immediately, we might have something coming very very soon.
        uiController.loopMainThreadForAtLeast(10);
      } else {
        // we've waited for the root view to be fully laid out and have window focus
        // for over 10 seconds. something is wrong.
        throw new RuntimeException(String.format("Waited for the root of the view hierarchy to have"
            + " window focus and not be requesting layout for over 10 seconds. If you specified a"
            + " non default root matcher, it may be picking a root that never takes focus."
            + " Otherwise, something is seriously wrong. Selected Root:\n%s\n. All Roots:\n%s"
            , findResult.needle, Joiner.on("\n").join(findResult.haystack)));
      }

      findResult = findRoot(rootMatcher);
      loops++;
    }

    return findResult.needle.getDecorView();
  }

  private boolean isReady(Root root) {
    // Root is ready (i.e. UI is no longer in flux) if layout of the root view is not being
    // requested and the root view has window focus (if it is focusable).
    View rootView = root.getDecorView();
    if (!rootView.isLayoutRequested()) {
      return rootView.hasWindowFocus() || !isFocusable().matches(root);
    }
    return false;
  }

  private static class FindRootResult {
    private final Root needle;
    private final List<Root> haystack;

    FindRootResult(Root needle, List<Root> haystack) {
      this.needle = needle;
      this.haystack = haystack;
    }
  }

  private FindRootResult findRoot(Matcher<Root> rootMatcher) {
    waitForAtLeastOneActivityToBeResumed();

    List<Root> roots = activeRootLister.listActiveRoots();

    // TODO(user): move these checks into the RootsOracle.
    if (roots.isEmpty()) {
      // Reflection broke
      throw new RuntimeException("No root window were discovered.");
    }

    if (roots.size() > 1) {
      // Multiple roots only occur:
      // when multiple activities are in some state of their lifecycle in the application
      // - we don't care about this, since we only want to interact with the RESUMED
      // activity, all other activities windows are not visible to the user so, out of
      // scope.
      // when a PopupWindow or PopupMenu is used
      // - this is a case where we definitely want to consider the top most window, since
      // it probably has the most useful info in it.
      // when an android.app.dialog is shown
      // - again, this is getting all the users attention, so it gets the test attention
      // too.
      if (Log.isLoggable(TAG, Log.VERBOSE)) {
        Log.v(TAG, String.format("Multiple windows detected: %s", roots));
      }
      //mf: added for debugging
      if(Log.isLoggable("Espresso", Log.DEBUG)){
        Log.d("Espresso", String.format("Multiple windows detected: %s", roots));
      }
    }

    List<Root> selectedRoots = Lists.newArrayList();
    for (Root root : roots) {
      if (rootMatcher.matches(root)) {
        selectedRoots.add(root);
      }
    }

    if (selectedRoots.isEmpty()) {
      throw NoMatchingRootException.create(rootMatcher, roots);
    }

    return new FindRootResult(reduceRoots(selectedRoots), roots);
  }

  @SuppressWarnings("unused")
  private void waitForAtLeastOneActivityToBeResumed() {
    Collection<Activity> resumedActivities =
        activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED);
    if (resumedActivities.isEmpty()) {
      uiController.loopMainThreadUntilIdle();
      resumedActivities = activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED);
    }
    if (resumedActivities.isEmpty()) {
      List<Activity> activities = Lists.newArrayList();
      for (Stage s : EnumSet.range(Stage.PRE_ON_CREATE, Stage.RESTARTED)) {
        activities.addAll(activityLifecycleMonitor.getActivitiesInStage(s));
      }
      if (activities.isEmpty()) {
        throw new RuntimeException("No activities found. Did you forget to launch the activity "
            + "by calling getActivity() or startActivitySync or similar?");
      }
      // well at least there are some activities in the pipeline - lets see if they resume.

      long[] waitTimes =
          {10, 50, 100, 500, TimeUnit.SECONDS.toMillis(2), TimeUnit.SECONDS.toMillis(30)};

      for (int waitIdx = 0; waitIdx < waitTimes.length; waitIdx++) {
        Log.w(TAG, "No activity currently resumed - waiting: " + waitTimes[waitIdx]
            + "ms for one to appear.");
        uiController.loopMainThreadForAtLeast(waitTimes[waitIdx]);
        resumedActivities = activityLifecycleMonitor.getActivitiesInStage(Stage.RESUMED);
        if (!resumedActivities.isEmpty()) {
          return; // one of the pending activities has resumed
        }
      }
      throw new NoActivityResumedException("No activities in stage RESUMED. Did you forget to "
          + "launch the activity. (test.getActivity() or similar)?");
    }
  }

  private Root reduceRoots(List<Root> subpanels) {
    Root topSubpanel = subpanels.get(0);
    if (subpanels.size() >= 1) {
      for (Root subpanel : subpanels) {
        if (isDialog().matches(subpanel)) {
          return subpanel;
        }
        if (subpanel.getWindowLayoutParams().get().type
            > topSubpanel.getWindowLayoutParams().get().type) {
          topSubpanel = subpanel;
        }
      }
    }
    return topSubpanel;
  }
}
