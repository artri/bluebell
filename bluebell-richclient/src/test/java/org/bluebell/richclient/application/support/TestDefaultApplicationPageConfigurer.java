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

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.bluebell.richclient.form.AbstractB2TableMasterForm;
import org.bluebell.richclient.form.AbstractBbChildForm;
import org.bluebell.richclient.form.AbstractBbSearchForm;
import org.bluebell.richclient.form.BbValidationForm;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.Test;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.test.context.ContextConfiguration;

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
     * 
     */
    public TestDefaultApplicationPageConfigurer() {

        // TODO Auto-generated constructor stub
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
        final AbstractB2TableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        final AbstractBbChildForm<Person> childForm = this.getBackingForm(this.getChildView());
        final AbstractBbSearchForm<Person, ?> searchForm = this.getBackingForm(this.getSearchView());
        final BbValidationForm<Person> validationForm = this.getBackingForm(this.getValidationView());

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
                new String("Avoid CS warnings");
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
}
