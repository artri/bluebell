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

package org.bluebell.richclient.application.config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bluebell.richclient.util.ObjectUtils;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * <code>FactoryBean</code> useful for recovering placeholder resolved values.
 * <p>
 * It's also a <code>BeanFactoryPostProcessor</code> in order to acts just after the bean factory is refreshed.
 * <p>
 * Note that also implements <code>PriorityOrdered</code> in order to be the first bean post processor, otherwise (if a
 * <code>PropertyPlaceholderConfigurer</code> -aka PPC - is executed before) the behaviour will forget prior PPC's.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbApplicationConfig implements BeanFactoryPostProcessor, PriorityOrdered,
        FactoryBean<MultiValueMap<String, String[]>>, ApplicationContextAware {

    /**
     * The name of the field with the bean name. Useful for debugging.
     */
    private static final String BEAN_NAME = "beanName";

    /**
     * A map containing resolved values for every placeholder.
     * <p>
     * For every placeholder (key) an ordered list of resolved values is provided.
     * <p>
     * Every resolved value consists on a String array where:
     * <ol>
     * <li>The first item is the resolved value itself.
     * <li>The second item is the placeholder configurer that resolved it.
     * <li>The third item is the priority order of the resolving placeholder configurer.
     * </ol>
     * 
     * @see LinkedMultiValueMap
     */
    private final MultiValueMap<String, String[]> applicationConfig = new LinkedMultiValueMap<String, String[]>();

    /**
     * The application context where this bean is defined.
     */
    private ApplicationContext applicationContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) { // throws BeansException {

        // Beans of type PPC
        final List<PropertyPlaceholderConfigurer> configurers = BbApplicationConfig.getConfigurers(beanFactory);

        // Post process bean factory with "useless" configurers
        for (PropertyPlaceholderConfigurer configurer : configurers) {
            new StatsPropertyPlaceholderConfigurer(configurer).postProcessBeanFactory(beanFactory);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This <b>must</b> be the first <code>bean post processor</code>, otherwise some placeholders will be ignored.
     */
    @Override
    public int getOrder() {

        return PriorityOrdered.HIGHEST_PRECEDENCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final MultiValueMap<String, String[]> getObject() throws Exception {

        return this.applicationConfig;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public final Class<? extends MultiValueMap<String, String[]>> getObjectType() {

        return (Class<? extends MultiValueMap<String, String[]>>) this.applicationConfig.getClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSingleton() {

        return Boolean.TRUE;
    }

    /**
     * Gets the application context where this bean is defined.
     * 
     * @return the application context.
     */
    public final ApplicationContext getApplicationContext() {

        return this.applicationContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) { // throws BeansException {

        Assert.notNull(applicationContext, "applicationContext");

        this.applicationContext = applicationContext;
    }

    /**
     * Gets (an unmodifiable version of)the placeholder values.
     * 
     * @return the placeholder values.
     */
    protected final MultiValueMap<String, String[]> getApplicationConfig() {

        return this.applicationConfig;
    }

    /**
     * Update configuration values for a given placeholder, resolved value and PPC.
     * 
     * @param placeholder
     *            the placeholder.
     * @param value
     *            last resolved value.
     * @param configurer
     *            the PPC that resolved the value.
     */
    private void update(String placeholder, String value, PropertyPlaceholderConfigurer configurer) {

        Assert.notNull(configurer, "configurer");

        final String beanName = BbApplicationConfig.getBeanName(configurer);
        final Integer order = configurer.getOrder();

        // Update placeholder values (discarding duplicates)
        final String[] newerValue = new String[] { value, beanName, String.valueOf(order) };
        @SuppressWarnings("unchecked")
        final String[] foundValue = (String[]) CollectionUtils.find(//
                (Collection<String[]>) MapUtils.getObject(this.applicationConfig, placeholder, ListUtils.EMPTY_LIST), //
                new Predicate() {

                    @Override
                    public boolean evaluate(Object object) {

                        Assert.isInstanceOf(String[].class, object);

                        return ArrayUtils.isEquals(newerValue, (String[]) object);
                    }
                });

        if (foundValue == null) {

            this.applicationConfig.add(placeholder, new String[] { value, beanName, String.valueOf(order) });
        }
    }

    /**
     * A <code>PropertyPlaceholderConfigurer</code> that doesn't change bean definitions but records placeholder values.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class StatsPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

        /**
         * Creates a new instance given a source one, copying every field and hardcoding the fact this placeholder
         * ignores unresolvable placeholders and not found resources.
         * 
         * @param source
         *            the source PPC.
         */
        public StatsPropertyPlaceholderConfigurer(PropertyPlaceholderConfigurer source) {

            ObjectUtils.shallowCopy(source, this);
            this.setIgnoreUnresolvablePlaceholders(Boolean.TRUE);
            this.setIgnoreResourceNotFound(Boolean.TRUE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {

            final String value = super.resolvePlaceholder(placeholder, props, systemPropertiesMode);

            if (value != null) {
                BbApplicationConfig.this.update(placeholder, value, this);
            }

            // (JAF), 20100921, returning "null" makes this placeholder useless !! (but records whatever we want)
            return null;
        }
    }

    /**
     * Prints PPC's debug information.
     * 
     * Example:
     * 
     * <pre>
     * Application context with id XX:
     * {
     *    substancePropertyPlaceholderConfigurer[5]
     *    vldockingLaunchPropertyPlaceholderConfigurer[2999]
     *    vldockingPropertyPlaceholderConfigurer[3000]
     *    personPropertyPlaceholderConfigurer[100000]
     *    defaultPropertyPlaceholderConfigurer[2147483647]
     * }
     * </pre>
     * 
     * @param applicationContext
     *            the application context to be debugged.
     * 
     * @return the debug information
     */
    public static String debugPrint(ConfigurableApplicationContext applicationContext) {

        final ListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        final List<PropertyPlaceholderConfigurer> configurers = BbApplicationConfig.getConfigurers(beanFactory);

        final StringBuffer sb = new StringBuffer();

        sb.append("Application context with id \"");
        sb.append(BbApplicationConfig.getApplicationContextId(applicationContext)).append("\":\n");
        sb.append('{');

        for (PropertyPlaceholderConfigurer configurer : configurers) {
            final String beanName = BbApplicationConfig.getBeanName(configurer);
            final Integer order = configurer.getOrder();

            sb.append("\t");
            sb.append(beanName);
            sb.append('[');
            sb.append(order);
            sb.append(']');
            sb.append('\n');
        }
        sb.append("}\n");

        return sb.toString();
    }

    /**
     * Prints configuration values.
     * 
     * Example:
     * 
     * <pre>
     *  1.richclient.substanceDecoratedComponentFactory:
     *    1.1.defaultComponentFactory::substancePropertyPlaceholderConfigurer[5]
     *    1.2.defaultComponentFactory::defaultPropertyPlaceholderConfigurer[2147483647]
     * 
     * 2.richclient.componentFactory:
     *    2.1.substanceComponentFactory::substancePropertyPlaceholderConfigurer[5]
     *    2.2.defaultComponentFactory::defaultPropertyPlaceholderConfigurer[2147483647]
     * 
     * 3.richclient.applicationPageFactory:
     *    3.1.vldockingApplicationPageFactory::vldockingPropertyPlaceholderConfigurer[3000]
     *    3.2.vldockingApplicationPageFactory::defaultPropertyPlaceholderConfigurer[2147483647]
     * 
     * 4.richclient.messageSourceBasenames:
     *    4.1.vldockingMessageSourceBasenames::vldockingPropertyPlaceholderConfigurer[3000]
     *    4.2.personMessageSourceBasenames::personPropertyPlaceholderConfigurer[100000]
     *    4.3.vldockingMessageSourceBasenames::defaultPropertyPlaceholderConfigurer[2147483647]
     * </pre>
     * 
     * @param placeholderValues
     *            the configuration values to be printed.
     * @return the printed configuration values.
     */
    public static String debugPrint(Map<String, List<String[]>> placeholderValues) {

        Integer keyCounter = Integer.valueOf(0);
        Integer valueCounter = Integer.valueOf(0);

        final StringBuffer sb = new StringBuffer();

        for (Map.Entry<String, List<String[]>> entry : placeholderValues.entrySet()) {

            // Key value (Placeholder)
            sb.append(++keyCounter).append('.').append(entry.getKey()).append(":\n");

            // Placeholder values
            valueCounter = Integer.valueOf(0);
            for (Object itr : entry.getValue()) {

                if (!(itr instanceof String[])) {
                    // FIXME, (JAF), 20101217, sometimes there is a value differente from String[]
                    break;
                }
                final String[] value = (String[]) itr;

                // \tkeyCounter.valueCounter.
                sb.append("\t").append(keyCounter).append('.').append(++valueCounter).append('.');

                // Value::PPC[order]\n
                sb.append(value[0]).append("::").append(value[1]).append('[').append(value[2]).append(']').append("\n");
            }
            sb.append("\n");
        }

        sb.append("\n");

        return sb.toString();
    }

    /**
     * Gets the value for a given placeholder.
     * 
     * @param applicationConfig
     *            the application config to be queried.
     * @param placeholder
     *            the placeholder.
     * @return the resolved value or <code>null</code> if not found.
     * 
     * @see #getValue(Map, String, Integer)
     */
    public static String getValue(Map<String, List<String[]>> applicationConfig, String placeholder) {

        return BbApplicationConfig.getValue(applicationConfig, placeholder, 0);
    }

    /**
     * Gets the value with the given precedence for a given placeholder.
     * 
     * @param applicationConfig
     *            the application config to be queried.
     * @param placeholder
     *            the placeholder.
     * @param precedence
     *            the precedence.
     * 
     * @return the resolved value or <code>null</code> if not found.
     * 
     * @see #getPlaceholderConfig(Map, String, Integer)
     */
    public static String getValue(Map<String, List<String[]>> applicationConfig, //
            String placeholder, Integer precedence) {

        final String[] config = BbApplicationConfig.getPlaceholderConfig(applicationConfig, placeholder, precedence);

        return (config != null) ? config[0] : null;
    }

    /**
     * Gets the configuration found for a given placeholder.
     * 
     * @param applicationConfig
     *            the application config to be queried.
     * @param placeholder
     *            the placeholder.
     * @param precedence
     *            the precedence.
     * @return the resolved configuration or <code>null</code> if not found.
     * 
     * @see #applicationConfig
     */
    public static String[] getPlaceholderConfig(Map<String, List<String[]>> applicationConfig, String placeholder,
            Integer precedence) {

        Assert.notNull(applicationConfig, "applicationConfig");
        Assert.isTrue(!StringUtils.isEmpty(placeholder), "!StringUtils.isEmpty(placeholder)");
        Assert.notNull(precedence, "precedence");
        Assert.isTrue(precedence >= 0, "precedence >= 0");

        final List<String[]> values = applicationConfig.get(placeholder);
        if (values != null) {
            if (precedence < values.size()) {

                return values.get(precedence);
            }
        }

        return null;
    }

    /**
     * Gets the application context id.
     * 
     * @param applicationContext
     *            the application context.
     * 
     * @return the application context id.
     */
    private static String getApplicationContextId(ApplicationContext applicationContext) {

        /*
         * Since ApplicationContext#getDisplayName() and ApplicationContext#getId() returns an illegible string it's
         * better to employ the creation date in order to identify application contexts one each other.
         */
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(applicationContext.getStartupDate());

        return calendar.getTime().toString();
    }

    /**
     * Gets every <code>PropertyPlaceholderConfigurer</code> (aka PPC) defined into a bean factory ordered by
     * precedence.
     * 
     * @param beanFactory
     *            the bean factory.
     * @return the PPC's.
     */
    private static List<PropertyPlaceholderConfigurer> getConfigurers(ListableBeanFactory beanFactory) {

        // Beans of type PPC
        final List<PropertyPlaceholderConfigurer> configurers = new ArrayList<PropertyPlaceholderConfigurer>(//
                beanFactory.getBeansOfType(PropertyPlaceholderConfigurer.class).values());

        // Priority ordered
        OrderComparator.sort(configurers);

        return configurers;
    }

    /**
     * Gets the bean name of a given <code>PropertyPlaceholderConfigurer</code> using reflection.
     * 
     * @param configurer
     *            the target configurer.
     * @return the bean name.
     */
    private static String getBeanName(PropertyPlaceholderConfigurer configurer) {

        // Resolve placeholder bean name using reflection
        final ConfigurablePropertyAccessor accessor = PropertyAccessorFactory.forDirectFieldAccess(configurer);
        final String beanName = (String) accessor.getPropertyValue(BbApplicationConfig.BEAN_NAME);

        return beanName;
    }
}
