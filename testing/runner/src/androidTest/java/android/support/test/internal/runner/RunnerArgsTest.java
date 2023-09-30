/*
 * Copyright (C) 2012 The Android Open Source Project
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
package android.support.test.internal.runner;

import android.os.Bundle;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link android.support.test.internal.runner.RunnerArgs}.
 */
@SmallTest
public class RunnerArgsTest {

    /**
     * Simple test for parsing test class name
     */
    @Test
    public void testFromBundle_singleClass() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_TEST_CLASS, "ClassName");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals(1, args.tests.size());
        assertEquals("ClassName", args.tests.get(0).testClassName);
        assertNull(args.tests.get(0).methodName);
    }

    /**
     * Test parsing bundle when multiple class names are provided.
     */
    @Test
    public void testFromBundle_multiClass() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_TEST_CLASS, "ClassName1,ClassName2");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals(2, args.tests.size());
        assertEquals("ClassName1", args.tests.get(0).testClassName);
        assertEquals("ClassName2", args.tests.get(1).testClassName);
        assertNull(args.tests.get(0).methodName);
        assertNull(args.tests.get(1).methodName);
    }

    /**
     * Test parsing bundle when class name and method name is provided.
     */
    @Test
    public void testFromBundle_method() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_TEST_CLASS, "ClassName1#method");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals("ClassName1", args.tests.get(0).testClassName);
        assertEquals("method", args.tests.get(0).methodName);
    }

    /**
     * Test {@link android.support.test.internal.runner.RunnerArgs.Builder#fromBundle(Bundle)} when
     * class name and method name is provided along with an additional class name.
     */
    @Test
    public void testFromBundle_classAndMethodCombo() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_TEST_CLASS, "ClassName1#method,ClassName2");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals(2, args.tests.size());
        assertEquals("ClassName1", args.tests.get(0).testClassName);
        assertEquals("method", args.tests.get(0).methodName);
        assertEquals("ClassName2", args.tests.get(1).testClassName);
        assertNull(args.tests.get(1).methodName);
    }

    /**
     * Simple test for parsing test class name
     */
    @Test
    public void testFromBundle_notSingleClass() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_NOT_TEST_CLASS, "ClassName");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals(1, args.notTests.size());
        assertEquals("ClassName", args.notTests.get(0).testClassName);
        assertNull(args.notTests.get(0).methodName);
    }

    /**
     * Test parsing bundle when multiple class names are provided.
     */
    @Test
    public void testFromBundle_notMultiClass() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_NOT_TEST_CLASS, "ClassName1,ClassName2");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals(2, args.notTests.size());
        assertEquals("ClassName1", args.notTests.get(0).testClassName);
        assertEquals("ClassName2", args.notTests.get(1).testClassName);
        assertNull(args.notTests.get(0).methodName);
        assertNull(args.notTests.get(1).methodName);
    }

    /**
     * Test parsing bundle when class name and method name is provided.
     */
    @Test
    public void testFromBundle_notMethod() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_NOT_TEST_CLASS, "ClassName1#method");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals("ClassName1", args.notTests.get(0).testClassName);
        assertEquals("method", args.notTests.get(0).methodName);
    }

    /**
     * Test {@link android.support.test.internal.runner.RunnerArgs.Builder#fromBundle(Bundle)} when
     * class name and method name is provided along with an additional class name.
     */
    @Test
    public void testFromBundle_notClassAndMethodCombo_different() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_NOT_TEST_CLASS, "ClassName1#method,ClassName2");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals(2, args.notTests.size());
        assertEquals("ClassName1", args.notTests.get(0).testClassName);
        assertEquals("method", args.notTests.get(0).methodName);
        assertEquals("ClassName2", args.notTests.get(1).testClassName);
        assertNull(args.notTests.get(1).methodName);
    }

    /**
     * Test {@link android.support.test.internal.runner.RunnerArgs.Builder#fromBundle(Bundle)} when
     * class name and method name is provided along with the same class name again.
     */
    @Test
    public void testFromBundle_notClassAndMethodCombo_same() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_NOT_TEST_CLASS, "ClassName1#method,ClassName1");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals(2, args.notTests.size());
        assertEquals("ClassName1", args.notTests.get(0).testClassName);
        assertEquals("method", args.notTests.get(0).methodName);
        assertEquals("ClassName1", args.notTests.get(1).testClassName);
        assertNull(args.notTests.get(1).methodName);
    }

    /**
     * Test parsing bundle when class name and not class name is provided.
     */
    @Test
    public void testFromBundle_classAndNotClass_different() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_TEST_CLASS, "ClassName1");
        b.putString(RunnerArgs.ARGUMENT_NOT_TEST_CLASS, "ClassName2#method");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals(1, args.tests.size());
        assertEquals(1, args.notTests.size());
        assertEquals("ClassName1", args.tests.get(0).testClassName);
        assertEquals("ClassName2", args.notTests.get(0).testClassName);
        assertEquals("method", args.notTests.get(0).methodName);
    }

    /**
     * Test parsing bundle when class name and not class name is provided.
     */
    @Test
    public void testFromBundle_classAndNotClass_same() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_TEST_CLASS, "ClassName1");
        b.putString(RunnerArgs.ARGUMENT_NOT_TEST_CLASS, "ClassName1#method");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals(1, args.tests.size());
        assertEquals(1, args.notTests.size());
        assertEquals("ClassName1", args.tests.get(0).testClassName);
        assertEquals("ClassName1", args.notTests.get(0).testClassName);
        assertEquals("method", args.notTests.get(0).methodName);
    }

    /**
     * Temp file used for testing
     */
    @Rule
    public TemporaryFolder mTmpFolder = new TemporaryFolder();

    /**
     * Test parsing bundle when multiple class and method names are provided within a test file
     */
    @Test
    public void testFromBundle_testFile() throws IOException {
        final File file = mTmpFolder.newFile("myTestFile.txt");
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write("ClassName3\n");
        out.write("ClassName4#method2\n");
        out.close();

        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_TEST_FILE, file.getPath());
        b.putString(RunnerArgs.ARGUMENT_TEST_CLASS, "ClassName1#method1,ClassName2");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals(4, args.tests.size());
        assertEquals("ClassName1", args.tests.get(0).testClassName);
        assertEquals("method1", args.tests.get(0).methodName);
        assertEquals("ClassName2", args.tests.get(1).testClassName);
        assertNull(args.tests.get(1).methodName);
        assertEquals("ClassName3", args.tests.get(2).testClassName);
        assertNull(args.tests.get(2).methodName);
        assertEquals("ClassName4", args.tests.get(3).testClassName);
        assertEquals("method2", args.tests.get(3).methodName);
    }

    /**
     * Test failure reading a testfile
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFromBundle_testFileFailure() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_TEST_FILE, "idontexist");
        new RunnerArgs.Builder().fromBundle(b).build();
    }

    /**
     * Test parsing bundle when test timeout is provided
     */
    @Test
    public void testFromBundle_timeout() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_TIMEOUT, "5000"); // 5 seconds
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertEquals(5000, args.testTimeout);
    }

    /**
     * Test parsing the boolean debug argument
     */
    @Test
    public void testFromBundle_debug() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_DEBUG, "true");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertTrue(args.debug);

        b.putString(RunnerArgs.ARGUMENT_DEBUG, "false");
        args = new RunnerArgs.Builder().fromBundle(b).build();
        assertFalse(args.debug);

        b.putString(RunnerArgs.ARGUMENT_DEBUG, "blargh");
        args = new RunnerArgs.Builder().fromBundle(b).build();
        assertFalse(args.debug);
    }

    /**
     * Test parsing the boolean logOnly argument
     */
    @Test
    public void testFromBundle_logOnly() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_LOG_ONLY, "true");
        RunnerArgs args = new RunnerArgs.Builder().fromBundle(b).build();
        assertTrue(args.logOnly);

        b.putString(RunnerArgs.ARGUMENT_LOG_ONLY, "false");
        args = new RunnerArgs.Builder().fromBundle(b).build();
        assertFalse(args.logOnly);

        b.putString(RunnerArgs.ARGUMENT_LOG_ONLY, "blargh");
        args = new RunnerArgs.Builder().fromBundle(b).build();
        assertFalse(args.logOnly);
    }

    @Test
    public void testFromBundle_allFieldsAreSupported() throws Exception {
        RunnerArgs defaultValues = new RunnerArgs.Builder().build();

        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_ANNOTATION, "annotation");
        b.putString(
                RunnerArgs.ARGUMENT_APP_LISTENER,
                "android.support.test.internal.runner.lifecycle.AppLifecycleListener");
        b.putString(RunnerArgs.ARGUMENT_COVERAGE, "true");
        b.putString(RunnerArgs.ARGUMENT_COVERAGE_PATH, "coveragePath");
        b.putString(RunnerArgs.ARGUMENT_DEBUG, "true");
        b.putString(RunnerArgs.ARGUMENT_DELAY_IN_MILLIS, "100");
        b.putString(RunnerArgs.ARGUMENT_DISABLE_ANALYTICS, "true");
        b.putString(RunnerArgs.ARGUMENT_LISTENER, "org.junit.runner.notification.RunListener");
        b.putString(RunnerArgs.ARGUMENT_LOG_ONLY, "true");
        b.putString(RunnerArgs.ARGUMENT_NOT_ANNOTATION, "notAnnotation");
        b.putString(RunnerArgs.ARGUMENT_SHARD_INDEX, "1");
        b.putString(RunnerArgs.ARGUMENT_SUITE_ASSIGNMENT, "true");
        b.putString(RunnerArgs.ARGUMENT_TEST_CLASS, "test.Class");
        b.putString(RunnerArgs.ARGUMENT_NOT_TEST_CLASS, "test.NotClass");
        b.putString(RunnerArgs.ARGUMENT_TEST_PACKAGE, "test.package");
        b.putString(RunnerArgs.ARGUMENT_NOT_TEST_PACKAGE, "test.notpackage");
        b.putString(RunnerArgs.ARGUMENT_TEST_SIZE, "medium");
        b.putString(RunnerArgs.ARGUMENT_TIMEOUT, "100");

        RunnerArgs fromBundle = new RunnerArgs.Builder().fromBundle(b).build();

        // Parsing of testFile require a real file on the disk, leave out this one.
        Collection<String> exceptions = Collections.singletonList("testFile");

        for (Field field : RunnerArgs.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || exceptions.contains(field.getName())) {
                continue;
            }

            assertNotEquals(
                    String.format("Field %s not set in fromBundle", field.getName()),
                    field.get(defaultValues),
                    field.get(fromBundle));
        }
    }

    /**
     * Test parsing bundle when an invalid test timeout is provided
     */
    @Test(expected = NumberFormatException.class)
    public void testFromBundle_timeoutWithWrongFormat() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_TIMEOUT, "not a long");
        new RunnerArgs.Builder().fromBundle(b);
    }

    /**
     * Test parsing bundle when a negative test timeout is provided
     */
    @Test(expected = NumberFormatException.class)
    public void testFromBundle_timeoutWithNegativeValue() {
        Bundle b = new Bundle();
        b.putString(RunnerArgs.ARGUMENT_TIMEOUT, "-500");
        new RunnerArgs.Builder().fromBundle(b);
    }
}
