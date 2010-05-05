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

package org.bluebell.richclient.form.binding.swing;

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.binding.Binding;
import org.springframework.richclient.form.binding.support.AbstractBinder;
import org.springframework.util.Assert;

/**
 * <em>Binder</em> que permite vincular colecciones con tablas a través de un <code>TableBinding</code>.
 * <p>
 * Para utilizar este componente se puede proceder de dos modos:
 * <ul>
 * <li>Utilizando la <em>binding factory</em> de Spring RCP para, por ejemplo, asociar este binder a una propiedad.
 * 
 * <pre>
 *     &lt;bean id=&quot;aceTableBinder&quot; 
 *         class=&quot;org.bluebell.richclient.form.binding.swing.TableBinder&quot;&gt;
 *         &lt;property name=&quot;columnPropertyNames&quot;&gt;
 *             &lt;list&gt;
 *                 &lt;value&gt;string&lt;/value&gt;
 *                 &lt;value&gt;number&lt;/value&gt;
 *             &lt;/list&gt;
 *         &lt;/property&gt;
 *         &lt;property name=&quot;dialogBackingForm&quot;&gt;
 *             &lt;bean class=&quot;es.uniovi.uosec.ui.form.InnerBeanForm&quot; /&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * <li>Instanciando {@link TableBinding} y configurándolo por código:
 * 
 * <pre>
 * TableBinding binding = new TableBinding(formModel, &quot;propertyName&quot;);
 * binding.setColumnPropertyNames(columnPropertyNames);
 * binding.setDialogBackingForm(form);
 * bindingFactory.interceptBinding(binding); // IMPRESCINDIBLE
 * </pre>
 * 
 * </ul>
 * 
 * @see TableBinding
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TableBinder extends AbstractBinder implements InitializingBean {

    /**
     * Los nombres de las columnas de la tabla creada por este <em>binder</em>.
     */
    private String[] columnPropertyNames;

    /**
     * El formulario a utilizar en el diálogo que permite añadir o editar una entidad de la tabla en un
     * <code>TableBinding</code>.
     */
    private Form dialogBackingForm;

    /**
     * El constructor por defecto del <em>binder</em>.
     */
    public TableBinder() {

        this(null, new String[] {});
    }

    /**
     * Construye el <em>binder</em> a partir de la clase requerida y las claves soportadas.
     * 
     * @param requiredSourceClass
     *            la clase requerida.
     * @param supportedContextKeys
     *            las clases soportadas.
     */
    public TableBinder(Class<Object> requiredSourceClass, String[] supportedContextKeys) {

        super(requiredSourceClass, supportedContextKeys);
    }

    /**
     * Comprueba que se hayan establecido los nombres de las columnas.
     * 
     * @throws Exception
     *             en caso de error.
     */
    public void afterPropertiesSet() throws Exception {

        Assert.notEmpty(this.getColumnPropertyNames());
        Assert.notNull(this.getDialogBackingForm());
    }

    /**
     * Obtiene los nombres de las columnas de la tabla creada por este <em>binding</em>.
     * 
     * @return los nombres de las columnas de la tabla creada por este <em>binding</em>.
     */
    public String[] getColumnPropertyNames() {

        return this.columnPropertyNames;
    }

    /**
     * Obtiene el formulario a utilizar en el diálogo que permite añadir o editar una entidad de la tabla en un
     * <code>TableBinding</code>.
     * 
     * @return el formulario.
     */
    public Form getDialogBackingForm() {

        return this.dialogBackingForm;
    }

    /**
     * Establece los nombres de las columnas de la tabla creada por este <em>binding</em>.
     * 
     * @param columnPropertyNames
     *            los nombres de las columnas de la tabla creada por este <em>binding</em>.
     */
    public void setColumnPropertyNames(String[] columnPropertyNames) {

        // Avoid PMD warning:
        // Security - Array is stored directly : The user-supplied array 'viewDescriptors' is stored directly.
        // Constructors and methods receiving arrays should clone objects and store the copy. This prevents that future
        // changes from the user affect the internal functionality.

        this.columnPropertyNames = new String[columnPropertyNames.length];
        for (int i = 0; i < columnPropertyNames.length; ++i) {
            this.columnPropertyNames[i] = columnPropertyNames[i]; // Since String are inmutable no copy is needed
        }
    }

    /**
     * Establece el formulario a utilizar en el diálogo que permite añadir o editar una entidad de la tabla en un
     * <code>TableBinding</code>.
     * 
     * @param dialogBackingForm
     *            el formulario.
     */
    public void setDialogBackingForm(Form dialogBackingForm) {

        this.dialogBackingForm = dialogBackingForm;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected JComponent createControl(Map context) {

        return this.getComponentFactory().createTable();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Binding doBind(JComponent control, FormModel formModel, String formPropertyPath, Map context) {

        final TableBinding binding = new TableBinding((JTable) control, formModel, formPropertyPath);
        binding.setColumnPropertyNames(this.getColumnPropertyNames());
        binding.setDialogBackingForm(this.getDialogBackingForm());

        return binding;
    }
}
