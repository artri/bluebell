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
package org.bluebell.richclient.test;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.bluebell.richclient.application.RcpMain;
import org.bluebell.richclient.application.support.DefaultApplicationPageConfigurer;
import org.bluebell.richclient.application.support.FormBackedView;
import org.bluebell.richclient.form.AbstractB2TableMasterForm;
import org.bluebell.richclient.form.AbstractBbChildForm;
import org.bluebell.richclient.form.AbstractBbSearchForm;
import org.bluebell.richclient.form.BbValidationForm;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationPageFactory;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.ViewDescriptor;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.application.support.MultiViewPageDescriptor;
import org.springframework.richclient.form.Form;
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
 *  |                 Detail View                 |
 *  |                                             |
 *  |                                             |
 *  +---------------------------------------------+
 *  | Validation |                                |
 *  +=============================================+
 * </pre>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration(locations = { RcpMain.DEFAULT_APP_CONTEXT_PATH })
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
     * The person detail view descriptor bean name.
     */
    protected static final String DETAIL_VIEW_DESCRIPTOR_BEAN_NAME = "personDetailViewDescriptor";

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
    private ApplicationPage applicationPage;

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
    private FormBackedView<AbstractB2TableMasterForm<Person>> masterView;

    /**
     * The detail view to be tested.
     */
    private FormBackedView<AbstractBbChildForm<Person>> detailView;

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
        TestCase.assertNotNull(this.getApplicationPage());
        TestCase.assertNotNull(this.getMasterView());
        TestCase.assertNotNull(this.getSearchView());
        TestCase.assertNotNull(this.getDetailView());
        TestCase.assertNotNull(this.getValidationView());
        TestCase.assertNotNull(this.getInitialView());
    }

    /**
     * Initialize other local variables different from those populated by Spring.
     * <p>
     * Call this method at the beginning of every test case.
     * 
     * @param pageDescriptor
     *            the page descriptor this method applies to.
     * 
     * @see #initializeApplicationAndWait()
     */
    @SuppressWarnings("unchecked")
    protected void initializeVariables(PageDescriptor pageDescriptor) {

        this.initializeApplicationAndWait();

        // Create related page
        this.setApplicationPage(//
                this.getApplicationPageFactory().createApplicationPage(this.getActiveWindow(), pageDescriptor));

        // Fire page components creation and show the new page
        this.getActiveWindow().showPage(this.getApplicationPage());

        // Retrieve page components
        this.setMasterView((FormBackedView<AbstractB2TableMasterForm<Person>>) //
                this.getApplicationPage().getView(AbstractBbSamplesTests.MASTER_VIEW_DESCRIPTOR_BEAN_NAME));
        this.setSearchView((FormBackedView<AbstractBbSearchForm<Person, Person>>) //
                this.getApplicationPage().getView(AbstractBbSamplesTests.SEARCH_VIEW_DESCRIPTOR_BEAN_NAME));
        this.setDetailView((FormBackedView<AbstractBbChildForm<Person>>) //
                this.getApplicationPage().getView(AbstractBbSamplesTests.DETAIL_VIEW_DESCRIPTOR_BEAN_NAME));
        this.setValidationView((FormBackedView<BbValidationForm<Person>>) //
                this.getApplicationPage().getView(AbstractBbSamplesTests.VALIDATION_VIEW_DESCRIPTOR_BEAN_NAME));
        this.setInitialView((AbstractView) this.getApplication().getApplicationContext().getBean(
                AbstractBbSamplesTests.INITIAL_VIEW_DESCRIPTOR_BEAN_NAME, ViewDescriptor.class).createPageComponent());

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
                    new String("Avoid Checkstyle warning");
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

        this.getActiveWindow().getControl().dispose();
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
     * Returns the view backing form.
     * 
     * @param <T>
     *            the form class.
     * @param view
     *            the view.
     * @return the backing form.
     */
    protected final <T extends Form> T getBackingForm(FormBackedView<T> view) {

        return (view != null) ? view.getBackingForm() : null;
    }

    /**
     * Returns the view backing form model.
     * 
     * @param <T>
     *            the form class.
     * @param view
     *            the view.
     * @return the backing form.
     */
    protected final <T extends Form> ValidatingFormModel getBackingFormModel(FormBackedView<T> view) {

        return (view != null) ? DefaultApplicationPageConfigurer.backingForm(view).getFormModel() : null;
    }

    /**
     * Gets the activeWindow.
     * 
     * @return the activeWindow
     */
    protected final ApplicationWindow getActiveWindow() {

        return Application.instance().getActiveWindow();
    }

    /**
     * Gets the applicationPage.
     * 
     * @return the applicationPage
     */
    protected final ApplicationPage getApplicationPage() {

        return this.applicationPage;
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
    protected FormBackedView<AbstractB2TableMasterForm<Person>> getMasterView() {

        return this.masterView;
    }

    /**
     * Gets the detailView.
     * 
     * @return the detailView
     */
    protected FormBackedView<AbstractBbChildForm<Person>> getDetailView() {

        return this.detailView;
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
     * Sets the applicationPage.
     * 
     * @param applicationPage
     *            the applicationPage to set
     */
    private void setApplicationPage(ApplicationPage applicationPage) {

        Assert.notNull(applicationPage, "applicationPage");

        this.applicationPage = applicationPage;
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
    private void setMasterView(FormBackedView<AbstractB2TableMasterForm<Person>> masterView) {

        this.masterView = masterView;
    }

    /**
     * Sets the detailView.
     * 
     * @param detailView
     *            the detailView to set
     */
    private void setDetailView(FormBackedView<AbstractBbChildForm<Person>> detailView) {

        this.detailView = detailView;
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
