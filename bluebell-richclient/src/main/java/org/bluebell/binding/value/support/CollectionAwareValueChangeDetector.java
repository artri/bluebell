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
