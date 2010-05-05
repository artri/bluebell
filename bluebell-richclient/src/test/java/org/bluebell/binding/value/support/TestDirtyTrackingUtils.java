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

package org.bluebell.binding.value.support;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.bluebell.richclient.form.builder.support.DirtyTrackingUtils;
import org.bluebell.richclient.test.AbstractBbRichClientTests;
import org.junit.Test;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.form.support.DefaultFormModel;
import org.springframework.test.context.ContextConfiguration;

/**
 * Class that tests the correct behavioir of <code>DirtyTrackingUtils</code>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration
public class TestDirtyTrackingUtils extends AbstractBbRichClientTests {

    /**
     * The parent form model identifier to be used.
     */
    private static final String PARENT_FORM_MODEL_ID = "parentFormModel";

    /**
     * The child form model identifier to be used.
     */
    private static final String CHILD_FORM_MODEL_ID = "childFormModel";

    /**
     * The internationalization properties.
     */
    @Resource
    private Properties testDirtyTrackingUtilsProperties;

    /**
     * Constructs the tests and changes the message source to be used.
     */
    public TestDirtyTrackingUtils() {

        System.setProperty("richclient.messageSource", "messageSource");
    }

    /**
     * Tests the correct behaviour of dependency injection.
     */
    @Test
    public void testDependencyInjection() {

        TestCase.assertNotNull(this.testDirtyTrackingUtilsProperties);
    }

    /**
     * Tests the correct behaviour of
     * {@link DirtyTrackingUtils#getI18nDirtyProperties(org.springframework.binding.form.FormModel)} .
     */
    @Test
    public void testGetI18nDirtyProperties() {

        final int dirtyValueModels = 4;

        final FormModel formModel = this.createFormModel();

        // There should be 4 dirty value models
        final Set<String[]> i18nDirtyProperties = DirtyTrackingUtils.getI18nDirtyProperties(formModel);
        TestCase.assertEquals(dirtyValueModels, i18nDirtyProperties.size());
    }

    /**
     * Tests the correct behaviour of {@link DirtyTrackingUtils#clearDirty(org.springframework.binding.form.FormModel)}
     * .
     */
    @Test
    public void testClearDirtyFormModel() {

        final FormModel formModel = this.createFormModel();

        // Clear dirty and check there isn't any dirty value model
        DirtyTrackingUtils.clearDirty(formModel);

        final Set<String[]> i18nDirtyProperties = DirtyTrackingUtils.getI18nDirtyProperties(formModel);

        TestCase.assertTrue(i18nDirtyProperties.isEmpty());
    }

    /**
     * Tests the correct behaviour of {@link DirtyTrackingUtils#clearDirty(org.springframework.binding.form.FormModel)}
     * .
     */
    @Test
    public void testClearDirtyValueModel() {

        final int total = 4;
        final FormModel parentFormModel = this.createFormModel();
        final FormModel childFormModel = ((HierarchicalFormModel) parentFormModel).getChildren()[0];

        Collection<String[]> dirtyProperties = DirtyTrackingUtils.getDirtyProperties(parentFormModel);

        int iter = 0;
        for (String[] dirtyProperty : dirtyProperties) {
            final String formModelId = dirtyProperty[0];
            final String propertyPath = dirtyProperty[1];

            FormModel formModel = childFormModel;
            if (TestDirtyTrackingUtils.PARENT_FORM_MODEL_ID.equals(formModelId)) {
                formModel = parentFormModel;
            }

            // Clear dirty in a single property
            DirtyTrackingUtils.clearDirty(formModel.getValueModel(propertyPath));

            // Check the number of dirty values models has decreased
            dirtyProperties = DirtyTrackingUtils.getDirtyProperties(parentFormModel);
            TestCase.assertEquals(total - (++iter), dirtyProperties.size());
        }
    }

    /**
     * Tests the correct behaviour of
     * {@link DirtyTrackingUtils#getI18nDirtyPropertiesHtmlString(org.springframework.binding.form.FormModel)} .
     */
    @Test
    public void testGetI18nDirtyPropertiesHtmlString() {

        final String parentFormModelId = TestDirtyTrackingUtils.PARENT_FORM_MODEL_ID;
        final String childFormModelId = TestDirtyTrackingUtils.CHILD_FORM_MODEL_ID;

        final String parentFormModelMsg = this.testDirtyTrackingUtilsProperties.getProperty(//
                parentFormModelId + ".caption");
        final String childFormModelMsg = this.testDirtyTrackingUtilsProperties.getProperty(//
                childFormModelId + ".caption");
        final String simplePropertyParentFormModelMsg = this.testDirtyTrackingUtilsProperties.getProperty(//
                parentFormModelId + ".message.label");
        final String compoundPropertyParentFormModelMsg = this.testDirtyTrackingUtilsProperties.getProperty(//
                parentFormModelId + ".foo.message.label");
        final String simplePropertyChildFormModelMsg = this.testDirtyTrackingUtilsProperties.getProperty(//
                childFormModelId + ".message.label");
        final String compoundPropertyChildFormModelMsg = this.testDirtyTrackingUtilsProperties.getProperty(//
                childFormModelId + ".foo.message.label");

        // Get HTML formatting for dirty value models
        final FormModel formModel = this.createFormModel();
        final String i18nDirtyPropertiesHtmlString = DirtyTrackingUtils.getI18nDirtyPropertiesHtmlString(formModel);

        // Check everything is ok
        TestCase.assertEquals(2, StringUtils.countMatches(i18nDirtyPropertiesHtmlString, //
                parentFormModelMsg));
        TestCase.assertEquals(2, StringUtils.countMatches(i18nDirtyPropertiesHtmlString, //
                childFormModelMsg));
        TestCase.assertEquals(1, StringUtils.countMatches(i18nDirtyPropertiesHtmlString, //
                simplePropertyParentFormModelMsg));
        TestCase.assertEquals(1, StringUtils.countMatches(i18nDirtyPropertiesHtmlString, // 
                compoundPropertyParentFormModelMsg));
        TestCase.assertEquals(1, StringUtils.countMatches(i18nDirtyPropertiesHtmlString, //
                simplePropertyChildFormModelMsg));
        TestCase.assertEquals(1, StringUtils.countMatches(i18nDirtyPropertiesHtmlString, //
                compoundPropertyChildFormModelMsg));
    }

    /**
     * Creates the form model used for testing.
     * 
     * @return the form model.
     */
    private FormModel createFormModel() {

        // Build and bind form models
        final DefaultFormModel parentFormModel = new DefaultFormModel(new Foo());
        final DefaultFormModel childFormModel = new DefaultFormModel(new Foo());

        parentFormModel.setId(TestDirtyTrackingUtils.PARENT_FORM_MODEL_ID);
        childFormModel.setId(TestDirtyTrackingUtils.CHILD_FORM_MODEL_ID);
        parentFormModel.addChild(childFormModel);

        // Change property vallues at parent and child form models
        parentFormModel.getValueModel("message").setValue("B");
        parentFormModel.getValueModel("foo.message").setValue("B");
        childFormModel.getValueModel("message").setValue("B");
        childFormModel.getValueModel("foo.message").setValue("B");

        return parentFormModel;
    }

    /**
     * Bean class useful for testing.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class Foo {
        /**
         * A simple property.
         */
        private String message;

        /**
         * An objecy reference.
         */
        private Foo foo;

        /**
         * Gets the message.
         * 
         * @return the message.
         */
        public String getMessage() {

            return this.message;
        }

        /**
         * Sets the message.
         * 
         * @param message
         *            the message.
         */
        public void setMessage(String message) {

            this.message = message;
        }

        /**
         * Gets the reference and if not exists then instances it.
         * 
         * @return the reference.
         */
        public Foo getFoo() {

            if (this.foo == null) {
                this.setFoo(new Foo());
            }

            return this.foo;
        }

        /**
         * Sets the object reference.
         * 
         * @param foo
         *            the object reference.
         */
        public void setFoo(Foo foo) {

            this.foo = foo;
        }
    }
}
