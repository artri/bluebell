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

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageListener;
import org.springframework.richclient.application.support.ApplicationServicesAccessor;
import org.springframework.util.Assert;

/**
 * This class is a page listener and an aspect.
 * <p>
 * <ul>
 * <li>As an <em>aspect</em> is responsible of intercepting page creation operations and attaching itself as page
 * listener.
 * <li>As <em>page listener</em> is responsible of doing some tricky actions after opening and closing pages.
 * </ul>
 * <p>
 * All of this is needed since page component activation doesn't work fine out of the box while dealing with multiple
 * pages.
 * <p>
 * <b>Note</code> ideally this class should be merged into
 * {@link org.springframework.richclient.application.support.AbstractApplicationPage}, but this is Bluebell, not Spring
 * RCP.
 * 
 * <pre>
 * <!--
 *         Bean: applicationWindowAspect
 *         Usage: magic
 *         Description: This bean is an aspect capable of intercepting every window creation to attach a page listener.
 * -->
 * <bean id="applicationWindowAspect" class="org.bluebell.richclient.application.support.ApplicationWindowAspect" />
 * </pre>
 * 
 * @see PageListener
 * @see ApplicationWindow
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@Aspect
public class ApplicationWindowAspect extends ApplicationServicesAccessor implements PageListener {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationWindowAspect.class);

    /**
     * String key useful to remember last active component.
     */
    private static final String LAST_ACTIVE_COMPONENT = ApplicationWindowAspect.class.getName()
            + "::lastActiveComponent";

    /**
     * A property of the focus handler.
     */
    private static final String LAST_FOCUSED_DOCKABLE = "lastFocusedDockable";

    /**
     * A property of the docking desktop.
     */
    private static final String FOCUS_HANDLER = "focusHandler";

    /**
     * Pointcut that intercepts window creation operations.
     * <p>
     * This method does nothing.
     */
    @Pointcut("execution(* org.springframework.richclient.application.ApplicationWindowFactory."
            + "createApplicationWindow(..))")
    public final void windowCreationOperation() {

    }

    /**
     * Attach <code>this</code> as a page listener of the just created application window.
     * 
     * @param applicationWindow
     *            the target application window.
     */
    @AfterReturning(pointcut = "windowCreationOperation()", returning = "applicationWindow")
    public final void addPageListener(ApplicationWindow applicationWindow) {

        applicationWindow.addPageListener(this);

        if (ApplicationWindowAspect.LOGGER.isDebugEnabled()) {
            ApplicationWindowAspect.LOGGER.debug("Listener added to window with number \""
                    + applicationWindow.getNumber() + "\"");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pageOpened(ApplicationPage applicationPage) {

        // 1. Show application page
        applicationPage.getControl().setVisible(Boolean.TRUE);

        // 2. Force a focus gained event, this is needed in order to be aware of recently retrieved application page
        // configuration (ApplicationPageConfigurer). Otherwise no event will be raised and no handlers will be invoked
        // (i.e. SharedCommandTargeter)
        final PageComponent lastActiveComponent = this.getLastActiveComponent(applicationPage);
        ApplicationUtils.forceFocusGained(applicationPage, lastActiveComponent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pageClosed(ApplicationPage applicationPage) {

        // 1. Hide application page
        applicationPage.getControl().setVisible(Boolean.FALSE);

        // 2. Reset DockingDesktop#focusHandler#lastFocusedDockable
        // this.doSomethingTrickyWithVLDocking(applicationPage);

        // 3. Reset AbstractApplicationPage#activeComponent after remembering it
        this.rememberLastActiveComponent(applicationPage);
        ApplicationUtils.resetActiveComponent(applicationPage);
    }

    /**
     * Reset <code>lastFocusedDockable</code> property from the <em>focus handler</em> of a <code>DockingDesktop</code>
     * control. This is needed in order to propagate focus changed events.
     * <p>
     * Yes, I know this code is VLDocking dependant... ...however no import is needed and therefore it's fully fault
     * tolerant.
     * <p>
     * <b>Updated</b> at 20101125, it seems to be this code is no longer needed.
     * 
     * @param applicationPage
     *            the target application page whose abstraction is a <code>DockingDesktop</code>.
     */
    protected final void doSomethingTrickyWithVLDocking(ApplicationPage applicationPage) {

        try {
            // This code is VLDocking dependant, however no import is needed and therefore it's fault tolerant
            final Object focusHandler = PropertyAccessorFactory.forDirectFieldAccess(applicationPage.getControl())//
                    .getPropertyValue(ApplicationWindowAspect.FOCUS_HANDLER);
            PropertyAccessorFactory.forDirectFieldAccess(focusHandler)//
                    .setPropertyValue(ApplicationWindowAspect.LAST_FOCUSED_DOCKABLE, null);

        } catch (NotReadablePropertyException e) {
            ApplicationWindowAspect.LOGGER.warn(//
                    "Unable to reset lastFocusedDockable, may be not using VLDocking?");
        }
    }

    /**
     * Gets the last active component on a given page (if any).
     * 
     * @param applicationPage
     *            the target application page.
     * @return the last active component (may be <code>null</code>).
     */
    protected PageComponent getLastActiveComponent(ApplicationPage applicationPage) {

        Assert.notNull(applicationPage, "applicationPage");

        final PageComponent currentActiveComponent = applicationPage.getActiveComponent();

        final PageComponent lastActiveComponent;
        if (currentActiveComponent != null) {
            lastActiveComponent = currentActiveComponent;
        } else {
            lastActiveComponent = (PageComponent) applicationPage.getControl().getClientProperty(
                    ApplicationWindowAspect.LAST_ACTIVE_COMPONENT);
        }

        return lastActiveComponent;
    }

    /**
     * Remembers last active component on a given page.
     * <p>
     * This implementation employs page control client properties as context shared between closing and opening events.
     * 
     * @param applicationPage
     *            the target application page.
     */
    protected void rememberLastActiveComponent(ApplicationPage applicationPage) {

        Assert.notNull(applicationPage, "applicationPage");

        applicationPage.getControl().putClientProperty(//
                ApplicationWindowAspect.LAST_ACTIVE_COMPONENT, applicationPage.getActiveComponent());
    }
}
