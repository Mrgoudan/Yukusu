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

package android.support.test.espresso;

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.test.espresso.base.RootViewPicker;
import android.support.test.espresso.base.ViewFinderImpl;
import android.support.test.espresso.matcher.RootMatchers;

import android.view.View;

import dagger.Module;
import dagger.Provides;

import org.hamcrest.Matcher;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Singleton;

/**
 * Adds the user interaction scope to the Espresso graph.
 */
//mf: added public
@Module
@Singleton
public class ViewInteractionModule {

  private final Matcher<View> viewMatcher;
  private final AtomicReference<Matcher<Root>> rootMatcher =
      new AtomicReference<Matcher<Root>>(RootMatchers.DEFAULT);

  //mf: added public
  public ViewInteractionModule(Matcher<View> viewMatcher) {
    this.viewMatcher = checkNotNull(viewMatcher);
  }

  @Provides
  AtomicReference<Matcher<Root>> provideRootMatcher() {
    return rootMatcher;
  }

  @Provides
  Matcher<View> provideViewMatcher() {
    return viewMatcher;
  }

  @Provides
  ViewFinder provideViewFinder(ViewFinderImpl impl) {
    return impl;
  }

  @Provides
  public View provideRootView(RootViewPicker rootViewPicker) {
    // RootsOracle acts as a provider, but returning Providers is illegal, so delegate.
    return rootViewPicker.get();
  }
}
