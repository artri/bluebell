/**
 * Copyright (C) 209 Julio Argüello <julio.arguello@gmail.com>
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
import java.util.List;

import javax.swing.JPopupMenu;

import org.bluebell.richclient.application.config.FilterCommand;
import org.bluebell.richclient.command.support.CommandUtils;
import org.bluebell.richclient.table.support.TableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.config.ApplicationWindowAware;
import org.springframework.richclient.application.event.LifecycleApplicationEvent;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.ActionCommandExecutor;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.CommandGroupFactoryBean;
import org.springframework.richclient.command.TargetableActionCommand;
import org.springframework.richclient.command.support.GlobalCommandIds;
import org.springframework.richclient.exceptionhandling.delegation.ExceptionHandlerDelegate;
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
public abstract class AbstractBb1TableMasterForm<T extends Object> extends AbstractBb0TableMasterForm<T> implements
        GlobalCommandsAccessor, ApplicationWindowAware {

    /**
     * Permite refrescar una entidad en el sistema (Se corresponde con la 'R' de C<b>R</b>UD).
     * 
     * @param entities
     *            el objeto a refrescar.
     * @return el objeto refrescado y <code>null</code> en caso de error.
     */
    protected abstract List<T> doRefresh(List<T> entities);

    /**
     * El nombre del parámetro de <code>refreshCommand</code> que le indica si se ha de limpiar la selección antes de
     * ejecutarlo.
     */
    public static final String CLEAR_SELECTION_BEFORE_REFRESH_COMMAND_PARAM = "clearSelection";

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
    private static final Logger LOGGER = LoggerFactory.getLogger(BbPageComponentsConfigurer.class);

    /**
     * La ventana a la que pertenece el formulario.
     */
    private ApplicationWindow applicationWindow;

    /**
     * Comando para cancelar la creación de una nueva entidad.
     */
    private ActionCommand cancelCommand;

    /**
     * El grupo de comandos de este formulario.
     */
    private CommandGroup commandGroup;

    /**
     * Comando que filtra la tabla maestra.
     */
    private ActionCommand filterCommand;

    /**
     * El menú <em>popup</em> de la tabla maestra.
     */
    private JPopupMenu popupMenu;

    /**
     * Comando que refresca una entidad.
     */
    private ActionCommand refreshCommand;

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
     * Construye el formulario maestro utilizando un modelo que devuelve siempre una colección vacía.
     * 
     * @param formId
     *            el identificador del formulario maestro.
     * @param detailType
     *            la clase del tipo detalle.
     */
    public AbstractBb1TableMasterForm(String formId, Class<T> detailType) {

        super(formId, detailType);
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
     * Obtiene la ventana a la que pertenece este formulario.
     * <p>
     * Nótese que alguien tiene que haberlo establecido antes. Si se utiliza <code>FormBackedView</code> entonces será
     * la vista quien establezca la ventana.
     * <p>
     * Si nadie lo ha establecido entonces devuelve la ventana activa.
     * 
     * @return la ventana.
     * 
     * @see Application#getActiveWindow()
     */
    public final ApplicationWindow getApplicationWindow() {

        return (this.applicationWindow != null) ? this.applicationWindow : Application.instance().getActiveWindow();
    }

    /**
     * Establece la ventana a la que pertenece este formulario.
     * 
     * @param window
     *            la ventana.
     */
    public void setApplicationWindow(ApplicationWindow window) {

        this.applicationWindow = window;
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
     * Método que devuelve al formulario a su situación inicial después de un fallo.
     * <p>
     * Por el momento limpia la selección de la tabla maestra y resetea los formularios hijos.
     */
    public void keepAliveAfterFailure() {

        this.changeSelection(null);
        this.getDetailForm().reset();
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
     * Crea el grupo de comandos de este formulario.
     * 
     * @return el grupo de comandos.
     */
    protected CommandGroup createCommandGroup() {

        final CommandGroup group = CommandGroup.createCommandGroup(new Object[] { this.getFilterCommand(), //
                CommandGroupFactoryBean.SEPARATOR_MEMBER_CODE, //
                CommandGroupFactoryBean.SEPARATOR_MEMBER_CODE, //
                GlobalCommandIds.PROPERTIES, //
                GlobalCommandIds.SAVE, //
                GlobalCommandsAccessor.CANCEL, //
                GlobalCommandIds.DELETE //
                });

        group.setCommandRegistry(this.getApplicationWindow().getCommandManager());

        return group;
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
        final ActionCommand command = new FilterCommand(commandId, AbstractBb1TableMasterForm.this.getMasterTable());

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
     * Crea el menú popup de este formulario.
     * 
     * @return el menú popup.
     */
    protected CommandGroup createPopupMenu() {

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

                AbstractBb1TableMasterForm.this.getMasterTable().selectAll();
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
            final boolean success = (AbstractBb1TableMasterForm.this.doDelete(entity) != null);
            if (!success) {

                new String("Avoid CS warning");
                // TODO, do something
            }

            // Actualizar la tabla maestra.
            AbstractBb1TableMasterForm.this.getMasterEventList().remove(entity);

            // Publicar el evento de notificación de borrado.
            AbstractBb1TableMasterForm.this.publishApplicationEvent(EventType.DELETED, entity);
        }
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

        return this.getCommandName(AbstractBb1TableMasterForm.CANCEL_COMMAND_ID);
    }

    /**
     * El grupo de comandos de este formulario y si no existe entonces lo crea.
     * 
     * @return el grupo de comandos.
     */
    @Override
    protected final CommandGroup getCommandGroup() {

        if (this.commandGroup == null) {
            this.commandGroup = this.createCommandGroup();
        }

        return this.commandGroup;
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

        return this.getCommandName(AbstractBb1TableMasterForm.FILTER_COMMAND_ID);
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

        return this.getCommandName(AbstractBb1TableMasterForm.NEW_FORM_OBJECT_COMMAND_ID);
    }

    /**
     * Obtiene el menú <em>popup</em> de la tabla maestra y si no existe lo crea.
     * 
     * @return el menú.
     */
    @Override
    protected JPopupMenu getPopupMenu() {

        if (this.popupMenu == null) {
            final CommandGroup group = this.createPopupMenu();
            group.setCommandRegistry(this.getApplicationWindow().getCommandManager());

            this.popupMenu = group.createPopupMenu();
        }
        return this.popupMenu;
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
     * Gets the dispatcher form.
     * 
     * @return the dispatcher form.
     */
    @SuppressWarnings("unchecked")
    BbDispatcherForm<T> getDispatcherForm() {

        return (BbDispatcherForm<T>) super.getDetailForm();
    }

}
