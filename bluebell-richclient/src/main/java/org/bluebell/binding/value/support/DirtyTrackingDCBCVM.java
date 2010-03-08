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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.InstanceofPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.BufferedCollectionValueModel;
import org.springframework.binding.value.support.DeepCopyBufferedCollectionValueModel;
import org.springframework.binding.value.support.DirtyTrackingValueModel;
import org.springframework.binding.value.support.ObservableEventList;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.util.ReflectionUtils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * Implementación de <em>buffered value model</em> con capacidad de <em>dirty tracking</em> e indicado para colecciones.
 * 
 * @param <T>
 *            la clase a la que pertenece cada elemento de la colección.
 * 
 * @see org.springframework.richclient.form.AbstractMasterForm.DirtyTrackingDCBCVM
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class DirtyTrackingDCBCVM<T> extends DeepCopyBufferedCollectionValueModel implements DirtyTrackingValueModel {

    /**
     * El campo con el <em>bufferedListModel</em>.
     */
    private static final Field BUFFERED_LIST_MODEL_FIELD = ReflectionUtils.findField(//
            BufferedCollectionValueModel.class, "bufferedListModel", //
            ObservableList.class);

    /**
     * Log para la clase {@link DirtyTrackingDCBCVM}.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DirtyTrackingDCBCVM.class);
    static {
        ReflectionUtils.makeAccessible(//
                DirtyTrackingDCBCVM.BUFFERED_LIST_MODEL_FIELD);
    }

    /**
     * La clase del <em>bean</em> con la colección.
     */
    private Class<Object> beanClass;

    /**
     * El nombre de la propiedad de {@link #beanClass} con la colección.
     */
    private String beanPropertyName;

    /**
     * Indica si se ha de utilizar copia en profundidad para recordar el valor original.
     */
    private boolean deepCopyEnabled = Boolean.FALSE;

    /**
     * Indica si el estado del <em>value model</em> es <em>dirty</em>.
     */
    private boolean dirty = Boolean.FALSE;

    /**
     * El valor anterior de la propiedad <code>dirty</code> del <em>value model</em>.
     */
    private boolean oldDirty = Boolean.FALSE;

    /**
     * Recuerda el valor original de la propiedad.
     */
    private Collection<T> originalValue;

    /**
     * Construye un nuevo <code>DirtyTrackingDCBCVM</code> utilizando copia en profundidad para recordar el valor
     * original.
     * 
     * @param wrappedModel
     *            el <em>value model</em> que va a ser envuelto.
     * @param wrappedType
     *            la clase de cada elemento de la colección.
     * @param beanClass
     *            la clase del <em>bean</em> con la colección.
     * @param beanPropertyName
     *            el nombre de la propiedad del <em>bean</em> con la colección.
     */
    public DirtyTrackingDCBCVM(ValueModel wrappedModel, Class<T> wrappedType, Class<Object> beanClass,
            String beanPropertyName) {

        this(wrappedModel, wrappedType, beanClass, beanPropertyName, Boolean.TRUE);
    }

    /**
     * Construye un nuevo <code>DirtyTrackingDCBCVM</code>.
     * 
     * @param wrappedModel
     *            el <em>value model</em> que va a ser envuelto.
     * @param wrappedType
     *            la clase de cada elemento de la colección.
     * @param beanClass
     *            la clase del <em>bean</em> con la colección.
     * @param beanPropertyName
     *            el nombre de la propiedad del <em>bean</em> con la colección.
     * @param deepCopyEnabled
     *            indica si se ha de utilizar copia en profundidad para recordar el valor original.
     */
    public DirtyTrackingDCBCVM(ValueModel wrappedModel, Class<T> wrappedType, Class<Object> beanClass,
            String beanPropertyName, Boolean deepCopyEnabled) {

        super(wrappedModel, wrappedType);
        this.setDirty(Boolean.FALSE);
        this.setBeanClass(beanClass);
        this.setBeanPropertyName(beanPropertyName);
        this.setDeepCopyEnabled(deepCopyEnabled);

        this.clearDirty();
    }

    /**
     * Resetea el <em>value model</em> indicando que ya no está sucio.
     */
    public void clearDirty() {

        this.setDirty(Boolean.FALSE);
        this.valueUpdated();
    }

    /**
     * Indica si el modelo está o no sucio.
     * 
     * @return <code>true</code> si el estado es dirty y <code>false</code> en caso contrario.
     */
    public boolean isDirty() {

        return this.dirty;
    }

    /**
     * Devuelve el <em>value model</em> a su valor original.
     */
    public void revertToOriginal() {

        this.revert();
    }

    /**
     * Establece el nuevo valor y se reponsabiliza de almacenar el valor original así como de limpiar el <em>dirty</em>
     * siempre y cuando el nuevo valor sea igual al original.
     * 
     * @param value
     *            el valor a establecer.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {

        final Collection<T> valueToSet = this.getValueToSet(value);
        final Collection<T> wrappedCollection = (Collection<T>) this.getWrappedValueModel().getValue();

        // 1) Establecer el valor apropiado sólo si es necesario
        this.setValueIfChanged(valueToSet);

        // 2) Si es el primer cambio recordar el valor original.
        final Boolean isFirstChange = !this.isDirty();
        if (isFirstChange) {
            this.validateCollection(wrappedCollection);

            // (JAF), 20090610, en este punto el bufferedListModel es una
            // deep copy del valor pasado como parámetro.
            this.rememberOriginalValue();
        }

        // 3) Si el nuevo valor es igual que el recordado entonces el
        // formulario no está dirty (probablemente se trate de un revert)
        final Boolean maybeReverting = !this.getValueChangeDetector().hasValueChanged(valueToSet, wrappedCollection);
        if (maybeReverting) {
            // (JAF), 20080929, no se utiliza "this.hasValueChanged" ya que
            // hace comprobaciones adicionales que hacen romper el algoritmo
            this.clearDirty();
        }
    }

    /**
     * Crea una <code>ObservableList</code> para que pueda ser usada como raíz de la lista de eventos del form model del
     * maestro.
     * 
     * @return una lista observable.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected ObservableList createBufferedListModel() {

        return new ObservableEventList(new BasicEventList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fireListModelChanged() {

        super.fireListModelChanged();

        // Comprobar si ha cambiado el valor de la propiedad dirty
        final Object newValue = this.getValue();
        this.setDirty(this.hasValueChanged(this.getOriginalValue(), newValue));
        this.valueUpdated();
    }

    /**
     * Obtiene el valor a establecer en el método {@link #setValue(Object)}.
     * <p>
     * Si el valor pasado como parámetro es un <em>bean</em> devuelve su propiedad {@link #getBeanPropertyName()}, en
     * caso contrario devuelve el propio valor.
     * 
     * @param value
     *            el valor pasado a {@link #setValue(Object)}.
     * @return el valor a establecer.
     */
    @SuppressWarnings("unchecked")
    protected Collection<T> getValueToSet(Object value) {

        final Boolean valueIsOfBeanClass = ((value != null) && value.getClass().equals(this.getBeanClass()));

        Object valueToSet = value;
        if (valueIsOfBeanClass) {
            final BeanWrapper beanWrapper = new BeanWrapperImpl(value);
            valueToSet = beanWrapper.getPropertyValue(this.getBeanPropertyName());
        }

        return (Collection<T>) valueToSet;
    }

    /**
     * Modifica el comportamiento de {@link BufferedCollectionValueModel#hasValueChanged} de modo que si uno de los
     * valores pasados como parámetro es una <code>EventList</code> entonces siempre devuelva que el valor ha cambiado.
     * <p>
     * Esto es un <b>hack</b> añadido como consecuencia de que si el valor original es una colección vacía (y
     * típicamente lo será si el <em>form object</em> del <em>form model</em> está sin inicializar), entonces la
     * propiedad <code>BufferedCollectionValueModel#buffering</code> será siempre <code>false</code>. Mientras así
     * suceda, la operación <code>revert</code> dejará de funcionar.
     * <p>
     * Una forma de evitar este comporamiento es definir que cuando se comparen colecciones, y una de ellas sea de tipo
     * <code>EventList</code>, se devuelva <code>true</code> indicando, <b>bajo cualquier supuesto</b>, que el valor ha
     * cambiado. Automáticamente la propiedad <code>BufferedCollectionValueModel#buffering</code> pasa a ser
     * <code>true</code> y el funcionamiento se normaliza.
     * 
     * 
     * @param oldValue
     *            el valor anterior a comparar.
     * @param newValue
     *            el nuevo valor a comparar.
     * @return <code>true</code> si el valor ha cambiado y <code>false</code> en caso contrario.
     */
    @Override
    protected boolean hasValueChanged(Object oldValue, Object newValue) {

        // HACK, 20090127, (JAF), esto es claramente un hack por las razones
        // aducidas en el Javadoc. El caso es que el
        // BufferedCollectionValueModel no detectaba cambio alguno y por
        // tanto el revert no hacía su labor.
        if ((oldValue != null) && (newValue != null) //
                && (EventList.class.isAssignableFrom(oldValue.getClass()) //
                || EventList.class.isAssignableFrom(newValue.getClass()))) {

            return Boolean.TRUE;
        }

        return super.hasValueChanged(oldValue, newValue);
    }

    /**
     * Recuerda el valor original.
     * 
     * @see #setOriginalValue(Collection)
     * @see #getBufferedListModel()
     */
    @SuppressWarnings("unchecked")
    protected void rememberOriginalValue() {

        // (JAF), 20081003, originalValue es una copia del valor original, de
        // no hacerlo así las colecciones actual y original compartirían
        // referencias por lo que no sería posible compararlas
        this.setOriginalValue(this.getBufferedListModel());
    }

    /**
     * Establece el valor indicado únicamente si ha cambiado.
     * 
     * @param value
     *            el valor a establecer.
     * @return <code>true</code> si ha sido necesario establecer el valor y <code>false</code> en caso contrario.
     */
    protected Boolean setValueIfChanged(Collection<T> value) {

        // (JAF), 20090614, esta comprobación se hace necesario ya que el método hasValueChanged de la clase padre
        // devuelve true aún a pesar de que sean colecciones equivalentes.

        // (JAF), 20090614, no se utiliza "this.hasValueChanged" ya que hace comprobaciones adicionales que hacen romper
        // el algoritmo
        final Boolean hasValueChanged = this.getValueChangeDetector().hasValueChanged(this.getOriginalValue(), value);
        if (hasValueChanged) {
            super.setValue(value);
        }

        return hasValueChanged;
    }

    /**
     * Valida que todos los elementos de una colección sean <code>Serializable</code>'s. En caso contrario eleva una
     * excepción.
     * 
     * @param collection
     *            la colección a validar.
     */
    protected void validateCollection(Collection<T> collection) {

        // (JAF), 20080107, si deepCopy está habilitado entonces todos los elementos de la colección deberían de ser
        // serializables, de no ser así la copia provocará una excepción silenciosa
        if (this.isDeepCopyEnabled() && (collection != null)) {

            CollectionUtils.predicatedCollection(collection, InstanceofPredicate.getInstance(Serializable.class));
        }
    }

    /**
     * Notifica si ha cambiado el valor de la propiedad <em>dirty</em>.
     */
    protected void valueUpdated() {

        final boolean currentDirty = this.isDirty();
        if (this.getOldDirty() != currentDirty) {
            this.setOldDirty(currentDirty);
            this.firePropertyChange(DirtyTrackingValueModel.DIRTY_PROPERTY, !currentDirty, currentDirty);
        }
    }

    /**
     * Obtiene la clase del <em>bean</em> con la colección.
     * 
     * @return la clase del <em>bean</em> con la colección.
     */
    private Class<Object> getBeanClass() {

        return this.beanClass;
    }

    /**
     * Obtiene el nombre de la propiedad de {@link #beanClass} con la colección.
     * 
     * @return el nombre de la propiedad de {@link #beanClass} con la colección.
     */
    private String getBeanPropertyName() {

        return this.beanPropertyName;
    }

    /**
     * Obtiene por reflectividad el valor de <code>bufferedListModel</code>.
     * 
     * @return el <code>bufferedListModel</code>.
     */
    private ObservableList getBufferedListModel() {

        try {
            return (ObservableList) DirtyTrackingDCBCVM.BUFFERED_LIST_MODEL_FIELD.get(this);
        } catch (final Exception e) {
            DirtyTrackingDCBCVM.LOGGER.info(e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el valor anterior de la propiedad <code>dirty</code> del <em>value model</em>.
     * 
     * @return el valor anterior de la propiedad <code>dirty</code> del <em>value model</em>.
     */
    private boolean getOldDirty() {

        return this.oldDirty;
    }

    /**
     * Obtiene el valor original de la propiedad.
     * 
     * @return el valor original de la propiedad.
     */
    private Collection<T> getOriginalValue() {

        if (this.originalValue == null) {
            this.originalValue = new ArrayList<T>();
        }

        return this.originalValue;
    }

    /**
     * Indica si se ha de utilizar copia en profundidad para recordar el valor original.
     * 
     * @return si se ha de utilizar copia en profundidad para recordar el valor original.
     */
    private boolean isDeepCopyEnabled() {

        return this.deepCopyEnabled;
    }

    /**
     * Establece la clase del <em>bean</em> con la colección.
     * 
     * @param beanClass
     *            la clase del <em>bean</em> con la colección.
     */
    private void setBeanClass(Class<Object> beanClass) {

        this.beanClass = beanClass;
    }

    /**
     * Establece el nombre de la propiedad de {@link #beanClass} con la colección.
     * 
     * @param beanPropertyName
     *            el nombre de la propiedad de {@link #beanClass} con la colección.
     */
    private void setBeanPropertyName(String beanPropertyName) {

        this.beanPropertyName = beanPropertyName;
    }

    /**
     * Establece si se ha de utilizar copia en profundidad para recordar el valor original.
     * 
     * @param deepCopyEnabled
     *            si se ha de utilizar copia en profundidad para recordar el valor original.
     */
    private void setDeepCopyEnabled(boolean deepCopyEnabled) {

        this.deepCopyEnabled = deepCopyEnabled;
    }

    /**
     * Establece si el <em>value model</em> es <em>dirty</em>.
     * 
     * @param dirty
     *            el nuevo valor.
     */
    private void setDirty(boolean dirty) {

        this.dirty = dirty;
    }

    /**
     * Establece el valor anterior de la propiedad <code>dirty</code> del <em>value model</em>.
     * 
     * @param oldDirty
     *            el valor anterior de la propiedad <code>dirty</code> del <em>value model</em>.
     */
    private void setOldDirty(boolean oldDirty) {

        this.oldDirty = oldDirty;
    }

    /**
     * Obtiene el valor original de la propiedad.
     * 
     * @param originalValue
     *            el valor original de la propiedad.
     */
    private void setOriginalValue(Collection<T> originalValue) {

        this.originalValue = originalValue;
    }
}
