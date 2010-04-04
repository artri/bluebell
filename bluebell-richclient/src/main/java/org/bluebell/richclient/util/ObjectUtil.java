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
package org.bluebell.richclient.util;

import java.lang.reflect.Field;

import org.springframework.beans.PropertyAccessor;
import org.springframework.richclient.beans.DefaultMemberPropertyAccessor;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Utility class for dealing with objects.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class ObjectUtil {

    /**
     * Utility classes should have a private constructor.
     */
    private ObjectUtil() {

        super();
    }

    /**
     * Makes a shallow copy of the source object into the target one.
     * <p>
     * This method differs from {@link ReflectionUtils#shallowCopyFieldState(Object, Object)} this doesn't require
     * source and target objects to share the same class hierarchy.
     * 
     * @param source
     *            the source object.
     * @param target
     *            the target object.
     */
    public static void shallowCopy(Object source, Object target) {

        Assert.notNull(source, "source");
        Assert.notNull(target, "target");

        final PropertyAccessor sourceAccessor = ObjectUtil.createPropertyAccesor(source);
        final PropertyAccessor targetAccessor = ObjectUtil.createPropertyAccesor(target);

        // Try to copy every property
        ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {

            /** 
             * {@inheritDoc}
             */
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

                final String name = field.getName();
                if ((sourceAccessor.isReadableProperty(name)) && (targetAccessor.isWritableProperty(name))) {
                    final Object value = sourceAccessor.getPropertyValue(name);
                    targetAccessor.setPropertyValue(name, value);
                }
            }
        });
    }

    /**
     * Creates a suitable property accessor for the given object.
     * 
     * @param object
     *            the target object.
     * @return the property accessor.
     */
    private static PropertyAccessor createPropertyAccesor(Object object) {

        Assert.notNull(object, "target");

        final PropertyAccessor propertyAccessor = new DefaultMemberPropertyAccessor(//
                object.getClass(), object, Boolean.TRUE, Boolean.FALSE);

        return propertyAccessor;
    }
}
