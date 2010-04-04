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

import org.bluebell.richclient.util.ObjectUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.richclient.application.ViewDescriptor;
import org.springframework.richclient.application.docking.vldocking.VLDockingViewDescriptor;
import org.springframework.util.Assert;

/**
 * A bean post processor that automatically recognizes view descriptors and make them VLDocking aware dependending on
 * view characteristics.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class VLDockingBeanPostProcessor implements BeanPostProcessor, InitializingBean, ApplicationContextAware {

    /**
     * The application context.
     */
    private ApplicationContext applicationContext;

    private VLDockingViewDescriptor masterViewDescriptorTemplate;

    private VLDockingViewDescriptor detailViewDescriptorTemplate;

    private VLDockingViewDescriptor searchViewDescriptorTemplate;

    private VLDockingViewDescriptor validationViewDescriptorTemplate;

    private VLDockingViewDescriptor unknownViewDescriptorTemplate;

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
            final ViewDescriptor targetViewDescriptor = new VLDockingViewDescriptor();

            // Copy template properties
            ObjectUtil.shallowCopy(this.searchViewDescriptorTemplate, targetViewDescriptor);

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

        Assert.notNull(this.getMasterViewDescriptorTemplate(), "this.getMasterViewDescriptorTemplate()");
        Assert.notNull(this.getDetailViewDescriptorTemplate(), "this.getDetailViewDescriptorTemplate()");
        Assert.notNull(this.getSearchViewDescriptorTemplate(), "this.getSearchViewDescriptorTemplate()");
        Assert.notNull(this.getValidationViewDescriptorTemplate(), "this.getValidationViewDescriptorTemplate()");
        Assert.notNull(this.getUnknownViewDescriptorTemplate(), "this.getUnknownViewDescriptorTemplate()");
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
     * Sets the master view descriptor template.
     * 
     * @param masterViewDescriptorTemplate
     *            the master view descriptor template to set.
     */
    public final void setMasterViewDescriptorTemplate(VLDockingViewDescriptor masterViewDescriptorTemplate) {

        Assert.notNull(masterViewDescriptorTemplate, "masterViewDescriptorTemplate");

        this.masterViewDescriptorTemplate = masterViewDescriptorTemplate;
    }

    /**
     * Sets the search view descriptor template.
     * 
     * @param searchViewDescriptorTemplate
     *            the search viewDescriptor template to set.
     */
    public final void setSearchViewDescriptorTemplate(VLDockingViewDescriptor searchViewDescriptorTemplate) {

        Assert.notNull(searchViewDescriptorTemplate, "searchViewDescriptorTemplate");

        this.searchViewDescriptorTemplate = searchViewDescriptorTemplate;
    }

    /**
     * Sets the detail view descriptor template.
     * 
     * @param detailViewDescriptorTemplate
     *            the detail view descriptor template to set.
     */
    public final void setDetailViewDescriptorTemplate(VLDockingViewDescriptor detailViewDescriptorTemplate) {

        Assert.notNull(detailViewDescriptorTemplate, "detailViewDescriptorTemplate");

        this.detailViewDescriptorTemplate = detailViewDescriptorTemplate;
    }

    /**
     * Sets the validation view descriptor template.
     * 
     * @param validationViewDescriptorTemplate
     *            the validation view descriptor template to set.
     */
    public final void setValidationViewDescriptorTemplate(VLDockingViewDescriptor validationViewDescriptorTemplate) {

        Assert.notNull(validationViewDescriptorTemplate, "validationViewDescriptorTemplate");

        this.validationViewDescriptorTemplate = validationViewDescriptorTemplate;
    }

    /**
     * Sets the unknown view descriptor template.
     * 
     * @param unknownViewDescriptorTemplate
     *            the unknown view descriptor template to set.
     */
    public final void setUnknownViewDescriptorTemplate(VLDockingViewDescriptor unknownViewDescriptorTemplate) {

        Assert.notNull(unknownViewDescriptorTemplate, "unknownViewDescriptorTemplate");

        this.unknownViewDescriptorTemplate = unknownViewDescriptorTemplate;
    }

    /**
     * Gets the master view descriptor template.
     * 
     * @return the master view descriptor template.
     */
    protected final VLDockingViewDescriptor getMasterViewDescriptorTemplate() {

        return this.masterViewDescriptorTemplate;
    }

    /**
     * Gets the search view descriptor template.
     * 
     * @return the search view descriptor template.
     */
    protected final VLDockingViewDescriptor getSearchViewDescriptorTemplate() {

        return this.searchViewDescriptorTemplate;
    }

    /**
     * Gets the detail view descriptor template.
     * 
     * @return the detail view descriptor template.
     */
    protected final VLDockingViewDescriptor getDetailViewDescriptorTemplate() {

        return this.detailViewDescriptorTemplate;
    }

    /**
     * Gets the validation view descriptor template.
     * 
     * @return the validation view descriptor template.
     */
    protected final VLDockingViewDescriptor getValidationViewDescriptorTemplate() {

        return this.validationViewDescriptorTemplate;
    }

    /**
     * Gets the unknown view descriptor template.
     * 
     * @return the unknown view descriptor template.
     */
    protected final VLDockingViewDescriptor getUnknownViewDescriptorTemplate() {

        return this.unknownViewDescriptorTemplate;
    }

    /**
     * Gets the application context.
     * 
     * @return the application context.
     */
    protected final ApplicationContext getApplicationContext() {

        return this.applicationContext;
    }
}
