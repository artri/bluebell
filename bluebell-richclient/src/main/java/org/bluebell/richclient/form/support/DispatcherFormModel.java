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

package org.bluebell.richclient.form.support;

import org.bluebell.richclient.form.util.BbDefaultFormModel;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.support.DefaultFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.form.AbstractForm;

/**
 * Modelo que delega la ejecución de alguno de sus métodos en un formulario (Pensado para formularios de tipo
 * {@link org.bluebell.richclient.form.BbDispatcherForm}).
 * <p>
 * Los métodos delegados son:
 * <ul>
 * <li>{@link #reset() }
 * <li>{@link #revert() }
 * <li>{@link #isDirty() }
 * <li>{@link #setEnabled(boolean) }
 * <li>{@link #setFormObject(Object) }
 * </ul>
 * El objetivo es que sea el formulario delegado quien determine exclusivamente cual es la lógica de estas operaciones.
 * Con el fin de que este modelo también sea consciente de dichas acciones se han creado métodos de la forma
 * <code>doInternalXXX</code> (ej.: {@link #doInternalReset()}).
 * <p>
 * También propaga las validaciones a los formularios hijos. Al respecto:
 * <ul>
 * <li>{@link #validate()}
 * <li>{@link #setValidating(boolean)}
 * </ul>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class DispatcherFormModel extends BbDefaultFormModel {
    /**
     * El identificador del modelo.
     */
    private static final String FORM_MODEL_ID = "delegateFormModel";

    /**
     * El formulario en quien se delega.
     */
    private final AbstractForm delegateForm;

    /**
     * Construye el modelo a partir del formulario delegado.
     * 
     * @param valueModel
     *            el <em>value model</em> sobre el que construir el modelo.
     * @param delegateForm
     *            el formulario delegado.
     * 
     * @see AbstractForm(ValueModel)
     */
    public DispatcherFormModel(ValueModel valueModel, AbstractForm delegateForm) {

        super(valueModel);

        this.setId(DispatcherFormModel.FORM_MODEL_ID);
        this.delegateForm = delegateForm;
    }

    /**
     * Resetea este modelo.
     * 
     * @see org.springframework.binding.form.support.AbstractFormModel#reset()
     */
    // FIXME, (JAF), 20090910, since this method is referenced from
    // DispatcherFormModel its visibility is changed from "protected" to
    // "public"
    public final void doInternalReset() {

        super.reset();
    }

    /**
     * Deshace los cambios sobre este modelo.
     * 
     * @see org.springframework.binding.form.support.AbstractFormModel#revert()
     */
    // FIXME, (JAF), 20090910, since this method is referenced from
    // DispatcherFormModel its visibility is changed from "protected" to
    // "public"
    public final void doInternalRevert() {

        super.revert();
    }

    /**
     * Habilita/Deshabilita este modelo.
     * 
     * @param enabled
     *            <em>flag</em> indicando si se ha de habilitar el modelo.
     * 
     * @see org.springframework.binding.form.support.AbstractFormModel#setEnabled(boolean)
     */
    // FIXME, (JAF), 20090910, since this method is referenced from
    // DispatcherFormModel its visibility is changed from "protected" to
    // "public"
    public final void doInternalSetEnabled(boolean enabled) {

        super.setEnabled(enabled);
    }

    /**
     * Gets the delegateForm.
     * 
     * @return the delegateForm
     */
    public AbstractForm getDelegateForm() {

        return this.delegateForm;
    }

    /**
     * Indica si el modelo está sucio. Delega esta responsabilidad en el método {@link AbstractForm#isDirty()} del
     * formulario delegado.
     * 
     * @return <code>true</code> si el modelo está sucio y <code>false</code> en caso contrario.
     */
    @Override
    public boolean isDirty() {

        return this.delegateForm.isDirty();
    }

    /**
     * Resetea el modelo e invoca a {@link AbstractForm#reset()} sobre el formulario delegado.
     * 
     * @see org.bluebell.richclient.form.BbDispatcherForm#reset()
     */
    @Override
    public void reset() {

        // (JAF), 20080914, delegateForm#reset() executes this.doInternalReset()
        // this.doInternalReset();
        this.delegateForm.reset();
    }

    /**
     * Deshace los cambios sobre el modelo e invoca a {@link AbstractForm#revert()} sobre el formulario delegado.
     * 
     * @see org.bluebell.richclient.form.BbDispatcherForm#revert()
     */
    @Override
    public void revert() {

        // (JAF), 20080914, delegateForm#revert() executes
        // this.doInternalRevert();
        // this.doInternalRevert();
        this.delegateForm.revert();
    }

    /**
     * Habilita/Deshabilita el modelo. Delega esta responsabilidad en el método {@link AbstractForm#setEnabled(boolean)}
     * del formulario delegado.
     * 
     * @param enabled
     *            <em>flag</em> indicando si se ha de habilitar el modelo.
     * 
     * @see org.bluebell.richclient.form.BbDispatcherForm#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {

        // (JAF), 20080914, delegateForm#setEnabled() executes setEnabled
        // this.doInternalSetEnabled(enabled);
        this.delegateForm.setEnabled(enabled);
    }

    /**
     * Establece el objeto a editar por el modelo. Delega esta responsabilidad en el método
     * {@link AbstractForm#setFormObject(Object)} del formulario delegado.
     * 
     * @param formObject
     *            el objeto a establecer.
     * 
     * @see org.bluebell.richclient.form.BbDispatcherForm#setFormObject(Object)
     */
    @Override
    public void setFormObject(Object formObject) {

        // (JAF), 20080914, delegateForm#setFormObject() executes setFormObject
        this.delegateForm.setFormObject(formObject);
    }

    /**
     * Establece si se han de validar tanto en este modelo como cada uno de sus hijos.
     * 
     * @param validating
     *            <em>flag</em> indicando si se ha o no de validar.
     * 
     * @see org.springframework.binding.form.ValidatingFormModel#setValidating(boolean)
     */
    @Override
    public void setValidating(boolean validating) {

        // Validation on children checks parent form model validating state at
        // first
        super.setValidating(validating);
        for (final FormModel childFormModel : this.getChildren()) {
            ((DefaultFormModel) childFormModel).setValidating(validating);
        }
    }

    /**
     * Valida tanto este modelo como cada uno de sus hijos.
     * 
     * @see org.springframework.binding.form.ValidatingFormModel#validate()
     */
    @Override
    public void validate() {

        // (JAF), 20090914, this call is not needed anymore: delegate form model
        // should not validate itself, it's just a
        // dispatcher to the child form models
        // super.validate();
        for (final FormModel childFormModel : this.getChildren()) {
            ((DefaultFormModel) childFormModel).validate();
        }
    }

    /**
     * Establece el objeto a editar por este modelo.
     * 
     * @param formObject
     *            el objeto.
     * 
     * @see org.springframework.binding.form.support.AbstractFormModel#setFormObject(Object)
     */
    protected final void doInternalSetFormObject(Object formObject) {

        super.setFormObject(formObject);
    }
}
