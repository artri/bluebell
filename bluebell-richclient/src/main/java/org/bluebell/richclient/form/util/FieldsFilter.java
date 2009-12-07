/**
 * 
 */
package org.bluebell.richclient.form.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Estrategia para la obtención de los campos de una clase del dominio.
 * 
 * @param <T>
 *            el tipo de la entidad a filtrar.
 * @param <ID>
 *            el tipo del identificador de la entidad a filtrar.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public interface FieldsFilter<T, ID extends Serializable> {

    /**
     * Obtiene los campos de una clase conforme a la política establecida.
     * 
     * @param clazz
     *            la clase.
     * @return los campos de la clase.
     */
    Collection<Field> getFields(Class<T> clazz);
}
