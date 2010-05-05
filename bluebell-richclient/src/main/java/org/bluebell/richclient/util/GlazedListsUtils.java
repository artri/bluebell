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
import java.util.List;

import org.apache.commons.collections.Predicate;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import ca.odell.glazedlists.TransformedList;

/**
 * Utility class for dealing with glazed lists.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class GlazedListsUtils {

    /**
     * The field <code>source</code> from {@link TransformedList}.
     */
    private static final Field SOURCE_FIELD = ReflectionUtils.findField(TransformedList.class, "source");

    static {
        ReflectionUtils.makeAccessible(GlazedListsUtils.SOURCE_FIELD);
    }

    /**
     * Utility classes should have a private constructor.
     */
    private GlazedListsUtils() {

        super();
    }

    /**
     * Checks whether a list is wrapped by other of type <code>TransformedList</code>.
     * 
     * @param <T>
     *            the type of the elements of the list.
     * @param list1
     *            the wrapper list.
     * @param list2
     *            the wrapped list.
     * @return <code>true</code> if wrapped and <code>false</code> in other case.
     * 
     * @since 20100424 due to performance reasons.
     */
    public static <T> Boolean isWrapped(final List<T> list1, final List<T> list2) {

        return !GlazedListsUtils.forAllDo(list1, new Predicate() {

            @Override
            public boolean evaluate(Object object) {

                return (object != list2);
            }
        });
    }

    /**
     * Evaluates every wrapped event list, including the one given as argument.
     * <p>
     * Stops after the first negative evaluation or at the end of the chain.
     * 
     * @param <T>
     *            the type of the elements of the list.
     * 
     * @param eventList
     *            the event list.
     * @param predicate
     *            the predicate to evaluate.
     * @return <code>true</code> if every evaluation is positive and <code>false</code> in other case.
     * 
     * @see TransformedList
     */
    public static <T> Boolean forAllDo(final List<T> eventList, final Predicate predicate) {

        Assert.notNull(eventList, "eventList");
        Assert.notNull(predicate, "predicate");

        Object source = eventList;

        while (source != null) {

            // Evaluate target event list
            if (!predicate.evaluate(source)) {
                return Boolean.FALSE;
            }

            if (source instanceof TransformedList<?, ?>) {
                source = ReflectionUtils.getField(GlazedListsUtils.SOURCE_FIELD, source);
            } else {
                source = null;
            }
        }

        return Boolean.TRUE;
    }

}
