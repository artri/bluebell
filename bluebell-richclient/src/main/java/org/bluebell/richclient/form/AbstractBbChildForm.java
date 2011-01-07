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
package org.bluebell.richclient.form;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bluebell.richclient.form.util.BbFormModelHelper;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.binding.value.support.ValueHolder;
import org.springframework.util.Assert;

/**
 * Clase base para todos los formularios hijos a utilizar como parte de la plantilla general definida por el framework
 * para aplicaciones Spring RCP.
 * 
 * @param <T>
 *            el tipo de la entidad maestra con la que se vincula este formulario.
 * 
 * @see AbstractB2TableMasterForm
 * @see BbDispatcherForm
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public abstract class AbstractBbChildForm<T extends Object> extends ApplicationWindowAwareForm {

    /**
     * El identificador por defecto del comando <em>revert</em>.
     */
    private static final String REVERT_COMMAND_ID = "revertCommand";

    /**
     * El formulario padre (el formulario detalle compuesto).
     */
    private BbDispatcherForm<T> dispatcherForm;

    /**
     * El formulario maestro.
     */
    private AbstractB2TableMasterForm<T> masterForm;

    /**
     * Construye el formulario hijo a partir de su padre y un identificador.
     * 
     * @param formId
     *            el identificador del formulario.
     */
    public AbstractBbChildForm(String formId) {

        super(formId);

        // Set editing form object index holder
        final ValueHolder editingFormObjectIndexHolder = new ValueHolder(-1);
        this.setEditingFormObjectIndexHolder(editingFormObjectIndexHolder);
    }

    /**
     * Obtiene el formulario maestro.
     * 
     * @return el formulario maestro.
     */
    public final AbstractB2TableMasterForm<T> getMasterForm() {

        return this.masterForm;
    }

    /**
     * Devuelve un formulario hermano con el identificador dado y <code>null</code> si no existe.
     * 
     * @param formId
     *            el identificador del formulario.
     * 
     * @return el formulario hermano,
     */
    public final AbstractBbChildForm<T> getSiblingForm(String formId) {

        return this.getDispatcherForm().getChildForm(formId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", this.getId()).toString();
    }

    /**
     * Crea el modelo del formulario hijo a partir del modelo de su padre.
     * <p>
     * Este método es invocado durante la construcción del formulario con el fin de obtener y establecer un nuevo
     * <em>form model</em> para el mismo.
     * 
     * @param parentFormModel
     *            el modelo del formulario padre.
     * 
     * @return el modelo del formulario.
     * 
     * @see org.springframework.richclient.form.AbstractForm#setFormModel(ValidatingFormModel)
     * @see org.springframework.richclient.form.AbstractMasterForm#configure()
     */
    protected ValidatingFormModel createFormModel(ValidatingFormModel parentFormModel) {

        // (JAF), 20110105, FIX ME!!! This method is not invoked!! http://jirabluebell.b2b2000.com/browse/BLUE-53
        final ValidatingFormModel formModel = BbFormModelHelper.createValidatingFormModel(//
                parentFormModel, this.getId());

        return formModel;
    }

    /**
     * Manejador previo al cambio del elemento seleccionado.
     * <p>
     * Esta implementación no realiza ninguna tarea. Las subclases pueden sobreescribirla para llevar a cabo un
     * comportamiento adicional anterior al cambio del elemento seleccionado.
     * <p>
     * Un caso típico de uso para este método es la desactivación de <em>listeners</em> durante la selección de una
     * entidad.
     * 
     * @param modelIndexes
     *            master event list relative indexes of the selection.
     * @param selection
     *            the selected entities.
     */
    protected void beforeSelectionChange(List<Integer> modelIndexes, List<T> selection) {

        // Nothing to do.
    }

    /**
     * Manejador posterior al cambio del elemento seleccionado.
     * <p>
     * Esta implementación no realiza ninguna tarea. Las subclases pueden sobreescribirla para llevar a cabo un
     * comportamiento adicional posterior al cambio del elemento seleccionado.
     * <p>
     * Un caso típico de uso para este método es la activación de <em>listeners</em> durante la selección de una entidad
     * o la modificación del estado de los controles en función del objeto siendo editado.
     * 
     * @param modelIndexes
     *            master event list relative indexes of the selection.
     * @param selection
     *            the selected entities.
     */
    protected void afterSelectionChange(List<Integer> modelIndexes, List<T> selection) {

        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRevertCommandFaceDescriptorId() {

        return AbstractBbChildForm.REVERT_COMMAND_ID;
    }

    /**
     * Establece el formulario maestro.
     * <p>
     * Produce una excepción en caso de que ya estuviera establecido.
     * 
     * @param masterForm
     *            el formulario maestro.
     */
    final void setMasterForm(AbstractB2TableMasterForm<T> masterForm) {

        Assert.isNull(this.masterForm);

        this.masterForm = masterForm;
    }

    /**
     * Establece el formulario padre.
     * 
     * @param dispatcherForm
     *            el formulario padre.
     */
    final void setDispatcherForm(BbDispatcherForm<T> dispatcherForm) {

        this.dispatcherForm = dispatcherForm;
    }
    
    /**
     * Let (<em>exclusively</em>) dispatcher form set the list of editable form objects.
     * 
     * @param editableFormObjects
     *            the list of editable form objects
     * 
     * @see org.springframework.richclient.form.AbstractForm#setEditableFormObjects
     */
    final void doSetEditableFormObjects(ObservableList editableFormObjects) {

        super.setEditableFormObjects(editableFormObjects);
    }

    /**
     * {@inheritDoc}
     */
    final void doSetEditingFormObjectIndexSilently(int index) {

        super.setEditingFormObjectIndexSilently(index);
    }

    /**
     * Let (<em>exclusively</em>) dispatcher form set whether this form is editing or not a new object.
     * 
     * @param editingNewFormOject
     *            the value to set.
     * 
     * @see org.springframework.richclient.form.AbstractForm#setEditingNewFormObject
     */
    final void doSetEditingNewFormObject(boolean editingNewFormOject) {

        super.setEditingNewFormObject(editingNewFormOject);
    }

    /**
     * Gets the dispatcher form.
     * <p>
     * <blockquote>Why should a child form want to know something about its dispatcher form?</blockquote>
     * <p>
     * Note:
     * <ul>
     * <li>Every child form knows its associated master and sibling forms.
     * <li>Form state is synchronized with dispatcher form.
     * </ul>
     * 
     * @return the dispatcher form.
     * 
     * @see BbDispatcherForm
     * @see BbDispatcherForm.DispatcherFormModel
     */
    private BbDispatcherForm<T> getDispatcherForm() {

        return this.dispatcherForm;
    }
}
