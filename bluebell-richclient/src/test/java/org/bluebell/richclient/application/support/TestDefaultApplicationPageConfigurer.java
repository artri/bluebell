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

import javax.annotation.Resource;
import javax.swing.JComponent;
import javax.swing.JLabel;

import junit.framework.TestCase;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.IdentityPredicate;
import org.bluebell.richclient.application.support.DefaultApplicationPageConfigurer.BbViewType;
import org.bluebell.richclient.form.AbstractBbChildForm;
import org.bluebell.richclient.form.AbstractBbMasterForm;
import org.bluebell.richclient.form.AbstractBbSearchForm;
import org.bluebell.richclient.form.AbstractBbTableMasterForm;
import org.bluebell.richclient.form.BbValidationForm;
import org.bluebell.richclient.form.FormUtils;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.swing.util.SwingUtils;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.Test;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

/**
 * Tests the correct behaviour of {@link DefaultApplicationPageConfigurer}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration
public class TestDefaultApplicationPageConfigurer extends AbstractBbSamplesTests {

    /**
     * A page descriptor containing a master view descriptor.
     */
    @Resource
    private PageDescriptor masterViewPageDescriptor;

    /**
     * A page descriptor containing a child view descriptor.
     */
    @Resource
    private PageDescriptor childViewPageDescriptor;

    /**
     * A page descriptor containing a search view descriptor.
     */
    @Resource
    private PageDescriptor searchViewPageDescriptor;

    /**
     * A page descriptor containing a validation view descriptor.
     */
    @Resource
    private PageDescriptor validationViewPageDescriptor;

    /**
     * A page descriptor containing a master and a child view descriptors.
     */
    @Resource
    private PageDescriptor masterAndChildViewsPageDescriptor;

    /**
     * A page descriptor containing a master and a detail view descriptors (reverse order).
     */
    @Resource
    private PageDescriptor rMasterAndChildViewsPageDescriptor;

    /**
     * A page descriptor containing a master and a search view descriptors.
     */
    @Resource
    private PageDescriptor masterAndSearchViewsPageDescriptor;

    /**
     * A page descriptor containing a master and a search view descriptors (reverse order).
     */
    @Resource
    private PageDescriptor rMasterAndSearchViewsPageDescriptor;

    /**
     * A page descriptor containing all kind of views.
     */
    @Resource
    private PageDescriptor fullPageDescriptor;

    /**
     * A page descriptor containing all kind of views (reverse order).
     */
    @Resource
    private PageDescriptor rFullPageDescriptor;

    /**
     * A page descriptor containing a master and a incompatible child view descriptors.
     */
    @Resource
    private PageDescriptor masterAndIncompatibleChildViewsPageDescriptor;

    /**
     * A page descriptor containing a master and a incompatible search view descriptors.
     */
    @Resource
    private PageDescriptor masterAndIncompatibleSearchViewsPageDescriptor;

    /**
     * The page configurer.
     */
    @Resource
    private DefaultApplicationPageConfigurer<?> defaultApplicationPageConfigurer;

    /**
     * Creates the test.
     */
    public TestDefaultApplicationPageConfigurer() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testDependencyInjection() {

        super.testDependencyInjection();

        TestCase.assertNotNull("masterViewPageDescriptor", this.masterViewPageDescriptor);
        TestCase.assertNotNull("childViewPageDescriptor", this.childViewPageDescriptor);
        TestCase.assertNotNull("searchViewPageDescriptor", this.searchViewPageDescriptor);
        TestCase.assertNotNull("validationViewPageDescriptor", this.validationViewPageDescriptor);
        TestCase.assertNotNull("masterAndChildViewsPageDescriptor", this.masterAndChildViewsPageDescriptor);
        TestCase.assertNotNull("rMasterAndChildViewsPageDescriptor", this.rMasterAndChildViewsPageDescriptor);
        TestCase.assertNotNull("masterAndSearchViewsPageDescriptor", this.masterAndSearchViewsPageDescriptor);
        TestCase.assertNotNull("rMasterAndSearchViewsPageDescriptor", this.rMasterAndSearchViewsPageDescriptor);
        TestCase.assertNotNull("fullPageDescriptor", this.fullPageDescriptor);
        TestCase.assertNotNull("rFullPageDescriptor", this.rFullPageDescriptor);
        TestCase.assertNotNull("masterAndIncompatibleChildViewsPageDescriptor",
                this.masterAndIncompatibleChildViewsPageDescriptor);
        TestCase.assertNotNull("this.defaultApplicationPageConfigurer", this.defaultApplicationPageConfigurer);
    }

    /**
     * Case that tests pages with a single master view descriptor.
     */
    @Test
    public void testMasterViewDescriptor() {

        this.initializeVariables(this.masterViewPageDescriptor);

        this.assertViewDescriptors(Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
    }

    /**
     * Case that tests pages with a single child view descriptor.
     */
    @Test
    public void testChildViewDescriptor() {

        this.initializeVariables(this.childViewPageDescriptor);

        this.assertViewDescriptors(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
    }

    /**
     * Case that tests pages with a single search view descriptor.
     */
    @Test
    public void testSearchViewDescriptor() {

        this.initializeVariables(this.searchViewPageDescriptor);

        this.assertViewDescriptors(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * Case that tests pages with a single validation view descriptor.
     */
    @Test
    public void testValidationViewDescriptor() {

        this.initializeVariables(this.validationViewPageDescriptor);

        this.assertViewDescriptors(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
    }

    /**
     * Case that tests pages with a master view descriptor and a child view descriptor.
     */
    @Test
    public void testMasterAndChildViewDescriptors() {

        this.initializeVariables(this.masterAndChildViewsPageDescriptor);

        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
    }

    /**
     * The same test as {@link #testMasterAndChildViewDescriptors()} but with inverse view descriptors order.
     */
    @Test
    public void testMasterAndChildViewDescriptorsReverse() {

        this.initializeVariables(this.rMasterAndChildViewsPageDescriptor);

        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
    }

    /**
     * Case that tests pages with a master view descriptor and a search view descriptor.
     */
    @Test
    public void testMasterAndSearchViewDescriptors() {

        this.initializeVariables(this.masterAndSearchViewsPageDescriptor);

        this.assertViewDescriptors(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * The same test as {@link #testMasterAndSearchViewDescriptors()} but with inverse view descriptors order.
     */
    @Test
    public void testMasterAndSearchViewDescriptorsReverse() {

        this.initializeVariables(this.rMasterAndSearchViewsPageDescriptor);

        this.assertViewDescriptors(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * Case that tests pages with a view descriptor per type.
     */
    @Test
    public void testFullPage() {

        this.initializeVariables(this.fullPageDescriptor);

        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
    }

    /**
     * The same test as {@link #testFullPage()} but with inverse view descriptors order.
     */
    @Test
    public void testFullPageReverse() {

        this.initializeVariables(this.rFullPageDescriptor);

        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
    }

    /**
     * Case that tests pages with a incompatible child view descriptor.
     */
    @Test
    public void testIncompatibleChildView() {

        this.initializeVariables(this.masterAndIncompatibleChildViewsPageDescriptor);

        final AbstractBbMasterForm<Person> masterForm = FormUtils.getBackingForm(this.getMasterView());

        TestCase.assertTrue("masterForm.getChildForms().isEmpty()", masterForm.getChildForms().isEmpty());
    }

    /**
     * Case that tests pages with a inccompatible search view descriptor.
     */
    @Test
    public void testIncompatibleSearchView() {

        this.initializeVariables(this.masterAndIncompatibleSearchViewsPageDescriptor);

        final AbstractBbMasterForm<Person> masterForm = FormUtils.getBackingForm(this.getMasterView());

        TestCase.assertTrue("masterForm.getSearchForms().isEmpty()", masterForm.getSearchForms().isEmpty());
    }

    /**
     * Tests multiple invocations.
     */
    @Test
    public void testMultipleInvocations() {

        this.testFullPageReverse();
        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);

        new DefaultApplicationPageConfigurer<Object>().configureApplicationPage(this.getInitializedPage());
        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);

        new DefaultApplicationPageConfigurer<Object>().configureApplicationPage(this.getInitializedPage());
        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
    }

    /**
     * Tests the correct behaviour of {@link DefaultApplicationPageConfigurer#getPageComponentType(String)}.
     */
    @Test
    public void testGetPageComponentType() {

        try {
            this.defaultApplicationPageConfigurer.getPageComponentType(null);
            TestCase.fail("this.defaultApplicationPageConfigurer.getPageComponentType(null);");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        TestCase.assertEquals(BbViewType.UNKNOWN_TYPE.name(),
                this.defaultApplicationPageConfigurer.getPageComponentType("unknown_page"));
    }

    /**
     * Tests how does page behave after closing and re-opening a view.
     * 
     * @see #doTestOpenAfterClose(ApplicationPage, PageComponent)
     */
    @Test
    public void testOpenAfterClose() {

        this.testFullPageReverse();
        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);

        this.doTestOpenAfterClose(this.getActivePage(), this.getMasterView());
        this.doTestOpenAfterClose(this.getActivePage(), this.getChildView());
        this.doTestOpenAfterClose(this.getActivePage(), this.getSearchView());
        this.doTestOpenAfterClose(this.getActivePage(), this.getValidationView());
    }

    /**
     * Tests the correct behaviour of opening after closing a pae component.
     * 
     * @param applicationPage
     *            the application page.
     * @param pageComponent
     *            the page component.
     */
    private void doTestOpenAfterClose(final ApplicationPage applicationPage, final PageComponent pageComponent) {

        Assert.notNull(applicationPage, "applicationPage");
        Assert.notNull(pageComponent, "pageComponent");

        final Predicate predicate = IdentityPredicate.getInstance(pageComponent);

        /*
         * 1. Page component is present at the page.
         */
        TestCase.assertFalse("CollectionUtils.select(applicationPage.getPageComponents(), predicate).isEmpty()",
                CollectionUtils.select(applicationPage.getPageComponents(), predicate).isEmpty());
        
        /*
         * 2. Close the page component and will be removed from the page
         */
        SwingUtils.runInEventDispatcherThread(new Runnable() {

            @Override
            public void run() {

                applicationPage.close(pageComponent);
            }
        });

        TestCase.assertTrue("CollectionUtils.select(applicationPage.getPageComponents(), predicate).isEmpty()",
                CollectionUtils.select(applicationPage.getPageComponents(), predicate).isEmpty());

        /*
         * 3. Close the page component and it is no longer referenced
         */
        SwingUtils.runInEventDispatcherThread(new Runnable() {

            @Override
            public void run() {

                applicationPage.showView(pageComponent.getId());
            }
        });

        TestCase.assertTrue("CollectionUtils.select(applicationPage.getPageComponents(), predicate).isEmpty()",
                CollectionUtils.select(applicationPage.getPageComponents(), predicate).isEmpty());
    }

    /**
     * Asserts page composition based on view descriptors existance.
     * 
     * @param masterViewIsNull
     *            <code>true</code> if master view is null.
     * @param childViewIsNull
     *            <code>true</code> if child view is null.
     * @param searchViewIsNull
     *            <code>true</code> if search view is null.
     * @param validationViewIsNull
     *            <code>true</code> if validation view is null.
     */
    private void assertViewDescriptors(Boolean masterViewIsNull, Boolean childViewIsNull, Boolean searchViewIsNull,
            Boolean validationViewIsNull) {

        // Page views related assertions
        TestCase.assertTrue(masterViewIsNull == (this.getMasterView() == null));
        TestCase.assertTrue(childViewIsNull == (this.getChildView() == null));
        TestCase.assertTrue(searchViewIsNull == (this.getSearchView() == null));
        TestCase.assertTrue(validationViewIsNull == (this.getValidationView() == null));

        // Master form related assertions
        final AbstractBbTableMasterForm<Person> masterForm = FormUtils.getBackingForm(this.getMasterView());
        final AbstractBbChildForm<Person> childForm = FormUtils.getBackingForm(this.getChildView());
        final AbstractBbSearchForm<Person, ?> searchForm = FormUtils.getBackingForm(this.getSearchView());
        final BbValidationForm<Person> validationForm = FormUtils.getBackingForm(this.getValidationView());

        if (masterForm != null) {
            if (childForm != null) {
                TestCase.assertTrue(masterForm.getChildForms().contains(childForm));
                TestCase.assertNotNull(this.getChildView().getGlobalCommandsAccessor());
            } else {
                TestCase.assertTrue(masterForm.getChildForms().isEmpty());
            }
            if (searchForm != null) {
                TestCase.assertTrue(masterForm.getSearchForms().contains(searchForm));
                TestCase.assertNotNull(this.getSearchView().getGlobalCommandsAccessor());
            } else {
                TestCase.assertTrue(masterForm.getSearchForms().isEmpty());
            }
            if (validationForm != null) {
                // TODO
                // TestCase.assertTrue(masterForm.getSearchForms().contains(searchForm));
                TestCase.assertNotNull(this.getValidationView().getGlobalCommandsAccessor());
            } else {
                this.getClass(); // "Avoid CS warnings"
                // TODO
                // TestCase.assertTrue(masterForm.getSearchForms().isEmpty());
            }
        }
        if (childForm != null) {
            TestCase.assertEquals(masterForm, childForm.getMasterForm());
        }
        if (searchForm != null) {
            TestCase.assertEquals(masterForm, searchForm.getMasterForm());
        }
        if (validationForm != null) {
            TestCase.assertEquals(masterForm, validationForm.getMasterForm());
        }
    }

    /**
     * A incompatible child form.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class IncompatibleChildForm extends AbstractBbChildForm<MyPerson> {

        /**
         * The default form id.
         */
        private static final String FORM_ID = "incompatibleChildForm";

        /**
         * Creates the form with the default form id.
         */
        public IncompatibleChildForm() {

            this(IncompatibleChildForm.FORM_ID);
        }

        /**
         * Creates the form.
         * 
         * @param formId
         *            the form id.
         */
        public IncompatibleChildForm(String formId) {

            super(IncompatibleChildForm.FORM_ID);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<MyPerson> getManagedType() {

            return MyPerson.class;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected JComponent createFormControl() {

            return new JLabel();
        }
    }

    /**
     * A incompatible child form.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class IncompatibleSearchForm extends AbstractBbSearchForm<MyPerson, Person> {

        /**
         * The default form id.
         */
        private static final String FORM_ID = "incompatibleSearchForm";

        /**
         * Creates the form with the default form id.
         */
        public IncompatibleSearchForm() {

            this(IncompatibleSearchForm.FORM_ID);
        }

        /**
         * Creates the form.
         * 
         * @param formId
         *            the form id.
         */
        public IncompatibleSearchForm(String formId) {

            super(formId);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<Person> getSearchParamsType() {

            return Person.class;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<MyPerson> getSearchResultsType() {

            return MyPerson.class;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected JComponent createSearchParamsControl() {

            return new JLabel();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<MyPerson> doSearch(Person searchParams) {

            return new ArrayList<MyPerson>();
        }
    }

    /**
     * A bean that extends <code>Person</code>.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class MyPerson extends Person {

        /**
         * This is a <code>Serializable</code> class.
         */
        private static final long serialVersionUID = -1391516792411506792L;

        /**
         * Creates a bean.
         */
        public MyPerson() {

            super();
        }
    }
}
