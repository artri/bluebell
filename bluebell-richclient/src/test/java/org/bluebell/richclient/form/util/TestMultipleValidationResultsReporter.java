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
package org.bluebell.richclient.form.util;

import java.util.List;

import javax.swing.JTextField;

import org.bluebell.binding.validation.support.BbValidationMessage;
import org.bluebell.richclient.samples.simple.form.PersonChildForm;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.validation.ValidationMessage;
import org.springframework.richclient.application.PageDescriptor;

/**
 * Tests the correct behaviour of {@link org.bluebell.richclient.form.MultipleValidationResultsReporter}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestMultipleValidationResultsReporter extends AbstractBbSamplesTests {

    /**
     * The detail form model.
     */
    private ValidatingFormModel detailFormModel;

    /**
     * The internal dispatcher form model.
     */
    private ValidatingFormModel dispatcherFormModel;

    /**
     * The master form model.
     */
    private ValidatingFormModel masterFormModel;

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void testDependencyInjection() {

        super.testDependencyInjection();

        Assert.assertNotNull(this.masterFormModel);
        Assert.assertNotNull(this.dispatcherFormModel);
        Assert.assertNotNull(this.detailFormModel);
    }

    /**
     * Tests the correct behaviour of validation and binding errors reporting mechanism according to <a
     * href="http://forum.springsource.org/showthread.php?t=78508">this post</a>.
     */
    @Test
    public void testBindingErrors() {

        final PersonChildForm form = new PersonChildForm();
        // final ValidatingFormModel formModel =
        // FormModelHelper.createFormModel(new Person());

        // After executing new form object command validation messages are empty
        form.getNewFormObjectCommand().execute();
        Assert.assertEquals(0, form.getFormModel().getValidationResults().getMessageCount());

        // Set a illegal age and a validation error is raised
        this.userAction(form, "age", "Illegal Value");
        Assert.assertEquals(1, form.getFormModel().getValidationResults().getMessageCount());

        // Execute the reset command and the count backs to 0
        form.reset();
        Assert.assertEquals(0, form.getFormModel().getValidationResults().getMessageCount());

        // After executing new form object command the count is 0 (whithout
        // fixing the error the count backs to 1!!!!)
        form.getNewFormObjectCommand().execute();
        Assert.assertEquals(0, form.getFormModel().getValidationResults().getMessageCount());
    }

    /**
     * Tests that validation messages are correctly retrieved after forcing an error on a form.
     */
    @Test
    public void testSimplestValidationCase() {

        this.initializeVariables(this.getPersonPageDescriptor());

        // After executing new form object command validation messages are empty
        this.getBackingForm(this.getMasterView()).getNewFormObjectCommand().execute();
        Assert.assertEquals(0, this.detailFormModel.getValidationResults().getMessageCount());

        // Set a illegal age and a validation error is raised
        this.userAction(this.getBackingForm(this.getDetailView()), "age", "IllegalValue");
        Assert.assertEquals(1, this.getValidationMessages().size());
        Assert.assertEquals("age", this.getValidationMessages().get(0).getProperty());

        // Execute the cancel command and the count backs to 0
        this.getBackingForm(this.getMasterView()).getCancelCommand().execute();
        Assert.assertEquals(0, this.getValidationMessages().size());
    }

    /**
     * Tests if validation feature is correctly enabled/disabled in each form.
     */
    @Test
    public void testValidatingState() {

        this.initializeVariables(this.getPersonPageDescriptor());

        // Initial state
        Assert.assertFalse(this.masterFormModel.isValidating());
        Assert.assertFalse(this.dispatcherFormModel.isValidating());
        Assert.assertFalse(this.detailFormModel.isValidating());

        // After executing new form object command
        this.getBackingForm(this.getMasterView()).getNewFormObjectCommand().execute();

        Assert.assertFalse(this.masterFormModel.isValidating());
        Assert.assertTrue(this.dispatcherFormModel.isValidating());
        Assert.assertTrue(this.detailFormModel.isValidating());

        // After executing cancel command
        this.getBackingForm(this.getMasterView()).getCancelCommand().execute();

        Assert.assertFalse(this.masterFormModel.isValidating());
        Assert.assertFalse(this.dispatcherFormModel.isValidating());
        Assert.assertFalse(this.detailFormModel.isValidating());
    }

    /**
     * Tests if validation messages are correctly retrieved after forcing an error on a sibling form.
     */
    @Test
    public void testValidationWithSiblingForm() {

        this.initializeVariables(this.getPersonPageDescriptor());

        // Atach a new child form
        final PersonChildForm siblingForm = new PersonChildForm("siblingForm");
        this.getBackingForm(this.getMasterView()).addChildForm(siblingForm);

        // After executing new form object command validation messages are empty
        this.getBackingFormModel(this.getDetailView()).getValidationResults().getMessages();
        this.getBackingForm(this.getMasterView()).getNewFormObjectCommand().execute();
        Assert.assertEquals(0, this.getValidationMessages().size());

        // Set a illegal age on detail form and a validation message is raised
        this.userAction(this.getBackingForm(this.getDetailView()), "age", "IllegalValue");
        Assert.assertEquals(1, this.getValidationMessages().size());
        Assert.assertEquals(1, this.dispatcherFormModel.getValidationResults().getMessageCount());
        Assert.assertEquals(1, this.detailFormModel.getValidationResults().getMessageCount());
        Assert.assertEquals(0, siblingForm.getFormModel().getValidationResults().getMessageCount());

        // Set a illegal age on sibling form and other validation message is
        // raised
        ((JTextField) this.getComponentNamed(siblingForm, "age")).setText("Illegal Value");
        Assert.assertEquals(2, this.getValidationMessages().size());
        Assert.assertEquals(2, this.dispatcherFormModel.getValidationResults().getMessageCount());
        Assert.assertEquals(1, this.detailFormModel.getValidationResults().getMessageCount());
        Assert.assertEquals(1, siblingForm.getFormModel().getValidationResults().getMessageCount());

        // Test validation messages are the expected
        Assert.assertEquals(2, this.getValidationMessages().size());
        final BbValidationMessage firstValidationMessage = (BbValidationMessage) this.getValidationMessages().get(0);
        final BbValidationMessage secondValidationMessage = (BbValidationMessage) this.getValidationMessages().get(1);
        Assert.assertEquals("age", firstValidationMessage.getProperty());
        Assert.assertEquals("age", secondValidationMessage.getProperty());

        final Boolean b1 = "siblingForm".equals(firstValidationMessage.getFormModel().getId());
        final Boolean b2 = "personDetailForm".equals(secondValidationMessage.getFormModel().getId());
        final Boolean b3 = "personDetailForm".equals(firstValidationMessage.getFormModel().getId());
        final Boolean b4 = "siblingForm".equals(secondValidationMessage.getFormModel().getId());
        Assert.assertTrue(b1 && b2 || b3 && b4);

        // Execute the cancel command and the count backs to 0
        this.getBackingForm(this.getMasterView()).getCancelCommand().execute();
        Assert.assertEquals(0, this.getValidationMessages().size());
        Assert.assertEquals(0, this.dispatcherFormModel.getValidationResults().getMessageCount());
        Assert.assertEquals(0, this.detailFormModel.getValidationResults().getMessageCount());
        Assert.assertEquals(0, siblingForm.getFormModel().getValidationResults().getMessageCount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeVariables(PageDescriptor pageDescriptor) {

        super.initializeVariables(pageDescriptor);

        this.masterFormModel = this.getBackingFormModel(this.getMasterView());
        this.detailFormModel = this.getBackingFormModel(this.getDetailView());
        this.dispatcherFormModel = (ValidatingFormModel) this.detailFormModel.getParent();

        // Ensure detail form model is not linked with master form model
        Assert.assertNull(this.dispatcherFormModel.getParent());
    }

    /**
     * {@inheritDoc}
     */
    @After
    public void cancel() throws Exception {

        if (this.getMasterView() != null) {
            this.getBackingForm(this.getMasterView()).getCancelCommand().execute();
        }
    }

    /**
     * Gets the validation messages reported by the validation view.
     * 
     * @return the validation messages.
     */
    private List<ValidationMessage> getValidationMessages() {

        return this.getBackingForm(this.getValidationView()).getValidationMessages();
    }
}
