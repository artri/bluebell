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
package org.bluebell.richclient.application.support;

import junit.framework.TestCase;

import org.bluebell.richclient.application.support.DefaultApplicationPageConfigurer;
import org.bluebell.richclient.form.AbstractB2TableMasterForm;
import org.bluebell.richclient.form.AbstractBbChildForm;
import org.bluebell.richclient.form.AbstractBbSearchForm;
import org.bluebell.richclient.form.BbValidationForm;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private PageDescriptor masterViewPageDescriptor;
    
    /**
     * A page descriptor containing a detail view descriptor.
     */
    @Autowired
    private PageDescriptor detailViewPageDescriptor;
    
    /**
     * A page descriptor containing a search view descriptor.
     */
    @Autowired
    private PageDescriptor searchViewPageDescriptor;
   
    /**
     * A page descriptor containing a validation view descriptor.
     */
    @Autowired
    private PageDescriptor validationViewPageDescriptor;
    
    /**
     * A page descriptor containing a master and a detail view descriptors.
     */
    @Autowired
    private PageDescriptor masterAndDetailViewsPageDescriptor;

    /**
     * A page descriptor containing a master and a detail view descriptors (reverse order).
     */
    @Autowired
    private PageDescriptor rMasterAndDetailViewsPageDescriptor;

    /**
     * A page descriptor containing a master and a search view descriptors.
     */
    @Autowired
    private PageDescriptor masterAndSearchViewsPageDescriptor;

    /**
     * A page descriptor containing a master and a search view descriptors (reverse order).
     */
    @Autowired
    private PageDescriptor rMasterAndSearchViewsPageDescriptor;

    /**
     * A page descriptor containing all kind of views.
     */
    @Autowired
    private PageDescriptor fullPageDescriptor;
    
    /**
     * A page descriptor containing all kind of views (reverse order).
     */
    @Autowired
    private PageDescriptor rFullPageDescriptor;
    
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void testDependencyInjection() {
         
        super.testDependencyInjection();
        
        TestCase.assertNotNull("masterViewPageDescriptor", this.masterViewPageDescriptor);
        TestCase.assertNotNull("detailViewPageDescriptor", this.detailViewPageDescriptor);
        TestCase.assertNotNull("searchViewPageDescriptor", this.searchViewPageDescriptor);
        TestCase.assertNotNull("validationViewPageDescriptor", this.validationViewPageDescriptor);
        TestCase.assertNotNull("masterAndDetailViewsPageDescriptor", this.masterAndDetailViewsPageDescriptor);
        TestCase.assertNotNull("rMasterAndDetailViewsPageDescriptor", this.rMasterAndDetailViewsPageDescriptor);
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
     * Case that tests pages with a single detail view descriptor.
     */
    @Test
    public void testDetailViewDescriptor() {

        this.initializeVariables(this.detailViewPageDescriptor);

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
     * Case that tests pages with a master view descriptor and a detail view descriptor.
     */
    @Test
    public void testMasterAndDetailViewDescriptors() {

        this.initializeVariables(this.masterAndDetailViewsPageDescriptor);

        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
    }

    /**
     * The same test as {@link #testMasterAndDetailViewDescriptors()} but with inverse view descriptors order.
     */
    @Test
    public void testMasterAndDetailViewDescriptorsReverse() {

        this.initializeVariables(this.rMasterAndDetailViewsPageDescriptor);

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

        new DefaultApplicationPageConfigurer<Object>().configureApplicationPage(this.getApplicationPage());
        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);

        new DefaultApplicationPageConfigurer<Object>().configureApplicationPage(this.getApplicationPage());
        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
    }

    /**
     * Asserts page composition based on view descriptors existance.
     * 
     * @param masterViewIsNull
     *            <code>true</code> if master view is null.
     * @param detailViewIsNull
     *            <code>true</code> if detail view is null.
     * @param searchViewIsNull
     *            <code>true</code> if search view is null.
     * @param validationViewIsNull
     *            <code>true</code> if validation view is null.
     */
    private void assertViewDescriptors(Boolean masterViewIsNull, Boolean detailViewIsNull, Boolean searchViewIsNull,
            Boolean validationViewIsNull) {

        // Page views related assertions
        TestCase.assertTrue(masterViewIsNull == (this.getMasterView() == null));
        TestCase.assertTrue(detailViewIsNull == (this.getDetailView() == null));
        TestCase.assertTrue(searchViewIsNull == (this.getSearchView() == null));
        TestCase.assertTrue(validationViewIsNull == (this.getValidationView() == null));

        // Master form related assertions
        final AbstractB2TableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        final AbstractBbChildForm<Person> detailForm = this.getBackingForm(this.getDetailView());
        final AbstractBbSearchForm<Person, ?> searchForm = this.getBackingForm(this.getSearchView());
        final BbValidationForm<Person> validationForm = this.getBackingForm(this.getValidationView());

        if (masterForm != null) {
            if (detailForm != null) {
                TestCase.assertTrue(masterForm.getDetailForms().contains(detailForm));
                TestCase.assertNotNull(this.getDetailView().getGlobalCommandsAccessor());
            } else {
                TestCase.assertTrue(masterForm.getDetailForms().isEmpty());
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
        if (detailForm != null) {
            TestCase.assertEquals(masterForm, detailForm.getMasterForm());
        }
        if (searchForm != null) {
            TestCase.assertEquals(masterForm, searchForm.getMasterForm());
        }
        if (validationForm != null) {
            TestCase.assertEquals(masterForm, validationForm.getMasterForm());
        }
    }
}
