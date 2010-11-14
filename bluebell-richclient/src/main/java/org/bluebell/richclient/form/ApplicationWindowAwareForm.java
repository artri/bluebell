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

package org.bluebell.richclient.form;

import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.config.ApplicationWindowAware;
import org.springframework.richclient.form.AbstractForm;

/**
 * Extensión de {@link AbstractForm} consciente de la ventana a la que pertenece el formulario.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public abstract class ApplicationWindowAwareForm extends AbstractForm implements ApplicationWindowAware {

    /**
     * La ventana a la que pertenece el formulario.
     */
    private ApplicationWindow applicationWindow;

    /**
     * El constructor por defecto del formulario.
     */
    public ApplicationWindowAwareForm() {

        super();
    }

    /**
     * Construye el formulario a partir de su modelo.
     * 
     * @param formModel
     *            el modelo del formulario.
     */
    public ApplicationWindowAwareForm(FormModel formModel) {

        super(formModel);
    }

    /**
     * Construye el formulario a partir de su modelo e identificador.
     * 
     * @param formModel
     *            el modelo del formulario.
     * @param formId
     *            el identificador del formulario.
     */
    public ApplicationWindowAwareForm(FormModel formModel, String formId) {

        super(formModel, formId);
    }

    /**
     * Construye el formulario y le establece un modelo a partir del padre y una de sus propiedades.
     * 
     * @param parentFormModel
     *            el modelo padre.
     * @param formId
     *            el identificador del formulario.
     * @param childFormObjectPropertyPath
     *            el <em>path</em> de la propiedad a partir de la que crear el modelo.
     */
    public ApplicationWindowAwareForm(HierarchicalFormModel parentFormModel, String formId,
            String childFormObjectPropertyPath) {

        super(parentFormModel, formId, childFormObjectPropertyPath);
    }

    /**
     * Construye el formulario y le establece un modelo a partir del padre y un <em>value holder</em>.
     * 
     * @param parentFormModel
     *            el modelo padre.
     * @param formId
     *            el identificador del formulario.
     * @param childFormObjectHolder
     *            el <em>value holder</em>.
     */
    public ApplicationWindowAwareForm(HierarchicalFormModel parentFormModel, String formId,
            ValueModel childFormObjectHolder) {

        super(parentFormModel, formId, childFormObjectHolder);
    }

    /**
     * Construye el formulario a partir de un objeto.
     * 
     * @param formObject
     *            el objeto.
     */
    public ApplicationWindowAwareForm(Object formObject) {

        super(formObject);
    }

    /**
     * Construye el formulario a partir de su identificador.
     * 
     * @param formId
     *            el identificador.
     */
    public ApplicationWindowAwareForm(String formId) {

        super(formId);
    }

    /**
     * Obtiene la ventana a la que pertenece este formulario.
     * <p>
     * Nótese que alguien tiene que haberlo establecido antes. Si se utiliza <code>FormBackedView</code> entonces será
     * la vista quien establezca la ventana.
     * 
     * @return la ventana.
     */
    public final ApplicationWindow getApplicationWindow() {

        return this.applicationWindow;
    }

    /**
     * Establece la ventana a la que pertenece este formulario.
     * 
     * @param window
     *            la ventana.
     */
    public final void setApplicationWindow(ApplicationWindow window) {

        this.applicationWindow = window;
    }
}
