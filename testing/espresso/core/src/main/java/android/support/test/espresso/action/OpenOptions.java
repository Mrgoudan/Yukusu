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

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.search.ActivityUtils;
import android.view.View;

import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;

/**
 * Enables clicking on views.
 */
public final class OpenOptions implements ViewAction {

  @Override
  @SuppressWarnings("unchecked")
  public Matcher<View> getConstraints() {
    Matcher<View> standardConstraint = isDisplayingAtLeast(90);
    return standardConstraint;
  }

  @Override
  public void perform(UiController uiController, View view) {
    ActivityUtils.getCurrentActivity().openOptionsMenu();
    uiController.loopMainThreadUntilIdle();
  }

  @Override
  public String getDescription() {
    return "open options";
  }
}
