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

/**
 * 
 */
package org.bluebell.richclient.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
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
 * @see AbstractBb2TableMasterForm
 * @see org.bluebell.richclient.test.form.BbCompositeDetailForm
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
    private BbDispatcherForm<T> detailForm;

    /**
     * <em>Listener</em> para escuchar cambios en el índice que identifica el elemento seleccionado con respecto a la
     * lista de elementos editables por el formulario.
     * 
     * @see IndexHolderPropertyChangeListener
     */
    private final PropertyChangeListener indexHolderPropertyChangeListener;

    /**
     * El formulario maestro.
     */
    private AbstractBb2TableMasterForm<T> masterForm;

    /**
     * Construye el formulario hijo a partir de su padre y un identificador.
     * 
     * @param formId
     *            el identificador del formulario.
     */
    public AbstractBbChildForm(String formId) {

        super(formId);

        // Permanecer a la escucha de los cambios en la selección del usuario.
        this.indexHolderPropertyChangeListener = new IndexHolderPropertyChangeListener();

        // Establecer el index holder donde consultar el elemento seleccionado.
        this.setEditingFormObjectIndexHolder(new ValueHolder(new Integer(-1)));
    }

    /**
     * Obtiene el formulario maestro.
     * 
     * @return el formulario maestro.
     */
    public AbstractBb2TableMasterForm<T> getMasterForm() {

        return this.masterForm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", this.getId()).toString();
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
     * @param selectedIndex
     *            el índice del elemento seleccionado.
     * @param selectedObject
     *            el objeto seleccionado.
     */
    protected void afterSelectionChange(int selectedIndex, T selectedObject) {

        // Nothing to do.
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
     * @param selectedIndex
     *            el índice del elemento a seleccionar.
     * @param selectedObject
     *            el objeto a seleccionar.
     */
    protected void beforeSelectionChange(int selectedIndex, T selectedObject) {

        // Nothing to do.
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
    protected abstract ValidatingFormModel createFormModel(ValidatingFormModel parentFormModel);

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRevertCommandFaceDescriptorId() {

        return AbstractBbChildForm.REVERT_COMMAND_ID;
    }

    /**
     * Devuelve un formulario hermano con el identificador dado y <code>null</code> si no existe.
     * 
     * @param formId
     *            el identificador del formulario.
     * 
     * @return el formulario hermano,
     */
    protected AbstractBbChildForm<T> getSiblingForm(String formId) {

        return this.detailForm.getChildForm(formId);
    }

    /**
     * Manejador para aquellos casos en los que no se seleccione ningún elemento.
     * <p>
     * Esta implementación no realiza ninguna tarea. Las subclases pueden sobreescribirla para llevar a cabo un
     * comportamiento adicional cada vez que se cambie el elemento seleccionado.
     */
    protected void onNoSelection() {

        // Nothing to do.
    }

    /**
     * Manejador para los cambios del elemento seleccionado.
     * <p>
     * Esta implementación no realiza ninguna tarea. Las subclases pueden sobreescribirla para llevar a cabo un
     * comportamiento adicional posterior al cambio del elemento seleccionado.
     * <p>
     * Un caso típico de uso para este método es la modificación del estado de los controles en función de los objetos
     * siendo editados.
     * 
     * @param selectedIndexes
     *            los índices de los elementos seleccionados.
     * @param selectedObjects
     *            el objeto seleccionado.
     */
    protected void onSelectionChange(List<Integer> selectedIndexes, List<T> selectedObjects) {

        // Nothing to do.
    }

    /**
     * Permite al formulario compuesto establecer la lista de objetos editados del formulario.
     * 
     * @param editableFormObjects
     *            la lista de objetos editables.
     * 
     * @see org.springframework.richclient.form.AbstractForm#setEditableFormObjects
     */
    void setEditableFormObjectFromDispatcherForm(ObservableList editableFormObjects) {

        // TODO, (JAF), 20090927, eliminar este método
        super.setEditableFormObjects(editableFormObjects);
    }

    /**
     * Establece el formulario maestro.
     * <p>
     * Produce una excepción en caso de que ya estuviera establecido.
     * 
     * @param masterForm
     *            el formulario maestro.
     */
    void setMasterForm(AbstractBb2TableMasterForm<T> masterForm) {

        Assert.isNull(this.masterForm);

        this.masterForm = masterForm;
    }

    /**
     * Permite al formulario compuesto establecer el modelo de este formulario a partir del formulario padre.
     * <p>
     * Añade el <em>value change listener</em> {@link #indexHolderPropertyChangeListener} y establece el formulario
     * padre {@link #detailForm}.
     * <p>
     * El nuevo modelo permanecerá deshabilitado y sin validaciones.
     * <p>
     * Provoca una excepción en caso de que el control del formulario ya haya sido creado.
     * 
     * @param parentForm
     *            el formulario padre.
     */
    final void updateFormModelUsingParentForm(BbDispatcherForm<T> parentForm) {

        // TODO revisar este método, sobre todo esta invocación
        // Assert.isTrue(!this.isControlCreated(),
        // "Cannot change form model if control is already created");

        this.setDetailForm(parentForm);
        this.getParentForm().getEditingIndexHolder().addValueChangeListener(this.indexHolderPropertyChangeListener);

        // Crear el nuevo modelo, deshabilitarlo y desactivar las validaciones
        // TODO cambiar y pensar la forma de crear los form models
        // this.setFormModel(this.createFormModel(parentForm.getFormModel()));
        this.getFormModel().setEnabled(Boolean.FALSE);
        this.getFormModel().setValidating(Boolean.FALSE);
    }

    /**
     * Obtiene el formulario padre.
     * 
     * @return el formulario padre.
     */
    @SuppressWarnings("unchecked")
    private BbDispatcherForm getParentForm() {

        // Se declará sin <T> para que funcione la ingeniería inversa
        return this.detailForm;
    }

    /**
     * Establece el formulario padre.
     * 
     * @param parentForm
     *            el formulario padre.
     */
    private void setDetailForm(BbDispatcherForm<T> parentForm) {

        this.detailForm = parentForm;
    }

    /**
     * <em>Listener</em> que controla el cambio del elemento seleccionado en el formulario compuesto con el fin de
     * establecerlo de forma silenciosa en el formulario hijo.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class IndexHolderPropertyChangeListener implements PropertyChangeListener {

        /**
         * Establece de forma silenciosa el índice del elemento seleccionado.
         * 
         * @param evt
         *            el evento de cambio.
         */
        public void propertyChange(PropertyChangeEvent evt) {

            final Integer newIndex = (Integer) evt.getNewValue();

            // Establecer el índice del elemento seleccionado en el formulario
            // hijo.
            AbstractBbChildForm.this.setEditingFormObjectIndexSilently(newIndex);
        }
    }
}
