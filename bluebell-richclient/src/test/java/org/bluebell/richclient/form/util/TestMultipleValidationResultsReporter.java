/**
 * 
 */
package org.bluebell.richclient.form.util;

import java.awt.Component;
import java.awt.Container;
import java.util.List;

import javax.swing.JTextField;

import org.bluebell.binding.validation.support.BbValidationMessage;
import org.bluebell.richclient.form.MultipleValidationResultsReporter;
import org.bluebell.richclient.samples.simple.form.PersonChildForm;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.Assert;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.validation.ValidationMessage;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.form.Form;

/**
 * Tests the correct behaviour of {@link MultipleValidationResultsReporter}.
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
     * Tests the correct behaviour of validation and binding errors reporting mechanism according to <a
     * href="http://forum.springsource.org/showthread.php?t=78508">this post</a>.
     */
    public void testBindingErrors() {

	final PersonChildForm form = new PersonChildForm();
	// final ValidatingFormModel formModel = FormModelHelper.createFormModel(new Person());

	// After executing new form object command validation messages are empty
	form.getNewFormObjectCommand().execute();
	Assert.assertEquals(0, form.getFormModel().getValidationResults().getMessageCount());

	// Set a illegal age and a validation error is raised
	this.userAction(form, "age", "Illegal Value");
	Assert.assertEquals(1, form.getFormModel().getValidationResults().getMessageCount());

	// Execute the reset command and the count backs to 0
	form.reset();
	Assert.assertEquals(0, form.getFormModel().getValidationResults().getMessageCount());

	// After executing new form object command the count is 0 (whithout fixing the error the count backs to 1!!!!)
	form.getNewFormObjectCommand().execute();
	Assert.assertEquals(0, form.getFormModel().getValidationResults().getMessageCount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testDependencyInjection() {

	super.testDependencyInjection();
	Assert.assertNotNull(this.masterFormModel);
	Assert.assertNotNull(this.dispatcherFormModel);
	Assert.assertNotNull(this.detailFormModel);
    }

    /**
     * Tests that validation messages are correctly retrieved after forcing an error on a form.
     */
    public void testSimplestValidationCase() {

	this.initializeVariables(this.personPageDescriptor);

	// After executing new form object command validation messages are empty
	this.getBackingForm(this.masterView).getNewFormObjectCommand().execute();
	Assert.assertEquals(0, this.detailFormModel.getValidationResults().getMessageCount());

	// Set a illegal age and a validation error is raised
	this.userAction(this.getBackingForm(this.detailView), "age", "IllegalValue");
	Assert.assertEquals(1, this.getValidationMessages().size());
	Assert.assertEquals("age", this.getValidationMessages().get(0).getProperty());

	// Execute the cancel command and the count backs to 0
	this.getBackingForm(this.masterView).getCancelCommand().execute();
	Assert.assertEquals(0, this.getValidationMessages().size());
    }

    /**
     * Tests if validation feature is correctly enabled/disabled in each form.
     */
    public void testValidatingState() {

	this.initializeVariables(this.personPageDescriptor);

	// Initial state
	Assert.assertFalse(this.masterFormModel.isValidating());
	Assert.assertFalse(this.dispatcherFormModel.isValidating());
	Assert.assertFalse(this.detailFormModel.isValidating());

	// After executing new form object command
	this.getBackingForm(this.masterView).getNewFormObjectCommand().execute();

	Assert.assertFalse(this.masterFormModel.isValidating());
	Assert.assertTrue(this.dispatcherFormModel.isValidating());
	Assert.assertTrue(this.detailFormModel.isValidating());

	// After executing cancel command
	this.getBackingForm(this.masterView).getCancelCommand().execute();

	Assert.assertFalse(this.masterFormModel.isValidating());
	Assert.assertFalse(this.dispatcherFormModel.isValidating());
	Assert.assertFalse(this.detailFormModel.isValidating());
    }

    /**
     * Tests if validation messages are correctly retrieved after forcing an error on a sibling form.
     */
    public void testValidationWithSiblingForm() {

	this.initializeVariables(this.personPageDescriptor);

	// Atach a new child form
	final PersonChildForm siblingForm = new PersonChildForm("siblingForm");
	this.getBackingForm(this.masterView).addChildForm(siblingForm);

	// After executing new form object command validation messages are empty
	this.getBackingFormModel(this.detailView).getValidationResults().getMessages();
	this.getBackingForm(this.masterView).getNewFormObjectCommand().execute();
	Assert.assertEquals(0, this.getValidationMessages().size());

	// Set a illegal age on detail form and a validation message is raised
	this.userAction(this.getBackingForm(this.detailView), "age", "IllegalValue");
	Assert.assertEquals(1, this.getValidationMessages().size());
	Assert.assertEquals(1, this.dispatcherFormModel.getValidationResults().getMessageCount());
	Assert.assertEquals(1, this.detailFormModel.getValidationResults().getMessageCount());
	Assert.assertEquals(0, siblingForm.getFormModel().getValidationResults().getMessageCount());

	// Set a illegal age on sibling form and other validation message is raised
	((JTextField) this.getComponentNamed(siblingForm, "age")).setText("Illegal Value");
	Assert.assertEquals(2, this.getValidationMessages().size());
	Assert.assertEquals(2, this.dispatcherFormModel.getValidationResults().getMessageCount());
	Assert.assertEquals(1, this.detailFormModel.getValidationResults().getMessageCount());
	Assert.assertEquals(1, siblingForm.getFormModel().getValidationResults().getMessageCount());

	// Test validation messages are the expected
	final BbValidationMessage firstValidationMessage = (BbValidationMessage) this.getValidationMessages().get(0);
	final BbValidationMessage secondValidationMessage = (BbValidationMessage) this.getValidationMessages().get(1);
	junit.framework.Assert.assertEquals("age", firstValidationMessage.getProperty());
	junit.framework.Assert.assertEquals("age", secondValidationMessage.getProperty());
	junit.framework.Assert.assertEquals("siblingForm", firstValidationMessage.getFormModel().getId());
	junit.framework.Assert.assertEquals("personDetailForm", secondValidationMessage.getFormModel().getId());

	// Execute the cancel command and the count backs to 0
	this.getBackingForm(this.masterView).getCancelCommand().execute();
	Assert.assertEquals(0, this.getValidationMessages().size());
	Assert.assertEquals(0, this.dispatcherFormModel.getValidationResults().getMessageCount());
	Assert.assertEquals(0, this.detailFormModel.getValidationResults().getMessageCount());
	Assert.assertEquals(0, siblingForm.getFormModel().getValidationResults().getMessageCount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeVariables(PageDescriptor pageDescriptor) {

	super.initializeVariables(pageDescriptor);

	this.masterFormModel = this.getBackingFormModel(this.masterView);
	this.detailFormModel = this.getBackingFormModel(this.detailView);
	this.dispatcherFormModel = (ValidatingFormModel) this.detailFormModel.getParent();

	// Ensure detail form model is not linked with master form model
	Assert.assertNull(this.dispatcherFormModel.getParent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onTearDown() throws Exception {

	super.onTearDown();

	if (this.masterView != null) {
	    this.getBackingForm(this.masterView).getCancelCommand().execute();
	}
    }

    /**
     * Gets the component with the given name.
     * 
     * @param form
     *            the form containing the component.
     * @param name
     *            the name of the component.
     * @return the control, may be <code>null</code> if not found.
     */
    private Component getComponentNamed(Form form, String name) {

	return TestMultipleValidationResultsReporter.getDescendantNamed(name, form.getControl());
    }

    /**
     * Gets the validation messages reported by the validation view.
     * 
     * @return the validation messages.
     */
    private List<ValidationMessage> getValidationMessages() {

	return this.getBackingForm(this.validationView).getValidationMessages();
    }

    /**
     * Simulate an user action changing the text value of a component.
     * 
     * @param form
     *            the form containing the text control.
     * @param property
     *            the property represented by the text control.
     * @param value
     *            the new text value to set.
     */
    private void userAction(Form form, String property, String value) {

	((JTextField) this.getComponentNamed(form, property)).replaceSelection(value);
    }

    /**
     * Does a pre-order search of a component with a given name.
     * 
     * @param name
     *            the name.
     * @param parent
     *            the root component in hierarchy.
     * @return the found component (may be null).
     */
    public static Component getDescendantNamed(String name, Component parent) {

	// TODO, (JAF), mover esto de aquí.

	org.springframework.util.Assert.notNull(name, "name");
	org.springframework.util.Assert.notNull(parent, "parent");

	// Base case
	if (name.equals(parent.getName())) {

	    return parent;
	}
	// Recursive case
	else if (parent instanceof Container) {
	    for (final Component component : ((Container) parent).getComponents()) {

		final Component foundComponent = //
		TestMultipleValidationResultsReporter.getDescendantNamed(name, component);

		if (foundComponent != null) {
		    return foundComponent;
		}
	    }
	}

	return null;
    }
}
