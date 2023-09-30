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

import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.util.HumanReadables;
import android.view.View;
import android.view.ViewConfiguration;

import org.hamcrest.Matcher;

import java.util.Random;

import static org.hamcrest.Matchers.any;

/**
 * Enables fling across a view.
 */
public final class GeneralFlingAction implements ViewAction {

  /** Maximum number of times to attempt sending a fling action. */
  private static final int MAX_TRIES = 3;

  private final Flinger flinger;
  private final Flinger.Direction direction;
  private final PrecisionDescriber precisionDescriber;
  private final Random random;

  public GeneralFlingAction(Flinger flinger, Flinger.Direction direction, PrecisionDescriber precisionDescriber, Random random) {
    this.flinger = flinger;
    this.direction = direction;
    this.precisionDescriber = precisionDescriber;
    this.random = random;
  }

  @Override
  public Matcher<View> getConstraints() {
    return any(View.class);
  }

  @Override
  public void perform(UiController uiController, View view) {
    float[] precision = precisionDescriber.describePrecision();

    Flinger.Status status = Flinger.Status.FAILURE;

    for (int tries = 0; tries < MAX_TRIES && status != Flinger.Status.SUCCESS; tries++) {
      try {
        status = flinger.sendFling(uiController, direction, precision, random);
      } catch (RuntimeException re) {
        throw new PerformException.Builder()
            .withActionDescription(this.getDescription())
            .withViewDescription(HumanReadables.describe(view))
            .withCause(re)
            .build();
      }

      //mf: consider to change
      int duration = ViewConfiguration.getPressedStateDuration();
      // ensures that all work enqueued to process the fling has been run.
      if (duration > 0) {
        uiController.loopMainThreadForAtLeast(duration);
      }
    }

    if (status == Flinger.Status.FAILURE) {
      throw new PerformException.Builder()
          .withActionDescription(getDescription())
          .withViewDescription(HumanReadables.describe(view))
          .withCause(new RuntimeException(String.format("Couldn't fling in %s direction. Tried %s times", direction, MAX_TRIES)))
          .build();
    }
  }

  @Override
  public String getDescription() {
    return flinger.toString().toLowerCase() + " fling";
  }
}
