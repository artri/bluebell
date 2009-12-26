/**
 * 
 */
package org.bluebell.richclient.form.support;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bluebell.richclient.form.util.EntityRenderer;
import org.bluebell.richclient.form.util.FieldsFilter;
import org.springframework.richclient.form.binding.BindingFactory;
import org.springframework.richclient.form.builder.TableFormBuilder;

/**
 * Implementación de <code>FieldFilter</code> y <code>EntityRenderer</code> que renderiza las entidades en forma de
 * tabla.
 * 
 * @param <T>
 *            el tipo de la entidad a renderizar.
 * @param <ID>
 *            el tipo del identificador de la entidad a renderizar.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class DefaultEntityRenderer<T, ID extends Serializable> implements FieldsFilter<T, ID>, EntityRenderer {

    /**
     * Obtiene los campos de la entidad que se deben renderizar.
     * 
     * @param clazz
     *            la clase de la que obtener los campos.
     * @return los campos a renderizar.
     */
    public Collection<Field> getFields(Class<T> clazz) {

        final List<Field> fields = new ArrayList<Field>();

        for (final Field field : clazz.getDeclaredFields()) {
            if (this.isValid(field)) {
                fields.add(field);
            }
        }

        return fields;
    }

    /**
     * Renderiza la entidad en forma de tabla utilizando los campos devueltos por {@link #getFields}.
     * 
     * @param bindingFactory
     *            la factoría para la creación de componentes.
     * @param fields
     *            los campos a renderizar.
     * @return un <em>builder</em> para la creación del formulario.
     */
    public TableFormBuilder render(BindingFactory bindingFactory, Collection<Field> fields) {

        final TableFormBuilder formBuilder = new TableFormBuilder(bindingFactory);
        formBuilder.setLabelAttributes("colGrId=label colSpec=right:pref");

        formBuilder.row();
        for (final Field field : fields) {
            formBuilder.add(field.getName());
            formBuilder.row();
        }

        return formBuilder;
    }

    /**
     * Determina si un campo es válido o no.
     * <p>
     * Un campo es válido si sólo si no es estático.
     * <p>
     * TODO refinar este comportamiento.
     * 
     * @param field
     *            el campo.
     * @return <code>true</code> si el campo es válido y <code>false</code> en caso contrario.
     */
    private boolean isValid(Field field) {

        final int modifiers = field.getModifiers();

        final boolean isStatic = Modifier.isStatic(modifiers);
        final boolean isCollection = Collection.class.isAssignableFrom(field.getType());

        return !isStatic && !isCollection;
    }
}
