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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import org.bluebell.richclient.application.config.FilterCommand;
import org.bluebell.richclient.command.support.CommandUtils;
import org.bluebell.richclient.table.support.TableUtils;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.context.ApplicationEvent;
import org.springframework.richclient.application.event.LifecycleApplicationEvent;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.ActionCommandExecutor;
import org.springframework.richclient.command.TargetableActionCommand;
import org.springframework.richclient.command.support.GlobalCommandIds;
import org.springframework.richclient.exceptionhandling.delegation.ExceptionHandlerDelegate;
import org.springframework.richclient.form.AbstractDetailForm;
import org.springframework.richclient.form.Form;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Extiende el comportamiento de {@link org.springframework.richclient.form.AbstractTableMasterForm } simplificando
 * la utilización de formularios maestros y añadiéndole capacidades adicionales.
 * Los cambios realizados son:
 * <ul>
 * <li>Implementa {@link GlobalCommandsAccessor}.
 * <li>Notifica a la barra de estado ante errores de validación.
 * <li>Incorpora métodos de conveniencia para realizar operaciones
 * <em>CRUD (<b>C</b>reate, <b>R</b>ead, <b>U</b>pdate, <b>D</b>elete</em>)
 * básicas en la capa de servicio.
 * <dl>
 * <dt><em>C</em>reate
 * <dd>{@link #doInsert(Object)}
 * <dt><em>R</em>ead
 * <dd>{@link #doRefresh(Object)}
 * <dd>{@link #showEntities(java.util.Collection)}
 * <dd>{@link #showEntities(java.util.Collection, boolean)}
 * <dt><em>U</em>pdate
 * <dd>{@link #doUpdate(Object)}
 * <dt><em>D</em>elete
 * <dd>{@link #doDelete(Object)}
 * </dl>
 * </ul>
 * 
 * @param <T>
 *            el tipo de las entidades que gestiona este formulario.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */

/**
 * Extiende el comportamiento de {@link AbstractBb1TableMasterForm } simplificando la utilización de los formularios
 * maestros y añadiéndole capacidades adicionales. Los cambios realizados son:
 * <ul>
 * <li>Añadido constructor a partir de una <em>javabean</em> y la propiedad a utilizar para obtener la relación de
 * entidades a mostrar.
 * <li>El control devuelto no incluye el formulario detalle ( {@link #createControl()}), además es posible modificarlo
 * en las clases hijas utilizando el método {@link #onCreateFormControl()}.
 * <li>Posibilidad de incluir formularios hijos para los extremos de las relaciones <em>parent-child</em> (
 * {@link #addChildForm(Form)}.
 * </ul>
 * 
 * @param <T>
 *            el tipo de las entidades que gestiona este formulario.
 * 
 * @see BbDispatcherForm
 * @see AbstractBbChildForm
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public abstract class AbstractB2TableMasterForm<T extends Object> extends AbstractBbTableMasterForm<T> implements
        GlobalCommandsAccessor {

    /**
     * El nombre del parámetro de <code>refreshCommand</code> que le indica si se ha de limpiar la selección antes de
     * ejecutarlo.
     */
    public static final String CLEAR_SELECTION_BEFORE_REFRESH_COMMAND_PARAM = "clearSelection";

    /**
     * Sufijo para el identificador del modelo padre de este formulario.
     */
    protected static final String PARENT_FORM_MODEL_SUFIX = "ParentFormModel";

    /**
     * El identificador por defecto del comando para cancelar la creación de una nueva entidad.
     */
    private static final String CANCEL_COMMAND_ID = "cancelCommand";

    /**
     * El identificador por defecto del comando de filtrado de la tabla maestra.
     */
    private static final String FILTER_COMMAND_ID = "filterCommand";

    /**
     * El identificador por defecto del comando para crear una nueva entidad.
     */
    private static final String NEW_FORM_OBJECT_COMMAND_ID = "newCommand";

    /**
     * The logger.
     */
    // private static final Logger LOGGER = LoggerFactory.getLogger(BbPageComponentsConfigurer.class);

    /**
     * Comando para cancelar la creación de una nueva entidad.
     */
    private ActionCommand cancelCommand;

    /**
     * Comando que filtra la tabla maestra.
     */
    private ActionCommand filterCommand;

    /**
     * Comando que refresca una entidad.
     */
    // private ActionCommand refreshCommand;

    /**
     * Comando que deshace los cambios sobre una entidad.
     */
    private ActionCommand revertAllCommand;

    /**
     * Comando que salva los cambios realizados sobre una entidad.
     */
    private ActionCommand saveCommand;

    /**
     * Comando que selecciona todos los elementos de la tabla maestra.
     */
    private ActionCommand selectAllCommand;

    /**
     * 
     */
    private List<AbstractBbSearchForm<T, ?>> searchForms;

    /**
     * Construye el formulario maestro utilizando un modelo que devuelve siempre una colección vacía.
     * 
     * @param formId
     *            el idetificador del formulario maestro.
     * @param detailType
     *            la clase del tipo detalle.
     */
    public AbstractB2TableMasterForm(String formId, Class<T> detailType) {

        super(formId, detailType);

        this.setSearchForms(new ArrayList<AbstractBbSearchForm<T, ?>>());
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
        Assert.notNull(this.getDetailForm());
        Assert.isInstanceOf(AbstractBbChildForm.class, childForm);

        // Añadir el formulario hijo e indicarle cual es su maestro
        ((AbstractBbChildForm<T>) childForm).setMasterForm(this);
        this.getDetailForm().addChildForm(childForm);
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
     * Gets the dispatcher form.
     * 
     * @return the dispatcher form.
     */
    @SuppressWarnings("unchecked")
    public BbDispatcherForm<T> getDispatcherForm() {

        return (BbDispatcherForm<T>) super.getDetailForm();
    }

    /**
     * Obtiene la relación de formularios hijos de este formulario.
     * 
     * @return los formularios hijos.
     */
    @SuppressWarnings("unchecked")
    public Collection<AbstractBbChildForm<T>> getDetailForms() {

        return ((BbDispatcherForm<T>) this.getDetailForm()).getChildForms();
    }

    /**
     * Gets the searchForms.
     * 
     * @return the searchForms
     */
    public List<AbstractBbSearchForm<T, ?>> getSearchForms() {

        return Collections.unmodifiableList(this.searchForms);
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
        Assert.notNull(this.getDetailForm());

        // Eliminar el formulario hijo
        this.getDetailForm().removeChildForm(childForm);
    }

    /**
     * Establece las entidades a mostrar añadiendo al comportamiento de la clase padre la notificación a los formularios
     * hijos en caso de que no haya ninguna entidad seleccionada.
     * 
     * @param entities
     *            las entidades a mostrar.
     * 
     * @param attach
     *            <em>flag</em> indicando si las nuevas entidades se han de sumar a las anteriores (<code>true</code>) o
     *            si por el contrario deben sustituirlos (<code>false</code>).
     * @see AbstractBb1TableMasterForm#showEntities(Collection, boolean)
     */
    @Override
    public final void showEntities(List<T> entities, Boolean attach) {

        super.showEntities(entities, attach);

        // Si una vez establecido el listado de entidades no hay ninguna seleccionada entonces notificar a los
        // formularios hijos.
        // Esto se hace necesario porque el método en super desinstala el selectionHandler
        // (JAF), 20081001, esta comprobación debería ser siempre true
        if (this.getMasterTable().getSelectionModel().getMaxSelectionIndex() < 0) {
            this.onNoSelection();
        }
    }

    /**
     * Obtiene el comando para cancelar la creación de una nueva entidad.
     * 
     * @return el comando de cancelación.
     * 
     * @see #createFilterCommand()
     */
    public final ActionCommand getCancelCommand() {

        if (this.cancelCommand == null) {
            this.cancelCommand = this.createCancelCommand();
        }
        return this.cancelCommand;
    }

    /**
     * Obtiene el comando de filtrado de la tabla maestra y si no existe entonces lo crea.
     * 
     * @return el comando de filtrado.
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
     * Obtiene el comando que que deshace los cambios sobre una entidad y si no existe lo crea.
     * 
     * @return el comando de <em>undo</em>.
     */
    public ActionCommand getRevertAllCommand() {

        if (this.revertAllCommand == null) {
            this.revertAllCommand = this.createRevertAllCommand();
        }
        return this.revertAllCommand;
    }

    /**
     * Obtiene el comando para guardar los cambios realizados sobre una entidad y si no existe lo crea.
     * 
     * @return el comando de salvado.
     */
    public ActionCommand getSaveCommand() {

        if (this.saveCommand == null) {
            this.saveCommand = this.createSaveCommand();
        }
        return this.saveCommand;
    }

    /**
     * Obtiene el comando de selección de todos los elementos de la tabla maestra y si no existe lo crea.
     * 
     * @return el comando de selección.
     */
    public final ActionCommand getSelectAllCommand() {

        if (this.selectAllCommand == null) {
            this.selectAllCommand = this.createSelectAllCommand();
        }
        return this.selectAllCommand;
    }

    /**
     * Método que devuelve al formulario a su situación inicial después de un fallo.
     * <p>
     * Por el momento limpia la selección de la tabla maestra y resetea los formularios hijos.
     */
    public void keepAliveAfterFailure() {

        this.changeSelection(null);
        this.getDetailForm().reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionCommandExecutor getRefreshCommand() {

        // FIXME
        return this.getNewFormObjectCommand();
    }

    /**
     * Permite refrescar una entidad en el sistema (Se corresponde con la 'R' de C<b>R</b>UD).
     * 
     * @param entities
     *            el objeto a refrescar.
     * @return el objeto refrescado y <code>null</code> en caso de error.
     */
    protected abstract List<T> doRefresh(List<T> entities);

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
     * Obtiene un nuevo nombre para un comando a partir del nombre inicial de la forma "xxxCommand".
     * <p>
     * El nombre resultante es "xxxYyyCommand,xxxCommand" donde "yyy" es el tipo de los objetos editados por este
     * formulario con la primera letra mayúscula.
     * 
     * @param defaultCommandName
     *            el nombre por defecto del comando.
     * @return el nuevo nombre.
     * 
     * @see CommandUtils#getCommandFaceDescriptorId(String, String)
     */
    protected final String getCommandName(String defaultCommandName) {

        final String detailType = StringUtils.capitalize(ClassUtils.getShortName(this.getDetailType()));

        return CommandUtils.getCommandFaceDescriptorId(defaultCommandName, detailType);
    }

    /**
     * Actualiza los controles en función del estado del formulario.
     * <p>
     * Añade al comportamiento original (
     * {@link org.springframework.richclient.form.AbstractTableMasterForm#updateControlsForState()}) la capacidad de
     * habilitar/deshabilitar el comando de refresco.
     */
    @Override
    protected void updateControlsForState() {

        super.updateControlsForState();

        // v2.5.18: el comando se activa si hay alguna fila seleccionada
        // final Boolean enabled = !this.getSelectedEntities().isEmpty();
        // this.getRefreshCommand().setEnabled(enabled);

        // this.getRefreshCommand().setEnabled(this.getDeleteCommand().isEnabled());
    }

    /**
     * Obtiene el identificador del comando de refresco de entidades.
     * 
     * @return el identificador.
     * 
     * @see #getCommandName(String)
     */
    protected String getRefreshCommandFaceDescriptorId() {

        return this.getCommandName(GlobalCommandsAccessor.REFRESH);
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
    protected ActionCommand configureCommand(ActionCommand command, Boolean longRunningCommand) {

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
    protected String constructSecurityControllerId(String commandFaceId) {

        final String[] ids = StringUtils.commaDelimitedListToStringArray(commandFaceId);

        return super.constructSecurityControllerId(ids[0]);
    }

    /**
     * Crea y configura el comando para cancelar la creación de una nueva entidad.
     * 
     * @return el comando de cancelación.
     */
    protected ActionCommand createCancelCommand() {

        Assert.notNull(this.getDetailForm());

        // Obtener los identificadores del comando, separados por comas y
        // ordenados según prioridad
        final String commandId = this.getCancelCommandFaceDescriptorId();

        // Crear el comando y sincronizar su estado enabled/disabled
        final ActionCommand command = new TargetableActionCommand(commandId, this.getDetailForm().getCancelCommand());

        // Determinar cuando ha de estar habilitado el comando.
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

        // Obtener los identificadores del comando, separados por comas y
        // ordenados según prioridad
        final String commandId = this.getFilterCommandFaceDescriptorId();

        // Crear el comando
        final ActionCommand command = new FilterCommand(commandId, AbstractB2TableMasterForm.this.getMasterTable());

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
     * Crea y configura el comando que deshace los cambios sobre una entidad.
     * 
     * @return el comando de <em>undo</em>.
     */
    protected ActionCommand createRevertAllCommand() {

        Assert.notNull(this.getDetailForm());

        // Obtener los identificadores del comando, separados por comas y
        // ordenados según prioridad
        final String commandId = this.getRevertAllCommandFaceDescriptorId();

        // Crear el comando
        final ActionCommand command = new TargetableActionCommand(commandId, this.getDetailForm().getRevertCommand());

        // Configurar el comando
        return this.configureCommand(command, Boolean.FALSE);
    }

    /**
     * Crea y configura el comando para guardar los cambios realizados sobre una entidad.
     * 
     * @return el comando de salvado.
     */
    protected ActionCommand createSaveCommand() {

        Assert.notNull(this.getDetailForm());

        // Obtener los identificadores del comando, separados por comas y
        // ordenados según prioridad
        final String commandId = this.getSaveCommandFaceDescriptorId();

        // Crear el comando
        final ActionCommand command = new TargetableActionCommand(commandId, this.getDetailForm().getCommitCommand());

        // Configurar el comando
        return this.configureCommand(command, Boolean.TRUE);
    }

    /**
     * Crea y configura el comando de selección de todos los elementos de la tabla maestra.
     * 
     * @return el comando de selección
     */
    protected ActionCommand createSelectAllCommand() {

        // Obtener los identificadores del comando, separados por comas y
        // ordenados según prioridad
        final String commandId = this.getSelectAllCommandFaceDescriptorId();

        // Crear el comando
        final ActionCommand command = new ActionCommand(commandId) {

            /**
             * Selecciona todas las entidades de la tabla maestra.
             */
            @Override
            protected void doExecuteCommand() {

                AbstractB2TableMasterForm.this.getMasterTable().selectAll();
            }
        };

        // Configurar el comando
        return this.configureCommand(command, Boolean.TRUE);
    }

    /**
     * Sobrescribe el método de la clase base que elimina de la tabla maestra el departamento seleccionado para su
     * eliminación también de forma persistente.
     */
    @Override
    protected final void deleteSelectedItems() {

        // Resetear el formulario detalle para que no dé falsos avisos de dirty
        this.getDetailForm().reset();

        final List<T> selection = TableUtils.getSelection(this.getMasterTable(), this.getMasterTableModel());

        for (T entity : selection) {

            // Eliminar la entidad y retonar null en caso de fallo.
            final boolean success = (AbstractB2TableMasterForm.this.doDelete(entity) != null);
            if (!success) {

                new String("Avoid CS warning");
                // TODO, do something
            }

            // Actualizar la tabla maestra.
            AbstractB2TableMasterForm.this.getMasterEventList().remove(entity);

            // Publicar el evento de notificación de borrado.
            AbstractB2TableMasterForm.this.publishApplicationEvent(EventType.DELETED, entity);
        }
    }

    /**
     * Permite eliminar una nueva entidad del sistema (Se corresponde con la 'D' de CRU<b>D</b>).
     * 
     * @param object
     *            el objeto a eliminar.
     * @return el objeto eliminado y <code>null</code> en caso de error.
     * 
     * @see #deleteSelectedItems()
     */
    protected abstract T doDelete(T object);

    /**
     * Permite insertar una nueva entidad en el sistema (Se corresponde con la 'C' de <b>C</b>RUD).
     * 
     * @param object
     *            el objeto a insertar.
     * @return el objeto insertado y <code>null</code> en caso de error.
     * 
     * @see AbstractBbDetailForm#postCommit(org.springframework.binding.form.FormModel)
     */
    protected abstract T doInsert(T object);

    /**
     * Permite actualizar una entidad en el sistema (Se corresponde con la 'U' de CR<b>U</b>D).
     * 
     * @param object
     *            el objeto a insertar.
     * @return el objeto actualizado y <code>null</code> en caso de error.
     * 
     * @see AbstractBbDetailForm#postCommit(org.springframework.binding.form.FormModel)
     */
    protected abstract T doUpdate(T object);

    /**
     * Obtiene el identificador del comando para cancelar la creación de una nueva entidad.
     * 
     * @return el identificador.
     * 
     * @see #getCommandName(String)
     */
    protected String getCancelCommandFaceDescriptorId() {

        return this.getCommandName(AbstractB2TableMasterForm.CANCEL_COMMAND_ID);
    }

    /**
     * Obtiene el identificador del comando de salvado de la edición actual.
     * 
     * @return el identificador.
     * 
     * @see #getCommandName(String)
     */
    @Override
    protected String getCommitCommandFaceDescriptorId() {

        return this.getCommandName(GlobalCommandIds.SAVE);
    }

    /**
     * Obtiene el identificador del comando que borra las entidades seleccionadas.
     * 
     * @return el identificador.
     * 
     * @see #getCommandName(String)
     */
    @Override
    protected String getDeleteCommandId() {

        return this.getCommandName(GlobalCommandIds.DELETE);
    }

    /**
     * Obtiene el identificador del comando de filtrado de la tabla maestra.
     * 
     * @return el identificador.
     * 
     * @see #getCommandName(String)
     */
    protected String getFilterCommandFaceDescriptorId() {

        return this.getCommandName(AbstractB2TableMasterForm.FILTER_COMMAND_ID);
    }

    /**
     * Obtiene el identificador del comando que crea una nueva entidad.
     * 
     * @return el identificador.
     * 
     * @see #getCommandName(String)
     */
    @Override
    protected String getNewFormObjectCommandId() {

        return this.getCommandName(AbstractB2TableMasterForm.NEW_FORM_OBJECT_COMMAND_ID);
    }

    /**
     * Obtiene el identificador que deshace los cambios sobre una entidad.
     * 
     * @return el identificador.
     * 
     * @see #getCommandName(String)
     */
    protected String getRevertAllCommandFaceDescriptorId() {

        return this.getCommandName(GlobalCommandsAccessor.REVERT_ALL);
    }

    /**
     * Obtiene el identificador que deshace los cambios parcialmente sobre una entidad.
     * 
     * @return el identificador.
     * 
     * @see #getCommandName(String)
     */
    @Override
    protected String getRevertCommandFaceDescriptorId() {

        return this.getCommandName(GlobalCommandIds.UNDO);
    }

    /**
     * Obtiene el identificador que guarda los cambios realizados sobre una entidad.
     * 
     * @return el identificador.
     * 
     * @see #getCommandName(String)
     */
    protected String getSaveCommandFaceDescriptorId() {

        return this.getCommandName(GlobalCommandIds.SAVE);
    }

    /**
     * Obtiene el identificador para la selección de todas las entidades de la tabla maestra.
     * 
     * @return el identificador.
     * 
     * @see #getCommandName(String)
     */
    protected String getSelectAllCommandFaceDescriptorId() {

        return this.getCommandName(GlobalCommandIds.SELECT_ALL);
    }

    /**
     * Manejador para los cambios del elemento seleccionado. Se ejecuta después de gestionar el cambio de elemento
     * seleccionado.
     * <p>
     * Esta implementación no realiza tarea alguna. Las subclases pueden sobreescribirla para llevar a cabo un
     * comportamiento adicional una vez se cambie el elemento seleccionado.
     * <p>
     * Una posible utilidad de este método es la deshabilitación de los componentes como consecuencia de la naturaleza
     * del elemento seleccionado.
     * 
     * @param originalIdx
     *            el índice del elemento seleccionado.
     * @param selection
     *            la entidad seleccionada.
     */
    protected void afterSelectionChange(int originalIdx, T selection) {

    }

    /**
     * Manejador para los cambios de los elementos seleccionados. Se ejecuta después de gestionar el cambio de los
     * elementos seleccionados.
     * <p>
     * Los mismos principios aplicables a {@link #afterSelectionChange(int, Object)} son válidos para este método.
     * 
     * @param originalIdxs
     *            los índices de los elementos seleccionados relativos a la <em>master event list</em>.
     * @param selection
     *            las entidades seleccionadas.
     * 
     * @see #afterSelectionChange(int, Object)
     */
    protected void afterSelectionChange(int[] originalIdxs, List<T> selection) {

    }

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
    protected final AbstractDetailForm createDetailForm(HierarchicalFormModel parentFormModel, ValueModel valueHolder,
            ObservableList masterList) {

        // Crear el formulario detalle compuesto.
        return new BbDispatcherForm<T>(this, valueHolder);
    }

    /**
     * Redefine {@link AbstractBb1TableMasterForm#createFormControl()} del siguiente modo:
     * <ul>
     * <li>Invoca al método de la clase padre.
     * <li>Da la oportunidad a las clases hijas de modificar el control creado ( {@link #onCreateFormControl()}).
     * <li>Excluye el formulario detalle.
     * <li>Actualiza los controles en función del estado.
     * </ul>
     * 
     * @return el componente con el formulario maestro excluyendo el formulario detalle.
     */
    // @Override
    // protected final JComponent createFormControl() {
    //
    // // Obtener el control del formulario maestro.
    // final JPanel panel = (JPanel) super.createFormControl();
    //
    // // El formulario maestro consta de una tabla y botones.
    // // final JPanel panel = new JPanel(new BorderLayout());
    // // panel.add(new JScrollPane(this.getMasterTable()), BorderLayout.CENTER);
    // // panel.add(this.createButtonBar(), BorderLayout.SOUTH);
    // // panel.setBorder(BorderFactory.createTitledBorder(//
    // // BorderFactory.createEtchedBorder()));
    //
    // // Dar la oportunidad a la clase hija de modificar el control.
    // this.onCreateFormControl(panel);
    //
    // // Actualizar los controles en función del estado.
    // super.updateControlsForState();
    // this.setEnabled(true);
    //
    // return panel;
    // }

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
     * Manejador para los cambios del elemento seleccionado.
     * <p>
     * Esta implementación no realiza tarea alguna. Las subclases pueden sobreescribirla para llevar a cabo un
     * comportamiento adicional cada vez que se cambie el elemento seleccionado.
     * <p>
     * Los usuarios de <b>JPA</b> pueden emplear este método para cargar las relaciones <em>lazy</em>.
     * <p>
     * Ejemplo:
     * 
     * <pre>
     * Assert.isInstanceOf(Departamento.class, selectedObject);
     * 
     * // El departamento seleccionado
     * Departamento departamento = (Departamento) selectedObject;
     * 
     * if (!JpaUtil.isInitialized(departamento.getCargosAcademicos())) {
     *     // Cargar los cargos académicos
     *     departamento = this.departamentoService //
     *             .cargarCargosAcademicosPorDepartamento(departamento);
     *     return departamento;
     * }
     * </pre>
     * 
     * @param selectedIndex
     *            el índice del elemento seleccionado.
     * @param selectedObject
     *            el objeto seleccionado.
     * 
     * @return el objeto seleccionado.
     * 
     * @see #createSelectionHandler()
     * @deprecated En favor de {@link #beforeSelectionChange(int, Object)} y {@link #afterSelectionChange(int, Object)}.
     */
    @Deprecated
    protected T onSelectionChange(int selectedIndex, T selectedObject) {

        return selectedObject;
    }

    /**
     * Sets the searchForms.
     * 
     * @param searchForms
     *            the searchForms to set
     */
    private void setSearchForms(List<AbstractBbSearchForm<T, ?>> searchForms) {

        Assert.notNull(searchForms, "searchForms");

        this.searchForms = searchForms;
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
     * {@inheritDoc}
     */
    @Override
    protected void onNoSelection() {

        super.onNoSelection();

        // Notify detail forms about empty selection
        for (final AbstractBbChildForm<T> childForm : this.getDetailForms()) {
            childForm.onNoSelection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final List<T> beforeSelectionChange(List<Integer> modelIndexes, List<T> selection) {

        final List<T> newSelection = this.doRefresh(selection);

        // Notify detail forms about selection
        for (final AbstractBbChildForm<T> childForm : this.getDetailForms()) {
            childForm.beforeSelectionChange(modelIndexes, newSelection);
        }

        return newSelection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void afterSelectionChange(List<Integer> modelIndexes, List<T> selection) {

        super.afterSelectionChange(modelIndexes, selection);

        // Notify detail forms about selection
        for (final AbstractBbChildForm<T> childForm : this.getDetailForms()) {
            childForm.afterSelectionChange(modelIndexes, selection);
        }
    }
}
