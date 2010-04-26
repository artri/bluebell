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

package org.bluebell.binding.value.support;

/**
 * 
 */

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.bluebell.richclient.util.ObjectUtils;
import org.springframework.binding.value.support.DefaultValueChangeDetector;
import org.springframework.util.ReflectionUtils;

import ca.odell.glazedlists.TransformedList;

/**
 * Extiende el comportamiento de {@link DefaultValueChangeDetector} con el objetivo de soportar colecciones en la medida
 * en que dos colecciones son iguales si tienen los mismos elementos.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 * 
 * @see CollectionUtils#isEqualCollection(Collection, Collection)
 */
public class CollectionAwareValueChangeDetector extends DefaultValueChangeDetector {

    /**
     * The source field of {@link TransformedList} class.
     */
    private static final Field SOURCE_FIELD = ReflectionUtils.findField(TransformedList.class, "source");
    static {
        ReflectionUtils.makeAccessible(CollectionAwareValueChangeDetector.SOURCE_FIELD);
    }

    /**
     * Determines if there has been a change in value between the provided arguments.
     * <p>
     * Overrides original implementation checking for identity, <code>null</code> values and collection equality.
     * Distinguish between lists and other kind of collections for optimization.
     * 
     * @param oldValue
     *            the original object value.
     * @param newValue
     *            the new object value.
     * @return <code>true</code> if the objects are different enough to indicate a change in the value model.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean hasValueChanged(Object oldValue, Object newValue) {

        if (oldValue == newValue) {
            return Boolean.FALSE;
        }
        if ((oldValue == null) || (newValue == null)) {
            return Boolean.TRUE;
        }
        if ((oldValue instanceof List) && (newValue instanceof List)) {
            // (JAF), 20100424, for performance reasons check this before proceed.
            final Boolean isWrapped = CollectionAwareValueChangeDetector.isWrapped(((List) oldValue), (List) newValue);

            return isWrapped ? Boolean.FALSE : !ObjectUtils.isEqualList((List) oldValue, (List) newValue);
        }
        if ((oldValue instanceof Collection) && (newValue instanceof Collection)) {
            return !CollectionUtils.isEqualCollection((Collection) oldValue, (Collection) newValue);
        }

        return super.hasValueChanged(oldValue, newValue);
    }

    /**
     * Checks whether a list is wrapped by other of type <code>TransformedList</code>. In such a case its content will
     * be interpreted as equals.
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
    private static final <T> Boolean isWrapped(List<T> list1, List<T> list2) {

        Object source = list1;
        while (source instanceof TransformedList<?, ?>) {

            if (source == list2) {
                return Boolean.TRUE;
            }

            source = ReflectionUtils.getField(CollectionAwareValueChangeDetector.SOURCE_FIELD, source);
        }

        return Boolean.FALSE;
    }
}
