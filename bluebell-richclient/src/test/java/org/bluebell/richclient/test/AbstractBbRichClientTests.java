package org.bluebell.richclient.test;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationLauncher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.Assert;

/**
 * Base class for creating Spring Richclient tests in a headless environment.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration
public abstract class AbstractBbRichClientTests extends AbstractJUnit4SpringContextTests implements InitializingBean {

    /**
     * The global application instance.
     */
    @Autowired
    protected Application application;

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {

	// org.springframework.util.Assert is preferred (i.e.: over junit.framework.Assert)to avoid get binded to a
	// specific test framework
	Assert.notNull(this.applicationContext, "this.applicationContext");

	new ApplicationLauncher(this.applicationContext);
    }
}
