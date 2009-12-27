/**
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
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
    private Application application;

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

    /**
     * Gets the application.
     * 
     * @return the application
     */
    public Application getApplication() {

        return this.application;
    }
}
