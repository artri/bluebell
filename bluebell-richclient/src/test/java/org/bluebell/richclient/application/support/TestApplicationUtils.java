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

import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.Test;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.ApplicationWindowFactory;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageComponentListener;
import org.springframework.richclient.application.PageDescriptor;

/**
 * Tests the correct behaviour of {@link ApplicationUtils}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestApplicationUtils extends AbstractBbSamplesTests {

    /**
     * 
     */
    private final PageComponentListener componentFocusGainedCounter = new ComponentFocusGainedCounter();

    /**
     * 
     */
    private int numberOfFocusGained = 0;

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
     * Test <code>forceFocusGained</code> fail conditions. Validations should be done!
     */
    @Test
    public void testForceFocusGainedFailConditions() {

        this.initializeVariables(this.getActiveWindow(), this.getPersonPageDescriptor());
        TestCase.assertEquals(this.getPersonPageDescriptor().getId(), this.getActivePage().getId());

        // Application page must be not null
        try {
            ApplicationUtils.forceFocusGained(null);
            TestCase.fail("ApplicationUtils.forceFocusGained(null);");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        // Application page must not be null even if page component is set
        try {
            ApplicationUtils.forceFocusGained(null, this.getMasterView());
            TestCase.fail("ApplicationUtils.forceFocusGained(null, this.getMasterView());");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        // Page component must be included into application page
        try {
            ApplicationUtils.forceFocusGained(this.getActivePage(), this.getInitialView());
            TestCase.fail("ApplicationUtils.forceFocusGained(this.getActivePage(), this.getInitialView());");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }
    }

    /**
     * Test <code>forceFocusGained</code> special case of null page descriptor.
     */
    @Test
    public void testForceFocusGainedWhetherPageComponentIsNull() {

        this.initializeVariables(this.getActiveWindow(), this.getPersonPageDescriptor());

        try {
            ApplicationUtils.forceFocusGained(this.getActivePage(), null);

            TestCase.assertTrue("ApplicationUtils.forceFocusGained(this.getActivePage(), null);", Boolean.TRUE);
        } catch (Exception e) {
            TestCase.fail(e.getMessage());
        }
    }

    /**
     * Tests the correct behaviour of
     * {@link ApplicationUtils#forceFocusGained(org.springframework.richclient.application.ApplicationPage)} within 2
     * windows and 2 diferent pages.
     */
    @Test
    public void testForceFocusGained() {

        final ApplicationWindowFactory applicationWindowFactory = (ApplicationWindowFactory) //
        Application.services().getService(ApplicationWindowFactory.class);

        final ApplicationWindow window1 = this.getActiveWindow();
        final ApplicationWindow window2 = applicationWindowFactory.createApplicationWindow();

        // Sequence 0
        this.initializeVariables(window1, this.initialPageDescriptor);
        this.initializeVariables(window1, this.getPersonPageDescriptor());
        this.initializeVariables(window2, this.initialPageDescriptor);
        this.initializeVariables(window2, this.getPersonPageDescriptor());

        // Sequence 1
        this.initializeVariables(window1, this.initialPageDescriptor);
        this.doTestForceFocusGained(window1, this.initialPageDescriptor);
        this.doTestForceFocusGained(window1, this.initialPageDescriptor, this.getInitialView());

        // Sequence 2
        this.initializeVariables(window1, this.getPersonPageDescriptor());
        this.doTestForceFocusGained(window1, this.getPersonPageDescriptor());
        this.doTestForceFocusGained(window1, this.getPersonPageDescriptor(), this.getChildView());

        // Sequence 3
        this.initializeVariables(window1, this.initialPageDescriptor);
        this.doTestForceFocusGained(window2, this.initialPageDescriptor);
        this.doTestForceFocusGained(window2, this.initialPageDescriptor, this.getInitialView());

        // Sequence 4
        this.initializeVariables(window1, this.getPersonPageDescriptor());
        this.doTestForceFocusGained(window2, this.getPersonPageDescriptor());
        this.doTestForceFocusGained(window2, this.getPersonPageDescriptor(), this.getChildView());
    }

    /**
     * Tests the correct behaviour of
     * {@link ApplicationUtils#resetActiveComponent(org.springframework.richclient.application.ApplicationPage)}.
     */
    @Test
    public void testResetActiveComponent() {

        // Fail
        try {
            ApplicationUtils.resetActiveComponent(null);
            TestCase.fail("ApplicationUtils.resetActiveComponent(null);");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        // Success
        TestCase.assertNotNull(this.getActivePage().getActiveComponent());
        ApplicationUtils.resetActiveComponent(this.getActivePage());
        TestCase.assertNull(this.getActivePage().getActiveComponent());

    }

    /**
     * Force a focus gained event over a given application window when a page is active forcing the current active
     * component to be activated again.
     * 
     * @param applicationWindow
     *            the target window.
     * @param pageDescriptor
     *            the target page descriptor.
     * 
     * @see #doTestForceFocusGained(ApplicationWindow, PageDescriptor, PageComponent)
     */
    private void doTestForceFocusGained(ApplicationWindow applicationWindow, PageDescriptor pageDescriptor) {

        applicationWindow.showPage(pageDescriptor);

        this.doTestForceFocusGained(//
                applicationWindow, pageDescriptor, applicationWindow.getPage().getActiveComponent());
    }

    /**
     * Force a focus gained event over a given application window when a page is active forcing a new page component to
     * be active.
     * <p>
     * Follow these steps:
     * <ol>
     * <li>Activate target window.
     * <li>Initialize test variables.
     * <li>Register a counter within the target page.
     * <li>Force focus gained and check a new active component is set and an event raised.
     * </ol>
     * 
     * @param applicationWindow
     *            the target window.
     * @param pageDescriptor
     *            the target page descriptor.
     * @param componentToActivate
     *            the target component to activate.
     */
    private void doTestForceFocusGained(ApplicationWindow applicationWindow, PageDescriptor pageDescriptor,
            PageComponent componentToActivate) {

        final int oldNumberOfFocusGained;
        final int newNumberOfFocusGained;
        final PageComponent oldActiveComponent;
        final PageComponent newActiveComponent;

        // 1. Activate target window
        Application.instance().getWindowManager().setActiveWindow(applicationWindow);

        // 2. Initialize test variables (internally shows the given page descriptor) and test it works as expected
        this.initializeVariables(applicationWindow, pageDescriptor);

        /*
         * PRE-CONDITIONS: Active application window, page descriptor and active component are expected
         */
        TestCase.assertSame(applicationWindow, this.getActiveWindow());
        TestCase.assertEquals(pageDescriptor.getId(), applicationWindow.getPage().getId());
        TestCase.assertNotNull("this.getActivePage().getActiveComponent()", this.getActivePage().getActiveComponent());

        // 3. Register the counter within the active page
        this.getActivePage().addPageComponentListener(this.componentFocusGainedCounter);

        // (Register old number of focus gained events and old active component)
        oldNumberOfFocusGained = this.numberOfFocusGained;
        oldActiveComponent = this.getActivePage().getActiveComponent();

        // 4. Proceed
        ApplicationUtils.forceFocusGained(this.getActivePage(), componentToActivate);

        // (Register new number of focus gained events and new active component)
        newNumberOfFocusGained = this.numberOfFocusGained;
        newActiveComponent = this.getActivePage().getActiveComponent();

        /*
         * POST-CONDITIONS: Number of focus gained events has increased and new active component is expected
         */
        TestCase.assertTrue("newNumberOfFocusGained > oldNumberOfFocusGained",
                newNumberOfFocusGained > oldNumberOfFocusGained);
        TestCase.assertNotNull("oldActiveComponent", oldActiveComponent);
        TestCase.assertSame(componentToActivate, newActiveComponent);
    }

    /**
     * Counts the number of component focus gained events.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private final class ComponentFocusGainedCounter implements PageComponentListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void componentFocusGained(PageComponent component) {

            ++TestApplicationUtils.this.numberOfFocusGained;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void componentOpened(PageComponent component) {

            // This method does nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void componentFocusLost(PageComponent component) {

            // This method does nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void componentClosed(PageComponent component) {

            // This method does nothing
        }
    }
}
