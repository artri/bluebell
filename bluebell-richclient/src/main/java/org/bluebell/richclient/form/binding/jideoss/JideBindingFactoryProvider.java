/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.bluebell.richclient.form.binding.jideoss;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.binding.form.FormModel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.richclient.form.binding.BindingFactory;
import org.springframework.richclient.form.binding.BindingFactoryProvider;
import org.springframework.richclient.util.Assert;

/**
 * Provider that constructs instances of {@link JideBindingFactory} on demand using Spring Application instance as
 * object factory.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 * 
 * @see org.springframework.richclient.application.ApplicationServices#getBindingFactory(FormModel)
 * @see org.springframework.richclient.application.ApplicationServices#getBindingFactoryProvider()
 */
public class JideBindingFactoryProvider implements BindingFactoryProvider, ApplicationContextAware, InitializingBean {

    /**
     * The Spring application context.
     */
    private ApplicationContext applicationContext;

    /**
     * The binding factory bean name.
     */
    private String bindingFactoryBeanName;

    /**
     * Produce a <code>BindingFactory</code> using the provided form model.
     * 
     * @param formModel
     *            the model on which to construct the binding factory.
     * 
     * @return the binding factory.
     */
    public BindingFactory getBindingFactory(FormModel formModel) {

        return (BindingFactory) this.getApplicationContext().getBean(this.getBindingFactoryBeanName(), formModel);
    }

    /**
     * Sets the application context.
     * 
     * @param application
     *            context the application context to set.
     */
    public void setApplicationContext(ApplicationContext applicationContext) {

        Assert.notNull(applicationContext, "applicationContext");

        this.applicationContext = applicationContext;
    }

    /**
     * Sets the binding factory bean name.
     * 
     * @param bindingFactoryBeanName
     *            the binding factory bean name to set.
     */
    public final void setBindingFactoryBeanName(String bindingFactoryBeanName) {

        Assert.notNull(bindingFactoryBeanName, "bindingFactoryBeanName");

        this.bindingFactoryBeanName = bindingFactoryBeanName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Assert.notNull(this.getApplicationContext(), "this.getApplicationContext()");
        Assert.notNull(this.getBindingFactoryBeanName(), "this.getBindingFactoryBeanName()");
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
     * Gets the binding factory bean name.
     * 
     * @return the binding factory bean name.
     */
    protected final String getBindingFactoryBeanName() {

        return this.bindingFactoryBeanName;
    }
}
