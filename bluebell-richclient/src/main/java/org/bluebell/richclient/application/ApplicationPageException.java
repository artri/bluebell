/**
 * 
 */
package org.bluebell.richclient.application;

import org.bluebell.richclient.exceptionhandling.BbApplicationException;
import org.springframework.util.Assert;

/**
 * Indicates that an application-level programming error or a runtime configuration error has occurred while dealing
 * with pages.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ApplicationPageException extends BbApplicationException {

    /**
     * The default error code for this kind of exceptions.
     */
    private static final String DEFAULT_ERROR_CODE = "ApplicationPageException";

    /**
     * Implements <code>Serializable</code>.
     */
    private static final long serialVersionUID = -3272948924428033851L;

    /**
     * The id of the page that causes the exception.
     */
    private String pageId;

    /**
     * Creates a new {@code ApplicationPageException} with the specified message and page id.
     * 
     * @param errorCode
     *            the error code.
     * @param pageId
     *            the page id.
     */
    public ApplicationPageException(String message, String pageId) {

	super(message, ApplicationPageException.DEFAULT_ERROR_CODE);
	this.setPageId(pageId);
    }

    /**
     * Creates a new {@code ApplicationPageException} with the specified message, error code and page id.
     * 
     * @param message
     *            the detail message.
     * @param errorCode
     *            the error code.
     * @param pageId
     *            the page id.
     */
    public ApplicationPageException(String message, String errorCode, String pageId) {

	super(message, errorCode);
	this.setPageId(pageId);
    }

    /**
     * Creates a new {@code ApplicationPageException} with the specified message, nested exception and page id.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     * @param pageId
     *            the page id.
     */
    public ApplicationPageException(String message, Throwable cause, String pageId) {

	super(message, cause, ApplicationPageException.DEFAULT_ERROR_CODE);
	this.setPageId(pageId);
    }

    /**
     * Creates a new {@code ApplicationPageException} with the specified message, nested exception, error code and page
     * id.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     * @param errorCode
     *            the error code.
     * @param pageId
     *            the page id.
     */
    public ApplicationPageException(String message, Throwable cause, String errorCode, String pageId) {

	super(message, cause, errorCode);
	this.setPageId(pageId);
    }

    /**
     * Creates a new {@code ApplicationPageException} with the specified nested exception and page id.
     * 
     * @param cause
     *            the nested exception.
     * @param pageId
     *            the page id.
     */
    public ApplicationPageException(Throwable cause, String pageId) {

	super(cause, ApplicationPageException.DEFAULT_ERROR_CODE);
	this.setPageId(pageId);
    }

    /**
     * Creates a new {@code ApplicationPageException} with the specified nested exception, error code and page id.
     * 
     * @param cause
     *            the nested exception.
     * @param errorCode
     *            the error code.
     * @param pageId
     *            the page id.
     */
    public ApplicationPageException(Throwable cause, String errorCode, String pageId) {

	super(cause, errorCode);
	this.setPageId(pageId);
    }

    /**
     * @return the page id.
     */
    public String getPageId() {

	// Ensure the invariant is not broken
	Assert.notNull(this.pageId, "[Assertion failed] - \"pageId\" is required; it must not be null");

	return this.pageId;
    }

    /**
     * @param pageId
     *            the page id to set.
     */
    protected void setPageId(String pageId) {

	Assert.notNull(pageId, "[Assertion failed] - \"pageId\" is required; it must not be null");

	this.pageId = pageId;
    }
}
