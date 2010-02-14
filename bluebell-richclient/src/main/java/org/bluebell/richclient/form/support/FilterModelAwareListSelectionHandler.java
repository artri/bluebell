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

package org.bluebell.richclient.form.support;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.bluebell.richclient.form.AbstractBb0TableMasterForm;
import org.bluebell.richclient.form.AbstractBb1TableMasterForm;
import org.bluebell.richclient.form.builder.support.DirtyTrackingUtils;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.application.support.ApplicationServicesAccessor;
import org.springframework.richclient.core.DefaultMessage;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.core.Severity;
import org.springframework.richclient.dialog.ConfirmationDialog;
import org.springframework.richclient.dialog.MessageDialog;
import org.springframework.richclient.form.AbstractDetailForm;
import org.springframework.richclient.table.ListSelectionListenerSupport;

// TODO REMOVE!!!

/**
 * <em>List selection handler</em> que emplea índices relativos a:
 * <ul>
 * <li>El modelo de la tabla de tipo {@link com.vlsolutions.swing.table.FilterModel} (<em>filteredIdx</em>). Empleado
 * por <code>VLJTable</code>.
 * <li>El modelo envuelto por el modelo de filtrado (<em>originalIdx</em>).
 * </ul>
 * <p>
 * En aquellos casos en los que el modelo de la tabla no sea de tipo {@link com.vlsolutions.swing.table.FilterModel}
 * ambos índices coincidirán.
 * <p>
 * Además publicita el método
 * {@link org.springframework.richclient.form.AbstractMasterForm.ListSelectionHandler#maybeChangeSelection(int)}.
 * 
 * @param <T>
 *            el tipo de las entidades entidades editadas desde el formulario maestro.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class FilterModelAwareListSelectionHandler<T> extends ListSelectionListenerSupport {

    /**
     * Los parámetros del comando "refreshCommand".
     */
    private static final Map<String, Object> REFRESH_COMMAND_PARAMETERS = new HashMap<String, Object>();

    static {
        FilterModelAwareListSelectionHandler.REFRESH_COMMAND_PARAMETERS.put(//
                AbstractBb1TableMasterForm.CLEAR_SELECTION_BEFORE_REFRESH_COMMAND_PARAM, Boolean.FALSE);
    }

    /**
     * El <em>accessor</em> a los servicios de aplicación.
     */
    private final CustomApplicationServicesAccessor applicationServicesAccessor;

    /**
     * EL formulario detalle.
     */
    private final AbstractDetailForm detailForm;

    /**
     * El diálogo que advierte al usuario que puede perder los cambios en la última edición.
     */
    private DirtyConfirmationDialog dirtyDialog;

    /**
     * El formulario maestro.
     */
    private final AbstractBb0TableMasterForm<T> masterForm;

    /**
     * La tabla maestra.
     */
    private final JTable masterTable;

    /**
     * El diálogo que advierte al usuario que ha habido un fallo durante el refresco de entidades.
     */
    private MessageDialog refreshFailureDialog;

    /**
     * Construye el <em>handler</em>.
     * 
     * @param masterForm
     *            el formulario maestro.
     * @param detailForm
     *            el formulario detalle.
     * @param jTable
     *            la tabla maestra.
     */
    public FilterModelAwareListSelectionHandler(AbstractBb0TableMasterForm<T> masterForm,
            AbstractDetailForm detailForm, JTable jTable) {

        this.masterForm = masterForm;
        this.detailForm = detailForm;
        this.masterTable = jTable;
        this.applicationServicesAccessor = new CustomApplicationServicesAccessor();
        this.createDialogs();
    }

    /**
     * Gestiona los cambios de selección <em>single</em> y <em>multiple</em>.
     * <p>
     * Para ello convierte, en primer lugar, los índices de las entidades seleccionadas por el usuario en índices
     * relativos a la <em>master event list</em> del formulario maestro.
     * <p>
     * A continuación <b>refresca</b> cada entidad {@link AbstractBb1TableMasterForm#doRefresh(Object)} con el objetivo
     * de sincronizar las vistas (Visión <b>pesimista</b>) y finalmente invoca a los métodos
     * <code>onSingleSelection</code> o <code>onMultiSelection</code> sobrecargados.
     * 
     * @param filteredIdxs
     *            los índices de las entidades seleccionadas por el usuario.
     * 
     * @see #onSingleSelection(int, int, Object)
     * @see #onMultiSelection(int[], int[], Object[])
     */
    // FIXME, (JAF), 20090910, since this method is referenced from
    // RefreshCommand its visibility is changed from "package" to "public"
    @SuppressWarnings("unchecked")
    public void handleSelection(int[] filteredIdxs) {

        // ¿Ha tenido éxito la operación?
        Boolean success = Boolean.TRUE;
        final Boolean isSingleSelection = (filteredIdxs.length == 1);

        // Obtener los índices originales
        // final int[] originalIdxs = FilterModelUtils.getModelIndexes(this.getMasterTable(), filteredIdxs);
        final int[] originalIdxs = new int[0];

        // Obtener los objetos seleccionados
        final Object[] selections = new Object[originalIdxs.length];
        for (int i = 0; i < originalIdxs.length; ++i) {
            final T entity = (T) this.getMasterForm().getMasterEventList().get(originalIdxs[i]);

            // Refrescar la selección por si hubiese sido modificada o eliminada desde alguna otra ventana o ubicación.
            // selections[i] = this.getMasterForm().doRefreshIfNeeded(entity);

            // Si el refresco ha fallado alertar al usuario y eliminar la entidad de la lista.
            if (selections[i] == null) {
                this.alertFailureOnRefresh(entity);
                this.getMasterForm().getMasterEventList().remove(originalIdxs[i]);
                success = Boolean.FALSE;
            }
        }

        // Si el refresco ha tenido éxito delegar en los método onSingleSelection u onMultiSelection sobrecargados
        if (!success) {
            return;
        } else if (isSingleSelection) {
            this.onSingleSelection(originalIdxs[0], filteredIdxs[0], (T) selections[0]);
        } else {
            this.onMultiSelection(originalIdxs, filteredIdxs, selections);
        }
    }

    /**
     * Deal with a change in the selected index. If we have unsaved changes, then we need to query the user to ensure
     * they want to really make the change.
     * 
     * @param newIndex
     *            the new selection index, may be -1 to clear the selection
     */
    public void maybeChangeSelection(final int newIndex) {

        if (newIndex == this.getDetailForm().getSelectedIndex()) {
            return;
        } else if (this.getDetailForm().isDirty()) {
            this.getDirtyDialog().setNewIndex(newIndex);
            this.getDirtyDialog().showDialog();
        } else {
            this.getDetailForm().setSelectedIndex(newIndex);
        }
    }

    /**
     * Obtiene el <em>accessor</em> a los servicios de aplicación.
     * 
     * @return el <em>accessor</em>.
     */
    protected CustomApplicationServicesAccessor getApplicationServicesAccessor() {

        return this.applicationServicesAccessor;
    }

    /**
     * Obtiene el formulario detalle.
     * 
     * @return el formulario detalle.
     */
    protected AbstractDetailForm getDetailForm() {

        return this.detailForm;
    }

    /**
     * Obtiene el diálogo que advierte al usuario que puede perder los cambios en la última edición.
     * 
     * @return el diálogo.
     */
    protected DirtyConfirmationDialog getDirtyDialog() {

        return this.dirtyDialog;
    }

    /**
     * Obtiene el formulario maestro.
     * 
     * @return el formulario maestro.
     */
    protected AbstractBb0TableMasterForm<T> getMasterForm() {

        return this.masterForm;
    }

    /**
     * Obtiene la tabla maestra.
     * 
     * @return la tabla maestra.
     */
    protected JTable getMasterTable() {

        return this.masterTable;
    }

    /**
     * Obtiene el diálogo que advierte al usuario que ha habido un fallo durante el refresco de entidades.
     * 
     * @return el diálogo.
     */
    protected MessageDialog getRefreshFailureDialog() {

        return this.refreshFailureDialog;
    }

    /**
     * Instala este <em>handler</em>.
     * <p>
     * Este método copia el comportamiento de
     * {@link org.springframework.richclient.form.AbstractMasterForm#installSelectionHandler()} con acceso protegido.
     */
    protected void installSelectionHandler() {

        final ListSelectionModel lsm = this.getMasterTable().getSelectionModel();
        if (lsm != null) {
            lsm.addListSelectionListener(this);
        }
    }

    /**
     * Manejador para los cambios en la entidad seleccionada ante situaciones <em>dirty</em> en las que el usuario
     * descarta el cambio.
     * 
     * @param selectedIndex
     *            el índice de la nueva entidad seleccionada (relativo a la <em>master event list</em>).
     */
    protected void onCancel(int selectedIndex) {

    }

    /**
     * Manejador para los cambios en la entidad seleccionada ante situaciones <em>dirty</em> en las que el usuario
     * confirma el cambio.
     * 
     * @param oldIndex
     *            el índice de la anterior entidad seleccionada (relativo a la <em>master event list</em>).
     * @param newIndex
     *            el índice de la nueva entidad seleccionada (relativo a la <em>master event list</em>).
     */
    protected void onConfirm(int oldIndex, int newIndex) {

    }

    /**
     * Método ejecutado cuando el usuario seleccionado más de una fila.
     * <p>
     * Delega su ejecución en {@link #handleSelection(int[])}.
     * 
     * @param filteredIdxs
     *            los índices relativos al modelo de filtrado.
     * @see #executeRefreshCommand()
     */
    @Override
    protected final void onMultiSelection(int[] filteredIdxs) {

        this.executeRefreshCommand();
    }

    /**
     * Maneja los eventos de selección múltiple.
     * <p>
     * Delega su ejecución en {@link #handleSelection(int[], int[], Object[])}.
     * 
     * @param originalIdxs
     *            los índices de los elementos seleccionados relativos al modelo base del modelo de filtrado.
     * @param filteredIdxs
     *            los índices de los elementos seleccionados relativos al del modelo de filtrado.
     * @param selection
     *            las entidades seleccionadas.
     * 
     * @see #handleSelection(int[], int[], Object[])
     */
    protected void onMultiSelection(int[] originalIdxs, int[] filteredIdxs, Object[] selection) {

        // (JAF), 20090721, ha sido necesario cambiar la signatura de "T[] selection" a "Object[] selection" para evitar
        // errores en tiempo de ejecución con los castings
        this.handleSelection(originalIdxs, filteredIdxs, selection);
    }

    /**
     * Called when nothing gets selected. Override this method to handle empty selection
     */
    @Override
    protected void onNoSelection() {

        this.maybeChangeSelection(-1);
    }

    /**
     * Método ejecutado cuando el usuario seleccionado una sola fila.
     * <p>
     * Delega su ejecución en {@link #handleSelection(int[])}.
     * 
     * @param filteredIdx
     *            el índice relativo al modelo de filtrado.
     * @see #executeRefreshCommand()
     */
    @Override
    protected final void onSingleSelection(int filteredIdx) {

        this.executeRefreshCommand();
    }

    /**
     * Maneja los eventos de selección simple.
     * <p>
     * Delega su ejecución en {@link #handleSelection(int[], int[], Object[])}.
     * 
     * @param originalIdx
     *            el índice del elemento seleccionado relativo al modelo original.
     * @param filteredIdx
     *            el índice del elemento seleccionado relativo al modelo de filtrado.
     * @param selection
     *            la entidad seleccionada.
     * 
     * @see #handleSelection(int[], int[], Object[])
     */
    protected void onSingleSelection(int originalIdx, int filteredIdx, T selection) {

        this.handleSelection(new int[] { originalIdx }, new int[] { filteredIdx }, new Object[] { selection });
    }

    /**
     * Establece el diálogo que advierte al usuario que puede perder los cambios en la última edición.
     * 
     * @param dialog
     *            el diálogo.
     */
    protected void setDirtyDialog(DirtyConfirmationDialog dialog) {

        this.dirtyDialog = dialog;
    }

    /**
     * Establece el diálogo que advierte al usuario que ha habido un fallo durante el refresco de entidades.
     * 
     * @param refreshFailureDialog
     *            el diálogo.
     */
    protected void setRefreshFailureDialog(MessageDialog refreshFailureDialog) {

        this.refreshFailureDialog = refreshFailureDialog;
    }

    /**
     * Desinstala este <em>handler</em>.
     * <p>
     * Este método copia el comportamiento de
     * {@link org.springframework.richclient.form.AbstractMasterForm#uninstallSelectionHandler()} con acceso protegido.
     */
    protected void uninstallSelectionHandler() {

        final ListSelectionModel lsm = this.getMasterTable().getSelectionModel();
        if (lsm != null) {
            lsm.removeListSelectionListener(this);
        }
    }

    /**
     * Notifica al usuario de que ha habido un fallo al refrescar una entidad.
     * 
     * @param selection
     *            la entidad que ha provocado el fallo.
     */
    private void alertFailureOnRefresh(T selection) {

        this.getRefreshFailureDialog().showDialog();
    }

    /**
     * Crea los diálogos.
     */
    private void createDialogs() {

        final CustomApplicationServicesAccessor accessor = this.getApplicationServicesAccessor();

        final String dirtyFormDialogTitle = accessor.getMessage(new String[] {//
                this.getMasterForm().getId() + ".dirtyChange.title", "masterForm.dirtyChange.title" });

        final String dirtyFormDialogMessage = accessor.getMessage(new String[] { //
                this.getMasterForm().getId() + ".dirtyChange.message", "masterForm.dirtyChange.message" });

        final String refreshFailureDialogTitle = accessor.getMessage(new String[] { // 
                this.getMasterForm().getId() + ".refreshFailure.title", "masterForm.refreshFailure.title" });

        final String refreshFailureDialogText = accessor.getMessage(new String[] {//
                this.getMasterForm().getId() + ".refreshFailure.message", "masterForm.refreshFailure.message" });

        final Message refreshFailureMessage = new DefaultMessage(refreshFailureDialogText, Severity.ERROR);

        // Crear los diálogos
        this.setDirtyDialog(new DirtyConfirmationDialog(dirtyFormDialogTitle, dirtyFormDialogMessage));

        this.setRefreshFailureDialog(new MessageDialog(refreshFailureDialogTitle, refreshFailureMessage));
    }

    /**
     * Ejecuta el comando de refresco tratando además situaciones de error.
     */
    private void executeRefreshCommand() {

        // (JAF), 20090329, delegar la selección en un comando para beneficiarse de sus interceptores y políticas
        // try {
        // this.getMasterForm().getRefreshCommand().execute(
        // FilterModelAwareListSelectionHandler.REFRESH_COMMAND_PARAMETERS);

        /*
         * (JAF), 20090730, es preferible delegar la gestión del error en un exception handler
         */
        // } catch (Throwable t) {
        // // (JAF), 20090630, tratar las situaciones de error ya que de otro
        // // modo el cliente se queda inestable
        // this.getMasterForm().keepAliveAfterFailure();
        //
        // // Manejar la excepción (Aunque quizás hubiera sido mejor
        // // relanzarla)
        // RcpMain.handleException(t);
        // }
    }

    /**
     * Gestiona los cambios de selección <em>single</em> y <em>multiple</em>, conocidos:
     * <ul>
     * <li>Los índices relativos a la <em>master evet list</em> de las entidades seleccionadas por el usuario.
     * <li>Los índices relativos a la tabla maesta de las entidades seleccionadas por el usuario.
     * <li>las entidades seleccionadas por el usuario.
     * </ul>
     * <p>
     * Básicamente lo que hace es actualizar las entidades seleccionadas en la <em>master event list</em> en primer
     * lugar y en la propia tabla a continuación.
     * <p>
     * Destacar que no debería ser necesario actualizar la tabla pero con las <code>VLJTable</code> sí.
     * 
     * @param originalIdxs
     *            los índices de los elementos seleccionados relativos al modelo base del modelo de filtrado.
     * @param filteredIdxs
     *            los índices de los elementos seleccionados relativos al del modelo de filtrado.
     * @param selection
     *            las entidades seleccionadas.
     */
    @SuppressWarnings("unchecked")
    private void handleSelection(int[] originalIdxs, int[] filteredIdxs, Object[] selection) {

        // (JAF), 20090721, ha sido necesario cambiar la signatura de "T[]
        // selection" a "Object[]" selection para evitar errores en tiempo de
        // ejecución con los castings

        final Boolean isSingleSelection = (filteredIdxs.length == 1);

        // Desinstalar el selectionHandler temporalmente para actualizar por
        // una parte la masterEventList y por otra la selección en la tabla
        // maestra (este último punto se hace necesario por el empleo de
        // VLJTable).
        this.uninstallSelectionHandler();
        this.getMasterTable().clearSelection();
        for (int i = 0; i < originalIdxs.length; ++i) {
            this.getMasterForm().getMasterEventList().set(originalIdxs[i], selection[i]);
        }
        for (int i = 0; i < originalIdxs.length; ++i) {
            this.getMasterTable().addRowSelectionInterval(filteredIdxs[i], filteredIdxs[i]);
        }
        this.installSelectionHandler();

        // Si la selección es simple consultar al usuario si se debe continuar
        if (isSingleSelection) {
            this.maybeChangeSelection(originalIdxs[0]);
        }
    }

    /**
     * Un <code>ApplicationServicesAccessor</code> que hace accesible determinados métodos a
     * {@link FilterModelAwareListSelectionHandler}. Concretamente:
     * <ul>
     * <li>{@link #getMessage(String[])}
     * <li>{@link #getMessage(String[], Object[])}
     * </ul>
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private static class CustomApplicationServicesAccessor extends ApplicationServicesAccessor {
        /**
         * {@inheritDoc}
         */
        @Override
        protected String getMessage(String[] messageCodes) {

            return super.getMessage(messageCodes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String getMessage(String[] messageCodes, Object[] args) {

            return super.getMessage(messageCodes, args);
        }
    }

    /**
     * Un diálogo con el que confirmar los cambios del elemento seleccionado ante situaciones en las que el formulario
     * detalle está <em>dirty</em>.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class DirtyConfirmationDialog extends ConfirmationDialog {

        /**
         * El nuevo índice a confirmar.
         */
        private int newIndex = -1;

        /**
         * Construye el diálogo a partir de su título y mensaje.
         * 
         * @param title
         *            el título.
         * @param message
         *            el mensaje.
         */
        public DirtyConfirmationDialog(String title, String message) {

            super(title, message);
        }

        /**
         * Obtiene el nuevo índice a confirmar.
         * 
         * @return el índice.
         */
        public int getNewIndex() {

            return this.newIndex;
        }

        /**
         * Estable el nuevo índice a confirmar.
         * 
         * @param newIndex
         *            el índice.
         */
        public void setNewIndex(int newIndex) {

            this.newIndex = newIndex;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected JComponent createDialogContentPane() {

            final FormModel detailFormModel = FilterModelAwareListSelectionHandler.this.getDetailForm().getFormModel();

            final String i18nDirtyPropertiesHtmlString = //
            DirtyTrackingUtils.getI18nDirtyPropertiesHtmlString(detailFormModel);

            final JComponent messageAreaPane = super.createDialogContentPane();
            final JComponent jFormattedTextField = new JFormattedTextField(i18nDirtyPropertiesHtmlString);

            final JPanel jPanel = new JPanel(new BorderLayout());
            jPanel.add(messageAreaPane, BorderLayout.NORTH);
            jPanel.add(jFormattedTextField, BorderLayout.SOUTH);

            return jPanel;
        }

        /**
         *{@inheritDoc}
         */
        @Override
        protected void onCancel() {

            super.onCancel();

            final AbstractDetailForm theDetailForm = FilterModelAwareListSelectionHandler.this.getDetailForm();
            final ListSelectionModel selectionModel = FilterModelAwareListSelectionHandler.this//
                    .getMasterTable().getSelectionModel();

            // Si se está editando un nuevo objeto limpiar la selección, en caso contrario establecerla
            final int oldIdx = theDetailForm.getSelectedIndex();
            if (theDetailForm.isEditingNewFormObject()) {
                selectionModel.clearSelection();
            } else {
                selectionModel.setSelectionInterval(oldIdx, oldIdx);
            }

            // Notificar al manejador
            FilterModelAwareListSelectionHandler.this.onCancel(oldIdx);
        }

        /**
         *{@inheritDoc}
         */
        @Override
        protected void onConfirm() {

            final AbstractDetailForm theDetailForm = FilterModelAwareListSelectionHandler.this.getDetailForm();

            // Establecer el índice del elemento seleccionado
            final int oldIdx = theDetailForm.getSelectedIndex();
            final int newIdx = this.getNewIndex();
            theDetailForm.setSelectedIndex(newIdx);

            // (JAF), 20090630, limpiar el dirty antes de seleccionar
            DirtyTrackingUtils.clearDirty(FilterModelAwareListSelectionHandler.this.getDetailForm().getFormModel());

            // Notificar al manejador
            FilterModelAwareListSelectionHandler.this.onConfirm(oldIdx, newIdx);
        }
    }
}
