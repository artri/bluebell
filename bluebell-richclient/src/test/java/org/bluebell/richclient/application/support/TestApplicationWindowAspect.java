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

/**
 * 
 */
package org.bluebell.richclient.application.support;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.apache.commons.lang.math.RandomUtils;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.Test;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.ApplicationWindowFactory;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.command.ActionCommandExecutor;
import org.springframework.richclient.command.TargetableActionCommand;
import org.springframework.richclient.command.support.GlobalCommandIds;
import org.springframework.richclient.util.Assert;

/**
 * Tests the correct behaviour of {@link org.bluebell.richclient.application.support.ApplicationWindowAspect}.
 * <p>
 * According to issue number <a href="http://jirabluebell.b2b2000.com/browse/BLUE-30">30</a> shared command executors
 * are not updated conveniently after changing pages. This is due to the following facts:
 * <ol>
 * <li><code>ApplicationPage#setActiveComponent</code> is not invoked in some cases.
 * <li>Sometimes, even if invoked, no action is done (tipically <code>#fireFocusXXX</code> like methods). These actions
 * are needed in order to register newer commands executors
 * {@link org.springframework.richclient.application.support.SharedCommandTargeter}.
 * </ol>
 * 
 * @see org.springframework.richclient.application.support.SharedCommandTargeter
 * @see ApplicationConfigAspect
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestApplicationWindowAspect extends AbstractBbSamplesTests {

    /**
     * The initial page descriptor.
     */
    @Resource
    private PageDescriptor initialPageDescriptor;

    /**
     * {@inheritDoc}
     */
    @Test
    public void testDependencyInjection() {

        super.testDependencyInjection();

        TestCase.assertNotNull("initialPageDescriptor", this.initialPageDescriptor);
    }

    /**
     * Tests the correct behaviour of shared commands update process after changing between windos, pages and page
     * components.
     * 
     * @see #doTestSharedCommandsUpdate(ApplicationWindow, ApplicationWindow, ApplicationWindow, ApplicationWindow)
     */
    @Test
    public void testSharedCommandsUpdate() {

        final int noIter = 100;

        final ApplicationWindowFactory applicationWindowFactory = (ApplicationWindowFactory) //
        Application.services().getService(ApplicationWindowFactory.class);

        final ApplicationWindow window1 = this.getActiveWindow();
        final ApplicationWindow window2 = applicationWindowFactory.createApplicationWindow();

        Application.instance().getWindowManager().add(window2);

        // Tests a well known sequence
        this.doTestSharedCommandsUpdate(window1, window1, window1, window1);
        this.doTestSharedCommandsUpdate(window2, window2, window2, window2);
        this.doTestSharedCommandsUpdate(window1, window1, window1, window1);
        this.doTestSharedCommandsUpdate(window2, window2, window2, window2);

        // Tests sequences randomly
        for (int i = 0; i < noIter; ++i) {
            this.doTestSharedCommandsUpdate(//
                    RandomUtils.nextBoolean() ? window1 : window2, //
                    RandomUtils.nextBoolean() ? window1 : window2, //
                    RandomUtils.nextBoolean() ? window1 : window2, //
                    RandomUtils.nextBoolean() ? window1 : window2);
        }
    }

    /**
     * 
     * Tests the correct behaviour of shared commands update process after changing between windows, pages and page
     * components.
     * <p>
     * Proceeds according to the following steps:
     * <ol>
     * <li>Tests showing initial page in first window
     * <li>Tests showing person page in second window
     * <li>Tests showing initial page again in third window
     * <li>Tests showing person page again in fourth window
     * </ol>
     * <p>
     * <b>Note</b> windows may be the same.
     * 
     * @param window1
     *            the first application window to be tested.
     * @param window2
     *            the second application window to be tested.
     * @param window3
     *            the third application window to be tested.
     * @param window4
     *            the fourth application window to be tested.
     * 
     * @see #doShowPageInWindow(ApplicationWindow, PageDescriptor)
     */
    protected final void doTestSharedCommandsUpdate(ApplicationWindow window1, ApplicationWindow window2,
            ApplicationWindow window3, ApplicationWindow window4) {

        Assert.notNull(window1, "window1");
        Assert.notNull(window2, "window2");
        Assert.notNull(window3, "window3");
        Assert.notNull(window4, "window4");

        this.doShowPageInWindow(window1, this.initialPageDescriptor);
        this.doShowPageInWindow(window2, this.getPersonPageDescriptor());
        this.doShowPageInWindow(window3, this.initialPageDescriptor);
        this.doShowPageInWindow(window4, this.getPersonPageDescriptor());
    }

    /**
     * Show a page in a given window. Follows these steps:
     * <ol>
     * <li>Activate target window
     * <li>Initialize test variables
     * <li>Show target page and test it works as expected
     * <li>Retrieve active component
     * <li>Ensure shared properties command executor is the expected one
     * </ol>
     * 
     * @param applicationWindow
     *            the target application window.
     * @param pageDescriptor
     *            the target page descriptor.
     */
    private void doShowPageInWindow(ApplicationWindow applicationWindow, PageDescriptor pageDescriptor) {

        final PageComponent activeComponent;
        final ActionCommandExecutor expectedCommandExecutor;

        // 1. Activate target window
        Application.instance().getWindowManager().setActiveWindow(applicationWindow);

        // 2. Initialize test variables (internally shows the given page descriptor) and test it works as expected
        this.initializeVariables(applicationWindow, pageDescriptor);
        TestCase.assertEquals(pageDescriptor.getId(), applicationWindow.getPage().getId());

        // 3. Retrieve active component
        activeComponent = applicationWindow.getPage().getActiveComponent();
        TestCase.assertNotNull("activeComponent", activeComponent);

        // 4. Ensure shared properties command executor is the expected one
        if (this.getMasterView() != null) {
            expectedCommandExecutor = this.getMasterView().getBackingForm().getNewFormObjectCommand();
        } else {
            expectedCommandExecutor = null;
        }

        TestCase.assertSame(expectedCommandExecutor, this.getPropertiesCommandExecutor(applicationWindow));
    }

    /**
     * Returns the current implementation of the shared command with id {@link GlobalCommandIds#PROPERTIES}.
     * 
     * @param applicationWindow
     *            the target application window.
     * 
     * @return the associated command executor.
     * 
     * @see #getSharedCommand(String)
     */
    private ActionCommandExecutor getPropertiesCommandExecutor(ApplicationWindow applicationWindow) {

        final TargetableActionCommand sharedCommand = this.getSharedCommand(//
                applicationWindow, GlobalCommandIds.PROPERTIES);

        final ActionCommandExecutor executor = (ActionCommandExecutor) PropertyAccessorFactory.forDirectFieldAccess(
                sharedCommand).getPropertyValue("commandExecutor");

        return executor;
    }
}
