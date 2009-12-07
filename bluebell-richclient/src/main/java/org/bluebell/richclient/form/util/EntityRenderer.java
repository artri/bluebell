/**
 * 
 */
package org.bluebell.richclient.form.util;

import java.lang.reflect.Field;
import java.util.Collection;

import org.springframework.richclient.form.binding.BindingFactory;
import org.springframework.richclient.form.builder.AbstractFormBuilder;

/**
 * Renderiza los campos de una entidad del dominio.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public interface EntityRenderer {

    /**
     * Renderiza una entidad utilizando los campos devueltos por {@link #getFields}.
     * 
     * @param bindingFactory
     *            la factoría para la creación de componentes.
     * @param fields
     *            los campos a renderizar.
     * @return un <em>builder</em> para la creación del formulario.
     */
    AbstractFormBuilder render(BindingFactory bindingFactory, Collection<Field> fields);
}
