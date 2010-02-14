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

package org.springframework.beans.factory.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * A property resource configurer that resolves placeholders in bean property values of context definitions, immediately
 * after the bean has been initialized.
 * 
 * It is useful to resolve bean property placeholders in other <code>Ordered</code>
 * <code>BeanFactoryPostProcessor</code>. It must be noted that the other <code>Ordered</code>
 * <code>BeanFactoryPostProcessor</code> must be defined after this.
 * 
 * A caveat is that not all <code>ListableBeanFactory</code> implementations return bean names in the order of
 * definition as prescribed by the interface, which is relied upon by this configurer.
 * 
 * <p>
 * Example XML context definition:
 * 
 * <pre>
 * &lt;bean id="systemPropertiesConfigurer"
 *      class="au.com.cardlink.common.spring.beans.factory.config.EagerPropertyPlaceholderConfigurer"&gt;
 *   &lt;property name="placeholderPrefix"&gt;&lt;value&gt;$${&lt;/value&gt;&lt;/property&gt;
 * &lt;/bean&gt;
 * &lt;bean id="configPropertiesConfigurer"
 *      class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer"&gt;
 *   &lt;property name="location"&gt;&lt;value&gt;$${config.properties.location}&lt;/value&gt;&lt;/property&gt;
 *   &lt;property name="fileEncoding"&gt;&lt;value&gt;$${cardlink.properties.encoding}&lt;/value&gt;&lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * @author Alex Wei (ozgwei@gmail.com)
 * @since 26/07/2006
 * @see org.springframework.core.Ordered
 * @see BeanFactoryPostProcessor
 * @see org.springframework.beans.factory.ListableBeanFactory
 * @see PropertyPlaceholderConfigurer
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>: Checkstyle fixes, comments and test.
 * @see <a href="http://forum.springsource.org/showthread.php?p=279633#post279633">Post on Spring Forum</a>
 * @see <a href="http://jira.springframework.org/browse/SPR-1076">JIRA issue</a>
 */
public class EagerPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements InitializingBean {

    /**
     * The bean factory.
     */
    private ConfigurableListableBeanFactory beanFactory;

    /**
     * Is processing completed.
     */
    private boolean processingCompleted = false;

    /**
     * Zero-argument constructor.
     */
    public EagerPropertyPlaceholderConfigurer() {

        super();
    }

    /**
     * Eagerly resolves property placeholders so that the bean definitions of other
     * <code>BeanFactoryPostProcessor</code> can be modified before instantiation.
     * 
     * @throws Exception
     *             if failure.
     */
    public void afterPropertiesSet() throws Exception {

        if (this.beanFactory != null) {
            super.postProcessBeanFactory(this.beanFactory);
            this.processingCompleted = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setBeanFactory(BeanFactory beanFactory) {

        // Obtains the BeanFactory where bean definitions with unresolved property placeholders are stored.
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
        } else {
            this.beanFactory = null;
        }
        super.setBeanFactory(beanFactory);
    }

    /**
     * Resolves property placeholders only if the post processing was not run in {@link #afterPropertiesSet}.
     * 
     * @param beanFactory
     *            the bean factory.
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {

        // throws BeansException {

        // Should beanFactory be compared with this.beanFactory to ensure they are the same factory?
        if (!processingCompleted) {
            super.postProcessBeanFactory(beanFactory);
            processingCompleted = true;
        }
    }

}
