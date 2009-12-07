package org.bluebell.richclient.form;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.bluebell.binding.validation.support.BbValidationMessage;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.validation.ValidationMessage;
import org.springframework.binding.validation.ValidationResults;
import org.springframework.richclient.dialog.Messagable;
import org.springframework.richclient.form.SimpleValidationResultsReporter;
import org.springframework.util.Assert;

/**
 * Extension of <code>SimpleValidationResultsReporter</code> that reports not only a single validation message but also
 * all the messages from the configured form model and children.
 * <p>
 * More details of the searching process can be found in the {@link #getValidationMessages()} method.
 * 
 * @see SimpleValidationResultsReporter
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class MultipleValidationResultsReporter extends SimpleValidationResultsReporter {

    // TODO, no heredar de SimpleValidationResultsReporter, sino que copiar su código. La herencia obliga a un par de
    // hacks...

    /**
     * The form model to be reported.
     */
    private ValidatingFormModel formModel;

    private Messagable messageReceiver;

    /**
     * Creates the reporter given the form model.
     */
    public MultipleValidationResultsReporter(ValidatingFormModel formModel, Messagable messageReceiver) {

	super(formModel.getValidationResults(), messageReceiver);

	this.setFormModel(formModel);
	this.setMessageReceiver(messageReceiver);
    }

    /**
     * @return the formModel
     */
    public ValidatingFormModel getFormModel() {

	return this.formModel;
    }

    /**
     * Gets the messageReceiver.
     * 
     * @return the messageReceiver
     */
    public Messagable getMessageReceiver() {

	return this.messageReceiver;
    }

    /**
     * Get the messages to be reported.
     * <p>
     * Searching only takes into account messages reported by leaf form models in hierarchy.
     * 
     * @return the messages reported by every leaf form model in hierarchy.
     */
    public Map<ValidatingFormModel, Collection<ValidationMessage>> getValidationMessages() {

	final Map<ValidatingFormModel, Collection<ValidationMessage>> messages = new HashMap<ValidatingFormModel, Collection<ValidationMessage>>();

	if (this.getFormModel() != null) {
	    // May be null during creation process
	    this.getValidationMessages(this.getFormModel(), messages);
	}

	return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validationResultsChanged(ValidationResults results) {

	if (this.getMessageReceiver() == null) {
	    // This is the first invocation, message receiver is not set yet
//	    super.validationResultsChanged(results);
	    return;
	}

	// Obtain validation messages
	final Map<ValidatingFormModel, Collection<ValidationMessage>> allMessages = this.getValidationMessages();

	// Reset valition messages
	this.getMessageReceiver().setMessage(null);

	// Iterates between messages building new form model aware message instances and reporting the message receiver
	for (final Map.Entry<ValidatingFormModel, Collection<ValidationMessage>> entry : allMessages.entrySet()) {

	    final ValidatingFormModel formModel = entry.getKey();
	    final Collection<ValidationMessage> formModelMessages = entry.getValue();

	    // Since this is a multiple validation results reporter, message receiver is notified one time per message
	    for (final ValidationMessage message : formModelMessages) {

		final ValidationMessage messageToSet = BbValidationMessage.createValidationMessage(message, formModel);
		this.getMessageReceiver().setMessage(messageToSet);
	    }
	}
    }

    /**
     * Get the messages that should be reported.
     * <p>
     * Searching only takes into account messages reported by leaf form models in hierarchy.
     * 
     * @param formModel
     *            the root form model where start searching.
     * @param messages
     *            the messages reported until now grouped by form model.
     */
    @SuppressWarnings("unchecked")
    private void getValidationMessages(FormModel formModel,
	    Map<ValidatingFormModel, Collection<ValidationMessage>> messages) {

	final Boolean isHierarchical = ClassUtils.isAssignable(formModel.getClass(), HierarchicalFormModel.class);
	final Boolean isLeaf = !isHierarchical || ArrayUtils.isEmpty(((HierarchicalFormModel) formModel).getChildren());
	final Boolean isValidating = ClassUtils.isAssignable(formModel.getClass(), ValidatingFormModel.class);

	// Base case
	if (isLeaf && isValidating) {
	    final ValidatingFormModel validatingFormModel = (ValidatingFormModel) formModel;

	    messages.put(validatingFormModel, validatingFormModel.getValidationResults().getMessages());
	}

	// Recursive case: inspect every child form model
	if (isHierarchical) {
	    for (final FormModel childFormModel : ((HierarchicalFormModel) formModel).getChildren()) {
		this.getValidationMessages(childFormModel, messages);
	    }
	}
    }

    /**
     * @param formModel
     *            the formModel to set
     */
    private void setFormModel(ValidatingFormModel formModel) {

	Assert.notNull(formModel, "formModel");

	this.formModel = formModel;
    }

    /**
     * Sets the messageReceiver.
     * 
     * @param messageReceiver
     *            the messageReceiver to set
     */
    private void setMessageReceiver(Messagable messageReceiver) {

	Assert.notNull(messageReceiver, "messageReceiver");

	this.messageReceiver = messageReceiver;
    }
}