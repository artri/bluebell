/*
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell VLDocking.
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
package org.bluebell.richclient.application.docking.vldocking;

import junit.framework.TestCase;

import org.bluebell.richclient.application.support.DefaultApplicationPageConfigurer;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ViewDescriptor;
import org.springframework.richclient.application.ViewDescriptorRegistry;
import org.springframework.richclient.application.docking.vldocking.VLDockingViewDescriptor;

/**
 * Tests the correct behaviour of {@link VLDockingBeanPostProcessor}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestVLDockingBeanPostProcessor extends AbstractBbSamplesTests {

    /**
     * The bean instance to test.
     */
    @Autowired
    public VLDockingBeanPostProcessor vlDockingBeanPostProcessor;

    /**
     * The default application page configurer implementation.
     */
    @Autowired
    public DefaultApplicationPageConfigurer<?> defaultApplicationPageConfigurer;

    /**
     * {@inheritDoc}
     */
    @Test
    public void testDependencyInjection() {

        super.testDependencyInjection();

        TestCase.assertNotNull(this.vlDockingBeanPostProcessor);
        TestCase.assertNotNull(this.defaultApplicationPageConfigurer);
        TestCase.assertNotNull(this.getViewDescriptorRegistry());
    }

    /**
     * Tests the correct behaviour of view descriptor implementor replacement.
     */
    @Test
    public void testViewDescriptorsClassesAreChanged() {

        final int numberOfViews = 5;
        final ViewDescriptor[] viewDescriptors = this.getViewDescriptorRegistry().getViewDescriptors();

        // Every view descriptor implements VLDockingViewDescriptor in spite of being defined with other implementor
        TestCase.assertEquals(numberOfViews, viewDescriptors.length);
        for (ViewDescriptor viewDescriptor : viewDescriptors) {
            TestCase.assertTrue(VLDockingViewDescriptor.class.isAssignableFrom(viewDescriptor.getClass()));
        }
    }

    /**
     * Tests the correct behaviour of view descriptor template employment.
     */
    @Test
    public void testViewDescriptorsTemplatesAreExpected() {

        this.initializeVariables(this.getPersonPageDescriptor());

        // The test view descriptors
        final VLDockingViewDescriptor masViewDesc = (VLDockingViewDescriptor) this.getMasterView().getDescriptor();
        final VLDockingViewDescriptor detViewDesc = (VLDockingViewDescriptor) this.getDetailView().getDescriptor();
        final VLDockingViewDescriptor seaViewDesc = (VLDockingViewDescriptor) this.getSearchView().getDescriptor();
        final VLDockingViewDescriptor valViewDesc = (VLDockingViewDescriptor) this.getValidationView().getDescriptor();
        final VLDockingViewDescriptor iniViewDesc = (VLDockingViewDescriptor) this.getInitialView().getDescriptor();

        // The templates to be used
        final VLDockingViewDescriptor masTmp = this.getTemplate(DefaultApplicationPageConfigurer.BbViewType.MASTER);
        final VLDockingViewDescriptor detTmp = this.getTemplate(DefaultApplicationPageConfigurer.BbViewType.DETAIL);
        final VLDockingViewDescriptor seaTmp = this.getTemplate(DefaultApplicationPageConfigurer.BbViewType.SEARCH);
        final VLDockingViewDescriptor valTmp = this.getTemplate(DefaultApplicationPageConfigurer.BbViewType.VALIDATION);
        final VLDockingViewDescriptor iniTmp = this.getTemplate(DefaultApplicationPageConfigurer.BbViewType.UNKNOWN);

        // Ensure returned view descriptors employs the expected templates
        this.doTestTemplatesAreExpected(masTmp, masViewDesc);
        this.doTestTemplatesAreExpected(detTmp, detViewDesc);
        this.doTestTemplatesAreExpected(seaTmp, seaViewDesc);
        this.doTestTemplatesAreExpected(valTmp, valViewDesc);
        this.doTestTemplatesAreExpected(iniTmp, iniViewDesc);

    }

    /**
     * Gets the template to be employed for the given view type.
     * 
     * @param type
     *            the view type.
     * @return the template.
     */
    protected final VLDockingViewDescriptor getTemplate(DefaultApplicationPageConfigurer.BbViewType type) {

        final String beanName = this.vlDockingBeanPostProcessor.getViewDescriptorsTemplates().get(type.name());

        TestCase.assertNotNull("beanName", beanName);

        return this.getApplication().getApplicationContext().getBean(beanName, VLDockingViewDescriptor.class);
    }

    /**
     * Gets the view descriptor registry.
     * 
     * @return the view descriptor registry.
     */
    protected final ViewDescriptorRegistry getViewDescriptorRegistry() {

        return (ViewDescriptorRegistry) Application.services().getService(ViewDescriptorRegistry.class);
    }

    /**
     * Checks <code>VLDockingViewDescriptor</code> parameters are the expected.
     * 
     * @param expected
     *            the expected view descriptor paramenters.
     * @param target
     *            the view descriptor to be tested.
     */
    private void doTestTemplatesAreExpected(VLDockingViewDescriptor expected, VLDockingViewDescriptor target) {

        TestCase.assertEquals(expected.getAutoHideBorder(), target.getAutoHideBorder());
        TestCase.assertEquals(expected.isAutoHideEnabled(), target.isAutoHideEnabled());
        TestCase.assertEquals(expected.isCloseEnabled(), target.isCloseEnabled());
        TestCase.assertEquals(expected.isFloatEnabled(), target.isFloatEnabled());
        TestCase.assertEquals(expected.isMaximizeEnabled(), target.isMaximizeEnabled());
    }

    // TODO, (JAF), 20100407, probar un view descriptor que ya sea de tipo vldocking
}
