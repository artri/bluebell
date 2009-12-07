/**
 * 
 */
package org.bluebell.richclient.exceptionhandling;

import org.springframework.core.ErrorCoded;
import org.springframework.richclient.application.ApplicationException;
import org.springframework.util.Assert;

/**
 * Extends {@link ApplicationException} in order to implement {@code ErrorCoded} interface.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
// TODO, (JAF), 20090910, this feature is made with the intention of being moved to the original class
// ApplicationException.
public class BbApplicationException extends ApplicationException implements ErrorCoded {

    /**
     * The default error code, useful to keep backwards compatibility.
     */
    private static final String DEFAULT_ERROR_CODE = "applicationException";

    /**
     * This is a <code>Serializable</code> class.
     */
    private static final long serialVersionUID = -8161115450228201788L;

    /**
     * The error code.
     * <p>
     * Useful for internationalization purposes.
     */
    private String errorCode;

    /**
     * Creates a new {@code BbApplicationException}.
     */
    public BbApplicationException() {

	super();
    }

    /**
     * Creates a new {@code BbApplicationException} with the specified error code.
     * 
     * @param errorCode
     *            the error code.
     */
    public BbApplicationException(String message) {

	super(message);
    }

    /**
     * Creates a new {@code BbApplicationException} with the specified message and error code.
     * 
     * @param message
     *            the detail message.
     * @param errorCode
     *            the error code.
     */
    public BbApplicationException(String message, String errorCode) {

	this(message);
	this.setErrorCode(errorCode);
    }

    /**
     * Creates a new {@code BbApplicationException} with the specified message and nested exception.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     */
    public BbApplicationException(String message, Throwable cause) {

	super(message, cause);
    }

    /**
     * Creates a new {@code BbApplicationException} with the specified message, nested exception and error code.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     * @param errorCode
     *            the error code.
     */
    public BbApplicationException(String message, Throwable cause, String errorCode) {

	this(message, cause);
	this.setErrorCode(errorCode);
    }

    /**
     * Creates a new {@code BbApplicationException} with the specified nested exception.
     * 
     * @param cause
     *            the nested exception.
     */
    public BbApplicationException(Throwable cause) {

	super(cause);
    }

    /**
     * Creates a new {@code BbApplicationException} with the specified nested exception and error code.
     * 
     * @param cause
     *            the nested exception.
     * @param errorCode
     *            the error code.
     */
    public BbApplicationException(Throwable cause, String errorCode) {

	this(cause);
	this.setErrorCode(errorCode);
    }

    /**
     * {@inheritDoc}
     */
    public String getErrorCode() {

	if (this.errorCode == null) {
	    this.setErrorCode(BbApplicationException.DEFAULT_ERROR_CODE);
	}

	return this.errorCode;
    }

    /**
     * @param errorCode
     *            the error code to set.
     */
    protected void setErrorCode(String errorCode) {

	Assert.notNull(errorCode, "[Assertion failed] - \"errorCode\" is required; it must not be null");

	this.errorCode = errorCode;
    }
}
