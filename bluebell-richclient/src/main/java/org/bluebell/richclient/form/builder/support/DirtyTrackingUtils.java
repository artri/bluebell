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

package org.bluebell.richclient.form.builder.support;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.bluebell.richclient.application.RcpMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.form.FieldMetadata;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.form.support.AbstractFormModel;
import org.springframework.binding.form.support.DefaultFieldMetadata;
import org.springframework.binding.form.support.DefaultFormModel;
import org.springframework.binding.form.support.FormModelMediatingValueModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.DirtyTrackingValueModel;
import org.springframework.binding.value.support.ValueModelWrapper;
import org.springframework.richclient.application.Application;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Clase de ayuda para lidiar con el manejo de <em>dirties</em>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class DirtyTrackingUtils {

    /**
     * Mensaje para instancias de <code>FormModel</code>.
     */
    private static final MessageFormat ABSTRACT_FORM_MODEL_DIRTY_FMT = new MessageFormat(
            "Abstract form model dirty value and form models are {0}");

    /**
     * El identificador del <em>form model</em> y el nombre de la propiedad <em>dirty</em>.
     */
    private static final MessageFormat DIRTY_PROPERTY_FMT = new MessageFormat("{0}:{1}");

    /**
     * Mensaje para instancias de <code>FieldMetadata</code>.
     */
    private static final MessageFormat FIELD_METADATA_DIRTY_FMT = new MessageFormat("Fieldmetadata is {0} dirty");

    /**
     * Mensaje para instancias de <code>FormModel</code>.
     */
    private static final MessageFormat FORM_MODEL_DIRTY_FMT = new MessageFormat("Form model with id {0} is {1} dirty");

    /**
     * Log para la clase {@link DirtyTrackingUtils}.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DirtyTrackingUtils.class);

    /**
     * La cadena {@value #NOT}.
     */
    private static final String NOT = "not";

    /**
     * Transforma un <em>array</em> de cadenas en una cadena utilizando como separador el caracter ":".
     */
    private static Transformer stringArrayToStringTransformer = new Transformer() {

        public Object transform(Object input) {

            return StringUtils.join((String[]) input, ":");
        }
    };

    /**
     * Transforma una cadena en un <em>array</em> de cadenas utilizando como separador el caracter ":".
     */
    private static Transformer stringToStringArrayTransformer = new Transformer() {

        public Object transform(Object input) {

            return StringUtils.split((String) input, ":");
        }
    };

    /**
     * El campo de {@link FormModelMediatingValueModel} con la propiedad <code>trackDirty</code>.
     */
    private static Field trackDirtyField;

    /**
     * Mensaje para instancias de <code>DirtyTrackingValueModel</code>.
     */
    private static final MessageFormat VALUE_MODEL_DIRTY_FMT = new MessageFormat(
            "Value model is {0} dirty, new value is {1}");

    static {
        DirtyTrackingUtils.trackDirtyField = ReflectionUtils.findField(
        //
                FormModelMediatingValueModel.class, "trackDirty", boolean.class);

        ReflectionUtils.makeAccessible(DirtyTrackingUtils.trackDirtyField);

    }

    /**
     * Constructor privado ya que es una clase de utilidad.
     */
    private DirtyTrackingUtils() {

    }

    /**
     * Limpia todos los <em>value model</em> de un formulario.
     * 
     * @param formModel
     *            el modelo del formulario.
     */
    @SuppressWarnings("unchecked")
    public static void clearDirty(FormModel formModel) {

        // Limpiar los formularios hijos si es que los tuviera
        if (formModel instanceof HierarchicalFormModel) {
            final HierarchicalFormModel parentFormModel = (HierarchicalFormModel) formModel;

            for (final FormModel childFormModel : parentFormModel.getChildren()) {
                DirtyTrackingUtils.clearDirty(childFormModel);
            }
        }

        // Limpiar los value model del form model
        final Set<String> fieldNames = formModel.getFieldNames();
        for (final String fieldName : fieldNames) {
            final ValueModel valueModel = formModel.getValueModel(fieldName);
            if (valueModel != null) {
                DirtyTrackingUtils.clearDirty(valueModel);
            }
        }
    }

    /**
     * Si un <em>value model</em> estaba <em>dirty</em> entonces lo limpia.
     * 
     * @param valueModel
     *            el <em>value model</em>
     * 
     * @return <code>true</code> si fue posible limpiar el <em>dirty</em> y <code>false</code> en caso contrario.
     */
    public static Boolean clearDirty(ValueModel valueModel) {

        if (valueModel instanceof DirtyTrackingValueModel) {

            // Si el valueModel es DirtyTrackingValueModel entonces limpiarlo
            final DirtyTrackingValueModel dirtyTrackingValueModel = (DirtyTrackingValueModel) valueModel;
            dirtyTrackingValueModel.clearDirty();

            return Boolean.TRUE;

        } else if (valueModel instanceof ValueModelWrapper) {

            // Sino si es un ValueModelWrapper reinvocar recursivamente
            final ValueModelWrapper valueModelWrapper = (ValueModelWrapper) valueModel;

            return DirtyTrackingUtils.clearDirty(//
                    valueModelWrapper.getWrappedValueModel());
        }

        return Boolean.FALSE;
    }

    /**
     * Obtiene las propiedades <em>dirty</em> de un formulario.
     * 
     * @param formModel
     *            el modelo del formulario.
     * @return una lista de <em>arrays</em> de dos posiciones, donde la primera de ellas es el identificador del
     *         <em>form model</em> y la segunda la propiedad representada por el <em>value model</em>.
     * 
     * 
     * @see #getDirtyProperties(FormModel, Set)
     */
    @SuppressWarnings("unchecked")
    public static Collection<String[]> getDirtyProperties(FormModel formModel) {

        final Set<String> dirtyProperties = new HashSet<String>();

        // Obtener las propiedades dirty
        DirtyTrackingUtils.getDirtyProperties(formModel, dirtyProperties);

        // Ordenar las propiedades dirty
        return CollectionUtils.collect(
        //
                dirtyProperties, DirtyTrackingUtils.stringToStringArrayTransformer);
    }

    /**
     * Obtiene las propiedades <em>dirty</em> de un formulario.
     * 
     * @param formModel
     *            el modelo del formulario.
     * @return una colección de <em>arrays</em> de dos posiciones, donde la primera de ellas es la descripción del
     *         <em>form model</em> y la segunda la descripción del <em>value model</em>.
     * 
     * @see #getDirtyProperties(FormModel, Set)
     */
    public static Set<String[]> getI18nDirtyProperties(FormModel formModel) {

        final Collection<String[]> dirtyProperties = DirtyTrackingUtils.getDirtyProperties(formModel);
        final Set<String[]> i18nDirtyProperties = new HashSet<String[]>();

        for (final String[] dirtyProperty : dirtyProperties) {

            final String formModelKey = dirtyProperty[0] + ".caption";
            final String valueModelKey = dirtyProperty[0] + "." + dirtyProperty[1] + ".label";

            final String formModelMessage = Application.instance().getApplicationContext().getMessage(formModelKey,
                    null, formModelKey, Locale.getDefault());
            final String valueModelMessage = Application.instance().getApplicationContext().getMessage(valueModelKey,
                    null, valueModelKey, Locale.getDefault());

            i18nDirtyProperties.add(//
                    new String[] { formModelMessage, valueModelMessage });
        }

        return i18nDirtyProperties;
    }

    /**
     * Obtiene las propiedades <em>dirty</em> de un formulario formateadas en HTML.
     * 
     * @param formModel
     *            el modelo del formulario.
     * @return una cadena HTML con las propiedades <em>dirty</em> de un formulario.
     * 
     * @see #getDirtyProperties(FormModel, Set)
     */
    @SuppressWarnings("unchecked")
    public static String getI18nDirtyPropertiesHtmlString(FormModel formModel) {

        final String lineSeparator = System.getProperty("line.separator");

        // Obtener las propiedades dirty
        Collection<String[]> dirtyProperties = DirtyTrackingUtils.getI18nDirtyProperties(formModel);

        // Ordenarlas
        final List<String> orderedDirtyProperties = new ArrayList<String>(//
                CollectionUtils.collect(dirtyProperties, //
                        DirtyTrackingUtils.stringArrayToStringTransformer));
        Collections.sort(orderedDirtyProperties);
        dirtyProperties = CollectionUtils.collect(orderedDirtyProperties, //
                DirtyTrackingUtils.stringToStringArrayTransformer);

        // Construir la cadena HTML
        final StringBuffer sb = new StringBuffer("<ul>");
        sb.append(lineSeparator);
        for (final String[] dirtyProperty : dirtyProperties) {
            final String formModelMessage = dirtyProperty[0];
            final String valueModelMessage = dirtyProperty[1];

            sb.append("<li>");
            sb.append(formModelMessage);
            sb.append(": ");
            sb.append(valueModelMessage);
            sb.append("</li>");
            sb.append(lineSeparator);
        }
        sb.append("</ul>");

        return sb.toString();
    }

    /**
     * Trata un evento de cambio de valor de la propiedad <code>dirty</code> delegando en el manejador apropiado en
     * función de su tipo.
     * 
     * @param evt
     *            el evento de cambio del valor de una propiedad.
     */
    public static void handle(PropertyChangeEvent evt) {

        final String propertyName = evt.getPropertyName();

        if (FormModel.DIRTY_PROPERTY.equals(propertyName)) {
            DirtyTrackingUtils.handleIfDebugEnabled(evt.getSource());
        }
    }

    /**
     * Establece si se ha o no de gestionar el estado <em>dirty</em> de un <em>value model</em>.
     * 
     * @param valueModel
     *            el <em>value model</em>.
     * @param enable
     *            <code>true</code> si se ha de gestionar y <code>false</code> en caso contrario.
     * @return <code>true</code> en caso de éxito.
     */
    public static Boolean mustTrackDirty(ValueModel valueModel, Boolean enable) {

        final DirtyTrackingValueModel dirtyTrackingValueModel = DirtyTrackingUtils
                .unwrapDirtyTrackingValueModel(valueModel);

        Boolean success = Boolean.FALSE;
        if (dirtyTrackingValueModel != null) {
            try {
                DirtyTrackingUtils.trackDirtyField.set(//
                        dirtyTrackingValueModel, enable);

                success = Boolean.TRUE;
            } catch (final Exception e) {
                RcpMain.handleException(e);
            }
        }

        return success;
    }

    /**
     * Establece un nuevo valor en un <em>value model</em> sin el control de <em>dirties</em> habilitado.
     * 
     * @param valueModel
     *            el <em>value model</em>.
     * @param newValue
     *            el nuevo valor.
     * 
     * @return <code>true</code> si fue posible limpiar el <em>dirty</em> y <code>false</code> en caso contrario.
     */
    public static Boolean setValueWithoutTrackDirty(ValueModel valueModel, Object newValue) {

        // Establecer el valor
        valueModel.setValue(newValue);

        // Limpiar el dirty
        return DirtyTrackingUtils.clearDirty(valueModel);
    }

    /**
     * Obtiene a partir de un <em>value model</em> el <code>DirtyTrackingValueModel</code> más cercano en la cadena de
     * emvoltorios (si es que los hubiera).
     * 
     * @param valueModel
     *            el <em>value model</em>.
     * @return el <code>DirtyTrackingValueModel</code> o <code>null</code> si es que no lo hubiera.
     */
    public static DirtyTrackingValueModel unwrapDirtyTrackingValueModel(ValueModel valueModel) {

        DirtyTrackingValueModel dirtyTrackingValueModel = null;

        if (valueModel instanceof DirtyTrackingValueModel) {
            // Si es un DTVM entonces retornarlo
            dirtyTrackingValueModel = (DirtyTrackingValueModel) valueModel;
        } else if (valueModel instanceof ValueModelWrapper) {
            // Sino si es un envoltorio invocar recursivamente
            final ValueModelWrapper wrapper = (ValueModelWrapper) valueModel;
            dirtyTrackingValueModel = DirtyTrackingUtils.//
                    unwrapDirtyTrackingValueModel(wrapper.getWrappedValueModel());
        }

        return dirtyTrackingValueModel;
    }

    /**
     * Obtiene las propiedades <em>dirty</em> de un formulario en el formato especificado por
     * {@link #DIRTY_PROPERTY_FMT}.
     * 
     * @param formModel
     *            el modelo del formulario.
     * @param dirtyProperties
     *            las propiedades <em>dirty</em>, sin duplicados.
     */
    @SuppressWarnings("unchecked")
    private static void getDirtyProperties(FormModel formModel, Set<String> dirtyProperties) {

        Assert.notNull(formModel);
        Assert.notNull(dirtyProperties);

        // Si es un form model jerárquico consultar a los hijos
        if (formModel instanceof HierarchicalFormModel) {
            final FormModel[] childrenFormModels = ((HierarchicalFormModel) formModel).getChildren();
            for (final FormModel childFormModel : childrenFormModels) {
                DirtyTrackingUtils.getDirtyProperties(//
                        childFormModel, dirtyProperties);
            }
        }

        // Consultar los value models para ver si alguno está sucio
        final Set<String> propertyNames = formModel.getFieldNames();
        for (final String propertyName : propertyNames) {
            final ValueModel vm = formModel.getValueModel(propertyName);
            final DirtyTrackingValueModel dirtyTrackingValueModel = DirtyTrackingUtils
                    .unwrapDirtyTrackingValueModel(vm);

            if ((dirtyTrackingValueModel != null) && dirtyTrackingValueModel.isDirty()) {
                final String dirtyProperty = DirtyTrackingUtils.DIRTY_PROPERTY_FMT.format(//
                        new String[] { formModel.getId(), propertyName });
                dirtyProperties.add(dirtyProperty);
            }
        }
    }

    /**
     * Obtiene por reflectividad el valor de una propiedad de una clase.
     * 
     * @param <T>
     *            el tipo del valor devuelto.
     * @param target
     *            el objeto con la propiedad.
     * @param propertyName
     *            el nombre de la propiedad.
     * @param type
     *            el tipo de la propiedad.
     * 
     * @return el valor de la propiedad o <code>null</code> si no exisitiese.
     */
    @SuppressWarnings("unchecked")
    private static <T> T getValue(Object target, String propertyName, Class<T> type) {

        // TODO, (JAF), 20080912, probablemente haya demasiados métodos
        // repartidos por el FW de acceder a campos por reflectividad,
        // uniformalos en una clase de utilidad.

        Assert.notNull(propertyName, "[Assertion failed] - name is required; it must not be null");
        Assert.notNull(type, "[Assertion failed] - type is required; it must not be null");
        Assert.notNull(target, "[Assertion failed] - target is required; it must not be null");

        final Field field = ReflectionUtils.findField(target.getClass(), propertyName, type);
        try {
            if (field != null) {
                ReflectionUtils.makeAccessible(field);
                return (T) field.get(target);
            }
        } catch (final IllegalArgumentException e) {
            return null;
        } catch (final IllegalAccessException e) {
            return null;
        }

        return null;
    }

    /**
     * Maneja el cambio en un <code>AbstractFormmodel</code>.
     * 
     * @param formModel
     *            el <em>form model</em>.
     */
    @SuppressWarnings("unchecked")
    private static void handle(AbstractFormModel formModel) {

        Assert.notNull(formModel);

        final StringBuffer sb = new StringBuffer();
        final Set dirtyValueAndFormModels = DirtyTrackingUtils.getValue(//
                formModel, "dirtyValueAndFormModels", Set.class);
        for (final Object object : dirtyValueAndFormModels) {
            if (object instanceof ValueModel) {
                sb.append("Value model: " + ((ValueModel) object).getValue());
                sb.append("\n");
            } else if (object instanceof FormModel) {
                sb.append("Form model with id " + ((FormModel) object).getId());
                sb.append("\n");
            }
        }

        DirtyTrackingUtils.LOGGER.debug(//
                DirtyTrackingUtils.ABSTRACT_FORM_MODEL_DIRTY_FMT.format(//
                        new String[] { sb.toString() }));

    }

    /**
     * Maneja el cambio en la metainformación de un campo ( <code>DefaultFieldMetadata</code>).
     * 
     * @param fieldMetadata
     *            la metainformación de un campo.
     */
    private static void handle(DefaultFieldMetadata fieldMetadata) {

        Assert.notNull(fieldMetadata);

        final DirtyTrackingValueModel valueModel = DirtyTrackingUtils.getValue(//
                fieldMetadata, "valueModel", DirtyTrackingValueModel.class);
        final DefaultFormModel formModel = (DefaultFormModel) DirtyTrackingUtils.getValue(fieldMetadata, "formModel",
                FormModel.class);

        if (valueModel != null) {
            DirtyTrackingUtils.handleIfDebugEnabled(valueModel);
        }
        if (formModel != null) {
            DirtyTrackingUtils.handleIfDebugEnabled(formModel);
        }
    }

    /**
     * Maneja el cambio en un <em>value model</em> con gestión de <em>dirties</em>.
     * 
     * @param valueModel
     *            el <em>value model</em>.
     */
    private static void handle(DirtyTrackingValueModel valueModel) {

        Assert.notNull(valueModel);

        final Object value = valueModel.getValue();
        if (valueModel.isDirty()) {
            DirtyTrackingUtils.LOGGER.debug(//
                    DirtyTrackingUtils.VALUE_MODEL_DIRTY_FMT.format(//
                            new Object[] { StringUtils.EMPTY, value }));
        } else {
            DirtyTrackingUtils.LOGGER.debug(//
                    DirtyTrackingUtils.VALUE_MODEL_DIRTY_FMT.format(//
                            new Object[] { DirtyTrackingUtils.NOT, value }));
        }
    }

    /**
     * Maneja el cambio en la metainformación de un campo ( <code>FieldMetadata</code>).
     * 
     * @param fieldMetadata
     *            la metainformación de un campo.
     */
    private static void handle(FieldMetadata fieldMetadata) {

        Assert.notNull(fieldMetadata);

        if (fieldMetadata.isDirty()) {
            DirtyTrackingUtils.LOGGER.debug(//
                    DirtyTrackingUtils.FIELD_METADATA_DIRTY_FMT.format(//
                            new String[] { StringUtils.EMPTY }));
        } else {
            DirtyTrackingUtils.LOGGER.debug(//
                    DirtyTrackingUtils.FIELD_METADATA_DIRTY_FMT.format(//
                            new String[] { DirtyTrackingUtils.NOT }));
        }
    }

    /**
     * Maneja el cambio en un <em>form model</em>.
     * 
     * @param formModel
     *            el <em>form model</em>.
     */
    private static void handle(FormModel formModel) {

        Assert.notNull(formModel);

        final String formId = formModel.getId();
        if (formModel.isDirty()) {
            DirtyTrackingUtils.LOGGER.debug(//
                    DirtyTrackingUtils.FORM_MODEL_DIRTY_FMT.format(//
                            new String[] { formId, StringUtils.EMPTY }));
        } else {
            DirtyTrackingUtils.LOGGER.debug(//
                    DirtyTrackingUtils.FORM_MODEL_DIRTY_FMT.format(//
                            new String[] { formId, DirtyTrackingUtils.NOT }));
        }
    }

    /**
     * Maneja el objeto que ha provocado el cambio en el valor de la propiedad <code>dirty</code> siempre y cuando el
     * nivel de log sea mayor o igual que "DEBUG".
     * 
     * @param source
     *            el objeto que ha provocado el cambio.
     */
    private static void handleIfDebugEnabled(Object source) {

        Assert.notNull(source);

        if (DirtyTrackingUtils.LOGGER.isDebugEnabled()) {
            // Form models
            if (source instanceof FormModel) {
                DirtyTrackingUtils.handle((FormModel) source);
            }
            if (source instanceof AbstractFormModel) {
                DirtyTrackingUtils.handle((AbstractFormModel) source);
            }

            // Value models
            if (source instanceof DirtyTrackingValueModel) {
                DirtyTrackingUtils.handle((DirtyTrackingValueModel) source);
            }

            // Field metadata
            if (source instanceof FieldMetadata) {
                DirtyTrackingUtils.handle((FieldMetadata) source);
            }
            if (source instanceof DefaultFieldMetadata) {
                DirtyTrackingUtils.handle((DefaultFieldMetadata) source);
            }
        }
    }
}
