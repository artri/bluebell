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

/**
 * 
 */
package org.bluebell.richclient.form.util;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Set;

import javax.swing.table.TableModel;

import org.bluebell.binding.value.support.DirtyTrackingDCBCVM;
import org.bluebell.richclient.exceptionhandling.BbApplicationException;
import org.bluebell.richclient.util.AtomicObservableEventList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.MutablePropertyAccessStrategy;
import org.springframework.binding.PropertyMetadataAccessStrategy;
import org.springframework.binding.form.ConfigurableFormModel;
import org.springframework.binding.form.FieldMetadata;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.form.support.AbstractFormModel;
import org.springframework.binding.form.support.DefaultFieldMetadata;
import org.springframework.binding.form.support.DefaultFormModel;
import org.springframework.binding.support.BeanPropertyAccessStrategy;
import org.springframework.binding.validation.RichValidator;
import org.springframework.binding.validation.support.CompositeRichValidator;
import org.springframework.binding.validation.support.RulesValidator;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.BufferedCollectionValueModel;
import org.springframework.binding.value.support.DirtyTrackingValueModel;
import org.springframework.binding.value.support.ValueHolder;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.richclient.table.support.GlazedTableModel;
import org.springframework.rules.RulesSource;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;

/**
 * Extiende {@link FormModelHelper} con nuevas métodos de utilidad.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 * 
 * @see FormModelHelper
 * @see es.uniovi.Validating.Translation
 */
public class BbFormModelHelper extends FormModelHelper {

    /**
     * El campo de <code>AbstractFormModel</code> con la propiedad "dirtyValueAndFormModels".
     */
    public static final Field DIRTY_VALUE_AND_FORM_MODELS_FIELD = ReflectionUtils.findField(//
            AbstractFormModel.class, "dirtyValueAndFormModels", Set.class);

    static {
        ReflectionUtils.makeAccessible(BbFormModelHelper.DIRTY_VALUE_AND_FORM_MODELS_FIELD);
    }

    /**
     * Log para la clase {@link BbFormModelHelper}.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BbFormModelHelper.class);

    /**
     * El mensaje a mostrar cuando no es posible instanciar una colección usando su constructor por defecto.
     */
    private static final MessageFormat NO_INSTANTIABLE_FMT = new MessageFormat(
            "Error instantiating collection of type {0}, " + "does a default constructor exist?");

    /**
     * El mensaje a mostrar cuando no es posible validar el modelo usando Hibernate Validator.
     */
    private static final MessageFormat NO_VALIDABLE_FMT = new MessageFormat(
            "The form model with id {0} cannot be validated " + "using Hibernate Validator: unknown class");

    /**
     * Añade un <em>value model</em> <em>buffered</em> y con gestión de <em>dirty tracking</em> para una propiedad de
     * tipo colección a un <em>form model</em> dado.
     * <p>
     * Para el control de <em>dirty</em> se realiza una copia <em>deep</em> del valor original
     * 
     * @see #addCollectionValueModel(ValidatingFormModel, String, Boolean)
     * 
     * @param formModel
     *            el modelo.
     * @param propertyName
     *            el nombre de la propiedad.
     * 
     * @return el <em>value model</em>.
     */
    public static DirtyTrackingValueModel addCollectionValueModel(ValidatingFormModel formModel, String propertyName) {

        return BbFormModelHelper.addCollectionValueModel(formModel, propertyName, Boolean.TRUE);
    }

    /**
     * Añade un <em>value model</em> <em>buffered</em> y con gestión de <em>dirty tracking</em> para una propiedad de
     * tipo colección a un <em>form model</em> dado.
     * <p>
     * Esté método está pensado para renderizar propiedades mediante una tabla y realiza todas las acciones necesarias
     * para que la sincronización tenga éxito.
     * 
     * <p>
     * El diagrama de objetos resultante en cuanto a <em>value model</em>'s envueltos es el siguiente:
     * 
     * <pre>
     * 0)ValidatingFormValueModel     &lt;--a efectos del problema a resolver
     *          |                           carece de significación.
     *          |
     *        wraps
     *          |
     *          |
     * 1)DirtyTrackingDCBCVM          &lt;--asociado con un DefaultFieldMetadata
     *          |                           que sustituye en AbstractFormModel 
     *          |                           al del punto 3).
     *        wraps                   &lt;--sustituye al value model del punto    
     *          |                           2)en propertyValueModels de 
     *          |                           AbstractFormModel.
     * =========================================================================
     *          |     
     *          |
     * 2)ValidatingFormValueModel     &lt;--incluido en propertyValueModels 
     *          |                           de AbstractFormModel.
     *          |
     *        wraps
     *          |
     *          |
     * 3)FormModelMediatingValueModel &lt;--incluido en mediatingValueModel de
     *          |                           AbstractFormModel.
     *          |                     &lt;--asociado con un DefaultFieldMetadata   
     *        wraps                         con el que AbstractFormModel 
     *          |                           sincroniza su detección de dirties.
     *          |
     * 4)PropertyValueModel           &lt;--lee y escribe la propiedad del 
     *                                      form object.
     * </pre>
     * 
     * La propagación del <em>dirty</em> utiliza los <em>value model</em> del punto 3).
     * 
     * <pre>
     * ValueModel-- &gt; DefaultFieldMetadata-- &gt; AbstractFormModel
     * </pre>
     * 
     * Por lo tanto se requiere que los cambios en 1) se propaguen sobre 3) y esto se consigue añadiendo a la típica
     * llamada {@link AbstractFormModel#add(String, ValueModel)} una invocación a
     * {@link AbstractFormModel#add(String, ValueModel, FieldMetadata)} estando el tercer parámetro sincronizado con el
     * <em>value model</em> del punto 1) y el <em>form model</em> a la escucha de cambios sobre el mismo. Esto tiene
     * como consecuencia que el <em>form model</em> registra <em>dirties</em> por duplicado.
     * 
     * <p>
     * Para que este método funcione el <em>form model</em> pasado por parámetro debe ser una instancia de
     * {@link BbDefaultFormModel} ya que limpia los <em>dirties</em> tras un <em>commit</em> y además permanece a la
     * escucha de cambios en la metainformación pasada como parámetro a
     * {@link AbstractFormModel#add(String, ValueModel, FieldMetadata)}.
     * 
     * @param formModel
     *            el modelo.
     * @param propertyName
     *            el nombre de la propiedad.
     * @param deepCopyEnabled
     *            indica si el valor original se ha de recordar utilizando una copia <em>deep</em>.
     * 
     * @return el <em>value model</em> del punto 1).
     */
    @SuppressWarnings("unchecked")
    public static DirtyTrackingValueModel addCollectionValueModel(ValidatingFormModel formModel, String propertyName,
            Boolean deepCopyEnabled) {

        Assert.isInstanceOf(BbDefaultFormModel.class, formModel);

        final PropertyMetadataAccessStrategy pmas = ((AbstractFormModel) formModel).getPropertyAccessStrategy()
                .getMetadataAccessStrategy();
        final MutablePropertyAccessStrategy pas = ((AbstractFormModel) formModel).getFormObjectPropertyAccessStrategy();

        // 1. Crear un value model para la propiedad
        final ValueModel propertyVm = pas.getPropertyValueModel(propertyName);

        // 2. Crear un BufferedCollectionValueModel envolviendo el value model del punto 1.
        @SuppressWarnings("rawtypes")
        final DirtyTrackingDCBCVM collectionVm = new DirtyTrackingDCBCVM(//
                propertyVm, //
                pmas.getPropertyType(propertyName), //
                null, //
                propertyName, //
                deepCopyEnabled);

        // 3. Crear la metainformación de collectionVm.
        final FieldMetadata collectionVmMetadata = new DefaultFieldMetadata(//
                formModel, //
                collectionVm, //
                pmas.getPropertyType(propertyName), //
                !pmas.isWriteable(propertyName), //
                pmas.getAllUserMetadata(propertyName));

        // 4. Añadir el value model al form model para que se le registre un commit trigger.
        formModel.add(propertyName, collectionVm);

        // 5. Re-añadir el value model al form model para que este permanezca a la escucha de cambios sobre él
        formModel.add(propertyName, collectionVm, collectionVmMetadata);

        return collectionVm;
    }

    /**
     * Crea un nuevo <em>form model</em> hijo, internacionalizado, validable y <em>buffered</em> para representar
     * propiedades de tipo colección. que soporta propiedades de tipo {@link TranslationAwarePropertyAccessStrategyV1}.
     * <p>
     * <b>Nótese</b> que por defecto este modelo no realiza validación alguna.
     * 
     * @param parentFormModel
     *            el modelo padre.
     * @param clazz
     *            la clase de cada elemento de la colección.
     * @param propertyName
     *            el nombre de la propiedad a representar.
     * @return el modelo.
     * @see DefaultFormModel
     * @deprecated Se prefiere el empleo de {@link #createValidatingChildPageCollectionFormModel}
     * 
     * @see #createValidatingChildPageCollectionFormModel
     */
    @Deprecated
    public static ValidatingFormModel createChildPageCollectionFormModel(ValidatingFormModel parentFormModel, Class<//
            ? extends Object> clazz, String propertyName) {

        return BbFormModelHelper.createValidatingChildPageCollectionFormModel(parentFormModel, clazz, propertyName);
    }

    /**
     * Crea un <code>TableModel</code> internacionalizado.
     * 
     * @param eventList
     *            la lista de entidades a reflejar en la tabla.
     * @param columnPropertyNames
     *            los nombres de las propiedades a mostrar.
     * @param id
     *            el identificador del modelo.
     * @return el modelo de la tabla.
     */
    @SuppressWarnings("unchecked")
    public static TableModel createTableModel(EventList<?> eventList, String[] columnPropertyNames, String id) {

        return new GlazedTableModel(eventList, columnPropertyNames, id) {

            /**
             * This is a <code>Serializable</code
             */
            private static final long serialVersionUID = -5065011806899360449L;

            /**
             * {@inheritDoc}
             */
            @SuppressWarnings("rawtypes")
            @Override
            protected TransformedList createSwingThreadProxyList(EventList source) {

                return new AtomicObservableEventList(super.createSwingThreadProxyList(source));
            }
        };
    }

    /**
     * Crea un <code>TableModel</code> internacionalizado para una propiedad de un <code>FormModel</code>.
     * <p>
     * El value model debe existir y su valor debe ser de tipo {@link EventList}.
     * 
     * @param formModel
     *            el <em>form model</em>.
     * @param propertyName
     *            el nombre de la propiedad.
     * @param columnPropertyNames
     *            los nombres de las propiedades a mostrar.
     * @param id
     *            el identificador del modelo.
     * @return el modelo de la tabla.
     * 
     * @see #createValidatingTableModel(EventList , String[] , String)
     */
    public static TableModel createTableModel(FormModel formModel, String propertyName, String[] columnPropertyNames,
            String id) {

        // El value model de la propiedad
        final ValueModel valueModel = formModel.getValueModel(propertyName);
        org.springframework.util.Assert.isTrue(valueModel != null, "Value model must exist");

        // El valor de la propiedad debe ser una event list
        final Object value = valueModel.getValue();
        org.springframework.util.Assert.isInstanceOf(EventList.class, value);

        // Crear el table model
        return BbFormModelHelper.createTableModel((EventList<?>) value, columnPropertyNames, id);
    }

    /**
     * Crea un nuevo <em>form model</em> hijo, internacionalizado, validable y <em>buffered</em> para representar
     * propiedades de tipo colección que soporta propiedades de tipo {@link TranslationAwarePropertyAccessStrategyV1}.
     * <p>
     * <b>Nótese</b> que por defecto este modelo no realiza validación alguna.
     * 
     * @param <T>
     *            the type of the backing form object.
     * 
     * @param parentFormModel
     *            el modelo padre.
     * @param clazz
     *            la clase de cada elemento de la colección.
     * @param propertyName
     *            el nombre de la propiedad a representar.
     * @return el modelo.
     * 
     * @see org.springframework.richclient.form.AbstractMasterForm#AbstractMasterForm(HierarchicalFormModel , String ,
     *      String , Class )
     */
    @SuppressWarnings("unchecked")
    public static <T> ValidatingFormModel createValidatingChildPageCollectionFormModel(
            HierarchicalFormModel parentFormModel, Class<T> clazz, String propertyName) {

        return BbFormModelHelper.createValidatingChildPageCollectionFormModel(parentFormModel, clazz, propertyName,
                Set.class);
    }

    /**
     * 
     * Crea un nuevo <em>form model</em> hijo, internacionalizado, validable y <em>buffered</em> para representar
     * propiedades de tipo colección que soporta propiedades de tipo {@link TranslationAwarePropertyAccessStrategyV1}.
     * <p>
     * <b>Nótese</b> que por defecto este modelo no realiza validación alguna.
     * 
     * @param <T>
     *            the type of the collection items.
     * @param <Q>
     *            the type of the collection.
     * 
     * @param parentFormModel
     *            el modelo padre.
     * @param clazz
     *            la clase de cada elemento de la colección.
     * @param propertyName
     *            el nombre de la propiedad a representar
     * @param colClazz
     *            la clase de la colección.
     * @return el modelo.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object, Q extends Collection<T>> ValidatingFormModel //
    createValidatingChildPageCollectionFormModel(HierarchicalFormModel parentFormModel, Class<T> clazz,
            String propertyName, final Class<Q> colClazz) {

        // El value model de la propiedad a representar
        final ValueModel propertyVM = parentFormModel.getValueModel(propertyName);

        // Construir el buffered value model
        @SuppressWarnings("rawtypes")
        final DirtyTrackingDCBCVM<T> collectionVM = new DirtyTrackingDCBCVM(propertyVM, colClazz, clazz, propertyName);

        // Crear el nuevo modelo a partir de la estrategia
        final DefaultFormModel formModel = new BbDefaultFormModel(new BeanPropertyAccessStrategy(collectionVM), true) {

            @Override
            protected void handleSetNullFormObject() {

                if (BbFormModelHelper.LOGGER.isDebugEnabled()) {
                    BbFormModelHelper.LOGGER.debug("New form object is null; creating new col and disabling form");
                }
                try {
                    final Class<T> clazz = (Class<T>) BufferedCollectionValueModel.getConcreteCollectionType(colClazz);
                    this.getFormObjectHolder().setValue(clazz.newInstance());
                    this.setEnabled(Boolean.FALSE);
                } catch (final InstantiationException e) {
                    throw new BbApplicationException(BbFormModelHelper.NO_INSTANTIABLE_FMT.format(//
                            new String[] { colClazz.getName() }), e);
                } catch (final IllegalAccessException e) {
                    throw new BbApplicationException(BbFormModelHelper.NO_INSTANTIABLE_FMT.format(//
                            new String[] { colClazz.getName() }), e);
                }
            }
        };

        return formModel;
    }

    /**
     * Crea un modelo hijo, internacionalizado, validable y <em>buffered</em> dado su modelo padre.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param parentModel
     *            el modelo padre.
     * 
     * @return el modelo.
     * 
     * @see FormModelHelper#createChildPageFormModel(HierarchicalFormModel)
     */
    public static ValidatingFormModel createValidatingChildPageFormModel(HierarchicalFormModel parentModel) {

        return BbFormModelHelper.createValidatingChildPageFormModel(parentModel, null);
    }

    /**
     * Crea un modelo hijo, internacionalizado, validable y <em>buffered</em> dado su modelo padre e identificador.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param parentModel
     *            el modelo padre.
     * @param childPageName
     *            el identificador del modelo hijo.
     * 
     * @return el modelo.
     * 
     * @see FormModelHelper#createChildPageFormModel(HierarchicalFormModel, String)
     */
    public static ValidatingFormModel createValidatingChildPageFormModel(HierarchicalFormModel parentModel,
            String childPageName) {

        return BbFormModelHelper.createValidatingChildPageFormModel(parentModel, childPageName, parentModel
                .getFormObjectHolder());
    }

    /**
     * Crea un modelo hijo, internacionalizado, validable y <em>buffered</em> dado su modelo padre, identificador y
     * <em>path</em> de la propiedad a representar relativo al modelo padre.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param parentModel
     *            el modelo padre.
     * @param childPageName
     *            el identificador del modelo hijo.
     * @param childFormObjectPropertyPath
     *            el <em>path</em> de la propiedad.
     * 
     * @return el modelo.
     * 
     * @see FormModelHelper#createChildPageFormModel(HierarchicalFormModel, String, String)
     */
    public static ValidatingFormModel createValidatingChildPageFormModel(HierarchicalFormModel parentModel,
            String childPageName, String childFormObjectPropertyPath) {

        final ValueModel childValueModel = parentModel.getValueModel(childFormObjectPropertyPath);

        return BbFormModelHelper.createValidatingChildPageFormModel(parentModel, childPageName, childValueModel);
    }

    /**
     * Crea un modelo hijo, internacionalizado, validable y <em>buffered</em> dado su modelo padre, identificador y su
     * <em>value model</em>.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param parentModel
     *            el modelo padre.
     * @param childPageName
     *            el identificador del modelo hijo.
     * @param childFormObjectHolder
     *            el <em>value model</em> del formulario hijo.
     * 
     * @return el modelo.
     * 
     * @see FormModelHelper#createChildPageFormModel(HierarchicalFormModel, String, ValueModel)
     */
    public static ValidatingFormModel createValidatingChildPageFormModel(HierarchicalFormModel parentModel,
            String childPageName, ValueModel childFormObjectHolder) {

        final ValidatingFormModel child = BbFormModelHelper.createValidatingFormModel(childFormObjectHolder);

        child.setId(childPageName);
        parentModel.addChild(child);

        return child;
    }

    /**
     * Crea un modelo compuesto, internacionalizado, validable y <em>buffered</em> dado su <em>backing object</em>.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObject
     *            el objeto sobre el que se construye el modelo.
     * 
     * @return el modelo.
     * 
     * @see FormModelHelper#createCompoundFormModel(Object)
     */
    public static HierarchicalFormModel createValidatingCompoundFormModel(Object formObject) {

        return BbFormModelHelper.createValidatingCompoundFormModel(formObject, null);
    }

    /**
     * Crea un modelo compuesto, internacionalizado, validable y <em>buffered</em> dado su <em>backing object</em> e
     * identificador.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObject
     *            el objeto sobre el que se construye el modelo.
     * 
     * @param formId
     *            el identificador del modelo.
     * 
     * @return el modelo.
     * 
     * @see FormModelHelper#createCompoundFormModel(Object, String)
     */
    public static ValidatingFormModel createValidatingCompoundFormModel(Object formObject, String formId) {

        return BbFormModelHelper.createValidatingFormModel(formObject, formId);
    }

    /**
     * Crea un modelo compuesto, internacionalizado, validable y <em>buffered</em> dado el <em>value model</em> del
     * <em>backing object</em> e identificador.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObjectHolder
     *            el <em>value model</em> del objeto sobre el que se construye el modelo.
     * 
     * @param formId
     *            el identificador del modelo.
     * 
     * @return el modelo.
     * 
     * @see FormModelHelper#createCompoundFormModel(ValueModel, String)
     */
    public static ValidatingFormModel createValidatingCompoundFormModel(ValueModel formObjectHolder, String formId) {

        return BbFormModelHelper.createValidatingFormModel(formObjectHolder, formId);
    }

    /**
     * Crea un modelo internacionalizado, validable y <em>buffered</em> dado su <em>backing object</em>.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObject
     *            el objeto sobre el que se construye el modelo.
     * @return el modelo.
     * 
     * @see FormModelHelper#createFormModel(Object)
     */
    public static ValidatingFormModel createValidatingFormModel(Object formObject) {

        return BbFormModelHelper.createValidatingFormModel(formObject, true);
    }

    /**
     * Crea un modelo internacionalizado y validable dado su <em>backing object</em>.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObject
     *            el objeto sobre el que se construye el modelo.
     * @param bufferChanges
     *            <em>flag</em> indicando si el modelo es o no <em>buffered</em> .
     * @return el modelo.
     * 
     * @see FormModelHelper#createFormModel(Object, boolean)
     */
    public static ValidatingFormModel createValidatingFormModel(Object formObject, boolean bufferChanges) {

        return BbFormModelHelper.createValidatingFormModel(formObject, bufferChanges, null);
    }

    /**
     * Crea un modelo internacionalizado y validable dado su <em>backing object</em>, identificador y reglas de
     * validación.
     * 
     * @param formObject
     *            el objeto sobre el que se construye el modelo.
     * @param bufferChanges
     *            <em>flag</em> indicando si el modelo es o no <em>buffered</em> .
     * @param rulesSource
     *            las reglas de validación del modelo.
     * @param formId
     *            el identificador del modelo.
     * 
     * @return el modelo.
     * 
     * @see FormModelHelper#createFormModel(Object, boolean, RulesSource, String)
     */
    public static ValidatingFormModel createValidatingFormModel(Object formObject, boolean bufferChanges,
            RulesSource rulesSource, String formId) {

        final ValidatingFormModel formModel = BbFormModelHelper.//
                createValidatingFormModel(formObject, bufferChanges, formId);

        formModel.setValidator(new RulesValidator(formModel, rulesSource));

        return formModel;
    }

    /**
     * Crea un modelo internacionalizado y validable dado su <em>backing object</em>.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObject
     *            el objeto sobre el que se construye el modelo.
     * @param bufferChanges
     *            <em>flag</em> indicando si el modelo es o no <em>buffered</em> .
     * @param formId
     *            el identificador del modelo.
     * 
     * @return el modelo.
     * 
     * @see FormModelHelper#createFormModel(Object, boolean, String)
     */
    public static ValidatingFormModel createValidatingFormModel(//
            Object formObject, boolean bufferChanges, String formId) {

        return BbFormModelHelper.createValidatingFormModel(new ValueHolder(formObject), bufferChanges, formId);
    }

    /**
     * Crea un modelo internacionalizado, validable y <em>buffered</em> dado su <em>backing object</em> e identificador.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObject
     *            el objeto sobre el que se construye el modelo.
     * @param formId
     *            el identificador del modelo.
     * 
     * @return el modelo.
     * 
     * @see FormModelHelper#createFormModel(Object, String)
     */
    public static ValidatingFormModel createValidatingFormModel(Object formObject, String formId) {

        return BbFormModelHelper.createValidatingFormModel(formObject, true, formId);
    }

    /**
     * Crea un modelo internacionalizado, validable y <em>buffered</em> dado el <em>value model</em> del
     * <em>backing object</em>.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObjectHolder
     *            el <em>value model</em> del objeto sobre el que se construye el modelo.
     * @return el modelo.
     * 
     * @see FormModelHelper#createFormModel(ValueModel)
     */
    public static ValidatingFormModel createValidatingFormModel(ValueModel formObjectHolder) {

        return BbFormModelHelper.createValidatingFormModel(formObjectHolder, true, null);
    }

    /**
     * Crea un modelo internacionalizado y validable dado el <em>value model</em> del <em>backing object</em> y su
     * identificador.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObjectHolder
     *            el <em>value model</em> del objeto sobre el que se construye el modelo.
     * @param bufferChanges
     *            <em>flag</em> indicando si el modelo es o no <em>buffered</em> .
     * @param formId
     *            el identificador del modelo.
     * @return el modelo.
     * 
     * @see FormModelHelper#createFormModel(ValueModel, boolean, String)
     */
    public static ValidatingFormModel createValidatingFormModel(ValueModel formObjectHolder, boolean bufferChanges,
            String formId) {

        // Construir el modelo internacionalizable
        final DefaultFormModel formModel = new BbDefaultFormModel(new BeanPropertyAccessStrategy(formObjectHolder),
                bufferChanges);
        formModel.setId(formId);

        // Hacer que el modelo valide usando Hibernate Validator.
        final Object value = formObjectHolder.getValue();
        if (value != null) {
            // El validador de Hibernate

            // TODO (JAF), 20090913, HibernateValidator is now disabled, int the
            // future it will probably be replaced by
            // JSR 303 validator
            // final HibernateRulesValidator hibernateValidator = new
            // BbHibernateRulesValidator(formModel, value
            // .getClass());

            // El validador de Spring RCP
            final RulesValidator rcpValidator = new RulesValidator(formModel);
            final CompositeRichValidator compositeValidator = new CompositeRichValidator(
                    new RichValidator[] { rcpValidator });
            // final CompositeRichValidator compositeValidator = new
            // CompositeRichValidator(hibernateValidator,
            // rcpValidator);

            // Establecer el validador y activar la validación
            formModel.setValidator(compositeValidator);
            formModel.setValidating(Boolean.TRUE);
        } else {
            BbFormModelHelper.LOGGER.warn(BbFormModelHelper.NO_VALIDABLE_FMT.format(new String[] { formId }));
        }

        // Registrar el modelo para recibir notificaciones
        // ante cambios en el idioma.
        return formModel;
    }

    /**
     * Crea un modelo internacionalizado, validable y <em>buffered</em> dado el <em>value model</em> del
     * <em>backing object</em> y su identificador.
     * <p>
     * Para la validación utiliza las reglas definidas en el prtopio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObjectHolder
     *            el <em>value model</em> del objeto sobre el que se construye el modelo.
     * @param formId
     *            el identificador del modelo.
     * @return el modelo.
     * 
     * @see FormModelHelper#createFormModel(ValueModel, String)
     */
    public static ValidatingFormModel createValidatingFormModel(ValueModel formObjectHolder, String formId) {

        return BbFormModelHelper.createValidatingFormModel(formObjectHolder, true, formId);
    }

    /**
     * Crea un modelo internacionalizado, validable y <em>unbuffered</em> dado su <em>backing object</em>.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObject
     *            el objeto sobre el que se construye el modelo.
     * @return el modelo.
     * 
     * @see FormModelHelper#createUnbufferedFormModel(Object)
     */
    public static ValidatingFormModel createValidatingUnbufferedFormModel(Object formObject) {

        return BbFormModelHelper.createValidatingFormModel(formObject, false);
    }

    /**
     * Crea un modelo internacionalizado, validable y <em>unbuffered</em> dado su <em>backing object</em> e
     * identificador.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObject
     *            el objeto sobre el que se construye el modelo.
     * @param formId
     *            el identificador del modelo.
     * @return el modelo.
     * 
     * @see FormModelHelper#createUnbufferedFormModel(Object, String)
     */
    public static ValidatingFormModel createValidatingUnbufferedFormModel(Object formObject, String formId) {

        return BbFormModelHelper.createValidatingFormModel(formObject, false, formId);
    }

    /**
     * Crea un modelo internacionalizado, validable y <em>unbuffered</em> dado el <em>value model</em> del
     * <em>backing object</em> e identificador.
     * <p>
     * Para la validación utiliza las reglas definidas en el propio objeto de dominio usando Hibernate Validator.
     * 
     * @param formObjectHolder
     *            el <em>value model</em> del objeto sobre el que se construye el modelo.
     * @param formId
     *            el identificador del modelo.
     * @return el modelo.
     * 
     * @see FormModelHelper#createUnbufferedFormModel(ValueModel, String)
     */
    public static ValidatingFormModel createValidatingUnbufferedFormModel(ValueModel formObjectHolder, String formId) {

        return BbFormModelHelper.createValidatingFormModel(formObjectHolder, false, formId);
    }

    /**
     * Habilita un <em>form model</em> definitivamente, incluyendo los modelos jerarquicamente superiores.
     * 
     * @param formModel
     *            el modelo a habilitar.
     */
    public static void enableFormModel(HierarchicalFormModel formModel) {

        if (formModel.getParent() != null) {
            BbFormModelHelper.enableFormModel(formModel.getParent());
        }

        if (ConfigurableFormModel.class.isAssignableFrom(formModel.getClass())) {
            ((ConfigurableFormModel) formModel).setEnabled(Boolean.TRUE);
        }
    }

}
