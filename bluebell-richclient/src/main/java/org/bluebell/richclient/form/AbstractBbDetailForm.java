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

import java.util.Arrays;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.ActionCommandInterceptor;
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
    private Boolean committing = Boolean.FALSE;

    /**
     * El formulario maestro.
     */
    private AbstractB2TableMasterForm<T> masterForm;

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
    public AbstractBbDetailForm(AbstractB2TableMasterForm<T> masterForm, String formId, ValueModel valueModel) {

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
    protected AbstractBbDetailForm(AbstractB2TableMasterForm<T> masterForm, FormModel formModel, String formId,
            ObservableList editableItemList) {

        super(formModel, formId, editableItemList);

        this.setMasterForm(masterForm);
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
     * Indica si hay una operación de salvado en curso.
     * 
     * @return <code>true</code> en caso afirmativo.
     */
    public final Boolean isCommitting() {

        return this.committing;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overrides parent in order to attach an action command interceptor that justs set
     * <code>editingNewFormObject</code> flag as <code>true</code>. Flag is reverted at {@link #postCommit(FormModel)}.
     * <p>
     * This is needed due to <code>AbstractForm</code> not always employs accessor methods so changes are not propagated
     * to children, find bellow a sample code snippet:
     * 
     * <pre>
     * public void postCommit(FormModel formModel) {
     * 
     *     if (editableFormObjects != null) {
     *         if (editingNewFormObject) {
     *             editableFormObjects.add(formModel.getFormObject());
     *             setEditingFormObjectIndexSilently(editableFormObjects.size() - 1);
     *         } else {
     *             int index = getEditingFormObjectIndex();
     *             // Avoid updating unless we have actually selected an object for
     *             // edit
     *             if (index &gt;= 0) {
     *                 IndexAdapter adapter = editableFormObjects.getIndexAdapter(index);
     *                 adapter.setValue(formModel.getFormObject());
     *                 adapter.fireIndexedObjectChanged();
     *             }
     *         }
     *     }
     *     if (clearFormOnCommit) {
     *         setFormObject(null);
     *     }
     *     editingNewFormObject = false;
     * }
     * </pre>
     * 
     * @see #postCommit(FormModel)
     * @see #setEditingNewFormObject(boolean)
     * @see <a href="http://jirabluebell.b2b2000.com/browse/BLUE-22">BLUE-22</a>
     */
    @Override
    public ActionCommand getNewFormObjectCommand() {

        ActionCommand newFormObjectCommand = FormUtils.getNewFormObjectCommand(this);

        if (newFormObjectCommand == null) {

            newFormObjectCommand = super.getNewFormObjectCommand();
            newFormObjectCommand.addCommandInterceptor(new ActionCommandInterceptor() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean preExecution(ActionCommand command) {

                    AbstractBbDetailForm.this.setEditingNewFormObject(Boolean.TRUE);

                    return Boolean.TRUE;
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void postExecution(ActionCommand command) {

                    // Nothing to do
                }
            });
        }

        return newFormObjectCommand;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() {

        try {
            this.setCommitting(Boolean.TRUE);            
            super.commit();
        } catch (RuntimeException e) {
            throw e;
        } finally {
            // (JAF), 2011010t, ensures committing flag is always reset
            this.setCommitting(Boolean.FALSE);
        }
    }

    /**
     * Realiza la operación de salvado conforme a {@link #doPostCommit(FormModel)} y finalmente marca que ya <b>no</b>
     * se está ejecutando una operación de salvado.
     * 
     * @param formModel
     *            el modelo del formulario objeto del salvado.
     * 
     * @see #isCommitting()
     * @see #preCommit(FormModel)
     */
    @Override
    public final void postCommit(FormModel formModel) {

        this.doPostCommit(formModel);

        // (JAF), 20110105, call #setEditingNewFormObject since AbstractForm uses direct field access instead of setter
        this.setEditingNewFormObject(Boolean.FALSE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", this.getId()).toString();
    }

    /**
     * Redirige el <em>post commit</em> al método {@link AbstractBb1TableMasterForm#doInsert(Object)} o
     * {@link AbstractBb1TableMasterForm#doUpdate(Object)} en función de {@link #isEditingNewFormObject()}.
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

        final Boolean inserting = this.isEditingNewFormObject();
        final T committedEntity = (T) formModel.getFormObject();
        final T managedEntity;

        if (inserting) {
            managedEntity = this.getMasterForm().doInsert(committedEntity);
        } else { // is an update
            managedEntity = this.getMasterForm().doUpdate(committedEntity);
        }

        // If success then update view and publish an event
        final boolean success = (managedEntity != null);
        if (success) {
            // [1] Change form object in order to reflect (on call to super) the managed entity instead of committed ono
            this.setFormObject(managedEntity);

            // [2] Call super
            super.postCommit(formModel);

            // [3] Select managed entity
            this.getMasterForm().changeSelection(Arrays.asList(managedEntity));

            // Publicar el evento
            // [4] Publish application event notifying an insert / update has been successfully done
            this.getMasterForm().publishApplicationEvent(//
                    inserting ? EventType.CREATED : EventType.MODIFIED, managedEntity);
        }
    }

    /**
     * Establece si hay una operación de salvado en curso.
     * 
     * @param committing
     *            <code>true</code> en caso afirmativo.
     */
    private void setCommitting(Boolean committing) {

        this.committing = committing;
    }

    /**
     * Establece el formulario maestro.
     * 
     * @param masterForm
     *            el formulario maestro.
     */
    private void setMasterForm(AbstractB2TableMasterForm<T> masterForm) {

        this.masterForm = masterForm;
    }
}
