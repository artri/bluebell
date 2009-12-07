/**
 * 
 */
package org.bluebell.binding.validation.support;

import java.util.Locale;

import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.validation.ValidationMessage;
import org.springframework.binding.validation.support.DefaultValidationMessage;
import org.springframework.context.MessageSource;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.core.Severity;
import org.springframework.util.Assert;

/**
 * 
 *
 */
public class BbValidationMessage extends DefaultValidationMessage {

    /**
     * This is a <code>Serializable</code> class.
     */
    private static final long serialVersionUID = -1733996868494711018L;

    private ValidatingFormModel formModel;

    /**
     * @param property
     * @param severity
     * @param message
     */
    public BbValidationMessage(String property, Severity severity, String message, ValidatingFormModel formModel) {

	super(property, severity, message);
	this.setFormModel(formModel);
    }

    /**
     * Gets the source.
     * 
     * @return the source
     */
    public String getSource() {

	final MessageSource messageSource = Application.instance().getApplicationContext();
	final String code = this.getFormModel().getId() + ".caption";

	return messageSource.getMessage(code, new String[0], this.getFormModel().getId(), Locale.getDefault());
    }

    /**
     * Gets the formModel.
     * 
     * @return the formModel
     */
    public ValidatingFormModel getFormModel() {

	return this.formModel;
    }

    /**
     * Sets the formModel.
     * 
     * @param formModel
     *            the formModel to set
     */
    public void setFormModel(ValidatingFormModel formModel) {

	Assert.notNull(formModel, "formModel");

	this.formModel = formModel;
    }

    public static BbValidationMessage createValidationMessage(ValidationMessage copyFrom, ValidatingFormModel formModel) {

	return new BbValidationMessage(copyFrom.getProperty(), copyFrom.getSeverity(), copyFrom.getMessage(), formModel);
    }
}
