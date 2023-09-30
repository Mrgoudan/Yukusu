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

import android.os.SystemClock;
import android.support.test.espresso.UiController;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Executes different swipe types to given positions.
 */
public enum Fling implements Flinger {

  /** Swipes quickly between the co-ordinates. */
  NORMAL {
    @Override
      public Status sendFling(UiController uiController, Flinger.Direction direction, float[] precision, Random random) {
        return sendLinearFling(uiController, direction, precision, random);
      }
  };

  private final static int FLING_MOTION_EVENT_NUM = 1;

  private static Status sendLinearFling(UiController uiController, Flinger.Direction direction, float[] precision, Random random) {
    checkNotNull(uiController);
    checkNotNull(direction);
    checkNotNull(precision);
    checkNotNull(random);

    //list of motion events for fling action
    List<MotionEvent> flingMotionEventList = new ArrayList<MotionEvent>();
    long downTime = SystemClock.uptimeMillis();
    float downX = -1;
    float downY = -1;
    for (int i = 0; i < FLING_MOTION_EVENT_NUM; ++i) {
      // generate a small random step
      int dX = -1;
      int dY = -1;
      if(direction==Flinger.Direction.DOWN){
        dX = 0;
        dY = 1;
        //dY = random.nextInt(10);
      }
      else if (direction==Flinger.Direction.UP){
        dX = 0;
        dY = -1;
        //dY = -1 * random.nextInt(10);
      }
      else if (direction==Flinger.Direction.LEFT){
        dX = -1;
        //dX = -1 * random.nextInt(10);
        dY = 0;
      }
      else {
        dX = 1;
        //dX = random.nextInt(10);
        dY = 0;
      }
      //creating event
      SparseArray<MotionEvent.PointerCoords> pointers = new SparseArray<MotionEvent.PointerCoords>();
      MotionEvent.PointerCoords coordinates = new MotionEvent.PointerCoords();
      coordinates.x = dX;
      coordinates.y = dY;
      coordinates.pressure = 0;
      coordinates.size = 0;
      pointers.append(0, coordinates);
      int pointerCount = pointers.size();
      int[] pointerIds = new int[pointerCount];
      MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[pointerCount];
      for (int j = 0; j < pointerCount; ++j) {
        pointerIds[j] = pointers.keyAt(j);
        pointerCoords[j] = pointers.valueAt(j);
      }
      MotionEvent ev = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
              MotionEvent.ACTION_MOVE, pointerCount, pointerIds, pointerCoords,
              0, precision[0], precision[1], 0, 0, InputDevice.SOURCE_TRACKBALL, 0);
      //first event
      if(i==0){
        downX = ev.getX();
        downY = ev.getY();
      }
      flingMotionEventList.add(ev);
    }


    try {
      for (int i = 0; i < flingMotionEventList.size(); ++i) {
        if (!MotionEvents.sendFling(uiController, flingMotionEventList.get(i))) {
          Log.e("Espresso", "Injection of move event as part of the fling failed. Sending cancel event.");
          MotionEvents.sendCancel(uiController, downTime, downX, downY);
          return Status.FAILURE;
        }
      }
    } finally {

    }

    //mf: check if wait is necessary
    //uiController.loopMainThreadForAtLeast(500);

    return Status.SUCCESS;
  }

}
