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
package org.bluebell.richclient.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Utility class for dealing with objects.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class ObjectUtils {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectUtils.class);

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

        final PropertyAccessor sourceAccessor = PropertyAccessorFactory.forDirectFieldAccess(source);
        final PropertyAccessor targetAccessor = PropertyAccessorFactory.forDirectFieldAccess(target);

        // Try to copy every property
        ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void doWith(Field field) { // throws IllegalArgumentException, IllegalAccessException {

                final String name = field.getName();

                if (!Modifier.isFinal(field.getModifiers()) && (targetAccessor.isWritableProperty(name))) {

                    final Object value = sourceAccessor.getPropertyValue(name);
                    targetAccessor.setPropertyValue(name, value);
                }
            }
        });
    }

    /**
     * Method based on
     * {@link org.apache.commons.collections.ListUtils#isEqualList(java.util.Collection, java.util.Collection)} rewrote
     * for performance reasons.
     * <p>
     * Basically employs {@link ObjectUtils#equals(Object, Object)} instead of {@link #equals(Object)} since the first
     * one checks identity before calling <code>equals</code>.
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

    /**
     * This is a utility method for getting raw objects that may have been proxied. It is intended to be used in cases
     * where raw implementations are needed rather than working with interfaces which they implement.
     * 
     * @param bean
     *            the potential proxy.
     * @return the most inner unwrapped bean.
     * 
     * @see #unwrapProxy(Object, Boolean)
     * @since 20101223 thanks to <a href="http://jirabluebell.b2b2000.com/browse/BLUE-34">BLUE-34</a>
     */
    public static Object unwrapProxy(Object bean) {

        return ObjectUtils.unwrapProxy(bean, Boolean.TRUE);
    }

    /**
     * This is a utility method for getting raw objects that may have been proxied. It is intended to be used in cases
     * where raw implementations are needed rather than working with interfaces which they implement.
     * 
     * @param bean
     *            the potential proxy.
     * @param recursive
     *            whether to procceeed recursively through nested proxies.
     * @return the unwrapped bean or <code>null</code> if target bean is <code>null</code>. If <code>recursive</code>
     *         parameter is <code>true</code> then returns the most inner unwrapped bean, otherwise the nearest target
     *         bean is returned.
     * 
     * @see Based on this <a href="http://forum.springsource.org/showthread.php?t=60216">Spring forum topic</a>.
     * @see Advised
     * @since 20101223 thanks to <a href="http://jirabluebell.b2b2000.com/browse/BLUE-34">BLUE-34</a>
     */
    public static Object unwrapProxy(Object bean, Boolean recursive) {

        Assert.notNull(recursive, "recursive");

        Object unwrapped = bean;

        // If the given object is a proxy, set the return value as the object being proxied, otherwise return the given
        // object
        if ((bean != null) && (bean instanceof Advised) && (AopUtils.isAopProxy(bean))) {

            final Advised advised = (Advised) bean;
            try {
                final Object target = advised.getTargetSource().getTarget();

                unwrapped = recursive ? ObjectUtils.unwrapProxy(target, recursive) : target;
            } catch (Exception e) {
                unwrapped = bean;
                ObjectUtils.LOGGER.warn("Failure unwrapping \"" + bean + "\".", e);
            }
        }

        return unwrapped;
    }
}
