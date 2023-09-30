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

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.search.ActivityUtils;
import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;

/**
 * Enables rotation.
 */
public final class Rotate implements ViewAction {

  @Override
  @SuppressWarnings("unchecked")
  public Matcher<View> getConstraints() {
    Matcher<View> standardConstraint = isDisplayingAtLeast(90);
    return standardConstraint;
  }

  @Override
  public void perform(UiController uiController, View view) {
    int orientation = ActivityUtils.getCurrentActivity().getResources().getConfiguration().orientation;
    if(orientation == Configuration.ORIENTATION_PORTRAIT){
      ActivityUtils.getCurrentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    else if(orientation == Configuration.ORIENTATION_LANDSCAPE){
      ActivityUtils.getCurrentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    else{
      Log.d("Espresso", "orientation unknown, changing to landscape");
      ActivityUtils.getCurrentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    uiController.loopMainThreadUntilIdle();
  }

  @Override
  public String getDescription() {
    return "rotate";
  }
}
