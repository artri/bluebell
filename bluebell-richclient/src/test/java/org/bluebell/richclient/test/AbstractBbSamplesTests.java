/**
 * 
 */
package org.bluebell.richclient.test;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.bluebell.richclient.application.support.FormBackedView;
import org.bluebell.richclient.form.AbstractBb2TableMasterForm;
import org.bluebell.richclient.form.AbstractBbChildForm;
import org.bluebell.richclient.form.AbstractBbSearchForm;
import org.bluebell.richclient.form.BbPageComponentsConfigurer;
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
import org.springframework.richclient.application.support.MultiViewPageDescriptor;
import org.springframework.richclient.form.Form;

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
public abstract class AbstractBbSamplesTests extends AbstractBbRichClientTests {

    /**
     * The application page factory bean name.
     */
    protected static final String APPLICATION_PAGE_FACTORY_BEAN_NAME = "applicationPageFactory";

    /**
     * The master view descriptor bean name
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
     * The page descriptor to be populated.
     */
    @Autowired
    protected MultiViewPageDescriptor personPageDescriptor;

    /**
     * The factory to create application pages.
     */
    protected ApplicationPageFactory applicationPageFactory;

    /**
     * The active window.
     */
    protected ApplicationWindow activeWindow;

    /**
     * The application page to be tested.
     */
    protected ApplicationPage applicationPage;

    /**
     * The master view to be tested.
     */
    protected FormBackedView<AbstractBb2TableMasterForm<Person>> masterView;

    /**
     * The detail view to be tested.
     */
    protected FormBackedView<AbstractBbChildForm<Person>> detailView;

    /**
     * The search view to be tested.
     */
    protected FormBackedView<AbstractBbSearchForm<Person, Person>> searchView;

    /**
     * The validation view to be tested.
     */
    protected FormBackedView<BbValidationForm<Person>> validationView;

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
	TestCase.assertNotNull(this.personPageDescriptor);

	this.initializeVariables(this.personPageDescriptor);

	// Initialized variables
	TestCase.assertNotNull(this.applicationPageFactory);
	TestCase.assertNotNull(this.activeWindow);
	TestCase.assertNotNull(this.applicationPage);
	TestCase.assertNotNull(this.masterView);
	TestCase.assertNotNull(this.searchView);
	TestCase.assertNotNull(this.detailView);
	TestCase.assertNotNull(this.validationView);
    }

    /**
     * Initialize other local variables different from those populated by Spring.
     * <p>
     * Call this method at the beginning of every test case.
     * 
     * @throws Exception
     *             in case of error.
     */
    public void initializeVariables(PageDescriptor pageDescriptor) {

	// Retrieve application page factory
	this.applicationPageFactory = this.getService(ApplicationPageFactory.class);

	// Retrieve active window
	this.activeWindow = Application.instance().getActiveWindow();

	// Create related page
	this.applicationPage = this.applicationPageFactory.createApplicationPage(this.activeWindow, pageDescriptor);

	try {
	    EventQueue.invokeAndWait(new Runnable() {

		@Override
		public void run() {

		    // Nothing to do, just waiting for page creation to be completed
		}
	    });
	} catch (InterruptedException e) {
	    TestCase.fail(e.getMessage());
	} catch (InvocationTargetException e) {
	    TestCase.fail(e.getMessage());
	}

	// Fire page components creation and show the new page
	this.activeWindow.showPage(this.applicationPage);

	// Retrieve page components
	this.masterView = this.applicationPage.getView(AbstractBbSamplesTests.MASTER_VIEW_DESCRIPTOR_BEAN_NAME);
	this.searchView = this.applicationPage.getView(AbstractBbSamplesTests.SEARCH_VIEW_DESCRIPTOR_BEAN_NAME);
	this.detailView = this.applicationPage.getView(AbstractBbSamplesTests.DETAIL_VIEW_DESCRIPTOR_BEAN_NAME);
	this.validationView = this.applicationPage.getView(AbstractBbSamplesTests.VALIDATION_VIEW_DESCRIPTOR_BEAN_NAME);
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

	return (view != null) ? BbPageComponentsConfigurer.getBackingForm(view).getFormModel() : null;
    }
}
