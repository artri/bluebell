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
package org.bluebell.richclient.test;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.bluebell.richclient.application.RcpMain;
import org.bluebell.richclient.application.support.FormBackedView;
import org.bluebell.richclient.form.AbstractBbChildForm;
import org.bluebell.richclient.form.AbstractBbSearchForm;
import org.bluebell.richclient.form.AbstractBbTableMasterForm;
import org.bluebell.richclient.form.BbDispatcherForm;
import org.bluebell.richclient.form.BbValidationForm;
import org.bluebell.richclient.form.FormUtils;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationPageFactory;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.ViewDescriptor;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.application.support.MultiViewPageDescriptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

/**
 * Base class for creating tests based on Bluebell richclient architecture.
 * <p>
 * Users should assume a structure like the figure below.
 * 
 * <pre>
 *  +=============================================+
 *  +=============================================+
 *  +-------------------------------+-------------+
 *  |                               |             |
 *  |          Master View          | Search View |
 *  |                               |             |
 *  +-------------------------------+-------------+
 *  |_______|     |_______________________________|
 *  |                                             |
 *  |                 Child View                  |
 *  |                                             |
 *  |                                             |
 *  +---------------------------------------------+
 *  | Validation |                                |
 *  +=============================================+
 * </pre>
 * 
 * @see #initializeVariables(ApplicationWindow, PageDescriptor)
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration(locations = { RcpMain.DEFAULT_APP_CONTEXT_PATH, RcpMain.DEFAULT_COMMON_CONTEXT_PATH })
public abstract class AbstractBbSamplesTests extends AbstractBbRichClientTests {

    /**
     * The application page factory bean name.
     */
    protected static final String APPLICATION_PAGE_FACTORY_BEAN_NAME = "applicationPageFactory";

    /**
     * The master view descriptor bean name.
     */
    protected static final String MASTER_VIEW_DESCRIPTOR_BEAN_NAME = "personMasterViewDescriptor";

    /**
     * The person search view descriptor bean name.
     */
    protected static final String SEARCH_VIEW_DESCRIPTOR_BEAN_NAME = "personSearchViewDescriptor";

    /**
     * The person child view descriptor bean name.
     */
    protected static final String CHILD_VIEW_DESCRIPTOR_BEAN_NAME = "personChildViewDescriptor";

    /**
     * The validation view descriptor bean name.
     */
    protected static final String VALIDATION_VIEW_DESCRIPTOR_BEAN_NAME = "validationViewDescriptor";

    /**
     * The initial view descriptor bean name.
     */
    protected static final String INITIAL_VIEW_DESCRIPTOR_BEAN_NAME = "initialViewDescriptor";

    /**
     * The application page to be tested.
     */
    private ApplicationPage initializedPage;

    /**
     * The factory to create application pages.
     */
    private ApplicationPageFactory applicationPageFactory;

    /**
     * The page descriptor to be populated.
     */
    @Autowired
    private MultiViewPageDescriptor personPageDescriptor;

    /**
     * The master view to be tested.
     */
    private FormBackedView<AbstractBbTableMasterForm<Person>> masterView;

    /**
     * The dispatcher form.
     */
    private BbDispatcherForm<Person> dispatcherForm;

    /**
     * The child view to be tested.
     */
    private FormBackedView<AbstractBbChildForm<Person>> childView;

    /**
     * The search view to be tested.
     */
    private FormBackedView<AbstractBbSearchForm<Person, Person>> searchView;

    /**
     * The validation view to be tested.
     */
    private FormBackedView<BbValidationForm<Person>> validationView;

    /**
     * The initial view.
     */
    private AbstractView initialView;

    /**
     * Creates the test indicating that protected variables should be populated.
     */
    protected AbstractBbSamplesTests() {

        System.setProperty("richclient.startingPageId", "personPageDescriptor");
    }

    /**
     * Test case that checks rich client application context is created and variables used for test cases are injected.
     */
    public void testDependencyInjection() {

        // Populated variables
        TestCase.assertNotNull(this.getPersonPageDescriptor());

        this.initializeVariables(this.getPersonPageDescriptor());

        // Initialized variables
        TestCase.assertNotNull(this.getApplicationPageFactory());
        TestCase.assertNotNull(this.getActiveWindow());
        TestCase.assertNotNull(this.getInitializedPage());
        TestCase.assertNotNull(this.getMasterView());
        TestCase.assertNotNull(this.getSearchView());
        TestCase.assertNotNull(this.getChildView());
        TestCase.assertNotNull(this.getValidationView());
        TestCase.assertNotNull(this.getInitialView());

        TestCase.assertNotNull(this.getDispatcherForm());
    }

    /**
     * Initialize other local variables different from those populated by Spring.
     * <p>
     * Call this method at the beginning of every test case.
     * </p>
     * <p>
     * <b>Note</b> {@link #initializeVariables(ApplicationWindow, PageDescriptor)} is preferred when dealing with
     * multiple windows.
     * </p>
     * 
     * @param pageDescriptor
     *            the page descriptor this method applies to.
     * 
     * @see #initializeVariables(ApplicationWindow, PageDescriptor)
     */
    protected void initializeVariables(PageDescriptor pageDescriptor) {

        this.initializeVariables(this.getActiveWindow(), pageDescriptor);
    }

    /**
     * Initialize other local variables different from those populated by Spring.
     * <p>
     * Call this method at the beginning of every test case.
     * 
     * @param applicationWindow
     *            the application windows to be employed.
     * @param pageDescriptor
     *            the page descriptor this method applies to.
     * 
     * @see #initializeApplicationAndWait()
     */
    @SuppressWarnings("unchecked")
    protected void initializeVariables(ApplicationWindow applicationWindow, PageDescriptor pageDescriptor) {

        Assert.notNull(applicationWindow, "applicationWindow");
        Assert.notNull(pageDescriptor, "pageDescriptor");

        this.initializeApplicationAndWait();

        // Create related page
        this.setInitializedPage(this.getApplicationPageFactory().createApplicationPage(//
                applicationWindow, pageDescriptor));

        // Fire page components creation and show the new page
        applicationWindow.showPage(this.getInitializedPage());

        // Retrieve initial view
        AbstractView foundInitialView = applicationWindow.getPage().getView(
                AbstractBbSamplesTests.INITIAL_VIEW_DESCRIPTOR_BEAN_NAME);
        if (foundInitialView == null) {
            final ViewDescriptor initialViewDescriptor = this.getApplication().getApplicationContext().getBean(//
                    AbstractBbSamplesTests.INITIAL_VIEW_DESCRIPTOR_BEAN_NAME, ViewDescriptor.class);

            foundInitialView = (AbstractView) initialViewDescriptor.createPageComponent();
        }
        this.setInitialView(foundInitialView);

        // Retrieve page components
        this.setMasterView((FormBackedView<AbstractBbTableMasterForm<Person>>) applicationWindow.getPage().getView(
                AbstractBbSamplesTests.MASTER_VIEW_DESCRIPTOR_BEAN_NAME));
        this.setSearchView((FormBackedView<AbstractBbSearchForm<Person, Person>>) applicationWindow.getPage().getView(
                AbstractBbSamplesTests.SEARCH_VIEW_DESCRIPTOR_BEAN_NAME));
        this.setChildView((FormBackedView<AbstractBbChildForm<Person>>) applicationWindow.getPage().getView(
                AbstractBbSamplesTests.CHILD_VIEW_DESCRIPTOR_BEAN_NAME));
        this.setValidationView((FormBackedView<BbValidationForm<Person>>) applicationWindow.getPage().getView(
                AbstractBbSamplesTests.VALIDATION_VIEW_DESCRIPTOR_BEAN_NAME));

        if (this.getMasterView() != null) {
            this.setDispatcherForm(FormUtils.getBackingForm(this.getMasterView()).getDispatcherForm());
        }
    }

    /**
     * Initializes the global application instance and waits until completion.
     * <p>
     * This method should be called at the beginning of every test case. May be
     * {@link #initializeVariables(PageDescriptor)} could be preferred instead.
     */
    protected final void initializeApplicationAndWait() {

        // Retrieve application page factory
        this.setApplicationPageFactory(this.getService(ApplicationPageFactory.class));

        try {
            EventQueue.invokeAndWait(new Runnable() {

                @Override
                public void run() {

                    // Nothing to do, just waiting for page creation to be completed
                    this.getClass(); //"Avoid Checkstyle warning"
                }
            });
        } catch (InterruptedException e) {
            TestCase.fail(e.getMessage());
        } catch (InvocationTargetException e) {
            TestCase.fail(e.getMessage());
        }
    }

    /**
     * Terminates the test.
     */
    protected void terminateTest() {

        // (JAF), 20101123, take into account tests with multiple windows
        // this.getActiveWindow().getControl().dispose();

        for (ApplicationWindow applicationWindow : Application.instance().getWindowManager().getWindows()) {
            applicationWindow.getControl().dispose();
        }
    }

    /**
     * Returns the specified service instance given its class.
     * 
     * @param <T>
     *            the service class.
     * @param serviceClass
     *            the service class.
     * @return the existing instance, if one.
     */
    @SuppressWarnings("unchecked")
    protected final <T> T getService(Class<T> serviceClass) {

        return (T) ApplicationServicesLocator.services().getService(serviceClass);
    }

    /**
     * Gets the initializedPage.
     * 
     * @return the initializedPage
     */
    protected final ApplicationPage getInitializedPage() {

        return this.initializedPage;
    }

    /**
     * Gets the applicationPageFactory.
     * 
     * @return the applicationPageFactory
     */
    protected final ApplicationPageFactory getApplicationPageFactory() {

        return this.applicationPageFactory;
    }

    /**
     * Gets the personPageDescriptor.
     * 
     * @return the personPageDescriptor
     */
    protected final MultiViewPageDescriptor getPersonPageDescriptor() {

        return this.personPageDescriptor;
    }

    /**
     * Gets the masterView.
     * 
     * @return the masterView
     */
    protected FormBackedView<AbstractBbTableMasterForm<Person>> getMasterView() {

        return this.masterView;
    }

    /**
     * Gets the dispatcher form.
     * 
     * @return the dispatcher form.
     */
    protected BbDispatcherForm<Person> getDispatcherForm() {

        return this.dispatcherForm;
    }

    /**
     * Gets the childView.
     * 
     * @return the childView
     */
    protected FormBackedView<AbstractBbChildForm<Person>> getChildView() {

        return this.childView;
    }

    /**
     * Gets the searchView.
     * 
     * @return the searchView
     */
    protected FormBackedView<AbstractBbSearchForm<Person, Person>> getSearchView() {

        return this.searchView;
    }

    /**
     * Gets the validationView.
     * 
     * @return the validationView
     */
    protected FormBackedView<BbValidationForm<Person>> getValidationView() {

        return this.validationView;
    }

    /**
     * Gets the initial view.
     * 
     * @return the initial view.
     */
    protected AbstractView getInitialView() {

        return this.initialView;
    }

    /**
     * Sets the initializedPage.
     * 
     * @param applicationPage
     *            the applicationPage to set
     */
    private void setInitializedPage(ApplicationPage applicationPage) {

        Assert.notNull(applicationPage, "initializedPage");

        this.initializedPage = applicationPage;
    }

    /**
     * Sets the applicationPageFactory.
     * 
     * @param applicationPageFactory
     *            the applicationPageFactory to set
     */
    private void setApplicationPageFactory(ApplicationPageFactory applicationPageFactory) {

        Assert.notNull(applicationPageFactory, "applicationPageFactory");

        this.applicationPageFactory = applicationPageFactory;
    }

    /**
     * Sets the masterView.
     * 
     * @param masterView
     *            the masterView to set
     */
    private void setMasterView(FormBackedView<AbstractBbTableMasterForm<Person>> masterView) {

        this.masterView = masterView;
    }

    /**
     * Sets the dispatcher form.
     * 
     * @param dispatcherForm
     *            the dispatcher form to set.
     */
    private void setDispatcherForm(BbDispatcherForm<Person> dispatcherForm) {

        Assert.notNull(dispatcherForm, "dispatcherForm");

        this.dispatcherForm = dispatcherForm;
    }

    /**
     * Sets the childView.
     * 
     * @param childView
     *            the childView to set
     */
    private void setChildView(FormBackedView<AbstractBbChildForm<Person>> childView) {

        this.childView = childView;
    }

    /**
     * Sets the searchView.
     * 
     * @param searchView
     *            the searchView to set
     */
    private void setSearchView(FormBackedView<AbstractBbSearchForm<Person, Person>> searchView) {

        this.searchView = searchView;
    }

    /**
     * Sets the validationView.
     * 
     * @param validationView
     *            the validationView to set
     */
    private void setValidationView(FormBackedView<BbValidationForm<Person>> validationView) {

        this.validationView = validationView;
    }

    /**
     * Sets the initial view.
     * 
     * @param initialView
     *            the initial view to set
     */
    private void setInitialView(AbstractView initialView) {

        this.initialView = initialView;
    }
}
