/**
 * 
 */
package org.bluebell.richclient.application;

import junit.framework.TestCase;

import org.bluebell.richclient.test.AbstractBbRichClientTests;
import org.springframework.richclient.application.Application;

/**
 * 
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestRichclientContextLoad extends AbstractBbRichClientTests {

    /**
     * The global application instance.
     */
    protected Application application;

    /**
     * Creates the test indicating that protected variables should be populated.
     */
    public TestRichclientContextLoad() {

	this.setPopulateProtectedVariables(Boolean.TRUE);
    }

    /**
     * Test case that checks rich client application context is created and global application instance is injected.
     */
    public void testDependencyInjection() {

	TestCase.assertNotNull(this.application);
    }
}
