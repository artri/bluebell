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

package org.bluebell.richclient.application.support;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;
import org.bluebell.richclient.application.ApplicationWindowException;
import org.bluebell.richclient.swing.util.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.ApplicationWindowFactory;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.support.AbstractApplicationWindow;
import org.springframework.richclient.application.support.AbstractPageDescriptor;
import org.springframework.richclient.application.support.EmptyPageDescriptor;
import org.springframework.richclient.factory.ComponentFactory;
import org.springframework.util.Assert;

/**
 * Application window factory that creates {@link TabbedApplicationWindow}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TabbedApplicationWindowFactory implements ApplicationWindowFactory {

    /**
     * The <em>logger</em>.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TabbedApplicationWindowFactory.class);

    /**
     * {@inheritDoc}
     */
    public ApplicationWindow createApplicationWindow() {

        if (TabbedApplicationWindowFactory.LOGGER.isDebugEnabled()) {
            TabbedApplicationWindowFactory.LOGGER.debug("Creating new TabbedPaneApplicationWindow");
        }

        // Create the application window and activate it
        final ApplicationWindow window = new TabbedApplicationWindow();
        Application.instance().getWindowManager().setActiveWindow(window);

        return window;
    }

    /**
     * Application window that holds pages into tabs.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class TabbedApplicationWindow extends AbstractApplicationWindow {

        /**
         * The identifier of the empty page descriptor to be used when tab count is zero.
         */
        public static final String EMPTY_PAGE_DESCRIPTOR_ID = "emptyPageDescriptor";

        /**
         * The name of the tab control client property with the application page.
         */
        public static final String APP_PAGE_PROPERTY_NAME = "applicationPage";

        /**
         * Error code for exceptions raised while trying to add a duplicated page.
         */
        public static final String DUPLICATED_PAGE_ERROR_CODE = "duplicatedPageException";

        /**
         * Error code for exceptions raised while trying to remove an unknown page.
         */
        public static final String UNKNOWN_PAGE_ERROR_CODE = "unknownPageException";

        /**
         * The <em>logger</em>.
         */
        private static final Logger LOGGER = LoggerFactory.getLogger(TabbedApplicationWindow.class);

        /**
         * The empty page to be used when tab count is zero.
         */
        private PageDescriptor emptyPageDescriptor;

        /**
         * The tabbed pane.
         */
        private JTabbedPane tabbedPane;

        /**
         * Gets the page at a given position.
         * 
         * @param index
         *            the page index.
         * @return the page.
         * 
         * @see #addPage(ApplicationPage)
         */
        public final ApplicationPage getPageAt(int index) {

            Assert.isTrue(index >= 0, "index >=0");
            Assert.isTrue(index < this.getTabbedPane().getTabCount(), "index < this.getTabbedPane().getTabCount()");

            // Obtain the page id throw the selected component name.
            final JComponent tabComponent = ((JComponent) this.getTabbedPane().getComponentAt(index));

            final ApplicationPage page = (ApplicationPage) tabComponent.getClientProperty(//
                    TabbedApplicationWindow.APP_PAGE_PROPERTY_NAME);

            return page;
        }

        /**
         * Gets the tab index of a given page.
         * 
         * @param page
         *            the page.
         * @return the tab index, <code>-1</code> if not found.
         */
        public final int getIndexOfPage(ApplicationPage page) {

            Assert.notNull(page, "page");

            return this.getTabbedPane().indexOfComponent(page.getControl());
        }

        /**
         * Gets the pages belonging to this window.
         * 
         * @return the pages.
         */
        public List<ApplicationPage> getPages() {

            final List<ApplicationPage> pages = new ArrayList<ApplicationPage>(this.getTabbedPane().getTabCount());

            for (int i = 0; i < this.getTabbedPane().getTabCount(); ++i) {
                pages.add(this.getPageAt(i));
            }

            return pages;
        }

        /**
         * Inserts a page as a new tab.
         * <p>
         * This method ensures the page does not already belong to the tabbed pane, in such a case raise an exception.
         * 
         * @param page
         *            the page.
         */
        public void addPage(final ApplicationPage page) {

            Assert.notNull(page, "page");
            Assert.notNull(page.getId(), "page.getId()");

            if (this.getIndexOfPage(page) >= 0) {
                throw new ApplicationWindowException("Page with id \"" + page.getId() + "\" is duplicated",
                        TabbedApplicationWindow.DUPLICATED_PAGE_ERROR_CODE, ((Integer) this.getNumber()).toString());
            }

            PageDescriptor descriptor = null;
            try {
                descriptor = this.getPageDescriptor(page.getId());
            } catch (IllegalStateException e) {
                TabbedApplicationWindow.LOGGER.warn("Page descriptor \"" + page.getId() + "\" not found");
            } finally {

                final String displayName = (descriptor != null) ? descriptor.getDisplayName() : StringUtils.EMPTY;
                final Icon icon = (descriptor != null) ? descriptor.getIcon() : null;
                final String description = (descriptor != null) ? descriptor.getDescription() : StringUtils.EMPTY;

                SwingUtils.runInEventDispatcherThread(new Runnable() {

                    @Override
                    public void run() {

                        final JTabbedPane pane = TabbedApplicationWindow.this.getTabbedPane();

                        pane.insertTab(displayName, icon, page.getControl(), description, pane.getTabCount());
                        page.getControl().putClientProperty(TabbedApplicationWindow.APP_PAGE_PROPERTY_NAME, page);
                    }
                });
            }
        }

        /**
         * Removes a page.
         * <p>
         * This method ensures the page already belongs to the tabbed pane, in other case raise an exception.
         * 
         * @param page
         *            the page.
         */
        public void removePage(ApplicationPage page) {

            Assert.notNull(page, "page");
            Assert.notNull(page.getId(), "page.getId()");

            if (this.isEmptyPage(page)) {
                return;
            }

            final int indexOfPage = this.getIndexOfPage(page);

            if (indexOfPage < 0) {
                throw new ApplicationWindowException("Page with id \"" + page.getId() + "\" is unknown",
                        TabbedApplicationWindow.UNKNOWN_PAGE_ERROR_CODE, ((Integer) this.getNumber()).toString());
            }

            SwingUtils.runInEventDispatcherThread(new Runnable() {

                @Override
                public void run() {

                    TabbedApplicationWindow.this.getTabbedPane().remove(indexOfPage);
                }
            });

        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected PageDescriptor getPageDescriptor(String pageDescriptorId) {

            if (this.getEmptyPageDescriptor().getId().equals(pageDescriptorId)) {

                return this.getEmptyPageDescriptor();
            }

            return super.getPageDescriptor(pageDescriptorId);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected JComponent createToolBarControl() {

            // Improve tool bar usability
            final JToolBar toolBar = (JToolBar) super.createToolBarControl();
            toolBar.setFloatable(Boolean.TRUE);
            toolBar.setEnabled(Boolean.TRUE);

            return toolBar;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected JComponent createWindowContentPane() {

            return this.getTabbedPane();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void setActivePage(ApplicationPage page) {

            Assert.notNull(page, "page");

            // Prevent painting empty pages
            if (this.isEmptyPage(page)) {
                Assert.isTrue(this.getTabbedPane().getTabCount() == 0, "Current page is empty and tab count is > 0");
                return;
            }

            final int selectedTab = this.getTabbedPane().getSelectedIndex();

            int indexOfPage = this.getIndexOfPage(page);
            if (indexOfPage == -1) { // Add the page
                this.addPage(page);
                indexOfPage = this.getIndexOfPage(page);
            } else if (selectedTab == indexOfPage) { // Avoid recursive calls
                return;
            }

            // Select tab and validate view
            this.getTabbedPane().setSelectedIndex(indexOfPage);
            this.getTabbedPane().validate();
        }

        /**
         * Return wheter the target page is an empty page.
         * 
         * @param page
         *            the page to be tested.
         * @return <code>true</code> if it's an empty page and <code>false</code> in other case.
         */
        private Boolean isEmptyPage(ApplicationPage page) {

            return (page != null) && (page.getId() != null)
                    && page.getId().equals(this.getEmptyPageDescriptor().getId());
        }

        /**
         * Gets the tabbed pane and if doesn't exist creates it.
         * 
         * @return the tabbed pane.
         */
        private JTabbedPane getTabbedPane() {

            if (this.tabbedPane == null) {
                final ComponentFactory componentFactory = (ComponentFactory) this.//
                        getServices().getService(ComponentFactory.class);

                final JTabbedPane pane = componentFactory.createTabbedPane();
                pane.setTabPlacement(SwingConstants.TOP);
                pane.getModel().addChangeListener(new TrackPageChangesListener());

                this.setTabbedPane(pane);
            }

            return this.tabbedPane;
        }

        /**
         * Sets the tabbed pane.
         * 
         * @param tabbedPane
         *            the tabbed pane.
         * @return the tabbed pane.
         */
        private JTabbedPane setTabbedPane(JTabbedPane tabbedPane) {

            Assert.notNull(tabbedPane, "tabbedPane");

            this.tabbedPane = tabbedPane;

            return this.tabbedPane;
        }

        /**
         * Gets the empty page descriptor.
         * 
         * @return the emptyPageDescriptor.
         */
        private PageDescriptor getEmptyPageDescriptor() {

            if (this.emptyPageDescriptor == null) {
                final AbstractPageDescriptor pageDescriptor = new EmptyPageDescriptor();
                pageDescriptor.setId(TabbedApplicationWindow.EMPTY_PAGE_DESCRIPTOR_ID);

                this.setEmptyPageDescriptor(pageDescriptor);
            }

            return this.emptyPageDescriptor;
        }

        /**
         * Sets the empty page descriptor.
         * 
         * @param emptyPageDescriptor
         *            the emptyPageDescriptor to set.
         */
        private void setEmptyPageDescriptor(PageDescriptor emptyPageDescriptor) {

            Assert.notNull(emptyPageDescriptor, "pageDescriptor");

            this.emptyPageDescriptor = emptyPageDescriptor;
        }

        /**
         * Listener that handles user interation.
         * <p>
         * Other application window implementations only depends on programatic page activation, however when dealing
         * with a tabbed pane a way of intercepting user actions is needed.
         * 
         * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
         */
        private class TrackPageChangesListener implements ChangeListener {

            /**
             * {@inheritDoc}
             */
            public void stateChanged(ChangeEvent e) {

                final TabbedApplicationWindow window = TabbedApplicationWindow.this;
                final JTabbedPane pane = window.getTabbedPane();

                final int tabCount = pane.getTabCount();
                final int selectedIndex = pane.getSelectedIndex();

                // Show the more suitable page
                if (selectedIndex < 0) { // Closing the unique tab

                    // Q: Which is the current page when all tabs are closed?
                    // R: Once an application page is shown later is not possible to set a null active page, so the best
                    // suitable way is to employ an empty page descriptor
                    window.showPage(TabbedApplicationWindow.this.emptyPageDescriptor);
                } else if (selectedIndex < tabCount) {
                    final ApplicationPage page = window.getPageAt(selectedIndex);
                    if (page != null) {
                        window.showPage(page);
                    }
                }
            }
        }
    }
}
