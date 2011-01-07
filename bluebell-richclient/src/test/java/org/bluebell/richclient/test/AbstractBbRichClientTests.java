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

package org.bluebell.richclient.test;

import java.awt.Component;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.JTextField;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.SystemUtils;
import org.bluebell.richclient.application.config.BbApplicationConfig;
import org.bluebell.richclient.swing.util.SwingUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationLauncher;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.config.ApplicationLifecycleAdvisor;
import org.springframework.richclient.command.TargetableActionCommand;
import org.springframework.richclient.form.Form;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.Assert;

/**
 * Base class for creating Spring Richclient tests.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration
public abstract class AbstractBbRichClientTests extends AbstractJUnit4SpringContextTests implements InitializingBean {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBbRichClientTests.class);

    /**
     * Bluebell placeholder for storing user preferences.
     */
    private static final String USER_PREFERENCES = "richclient.userPreferences";

    /**
     * The global application instance.
     */
    @Autowired
    private Application application;

    /**
     * The application config, useful for debugging.
     */
    @Resource
    // @Autowire does not work for this factory bean
    protected Map<String, List<String[]>> applicationConfig;

    /**
     * The application launcher.
     */
    private static Boolean initialized = Boolean.FALSE;

    /**
     * Changes user preferences folder just for testing purposes.
     * 
     * @since 20101226 due to <a href="http://jirabluebell.b2b2000.com/browse/BLUE-40">BLUE-40</a>
     */
    @BeforeClass
    public static void changeUserPreferences() {

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        final String relativePath = "bluebell/" + simpleDateFormat.format(new Date());

        // (JAF), 20101226, last backslash is needed in order to create relative paths as expected
        final FileSystemResource javaIoTmpResource = new FileSystemResource(SystemUtils.getJavaIoTmpDir() + "/");
        final org.springframework.core.io.Resource bbTmpResource = javaIoTmpResource.createRelative(relativePath);

        try {
            FileUtils.forceDeleteOnExit(bbTmpResource.getFile());

            System.setProperty(AbstractBbRichClientTests.USER_PREFERENCES, bbTmpResource.getFile().getAbsolutePath());
        } catch (IOException e) {
            TestCase.fail("Unable to change user preferences folder");
        }
    }

    /**
     * Close the application after every test is executed.
     * <p>
     * Note it is not possible to call <code>Application#close</code> directly since it invokes <code>System#exit</code>
     * that causes exiting JVM outside normal test lifecycle.
     * <p>
     * So, this method just close application context, notify others about shutdown proccess and reset
     * <code>Application#SOLE_INSTANCE</code> in order to avoid the following message: <blockquote> The global rich
     * client application instance has not yet been initialized; it must be created and loaded first. </blockquote>
     */
    @AfterClass
    public static void closeApplication() {

        // final WindowManager windowManager = Application.instance().getWindowManager();
        final ApplicationContext applicationContext = Application.instance().getApplicationContext();
        final ApplicationLifecycleAdvisor lifecycleAdvisor = Application.instance().getLifecycleAdvisor();

        try {
            /*
             * (JAF), 20101219, closing windowManager makes "Maven Surefire Report Plugin" ignore these tests.
             */
            // if (windowManager.close()) {
            if (applicationContext instanceof ConfigurableApplicationContext) {
                ((ConfigurableApplicationContext) applicationContext).close();
            }
            lifecycleAdvisor.onShutdown();
            // }
        } finally {
            Application.load(null);
            // System.exit(0);
        }
    }

    /**
     * Launchs the application, just once.
     */
    @PostConstruct
    public void launch() {

        if (!AbstractBbRichClientTests.initialized) {
            new ApplicationLauncher(this.applicationContext);

            AbstractBbRichClientTests.initialized = Boolean.TRUE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        // org.springframework.util.Assert is preferred (i.e.: over junit.framework.Assert)to avoid get binded to a
        // specific test framework
        Assert.notNull(this.applicationContext, "this.applicationContext");
        Assert.notNull(this.applicationConfig, "this.applicationConfig");
    }

    /**
     * Prinsts debug information useful for detecting failures before any test execution.
     */
    @Before
    public final void beforeAnyTestCase() {

        MessageSource messageSource = (MessageSource) Application.services().getService(MessageSource.class);
        HierarchicalMessageSource childMessageSource = null;

        final StringBuffer messageSources = new StringBuffer();
        messageSources.append("Message sources chain: ");
        while (messageSource != null) {

            messageSources.append(ObjectUtils.identityToString(messageSource));
            messageSources.append(">>");

            if (messageSource == childMessageSource) {
                AbstractBbRichClientTests.LOGGER.error("OEOEOOEOEO");
                break;
            } else if (messageSource instanceof HierarchicalMessageSource) {
                childMessageSource = (HierarchicalMessageSource) messageSource;
                messageSource = childMessageSource.getParentMessageSource();
            }
        }

        if (AbstractBbRichClientTests.LOGGER.isDebugEnabled()) {
            AbstractBbRichClientTests.LOGGER.debug(ClassUtils.getShortClassName(this.getClass()));
            AbstractBbRichClientTests.LOGGER.debug(BbApplicationConfig.debugPrint(this.applicationConfig));
            AbstractBbRichClientTests.LOGGER.debug(messageSources.toString());
        }
    }

    /**
     * Gets the application.
     * 
     * @return the application
     */
    public Application getApplication() {

        return this.application;
    }

    /**
     * Simulate an user action changing the text value of a component.
     * 
     * @param form
     *            the form containing the text control.
     * @param property
     *            the property represented by the text control.
     * @param value
     *            the new text value to set.
     */
    protected void userAction(Form form, String property, final String value) {

        final JTextField textField = (JTextField) this.getComponentNamed(form, property);

        SwingUtils.runInEventDispatcherThread(new Runnable() {

            @Override
            public void run() {

                // textField.selectAll();
                textField.replaceSelection(value);
            }
        });
    }

    /**
     * Gets the active window.
     * 
     * @return the active window
     */
    protected final ApplicationWindow getActiveWindow() {

        return Application.instance().getActiveWindow();
    }

    /**
     * Gets the currently active page.
     * 
     * @return the active page.
     */
    protected final ApplicationPage getActivePage() {

        return this.getActiveWindow().getPage();
    }

    /**
     * Gets the component with the given name.
     * 
     * @param form
     *            the form containing the component.
     * @param name
     *            the name of the component.
     * @return the control, may be <code>null</code> if not found.
     */
    protected final Component getComponentNamed(Form form, String name) {

        return SwingUtils.getDescendantNamed(name, form.getControl());
    }

    /**
     * Gets the current shared command with a given id (if found) in the active window.
     * 
     * @param commandId
     *            the command id.
     * 
     * @return the current shared command instance.
     * 
     * @see #getSharedCommand(ApplicationWindow, String)
     */
    protected final TargetableActionCommand getSharedCommand(String commandId) {

        return this.getSharedCommand(this.getActiveWindow(), commandId);
    }

    /**
     * Gets the current shared command with a given id (if found).
     * 
     * @param applicationWindow
     *            the window.
     * @param commandId
     *            the command id.
     * 
     * @return the current shared command instance.
     * 
     * @see ApplicationWindow#getSharedCommands()
     */
    protected final TargetableActionCommand getSharedCommand(ApplicationWindow applicationWindow, String commandId) {

        Assert.notNull(applicationWindow, "applicationWindow");
        Assert.notNull(commandId, "commandId");

        TargetableActionCommand currentCommand;

        @SuppressWarnings("unchecked")
        final Iterator<TargetableActionCommand> itr = applicationWindow.getSharedCommands();
        while (itr.hasNext()) {

            currentCommand = itr.next();

            if (commandId.equals(currentCommand.getId())) {
                return currentCommand;
            }
        }

        return null;
    }
}
