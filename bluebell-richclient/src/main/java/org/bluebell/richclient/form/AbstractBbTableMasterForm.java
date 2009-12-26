package org.bluebell.richclient.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bluebell.richclient.application.config.FilterCommand;
import org.bluebell.richclient.command.support.CommandUtil;
import org.bluebell.richclient.form.support.FilterModelAwareListSelectionHandler;
import org.bluebell.richclient.form.util.BbFormModelHelper;
import org.bluebell.richclient.table.support.FilterModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.form.ConfigurableFormModel;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.context.ApplicationEvent;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.config.ApplicationWindowAware;
import org.springframework.richclient.application.event.LifecycleApplicationEvent;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.CommandGroupFactoryBean;
import org.springframework.richclient.command.TargetableActionCommand;
import org.springframework.richclient.command.support.GlobalCommandIds;
import org.springframework.richclient.exceptionhandling.delegation.ExceptionHandlerDelegate;
import org.springframework.richclient.form.AbstractTableMasterForm;
import org.springframework.richclient.form.FormModelHelper;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Extiende el comportamiento de {@link AbstractTableMasterForm } simplificando
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
 * <dd>{@link #setVisibleEntities(Collection)}
 * <dd>{@link #setVisibleEntities(Collection, boolean)}
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
public abstract class AbstractBbTableMasterForm<T extends Object> extends AbstractTableMasterForm implements
	GlobalCommandsAccessor, ApplicationWindowAware {

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
    private static final Logger LOGGER = LoggerFactory.getLogger(BbPageComponentsConfigurer.class);

    /**
     * <em>Flag</em> indicando si se ha de refrescar siempre una entidad ante una llamada a
     * {@link #doRefreshIfNeeded(Object)}.
     */
    private Boolean alwaysRefresh = Boolean.TRUE;

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
     * El manejador para los cambios de selección en la tabla maestra.
     */
    private ListSelectionListener listSelectionHandler;

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
     * 
     * @see ReturnEmptyCollection
     */
    public AbstractBbTableMasterForm(String formId, Class<T> detailType) {

	this(FormModelHelper.createFormModel(//
		new ReturnEmptyCollection(), //
		formId + AbstractBbTableMasterForm.PARENT_FORM_MODEL_SUFIX), //
		ReturnEmptyCollection.PROPERTY_NAME, //
		formId,//
		detailType);

	((ConfigurableFormModel) this.getFormModel().getParent()).setEnabled(Boolean.TRUE);
    }

    /**
     * Construye el formulario maestro utilizando el modelo padre y una de sus propiedades.
     * <p>
     * This constructor is not needed anymore.
     * 
     * @param parentFormModel
     *            el modelo del formulario padre jerárquico.
     * @param property
     *            la propiedad del modelo padre que se recuperará para obtener la lista de entidades editables por este
     *            formulario.
     * @param formId
     *            el idetificador del formulario maestro.
     * @param detailType
     *            la clase del tipo detalle.
     */
    private AbstractBbTableMasterForm(HierarchicalFormModel parentFormModel, String property, String formId,
	    Class<T> detailType) {

	super(parentFormModel, property, formId, detailType);
	this.getFormModel().setValidating(Boolean.FALSE);
    }

    /**
     * Permite refrescar una entidad en el sistema siempre y cuando no este en curso una operación de salvado.
     * 
     * @param object
     *            el objeto a refrescar.
     * @return el objeto refrescado salvo que haya una operación de salvado en curso, en cuyo caso se retorna el objeto
     *         pasado como parámetro. Retorna <code>null</code> si se produce un error.
     */
    // FIXME, (JAF), 20090910, since this method is referenced from
    // FilterModelAwareListSelectionHandler#handleSelection(int[]) its
    // visibility is changed from "protected" to "public"
    @SuppressWarnings("unchecked")
    public final T doRefreshIfNeeded(T object) {

	final AbstractBbDetailForm<T> detailForm = (AbstractBbDetailForm<T>) this.getDetailForm();

	final T refreshedObject = (detailForm.isCommiting() && !this.isAlwaysRefresh()) ? object : this
		.doRefresh(object);

	// Publicar el evento
	AbstractBbTableMasterForm.this.publishApplicationEvent(EventType.REFRESHED, refreshedObject);

	return refreshedObject;
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
    public ApplicationWindow getApplicationWindow() {

	return (this.applicationWindow != null) ? this.applicationWindow //
		: Application.instance().getActiveWindow();
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
     * Obtiene el comando de refresco de entidades y si no existe lo crea.
     * 
     * @return el comando de refresco.
     */
    public ActionCommand getRefreshCommand() {

	if (this.refreshCommand == null) {
	    this.refreshCommand = this.createRefreshCommand();
	}
	return this.refreshCommand;
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
     * Obtiene las entidades seleccionadas.
     * 
     * @return las entidades seleccionadas.
     */
    public final Collection<T> getSelectedEntities() {

	final Collection<T> selectedItems = new ArrayList<T>();

	// Recuperar las entidades seleccionadas.
	this.doForEachSelectedItem(new ListClosure<T>() {

	    public T call(T item, int index) {

		selectedItems.add(item);
		return item;
	    }
	});

	return selectedItems;
    }

    /**
     * Obtiene la entidad seleccionada.
     * 
     * @return la primera entidad seleccionada o <code>null</code> si no existiera ninguna.
     */
    public final T getSelectedEntity() {

	final Collection<T> selectedEntities = this.getSelectedEntities();

	return selectedEntities.isEmpty() ? null : selectedEntities.iterator().next();
    }

    /**
     * Indica si se ha de refrescar siempre una entidad ante una llamada a {@link #doRefreshIfNeeded(Object)}.
     * 
     * @return <code>true</code> si se ha de refrescar siempre y <code>false</code> en caso contrario.
     */
    public Boolean isAlwaysRefresh() {

	return this.alwaysRefresh;
    }

    /**
     * Método que devuelve al formulario a su situación inicial después de un fallo.
     * <p>
     * Por el momento limpia la selección de la tabla maestra y resetea los formularios hijos.
     */
    public void keepAliveAfterFailure() {

	this.setSelectedEntity(null);
	this.getDetailForm().reset();
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
    public void publishApplicationEvent(EventType eventType, T source) {

	final ApplicationEvent applicationEvent = new LifecycleApplicationEvent(eventType.toString(), source);
	this.getApplicationContext().publishEvent(applicationEvent);
    }

    /**
     * Establece si se ha de refrescar siempre una entidad ante una llamada a {@link #doRefreshIfNeeded(Object)}.
     * 
     * @param alwaysRefresh
     *            <code>true</code> si se ha de refrescar siempre y <code>false</code> en caso contrario.
     */
    public void setAlwaysRefresh(Boolean alwaysRefresh) {

	this.alwaysRefresh = alwaysRefresh;
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
     * Permite seleccionar una entidad en la tabla maestra.
     * <p>
     * Si la entidad a seleccionar es <code>null</code> entonces limpia la selección. En el caso de que no pertenezca a
     * la tabla maestra la añade y en cualquier caso, si el modelo no la filtra, entonces la selecciona.
     * 
     * @param entity
     *            la entidad a seleccionar.
     * 
     * @see FilterModelUtil#setSelectedEntity(ca.odell.glazedlists.EventList, javax.swing.JTable, Object)
     */
    @SuppressWarnings("unchecked")
    public final void setSelectedEntity(T entity) {

	// (JAF), 20090730, mejora para poder limpiar la selección (ej.: para
	// gestión de errores)
	if (entity == null) {
	    this.getMasterTable().clearSelection();
	} else {
	    // (JAF), 20080105, este método se mantiene por compatibilidad mas
	    // ha sido reescrito en TableModelUtil
	    FilterModelUtil.setSelectedEntity(this.getMasterEventList(), this.getMasterTable(), entity);
	}
    }

    /**
     * Establece las entidades a mostrar reemplazando las anteriores si las hubiere.
     * 
     * @param entities
     *            las entidades a mostrar.
     * 
     * @see #setVisibleEntities(Collection, boolean)
     */
    public final void setVisibleEntities(Collection<T> entities) {

	this.setVisibleEntities(entities, false);
    }

    /**
     * Establece las entidades a mostrar.
     * <p>
     * Si el formulario detalle está <em>dirty</em> solicita la confirmación del usuario.
     * 
     * @param entities
     *            las entidades a mostrar.
     * 
     * @param attach
     *            <em>flag</em> indicando si las nuevas entidades se han de sumar a las anteriores (<code>true</code>) o
     *            si por el contrario deben sustituirlos (<code>false</code>).
     */
    @SuppressWarnings("unchecked")
    public void setVisibleEntities(Collection<T> entities, boolean attach) {

	// El índice del elemento seleccionado antes y después de la
	// confirmación del usuario
	int previousIndex = 0;
	int currentIndex = 0;

	// Deseleccionar el formulario maestro recordando el índice anterior y
	// posterior a la selección.
	previousIndex = this.getDetailForm().getSelectedIndex();
	// Solicitar la confirmación del usuario
	((FilterModelAwareListSelectionHandler<T>) this.getSelectionHandler()).maybeChangeSelection(-1);
	currentIndex = this.getDetailForm().getSelectedIndex();

	// Proceder si es un attach, o...
	// ... si ha cambiado la selección, o...
	// ... si no había nada seleccionado y sigue sin haberlo.
	boolean proceed = attach;
	proceed |= previousIndex != currentIndex;
	proceed |= (previousIndex == -1) && (currentIndex == -1);

	// Se desinstala primero y reinstala más adelante el selection handler
	// para evitar la publicación redundante de eventos.
	final Collection<T> allEntities = new HashSet<T>(entities);
	if (proceed) {
	    // Desinstalar el selection handler
	    this.uninstallSelectionHandler();

	    if (attach) {
		allEntities.addAll(this.getMasterEventList());
	    }

	    // Actualizar la master event list
	    FilterModelUtil.replace(this.getMasterTableModel(), this.getMasterEventList(), allEntities);

	    // Reinstalar el selection handler
	    this.installSelectionHandler();

	    // Resetear el formulario detalle
	    this.getDetailForm().reset();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

	return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", this.getId()).toString();
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
	final ActionCommand cancelCommand = new TargetableActionCommand(//
		commandId, this.getDetailForm().getCancelCommand());

	// Determinar cuando ha de estar habilitado el comando.
	cancelCommand.setEnabled(Boolean.FALSE);
	this.getNewFormObjectCommand().addEnabledListener(new PropertyChangeListener() {
	    public void propertyChange(PropertyChangeEvent evt) {

		cancelCommand.setEnabled(!(Boolean) evt.getNewValue());
	    }
	});

	// Configurar el comando
	return this.configureCommand(cancelCommand, Boolean.FALSE);
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
	final ActionCommand filterCommand = new FilterCommand(//
		commandId, AbstractBbTableMasterForm.this.getMasterTable());

	// Configurar el comando
	return this.configureCommand(filterCommand, Boolean.FALSE);
    }

    /**
     * Redefine {@link AbstractTableMasterForm#createFormControl()} para que la barra de estado sea consciente de los
     * errores de validación.
     * 
     * @return el componente.
     */
    @Override
    protected JComponent createFormControl() {

	// Obtener el control del formulario maestro.
	final JComponent jComponent = super.createFormControl();

	// HACK, (JAF) 20080725, para que el scrolling sea más usable
	this.getMasterTable().setPreferredScrollableViewportSize(this.getMasterTable().getPreferredSize());

	return jComponent;
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
     * Crea y configura el comando de refresco de entidades.
     * 
     * @return el comando de refresco.
     */
    protected ActionCommand createRefreshCommand() {

	// Obtener los identificadores del comando, separados por comas y
	// ordenados según prioridad
	final String commandId = this.getRefreshCommandFaceDescriptorId();

	// Crear el comando
	final ActionCommand refreshCommand = new ActionCommand(commandId) {

	    /**
	     * Refresca los objetos seleccionados.
	     */
	    @Override
	    @SuppressWarnings("unchecked")
	    protected void doExecuteCommand() {

		final FilterModelAwareListSelectionHandler<T> selectionHandler = (FilterModelAwareListSelectionHandler<T>) //
		AbstractBbTableMasterForm.this.getSelectionHandler();
		final List<Integer> filteredIdxs = FilterModelUtil.getFilteredSelectedIdxs(//
			AbstractBbTableMasterForm.this.getMasterTable());

		// (JAF), 20090629, si clearBefore es true entonces resetear el
		// formulario hijo antes
		final Boolean clearSelection = this.getParameter(//
			AbstractBbTableMasterForm.CLEAR_SELECTION_BEFORE_REFRESH_COMMAND_PARAM) == Boolean.FALSE ? Boolean.FALSE//
			: Boolean.TRUE;
		if (clearSelection) {
		    ((FilterModelAwareListSelectionHandler<T>) //
		    AbstractBbTableMasterForm.this.getSelectionHandler()).maybeChangeSelection(-1);
		}

		selectionHandler.handleSelection(//
			ArrayUtils.toPrimitive(filteredIdxs.toArray(new Integer[filteredIdxs.size()]), 0));
	    }
	};

	// Configurar el comando
	return this.configureCommand(refreshCommand, Boolean.TRUE);
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
	final ActionCommand revertAllCommand = new TargetableActionCommand(//
		commandId, this.getDetailForm().getRevertCommand());

	// Configurar el comando
	return this.configureCommand(revertAllCommand, Boolean.FALSE);
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
	final ActionCommand saveCommand = new TargetableActionCommand(//
		commandId, this.getDetailForm().getCommitCommand());

	// Configurar el comando
	return this.configureCommand(saveCommand, Boolean.TRUE);
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
	final ActionCommand selectAllCommand = new ActionCommand(commandId) {

	    /**
	     * Selecciona todas las entidades de la tabla maestra.
	     */
	    @Override
	    protected void doExecuteCommand() {

		AbstractBbTableMasterForm.this.getMasterTable().selectAll();
	    }
	};

	// Configurar el comando
	return this.configureCommand(selectAllCommand, Boolean.TRUE);
    }

    /**
     * Crea un <em>list selection listener</em> para la tabla maestra de tipo
     * {@link FilterModelAwareListSelectionHandler}.
     * 
     * @return el <em>listener</em>.
     */
    protected ListSelectionListener createSelectionHandler() {

	return new FilterModelAwareListSelectionHandler<T>(this, this.getDetailForm(), this.getMasterTable());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TableModel createTableModel() {

	return BbFormModelHelper.createTableModel(this.getEventList(), this.getColumnPropertyNames(), this.getId());
    }

    /**
     * Sobrescribe el método de la clase base que elimina de la tabla maestra el departamento seleccionado para su
     * eliminación también de forma persistente.
     */
    @Override
    protected final void deleteSelectedItems() {

	// Resetear el formulario detalle para que no dé falsos avisos de dirty
	this.getDetailForm().reset();

	this.doForEachSelectedItem(new ListClosure<T>() {

	    public T call(T entity, int index) {

		// Eliminar la entidad y retonar null en caso de fallo.
		final boolean success = AbstractBbTableMasterForm.this.doDelete(entity) != null;
		if (!success) {
		    return null;
		}

		// Actualizar la tabla maestra.
		AbstractBbTableMasterForm.this.getMasterEventList().remove(entity);

		// Publicar el evento de notificación de borrado.
		AbstractBbTableMasterForm.this.publishApplicationEvent(EventType.DELETED, entity);

		return entity;
	    }
	});
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
     * Permite refrescar una entidad en el sistema (Se corresponde con la 'R' de C<b>R</b>UD).
     * 
     * @param object
     *            el objeto a refrescar.
     * @return el objeto refrescado y <code>null</code> en caso de error.
     */
    protected abstract T doRefresh(T object);

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

	return this.getCommandName(AbstractBbTableMasterForm.CANCEL_COMMAND_ID);
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
     * Obtiene un nuevo nombre para un comando a partir del nombre inicial de la forma "xxxCommand".
     * <p>
     * El nombre resultante es "xxxYyyCommand,xxxCommand" donde "yyy" es el tipo de los objetos editados por este
     * formulario con la primera letra mayúscula.
     * 
     * @param defaultCommandName
     *            el nombre por defecto del comando.
     * @return el nuevo nombre.
     * 
     * @see CommandUtil#getCommandFaceDescriptorId(String, String)
     */
    protected String getCommandName(String defaultCommandName) {

	// (JAF), 20080105, este método se mantiene por compatibilidad mas ha
	// sido reescrito en CommandUtil

	final String detailType = StringUtils.capitalize(ClassUtils.getShortName(this.getDetailType()));

	return CommandUtil.getCommandFaceDescriptorId(defaultCommandName, detailType);
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

	return this.getCommandName(AbstractBbTableMasterForm.FILTER_COMMAND_ID);
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

	return this.getCommandName(AbstractBbTableMasterForm.NEW_FORM_OBJECT_COMMAND_ID);
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
     * Devuelve el <em>list selection listener</em> de la tabla maestra y si no existe lo crea.
     * 
     * @return el <em>listener</em>.
     */
    @Override
    protected final ListSelectionListener getSelectionHandler() {

	if (this.listSelectionHandler == null) {
	    this.listSelectionHandler = this.createSelectionHandler();
	}
	return this.listSelectionHandler;
    }

    /**
     * Actualiza los controles en función del estado del formulario.
     * <p>
     * Añade al comportamiento original ( {@link AbstractTableMasterForm#updateControlsForState()}) la capacidad de
     * habilitar/deshabilitar el comando de refresco.
     */
    @Override
    protected void updateControlsForState() {

	super.updateControlsForState();

	// v2.5.18: el comando se activa si hay alguna fila seleccionada
	// final Boolean enabled = !this.getSelectedEntities().isEmpty();
	// this.getRefreshCommand().setEnabled(enabled);

	this.getRefreshCommand().setEnabled(this.getDeleteCommand().isEnabled());
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
     * @see CommandUtil#configureCommand(ActionCommand, org.springframework.binding.form.ValidatingFormModel)
     */
    private ActionCommand configureCommand(ActionCommand command, Boolean longRunningCommand) {

	return CommandUtil.configureCommand(command, this.getFormModel(), longRunningCommand);
    }

    /**
     * Ejecuta un objeto función para cada elemento seleccionado en la tabla maestra.
     * 
     * @param closure
     *            el objeto función.
     */
    @SuppressWarnings("unchecked")
    private void doForEachSelectedItem(ListClosure<T> closure) {

	// (JAF), 20080624, recordar los índices de los elementos seleccionados,
	// de no hacerlo así la closure podría alterar el selection model.
	final List<Integer> originalSelectedIdxs = FilterModelUtil.getOriginalSelectedIdxs(this.getMasterTable());

	// Iterar sobre los elementos seleccionados en orden inverso y ejecutar
	// la closure
	for (final Integer originalIdx : originalSelectedIdxs) {
	    final T object = (T) this.getMasterEventList().get(originalIdx);
	    if (closure.call(object, originalIdx) == null) {
		// TODO, (JAF), 20080428, evaluar como gestionar un fallo
		// de la closure en este punto.
		if (AbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
		    AbstractBbTableMasterForm.LOGGER.debug("Deleting " + object + "has failed");
		}
	    }
	}
    }

    /**
     * Tipos de eventos que un formulario maestro puede publicar.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static enum EventType {
	/**
	 * Tipo de evento para reflejar que se ha creado una entidad.
	 */
	CREATED(LifecycleApplicationEvent.CREATED),
	/**
	 * Tipo de evento para reflejar que se ha borrado una entidad.
	 */
	DELETED(LifecycleApplicationEvent.DELETED),
	/**
	 * Tipo de evento para reflejar que se ha modificado una entidad.
	 */
	MODIFIED(LifecycleApplicationEvent.MODIFIED),
	/**
	 * Tipo de evento para reflejar que se ha refrescado una entidad.
	 */
	REFRESHED("lifecycleEvent.refreshed");

	/**
	 * Cadena identificando el tipo del evento.
	 */
	private final String type;

	/**
	 * Obtiene el tipo del evento.
	 * 
	 * @param type
	 *            el tipo del evento.
	 */
	private EventType(String type) {

	    this.type = type;
	}

	/**
	 * Imprime el tipo del evento.
	 * 
	 * @return el tipo del evento.
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {

	    return this.type;
	}
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

	    // final ApplicationWindow window = Application.instance().getActiveWindow();
	    // final ApplicationPage page = window != null ? window.getPage() //
	    // : null;

	    // TODO, 20090919, me da que haciendolo con hilos ya no habría estos problemas

	    // if (page != null && page instanceof BbVLDockingApplicationPage) {
	    //
	    // final BbVLDockingApplicationPage<Object> tPage = (BbVLDockingApplicationPage<Object>) page;
	    // final AbstractBbTableMasterForm<Object> masterForm = tPage.getLastMasterForm();
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

	    // Nothing to do.
	}
    }

    /**
     * Clase cuyo método <em>getter</em> para la propiedad {@link #PROPERTY_NAME} devuelve siempre una colección vacía.
     * <p>
     * Simplifica la creación de modelos padres del formulario maestro.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    protected static class ReturnEmptyCollection {

	/**
	 * El nombre de la propiedad.
	 */
	public static final String PROPERTY_NAME = "emptyCollection";

	/**
	 * Devuelve una colección vacía.
	 * 
	 * @return una colección vacía.
	 */
	public Collection<Object> getEmptyCollection() {

	    return Collections.emptySet();
	}
    }

    /**
     * <em>Closure</em> especializada para el trabajo con listas.
     * 
     * @param <T>
     *            el tipo de los elementos de la lista.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private interface ListClosure<T> {
	/**
	 * Ejecuta una función sobre un elemento de una lista.
	 * 
	 * @param item
	 *            el elemento de la lista.
	 * @param index
	 *            el índice del elemento correlativo a la lista.
	 * @return el <em>item</em> pasado como parámetro una vez ejecutada la <em>closure</em> o <code>null</code> en
	 *         caso de fallo.
	 */
	T call(T item, int index);
    }
}
