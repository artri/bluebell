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

import java.util.ArrayList;
import java.util.List;

import org.bluebell.richclient.swing.util.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.PageDescriptorRegistry;
import org.springframework.richclient.application.support.MultiViewPageDescriptor;
import org.springframework.richclient.application.support.SingleViewPageDescriptor;
import org.springframework.util.Assert;

/**
 * Utility class for dealing with general application points.
 * <p>
 * Extends {@link org.springframework.richclient.application.Application} capabilities.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class ApplicationUtils {

    // (JAF), 20101124, should other core utility classes be at this package (org.bluebell.richclient.util)? --> I don't
    // think so, Spring Framework do it like BB

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationUtils.class);

    /**
     * The active component property name of
     * {@link org.springframework.richclient.application.support.AbstractApplicationPage}.
     */
    private static final String ACTIVE_COMPONENT = "activeComponent";

    /**
     * Utility classes should have a private constructor.
     */
    private ApplicationUtils() {

        super();
    }

    /**
     * Gets the declared page component descriptors of a given page.
     * 
     * @param applicationPage
     *            the application page.
     * @return a list of page descriptors. Never empty.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getDeclaredPageComponentDescriptors(ApplicationPage applicationPage) {

        Assert.notNull(applicationPage, "applicationPage");

        // Page and view descriptors registry
        final PageDescriptorRegistry pageDescriptorRegistry = (PageDescriptorRegistry) Application.services()
                .getService(PageDescriptorRegistry.class);

        final List<String> pageDescriptors = new ArrayList<String>();

        final PageDescriptor pageDescriptor = pageDescriptorRegistry.getPageDescriptor(applicationPage.getId());
        if (pageDescriptor instanceof MultiViewPageDescriptor) {
            final MultiViewPageDescriptor multiViewPageDescriptor = (MultiViewPageDescriptor) pageDescriptor;
            pageDescriptors.addAll(multiViewPageDescriptor.getViewDescriptors());
        } else if (pageDescriptor instanceof SingleViewPageDescriptor) {
            final SingleViewPageDescriptor singleViewPageDescriptor = (SingleViewPageDescriptor) pageDescriptor;
            pageDescriptors.add(singleViewPageDescriptor.getId());
        }

        return pageDescriptors;
    }

    /**
     * Sets the active component of a given page ensuring this will raise a <code>focusGainedEvent</code>.
     * <p>
     * Newer active component will be:
     * <ol>
     * <li>Older if found.
     * <li>First page component.
     * <li><code>Null</code>.
     * </ol>
     * 
     * @param applicationPage
     *            the target application page.
     * 
     * @see #forceFocusGained(ApplicationPage, PageComponent)
     */
    public static void forceFocusGained(ApplicationPage applicationPage) {

        Assert.notNull(applicationPage, "applicationPage");

        final PageComponent oldActiveComponent = applicationPage.getActiveComponent();
        final PageComponent newActiveComponent;

        // Select new active component
        if (oldActiveComponent != null) {
            newActiveComponent = oldActiveComponent;
        } else if (!applicationPage.getPageComponents().isEmpty()) {
            newActiveComponent = applicationPage.getPageComponents().get(0);
        } else {
            newActiveComponent = null;
        }

        ApplicationUtils.forceFocusGained(applicationPage, newActiveComponent);
    }

    /**
     * Sets the active component of a given page ensuring this will raise a <code>focusGainedEvent</code>.
     * <p>
     * <b>Note</b> this code may not work if <em>tabbed pane</em> request "permanently" focus, in such a case they'll
     * win... For instance take into account the following line at <code>WidgetDesktopStyle</code>:
     * 
     * <pre>
     *      (JAF), 20101205, Very important!!!
     * 
     *      Should request focus on tab selection be activated? On one hand in case negative activation will not be 
     *      triggered correctly. On the other hand (affirmative case) every time a tabbed container is shown 
     *      request focus, so this is a problem since is better to keep old dockable focused (activated!)
     * 
     *      Decission is to keep value as FALSE since is prior to retain old selection.
     * 
     *      this.defaults.put("TabbedContainer.requestFocusOnTabSelection", Boolean.FALSE);
     * </pre>
     * 
     * Or the following at <code>org.bluebell.richclient.factory.ComponentFactoryDecorator#</code>:
     * 
     * <pre>
     * public JTabbedPane createTabbedPane() {
     * 
     *     // (JAF), 20101206, as explained in
     *     // org.bluebell.richclient.application.support.ApplicationUtils#forceFocusGained tabbed panes usually
     *     // requires focus for themselves, this is a problem that also occurs in BbFocusHighlighter (VLDocking module)
     *     // as explained here: http://www.javalobby.org/java/forums/t43667.html
     *     final JTabbedPane tabbedPane = this.getDecoratedComponentFactory().createTabbedPane();
     *     tabbedPane.setFocusable(Boolean.FALSE);
     * 
     *     return tabbedPane;
     * }
     * </pre>
     * 
     * @param applicationPage
     *            the target application page.
     * @param newActiveComponent
     *            the new active component.
     */
    public static void forceFocusGained(final ApplicationPage applicationPage, final PageComponent newActiveComponent) {

        Assert.notNull(applicationPage, "applicationPage");
        Assert.isTrue((newActiveComponent == null)
                || (applicationPage.getPageComponents().contains(newActiveComponent)),
                "New active component should be either null or be contained into the page components");

        // Proceed
        if (newActiveComponent != null) {
            ApplicationUtils.resetActiveComponent(applicationPage);
            applicationPage.setActiveComponent(newActiveComponent);

            SwingUtils.runInEventDispatcherThread(new Runnable() {

                @Override
                public void run() {

                    Boolean success = Boolean.FALSE;
                    try {
                        success = newActiveComponent.getControl().requestFocusInWindow();
                    } catch (Exception e) {
                        ApplicationUtils.LOGGER.warn("Failed to get \"" + newActiveComponent.getId() + "\" control");
                    } finally {
                        if (ApplicationUtils.LOGGER.isDebugEnabled()) {
                            ApplicationUtils.LOGGER.debug("Forcing focus gained on \"" + newActiveComponent.getId()
                                    + "\" result is \"" + success + "\"");
                        }
                    }
                }
            });
        }
    }

    /**
     * Tricky method that sets a <code>null</code> active component into the given page without raising any event or
     * nothing...
     * 
     * @param applicationPage
     *            the target application page.
     */
    public static void resetActiveComponent(ApplicationPage applicationPage) {

        Assert.notNull(applicationPage, "applicationPage");

        PropertyAccessorFactory.forDirectFieldAccess(applicationPage).setPropertyValue(
                ApplicationUtils.ACTIVE_COMPONENT, null);
    }
}
