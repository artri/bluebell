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

import java.util.List;
import java.util.Map;

import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageComponentDescriptor;

/**
 * Service responsible for configuring an application page.
 * 
 * @param <T>
 *            the type of the entities to be managed.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public interface ApplicationPageConfigurer<T> {

    /**
     * Configures an application page.
     * 
     * @param applicationPage
     *            the page to be configured.
     */
    void configureApplicationPage(ApplicationPage applicationPage);

    /**
     * Classify page components according to an specific criteria.
     * <p>
     * Component type is a <code>String</code> since implementors can specify their own classification.
     * 
     * @param applicationPage
     *            the page to be classified.
     * 
     * @return the page components classification (never returns <code>null</code>).
     * 
     * @see #getPageComponentType(PageComponent)
     */
    Map<String, List<? extends PageComponent>> classifyApplicationPage(ApplicationPage applicationPage);

    /**
     * Gets the page component type.
     * 
     * @param pageComponentDescriptor
     *            the page component descriptor.
     * @return the type (never returns <code>null</code>).
     * 
     * @see #classifyApplicationPage(ApplicationPage)
     */
    String getPageComponentType(PageComponentDescriptor pageComponentDescriptor);
}
