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

package org.bluebell.richclient.form;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bluebell.richclient.form.AbstractBbTableMasterForm.EventType;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.richclient.form.AbstractDetailForm;

/**
 * Implementación básica de formulario detalle que distingue las operaciones de inserción de las de actualización.
 * 
 * @param <T>
 *            el tipo de los objetos editados por el formulario maestro.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
abstract class AbstractBbDetailForm<T> extends AbstractDetailForm {

    /**
     * <em>Flag</em> indicando si hay una operación de salvado en curso.
     * 
     * @see #preCommit(FormModel)
     * @see #postCommit(FormModel)
     */
    private Boolean commiting = Boolean.FALSE;

    /**
     * El formulario maestro.
     */
    private AbstractBbTableMasterForm<T> masterForm;

    /**
     * Crea el formulario detalle a partir del formulario maestro, el identificador y un <em>value model</em>.
     * 
     * @param masterForm
     *            el formulario maestro.
     * @param formId
     *            el identificador del formulario.
     * @param valueModel
     *            <em>value model</em> a partir del cual crear el modelo de este formulario.
     */
    public AbstractBbDetailForm(AbstractBbTableMasterForm<T> masterForm, String formId, ValueModel valueModel) {

        super(masterForm.getFormModel(), formId, valueModel, masterForm.getMasterEventList());

        this.setMasterForm(masterForm);
    }

    /**
     * Crea el formulario detalle a partir de su modelo, el identificador y una lista observable de entidades editables.
     * 
     * @param masterForm
     *            el formulario maestro.
     * @param formModel
     *            el modelo del formulario.
     * @param formId
     *            el identificador del formulario.
     * @param editableItemList
     *            la lista observable de entidades editables.
     */
    protected AbstractBbDetailForm(AbstractBbTableMasterForm<T> masterForm, FormModel formModel, String formId,
            ObservableList editableItemList) {

        super(formModel, formId, editableItemList);

        this.setMasterForm(masterForm);
    }

    /**
     * Obtiene el formulario maestro.
     * 
     * @return el formulario maestro.
     */
    public AbstractBbTableMasterForm<T> getMasterForm() {

        return this.masterForm;
    }

    /**
     * Indica si hay una operación de salvado en curso.
     * 
     * @return <code>true</code> en caso afirmativo.
     */
    public Boolean isCommiting() {

        return this.commiting;
    }

    /**
     * Realiza la operación de salvado conforme a {@link #doPostCommit(FormModel)} y finalmente marca que ya <b>no</b>
     * se está ejecutando una operación de salvado.
     * 
     * @param formModel
     *            el modelo del formulario objeto del salvado.
     * 
     * @see #isCommiting()
     * @see #preCommit(FormModel)
     */
    @Override
    public final void postCommit(FormModel formModel) {

        this.doPostCommit(formModel);
        this.setCommiting(Boolean.FALSE);
    }

    /**
     * Marca que se está efectuando una operación de salvado.
     * 
     * @param formModel
     *            el modelo de formulario objeto del salvado.
     * 
     * @see #isCommiting()
     * @see #postCommit(FormModel)
     */
    @Override
    public final void preCommit(FormModel formModel) {

        this.setCommiting(Boolean.TRUE);
        super.preCommit(formModel);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", this.getId()).toString();
    }

    /**
     * Redirige el <em>post commit</em> al método {@link AbstractBbTableMasterForm#doInsert(Object)} o
     * {@link AbstractBbTableMasterForm#doUpdate(Object)} en función de {@link #isEditingNewFormObject()}.
     * <p>
     * Además en caso de éxito publica un evento notificando que se ha insertado o actualizado una entidad.
     * 
     * @param formModel
     *            el modelo sobre el que tuvo lugar la operación <em>commit</em> .
     * 
     * @see org.springframework.binding.form.CommitListener#postCommit(FormModel)
     * @see org.springframework.richclient.application.event.LifecycleApplicationEvent
     */
    @SuppressWarnings("unchecked")
    private void doPostCommit(FormModel formModel) {

        final Boolean isInserting = this.isEditingNewFormObject();
        T entity = (T) formModel.getFormObject();

        if (isInserting) { // es una inserción.
            entity = this.getMasterForm().doInsert(entity);
        } else { // es una actualización.
            entity = this.getMasterForm().doUpdate(entity);
        }

        // En caso de éxito actualizar la vista y publicar un evento.
        final boolean success = entity != null;
        if (success) {
            // (JAF), 20090323, está línea es innecesaria e ineficiente ya que
            // dispara múltiples eventos prescindibles
            // formModel.getFormObjectHolder().setValue(entity);

            // Ejecutar la lógica original de commit y seleccionar la fila
            // apropiada.
            super.postCommit(formModel);
            this.getMasterForm().setSelectedEntity(entity);

            // Publicar el evento
            // TODO, (JAF), 20080428, la publicación de eventos debería de ser
            // un método privada del formulario maestro. Los tipos de eventos
            // también deberían ser un tipo de dato privado.
            this.getMasterForm().publishApplicationEvent(isInserting ? EventType.CREATED //
                    : EventType.MODIFIED, //
                    entity);
        }
    }

    /**
     * Establece si hay una operación de salvado en curso.
     * 
     * @param commiting
     *            <code>true</code> en caso afirmativo.
     */
    private void setCommiting(Boolean commiting) {

        this.commiting = commiting;
    }

    /**
     * Establece el formulario maestro.
     * 
     * @param masterForm
     *            el formulario maestro.
     */
    private void setMasterForm(AbstractBbTableMasterForm<T> masterForm) {

        this.masterForm = masterForm;
    }
}
