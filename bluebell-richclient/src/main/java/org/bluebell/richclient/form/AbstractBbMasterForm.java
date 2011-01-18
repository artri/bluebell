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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bluebell.richclient.command.support.CommandUtils;
import org.bluebell.richclient.form.util.BbFormModelHelper;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.context.ApplicationEvent;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.config.ApplicationWindowAware;
import org.springframework.richclient.application.event.LifecycleApplicationEvent;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.support.GlobalCommandIds;
import org.springframework.richclient.form.AbstractMasterForm;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Extends <code>AbstractMasterForm</code> including template methods and simplifying others.
 * 
 * @param <T>
 *            the type of entities managed by this form.
 * 
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public abstract class AbstractBbMasterForm<T extends Object> extends AbstractMasterForm implements
        ApplicationWindowAware, GlobalCommandsAccessor {

    /**
     * The parent form model sufix id.
     */
    private static final String PARENT_FORM_MODEL_SUFIX = "ParentFormModel";

    /**
     * The default command id for cancel command.
     * 
     * @see #getCancelCommand()
     */
    private static final String CANCEL_COMMAND_ID = "cancelCommand";

    /**
     * The default command id for filter command.
     * 
     * @see #getFilterCommand()
     */
    private static final String FILTER_COMMAND_ID = "filterCommand";

    /**
     * The default command id for new form object command.
     */
    private static final String NEW_FORM_OBJECT_COMMAND_ID = "newCommand";

    /**
     * The windows this form belongs to.
     */
    private ApplicationWindow applicationWindow;

    /**
     * The search forms.
     */
    private List<AbstractBbSearchForm<T, ?>> searchForms = new ArrayList<AbstractBbSearchForm<T, ?>>();

    /**
     * The command group used to build the buttons of this form.
     */
    private CommandGroup buttonsCommandGroup;

    /**
     * The cancel command.
     */
    private ActionCommand cancelCommand;

    /**
     * The filter command.
     */
    private ActionCommand filterCommand;

    /**
     * The refresh command.
     */
    private ActionCommand refreshCommand;

    /**
     * The revert all command.
     */
    private ActionCommand revertAllCommand;

    /**
     * The save command.
     */
    private ActionCommand saveCommand;

    /**
     * The select all command.
     */
    private ActionCommand selectAllCommand;

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
     * @see #AbstractBbMasterForm(HierarchicalFormModel, String, String, Class)
     * @see ParentFormBackingBean
     */
    public AbstractBbMasterForm(String formId, Class<T> detailType) {

        this(BbFormModelHelper.createValidatingFormModel(//
                new ParentFormBackingBean(), Boolean.FALSE, //
                formId + AbstractBbMasterForm.PARENT_FORM_MODEL_SUFIX), //
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
    protected AbstractBbMasterForm(HierarchicalFormModel parentFormModel, String property, String formId,
            Class<T> detailType) {

        super(parentFormModel, property, formId, detailType);

        this.getFormModel().setValidating(Boolean.FALSE);
    }

    /**
     * Shows the given entities.
     * <p>
     * If would there be some entities currently being shown then would replace them.
     * 
     * @param entities
     *            the entities to be shown.
     * 
     * @return <code>true</code> if success and <code>false</code> in other case.
     */
    public abstract Boolean showEntities(List<T> entities);

    /**
     * Shows the given entities.
     * <p>
     * If would there be some entities currently being shown then would replace them.
     * 
     * @param entities
     *            the entities to be shown.
     * @param attach
     *            whether to attach the entities to those currently being shown.
     * 
     * @return <code>true</code> if success and <code>false</code> in other case.
     */
    public abstract Boolean showEntities(List<T> entities, Boolean attach);

    /**
     * Shows the given entities.
     * 
     * @param entities
     *            the entities to be shown.
     * @param attach
     *            whether to attach these entities to those currently being shown.
     * @param force
     *            whether to force showing entities without requesting user confirmation.
     * 
     * @return <code>true</code> if success and <code>false</code> in other case.
     */
    public abstract Boolean showEntities(List<T> entities, Boolean attach, Boolean force);

    /**
     * Selects the given entities.
     * <p>
     * If new selection is either <code>null</code> or empty then clears selection.
     * 
     * @param newSelection
     *            the entities to select.
     */
    public abstract void changeSelection(List<T> newSelection);

    /**
     * Gets the selected entities.
     * 
     * @return the selected entities.
     */
    public abstract List<T> getSelection();

    /**
     * Gets the dispatcher form.
     * 
     * @return the dispatcher form.
     */
    @SuppressWarnings("unchecked")
    public final BbDispatcherForm<T> getDispatcherForm() {

        return (BbDispatcherForm<T>) super.getDetailForm();
    }

    /**
     * Incorpora un formulario hijo a la relación de formularios hijos del formulario detalle.
     * <p>
     * Se asegura de que tanto el formulario hijo pasado como parámetro y el formulario detalle no sean nulos, y en tal
     * caso provoca una excepción. Además el primero debe ser de tipo {@link AbstractBbChildForm}.
     * 
     * @param childForm
     *            el formulario hijo.
     * 
     * @see BbDispatcherForm#addChildForm(org.springframework.richclient.form.Form)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void addChildForm(Form childForm) {

        Assert.notNull(childForm);
        Assert.notNull(this.getDispatcherForm());
        Assert.isInstanceOf(AbstractBbChildForm.class, childForm);

        // Añadir el formulario hijo e indicarle cual es su maestro
        ((AbstractBbChildForm<T>) childForm).setMasterForm(this);
        this.getDispatcherForm().addChildForm(childForm);
    }

    /**
     * Elimina un formulario hijo de la relación de formularios hijos del formulario detalle.
     * <p>
     * Se asegura de que tanto el formulario hijo pasado como parámetro y el formulario detalle no sean nulos, y en tal
     * caso provoca una excepción.
     * 
     * @param childForm
     *            el formulario hijo.
     * @see BbDispatcherForm#removeChildForm(org.springframework.richclient.form.Form)
     */
    @Override
    public final void removeChildForm(Form childForm) {

        // Comprobar los parámetros
        Assert.notNull(childForm);
        Assert.notNull(this.getDispatcherForm());

        // Eliminar el formulario hijo
        this.getDispatcherForm().removeChildForm(childForm);
    }

    /**
     * Obtiene la relación de formularios hijos de este formulario.
     * 
     * @return los formularios hijos.
     */
    public final Collection<AbstractBbChildForm<T>> getChildForms() {

        return ((BbDispatcherForm<T>) this.getDispatcherForm()).getChildForms();
    }

    /**
     * Adds a search form to the page.
     * 
     * @param searchForm
     *            the search form.
     */
    public final void addSearchForm(AbstractBbSearchForm<T, ?> searchForm) {

        Assert.notNull(searchForm, "searchForm");

        if (this.getSearchForms().contains(searchForm)) {
            throw new IllegalStateException(); // TODO cambiar la excepción
        } else if (searchForm.getMasterForm() != null) {
            throw new IllegalStateException(); // TODO cambiar la excepción
        }

        searchForm.setMasterForm(this);
        this.searchForms.add(searchForm);
    }

    /**
     * Gets the searchForms.
     * 
     * @return the searchForms
     */
    public final List<AbstractBbSearchForm<T, ?>> getSearchForms() {

        return Collections.unmodifiableList(this.searchForms);
    }

    /**
     * Gets the command that cancels new form object creation.
     * 
     * @return the command.
     * 
     * @see #createCancelCommand()
     */
    public final ActionCommand getCancelCommand() {

        if (this.cancelCommand == null) {
            this.cancelCommand = this.createCancelCommand();
        }
        return this.cancelCommand;
    }

    /**
     * Gets the command that filters master table.
     * 
     * @return the command.
     * 
     * @see #createFilterCommand()
     */
    public final ActionCommand getFilterCommand() {

        if (this.filterCommand == null) {
            this.filterCommand = this.createFilterCommand();
        }
        return this.filterCommand;
    }

    /**
     * {@inheritDoc}
     * 
     * @see #createRefreshCommand()
     */
    @Override
    public final ActionCommand getRefreshCommand() {

        if (this.refreshCommand == null) {
            this.refreshCommand = this.createRefreshCommand();
        }
        return this.refreshCommand;
    }

    /**
     * Gets the command that reverts all changes on child forms.
     * 
     * @return the command.
     * 
     * @see #createRevertAllCommand()
     */
    public final ActionCommand getRevertAllCommand() {

        if (this.revertAllCommand == null) {
            this.revertAllCommand = this.createRevertAllCommand();
        }
        return this.revertAllCommand;
    }

    /**
     * Gets the command that saves changes.
     * 
     * @return the command.
     * 
     * @see #createSaveCommand()
     */
    public final ActionCommand getSaveCommand() {

        if (this.saveCommand == null) {
            this.saveCommand = this.createSaveCommand();
        }
        return this.saveCommand;
    }

    /**
     * Gets the command that selects all visible entities on master table.
     * 
     * @return the command.
     * 
     * @see #createSelectAllCommand()
     */
    public final ActionCommand getSelectAllCommand() {

        if (this.selectAllCommand == null) {
            this.selectAllCommand = this.createSelectAllCommand();
        }
        return this.selectAllCommand;
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
    public final void setApplicationWindow(ApplicationWindow applicationWindow) {

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
     * Lets user implement insertion on its own.
     * <p>
     * This is the <b>C</b> of CRUD.
     * </p>
     * 
     * @param object
     *            the object to be inserted.
     * @return the inserted object or <code>null</code> if error.
     * 
     * @see BbDispatcherForm#postCommit(org.springframework.binding.form.FormModel)
     */
    protected abstract T doInsert(T object);

    /**
     * Lets user implement refresh on its own.
     * <p>
     * This is the <b>R</b> of CRUD.
     * </p>
     * 
     * @param entities
     *            the object to be refreshed.
     * @return the refreshed object or <code>null</code> if error.
     */
    protected abstract List<T> doRefresh(List<T> entities);

    /**
     * Lets user implement update on its own.
     * <p>
     * This is the <b>U</b> of CRUD.
     * </p>
     * 
     * @param object
     *            the object to be updated.
     * @return the updated object or <code>null</code> if error.
     * 
     * @see BbDispatcherForm#postCommit(org.springframework.binding.form.FormModel)
     */
    protected abstract T doUpdate(T object);

    /**
     * Lets user implement deletion on its own.
     * <p>
     * This is the <b>D</b> of CRUD.
     * </p>
     * 
     * @param object
     *            the object to be deleted.
     * @return the deleted object or <code>null</code> if error.
     * 
     * @see #deleteSelectedItems()
     */
    protected abstract T doDelete(T object);

    /**
     * Crea el formulario detalle utilizando un {@link BbDispatcherForm}.
     * 
     * @param parentFormModel
     *            el modelo del formulario padre.
     * @param valueHolder
     *            <em>value model</em> para el objeto a editar.
     * @param masterList
     *            la relación de objetos editables por el formulario.
     * @return el formulario detalle.
     */
    @Override
    protected final BbDispatcherForm<T> createDetailForm(HierarchicalFormModel parentFormModel, ValueModel valueHolder,
            ObservableList masterList) {
    
        return new BbDispatcherForm<T>(this, valueHolder);
    }

    /**
     * Publica un evento de aplicación indicando que se ha creado, modificado, refrescado o eliminado un objeto.
     * <p>
     * 
     * @param eventType
     *            el tipo del evento.
     * @param source
     *            el objeto desencadenante del evento.
     */
    protected final void publishApplicationEvent(EventType eventType, T source) {
    
        final ApplicationEvent applicationEvent = new LifecycleApplicationEvent(eventType.toString(), source);
        this.getApplicationContext().publishEvent(applicationEvent);
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
     * Gets a command name given a proposal ("xxxCommand").
     * <p>
     * The returned command name is "xxxYyyCommand,xxxCommand", where "yyy" is the detail type capitalized class name.
     * </p>
     * 
     * @param defaultCommandName
     *            the default command name.
     * @return the name.
     * 
     * @see CommandUtils#getCommandFaceDescriptorId(String, String)
     */
    protected final String getCommandName(String defaultCommandName) {

        final String type = StringUtils.capitalize(ClassUtils.getShortName(this.getDetailType()));

        return CommandUtils.getCommandFaceDescriptorId(defaultCommandName, type);
    }

    /**
     * Configura el comando dado previo establecimiento de un <em>security controller id</em>.
     * 
     * @param command
     *            el comando.
     * @param longRunningCommand
     *            indica si es un comando de larga duración.
     * 
     * @return el comando pasado como parámetro una vez configurado.
     * 
     * @see CommandUtils#configureCommand(ActionCommand, org.springframework.binding.form.ValidatingFormModel)
     */
    protected final ActionCommand configureCommand(ActionCommand command, Boolean longRunningCommand) {

        return CommandUtils.configureCommand(command, this.getFormModel(), longRunningCommand);
    }

    /**
     * Sobreescribe {@link org.springframework.richclient.form.AbstractForm#constructSecurityControllerId(String)} para
     * que utilice el identificador más prioritario.
     * 
     * @param commandFaceId
     *            el identificador original.
     * @return el identificador para la seguridad.
     */
    @Override
    protected final String constructSecurityControllerId(String commandFaceId) {

        final String[] ids = StringUtils.commaDelimitedListToStringArray(commandFaceId);

        return super.constructSecurityControllerId(ids[0]);
    }

    /**
     * Creates the command that cancels new form object creation.
     * 
     * @return the command.
     */
    protected abstract ActionCommand createCancelCommand();

    /**
     * Creates the command that filters master table.
     * 
     * @return the command.
     */
    protected abstract ActionCommand createFilterCommand();

    /**
     * {@inheritDoc}
     */
    @Override
    protected ActionCommand createNewFormObjectCommand() {

        return super.createNewFormObjectCommand();
    }

    /**
     * Creates the command that refresh selection on master table.
     * 
     * @return the command.
     */
    protected abstract ActionCommand createRefreshCommand();

    /**
     * Creates the command that reverts all changes on child forms.
     * 
     * @return the command.
     */
    protected abstract ActionCommand createRevertAllCommand();

    /**
     * Creates the command that saves changes.
     * 
     * @return the command.
     */
    protected abstract ActionCommand createSaveCommand();

    /**
     * Creates the command that selects all visible entities on master table.
     * 
     * @return the command.
     */
    protected abstract ActionCommand createSelectAllCommand();

    /**
     * Gets the cancel command id.
     * 
     * @return the id.
     * 
     * @see #getCommandName(String)
     */
    protected String getCancelCommandFaceDescriptorId() {

        return this.getCommandName(AbstractBbMasterForm.CANCEL_COMMAND_ID);
    }

    /**
     * Gets the commit command id.
     * 
     * @return the id.
     * 
     * @see #getCommandName(String)
     */
    @Override
    protected String getCommitCommandFaceDescriptorId() {

        return this.getCommandName(GlobalCommandIds.SAVE);
    }

    /**
     * Gets the delete command id.
     * 
     * @return the id.
     * 
     * @see #getCommandName(String)
     */
    @Override
    protected String getDeleteCommandId() {

        return this.getCommandName(GlobalCommandIds.DELETE);
    }

    /**
     * Gets the filter command id.
     * 
     * @return the id.
     * 
     * @see #getCommandName(String)
     */
    protected String getFilterCommandFaceDescriptorId() {

        return this.getCommandName(AbstractBbMasterForm.FILTER_COMMAND_ID);
    }

    /**
     * Gets the new form object command id.
     * 
     * @return the id.
     * 
     * @see #getCommandName(String)
     */
    @Override
    protected String getNewFormObjectCommandId() {

        return this.getCommandName(AbstractBbMasterForm.NEW_FORM_OBJECT_COMMAND_ID);
    }

    /**
     * Gets the refresh command id.
     * 
     * @return the id.
     * 
     * @see #getCommandName(String)
     */
    protected String getRefreshCommandFaceDescriptorId() {

        return this.getCommandName(GlobalCommandsAccessor.REFRESH);
    }

    /**
     * Gets the revert all command id.
     * 
     * @return the id.
     * 
     * @see #getCommandName(String)
     */
    protected String getRevertAllCommandFaceDescriptorId() {

        return this.getCommandName(GlobalCommandsAccessor.REVERT_ALL);
    }

    /**
     * Gets the revert command id.
     * 
     * @return the id.
     * 
     * @see #getCommandName(String)
     */
    @Override
    protected String getRevertCommandFaceDescriptorId() {

        return this.getCommandName(GlobalCommandIds.UNDO);
    }

    /**
     * Gets the save command id.
     * 
     * @return the id.
     * 
     * @see #getCommandName(String)
     */
    protected String getSaveCommandFaceDescriptorId() {

        return this.getCommandName(GlobalCommandIds.SAVE);
    }

    /**
     * Gets the select all command id.
     * 
     * @return the id.
     * 
     * @see #getCommandName(String)
     */
    protected String getSelectAllCommandFaceDescriptorId() {

        return this.getCommandName(GlobalCommandIds.SELECT_ALL);
    }

    /**
     * Returns whether should proceed with an user action that can breaks a current edition.
     * 
     * @return <code>true</code> to proceed and <code>false</code> in other case.
     */
    abstract Boolean shouldProceed();

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
        protected ParentFormBackingBean(Collection<? extends Object> entities) {

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
