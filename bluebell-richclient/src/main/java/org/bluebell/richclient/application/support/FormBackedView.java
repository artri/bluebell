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

package org.bluebell.richclient.application.support;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bluebell.richclient.form.FormInstantiationException;
import org.bluebell.richclient.form.GlobalCommandsAccessor;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyBatchUpdateException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.richclient.application.PageComponentContext;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.support.GlobalCommandIds;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.AbstractMasterForm;
import org.springframework.richclient.form.Form;
import org.springframework.util.Assert;

/**
 * Implementación de una vista que admite un formulario para ser visualizada.
 * <p>
 * Utiliza un formulario (<code>Form</code>) para crear su <code>control</code> y un {@link GlobalCommandsAccessor} para
 * registrar los <em>local executors</em> de los comandos globales.
 * 
 * @param <T>
 *            el tipo del formulario.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class FormBackedView<T extends Form> extends AbstractView {

    /**
     * Mensaje asociado a la excepción que se produce si no es posible instanciar el formulario.
     */
    private static final String INSTANTIATE_ERROR_MSG = "Error while trying to instantiate form";

    /**
     * Mensaje asociado a la excepción que se produce si no hay un constructor apropiado.
     */
    private static final MessageFormat NO_CONSTRUCTOR_ERROR_MSG = new MessageFormat(
            "No constructor with {0} parameters found");

    /**
     * Mensaje asociado a la excepción que se produce si no es posible establecer alguna propiedad.
     */
    private static final String PROPERTY_ERROR_MSG = "Cannot stablish property";

    /**
     * Un borde vacío con espacios.
     */
    private static final Border EMPTY_BORDER_WITH_GAPS = BorderFactory.createEmptyBorder(3, 3, 3, 3);

    /**
     * The form class.
     */
    private Class<T> formClass;

    /**
     * Un mapa con las propiedades del formulario a instanciar; la clave es el nombre de la propiedad y su valor la
     * referencia a establecer.
     */
    private Map<String, Object> formProperties;

    /**
     * <code>ApplicationEventMulticaster</code> para el registro de los eventos.
     */
    private final ApplicationEventMulticaster applicationEventMulticaster;

    /**
     * El formulario sobre el que se construye la vista.
     */
    private T backingForm;

    /**
     * El <em>accesor</em> para la obtención de los comandos globales.
     */
    private GlobalCommandsAccessor globalCommandsAccessor;

    /**
     * Construye la vista.
     */
    public FormBackedView() {

        super();

        this.applicationEventMulticaster = (ApplicationEventMulticaster) //
        this.getApplicationContext().getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME);
    }

    /**
     * Sets the form class.
     * 
     * @param formClass
     *            the form class to set.
     */
    public final void setFormClass(Class<T> formClass) {

        Assert.notNull(formClass, "formClass");

        this.formClass = formClass;
    }

    /**
     * Obtiene el formulario.
     * 
     * @return el formulario.
     */
    public final T getBackingForm() {

        return this.backingForm;
    }

    /**
     * Establece las propiedades del formulario.
     * 
     * @param formProperties
     *            las propieades del formulario.
     */
    public final void setFormProperties(Map<String, Object> formProperties) {

        this.formProperties = formProperties;
    }

    /**
     * Gets the globalCommandsAccessor.
     * 
     * @return the globalCommandsAccessor
     */
    public final GlobalCommandsAccessor getGlobalCommandsAccessor() {

        return this.globalCommandsAccessor;
    }

    /**
     * Establece el <em>accesor</em> para los comandos globales.
     * <p>
     * Además comprueba que en el momento de la invocación se haya establecido el contexto de la página, en caso
     * contrario elevará una excepción.
     * 
     * @param globalCommandsAccessor
     *            el <em>accesor</em>.
     */
    public final void setGlobalCommandsAccessor(GlobalCommandsAccessor globalCommandsAccessor) {

        Assert.notNull(globalCommandsAccessor, "globalCommandsAccessor");
        Assert.notNull(this.getContext(), "this.getContext()");

        this.globalCommandsAccessor = globalCommandsAccessor;

        this.registerLocalCommandExecutors(this.getContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", this.getId()).toString();
    }

    /**
     * Registra los <em>command executor</em> locales para los comandos globales.
     * <p>
     * Para ello obtiene los comandos a partir de un {@link GlobalCommandsAccessor}.
     * 
     * @param context
     *            el contexto de la página.
     * 
     * @see AbstractView#registerLocalCommandExecutors(PageComponentContext)
     * @see GlobalCommandsAccessor
     */
    @Override
    protected void registerLocalCommandExecutors(PageComponentContext context) {

        // FIXME, no tengo claro que el registro de comandos globales funcione sólo con esto, aunque puede que sí!!

        // TODO, (JAF), 20090919, esto debería de ir a la nueva clase que sincroniza los componentes de la página. Eso
        // no está claro!!!!
        if (this.getGlobalCommandsAccessor() != null) {
            context.register(GlobalCommandIds.PROPERTIES, this.getGlobalCommandsAccessor().getNewFormObjectCommand());
            context.register(GlobalCommandIds.SAVE, this.getGlobalCommandsAccessor().getSaveCommand());
            context.register(GlobalCommandsAccessor.CANCEL, this.getGlobalCommandsAccessor().getCancelCommand());
            context.register(GlobalCommandIds.DELETE, this.getGlobalCommandsAccessor().getDeleteCommand());
            context.register(GlobalCommandsAccessor.REFRESH, this.getGlobalCommandsAccessor().getRefreshCommand());
            context.register(GlobalCommandsAccessor.REVERT, this.getRevertCommand(this.getBackingForm()));
            context.register(GlobalCommandsAccessor.REVERT_ALL, this.getGlobalCommandsAccessor().getRevertAllCommand());

            // (JAF), 20090630, este comando se deshabilita ya que puede causar
            // más problemas que beneficios...
            // context.register(GlobalCommandsAccesor.SELECT_ALL_ENTITIES, //
            // this.globalCommandsAccesor.getSelectAllCommand());

            // (JAF), 20090113, "TextComponentPopup" registra sus propios global
            // command executors
            context.register(GlobalCommandIds.CUT, null);
            context.register(GlobalCommandIds.COPY, null);
            context.register(GlobalCommandIds.PASTE, null);
            context.register(GlobalCommandIds.UNDO, null);
            context.register(GlobalCommandIds.REDO, null);
            // context.register(GlobalCommandIds.SELECT_ALL, null);
        }
    }

    /**
     * Crea el control a partir del formulario.
     * <p>
     * Produce una excepción en el caso de que el <em>backing form</em> sea nulo.
     * 
     * @return el control del formulario.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected JComponent createControl() {

        // Obtener el formulario y establecer su formulario y accesor.
        final T form = this.createForm();
        this.setBackingForm(form);

        // Se registran listeners para los formularios que implementan el interface ApplicationListener
        if (form instanceof ApplicationListener<?>) {
            final ApplicationListener<ApplicationEvent> applicationListener = //
            (ApplicationListener<ApplicationEvent>) form;
            this.applicationEventMulticaster.addApplicationListener(applicationListener);
        }

        // Envolver el control en un JScrollPane
        final JComponent control = this.getBackingForm().getControl();
        final JScrollPane scrollPane = new JScrollPane(control);

        // Añadir los bordes
        // FIXME, (JAF), with substance this border can take a different color from form one.
        control.setBorder(FormBackedView.EMPTY_BORDER_WITH_GAPS);
        scrollPane.setBorder(FormBackedView.EMPTY_BORDER_WITH_GAPS);

        // TODO, this code is substance dependent!!
        scrollPane.putClientProperty("substancelaf.componentFlat", Boolean.TRUE);
        control.putClientProperty("substancelaf.componentFlat", Boolean.FALSE);

        return scrollPane;
    }

    /**
     * Obtiene la clase del formulario.
     * 
     * @return la clase del formulario.
     */
    protected final Class<T> getFormClass() {

        return this.formClass;
    }

    /**
     * Obtiene las propiedades del formulario.
     * 
     * @return las propiedades del formulario.
     */
    protected final Map<String, Object> getFormProperties() {

        return this.formProperties;
    }

    /**
     * Obtiene la implementación del comando global {@value GlobalCommandIds#UNDO} dado el <em>backing form</em>.
     * 
     * @param backingForm
     *            el formulario.
     * @return la implementación del comando, <code>null</code> para los formularios maestros y formularios que no
     *         implementen <code>AbstractForm</code>.
     */
    protected final ActionCommand getRevertCommand(Form backingForm) {

        if (backingForm instanceof AbstractMasterForm) {
            return null;
        } else if (backingForm instanceof AbstractForm) {
            return ((AbstractForm) backingForm).getRevertCommand();
        }

        return null;
    }

    /**
     * Establece el formulario.
     * 
     * @param backingForm
     *            el formulario.
     */
    private void setBackingForm(T backingForm) {

        this.backingForm = backingForm;
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
    private T createForm() {

        return this.instantiate(this.getFormClass(), new Class[] {}, new Object[] {});
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
    private T instantiate(Class<T> clazz, Class<?>[] parameterTypes, Object[] parameterValues) {

        T instance = null;
        try {
            final Constructor<T> constructor = clazz.getConstructor(parameterTypes);

            // Instanciar el formulario
            instance = BeanUtils.instantiateClass(constructor, parameterValues);

            // Establecer las propiedades del formulario
            if (this.getFormProperties() != null) {
                final BeanWrapper wrapper = new BeanWrapperImpl(instance);
                wrapper.setPropertyValues(this.getFormProperties());
            }
        } catch (final SecurityException e) {
            throw new FormInstantiationException(FormBackedView.NO_CONSTRUCTOR_ERROR_MSG.format(//
                    new Object[] { parameterTypes.length }), e, clazz.getName());
        } catch (final NoSuchMethodException e) {
            throw new FormInstantiationException(FormBackedView.NO_CONSTRUCTOR_ERROR_MSG.format(//
                    new Object[] { parameterTypes.length }), clazz.getName());
        } catch (final BeanInstantiationException e) {
            throw new FormInstantiationException(FormBackedView.INSTANTIATE_ERROR_MSG, e, clazz.getName());
        } catch (final InvalidPropertyException e) {
            throw new FormInstantiationException(FormBackedView.PROPERTY_ERROR_MSG, e, clazz.getName());
        } catch (final PropertyBatchUpdateException e) {
            throw new FormInstantiationException(FormBackedView.PROPERTY_ERROR_MSG, e, clazz.getName());
        } catch (final PropertyAccessException e) {
            throw new FormInstantiationException(FormBackedView.PROPERTY_ERROR_MSG, e, clazz.getName());
        }

        return instance;
    }
}
