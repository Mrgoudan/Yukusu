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

import android.graphics.Rect;
import android.view.View;

/**
 * Calculates coordinate position for general locations.
 */
public enum GeneralLocation implements CoordinatesProvider {

  TOP_LEFT {
  @Override
    public float[] calculateCoordinates(View view) {
      return getCoordinates(view, Position.BEGIN, Position.BEGIN);
    }
  },
  TOP_CENTER {
  @Override
    public float[] calculateCoordinates(View view) {
      return getCoordinates(view, Position.BEGIN, Position.MIDDLE);
    }
  },
  TOP_RIGHT {
  @Override
    public float[] calculateCoordinates(View view) {
      return getCoordinates(view, Position.BEGIN, Position.END);
    }
  },
  CENTER_LEFT {
  @Override
    public float[] calculateCoordinates(View view) {
      return getCoordinates(view, Position.MIDDLE, Position.BEGIN);
    }
  },
  CENTER {
  @Override
    public float[] calculateCoordinates(View view) {
      return getCoordinates(view, Position.MIDDLE, Position.MIDDLE);
    }
  },
  CENTER_RIGHT {
  @Override
    public float[] calculateCoordinates(View view) {
      return getCoordinates(view, Position.MIDDLE, Position.END);
    }
  },
  BOTTOM_LEFT {
  @Override
    public float[] calculateCoordinates(View view) {
      return getCoordinates(view, Position.END, Position.BEGIN);
    }
  },
  BOTTOM_CENTER {
  @Override
    public float[] calculateCoordinates(View view) {
      return getCoordinates(view, Position.END, Position.MIDDLE);
    }
  },
  BOTTOM_RIGHT {
  @Override
    public float[] calculateCoordinates(View view) {
      return getCoordinates(view, Position.END, Position.END);
    }
  },
  VISIBLE_CENTER {
  @Override
    public float[] calculateCoordinates(View view) {
      return getCoordinatesOfVisiblePart(view, Position.MIDDLE, Position.MIDDLE);
    }
  },
  CENTER_LEFT_WITH_DRAWER_OFFSET {
    @Override
    public float[] calculateCoordinates(View view) {
      final int[] xy = new int[2];
      view.getLocationOnScreen(xy);
      float coordinate[] = new float[2];
      coordinate[0] = xy[0]+(view.getWidth()/2);
      coordinate[1] = xy[1]+(view.getHeight()/2);
      return coordinate;
    }
  },
  CENTER_LEFT_WITH_OFFSET {
    @Override
    public float[] calculateCoordinates(View view) {
      final int[] xy = new int[2];
      view.getLocationOnScreen(xy);
      float coordinate[] = new float[2];
      coordinate[0] = xy[0]+400;
      coordinate[1] = xy[1]+(view.getHeight()/2);
      return coordinate;
    }
  },
  CENTER_RIGHT_WITH_OFFSET {
    @Override
    public float[] calculateCoordinates(View view) {
      final int[] xy = new int[2];
      view.getLocationOnScreen(xy);
      float coordinate[] = new float[2];
      coordinate[0] = xy[0]+view.getWidth()-400;
      coordinate[1] = xy[1]+(view.getHeight()/2);
      return coordinate;
    }
  },
  TOP_CENTER_WITH_OFFSET {
    @Override
    public float[] calculateCoordinates(View view) {
      final int[] xy = new int[2];
      view.getLocationOnScreen(xy);
      float coordinate[] = new float[2];
      coordinate[0] = xy[0]+(view.getWidth()/2);
      coordinate[1] = xy[1]+400;
      return coordinate;
    }
  },
  BOTTOM_CENTER_WITH_OFFSET {
    @Override
    public float[] calculateCoordinates(View view) {
      final int[] xy = new int[2];
      view.getLocationOnScreen(xy);
      float coordinate[] = new float[2];
      coordinate[0] = xy[0]+(view.getWidth()/2);
      coordinate[1] = xy[1]+view.getHeight()-400;
      return coordinate;
    }
  };

  /**
   * Translates the given coordinates by the given distances. The distances are given in term
   * of the view's size -- 1.0 means to translate by an amount equivalent to the view's length.
   */
  static CoordinatesProvider translate(final CoordinatesProvider coords,
      final float dx, final float dy) {
    return new CoordinatesProvider() {
      @Override
      public float[] calculateCoordinates(View view) {
        float xy[] = coords.calculateCoordinates(view);
        xy[0] += dx * view.getWidth();
        xy[1] += dy * view.getHeight();
        return xy;
      }
    };
  }

  private static float[] getCoordinates(View view, Position vertical, Position horizontal) {
    final int[] xy = new int[2];
    view.getLocationOnScreen(xy);
    final float x = horizontal.getPosition(xy[0], view.getWidth());
    final float y = vertical.getPosition(xy[1], view.getHeight());
    float[] coordinates = {x, y};
    return coordinates;
  }

  private static float[] getCoordinatesOfVisiblePart(View view, Position vertical,
      Position horizontal) {
    final int[] xy = new int[2];
    view.getLocationOnScreen(xy);
    Rect visibleParts = new Rect();
    view.getGlobalVisibleRect(visibleParts);
    final float x = horizontal.getPosition(xy[0], visibleParts.width());
    final float y = vertical.getPosition(xy[1], visibleParts.height());
    float[] coordinates = {x, y};
    return coordinates;
  }

  private static enum Position {
    BEGIN {
    @Override
      public float getPosition(int viewPos, int viewLength) {
        return viewPos;
      }
    },
    MIDDLE {
    @Override
      public float getPosition(int viewPos, int viewLength) {
        // Midpoint between the leftmost and rightmost pixel (position viewLength - 1).
        return viewPos + (viewLength - 1) / 2.0f;
      }
    },
    END {
    @Override
      public float getPosition(int viewPos, int viewLength) {
        return viewPos + viewLength - 1;
      }
    };

    abstract float getPosition(int widgetPos, int widgetLength);
  }
}
