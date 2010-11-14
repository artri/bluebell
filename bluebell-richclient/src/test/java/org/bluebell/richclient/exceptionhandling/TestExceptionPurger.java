/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell Rich Client.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bluebell.richclient.exceptionhandling;

import junit.framework.TestCase;

import org.bluebell.richclient.test.AbstractBbRichClientTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.richclient.application.ApplicationException;
import org.springframework.richclient.exceptionhandling.delegation.ExceptionPurger;

/**
 * Class that tests the correct behaviour of the configured {@link ExceptionPurger}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestExceptionPurger extends AbstractBbRichClientTests {

    /**
     * The exception purger to be tested.
     */
    @Autowired
    private ExceptionPurger defaultExceptionPurger;

    /**
     * Test the correct behaviour of dependency injection.
     */
    @Test
    public void testDependencyInjection() {

        TestCase.assertNotNull(this.defaultExceptionPurger);
    }

    /**
     * Tests the correct behaviour of <code>ExceptionPurger</code>.
     */
    @Test
    public void testExcludeRuntimeException() {

        Throwable purged = null;
        RuntimeException runtimeException = null;

        // A wrapped checked exception
        final ApplicationException applicationException = new ApplicationException("message");

        // Unwrapped RuntimeException
        runtimeException = new RuntimeException();
        purged = this.defaultExceptionPurger.purge(runtimeException);
        TestCase.assertTrue(runtimeException == purged);

        // Wrapped RuntimeException
        runtimeException = new RuntimeException(applicationException);
        purged = this.defaultExceptionPurger.purge(runtimeException);
        TestCase.assertTrue(applicationException == purged);
    }
}
