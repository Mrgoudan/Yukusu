/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.test.runner.listener;


import android.os.Bundle;
import android.support.test.internal.runner.listener.InstrumentationResultPrinter;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.Description;

@SmallTest
public class InstrumentationResultPrinterTest {

    /**
     * Ensure the correct result code along with a stack trace was sent back to instrumentation
     */
    @Test
    public void testExecutionFlowDuringProcessCrash() throws Exception {
        final int[] resultCode = new int[1];
        final Bundle[] resultBundle = new Bundle[1];

        final InstrumentationResultPrinter intrResultPrinter = new InstrumentationResultPrinter() {
            @Override
            public void sendStatus(int code, Bundle bundle) {
                resultCode[0] = code;
                resultBundle[0] = bundle;
            }
        };

        // fake start test execution
        intrResultPrinter.testStarted(Description.EMPTY);
        Assert.assertEquals(InstrumentationResultPrinter.REPORT_VALUE_RESULT_START, resultCode[0]);
        Assert.assertFalse(
                resultBundle[0].containsKey(InstrumentationResultPrinter.REPORT_KEY_STACK));

        // define and report a RunTimeException
        Throwable e = new RuntimeException();
        intrResultPrinter.reportProcessCrash(e);

        // ensure the correct result code along with a stack trace was sent back to instrumentation
        Assert.assertEquals(
                InstrumentationResultPrinter.REPORT_VALUE_RESULT_FAILURE, resultCode[0]);
        Assert.assertTrue(
                resultBundle[0].containsKey(InstrumentationResultPrinter.REPORT_KEY_STACK));
    }
}
