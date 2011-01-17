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

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.list.SetUniqueList;
import org.apache.commons.lang.ArrayUtils;
import org.bluebell.richclient.application.config.FilterCommand;
import org.bluebell.richclient.form.binding.swing.TableBinding;
import org.bluebell.richclient.form.builder.support.DirtyTrackingUtils;
import org.bluebell.richclient.form.util.BbFormModelHelper;
import org.bluebell.richclient.table.support.TableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.form.ConfigurableFormModel;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.value.ValueChangeDetector;
import org.springframework.binding.value.support.ValueHolder;
import org.springframework.richclient.application.config.ApplicationWindowAware;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.CommandGroupFactoryBean;
import org.springframework.richclient.command.TargetableActionCommand;
import org.springframework.richclient.command.support.GlobalCommandIds;
import org.springframework.richclient.dialog.CloseAction;
import org.springframework.richclient.dialog.ConfirmationDialog;
import org.springframework.richclient.exceptionhandling.delegation.ExceptionHandlerDelegate;
import org.springframework.richclient.table.ListSelectionListenerSupport;
import org.springframework.richclient.table.support.GlazedTableModel;
import org.springframework.richclient.util.Assert;
import org.springframework.richclient.util.PopupMenuMouseListener;

import ca.odell.glazedlists.EventList;

/**
 * Extends <code>AbstractBbMasterForm</code> in the following way:
 * <ul>
 * <li>Provides shortcuts to leading with selection changing and filling master table.
 * <li>Requests user confirmation before aborting a current edition.
 * <li>Includes an improved selection handling mechanism, where events come with view and model relative indexes.
 * <li>View is built on top of {@link TableBinding}.
 * <li>Implements <code>ApplicationWindowAware</code>.
 * </ul>
 * 
 * @param <T>
 *            the type of entities managed by this view.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public abstract class AbstractBbTableMasterForm<T extends Object> extends AbstractBbMasterForm<T> implements
        ApplicationWindowAware, GlobalCommandsAccessor {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBbTableMasterForm.class);

    /**
     * Debug message for <code>#beforeSelection</code>.
     */
    private static final MessageFormat BEFORE_SELECTION_FMT = new MessageFormat(
            "Before selecting model indexes \"{0}\" on {1}");

    /**
     * Debug message for <code>#afterSelection</code>.
     */
    private static final MessageFormat AFTER_SELECTION_FMT = //
    new MessageFormat("After selecting model indexes \"{0}\" on {1}");

    /**
     * Determines when a selection change is currently being processed in order to avoid redundant user confirmation
     * requests.
     * 
     * @see MasterFormListSelectionHandler invocations <b>1.b</b> and <b>4</b>.
     */
    private Boolean changingSelection = Boolean.FALSE;

    /**
     * The list selection handler to be installed on the master event list.
     */
    private ListSelectionListener listSelectionHandler;

    /**
     * The binding used to render the master table.
     */
    private TableBinding masterTableBinding;

    /**
     * The command group used to build the popup menu of the master table.
     */
    private CommandGroup popupMenuCommandGroup;

    /**
     * Determines when a <code>showEntities</code> invocation is currently being processed in order to avoid redundant
     * user confirmation requests.
     * 
     * @see MasterFormListSelectionHandler invocations <b>1.b</b> and <b>4</b>.
     */
    private Boolean showingEntities = Boolean.FALSE;

    /**
     * Creates the master form given its identifier and the detail type.
     * <p>
     * Employs a backing bean property that always returns an empty collection.
     * 
     * @param formId
     *            the form id.
     * @param detailType
     *            the detail type.
     */
    public AbstractBbTableMasterForm(String formId, Class<T> detailType) {

        super(formId, detailType);
    }

    /**
     * Creates the master form.
     * 
     * @param parentFormModel
     *            the parent form model.
     * @param property
     *            the property that holds the visible entities.
     * @param formId
     *            the form id.
     * @param detailType
     *            the detail type.
     */
    private AbstractBbTableMasterForm(HierarchicalFormModel parentFormModel, String property, String formId,
            Class<T> detailType) {

        super(parentFormModel, property, formId, detailType);
    }

    /**
     * Show the given entities into the master table.
     * <p>
     * If would there be some entities currently being shown then would replace them.
     * 
     * @param entities
     *            the entities to be shown.
     * 
     * @see #showEntities(Collection, Boolean, Boolean)
     */
    @Override
    public final Boolean showEntities(List<T> entities) {

        return this.showEntities(entities, Boolean.FALSE, Boolean.FALSE);
    }

    /**
     * Show the given entities into the master table.
     * <p>
     * If would there be some entities currently being shown then would replace them.
     * 
     * @param entities
     *            the entities to be shown.
     * @param attach
     *            whether to attach the entities to those currently being shown.
     * 
     * @see #showEntities(Collection, Boolean, Boolean)
     */
    @Override
    public final Boolean showEntities(List<T> entities, Boolean attach) {

        return this.showEntities(entities, attach, Boolean.FALSE);
    }

    /**
     * Shows the given entities into the master table.
     * 
     * @param entities
     *            the entities to be shown.
     * @param attach
     *            whether to attach these entities to those currently being shown.
     * @param force
     *            whether to force showing entities without requesting user confirmation.
     * 
     * @return <code>true</code> if success and <code>false</code> in other case (i.e.:user declined selection change).
     * 
     * @see #shouldProceed()
     */
    @Override
    public final Boolean showEntities(List<T> entities, Boolean attach, Boolean force) {

        Assert.notNull(entities, "entities");
        Assert.notNull(attach, "attach");
        Assert.notNull(force, "force");

        // Reset selection remembering indexes (requests user confirmation)
        final Boolean proceed = force || this.shouldProceed();

        // if (attach | proceed) { // (JAF), 20110103, selection is lost even when attaching
        if (proceed) {

            /*
             * (JAF), 20110111, at this point two user confirmation requests may be thrown
             * 
             * To avoid this situation the flag "showingEntities" does the trick.
             * 
             * @see MasterFormListSelectionHandler sequence diagram
             * 
             * @see #shouldProceed()
             */
            try {
                this.showingEntities = Boolean.TRUE;

                // Update master event list keeping order and removing duplicates.
                @SuppressWarnings("unchecked")
                final List<T> allEntities = SetUniqueList.decorate(entities);

                // PRE-CONDITION: user has confirmed change (if needed)
                final Boolean done = TableUtils.showEntities(this.getMasterTableModel(), allEntities, attach);
                // POST-CONDITION: if done new entities are shown, listeners notified and selection emptied

                Assert.isTrue(!done || this.getSelection().isEmpty(), "!done || this.getSelection().isEmpty()");

            } catch (RuntimeException e) {
                throw e;
            } finally {
                // (JAF), 20110111, ensures showingEntities flag is always reset
                this.showingEntities = Boolean.FALSE;
            }
        }

        return proceed;
    }

    /**
     * Selects the given entities.
     * <p>
     * If new selection is either <code>null</code> or empty then clears selection.
     * 
     * @param newSelection
     *            the entities to select.
     * 
     * @see #showEntities(List)
     * @see TableUtils#changeSelection(JTable, GlazedTableModel, List)
     * @see #shouldProceed()
     */
    @Override
    public final void changeSelection(List<T> newSelection) {

        // Selection must be included into entities currently being shown.
        final Boolean proceed = this.showEntities(new ArrayList<T>(newSelection), Boolean.TRUE);

        if (proceed) {

            /*
             * (JAF), 20110102, at this point two user confirmation requests may be thrown
             * 
             * To avoid this situation the flag "changingSelection" does the trick.
             * 
             * @see MasterFormListSelectionHandler sequence diagram
             * 
             * @see #shouldProceed()
             */
            try {
                this.changingSelection = Boolean.TRUE;

                // PRE-CONDITION: user has confirmed selection (if needed) && new selection is currently being shown
                TableUtils.changeSelection(this.getMasterTable(), this.getMasterTableModel(), newSelection);
                // POST-CONDITION: selection is changed and listeners notified
            } catch (RuntimeException e) {
                throw e;
            } finally {
                // (JAF), 20110102, ensures changingSelection flag is always reset
                this.changingSelection = Boolean.FALSE;
            }
        }
    }

    /**
     * Gets the selected entities.
     * 
     * @return the selected entities.
     */
    @Override
    public final List<T> getSelection() {

        return TableUtils.getSelection(this.getMasterTable(), this.getMasterTableModel());
    }

    /**
     * Gets the property names to show in columns of the master table.
     * 
     * @return an array of property names
     */
    protected abstract String[] getColumnPropertyNames();

    /**
     * Requests user confirmation to proceed with an user action in order to prevent information lost.
     * 
     * @return <code>true</code> to proceed and <code>false</code> in any other case.
     */
    protected Boolean requestUserConfirmation() {

        // Dirty dialog
        final String title = AbstractBbTableMasterForm.this.getMessage(//
                new String[] { this.getId() + ".dirtyChange.title", "masterForm.dirtyChange.title" });
        final String message = AbstractBbTableMasterForm.this.getMessage(//
                new String[] { this.getId() + ".dirtyChange.message", "masterForm.dirtyChange.message" });

        final ValueHolder proceed = new ValueHolder(Boolean.FALSE);

        final ConfirmationDialog dialog = new RequestUserConfirmationDialog(title, message) {

            @Override
            protected void onCancel() {

                if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
                    AbstractBbTableMasterForm.LOGGER.debug("User cancel request");
                }

                super.onCancel();
            }

            @Override
            protected void onConfirm() {

                if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
                    AbstractBbTableMasterForm.LOGGER.debug("User confirmed request");
                }

                proceed.setValue(Boolean.TRUE);
            };
        };

        dialog.showDialog();

        return (Boolean) proceed.getValue();
    }

    /**
     * Manages selection change events.
     * <p>
     * Updates <code>masterEventList</code> and <code>masterTable</code> state. If current selection has been edited
     * then requests user confirmation before changing selection definitely.
     * 
     * @param oldModelIndexes
     *            master event list relative indexes of the previous selection.
     * @param oldViewIndexes
     *            user relative indexes of the previous selection.
     * @param newModelIndexes
     *            master event list relative indexes of the selection.
     * @param newViewIndexes
     *            user relative indexes of the selection.
     * @param newSelection
     *            the selected entities.
     */
    @SuppressWarnings("unchecked")
    protected final void doSelectionChange(List<Integer> oldModelIndexes, List<Integer> oldViewIndexes,
            List<Integer> newModelIndexes, List<Integer> newViewIndexes, List<T> newSelection) {

        final Integer oldSelectedIndex = this.getDetailForm().getSelectedIndex();
        final Boolean emptySelection = (newSelection.isEmpty());
        final Boolean singleSelection = (newSelection.size() == 1);
        final Integer indexToSelect;

        if (emptySelection) {
            indexToSelect = -1;
        } else if (singleSelection) {
            indexToSelect = newModelIndexes.get(0);
        } else { // Multiple selection. (JAF), 20110102
            indexToSelect = -1;
        }

        /*
         * BLUE-62, (JAF), 20110116, http://jirabluebell.b2b2000.com/browse/BLUE-62
         * 
         * Master event list have not changed so dispatcher form and child forms are not aware of new selection
         */
        final ValueChangeDetector valueChangeDetector = //
        (ValueChangeDetector) this.getService(ValueChangeDetector.class);

        for (int i = 0; i < newModelIndexes.size(); ++i) {

            final Integer modelIndex = newModelIndexes.get(i);
            final T oldValue = (T) this.getMasterEventList().get(modelIndex);
            final T newValue = newSelection.get(i);

            if (valueChangeDetector.hasValueChanged(oldValue, newValue)) {
                this.getMasterEventList().set(modelIndex, newValue);
            }
        }

        /*
         * BLUE-41, (JAF), 20101227, http://jirabluebell.b2b2000.com/browse/BLUE-41
         * 
         * Notify detail form and listeners in any case
         */
        this.getDetailForm().setSelectedIndex(indexToSelect);
        if ((indexToSelect == -1) && (oldSelectedIndex != -1)) {
            // (JAF), 20110103, form becomes empty and disabled when selected index changes from any to -1
            this.getDetailForm().reset();
        }

        DirtyTrackingUtils.clearDirty(this.getDetailFormModel());
    }

    /**
     * Rejects a selection change action keeping getting back previous selection.
     * 
     * @param oldModelIndexes
     *            master event list relative indexes of the previous selection.
     * @param oldViewIndexes
     *            user relative indexes of the previous selection.
     * @param newModelIndexes
     *            master event list relative indexes of the selection.
     * @param newViewIndexes
     *            user relative indexes of the selection.
     * @param selection
     *            the selected entities.
     */
    protected final void undoSelectionChange(List<Integer> oldModelIndexes, List<Integer> oldViewIndexes,
            List<Integer> newModelIndexes, List<Integer> newViewIndexes, List<T> selection) {

        // If editing new form object then clear selection on cancel, else get back the former selected index
        if (this.getDetailForm().isEditingNewFormObject()) {
            this.getSelectionModel().clearSelection();
        } else {
            TableUtils.changeSelection(this.getMasterTable(), oldViewIndexes);
        }
    }

    /**
     * Handles selection change events just before selection gets effective.
     * <p>
     * Allows replacing selected entities (i.e.: lazy loading).
     * 
     * @param modelIndexes
     *            master event list relative indexes of the selection.
     * @param selection
     *            the selected entities.
     * @return the entities to be selected.
     */
    protected final List<T> beforeSelectionChange(List<Integer> modelIndexes, List<T> selection) {

        if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
            AbstractBbTableMasterForm.LOGGER.debug(AbstractBbTableMasterForm.BEFORE_SELECTION_FMT.format(//
                    new Object[] { ArrayUtils.toString(modelIndexes), this.getId() }));
        }

        /*
         * Bug http://jirabluebell.b2b2000.com/browse/BLUE-61 fixed
         * 
         * final List<T> newSelection = this.doRefresh(selection);
         */
        final Boolean committing = this.getDispatcherForm().isCommitting();
        final List<T> newSelection = (committing) ? selection : this.doRefresh(selection);

        // Notify child forms about selection
        for (final AbstractBbChildForm<T> childForm : this.getChildForms()) {
            childForm.beforeSelectionChange(modelIndexes, newSelection);
        }

        return newSelection;
    }

    /**
     * Handles selection change events just after selection gets effective.
     * <p>
     * Useful for updating view state (i.e.: for customization purposes).
     * 
     * @param modelIndexes
     *            master event list relative indexes of the selection.
     * @param selection
     *            the selected entities.
     */
    protected final void afterSelectionChange(List<Integer> modelIndexes, List<T> selection) {

        if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
            AbstractBbTableMasterForm.LOGGER.debug(AbstractBbTableMasterForm.AFTER_SELECTION_FMT.format(//
                    new Object[] { ArrayUtils.toString(modelIndexes), this.getId() }));
        }

        // Notify child forms about selection
        for (final AbstractBbChildForm<T> childForm : this.getChildForms()) {
            childForm.afterSelectionChange(modelIndexes, selection);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Employs a table binding for displaying the master form.
     */
    @Override
    protected final JComponent createFormControl() {

        // Configure all sub-components
        this.configure();

        final FormModel parentFormModel = this.getFormModel().getParent();
        final TableModel tableModel = this.createTableModel();

        // Configure master table binding
        this.setMasterTableBinding(new TableBinding(tableModel, parentFormModel, ParentFormBackingBean.PROPERTY_NAME));
        this.getMasterTableBinding().setColumnPropertyNames(this.getColumnPropertyNames());
        this.getMasterTableBinding().setButtonsCommandGroup(this.getCommandGroup());
        this.getMasterTableBinding().setPopupMenuCommandGroup(this.getPopupMenuCommandGroup());
        // Interception is not interesting in this case
        // ((SwingBindingFactory)this.getBindingFactory()).interceptBinding(tableBinding);

        // Configure master table
        this.getMasterTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.getMasterTable().addMouseListener(new PopupMenuMouseListener(this.getPopupMenu()));
        // this.getMasterTable().setPreferredScrollableViewportSize(this.getMasterTable().getPreferredSize());

        // Setup selection listener so that it controls the detail form
        this.installSelectionHandler();

        // Update controls for state
        this.updateControlsForState();

        return this.getMasterTableBinding().getControl();
    }

    /**
     * Método al que es posible invocar con el objetivo de modificar el control del formulario antes de su creación
     * defintiva.
     * <p>
     * Esta implementación no realiza tarea alguna.
     * 
     * @param panel
     *            el panel que conforma el formulario.
     */
    protected void onCreateFormControl(JPanel panel) {

        // Nothing to do
    }

    /**
     * Gets the popup menu for the master table.
     * <p>
     * This is built from the command group returned from {@link #getPopupMenuCommandGroup()}.
     * 
     * @return the popup menu.
     */
    @Override
    protected final JPopupMenu getPopupMenu() {

        return this.getPopupMenuCommandGroup().createPopupMenu();
    }

    /**
     * Gets the popup menu command group and if doesn't exist then creates it.
     * 
     * @return the popup menu command group.
     */
    protected final CommandGroup getPopupMenuCommandGroup() {

        if (this.popupMenuCommandGroup == null) {
            this.popupMenuCommandGroup = this.createButtonsCommandGroup();
        }

        return this.popupMenuCommandGroup;
    }

    /**
     * Creates the popup menu command group.
     * 
     * @return the popup menu command group.
     */
    protected CommandGroup createPopupMenuCommandGroup() {

        final CommandGroup group = CommandGroup.createCommandGroup(new Object[] { GlobalCommandIds.PROPERTIES, //
                GlobalCommandIds.SAVE, //
                GlobalCommandsAccessor.CANCEL, //
                GlobalCommandIds.DELETE, //
                CommandGroupFactoryBean.SEPARATOR_MEMBER_CODE, //
                GlobalCommandsAccessor.REVERT, //
                GlobalCommandsAccessor.REVERT_ALL //
                });

        group.setCommandRegistry(this.getApplicationWindow().getCommandManager());

        return group;
    }

    /**
     * Creates the selection handler to be installed in the master event list.
     * 
     * @return the selection handler.
     */
    protected ListSelectionListener createSelectionHandler() {

        return new MasterFormListSelectionHandler();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    protected TableModel createTableModel() {

        final EventList<T> eventList = this.getMasterEventList();

        // Change backing form object in order to force table binding to use the event list as value model
        final ConfigurableFormModel parentFormModel = (ConfigurableFormModel) this.getFormModel().getParent();
        parentFormModel.setFormObject(new ParentFormBackingBean(eventList));

        return BbFormModelHelper.createTableModel(eventList, this.getColumnPropertyNames(), this.getId());
    }

    /**
     * Sobrescribe el método de la clase base que elimina de la tabla maestra el departamento seleccionado para su
     * eliminación también de forma persistente.
     */
    @Override
    protected final void deleteSelectedItems() {

        // Resetear el formulario detalle para que no dé falsos avisos de dirty
        this.getDispatcherForm().reset();

        final List<T> selection = TableUtils.getSelection(this.getMasterTable(), this.getMasterTableModel());

        for (T entity : selection) {

            // Eliminar la entidad y retonar null en caso de fallo.
            final boolean success = (AbstractBbTableMasterForm.this.doDelete(entity) != null);
            if (!success) {

                new String("Avoid CS warning");
                // TODO, do something
            }

            // Actualizar la tabla maestra.
            AbstractBbTableMasterForm.this.getMasterEventList().remove(entity);

            // Publicar el evento de notificación de borrado.
            AbstractBbTableMasterForm.this.publishApplicationEvent(EventType.DELETED, entity);
        }
    }

    /**
     * Gets the master table.
     * 
     * @return the master table.
     */
    protected final JTable getMasterTable() {

        return this.getMasterTableBinding().getTable();
    }

    /**
     * Gets the master table binding.
     * 
     * @return the binding.
     */
    protected final TableBinding getMasterTableBinding() {

        return this.masterTableBinding;
    }

    /**
     * @return Returns the masterTableModel.
     */
    protected final GlazedTableModel getMasterTableModel() {

        return (GlazedTableModel) (getMasterTable() != null ? getMasterTable().getModel() : null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final ListSelectionListener getSelectionHandler() {

        if (this.listSelectionHandler == null) {
            this.listSelectionHandler = this.createSelectionHandler();
        }
        return this.listSelectionHandler;
    }

    /**
     * Gets the selection model for the master list representation.
     * 
     * @return selection model or <code>null</code> if master table has not been constructed yet.
     */
    @Override
    protected ListSelectionModel getSelectionModel() {

        return this.getMasterTable().getSelectionModel();
    }

    /**
     * Crea y configura el comando para cancelar la creación de una nueva entidad.
     * 
     * @return el comando de cancelación.
     */
    protected ActionCommand createCancelCommand() {

        Assert.notNull(this.getDispatcherForm());

        // Obtener los identificadores del comando, separados por comas y ordenados según prioridad
        final String commandId = this.getCancelCommandFaceDescriptorId();
        final BbDispatcherForm<T> dispatcherForm = this.getDispatcherForm();

        // Crear el comando y sincronizar su estado enabled/disabled
        final ActionCommand command = new TargetableActionCommand(commandId, dispatcherForm.getCancelCommand());

        // enable/disable command dependending on new form object command
        command.setEnabled(Boolean.FALSE);
        this.getNewFormObjectCommand().addEnabledListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {

                command.setEnabled(!(Boolean) evt.getNewValue());
            }
        });

        // Configurar el comando
        return this.configureCommand(command, Boolean.FALSE);
    }

    /**
     * Crea y configura el comando de filtrado de la tabla maestra.
     * 
     * @return el comando de filtrado.
     */
    protected ActionCommand createFilterCommand() {

        // Obtener los identificadores del comando, separados por comas y ordenados según prioridad
        final String commandId = this.getFilterCommandFaceDescriptorId();

        // Crear el comando
        final ActionCommand command = new FilterCommand(commandId, AbstractBbTableMasterForm.this.getMasterTable());

        // Configurar el comando
        return this.configureCommand(command, Boolean.FALSE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ActionCommand createNewFormObjectCommand() {

        final ActionCommand newFormObjectCommand = super.createNewFormObjectCommand();

        return this.configureCommand(newFormObjectCommand, Boolean.FALSE);
    }

    /**
     * Crea y configura el comando de selección de todos los elementos de la tabla maestra.
     * 
     * @return el comando de selección
     */
    protected ActionCommand createRefreshCommand() {

        // Obtener los identificadores del comando, separados por comas y ordenados según prioridad
        final String commandId = this.getRefreshCommandFaceDescriptorId();

        // Crear el comando
        final ActionCommand command = new ActionCommand(commandId) {

            /**
             * Selecciona todas las entidades de la tabla maestra.
             */
            @Override
            protected void doExecuteCommand() {

                final JTable table = AbstractBbTableMasterForm.this.getMasterTable();
                final Boolean proceed = AbstractBbTableMasterForm.this.shouldProceed();

                if (proceed) {
                    try {
                        // Fix issue http://jirabluebell.b2b2000.com/browse/BLUE-56
                        AbstractBbTableMasterForm.this.changingSelection = Boolean.TRUE;

                        TableUtils.refreshSelection(table);
                    } catch (RuntimeException e) {
                        throw e;
                    } finally {
                        // (JAF), 20110117, ensures changingSelection flag is always reset
                        AbstractBbTableMasterForm.this.changingSelection = Boolean.FALSE;
                    }
                }
            }
        };

        // (JAF), 20110113, enable/disable command dependending on table state
        command.setEnabled(Boolean.FALSE);
        this.getMasterTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {

                if (!e.getValueIsAdjusting()) {
                    final JTable table = AbstractBbTableMasterForm.this.getMasterTable();
                    final Boolean enabled = (table.getSelectionModel().getMaxSelectionIndex() > -1);
                    command.setEnabled(enabled);
                }
            }
        });

        // Configurar el comando
        return this.configureCommand(command, Boolean.TRUE);
    }

    /**
     * Crea y configura el comando que deshace los cambios sobre una entidad.
     * 
     * @return el comando de <em>undo</em>.
     */
    protected ActionCommand createRevertAllCommand() {

        Assert.notNull(this.getDispatcherForm());

        // Obtener los identificadores del comando, separados por comas y ordenados según prioridad
        final String commandId = this.getRevertAllCommandFaceDescriptorId();
        final BbDispatcherForm<T> dispatcherForm = this.getDispatcherForm();

        // Crear el comando
        final ActionCommand command = new TargetableActionCommand(commandId, dispatcherForm.getRevertCommand());

        // Configurar el comando
        return this.configureCommand(command, Boolean.FALSE);
    }

    /**
     * Crea y configura el comando para guardar los cambios realizados sobre una entidad.
     * 
     * @return el comando de salvado.
     */
    protected ActionCommand createSaveCommand() {

        Assert.notNull(this.getDispatcherForm());

        // Obtener los identificadores del comando, separados por comas y ordenados según prioridad
        final String commandId = this.getSaveCommandFaceDescriptorId();
        final BbDispatcherForm<T> dispatcherForm = this.getDispatcherForm();

        // Crear el comando
        final ActionCommand command = new TargetableActionCommand(commandId, dispatcherForm.getCommitCommand());

        // Configurar el comando
        return this.configureCommand(command, Boolean.TRUE);
    }

    /**
     * Crea y configura el comando de selección de todos los elementos de la tabla maestra.
     * 
     * @return el comando de selección
     */
    protected ActionCommand createSelectAllCommand() {

        // Obtener los identificadores del comando, separados por comas y ordenados según prioridad
        final String commandId = this.getSelectAllCommandFaceDescriptorId();

        // Crear el comando
        final ActionCommand command = new ActionCommand(commandId) {

            /**
             * Selecciona todas las entidades de la tabla maestra.
             */
            @Override
            protected void doExecuteCommand() {

                AbstractBbTableMasterForm.this.getMasterTable().selectAll();
            }
        };

        // (JAF), 20110113, enable/disable command dependending on table state
        this.getMasterTableModel().addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {

                final Boolean enabled = (AbstractBbTableMasterForm.this.getMasterTable().getRowCount() > 0);
                command.setEnabled(enabled);
            }
        });

        // Configurar el comando
        return this.configureCommand(command, Boolean.TRUE);
    }

    /**
     * Returns whether should proceed with an user action that can breaks a current edition.
     * 
     * @return <code>true</code> to proceed and <code>false</code> in other case.
     */
    Boolean shouldProceed() {

        final Boolean shouldProceed;

        if (this.changingSelection) {
            // (JAF), 20110102, avoids unnecessary and redundant user confirmation requests (@see #changeSelection)
            shouldProceed = Boolean.TRUE;
        } else if (this.showingEntities) {
            // (JAF), 20110102, avoids unnecessary and redundant user confirmation requests (@see #showEntities)
            shouldProceed = Boolean.TRUE;
        } else if (!this.getDetailForm().isDirty()) {
            shouldProceed = Boolean.TRUE;
        } else {
            shouldProceed = this.requestUserConfirmation();
        }

        return shouldProceed;
    }

    /**
     * Template method that manages selection change events.
     * <p>
     * Allow master form implementors to treat selected entities before selection gets definitive. Note it's also
     * possible to add behaviour after selection, respectively:
     * <ol>
     * <li>{@link #beforeSelectionChange(List, List)}
     * <li>{@link #afterSelectionChange(List, List)}
     * </ol>
     * <p>
     * <em>This method is private and can only be invoked within a list selection event.</em>
     * 
     * @param newModelIndexes
     *            master event list relative indexes of the selection.
     * @param newViewIndexes
     *            user relative indexes of the selection.
     * @param newSelection
     *            the selected entities.
     * 
     * @see AbstractBbTableMasterForm#beforeSelectionChange(List, List)
     * @see #doSelectionChange(int[], int[], List)
     * @see AbstractBbTableMasterForm#afterSelectionChange(List, List)
     */
    private final void handleSelectionChange(List<Integer> newModelIndexes, List<Integer> newViewIndexes,
            List<T> newSelection) {

        // (JAF), 20110110, at this point old indexes become newer, so employing TableUtils is not a valid way
        // @see http://jirabluebell.b2b2000.com/browse/BLUE-45
        // final List<Integer> oldViewIndexes = TableUtils.getSelectedViewIndexes(this.getMasterTable());
        // final List<Integer> oldModelIndexes = TableUtils.getModelIndexes(this.getMasterTable(), oldViewIndexes);

        // Old view and model indexes
        final List<Integer> oldModelIndexes = Arrays.asList(this.getDetailForm().getSelectedIndex());
        final List<Integer> oldViewIndexes = TableUtils.getViewIndexes(this.getMasterTable(), oldModelIndexes);

        /*
         * Do changes silently (uninstall selection handler before and install later)
         */
        this.uninstallSelectionHandler();

        final Boolean shouldProceed = this.shouldProceed();
        if (shouldProceed) {
            final List<T> managedSelection = this.beforeSelectionChange(newModelIndexes, newSelection);
            this.doSelectionChange(oldModelIndexes, oldViewIndexes, newModelIndexes, newViewIndexes, managedSelection);
            this.afterSelectionChange(newModelIndexes, managedSelection);
        } else {
            this.undoSelectionChange(oldModelIndexes, oldViewIndexes, newModelIndexes, newViewIndexes, newSelection);
        }

        this.installSelectionHandler();
    }

    /**
     * Sets the master table binding.
     * 
     * @param masterTableBinding
     *            the binding to set.
     */
    private void setMasterTableBinding(TableBinding masterTableBinding) {

        Assert.notNull(masterTableBinding, "masterTableBinding");

        this.masterTableBinding = masterTableBinding;
    }

    /**
     * Manejador de errores que mantiene "vivo" el formulario maestro después de un fallo.
     * <p>
     * Se hace necesario ya que ante una excepción no controlada los <em>backing objects</em> de los formularios quedan
     * en un estado precario y provocan continuas excepciones dejando la aplicación insusable.
     * <p>
     * Por tanto esta implementación mantiene vivo el formulario en {@link #hasAppropriateHandler(Throwable)} y devuelve
     * siempre <code>false</code> en {@link #uncaughtException(Thread, Throwable)}.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class KeepAliveAfterFailureExceptionHandlerDelegate implements ExceptionHandlerDelegate {

        // private void keepAliveAfterFailure() {
        //
        // this.changeSelection(null);
        // this.getDispatcherForm().reset();
        // }

        /**
         * {@inheritDoc}
         */
        public boolean hasAppropriateHandler(Throwable thrownTrowable) {

            // final ApplicationWindow window =
            // Application.instance().getActiveWindow();
            // final ApplicationPage page = window != null ? window.getPage() //
            // : null;

            // TODO, 20090919, me da que haciendolo con hilos ya no habría estos
            // problemas

            // if (page != null && page instanceof BbVLDockingApplicationPage) {
            //
            // final BbVLDockingApplicationPage<Object> tPage =
            // (BbVLDockingApplicationPage<Object>) page;
            // final AbstractBbTableMasterForm<Object> masterForm =
            // tPage.getLastMasterForm();
            //
            // if (masterForm != null) {
            // masterForm.keepAliveAfterFailure();
            // }
            // }

            return Boolean.FALSE;
        }

        /**
         * {@inheritDoc}
         */
        public void uncaughtException(Thread t, Throwable e) {

            // Nothing to do
        }
    }

    /**
     * Improved list selection listener that request user confirmation before changing selection. This is really useful
     * to prevent information lost after changing selection.
     * <p>
     * It's capable of transforming selection events handlers into methods with more semantic which distinguish between
     * <code>JTable</code> relative row indexes and backing collection relative indexes:
     * <ul>
     * <li>From {@link #onSingleSelection(int)} to {@link #onSingleSelection(int, int, Object)}
     * <li>From {@link #onMultiSelection(int[])} to {@link #onMultiSelection(List, List, List)}
     * </ul>
     * <p>
     * Find below how selection changes work:
     * 
     * <pre>
     *  Application code  User action   ListSelectListener  AbstractBbTableMasterForm              TableUtils
     *          |               |               |                       |                               |
     *        #############################################################################################
     *        # | __CASE (1)__  |               |                       |                               | #
     *        # |               |               |                       |                               | #
     *        # |(1.a)changeSelection(List[T])  |                       |                               | #
     *        # 0------------------------------------------------------>|                               | #
     *        # |               |               |                    +--|                               | #
     *        # |               |               |                    |(1.b)proceed=showEntities(List[T],Boolean)
     *        # |               |               |                    +->|                               | #
     *        # |               |               |                       |                               | #
     *        # |               |               |                       |\if(proceed)                   | #
     *        # |               |               |                       | \                             | #
     *        # |               |               |                       |  |(1.c)changeSelection(JTable,List[I])
     *        # |               |               |                       |  |--------------------------->| #
     *        # |               |               |                       | /                             | #
     *        # |               |               |                       |/                              | #
     *        # |               |               |                       |               (1.d)raise event| #
     *        # |               |               X<------------------------------------------------------| #
     *        # |               |               |                       |                               | #
     *        #############################################################################################
     *        # | __CASE (2)__  |               |                       |                               | #
     *        # |               |(2.a)clic      |                       |                               | #
     *        # |               0-------------->X                       |                               | #
     *        #############################################################################################
     *          |               |               |                       |                               |
     *          |               |               |(3)handleSelectionChange(List[I],List[I],List[T])      |
     *          |               |               X---------------------->|                               |
     *          |               |               |                      /|                               |
     *          |               |               |                     /(4)if(shouldProceed())           |
     *          |               |               |                 +--|  |                               |
     *          |               |               |                 |  |  |                               |
     *          |               |               |                 |(5)beforeSelectionChange(List[Integer],List[T])
     *          |               |               |                 |  |  |                               |
     *          |               |               |                 +->|  |                               |
     *          |               |               |                 +--|  |                               |
     *          |               |               |                 |  |  |                               |
     *          |               |               |                 |(6)doSelectionChange(List[I],List[I],List[I],List[T])
     *          |               |               |                 |  |  |                               |
     *          |               |               |                 +->|  |                               |
     *          |               |               |                 +--|  |                               |
     *          |               |               |                 |  |  |                               |
     *          |               |               |                 |(7)afterSelectionChange(List[Integer],List[T])
     *          |               |               |                 |  |  |                               |
     *          |               |               |                 +->|  |                               |
     *          |               |               |                    \  |                               |
     *          |               |               |                     \ |                               |
     *          |               |               |                       |                               |
     * </pre>
     * 
     * 
     * @see AbstractBbTableMasterForm#doRefresh(List)
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    protected class MasterFormListSelectionHandler extends ListSelectionListenerSupport {

        /**
         * {@inheritDoc}
         */
        @Override
        protected final void onMultiSelection(int[] viewIndexes) {

            final List<Integer> list = new ArrayList<Integer>(viewIndexes.length);
            for (int i = 0; i < viewIndexes.length; ++i) {
                list.add(i, viewIndexes[i]);
            }

            this.delegateSelectionChange(list);
        }

        /**
         * Called when nothing gets selected.
         * <p>
         * Override this method to handle empty selection
         */
        @SuppressWarnings("unchecked")
        @Override
        protected final void onNoSelection() {

            if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
                AbstractBbTableMasterForm.LOGGER.debug("Selection is empty");
            }

            AbstractBbTableMasterForm.this.handleSelectionChange(//
                    ListUtils.EMPTY_LIST, ListUtils.EMPTY_LIST, ListUtils.EMPTY_LIST);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected final void onSingleSelection(int viewIndex) {

            this.delegateSelectionChange(Arrays.asList(viewIndex));
        }

        /**
         * Delegates selection changes, either single or multiple, into the appropiate method.
         * 
         * @param viewIndexes
         *            user relative indexes.
         * 
         * @see AbstractBbTableMasterForm#doRefresh(List)
         * @see #onSingleSelection(int, int, Object)
         * @see #onMultiSelection(List, List, List)
         */
        private final void delegateSelectionChange(List<Integer> viewIndexes) {

            // Constants
            final AbstractBbTableMasterForm<T> masterForm = AbstractBbTableMasterForm.this;
            final JTable masterTable = masterForm.getMasterTable();
            final GlazedTableModel masterTableModel = masterForm.getMasterTableModel();

            // Obtain the new selection
            final List<Integer> modelIndexes = TableUtils.getModelIndexes(masterTable, viewIndexes);
            final List<T> newSelection = TableUtils.getSelection(masterTable, masterTableModel, modelIndexes);

            // Current selection state (none, single or multiple)
            final Boolean isEmptySelection = viewIndexes.isEmpty();
            final Boolean isSingleSelection = (viewIndexes.size() == 1);
            final Boolean isMultiSelection = !isEmptySelection && !isSingleSelection;

            // Invoke appropiate delegate method and alert user on refresh failure
            if (isEmptySelection) {
                Assert.state(!isEmptySelection, "Invariant broken, empty selection is managed by \"#onNoSelection\"");
            } else if (isSingleSelection) {
                this.onSingleSelection(modelIndexes.get(0), viewIndexes.get(0), newSelection.get(0));
            } else if (isMultiSelection) {
                this.onMultiSelection(modelIndexes, viewIndexes, newSelection);
            }
        }

        /**
         * Called when multiple entities get selected.
         * <p>
         * Override this method to handle multi selection.
         * 
         * @param modelIndexes
         *            master event list relative indexes of the selection.
         * @param viewIndexes
         *            user relative indexes of the selection.
         * @param newSelection
         *            the selected entities.
         * 
         * @see #doSelectionChange(List, List, List)
         */
        private void onMultiSelection(List<Integer> modelIndexes, List<Integer> viewIndexes, List<T> newSelection) {

            if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
                AbstractBbTableMasterForm.LOGGER.debug("Selected rows " + ArrayUtils.toString(viewIndexes));
            }

            AbstractBbTableMasterForm.this.handleSelectionChange(modelIndexes, viewIndexes, newSelection);
        }

        /**
         * Called when a single entity gets selected.
         * <p>
         * Override this method to handle single selection
         * 
         * @param modelIndex
         *            master event list relative index of the selection.
         * @param viewIndex
         *            user relative index of the selection.
         * @param newSelection
         *            the selected entity.
         * 
         * @see #doSelectionChange(List, List, List)
         */
        @SuppressWarnings("unchecked")
        private void onSingleSelection(int modelIndex, int viewIndex, T newSelection) {

            if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
                AbstractBbTableMasterForm.LOGGER.debug("Selected row " + viewIndex);
            }

            AbstractBbTableMasterForm.this.handleSelectionChange(//
                    Arrays.asList(modelIndex), Arrays.asList(viewIndex), Arrays.asList(newSelection));
        }
    }

    /**
     * An abstract confirmation dialog implementation that requires user confirmation before proceed.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    protected abstract class RequestUserConfirmationDialog extends ConfirmationDialog {

        /**
         * Creates the confirmation dialog given its title and message.
         * 
         * @param title
         *            the confirmation dialog title.
         * @param message
         *            the confirmation dialog message.
         */
        public RequestUserConfirmationDialog(String title, String message) {

            super(title, message);

            this.setCloseAction(CloseAction.DISPOSE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected JComponent createDialogContentPane() {

            final String description = DirtyTrackingUtils.getI18nDirtyPropertiesHtmlString(//
                    AbstractBbTableMasterForm.this.getDetailFormModel());

            // The dialog contains a message area on top and a formatted text field at the bottom.
            final JComponent messageAreaPane = super.createDialogContentPane();
            final JComponent formattedTextField = new JFormattedTextField(description);

            final JPanel jPanel = new JPanel(new BorderLayout());
            jPanel.add(messageAreaPane, BorderLayout.NORTH);
            jPanel.add(formattedTextField, BorderLayout.SOUTH);

            return jPanel;
        }
    }
}
