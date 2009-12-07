package org.bluebell.richclient.form.binding.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.bluebell.richclient.application.config.FilterCommand;
import org.bluebell.richclient.command.support.CommandUtil;
import org.bluebell.richclient.form.util.BbFormModelHelper;
import org.bluebell.richclient.table.support.FilterModelUtil;
import org.springframework.binding.form.FieldMetadata;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.CommandGroupFactoryBean;
import org.springframework.richclient.dialog.CloseAction;
import org.springframework.richclient.dialog.FormBackedDialogPage;
import org.springframework.richclient.dialog.TitledPageApplicationDialog;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.FormGuard;
import org.springframework.richclient.form.binding.support.CustomBinding;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;

/**
 * Vincula una propiedad de tipo colección con una tabla con posibilidades para
 * la adición, modificación, borrado y búsqueda de elementos.
 * <p>
 * El control consta de una tabla en la parte izquierda y una botonera vertical
 * en la derecha.
 * <p>
 * Es necesario especificar los nombres de las propiedades a mostrar como
 * columnas en la tabla y el formulario a utilizar para crear y modificar las
 * entidades representadas por cada fila.
 * <p>
 * Es posible configurar el componente devuelto utilizando las siguientes claves
 * (donde xxx es el nombre de la propiedad):
 * <dl>
 * <dt>xxx.yyy
 * <dd>Donde yyy es el nombre de una propiedad de las entidades representadas
 * por cada fila.
 * <dd>Precede a cualquier de los sufijos utilizados para configurar las
 * columnas de una tabla.
 * <dt>filterXxxCommand
 * <dd>Para el comando de filtrado.
 * <dd>Precede a cualquiera de los sufijos utilizados para configurar comandos
 * (ej.: icon, caption...).
 * <dt>addXxxCommand
 * <dd>Para el comando que añade entidades a la tabla.
 * <dd>Precede a cualquiera de los sufijos utilizados para configurar comandos
 * (ej.: icon, caption...).
 * <dt>modifyXxxCommand
 * <dd>Para el comando que modifica entidades de la tabla.
 * <dd>Precede a cualquiera de los sufijos utilizados para configurar comandos
 * (ej.: icon, caption...).
 * <dt>removeXxxCommand
 * <dd>Para el comando que elimina entidades de la tabla.
 * <dd>Precede a cualquiera de los sufijos utilizados para configurar comandos
 * (ej.: icon, caption...).
 * <dt>xxxApplicationDialog.title
 * <dd>El título del diálogo para añadir o editar entidades de la tabla.
 * <dt>xxxApplicationDialog.titlePane
 * <dd>El título del panel de título del diálogo para añadir o editar entidades
 * de la tabla.
 * </dl>
 * 
 * @see TableBinder
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TableBinding extends CustomBinding {

    /**
     * El identificador por defecto del comando que permite añadir un nuevo elemento a la tabla.
     */
    private static final String ADD_COMMAND_ID = "addCommand";

    /**
     * El identificador por defecto del comando de filtrado de la tabla.
     */
    private static final String FILTER_COMMAND_ID = "filterCommand";

    /**
     * El identificador por defecto del comando que permite modificar un elemento de la tabla.
     */
    private static final String MODIFY_COMMAND_ID = "modifyCommand";

    /**
     * El identificador por defecto del comando que permite eliminar un elemento de la tabla.
     */
    private static final String REMOVE_COMMAND_ID = "removeCommand";

    /**
     * El comando que permite añadir un nuevo elemento a la tabla.
     */
    private ActionCommand addCommand;

    /**
     * Los nombres de las columnas de la tabla creada por este <em>binding</em>.
     */
    private String[] columnPropertyNames;

    /**
     * El grupo de comandos asociados a la tabla.
     */
    private CommandGroup commandGroup;

    /**
     * El diálogo a mostrar para añadir o editar una entidad de la tabla.
     */
    private EditingDialog dialog;

    /**
     * El formulario a utilizar en el diálogo que permite añadir o editar una entidad de la tabla.
     */
    private Form dialogBackingForm;

    /**
     * El comando de filtrado de la tabla.
     */
    private ActionCommand filterCommand;

    /**
     * El tamaño de alto del diálogo.
     */
    // TODO, (JAF), 20090513, esta variable debería denominarse dialogHeight.
    private Integer heightDialog;

    /**
     * El comando que permite añadir un nuevo elemento a la tabla.
     */
    private ActionCommand modifyCommand;

    /**
     * El menú <em>popup</em> asociado a la tabla.
     */
    private CommandGroup popupMenu;

    /**
     * El comando que permite eliminar un elemento de la tabla.
     */
    private ActionCommand removeCommand;

    /**
     * El componente tabla creado por este <em>binding</em>.
     */
    private JTable table;

    /**
     * El tamaño de ancho del diálogo.
     */
    // TODO, (JAF), 20090513, esta variable debería denominarse dialogWidth.
    private Integer widthDialog;

    /**
     * Construye el <em>binding</em> a partir del modelo del formulario, la propiedad a la que hace referencia y los
     * nombres de las columnas a representar en la tabla.
     * 
     * @param formModel
     *            el modelo del formulario.
     * @param formPropertyPath
     *            la propiedad a la que hace referencia el <em>binding</em>.
     */
    public TableBinding(FormModel formModel, String formPropertyPath) {

	super(formModel, formPropertyPath, null);

	// Establecer la tabla y sus columnas
	this.setTable(this.getComponentFactory().createTable());
    }

    /**
     * Construye el <em>binding</em> a partir de la tabla a utilizar, el modelo del formulario, la propiedad a la que
     * hace referencia y los nombres de las columnas a representar en la tabla.
     * 
     * @param jTable
     *            la tabla a utilizar.
     * @param formModel
     *            el modelo del formulario.
     * @param formPropertyPath
     *            la propiedad a la que hace referencia el <em>binding</em>.
     */
    public TableBinding(JTable jTable, FormModel formModel, String formPropertyPath) {

	super(formModel, formPropertyPath, null);

	Assert.notNull(jTable);

	// Establecer la tabla y sus columnas
	this.setTable(jTable);
    }

    /**
     * Obtiene el comando que permite añadir un nuevo elemento a la tabla y si no existe lo crea.
     * 
     * @return el comando que permite añadir un nuevo elemento a la tabla.
     */
    public ActionCommand getAddCommand() {

	if (this.addCommand == null) {
	    this.addCommand = this.createAddCommand();
	}

	return this.addCommand;
    }

    /**
     * Obtiene los nombres de las columnas de la tabla creada por este <em>binding</em>.
     * 
     * @return los nombres de las columnas de la tabla creada por este <em>binding</em>.
     */
    public String[] getColumnPropertyNames() {

	return this.columnPropertyNames;
    }

    /**
     * Obtiene el formulario a utilizar en el diálogo que permite añadir o editar una entidad de la tabla.
     * 
     * @return el formulario.
     */
    public Form getDialogBackingForm() {

	return this.dialogBackingForm;
    }

    /**
     * Obtiene el comando de filtrado de la tabla.
     * 
     * @return el comando de filtrado.
     */
    public ActionCommand getFilterCommand() {

	if (this.filterCommand == null) {
	    this.filterCommand = this.createFilterCommand();
	}

	return this.filterCommand;
    }

    /**
     * Obtiene el comando que permite modificar un elemento de la tabla y si no existe lo crea.
     * 
     * @return el comando que permite modificar un elemento de la tabla.
     */
    public ActionCommand getModifyCommand() {

	if (this.modifyCommand == null) {
	    this.modifyCommand = this.createModifyCommand();
	}

	return this.modifyCommand;
    }

    /**
     * Obtiene el comando que permite eliminar un elemento de la tabla y si no existe lo crea.
     * 
     * @return el comando que permite eliminar un elemento de la tabla.
     */
    public ActionCommand getRemoveCommand() {

	if (this.removeCommand == null) {
	    this.removeCommand = this.createRemoveCommand();
	}

	return this.removeCommand;
    }

    /**
     * Obtiene el componente tabla creado por este <em>binding</em>.
     * 
     * @return el componente tabla creado por este <em>binding</em>.
     */
    public JTable getTable() {

	return this.table;
    }

    /**
     * Establece los nombres de las columnas de la tabla creada por este <em>binding</em>.
     * 
     * @param columnPropertyNames
     *            los nombres de las columnas de la tabla creada por este <em>binding</em>.
     */
    public void setColumnPropertyNames(String[] columnPropertyNames) {

	this.columnPropertyNames = columnPropertyNames;
    }

    /**
     * Establece el formulario a utilizar en el diálogo que permite añadir o editar una entidad de la tabla.
     * 
     * @param form
     *            el formulario.
     */
    public void setDialogBackingForm(Form form) {

	this.dialogBackingForm = form;
    }

    /**
     * Establece el alto del diálogo.
     * 
     * @param heightDialog
     *            el alto del diálogo.
     */
    public void setHeightDialog(Integer heightDialog) {

	Assert.notNull(heightDialog);
	Assert.isTrue(heightDialog > 0);

	this.heightDialog = heightDialog;
    }

    /**
     * Establece el ancho del diálogo.
     * 
     * @param widthDialog
     *            el ancho del diálogo.
     */
    public void setWidthDialog(Integer widthDialog) {

	Assert.notNull(widthDialog);
	Assert.isTrue(widthDialog > 0);

	this.widthDialog = widthDialog;
    }

    /**
     * Configura la tabla añadiéndole los <em>listeners</em> necesarios para mejorar la esperiencia de usuario.
     * <p>
     * 20090303 - Se ha modificado la visibilidad de este método para que sea posible modificar su comportamiento (ej.:
     * eliminar o añadir listeners).
     */
    protected void configureTable() {

	// Establecer el modelo
	this.getTable().setModel(this.createTableModel());

	// Añadir los listeners (espacio, doble click, popup y selección para
	// actualizar el estado de los controles)
	this.getTable().addKeyListener(new KeyListener() {

	    public void keyPressed(KeyEvent e) {

		if (e.isAltDown() && (e.getKeyCode() == KeyEvent.VK_ENTER)
			&& TableBinding.this.getModifyCommand().isEnabled()) {
		    TableBinding.this.getModifyCommand().execute();
		}
	    }

	    public void keyReleased(KeyEvent e) {

		// Nothing to do
	    }

	    public void keyTyped(KeyEvent e) {

		// Nothing to do
	    }
	});
	// (JAF), 20090128, obviar el doble click ya que esa combinación está
	// ahora pensada para la navegación.

	// this.getTable().addMouseListener(
	// new PopupMenuMouseListener(this.getPopupMenu().createPopupMenu()) {
	//
	// @Override
	// public void mousePressed(MouseEvent e) {
	//
	// super.mousePressed(e);
	//
	// // Controlar el doble click
	// if ((e.getClickCount() == 2)
	// && TableBinding.this.getModifyCommand().isEnabled()) {
	// TableBinding.this.getModifyCommand().execute();
	// }
	// }
	// });
	this.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

	    public void valueChanged(ListSelectionEvent e) {

		TableBinding.this.updateControlForState();
	    }
	});
    }

    /**
     * Crea y configura el comando que permite añadir un nuevo elemento a la tabla.
     * 
     * @return el comando de agregado.
     */
    protected ActionCommand createAddCommand() {

	// Obtener los identificadores del comando, separados por comas y
	// ordenados según prioridad
	final String commandId = this.getAddCommandFaceDescriptorId();

	// Crear el comando
	final ActionCommand addCommand = new AddCommand(commandId);

	// Configurar el comando
	return CommandUtil.configureCommand(addCommand, (ValidatingFormModel) this.getFormModel());
    }

    /**
     * Crea el grupo de comandos asociados a la tabla.
     * 
     * @return el grupo de comandos.
     */
    protected CommandGroup createCommandGroup() {

	final CommandGroup group = CommandGroup.createCommandGroup(new Object[] { this.getFilterCommand(), //
		CommandGroupFactoryBean.SEPARATOR_MEMBER_CODE, //
		this.getAddCommand(), //
		this.getModifyCommand(), //
		this.getRemoveCommand() });

	group.setCommandRegistry(//
		this.getActiveWindow().getCommandManager());

	return group;
    }

    /**
     * Crea el comando de filtrado de la tabla.
     * 
     * @return el comando de filtrado.
     */
    protected ActionCommand createFilterCommand() {

	// Obtener los identificadores del comando, separados por comas y
	// ordenados según prioridad
	final String commandId = this.getFilterCommandFaceDescriptorId();

	// Crear el comando
	final ActionCommand filterCommand = new FilterCommand(commandId, this.getTable());

	// Configurar el comando
	return CommandUtil.configureCommand(filterCommand, (ValidatingFormModel) this.getFormModel());
    }

    /**
     * Crea y configura el comando que permite modificar un elemento de la tabla.
     * 
     * @return el comando de modificación.
     */
    protected ActionCommand createModifyCommand() {

	// Obtener los identificadores del comando, separados por comas y
	// ordenados según prioridad
	final String commandId = this.getModifyCommandFaceDescriptorId();

	// Crear el comando
	final ActionCommand modifyCommand = new ModifyCommand(commandId);

	// Configurar el comando
	return CommandUtil.configureCommand(modifyCommand, (ValidatingFormModel) this.getFormModel());
    }

    /**
     * Crea el menú popup de la tabla.
     * 
     * @return el menú popup.
     */
    protected CommandGroup createPopupMenu() {

	final CommandGroup group = CommandGroup.createCommandGroup(new Object[] { this.getAddCommand(), //
		this.getModifyCommand(), //
		this.getRemoveCommand() });

	group.setCommandRegistry(//
		this.getActiveWindow().getCommandManager());

	return group;
    }

    /**
     * Crea y configura el comando que permite eliminar un elemento de la tabla.
     * 
     * @return el comando de eliminación.
     */
    protected ActionCommand createRemoveCommand() {

	// Obtener los identificadores del comando, separados por comas y
	// ordenados según prioridad
	final String commandId = this.getRemoveCommandFaceDescriptorId();

	// Crear el comando
	final ActionCommand removeCommand = new RemoveCommand(commandId);

	// Configurar el comando
	return CommandUtil.configureCommand(removeCommand, (ValidatingFormModel) this.getFormModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent doBindControl() {

	// Comprobaciones de parámetros
	Assert.notNull(this.getColumnPropertyNames());
	Assert.notNull(this.getDialogBackingForm());

	// Configurar la tabla
	this.configureTable();

	// Construir el control del binding
	final JScrollPane scroolPane = new JScrollPane(this.getTable());
	final JComponent buttonStack = this.getCommandGroup().createButtonStack();

	// La tabla a la izquierda y los botones a la derecha
	final JPanel jPanel = new JPanel();
	jPanel.setLayout(new BorderLayout());
	jPanel.add(scroolPane, BorderLayout.CENTER);
	jPanel.add(buttonStack, BorderLayout.EAST);

	return jPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void enabledChanged() {

	this.updateControlForState();
    }

    /**
     * Obtiene el identificador del comando que permite añadir un nuevo elemento a la tabla.
     * 
     * @return el identificador.
     * 
     * @see CommandUtil#getCommandFaceDescriptorId(String, String)
     */
    protected String getAddCommandFaceDescriptorId() {

	return CommandUtil.getCommandFaceDescriptorId(//
		TableBinding.ADD_COMMAND_ID, this.getProperty());
    }

    /**
     * Obtiene el grupo de comandos asociados a la tabla.
     * 
     * @return el grupo de comandos.
     */
    protected CommandGroup getCommandGroup() {

	if (this.commandGroup == null) {
	    this.commandGroup = this.createCommandGroup();
	}

	return this.commandGroup;
    }

    /**
     * Obtiene el diálogo a mostrar para añadir o editar una entidad de la tabla, y si no existe lo crea a partir del
     * formulario ( {@link #dialogBackingForm}).
     * 
     * @return el diálogo.
     */
    protected EditingDialog getDialog() {

	if (this.dialog == null) {
	    this.setDialog(new EditingDialog());

	    // FormGuard para que no se habilite el comando Aceptar
	    // cuando el formulario tiene errores.
	    final FormGuard formGuard = new FormGuard(this.getDialogBackingForm().getFormModel());
	    formGuard.addGuarded(this.getDialog(), FormGuard.ON_NOERRORS);
	}

	return this.dialog;
    }

    /**
     * Obtiene el identificador del comando de filtrado de la tabla.
     * 
     * @return el identificador.
     * 
     * @see CommandUtil#getCommandFaceDescriptorId(String, String)
     */
    protected String getFilterCommandFaceDescriptorId() {

	return CommandUtil.getCommandFaceDescriptorId(//
		TableBinding.FILTER_COMMAND_ID, this.getProperty());
    }

    /**
     * Obtiene el alto del diálogo.
     * 
     * @return el alto del diálogo.
     */
    protected Integer getHeightDialog() {

	return this.heightDialog;
    }

    /**
     * Obtiene el identificador del comando que permite modificar un elemento de la tabla.
     * 
     * @return el identificador.
     * 
     * @see CommandUtil#getCommandFaceDescriptorId(String, String)
     */
    protected String getModifyCommandFaceDescriptorId() {

	return CommandUtil.getCommandFaceDescriptorId(//
		TableBinding.MODIFY_COMMAND_ID, this.getProperty());
    }

    /**
     * Crea el menú <em>popup</em> asociado a la tabla.
     * 
     * @return un <em>command group</em> con los comandos del menú <em>popup</em>.
     */
    protected CommandGroup getPopupMenu() {

	if (this.popupMenu == null) {
	    this.popupMenu = this.createPopupMenu();
	}

	return this.popupMenu;
    }

    /**
     * Obtiene el identificador del comando que permite eliminar un elemento de la tabla.
     * 
     * @return el identificador.
     * 
     * @see CommandUtil#getCommandFaceDescriptorId(String, String)
     */
    protected String getRemoveCommandFaceDescriptorId() {

	return CommandUtil.getCommandFaceDescriptorId(//
		TableBinding.REMOVE_COMMAND_ID, this.getProperty());
    }

    /**
     * Obtiene el ancho del diálogo.
     * 
     * @return el ancho del diálogo.
     */
    protected Integer getWidthDialog() {

	return this.widthDialog;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readOnlyChanged() {

	this.updateControlForState();
    }

    /**
     * Establece el diálogo a mostrar para añadir o editar una entidad de la tabla.
     * 
     * @param dialog
     *            el diálogo.
     */
    protected void setDialog(EditingDialog dialog) {

	this.dialog = dialog;
    }

    /**
     * Establece el componente tabla creado por este <em>binding</em>.
     * 
     * @param table
     *            el componente tabla creado por este <em>binding</em>.
     */
    protected void setTable(JTable table) {

	this.table = table;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void valueModelChanged(Object newValue) {

	// Ya que ValueModel y GlazedTableModel comparten event list no es
	// necesario hacer nada.
    }

    /**
     * Crea el modelo del componente tabla creado por este <em>binding</em>.
     * <p>
     * Si el <em>value model</em> de la propiedad asociada al <em>binding</em> no es del tipo esperado lo modifica
     * haciendo uso de {@link BbFormModelHelper#addCollectionValueModel(ValidatingFormModel, String)}.
     * 
     * @return el modelo de la tabla.
     */
    @SuppressWarnings("unchecked")
    private TableModel createTableModel() {

	// Asegurarse de que el value model de la colección es el esperado y en
	// caso contrario modificarlo
	final Object value = this.getValue();
	if (!(value instanceof EventList)) {
	    BbFormModelHelper.addCollectionValueModel((ValidatingFormModel) //
		    this.getFormModel(), this.getProperty(), Boolean.TRUE);
	}

	// Crear el table model
	final EventList<Object> eventList = (EventList<Object>) this.getValue();

	return BbFormModelHelper.createTableModel(eventList, this.getColumnPropertyNames(), this.getProperty());
    }

    /**
     * Actualiza el componente en función de su estado.
     */
    private void updateControlForState() {

	final FieldMetadata fieldMetadata = this.getFormModel().getFieldMetadata(this.getProperty());

	final Boolean enabled = fieldMetadata.isEnabled();
	final Boolean readOnly = fieldMetadata.isReadOnly();
	final int selectedRows = this.getTable().getSelectedRowCount();
	final Boolean isSingleSelection = selectedRows == 1;
	final Boolean isMultipleSelection = selectedRows > 0;

	// Habilitar/Deshabilitar la tabla y los comandos
	this.getTable().setEnabled(enabled && !readOnly);
	this.getFilterCommand().setEnabled(enabled);
	this.getAddCommand().setEnabled(enabled && !readOnly);
	this.getModifyCommand().setEnabled(enabled && !readOnly && isSingleSelection);
	this.getRemoveCommand().setEnabled(enabled && !readOnly && isMultipleSelection);
    }

    /**
     * El comando que permite añadir un nuevo elemento a la tabla.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class AddCommand extends ActionCommand {

	/**
	 * Construye el comando a partir de su identificador.
	 * 
	 * @param id
	 *            el identificador del comando.
	 */
	public AddCommand(String id) {

	    super(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doExecuteCommand() {

	    TableBinding.this.getDialog().setCreatingNewEntity(Boolean.TRUE);
	    TableBinding.this.getDialog().showDialog();
	}
    }

    /**
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class EditingDialog extends TitledPageApplicationDialog {

	/**
	 * <em>Flag</em> indicando si el diálogo se encuentra creando una nueva entidad o editando una existente.
	 */
	private Boolean creatingNewEntity;

	/**
	 * Forma la clave del mensaje con el título del diálogo.
	 */
	private final MessageFormat dialogTitleFmt = new MessageFormat("{0}ApplicationDialog.title");

	/**
	 * Forma la clave del mensaje con el título del panel del diálogo.
	 */
	private final MessageFormat dialogTitlePaneTitleFmt = new MessageFormat("{0}ApplicationDialog.titlePane");

	/**
	 * Construye y configura el diálogo.
	 */
	public EditingDialog() {

	    super();

	    this.setDialogPage(new FormBackedDialogPage(//
		    TableBinding.this.getDialogBackingForm()));

	    // Configurar el diálogo
	    this.setTitle(TableBinding.this.getMessage(//
		    this.dialogTitleFmt.format(//
			    new String[] { TableBinding.this.getProperty() })));
	    this.setTitlePaneTitle(TableBinding.this.getMessage(//
		    this.dialogTitlePaneTitleFmt.format(//
			    new String[] { TableBinding.this.getProperty() })));
	    this.setCloseAction(CloseAction.DISPOSE);

	    // Establecer el tamaño del diálogo
	    if ((TableBinding.this.getWidthDialog() != null) && (TableBinding.this.getHeightDialog() != null)) {
		this.setPreferredSize(new Dimension(//
			TableBinding.this.getWidthDialog(), //
			TableBinding.this.getHeightDialog()));
	    }
	}

	/**
	 * Indica si el diálogo se encuentra creando una nueva entidad o editando una existente.
	 * 
	 * @return <code>true</code> si está creando y <code>false</code> en caso contrario.
	 */
	public Boolean isCreatingNewEntity() {

	    return this.creatingNewEntity;
	}

	/**
	 * Establece si el diálogo se encuentra creando una nueva entidad o editando una existente.
	 * 
	 * @param creatingNewEntity
	 *            <code>true</code> para crear y <code>false</code> para editar.
	 */
	public void setCreatingNewEntity(Boolean creatingNewEntity) {

	    this.creatingNewEntity = creatingNewEntity;
	}

	/**
	 * Si se trata de una creación resetea el formulario, mientras que si es una edición le establece la entidad
	 * objeto de la misma.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void onAboutToShow() {

	    if (this.isCreatingNewEntity()) {
		// Resetear y habilitar el formulario
		TableBinding.this.getDialogBackingForm().reset();
		TableBinding.this.getDialogBackingForm().getFormModel().setEnabled(Boolean.TRUE);
	    } else {
		// Establecer la entidad objeto de la edición en el formulario.
		final EventList<Object> eventList = (EventList<Object>) TableBinding.this.getValue();
		final int index = FilterModelUtil.getOriginalSelectedIdxs(//
			TableBinding.this.getTable()).get(0);

		TableBinding.this.getDialogBackingForm().setFormObject(eventList.get(index));
	    }

	    super.onAboutToShow();
	}

	/**
	 * Comitea el formulario y actualiza la tabla con la entidad modificada o de nueva creación.
	 * 
	 * @return <code>true</code>.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected boolean onFinish() {

	    // Commitear el formulario y obtener la entidad a añadir
	    TableBinding.this.getDialogBackingForm().commit();
	    final Object formObject = TableBinding.this.getDialogBackingForm().getFormObject();

	    // Añadir una nueva entrada a la tabla
	    final EventList<Object> eventList = (EventList<Object>) TableBinding.this.getValue();

	    if (!this.isCreatingNewEntity()) {
		final int index = FilterModelUtil.getOriginalSelectedIdxs(//
			TableBinding.this.getTable()).get(0);
		eventList.set(index, formObject);
	    }

	    if (formObject instanceof Collection) {
		FilterModelUtil.setSelectedEntities(eventList, //
			TableBinding.this.getTable(), //
			(Collection) formObject, //
			Boolean.TRUE);
	    } else {
		FilterModelUtil.setSelectedEntity(eventList, //
			TableBinding.this.getTable(), //
			formObject);

	    }
	    TableBinding.this.getTable().requestFocusInWindow();

	    return Boolean.TRUE;
	}
    }

    /**
     * El comando que permite modificar un elemento a la tabla.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class ModifyCommand extends ActionCommand {

	/**
	 * Construye el comando a partir de su identificador.
	 * 
	 * @param id
	 *            el identificador del comando.
	 */
	public ModifyCommand(String id) {

	    super(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doExecuteCommand() {

	    TableBinding.this.getDialog().setCreatingNewEntity(Boolean.FALSE);
	    TableBinding.this.getDialog().showDialog();
	}
    }

    /**
     * El comando que permite eliminar un elemento de la tabla.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class RemoveCommand extends ActionCommand {

	/**
	 * Construye el comando a partir de su identificador.
	 * 
	 * @param id
	 *            el identificador del comando.
	 */
	public RemoveCommand(String id) {

	    super(id);
	}

	/**
	 * Permite eliminar un elemento de la tabla.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doExecuteCommand() {

	    // Los índices de las filas seleccionadas
	    final List<Integer> indexes = FilterModelUtil.getOriginalSelectedIdxs(//
		    TableBinding.this.getTable());

	    // Eliminar las filas seleccionadas
	    final EventList<Serializable> eventList = (EventList<Serializable>) TableBinding.this.getValue();
	    for (final int index : indexes) {
		eventList.remove(index);
	    }
	}
    }
}
