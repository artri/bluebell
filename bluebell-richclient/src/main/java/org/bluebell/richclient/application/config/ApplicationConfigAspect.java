/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
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
package org.bluebell.richclient.application.config;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.richclient.application.support.ApplicationServicesAccessor;

/**
 * 
 * 
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
// @Aspect
public class ApplicationConfigAspect extends ApplicationServicesAccessor implements BeanPostProcessor {

    // (JAF), 20101220, end this class.

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfigAspect.class);

    /**
     * Checks message source hierarchy is as expected. If doesn't then tries to solve the problem.
     * 
     * @param targetMessageSource
     *            the target message source.
     */
    public final void checkMessageSourceHierarchy(MessageSource targetMessageSource) {

        final Set<MessageSource> messageSources = new HashSet<MessageSource>();
        final StringBuffer sb = new StringBuffer();

        HierarchicalMessageSource childMessageSource = null;
        MessageSource parentMessageSource = targetMessageSource;
        while (parentMessageSource != null) {

            sb.append(ObjectUtils.identityToString(parentMessageSource));
            sb.append(">>");

            if (!messageSources.add(parentMessageSource)) {
                ApplicationConfigAspect.LOGGER.warn("OEOEOOEOEO");

                parentMessageSource = null;
                childMessageSource.setParentMessageSource(parentMessageSource);
            } else if (parentMessageSource instanceof HierarchicalMessageSource) {

                childMessageSource = (HierarchicalMessageSource) parentMessageSource;
                parentMessageSource = childMessageSource.getParentMessageSource();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) { // throws BeansException {

        return bean;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) { // throws BeansException {

        if ("messageSource".equals(beanName)) {
            this.checkMessageSourceHierarchy((MessageSource) bean);
        }

        return bean;
    }
}
