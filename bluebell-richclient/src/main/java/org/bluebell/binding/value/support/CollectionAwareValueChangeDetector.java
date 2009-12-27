/**
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

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
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
     * Determina si los argumentos pasados por parámetros son diferentes. Si son colecciones entonces habrá habido
     * cambios si sólo si ambas colecciones no contienen los mismos elementos, en cualquier otro caso delega su
     * ejecución en {@link DefaultValueChangeDetector#hasValueChanged(Object, Object)}.
     * 
     * @param oldValue
     *            el valor original.
     * @param newValue
     *            el nuevo valor.
     * @return <code>true</code> si los objetos son lo suficientemente diferentes como para indicar un cambio en el
     *         <em>value model</em> .
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean hasValueChanged(Object oldValue, Object newValue) {

        if (oldValue == newValue) {
            // (JAF), 20081002, esta comprobación no se hacía y con colecciones
            // no se puede asumir eso.
            return Boolean.FALSE;
        } else if ((oldValue != null) && (newValue != null) && Collection.class.isAssignableFrom(oldValue.getClass())
                && Collection.class.isAssignableFrom(newValue.getClass())) {

            // Los dos parámetros son colecciones.
            return !CollectionUtils.isEqualCollection((Collection) (oldValue), (Collection) newValue);
        }

        // (JAF), 20090612, sería mejor utilizar la igualdad a nivel de objeto y
        // no de identidad: sería más pesado y rompe parte del código actual.
        // return !ObjectUtils.equals(oldValue, newValue);

        return super.hasValueChanged(oldValue, newValue);
    }
}
