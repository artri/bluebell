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

package org.bluebell.richclient.application.config;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.TransformerUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bluebell.richclient.util.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
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
@SuppressWarnings("unchecked")
public class BbApplicationConfig implements BeanFactoryPostProcessor, PriorityOrdered,
        FactoryBean<MultiValueMap<String, String[]>> {

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
     */
    final MultiValueMap<String, String[]> applicationConfig = new LinkedMultiValueMap<String, String[]>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        // Beans of type PPC
        final List<PropertyPlaceholderConfigurer> configurers = new ArrayList<PropertyPlaceholderConfigurer>(
                beanFactory.getBeansOfType(PropertyPlaceholderConfigurer.class).values());

        // Priority ordered
        OrderComparator.sort(configurers);

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
    public MultiValueMap<String, String[]> getObject() throws Exception {

        return this.applicationConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends MultiValueMap<String, String[]>> getObjectType() {

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

        // Retrieve current placeholder values
        // Resolve placeholder bean name using reflection
        final ConfigurablePropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(configurer);

        final String beanName = (String) propertyAccessor.getPropertyValue(BbApplicationConfig.BEAN_NAME);
        final Integer order = configurer.getOrder();

        // Update placeholder values (discarding duplicates)
        final String[] newerValue = new String[] { value, beanName, String.valueOf(order) };
        final String[] foundValue = (String[]) CollectionUtils.find( //
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
     * Transforms a list of string array values into a readable string with the values of a placeholder ordered.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private static class PlaceholderStatsTransformer implements Transformer {

        /**
         * {@inheritDoc}
         */
        @Override
        public Object transform(Object input) {

            Assert.isInstanceOf(List.class, input);

            final List<String[]> values = (List<String[]>) input;
            final StringBuffer sb = new StringBuffer();

            for (String[] value : values) {
                sb.append(value[0]); // Value
                sb.append("[");
                sb.append(value[2]); // Order
                sb.append("]");
                sb.append("::");
                sb.append(value[1]); // PPC
                sb.append(" >> ");
            }

            return sb.toString();
        }
    }

    /**
     * Prints configuration values.
     * 
     * @param placeholderValues
     *            the configuration values to be printed.
     * @return the printed configuration values.
     */  
    public static String debugPrint(Map<String, List<String[]>> placeholderValues) {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(baos);

        final Map<String, String[]> mapToBePrinted = MapUtils.transformedMap(//
                new HashMap<String, String[]>(), TransformerUtils.nopTransformer(), new PlaceholderStatsTransformer());
        mapToBePrinted.putAll((Map) placeholderValues);
        MapUtils.debugPrint(outPrint, "Configuration values", mapToBePrinted);

        return baos.toString();
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
    public static String getValue(Map<String, List<String[]>> applicationConfig, String placeholder, Integer precedence) {

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

}