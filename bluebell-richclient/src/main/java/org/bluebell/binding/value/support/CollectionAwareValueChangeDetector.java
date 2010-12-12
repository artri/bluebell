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
 * Extends {@link DefaultValueChangeDetector} behaviour in order to provide support for collections so "two collections
 * are equal if and only if they have same elements".
 * <p>
 * <b>How does Bluebell works with it?</b>
 * <dl>
 * <dt>Application context
 * <dd>Main application context
 * <dt>Bean declaration
 * <dd>Find below provided bean declaration:
 * 
 * <pre>
 * <!--
 *         Bean: defaultValueChangeDetector
 *         Usage: platform optional
 *         Description: This specifies the value change detector used for value models (and others) in order to detect changes.
 * -->
 * <bean id="defaultValueChangeDetector" class="org.bluebell.binding.value.support.CollectionAwareValueChangeDetector" />
 * </pre>
 * 
 * <dt>Bean configuration
 * <dd>Find below the main bean with whom it collaborates:
 * 
 * <pre>
 * <!--
 *         Bean: defaultApplicationServices
 *         Usage: Platform required (unless you set this up programmatically)
 *         Description: This configures the application services available to the platform. There are specific setter methods for
 *         all the standard services,
 *         see the javadoc on the DefaultApplicationServices class for more details.
 * 
 *         NOTE: The use of bean ids (idref) is preferred over using direct bean references to avoid startup problems with circular
 *         references.
 * -->
 * <bean id="defaultApplicationServices" class="org.springframework.richclient.application.support.DefaultApplicationServices">
 *         ...
 *         p:value-change-detector-id="${richclient.valueChangeDetector}"
 * </bean>
 * </pre>
 * 
 * <dt>Aditional configuration
 * <dd>A PPC (<code>PropertyPlaceholderConfigurer</code>) must provide a valid replacement for
 * <code>${richclient.valueChangeDetector}</code>. Default configuration defines
 * <code>defaultPropertyPlaceholderConfigurer</code> with the following property:
 * 
 * <pre>
 * richclient.valueChangeDetector = defaultValueChangeDetector
 * </pre>
 * 
 * </dl>
 * 
 * @see CollectionUtils#isEqualCollection(Collection, Collection)
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
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
