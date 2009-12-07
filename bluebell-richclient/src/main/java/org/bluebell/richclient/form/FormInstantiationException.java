/**
 * 
 */
package org.bluebell.richclient.form;

import java.text.MessageFormat;

import org.bluebell.richclient.exceptionhandling.BbApplicationException;
import org.springframework.util.Assert;

/**
 * Indicates that an application-level programming error or a runtime configuration error has occurred when trying to
 * instantiate a form.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class FormInstantiationException extends BbApplicationException {

    /**
     * The default error code for this kind of exceptions.
     */
    private static final String DEFAULT_ERROR_CODE = "formInstantiationException";

    /**
     * Constructs the default message for this kind of exception.
     */
    private static final MessageFormat DEFAULT_MESSAGE_FMT = new MessageFormat(
	    "Error instantiating form with id \"{0}\"");

    /**
     * Es una clase <em>serializable</em>.
     */
    private static final long serialVersionUID = 7815485053919595591L;

    /**
     * The id of the form that causes the exception.
     */
    private String formId;

    /**
     * Creates a new {@code PageCreationException} with the specified form id.
     * 
     * @param formId
     *            the form id.
     */
    public FormInstantiationException(String formId) {

	super(FormInstantiationException.DEFAULT_MESSAGE_FMT.format(new String[] { formId }),
		FormInstantiationException.DEFAULT_ERROR_CODE);
	this.setFormId(formId);
    }

    /**
     * Creates a new {@code PageCreationException} with the specified message and form id.
     * 
     * @param errorCode
     *            the error code.
     * @param formId
     *            the form id.
     */
    public FormInstantiationException(String message, String formId) {

	super(message, FormInstantiationException.DEFAULT_ERROR_CODE);
	this.setFormId(formId);
    }

    /**
     * Creates a new {@code PageCreationException} with the specified message, error code and form id.
     * 
     * @param message
     *            the detail message.
     * @param errorCode
     *            the error code.
     * @param formId
     *            the form id.
     */
    public FormInstantiationException(String message, String errorCode, String formId) {

	super(message, errorCode);
	this.setFormId(formId);
    }

    /**
     * Creates a new {@code PageCreationException} with the specified message, nested exception and form id.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     * @param formId
     *            the form id.
     */
    public FormInstantiationException(String message, Throwable cause, String formId) {

	super(message, cause, FormInstantiationException.DEFAULT_ERROR_CODE);
	this.setFormId(formId);
    }

    /**
     * Creates a new {@code PageCreationException} with the specified message, nested exception, error code and form id.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     * @param errorCode
     *            the error code.
     * @param formId
     *            the form id.
     */
    public FormInstantiationException(String message, Throwable cause, String errorCode, String formId) {

	super(message, cause, errorCode);
	this.setFormId(formId);
    }

    /**
     * Creates a new {@code PageCreationException} with the specified nested exception and form id.
     * 
     * @param cause
     *            the nested exception.
     * @param formId
     *            the form id.
     */
    public FormInstantiationException(Throwable cause, String formId) {

	super(cause, FormInstantiationException.DEFAULT_ERROR_CODE);
	this.setFormId(formId);
    }

    /**
     * Creates a new {@code PageCreationException} with the specified nested exception, error code and form id.
     * 
     * @param cause
     *            the nested exception.
     * @param errorCode
     *            the error code.
     * @param formId
     *            the form id.
     */
    public FormInstantiationException(Throwable cause, String errorCode, String formId) {

	super(cause, errorCode);
	this.setFormId(formId);
    }

    /**
     * @return the form id.
     */
    public String getFormId() {

	// Ensure the invariant is not broken
	Assert.notNull(this.formId, "[Assertion failed] - \"formId\" is required; it must not be null");

	return this.formId;
    }

    /**
     * @param formId
     *            the form id to set.
     */
    protected void setFormId(String formId) {

	Assert.notNull(formId, "[Assertion failed] - \"formId\" is required; it must not be null");

	this.formId = formId;
    }
}
