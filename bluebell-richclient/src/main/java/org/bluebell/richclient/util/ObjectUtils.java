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
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.PropertyAccessor;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Utility class for dealing with objects.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class ObjectUtils {

    /**
     * Utility classes should have a private constructor.
     */
    private ObjectUtils() {

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

        final PropertyAccessor sourceAccessor = new DirectFieldAccessor(source);
        final PropertyAccessor targetAccessor = new DirectFieldAccessor(target);

        // Try to copy every property
        ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void doWith(Field field) {

                // throws IllegalArgumentException, IllegalAccessException {

                final String name = field.getName();

                if (!Modifier.isFinal(field.getModifiers()) && (targetAccessor.isWritableProperty(name))) {

                    final Object value = sourceAccessor.getPropertyValue(name);
                    targetAccessor.setPropertyValue(name, value);
                }
            }
        });
    }

    /**
     * Method based on {@link ListUtils#isEqualList(Collection, Collection)} rewrote for performance reasons.
     * <p>
     * Basically employs {@link ObjectUtils#equals(Object, Object)} instead of {@link #equals(Object)} since that checks
     * identity before calling <code>equals</code>.
     * 
     * @param <T>
     *            the type of the elements in the list.
     * @param list1
     *            the first list, may be null
     * @param list2
     *            the second list, may be null
     * 
     * @return whether the lists are equal by value comparison
     */
    public static <T> Boolean isEqualList(List<T> list1, List<T> list2) {

        if (list1 == list2) {
            return Boolean.TRUE;
        } else if ((list1 == null) || (list2 == null) || (list1.size() != list2.size())) {
            return Boolean.FALSE;
        }

        final Iterator<T> itr1 = list1.iterator();
        final Iterator<T> itr2 = list2.iterator();
        Object obj1 = null;
        Object obj2 = null;

        while (itr1.hasNext() && itr2.hasNext()) {
            obj1 = itr1.next();
            obj2 = itr2.next();

            if (!(obj1 == null ? obj2 == null : org.apache.commons.lang.ObjectUtils.equals(obj1, obj2))) {
                return Boolean.FALSE;
            }
        }

        return !(itr1.hasNext() || itr2.hasNext());
    }
    
    
}
