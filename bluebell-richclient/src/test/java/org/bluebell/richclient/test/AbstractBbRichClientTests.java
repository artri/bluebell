package org.bluebell.richclient.test;

import org.bluebell.richclient.application.RcpMain;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.richclient.application.ApplicationLauncher;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Base class for creating spring rich client tests in a headless environment.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public abstract class AbstractBbRichClientTests extends AbstractDependencyInjectionSpringContextTests {

    /**
     * {@inheritDoc}
     */
    @Override
    protected ConfigurableApplicationContext createApplicationContext(String[] locations) {

	final ConfigurableApplicationContext applicationContext = super.createApplicationContext(locations);

	new ApplicationLauncher(applicationContext);

	return applicationContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getConfigLocations() {

	return new String[] { RcpMain.MAIN_APPLICATION_CONTEXT_PATH, RcpMain.DEFAULT_APPLICATION_CONTEXT_PATH };
    }
}
