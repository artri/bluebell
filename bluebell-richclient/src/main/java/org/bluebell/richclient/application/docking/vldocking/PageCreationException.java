/**
 * 
 */
package org.bluebell.richclient.application.docking.vldocking;

import java.text.MessageFormat;

import org.bluebell.richclient.exceptionhandling.BbApplicationException;
import org.springframework.util.Assert;

/**
 * Indicates that an application-level programming error or a runtime configuration error has occurred when trying to
 * create a page.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class PageCreationException extends BbApplicationException {

    // TODO, ¿está esta excepción en el paquete correcto??
    
    /**
     * The default error code for this kind of exceptions.
     */
    private static final String DEFAULT_ERROR_CODE = "pageCreationException";

    /**
     * Constructs the default message for this kind of exception.
     */
    private static final MessageFormat DEFAULT_MESSAGE_FMT = new MessageFormat("Error creating page with id \"{0}\"");

    /**
     * Implements <code>Serializable</code>.
     */
    private static final long serialVersionUID = -3272948924428033851L;

    /**
     * The id of the page that causes the exception.
     */
    private String pageId;

    /**
     * Creates a new {@code PageCreationException} with the specified page id.
     * 
     * @param pageId
     *            the page id.
     */
    public PageCreationException(String pageId) {

	super(PageCreationException.DEFAULT_MESSAGE_FMT.format(new String[] { pageId }),
		PageCreationException.DEFAULT_ERROR_CODE);
	this.setPageId(pageId);
    }

    /**
     * Creates a new {@code PageCreationException} with the specified message and page id.
     * 
     * @param errorCode
     *            the error code.
     * @param pageId
     *            the page id.
     */
    public PageCreationException(String message, String pageId) {

	super(message, PageCreationException.DEFAULT_ERROR_CODE);
	this.setPageId(pageId);
    }

    /**
     * Creates a new {@code PageCreationException} with the specified message, error code and page id.
     * 
     * @param message
     *            the detail message.
     * @param errorCode
     *            the error code.
     * @param pageId
     *            the page id.
     */
    public PageCreationException(String message, String errorCode, String pageId) {

	super(message, errorCode);
	this.setPageId(pageId);
    }

    /**
     * Creates a new {@code PageCreationException} with the specified message, nested exception and page id.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     * @param pageId
     *            the page id.
     */
    public PageCreationException(String message, Throwable cause, String pageId) {

	super(message, cause, PageCreationException.DEFAULT_ERROR_CODE);
	this.setPageId(pageId);
    }

    /**
     * Creates a new {@code PageCreationException} with the specified message, nested exception, error code and page id.
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
    public PageCreationException(String message, Throwable cause, String errorCode, String pageId) {

	super(message, cause, errorCode);
	this.setPageId(pageId);
    }

    /**
     * Creates a new {@code PageCreationException} with the specified nested exception and page id.
     * 
     * @param cause
     *            the nested exception.
     * @param pageId
     *            the page id.
     */
    public PageCreationException(Throwable cause, String pageId) {

	super(cause, PageCreationException.DEFAULT_ERROR_CODE);
	this.setPageId(pageId);
    }

    /**
     * Creates a new {@code PageCreationException} with the specified nested exception, error code and page id.
     * 
     * @param cause
     *            the nested exception.
     * @param errorCode
     *            the error code.
     * @param pageId
     *            the page id.
     */
    public PageCreationException(Throwable cause, String errorCode, String pageId) {

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
