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

package android.support.test.espresso.matcher;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.matcher.PreferenceMatchers.isEnabled;
import static android.support.test.espresso.matcher.PreferenceMatchers.withKey;
import static android.support.test.espresso.matcher.PreferenceMatchers.withSummary;
import static android.support.test.espresso.matcher.PreferenceMatchers.withSummaryText;
import static android.support.test.espresso.matcher.PreferenceMatchers.withTitle;
import static android.support.test.espresso.matcher.PreferenceMatchers.withTitleText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import android.support.test.runner.AndroidJUnit4;
import android.support.test.testapp.test.R;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for preference matchers.
 */
@SmallTest
@RunWith(AndroidJUnit4.class)
public class PreferenceMatchersTest {

  @Test
  public void withSummaryTest() {
    CheckBoxPreference pref = new CheckBoxPreference(getInstrumentation().getContext());
    pref.setSummary(R.string.something);
    assertThat(pref, withSummary(R.string.something));
    assertThat(pref, not(withSummary(R.string.other_string)));
    assertThat(pref, withSummaryText("Hello World"));
    assertThat(pref, not(withSummaryText(("Hello Mars"))));
    assertThat(pref, withSummaryText(is("Hello World")));
  }

  @Test
  public void withTitleTest() {
    CheckBoxPreference pref = new CheckBoxPreference(getInstrumentation().getContext());
    assertThat(pref, not(withTitle(R.string.other_string)));
    assertThat(pref, not(withTitleText("not null")));
    pref.setTitle(R.string.other_string);
    assertThat(pref, withTitle(R.string.other_string));
    assertThat(pref, not(withTitle(R.string.something)));
    assertThat(pref, withTitleText("Goodbye!!"));
    assertThat(pref, not(withTitleText(("Hello Mars"))));
    assertThat(pref, withTitleText(is("Goodbye!!")));
  }

  @Test
  public void isEnabledTest() {
    CheckBoxPreference pref = new CheckBoxPreference(getInstrumentation().getContext());
    pref.setEnabled(true);
    assertThat(pref, isEnabled());
    pref.setEnabled(false);
    assertThat(pref, not(isEnabled()));
    EditTextPreference pref2 = new EditTextPreference(getInstrumentation().getContext());
    pref2.setEnabled(true);
    assertThat(pref2, isEnabled());
    pref2.setEnabled(false);
    assertThat(pref2, not(isEnabled()));
  }

  @Test
  public void withKeyTest() {
    CheckBoxPreference pref = new CheckBoxPreference(getInstrumentation().getContext());
    pref.setKey("foo");
    assertThat(pref, withKey("foo"));
    assertThat(pref, not(withKey("bar")));
    assertThat(pref, withKey(is("foo")));
  }
}