/*
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell VLDocking.
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
package org.bluebell.richclient.application.docking.vldocking;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.bluebell.richclient.application.ApplicationPageConfigurer;
import org.bluebell.richclient.util.ObjectUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.richclient.application.ViewDescriptor;
import org.springframework.richclient.application.docking.vldocking.VLDockingViewDescriptor;
import org.springframework.richclient.application.support.ApplicationServicesAccessor;
import org.springframework.util.Assert;

/**
 * A bean post processor that automatically recognizes view descriptors and make them VLDocking aware dependending on
 * view characteristics.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class VLDockingBeanPostProcessor extends ApplicationServicesAccessor implements BeanPostProcessor,
        InitializingBean, ApplicationContextAware {

    /**
     * The application context.
     */
    private ApplicationContext applicationContext;

    /**
     * The mapping between page components types and view descriptor templates.
     * <p>
     * The key is the page component type and the value the name of the prototype bean with the VLDocking view
     * descriptor template.
     * 
     * @see ApplicationPageConfigurer#getPageComponentType(org.springframework.richclient.application.PageComponentDescriptor)
     */
    private Map<String, String> viewDescriptorsTemplates;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        return bean;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Replaces those view descriptors not implementing {@link VLDockingViewDescriptor}.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (bean instanceof VLDockingViewDescriptor) {
            return bean;
        } else if (bean instanceof ViewDescriptor) {
            final ViewDescriptor sourceViewDescriptor = (ViewDescriptor) bean;
            final ViewDescriptor targetViewDescriptor = this.getTemplate(sourceViewDescriptor);

            // Copy source state
            ObjectUtil.shallowCopy(sourceViewDescriptor, targetViewDescriptor);

            return targetViewDescriptor;
        }

        return bean;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Assert.notNull(this.getViewDescriptorsTemplates(), "this.getViewDescriptorsTemplates");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        Assert.notNull(applicationContext, "applicationContext");

        this.applicationContext = applicationContext;
    }

    /**
     * Sets the view descriptors templates mapping.
     * 
     * @param viewDescriptorsTemplates
     *            the mapping.
     */
    public final void setViewDescriptorsTemplates(Map<String, String> viewDescriptorsTemplates) {

        Assert.notNull(viewDescriptorsTemplates, "viewDescriptorsTemplates");

        this.viewDescriptorsTemplates = viewDescriptorsTemplates;
    }

    /**
     * Gets the application context.
     * 
     * @return the application context.
     */
    protected final ApplicationContext getApplicationContext() {

        return this.applicationContext;
    }

    /**
     * Gets the view descriptors templates mapping.
     * 
     * @return the mapping.
     */
    protected final Map<String, String> getViewDescriptorsTemplates() {

        if (this.viewDescriptorsTemplates == null) {
            this.viewDescriptorsTemplates = new HashMap<String, String>();
        }

        return this.viewDescriptorsTemplates;
    }

    /**
     * Gets the configured template for the given view descriptor.
     * 
     * @param viewDescriptor
     *            the view descriptor.
     * @return the more suitable template.
     */
    private VLDockingViewDescriptor getTemplate(ViewDescriptor viewDescriptor) {

        Assert.notNull(viewDescriptor, "viewDescriptor");

        final VLDockingViewDescriptor vlDockingViewDescriptor;

        // Obtain the page component type
        final ApplicationPageConfigurer<?> applicationPageConfigurer = // 
        (ApplicationPageConfigurer<?>) this.getService(ApplicationPageConfigurer.class);

        final String pageComponentType = applicationPageConfigurer.getPageComponentType(viewDescriptor);

        // Obtain the template name
        final String templateName = this.getViewDescriptorsTemplates().get(pageComponentType);

        if (templateName != null) {
            // ApplicationContext#getBean(String, Object[]) ensures target bean scope is prototype
            vlDockingViewDescriptor = (VLDockingViewDescriptor) //
            this.getApplicationContext().getBean(templateName, ArrayUtils.EMPTY_OBJECT_ARRAY);
        } else {
            vlDockingViewDescriptor = new VLDockingViewDescriptor();
        }

        return vlDockingViewDescriptor;
    }
}
