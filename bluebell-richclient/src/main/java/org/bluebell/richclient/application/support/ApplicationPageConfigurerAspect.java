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

import java.text.MessageFormat;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.bluebell.richclient.application.ApplicationPageConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageComponentListener;
import org.springframework.richclient.application.support.ApplicationServicesAccessor;
import org.springframework.richclient.application.support.MultiViewPageDescriptor;
import org.springframework.util.Assert;

/**
 * Simple aspect that makes the following actions after creating any page:
 * <ol>
 * <li>Trigger page control creation.
 * <li>Show every single view descriptor.
 * <li>Call {@link ApplicationPageConfigurer#configureApplicationPage(ApplicationPage)}.
 * </ol>
 * 
 * <pre>
 * {@code
 * 
 *  <bean id="applicationPageConfigurerAspect" 
 *     class="org.bluebell.richclient.application.support.ApplicationPageConfigurerAspect"
 *     p:applicationPageConfigurer-ref="$ richclient.applicationPageConfigurer}" />
 * }
 * </pre>
 * 
 * @see ApplicationPageConfigurer
 * @see #afterReturningPageCreationOperation(ApplicationWindow, MultiViewPageDescriptor, ApplicationPage)
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@Aspect
public class ApplicationPageConfigurerAspect extends ApplicationServicesAccessor implements PageComponentListener,
        InitializingBean {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPageConfigurerAspect.class);

    /**
     * Message format required for debug information during page creation.
     */
    private static final MessageFormat PAGE_CREATION_FMT = new MessageFormat(
            "{0, choice, 0#Before|0<After} creating page \"{1}\" in window \"{2}\"");

    /**
     * Message format required for debug information during page opened / closed event.
     */
    private static final MessageFormat PAGE_COMPONENT_EVENT_FMT = new MessageFormat(
            "Page {0} configured after {1, choice, 0#opening|0<closing} page component \"{2}\" in window \"{3}\"");

    /**
     * The application page configurer.
     */
    private ApplicationPageConfigurer<?> applicationPageConfigurer;

    /**
     * Pointcut that intercepts page creation operations.
     * <p>
     * This method does nothing.
     */
    @Pointcut("execution(* org.springframework.richclient.application.ApplicationPageFactory."
            + "createApplicationPage(..))")
    public final void pageCreationOperation() {

    }

    /**
     * Advise that acts just before intercepting page creation operations.
     * <p>
     * This implementation ensures application window and page descriptor are not null and then writes a debug message.
     * 
     * @param window
     *            the target window.
     * @param pageDescriptor
     *            the page descriptor.
     */
    @Before("pageCreationOperation() && args(window,pageDescriptor)")
    public final void beforePageCreationOperation(ApplicationWindow window, MultiViewPageDescriptor pageDescriptor) {

        Assert.notNull(window, "window");
        Assert.notNull(pageDescriptor, "pageDescriptor");

        if (ApplicationPageConfigurerAspect.LOGGER.isDebugEnabled()) {
            ApplicationPageConfigurerAspect.LOGGER.debug(//
                    ApplicationPageConfigurerAspect.PAGE_CREATION_FMT.format(new Object[] {//
                            Integer.valueOf(0), pageDescriptor.getId(), Integer.valueOf(window.getNumber()) }));
        }
    }

    /**
     * Process a page just inmediatly after creating it.
     * <p>
     * Note that at this point the page <b>has no components</b>, so we need to add them before.
     * <p>
     * This method operates in 3 phases:
     * <ol>
     * <li>Triggers page control creation. This <b>DOES NOT</b> include page components controls creation.
     * <li>Add all described views to the page.
     * <li>Process the page.
     * </ol>
     * <p>
     * There is an invariant consisting on "page components control are not created at all at this method".
     * 
     * @param window
     *            the application window where the page is about to show.
     * @param pageDescriptor
     *            the page descriptor.
     * @param applicationPage
     *            the created page.
     * 
     * @see #configureApplicationPage(ApplicationPage)
     */
    @AfterReturning(pointcut = "pageCreationOperation() && args(window,pageDescriptor)", returning = "applicationPage")
    public final void afterReturningPageCreationOperation(ApplicationWindow window,
            final MultiViewPageDescriptor pageDescriptor, ApplicationPage applicationPage) {

        Assert.notNull(window, "window");
        Assert.notNull(pageDescriptor, "pageDescriptor");
        Assert.notNull(applicationPage, "page");

        if (ApplicationPageConfigurerAspect.LOGGER.isDebugEnabled()) {
            ApplicationPageConfigurerAspect.LOGGER.debug(//
                    ApplicationPageConfigurerAspect.PAGE_CREATION_FMT.format(new Object[] { //
                            Integer.valueOf(1), pageDescriptor.getId(), Integer.valueOf(window.getNumber()) }));
        }

        this.configureApplicationPage(applicationPage);

        // (JAF), 20110128, listen to page component opening or closing
        applicationPage.addPageComponentListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void componentOpened(PageComponent pageComponent) {

        this.configureApplicationPage(pageComponent, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void componentClosed(PageComponent pageComponent) {

        this.configureApplicationPage(pageComponent, Boolean.FALSE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void componentFocusGained(PageComponent pageComponent) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void componentFocusLost(PageComponent pageComponent) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Assert.notNull(this.getApplicationPageConfigurer(), "this.getApplicationPageConfigurer()");
    }

    /**
     * Sets the application page configurer.
     * 
     * @param applicationPageConfigurer
     *            the application page configurer to set.
     */
    public final void setApplicationPageConfigurer(ApplicationPageConfigurer<?> applicationPageConfigurer) {

        Assert.notNull(applicationPageConfigurer, "applicationPageConfigurer");

        this.applicationPageConfigurer = applicationPageConfigurer;
    }

    /**
     * Gets the application page configurer.
     * 
     * @return the application page configurer.
     */
    protected final ApplicationPageConfigurer<?> getApplicationPageConfigurer() {

        return this.applicationPageConfigurer;
    }

    /**
     * Reconfigures page every time a meaningful event is raised.
     * 
     * @param pageComponent
     *            the page component.
     * @param opened
     *            <code>true</code> if page component has been opened and <code>false</code> in other case.
     */
    private void configureApplicationPage(PageComponent pageComponent, Boolean opened) {

        Assert.notNull(pageComponent, "pageComponent");

        final ApplicationPage applicationPage = pageComponent.getContext().getPage();

        this.configureApplicationPage(applicationPage);

        if (ApplicationPageConfigurerAspect.LOGGER.isDebugEnabled()) {
            final Integer windowNumber = applicationPage.getWindow().getNumber();

            ApplicationPageConfigurerAspect.LOGGER.debug(//
                    ApplicationPageConfigurerAspect.PAGE_COMPONENT_EVENT_FMT.format(new Object[] { //
                            applicationPage.getId(), opened ? 0x0 : 0x1, pageComponent.getId(), windowNumber }));
        }
    }

    /**
     * Reconfigures page every time a meaningful event is raised.
     * 
     * @param applicationPage
     *            the application page.
     */
    private void configureApplicationPage(ApplicationPage applicationPage) {

        Assert.notNull(applicationPage, "applicationPage");

        this.getApplicationPageConfigurer().configureApplicationPage(applicationPage);
    }
}
