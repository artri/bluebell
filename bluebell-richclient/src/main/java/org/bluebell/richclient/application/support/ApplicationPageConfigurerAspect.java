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
import org.bluebell.richclient.swing.util.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.PageDescriptorRegistry;
import org.springframework.richclient.application.support.ApplicationServicesAccessor;
import org.springframework.richclient.application.support.MultiViewPageDescriptor;
import org.springframework.richclient.application.support.SingleViewPageDescriptor;
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
 * <!--
 *         Bean: applicationPageConfigurerAspect
 *         Usage: magic
 *         Description: This bean is an aspect capable of intercepting every page creation to configure pages, linking well knows
 *         page components.
 * -->
 * <bean id="applicationPageConfigurerAspect" class="org.bluebell.richclient.application.support.ApplicationPageConfigurerAspect" />
 * </pre>
 * 
 * @see ApplicationPageConfigurer
 * @see #afterReturningPageCreationOperation(ApplicationWindow, MultiViewPageDescriptor, ApplicationPage)
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@Aspect
public class ApplicationPageConfigurerAspect extends ApplicationServicesAccessor {

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
            final MultiViewPageDescriptor pageDescriptor, final ApplicationPage applicationPage) {

        Assert.notNull(window, "window");
        Assert.notNull(pageDescriptor, "pageDescriptor");
        Assert.notNull(applicationPage, "page");

        if (ApplicationPageConfigurerAspect.LOGGER.isDebugEnabled()) {
            ApplicationPageConfigurerAspect.LOGGER.debug(//
                    ApplicationPageConfigurerAspect.PAGE_CREATION_FMT.format(new Object[] { //
                            Integer.valueOf(1), pageDescriptor.getId(), Integer.valueOf(window.getNumber()) }));
        }

        final ApplicationPageConfigurer<?> applicationPageConfigurer = (ApplicationPageConfigurer<?>) Application
                .services().getService(ApplicationPageConfigurer.class);

        // Page components creation must be done in the event dispatcher thread
        SwingUtils.runInEventDispatcherThread(new Runnable() {

            public void run() {

                // 1) Trigger page control creation, this will attach page components to application page
                applicationPage.getControl();

                // 2) (JAF), 20101124, at this point (after 1), page components should have already been added
                ApplicationPageConfigurerAspect.this.invariant(applicationPage);

                // 2) Add all view descriptors to the page
                // final List<String> viewDescriptorIds = pageDescriptor.getViewDescriptors();
                // for (final String viewDescriptorId : viewDescriptorIds) {
                // // We just need to add the page componente but API force us to call showView
                // if (applicationPage.getView(viewDescriptorId) != null) {
                // applicationPage.showView(viewDescriptorId);
                // }}

                // 3) Process page
                applicationPageConfigurer.configureApplicationPage(applicationPage);
            }
        });

    }

    /**
     * Ensures the number of page components of a given page is greater than the number of page component descriptors of
     * its associated page descriptor.
     * 
     * @param applicationPage
     *            the application page to be tested.
     */
    private void invariant(ApplicationPage applicationPage) {

        final PageDescriptorRegistry pageDescriptorRegistry = (PageDescriptorRegistry) Application.services()
                .getService(PageDescriptorRegistry.class);
        final PageDescriptor pageDescriptor = pageDescriptorRegistry.getPageDescriptor(applicationPage.getId());

        final int noPageComponents = applicationPage.getPageComponents().size();
        final int noViewDescriptors;
        if (pageDescriptor instanceof MultiViewPageDescriptor) {
            noViewDescriptors = ((MultiViewPageDescriptor) pageDescriptor).getViewDescriptors().size();
        } else if (pageDescriptor instanceof SingleViewPageDescriptor) {
            noViewDescriptors = 1;
        } else {
            noViewDescriptors = 0;
        }

        Assert.isTrue(noPageComponents >= noViewDescriptors);
    }
}
