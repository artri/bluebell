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
    protected ExceptionPurger exceptionPurger;

    /**
     * Test the correct behaviour of dependency injection.
     */
    @Test
    public void testDependencyInjection() {

	TestCase.assertNotNull(this.exceptionPurger);
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
	purged = this.exceptionPurger.purge(runtimeException);
	TestCase.assertTrue(runtimeException == purged);

	// Wrapped RuntimeException
	runtimeException = new RuntimeException(applicationException);
	purged = this.exceptionPurger.purge(runtimeException);
	TestCase.assertTrue(applicationException == purged);
    }
}
