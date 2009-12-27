/**
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
package org.bluebell.richclient.form.util;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bluebell.richclient.form.AbstractBb2TableMasterForm;
import org.bluebell.richclient.form.AbstractBbChildForm;
import org.bluebell.richclient.form.AbstractBbSearchForm;
import org.bluebell.richclient.form.BbPageComponentsConfigurer;
import org.bluebell.richclient.form.BbValidationForm;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.Test;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.support.MultiViewPageDescriptor;

/**
 * Tests the correct behaviour of {@link BbPageComponentsConfigurer}.
 * 
 *@author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestBbPageComponentsConfigurer extends AbstractBbSamplesTests {

    /**
     * Case that tests pages with a single master view descriptor.
     */
    @Test
    public void testMasterViewDescriptor() {

        this.initTest(new String[] { AbstractBbSamplesTests.MASTER_VIEW_DESCRIPTOR_BEAN_NAME });

        this.assertViewDescriptors(Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
    }

    /**
     * Case that tests pages with a single detail view descriptor.
     */
    @Test
    public void testDetailViewDescriptor() {

        this.initTest(new String[] { AbstractBbSamplesTests.DETAIL_VIEW_DESCRIPTOR_BEAN_NAME });

        this.assertViewDescriptors(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
    }

    /**
     * Case that tests pages with a single search view descriptor.
     */
    @Test
    public void testSearchViewDescriptor() {

        this.initTest(new String[] { AbstractBbSamplesTests.SEARCH_VIEW_DESCRIPTOR_BEAN_NAME });

        this.assertViewDescriptors(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * Case that tests pages with a single validation view descriptor.
     */
    @Test
    public void testValidationViewDescriptor() {

        this.initTest(new String[] { AbstractBbSamplesTests.VALIDATION_VIEW_DESCRIPTOR_BEAN_NAME });

        this.assertViewDescriptors(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
    }

    /**
     * Case that tests pages with a master view descriptor and a detail view descriptor.
     */
    @Test
    public void testMasterAndDetailViewDescriptors() {

        this.initTest(new String[] { AbstractBbSamplesTests.MASTER_VIEW_DESCRIPTOR_BEAN_NAME,
                AbstractBbSamplesTests.DETAIL_VIEW_DESCRIPTOR_BEAN_NAME });

        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
    }

    /**
     * The same test as {@link #testMasterAndDetailViewDescriptors()} but with inverse view descriptors order.
     */
    @Test
    public void testMasterAndDetailViewDescriptorsReverse() {

        this.initTest(new String[] { AbstractBbSamplesTests.DETAIL_VIEW_DESCRIPTOR_BEAN_NAME,
                AbstractBbSamplesTests.MASTER_VIEW_DESCRIPTOR_BEAN_NAME });

        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
    }

    /**
     * Case that tests pages with a master view descriptor and a search view descriptor.
     */
    @Test
    public void testMasterAndSearchViewDescriptors() {

        this.initTest(new String[] { AbstractBbSamplesTests.MASTER_VIEW_DESCRIPTOR_BEAN_NAME,
                AbstractBbSamplesTests.SEARCH_VIEW_DESCRIPTOR_BEAN_NAME });

        this.assertViewDescriptors(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * The same test as {@link #testMasterAndSearchViewDescriptors()} but with inverse view descriptors order.
     */
    @Test
    public void testMasterAndSearchViewDescriptorsReverse() {

        this.initTest(new String[] { AbstractBbSamplesTests.SEARCH_VIEW_DESCRIPTOR_BEAN_NAME,
                AbstractBbSamplesTests.MASTER_VIEW_DESCRIPTOR_BEAN_NAME });

        this.assertViewDescriptors(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * Case that tests pages with a view descriptor per type.
     */
    @Test
    public void testFullPage() {

        final List<?> viewDescriptors = this.getPersonPageDescriptor().getViewDescriptors();
        final String[] viewDescriptorsArray = viewDescriptors.toArray(new String[viewDescriptors.size()]);
        this.initTest(viewDescriptorsArray);

        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
    }

    /**
     * The same test as {@link #testFullPage()} but with inverse view descriptors order.
     */
    @Test
    public void testFullPageReverse() {

        final List<?> viewDescriptors = this.getPersonPageDescriptor().getViewDescriptors();
        final String[] viewDescriptorsArray = viewDescriptors.toArray(new String[viewDescriptors.size()]);
        ArrayUtils.reverse(viewDescriptorsArray);
        this.initTest(viewDescriptorsArray);

        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
    }

    /**
     * Tests multiple invocations.
     */
    @Test
    public void testMultipleInvocations() {

        this.testFullPageReverse();
        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);

        new BbPageComponentsConfigurer<Object>().configureApplicationPage(this.getApplicationPage());
        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);

        new BbPageComponentsConfigurer<Object>().configureApplicationPage(this.getApplicationPage());
        this.assertViewDescriptors(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
    }

    /**
     * Creates a page with the given view descriptors and show it in the active window.
     * 
     * @param viewDescriptorIds
     *            the view descriptors.
     * @return the resultant page descriptor.
     */
    private PageDescriptor initTest(String[] viewDescriptorIds) {

        // Create the page descriptor (use diferent id each time)
        final MultiViewPageDescriptor multiViewPageDescriptor = new MultiViewPageDescriptor();
        multiViewPageDescriptor.setId(StringUtils.join(viewDescriptorIds));
        multiViewPageDescriptor.setViewDescriptors(Arrays.asList(viewDescriptorIds));

        // Initialize test variables
        super.initializeVariables(multiViewPageDescriptor);

        return multiViewPageDescriptor;
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
        final AbstractBb2TableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
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
