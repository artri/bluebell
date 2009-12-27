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

/**
 * 
 */
package org.bluebell.richclient.application.docking.vldocking;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.Map;

import org.bluebell.richclient.application.support.FormBackedView;
import org.bluebell.richclient.form.FormInstantiationException;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyBatchUpdateException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.richclient.application.View;
import org.springframework.richclient.application.docking.vldocking.VLDockingViewDescriptor;
import org.springframework.richclient.form.AbstractForm;

/**
 * Descriptor de vista, especialización de {@link VLDockingViewDescriptor} que permite crear vistas a partir de un
 * formulario.
 * <p>
 * Instancia los formularios utilizando su constructor por defecto o, si se han establecido las propiedades
 * {@link #bean} y {@link #propertyName}, un constructor con estos dos parámetros.
 * 
 * @param <T>
 *            el tipo del formulario.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class FormBackedVLDockingViewDescriptor<T extends AbstractForm> extends VLDockingViewDescriptor {

    /**
     * Mensaje asociado a la excepción que se produce si no es posible instanciar el formulario.
     */
    private static final String INSTANTIATION_ERROR_MESSAGE = "Error while trying to instantiate form";

    /**
     * Mensaje asociado a la excepción que se produce si no hay un constructor apropiado.
     */
    private static final MessageFormat NO_CONSTRUCTOR_ERROR_MESSAGE = new MessageFormat(
            "No constructor with {0} parameters found");

    /**
     * Mensaje asociado a la excepción que se produce si no es posible establecer alguna propiedad.
     */
    private static final String PROPERTY_ERROR_MESSAGE = "Cannot stablish property";

    /**
     * <code>ApplicationEventMulticaster</code> para el registro de los eventos.
     */
    private final ApplicationEventMulticaster applicationEventMulticaster;

    /**
     * El <em>bean</em> a partir del cual instanciar el formulario.
     */
    private Object bean;

    /**
     * La clase del formulario.
     */
    private Class<T> formClass;

    /**
     * Un mapa con las propiedades del formulario a instanciar; la clave es el nombre de la propiedad y su valor la
     * referencia a establecer.
     */
    private Map<String, Object> formProperties;

    /**
     * El nombre de la propiedad a partir de la cual instanciar el formulario.
     */
    private String propertyName;

    /**
     * Construye el descriptor de la vista y establece que la clase de la vista es {@link FormBackedView}.
     */
    public FormBackedVLDockingViewDescriptor() {

        super();
        this.setViewClass(FormBackedView.class);

        // HACK method para obtener el application event multicaster
        this.applicationEventMulticaster = (ApplicationEventMulticaster) //
        this.getApplicationContext().getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME);
    }

    /**
     * Obtiene la clase del formulario.
     * 
     * @return la clase del formulario.
     */
    public Class<T> getFormClass() {

        return this.formClass;
    }

    /**
     * Instancia un formulario utilizando un constructor sin parámetros.
     * <p>
     * También establece las propiedades especificadas en {@link #formProperties} utilizando la convención
     * <em>javabean</em>.
     * 
     * @return el formulario.
     * 
     * @see #instantiate(Class[], Object[])
     */
    @SuppressWarnings("unchecked")
    public T instantiateForm() {

        final Class[] parameterTypes = new Class[] {};
        final Object[] parameterValues = new Object[] {};

        return this.instantiate(this.getFormClass(), parameterTypes, parameterValues);
    }

    /**
     * Instancia un formulario utilizando un constructor con dos parámetros, respectivamente:
     * <ol>
     * <li>{@link #bean}
     * <li>{@link #propertyName}
     * </ol>
     * <p>
     * También establece las propiedades especificadas en {@link #formProperties} utilizando la convención
     * <em>javabean</em>.
     * 
     * @param bean
     *            el primer parámetro del constructor.
     * @param propertyName
     *            el segundo parámetro del constructor.
     * 
     * @return el formulario.
     * 
     * @see #instantiate(Class[], Object[])
     */
    @SuppressWarnings("unchecked")
    public T instantiateForm(Object bean, String propertyName) {

        final Class[] parameterTypes = new Class[] { Object.class, String.class };
        final Object[] parameterValues = new Object[] { bean, propertyName };

        return this.instantiate(this.getFormClass(), parameterTypes, parameterValues);
    }

    /**
     * Establece el <em>bean</em> a partir del cual instanciar el formulario.
     * 
     * @param bean
     *            el <em>bean</em>.
     */
    public void setBean(Object bean) {

        this.bean = bean;
    }

    /**
     * Establece la clase del formulario.
     * 
     * @param formClass
     *            la clase del formulario.
     */
    public void setFormClass(Class<T> formClass) {

        this.formClass = formClass;
    }

    /**
     * Establece las propiedades del formulario.
     * 
     * @param formProperties
     *            las propieades del formulario.
     */
    public void setFormProperties(Map<String, Object> formProperties) {

        this.formProperties = formProperties;
    }

    /**
     * Establece el nombre de la propiedad a partir de la cual instanciar el formulario.
     * 
     * @param propertyName
     *            el nombre de la propiedad.
     */
    public void setPropertyName(String propertyName) {

        this.propertyName = propertyName;
    }

    /**
     * Asegura que la clase de la vista es asignable a {@link FormBackedView}, en caso negativo provoca una
     * <code>IllegalArgumentException</code>.
     * 
     * @param viewClass
     *            la clase de la vista.
     * 
     * @see VLDockingViewDescriptor#setViewClass(Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void setViewClass(Class viewClass) {

        if (!FormBackedView.class.isAssignableFrom(viewClass)) {
            throw new IllegalArgumentException("FormBackedView is not assignable from " + viewClass);
        } else {
            super.setViewClass(viewClass);
        }
    }

    /**
     * Crea una vista de tipo {@link FormBackedView} a partir de un formulario de tipo {@link #formClass}.
     * 
     * @return la vista creada.
     * 
     * @see #getForm()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected View createView() {

        // Crear la vista
        final FormBackedView<T> view = (FormBackedView<T>) super.createView();

        // Obtener el formulario y establecer su formulario y accesor.
        final T form = this.createForm();
        view.setBackingForm(form);

        // Se registran listeners para los formularios que implementan el
        // interface ApplicationListener
        if (form instanceof ApplicationListener) {
            final ApplicationListener applicationListener = (ApplicationListener) form;
            this.applicationEventMulticaster.addApplicationListener(applicationListener);
        }

        return view;
    }

    /**
     * Instancia un formulario de tipo {@link #formClass} utilizando:
     * <dl>
     * <dt>{@link #instantiateForm(Object, String)}
     * <dd>Si el <em>bean</em> y el nombre de la propiedad son diferentes de <code>null</code>.
     * <dt>{@link #instantiateForm()}
     * <dd>En caso contrario.
     * </dl>
     * 
     * @return el formulario instanciado.
     * 
     * @see #instantiateForm()
     * @see #instantiateForm(Object, String)
     */
    private T createForm() {

        if ((this.getBean() != null) && (this.getPropertyName() != null)) {
            return this.instantiateForm(this.getBean(), this.getPropertyName());
        } else {
            return this.instantiateForm();
        }
    }

    /**
     * Obtiene el <em>bean</em> a partir del cual instanciar el formulario.
     * 
     * @return el <em>bean</em>.
     */
    private Object getBean() {

        return this.bean;
    }

    /**
     * Obtiene las propiedades del formulario.
     * 
     * @return las propiedades del formulario.
     */
    private Map<String, Object> getFormProperties() {

        return this.formProperties;
    }

    /**
     * Obtiene el nombre de la propiedad a partir de la cual instanciar el formulario.
     * 
     * @return el nombre de la propiedad.
     */
    private String getPropertyName() {

        return this.propertyName;
    }

    /**
     * Instancia un formulario de tipo <code>T</code> dados los tipos de los parámetros de su constructor y sus valores.
     * <p>
     * En caso de producirse una excepción la envuelve y eleva utilizando una {@link FormInstantiationException} con el
     * mensaje apropiado.
     * 
     * @param clazz
     *            la clase del formulario a instanciar.
     * @param parameterTypes
     *            los tipos de los parámetros.
     * @param parameterValues
     *            los valores de los parámetros.
     * @return el formulario.
     */
    @SuppressWarnings("unchecked")
    private T instantiate(Class<T> clazz, Class[] parameterTypes, Object[] parameterValues) {

        T instance = null;
        try {
            final Constructor<T> constructor = clazz.getConstructor(parameterTypes);

            // Instanciar el formulario
            instance = (T) BeanUtils.instantiateClass(constructor, parameterValues);

            // Establecer las propiedades del formulario
            if (this.getFormProperties() != null) {
                final BeanWrapper wrapper = new BeanWrapperImpl(instance);
                wrapper.setPropertyValues(this.getFormProperties());
            }
        } catch (final SecurityException e) {
            throw new FormInstantiationException(//
                    FormBackedVLDockingViewDescriptor.NO_CONSTRUCTOR_ERROR_MESSAGE.format(//
                            new Object[] { parameterTypes.length }), e, clazz.getName());
        } catch (final NoSuchMethodException e) {
            throw new FormInstantiationException(//
                    FormBackedVLDockingViewDescriptor.NO_CONSTRUCTOR_ERROR_MESSAGE.format(//
                            new Object[] { parameterTypes.length }), clazz.getName());
        } catch (final BeanInstantiationException e) {
            throw new FormInstantiationException(//
                    FormBackedVLDockingViewDescriptor.INSTANTIATION_ERROR_MESSAGE, e, clazz.getName());
        } catch (final InvalidPropertyException e) {
            throw new FormInstantiationException(//
                    FormBackedVLDockingViewDescriptor.PROPERTY_ERROR_MESSAGE, e, clazz.getName());
        } catch (final PropertyBatchUpdateException e) {
            throw new FormInstantiationException(//
                    FormBackedVLDockingViewDescriptor.PROPERTY_ERROR_MESSAGE, e, clazz.getName());
        } catch (final PropertyAccessException e) {
            throw new FormInstantiationException(//
                    FormBackedVLDockingViewDescriptor.PROPERTY_ERROR_MESSAGE, e, clazz.getName());
        }

        return instance;
    }
}
