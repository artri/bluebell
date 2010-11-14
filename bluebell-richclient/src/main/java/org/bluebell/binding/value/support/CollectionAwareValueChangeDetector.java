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

package org.bluebell.binding.value.support;

/**
 * 
 */

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.bluebell.richclient.util.GlazedListsUtils;
import org.bluebell.richclient.util.ObjectUtils;
import org.springframework.binding.value.support.DefaultValueChangeDetector;

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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean hasValueChanged(Object oldValue, Object newValue) {

        if (oldValue == newValue) {
            return Boolean.FALSE;
        }
        if ((oldValue == null) || (newValue == null)) {
            return Boolean.TRUE;
        }
        if ((oldValue instanceof List) && (newValue instanceof List)) {
            // (JAF), 20100424, for performance reasons check this before proceed.

            final Boolean isWrapped = GlazedListsUtils.isWrapped(((List) oldValue), (List) newValue);

            return isWrapped ? Boolean.FALSE : !ObjectUtils.isEqualList((List) oldValue, (List) newValue);
        }
        if ((oldValue instanceof Collection) && (newValue instanceof Collection)) {
            return !CollectionUtils.isEqualCollection((Collection) oldValue, (Collection) newValue);
        }

        return super.hasValueChanged(oldValue, newValue);
    }
}
