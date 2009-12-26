/**
 * 
 */
package org.bluebell.richclient.application;

import junit.framework.TestCase;

import org.bluebell.richclient.test.AbstractBbRichClientTests;
import org.junit.Test;

/**
 * Class that tests the correct behaviour of application instance context loading.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestRichclientContextLoad extends AbstractBbRichClientTests {

    /**
     * Test case that checks richclient application context is created and global application instance is injected.
     */
    @Test
    public void testDependencyInjection() {

        TestCase.assertNotNull(this.getApplication());
    }
}
