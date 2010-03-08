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
    private static final String DEFAULT_ERROR_CODE = "applicationPageException";

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
     * @param message
     *            the message.
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
     * Gets the page id.
     * 
     * @return the page id.
     */
    public String getPageId() {

        // Ensure the invariant is not broken
        Assert.notNull(this.pageId, "[Assertion failed] - \"pageId\" is required; it must not be null");

        return this.pageId;
    }

    /**
     * Sets the page id.
     * 
     * @param pageId
     *            the page id to set.
     */
    protected void setPageId(String pageId) {

        Assert.notNull(pageId, "[Assertion failed] - \"pageId\" is required; it must not be null");

        this.pageId = pageId;
    }
}
