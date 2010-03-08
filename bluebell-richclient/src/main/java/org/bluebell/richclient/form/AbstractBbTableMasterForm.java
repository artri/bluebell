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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.list.SetUniqueList;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bluebell.richclient.form.binding.swing.TableBinding;
import org.bluebell.richclient.form.builder.support.DirtyTrackingUtils;
import org.bluebell.richclient.form.util.BbFormModelHelper;
import org.bluebell.richclient.table.support.TableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.form.ConfigurableFormModel;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.value.support.ValueHolder;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.config.ApplicationWindowAware;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.CommandGroupFactoryBean;
import org.springframework.richclient.command.support.GlobalCommandIds;
import org.springframework.richclient.dialog.ConfirmationDialog;
import org.springframework.richclient.form.AbstractMasterForm;
import org.springframework.richclient.table.ListSelectionListenerSupport;
import org.springframework.richclient.table.support.GlazedTableModel;
import org.springframework.richclient.util.Assert;
import org.springframework.richclient.util.PopupMenuMouseListener;

import ca.odell.glazedlists.EventList;

/**
 * Extends <code>AbstractTableMasterForm</code> in the following way:
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
public abstract class AbstractBbTableMasterForm<T extends Object> extends AbstractMasterForm implements
        ApplicationWindowAware {

    /**
     * Sufix that identifies the parent form model.
     */
    protected static final String PARENT_FORM_MODEL_SUFIX = "ParentFormModel";

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBbTableMasterForm.class);

    /**
     * Debug message for <code>#beforeSelection</code>.
     */
    private static final MessageFormat BEFORE_SELECTION_FMT = //
    new MessageFormat("Before selecting model indexes \"{0}\" on {1}");

    /**
     * Debug message for <code>#afterSelection</code>.
     */
    private static final MessageFormat AFTER_SELECTION_FMT = //
    new MessageFormat("After selecting model indexes \"{0}\" on {1}");

    /**
     * The binding used to render the master table.
     */
    private TableBinding masterTableBinding;

    /**
     * The list selection handler to be installed on the master event list.
     */
    private ListSelectionListener listSelectionHandler;

    /**
     * The command group used to build the buttons of this form.
     */
    private CommandGroup buttonsCommandGroup;

    /**
     * The command group used to build the popup menu of the master table.
     */
    private CommandGroup popupMenuCommandGroup;

    /**
     * The windows this form belongs to.
     */
    private ApplicationWindow applicationWindow;

    /**
     * Creates the master form given its identifier and the detail type.
     * <p>
     * Employs a backing bean property that always returns an empty collection.
     * 
     * @param formId
     *            the form id.
     * @param detailType
     *            the detail type.
     * 
     * @see #AbstractBb0TableMasterForm(HierarchicalFormModel, String, String, Class)
     * @see ParentFormBackingBean
     */
    public AbstractBbTableMasterForm(String formId, Class<T> detailType) {

        this(BbFormModelHelper.createValidatingFormModel(new ParentFormBackingBean(), Boolean.FALSE, //
                formId + AbstractBbTableMasterForm.PARENT_FORM_MODEL_SUFIX), //
                ParentFormBackingBean.PROPERTY_NAME, formId, detailType);
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

        this.getFormModel().setValidating(Boolean.FALSE);
    }

    /**
     * Gets the master table binding.
     * 
     * @return the binding.
     */
    public TableBinding getMasterTableBinding() {

        return this.masterTableBinding;
    }

    /**
     * Gets the selected entities.
     * 
     * @return the selected entities.
     */
    public final List<T> getSelection() {

        return TableUtils.getSelection(this.getMasterTable(), this.getMasterTableModel());
    }

    /**
     * Selects the given entities.
     * <p>
     * If the collection param is either <code>null</code> or empty then clears selection before selecting.
     * 
     * @param newSelection
     *            the entities to select.
     * 
     * @see TableUtils#changeSelection(JTable, GlazedTableModel, EventList, List)
     */
    public final void changeSelection(List<T> newSelection) {

        TableUtils.showEntities(this.getMasterTableModel(), newSelection, Boolean.TRUE);

        final List<Integer> modelIndexes = TableUtils.getModelIndexes(this.getMasterTableModel(), newSelection);
        final List<Integer> viewIndexes = TableUtils.getViewIndexes(this.getMasterTable(), modelIndexes);

        this.handleSelectionChange(modelIndexes, viewIndexes, newSelection);
    }

    /**
     * Show the given entities within the master table. If would there be some entities then replace them.
     * 
     * @param entities
     *            the entities to be shown.
     * 
     * @see #showEntities(Collection, boolean)
     */
    public final void showEntities(List<T> entities) {

        this.showEntities(entities, Boolean.FALSE);
    }

    /**
     * Shows the given entities within the master table.
     * 
     * @param entities
     *            the entities to be shown.
     * 
     * @param attach
     *            whether to attach the entities to currents.
     */
    @SuppressWarnings("unchecked")
    public void showEntities(List<T> entities, Boolean attach) {

        // Reset selection remembering indexes (requests user confirmation)
        final Boolean proceed = attach | this.shouldProceed();

        if (proceed) {

            final List<T> allEntities = SetUniqueList.decorate(entities);

            // Update master event list keeping order and removing duplicates.
            // Uninstall selection handler before proceeding and reinstall later to avoid redundant events.
            this.uninstallSelectionHandler();
            TableUtils.showEntities(this.getMasterTableModel(), allEntities, attach);
            this.installSelectionHandler();

            // Reset detail form
            this.getDetailForm().reset();
        }
    }

    /**
     * Requests user confirmation to proceed with an user action in order to prevent information lost.
     * 
     * @return <code>true</code> to proceed and <code>false</code> in any other case.
     */
    public Boolean requestUserConfirmation() {

        // Dirty dialog
        final String title = AbstractBbTableMasterForm.this.getMessage(//
                new String[] { this.getId() + ".dirtyChange.title", "masterForm.dirtyChange.title" });
        final String message = AbstractBbTableMasterForm.this.getMessage(//
                new String[] { this.getId() + ".dirtyChange.message", "masterForm.dirtyChange.message" });
        final String description = DirtyTrackingUtils.getI18nDirtyPropertiesHtmlString(this.getDetailFormModel());
        final ValueHolder proceed = new ValueHolder(Boolean.FALSE);

        final ConfirmationDialog dialog = new ConfirmationDialog(title, message) {

            /**
             * {@inheritDoc}
             */
            @Override
            protected void onConfirm() {

                if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
                    AbstractBbTableMasterForm.LOGGER.debug("User confirmed request");
                }

                proceed.setValue(Boolean.TRUE);
            }

            /**
             * {@inheritDoc}
             */
            protected void onCancel() {

                if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
                    AbstractBbTableMasterForm.LOGGER.debug("User cancel request");
                }

            };

            /**
             * {@inheritDoc}
             */
            @Override
            protected JComponent createDialogContentPane() {

                // The dialog contains a message area on top and a formatted text field at the bottom.
                final JComponent messageAreaPane = super.createDialogContentPane();
                final JComponent formattedTextField = new JFormattedTextField(description);

                final JPanel jPanel = new JPanel(new BorderLayout());
                jPanel.add(messageAreaPane, BorderLayout.NORTH);
                jPanel.add(formattedTextField, BorderLayout.SOUTH);

                return jPanel;
            }
        };

        dialog.showDialog();

        return (Boolean) proceed.getValue();
    }

    /**
     * Gets the application windows this form belongs to.
     * <p>
     * Note someone should stablish it before. If <code>FormBackedView</code> is used then it will be the one.
     * <p>
     * If it's not stablished then returns the active window.
     * <p>
     * This is needed to avoid race conditions when dealing with <code>Application.instance().getActiveWindow()</code>
     * since while creating a page the active window may be the former.
     * 
     * @return the application window.
     * 
     * @see Application#getActiveWindow()
     */
    public final ApplicationWindow getApplicationWindow() {

        return (this.applicationWindow != null) ? this.applicationWindow : Application.instance().getActiveWindow();
    }

    /**
     * Sets the application windows this form belongs to.
     * 
     * @param applicationWindow
     *            the application window.
     */
    public void setApplicationWindow(ApplicationWindow applicationWindow) {

        Assert.notNull(applicationWindow, "applicationWindow");

        this.applicationWindow = applicationWindow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", this.getId()).toString();
    }

    /**
     * Manages selection change events.
     * <p>
     * Let master form implementors change new selected entities before changing selection definitely. It's also
     * possible to add behaviour after selection.
     * 
     * @param newModelIndexes
     *            master event list relative indexes of the selection.
     * @param newViewIndexes
     *            user relative indexes of the selection.
     * @param selection
     *            the selected entities.
     * 
     * @see AbstractBbTableMasterForm#beforeSelectionChange(List, List)
     * @see #doSelectionChange(int[], int[], List)
     * @see AbstractBbTableMasterForm#afterSelectionChange(List, List)
     */
    protected final void handleSelectionChange(List<Integer> newModelIndexes, List<Integer> newViewIndexes,
            List<T> selection) {

        // Old view and model indexes
        final List<Integer> oldViewIndexes = TableUtils.getSelectedViewIndexes(this.getMasterTable());
        final List<Integer> oldModelIndexes = TableUtils.getModelIndexes(this.getMasterTable(), newViewIndexes);

        this.uninstallSelectionHandler();

        if (this.shouldProceed()) {
            final List<T> newSelection = this.beforeSelectionChange(newModelIndexes, selection);
            this.doSelectionChange(oldModelIndexes, oldViewIndexes, newModelIndexes, newViewIndexes, newSelection);
            this.afterSelectionChange(newModelIndexes, newSelection);
        } else {
            this.undoSelectionChange(oldModelIndexes, oldViewIndexes, newModelIndexes, newViewIndexes, selection);
        }

        this.installSelectionHandler();
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
     * @param selection
     *            the selected entities.
     */
    protected final void doSelectionChange(List<Integer> oldModelIndexes, List<Integer> oldViewIndexes,
            List<Integer> newModelIndexes, List<Integer> newViewIndexes, List<T> selection) {

        TableUtils.changeSelection(this.getMasterTable(), this.getMasterTableModel(), selection);

        // If is single selection the detail form needs to be aware of it
        final Boolean isSingleSelection = (selection.size() == 1);
        if (isSingleSelection) {
            this.getDetailForm().setSelectedIndex(newModelIndexes.get(0));
            DirtyTrackingUtils.clearDirty(AbstractBbTableMasterForm.this.getDetailForm().getFormModel());
        }
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
     * Allows replacing selected entities (i.e.: for lazy loading).
     * 
     * @param modelIndexes
     *            master event list relative indexes of the selection.
     * @param selection
     *            the selected entities.
     * @return the entities to be selected.
     */
    protected List<T> beforeSelectionChange(List<Integer> modelIndexes, List<T> selection) {

        if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
            AbstractBbTableMasterForm.LOGGER.debug(AbstractBbTableMasterForm.BEFORE_SELECTION_FMT.format(//
                    new Object[] { ArrayUtils.toString(modelIndexes), this.getId() }));
        }

        return selection;
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
    protected void afterSelectionChange(List<Integer> modelIndexes, List<T> selection) {

        if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
            AbstractBbTableMasterForm.LOGGER.debug(AbstractBbTableMasterForm.AFTER_SELECTION_FMT.format(//
                    new Object[] { ArrayUtils.toString(modelIndexes), this.getId() }));
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Employs a table binding for displaying the master form.
     */
    @Override
    protected JComponent createFormControl() {

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
     * {@inheritDoc}
     */
    @Override
    protected final CommandGroup getCommandGroup() {

        return this.getButtonsCommandGroup();
    }

    /**
     * Gets the buttons command group and if doesn't exist then creates it.
     * 
     * @return the buttons command group.
     */
    protected final CommandGroup getButtonsCommandGroup() {

        if (this.buttonsCommandGroup == null) {
            this.buttonsCommandGroup = this.createButtonsCommandGroup();
        }

        return this.buttonsCommandGroup;
    }

    /**
     * Creates the buttons command group.
     * 
     * @return the buttons command group.
     */
    protected CommandGroup createButtonsCommandGroup() {

        final CommandGroup group = CommandGroup.createCommandGroup(new Object[] {
        // this.getFilterCommand(), //
                // CommandGroupFactoryBean.SEPARATOR_MEMBER_CODE, //
                // CommandGroupFactoryBean.SEPARATOR_MEMBER_CODE, //
                GlobalCommandIds.PROPERTIES, //
                GlobalCommandIds.SAVE, //
                GlobalCommandsAccessor.CANCEL, //
                GlobalCommandIds.DELETE //
                });

        group.setCommandRegistry(this.getApplicationWindow().getCommandManager());

        return group;
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
     * Gets the master table.
     * 
     * @return the master table.
     */
    protected final JTable getMasterTable() {

        return this.getMasterTableBinding().getTable();
    }

    /**
     * @return Returns the masterTableModel.
     */
    protected final GlazedTableModel getMasterTableModel() {

        return (GlazedTableModel) (getMasterTable() != null ? getMasterTable().getModel() : null);
    }

    /**
     * Gets the property names to show in columns of the master table.
     * 
     * @return an array of property names
     */
    protected abstract String[] getColumnPropertyNames();

    /**
     * Gets the selection model for the master list representation.
     * 
     * @return selection model or <code>null</code> if master table has not been constructed yet.
     */
    @Override
    protected ListSelectionModel getSelectionModel() {

        return this.isControlCreated() ? this.getMasterTable().getSelectionModel() : null;
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
     * Creates the selection handler to be installed in the master event list.
     * 
     * @return the selection handler.
     */
    protected ListSelectionListener createSelectionHandler() {

        return new MasterFormListSelectionHandler();
    }

    /**
     * Returns whether should proceed with an user action that can breaks a current edition.
     * 
     * @return <code>true</code> to proceed and <code>false</code> in any other case.
     */
    private Boolean shouldProceed() {

        if (!this.getDetailForm().isDirty()) {
            return Boolean.TRUE;
        } else {
            return this.requestUserConfirmation();
        }
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
     * Improved list selection listener that request user confirmation before changing selection. This is really useful
     * to prevent information lost after changing the selected entity.
     * <p>
     * It's also capable of transforming selection events handlers into methods with more semantic:
     * <dl>
     * <dt>From {@link #onSingleSelection(int)}:
     * <dd>To {@link #onSingleSelection(int, int, T)}
     * <dt>From {@link #onMultiSelection(int[])}
     * <dd>To {@link #onSingleSelection(int[], int[], List<T>)}
     * </dl>
     * <p>
     * Calls {@link AbstractBbTableMasterForm#doRefresh(List)} after a selection event is raised, in order to allow
     * implementor to refresh selection state in a customized way. This may be useful to synchronize page state when
     * dealing with multiple pages, windows or clients.
     * <p>
     * Internally distinguish between <code>JTable</code> relative row indexes and backing collection relative indexes.
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
        protected final void onSingleSelection(int viewIndex) {

            this.delegateSelectionChange(Arrays.asList(viewIndex));
        }

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
         * Delegates selection changes, either single or multiple, into the appropiate method.
         * 
         * @param viewIndexes
         *            user relative indexes.
         * 
         * @see AbstractBbTableMasterForm#doRefresh(List)
         * @see #onSingleSelection(int, int, Object)
         * @see #onMultiSelection(List, List, List)
         */
        @SuppressWarnings("unchecked")
        protected final void delegateSelectionChange(List<Integer> viewIndexes) {

            // Constants
            final AbstractBbTableMasterForm<T> masterForm = AbstractBbTableMasterForm.this;
            final JTable masterTable = masterForm.getMasterTable();
            final EventList<T> masterEventList = masterForm.getMasterEventList();

            // Obtain the current selection
            List<T> selection = new ArrayList<T>(masterEventList.size());
            // TODO move this logic to filtermodelutil
            final List<Integer> modelIndexes = TableUtils.getModelIndexes(masterTable, viewIndexes);
            for (int i = 0; i < modelIndexes.size(); ++i) {
                final T entity = masterEventList.get(modelIndexes.get(i));
                selection.add(entity);
            }

            // Current selection state (none, single or multiple)
            final Boolean isEmptySelection = viewIndexes.isEmpty();
            final Boolean isSingleSelection = (viewIndexes.size() == 1);
            final Boolean isMultiSelection = !isEmptySelection && !isSingleSelection;

            // Invoke appropiate delegate method and alert user on refresh failure
            if (isEmptySelection) {
                Assert.state(!isEmptySelection, "Invariant broken, empty selection is managed by \"#onNoSelection\"");
            } else if (isSingleSelection) {
                this.onSingleSelection(modelIndexes.get(0), viewIndexes.get(0), selection.get(0));
            } else if (isMultiSelection) {
                this.onMultiSelection(modelIndexes, viewIndexes, selection);
            }
        }

        /**
         * Called when nothing gets selected.
         * <p>
         * Override this method to handle empty selection
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void onNoSelection() {

            if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
                AbstractBbTableMasterForm.LOGGER.debug("Selection is empty");
            }

            AbstractBbTableMasterForm.this.handleSelectionChange(//
                    ListUtils.EMPTY_LIST, ListUtils.EMPTY_LIST, ListUtils.EMPTY_LIST);
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
         * @param selection
         *            the selected entity.
         * 
         * @see #doSelectionChange(List, List, List)
         */
        @SuppressWarnings("unchecked")
        protected void onSingleSelection(int modelIndex, int viewIndex, T selection) {

            if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
                AbstractBbTableMasterForm.LOGGER.debug("Selected row " + viewIndex);
            }

            AbstractBbTableMasterForm.this.handleSelectionChange(//
                    Arrays.asList(modelIndex), Arrays.asList(viewIndex), Arrays.asList(selection));
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
         * @param selection
         *            the selected entities.
         * 
         * @see #doSelectionChange(List, List, List)
         */
        protected void onMultiSelection(List<Integer> modelIndexes, List<Integer> viewIndexes, List<T> selection) {

            if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
                AbstractBbTableMasterForm.LOGGER.debug("Selected rows " + ArrayUtils.toString(viewIndexes));
            }

            AbstractBbTableMasterForm.this.handleSelectionChange(modelIndexes, viewIndexes, selection);
        }
    }

    /**
     * Backing bean for the parent form.
     * <p>
     * Makes it easy creating parent form model.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    protected static final class ParentFormBackingBean {

        /**
         * The property name.
         */
        public static final String PROPERTY_NAME = "entities";

        /**
         * The property of the backing bean holding the entities to be shown.
         */
        private Collection<? extends Object> entities = Collections.emptyList();

        /**
         * Creates the backing bean.
         */
        private ParentFormBackingBean() {

        }

        /**
         * Creates the backing bean given the entities to be shown.
         * 
         * @param entities
         *            the entities.
         */
        private ParentFormBackingBean(Collection<? extends Object> entities) {

            this.setEntities(entities);
        }

        /**
         * Gets the entities to be shown.
         * 
         * @return the entities.
         */
        public Collection<? extends Object> getEntities() {

            return this.entities;
        }

        /**
         * Sets the entities to be shown.
         * 
         * @param entities
         *            the entities.
         */
        public void setEntities(Collection<? extends Object> entities) {

            this.entities = entities;
        }
    }
}
