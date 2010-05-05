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

package org.bluebell.richclient.util;

import org.apache.commons.collections.Transformer;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;

/**
 * <code>Transformer</code> that outputs a field value.
 * <p>
 * Its behaviour is similar to <code>org.apache.commons.beanutils.BeanToPropertyValueTransformer</code> but in this case
 * direct field access is done.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class ObjectToFieldValueTransformer implements Transformer {

    /**
     * The property name.
     */
    private String propertyName;

    /**
     * Creates the transformer.
     * 
     * @param propertyName
     *            the property name.
     */
    private ObjectToFieldValueTransformer(String propertyName) {

        this.setPropertyName(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object transform(Object input) {

        if (input == null) {
            return null;
        }

        return PropertyAccessorFactory.forDirectFieldAccess(input).getPropertyValue(this.getPropertyName());
    }

    /**
     * Gets the property name.
     * 
     * @return the property name.
     */
    private String getPropertyName() {

        return this.propertyName;
    }

    /**
     * Sets the property name.
     * 
     * @param propertyName
     *            name the property name to set.
     */
    private void setPropertyName(String propertyName) {

        Assert.notNull(propertyName, "propertyName");

        this.propertyName = propertyName;
    }

    /**
     * Creates a transformer for the given property name.
     * 
     * @param propertyName
     *            the property name.
     * @return the transformer.
     */
    public static ObjectToFieldValueTransformer getInstance(String propertyName) {

        return new ObjectToFieldValueTransformer(propertyName);
    }
}
