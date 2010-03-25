/*
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

/**
 * 
 */
package org.bluebell.richclient.application.support;

import java.util.List;

import junit.framework.TestCase;

import org.bluebell.richclient.application.ApplicationWindowException;
import org.bluebell.richclient.application.support.TabbedApplicationWindowFactory.TabbedApplicationWindow;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.WindowManager;
import org.springframework.richclient.application.support.AbstractPageDescriptor;
import org.springframework.richclient.application.support.EmptyPageDescriptor;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.CommandManager;
import org.springframework.richclient.command.support.NewWindowCommand;
import org.springframework.test.context.ContextConfiguration;

/**
 * Tests the correct behaviour of {@link TabbedApplicationWindowFactory}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration
public class TestTabbedApplicationWindowFactory extends AbstractBbSamplesTests {

    /**
     * A page descriptor useful for testing.
     */
    @Autowired
    private PageDescriptor pageDescriptor1;

    /**
     * A page descriptor useful for testing.
     */
    @Autowired
    private PageDescriptor pageDescriptor2;

    /**
     * A page descriptor useful for testing.
     */
    @Autowired
    private PageDescriptor pageDescriptor3;

    /**
     * The new window command.
     */
    private ActionCommand newWindowCommand;

    /**
     * {@inheritDoc}
     */
    @Test
    public void testDependencyInjection() {

        super.testDependencyInjection();

        TestCase.assertNotNull("pageDescriptor1", this.pageDescriptor1);
        TestCase.assertNotNull("pageDescriptor2", this.pageDescriptor2);
        TestCase.assertNotNull("pageDescriptor3", this.pageDescriptor3);
    }

    /**
     * Tests the correct behaviour of a single page insertion and removal.
     */
    @Test
    public void testSinglePage() {

        final TabbedApplicationWindow window = (TabbedApplicationWindow) this.getActiveWindow();
        ApplicationPage activePage = null;

        // Show pd1 and ensure pd1 is the active page
        activePage = this.showPage(this.pageDescriptor1);
        TestCase.assertEquals(this.pageDescriptor1.getId(), activePage.getId());

        // Show pd1 again and ensure it's the same instance
        TestCase.assertTrue(activePage == this.showPage(this.pageDescriptor1));

        // Remove pd1 and ensure the empty page becomes the active one
        window.removePage(activePage);
        activePage = window.getPage();
        TestCase.assertEquals(TabbedApplicationWindow.EMPTY_PAGE_DESCRIPTOR_ID, activePage.getId());
    }

    /**
     * Tests the correct behaviour of multiple pages insertion and removal.
     */
    @Test
    public void testMultiplePages() {

        final TabbedApplicationWindow window = (TabbedApplicationWindow) this.getActiveWindow();
        ApplicationPage activePage = null;

        final ApplicationPage emptyPage = this.getActiveWindow().getPage();

        // Show pd1 and ensure pd1 is the active page
        activePage = this.showPage(this.pageDescriptor1);
        final ApplicationPage page1 = activePage;
        TestCase.assertEquals(this.pageDescriptor1.getId(), activePage.getId());

        // Show pd2 and ensure pd2 is the active page
        activePage = this.showPage(this.pageDescriptor2);
        final ApplicationPage page2 = activePage;
        TestCase.assertEquals(this.pageDescriptor2.getId(), activePage.getId());

        // Show pd1 again and ensure it's the same instance
        TestCase.assertTrue(page1 == this.showPage(this.pageDescriptor1));

        // Show pd2 again and ensure it's the same instance
        TestCase.assertTrue(page2 == this.showPage(this.pageDescriptor2));

        // Remove a hidden (non active) page
        window.removePage(page1);
        activePage = window.getPage();
        TestCase.assertEquals(page2.getId(), activePage.getId());

        // Remove the active page
        window.removePage(page2);
        activePage = window.getPage();
        TestCase.assertEquals(TabbedApplicationWindow.EMPTY_PAGE_DESCRIPTOR_ID, activePage.getId());

        // Ensure there is only one empty page instance per window
        TestCase.assertTrue(emptyPage == activePage);
    }

    /**
     * Tests the correct behaviour of opening a new window when the active page is empty.
     */
    @Test
    public void testOpenNewWindowWithEmptyActivePage() {

        final String expectedPageId = TabbedApplicationWindow.EMPTY_PAGE_DESCRIPTOR_ID;

        this.doTestOpenNewWindow(expectedPageId);
    }

    /**
     * Tests the correct behaviour of opening a new window when there is a single page.
     */
    @Test
    public void testOpenNewWindowWithSinglePage() {

        final String expectedPageId = this.pageDescriptor1.getId();

        this.showPage(this.pageDescriptor1);

        this.doTestOpenNewWindow(expectedPageId);
    }

    /**
     * Tests the correct behaviour of opening a new window when there are multiple pages.
     */
    @Test
    public void testOpenNewWindowWithMultiplePages() {

        final String expectedPageId = this.pageDescriptor3.getId();

        this.showPage(this.pageDescriptor1);
        this.showPage(this.pageDescriptor2);
        this.showPage(this.pageDescriptor3);

        this.doTestOpenNewWindow(expectedPageId);
    }

    /**
     * Tests an error is raised while trying to add a null page.
     */
    @Test
    public void testAddNullPage() {

        final TabbedApplicationWindow window = (TabbedApplicationWindow) this.getActiveWindow();

        try {
            window.addPage(null);
            TestCase.fail("Adding null pages should raise an exception");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }
    }

    /**
     * Tests an error is raised while trying to add a duplicated page.
     */
    @Test
    public void testAddDuplicatedPage() {

        final TabbedApplicationWindow window = (TabbedApplicationWindow) this.getActiveWindow();

        window.showPage(this.pageDescriptor1);

        try {
            window.addPage(window.getPage());
            TestCase.fail("Adding duplicated pages should raise an exception");
        } catch (ApplicationWindowException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }
    }

    /**
     * Tests everything works ok in spite of adding a page with an unknown descriptor.
     */
    @Test
    public void testAddUnknownPageDescriptor() {

        final TabbedApplicationWindow window = (TabbedApplicationWindow) this.getActiveWindow();

        // At this point the tab panel is empty
        TestCase.assertTrue(window.getPages().isEmpty());

        // Add an unknow page
        final AbstractPageDescriptor pageDescriptor = new EmptyPageDescriptor();
        pageDescriptor.setId(TabbedApplicationWindow.EMPTY_PAGE_DESCRIPTOR_ID);
        
        window.showPage(pageDescriptor);
        TestCase.assertTrue("Page added successfully", Boolean.TRUE);
    }

    /**
     * Tests everything works ok after adding a page with a known descriptor.
     */
    @Test
    public void testAddKnownPageDescriptor() {

        final TabbedApplicationWindow window = (TabbedApplicationWindow) this.getActiveWindow();

        // At this point the tab panel is empty
        TestCase.assertTrue(window.getPages().isEmpty());

        // Add a known page
        window.showPage(this.getPersonPageDescriptor());
        TestCase.assertTrue("Page added successfully", Boolean.TRUE);
    }

    /**
     * Tests an error is raised while trying to remove a null page.
     */
    @Test
    public void testRemoveNullPage() {

        final TabbedApplicationWindow window = (TabbedApplicationWindow) this.getActiveWindow();

        try {
            window.removePage(null);
            TestCase.fail("Removing null pages should raise an exception");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }
    }

    /**
     * Tests an error is raised while trying to remove an isolated page.
     */
    @Test
    public void testRemoveIsolatedPage() {

        final TabbedApplicationWindow window = (TabbedApplicationWindow) this.getActiveWindow();

        // Remember a page
        window.showPage(this.pageDescriptor1);
        final ApplicationPage page = window.getPage();
        this.removePages();

        try {
            window.removePage(page);
            TestCase.fail("Removing isolated pages should raise an exception");
        } catch (ApplicationWindowException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }
    }

    /**
     * Tests everything works ok when removing a page with an unknown descriptor.
     */
    @Test
    public void testRemoveUnknownPageDescriptor() {

        final TabbedApplicationWindow window = (TabbedApplicationWindow) this.getActiveWindow();

        // At this point the tab panel is empty
        TestCase.assertTrue(window.getPages().isEmpty());

        // Add an unknow page
        final AbstractPageDescriptor pageDescriptor = new EmptyPageDescriptor();
        pageDescriptor.setId(TabbedApplicationWindow.EMPTY_PAGE_DESCRIPTOR_ID);
        
        window.showPage(pageDescriptor);

        // Remove the page with unknown descriptor
        final ApplicationPage page = window.getPage();
        window.removePage(page);
        TestCase.assertTrue("Page removed successfully", Boolean.TRUE);
    }

    /**
     * Tests everything works ok after removing a page with a known descriptor.
     */
    @Test
    public void testRemoveKnownPageDescriptor() {

        final TabbedApplicationWindow window = (TabbedApplicationWindow) this.getActiveWindow();

        // At this point the tab panel is empty
        TestCase.assertTrue(window.getPages().isEmpty());

        // Add a known page
        window.showPage(this.getPersonPageDescriptor());

        // Remove the page with known descriptor
        final ApplicationPage page = window.getPage();
        window.removePage(page);
        TestCase.assertTrue("Page removed successfully", Boolean.TRUE);
    }

    /**
     * Method invoked at startup.
     * <p/>
     * Initializes test cases.
     */
    @Before
    public void startup() {

        this.initializeApplicationAndWait();
        this.removePages();

        final CommandManager commandMgr = this.getActiveWindow().getCommandManager();
        this.newWindowCommand = (NewWindowCommand) commandMgr.getCommand("newWindowCommand", NewWindowCommand.class);
    }

    /**
     * Do <code>testOpenNewWindow</code> like tests.
     * <p>
     * Ensures the current window active page is the expected one.
     * 
     * @param expectedPageId
     *            the current window expected active page id.
     */
    private void doTestOpenNewWindow(String expectedPageId) {

        final WindowManager windowManager = Application.instance().getWindowManager();

        // Validate expectation
        final ApplicationWindow window1 = windowManager.getActiveWindow();
        final ApplicationPage page1 = window1.getPage();
        TestCase.assertEquals(expectedPageId, window1.getPage().getId());

        // Open a new window
        TestTabbedApplicationWindowFactory.this.newWindowCommand.execute();

        // Validate expectation after opening a new window
        final ApplicationWindow window2 = windowManager.getActiveWindow();
        final ApplicationPage page2 = window2.getPage();

        // Ensure (window1 != window2) and (activePage1 != activePage2)
        TestCase.assertTrue(window1 != window2);
        TestCase.assertTrue(page1 != page2);
    }

    /**
     * Remove all existing pages from current window.
     */
    private void removePages() {

        final TabbedApplicationWindow window = (TabbedApplicationWindow) //
        TestTabbedApplicationWindowFactory.this.getActiveWindow();

        final List<ApplicationPage> pages = window.getPages();
        for (ApplicationPage page : pages) {
            window.removePage(page);
        }
    }

    /**
     * Shows a page given its descriptor.
     * 
     * @param pageDescriptor
     *            the page descriptor.
     * @return the page.
     */
    private ApplicationPage showPage(PageDescriptor pageDescriptor) {

        this.getActiveWindow().showPage(pageDescriptor);

        return this.getActiveWindow().getPage();
    }
}
