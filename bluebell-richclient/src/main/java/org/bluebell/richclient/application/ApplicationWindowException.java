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
 * with windows.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ApplicationWindowException extends BbApplicationException {

    /**
     * The default error code for this kind of exceptions.
     */
    private static final String DEFAULT_ERROR_CODE = "applicationWindowException";

    /**
     * Implements <code>Serializable</code>.
     */
    private static final long serialVersionUID = -672534048521870906L;

    /**
     * The id of the window that causes the exception.
     */
    private String windowId;

    /**
     * Creates a new {@code ApplicationWindowException} with the specified message and window id.
     * 
     * @param message
     *            the message.
     * @param windowId
     *            the window id.
     */
    public ApplicationWindowException(String message, String windowId) {

        super(message, ApplicationWindowException.DEFAULT_ERROR_CODE);
        this.setWindowId(windowId);
    }

    /**
     * Creates a new {@code ApplicationWindowException} with the specified message, error code and window id.
     * 
     * @param message
     *            the detail message.
     * @param errorCode
     *            the error code.
     * @param windowId
     *            the window id.
     */
    public ApplicationWindowException(String message, String errorCode, String windowId) {

        super(message, errorCode);
        this.setWindowId(windowId);
    }

    /**
     * Creates a new {@code ApplicationWindowException} with the specified message, nested exception and window id.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     * @param windowId
     *            the window id.
     */
    public ApplicationWindowException(String message, Throwable cause, String windowId) {

        super(message, cause, ApplicationWindowException.DEFAULT_ERROR_CODE);
        this.setWindowId(windowId);
    }

    /**
     * Creates a new {@code ApplicationWindowException} with the specified message, nested exception, error code and
     * window id.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     * @param errorCode
     *            the error code.
     * @param windowId
     *            the window id.
     */
    public ApplicationWindowException(String message, Throwable cause, String errorCode, String windowId) {

        super(message, cause, errorCode);
        this.setWindowId(windowId);
    }

    /**
     * Creates a new {@code ApplicationWindowException} with the specified nested exception and window id.
     * 
     * @param cause
     *            the nested exception.
     * @param windowId
     *            the window id.
     */
    public ApplicationWindowException(Throwable cause, String windowId) {

        super(cause, ApplicationWindowException.DEFAULT_ERROR_CODE);
        this.setWindowId(windowId);
    }

    /**
     * Creates a new {@code ApplicationWindowException} with the specified nested exception, error code and window id.
     * 
     * @param cause
     *            the nested exception.
     * @param errorCode
     *            the error code.
     * @param windowId
     *            the window id.
     */
    public ApplicationWindowException(Throwable cause, String errorCode, String windowId) {

        super(cause, errorCode);
        this.setWindowId(windowId);
    }

    /**
     * Gets the window id.
     * 
     * @return the window id.
     */
    public String getWindowId() {

        // Ensure the invariant is not broken
        Assert.notNull(this.windowId, "[Assertion failed] - \"windowId\" is required; it must not be null");

        return this.windowId;
    }

    /**
     * Sets the window id.
     * 
     * @param windowId
     *            the window id to set.
     */
    protected void setWindowId(String windowId) {

        Assert.notNull(windowId, "[Assertion failed] - \"windowId\" is required; it must not be null");

        this.windowId = windowId;
    }
}
