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

import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.support.AbstractApplicationPage;
import org.springframework.util.Assert;

/**
 * Utility class for dealing with general application points.
 * <p>
 * Extends {@link org.springframework.richclient.application.Application} capabilities.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class ApplicationUtils {

    // (JAF), 20101124, should other core utility classes be at this package (org.bluebell.richclient.util)?

    /**
     * The active component property name of {@link AbstractApplicationPage}.
     */
    private static final String ACTIVE_COMPONENT = "activeComponent";

    /**
     * Utility classes should have a private constructor.
     */
    private ApplicationUtils() {

        super();
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
     * 
     * @param applicationPage
     *            the target application page.
     * @param newActiveComponent
     *            the new active component.
     */
    public static void forceFocusGained(ApplicationPage applicationPage, PageComponent newActiveComponent) {

        Assert.notNull(applicationPage, "applicationPage");
        Assert.isTrue((newActiveComponent == null)
                || (applicationPage.getPageComponents().contains(newActiveComponent)),
                "New active component should be either null or be contained into the page components");

        // Proceed
        if (newActiveComponent != null) {
            ApplicationUtils.resetActiveComponent(applicationPage);
            applicationPage.setActiveComponent(newActiveComponent);
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
