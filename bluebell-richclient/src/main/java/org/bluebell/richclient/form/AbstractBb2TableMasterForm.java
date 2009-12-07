package org.bluebell.richclient.form;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import org.bluebell.richclient.form.support.FilterModelAwareListSelectionHandler;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.richclient.form.AbstractDetailForm;
import org.springframework.richclient.form.Form;
import org.springframework.util.Assert;

/**
 * Extiende el comportamiento de {@link AbstractBbTableMasterForm } simplificando la utilización de formularios maestros
 * y añadiéndole capacidades adicionales. Los cambios realizados son:
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
public abstract class AbstractBb2TableMasterForm<T extends Object> extends AbstractBbTableMasterForm<T> {

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
    public AbstractBb2TableMasterForm(String formId, Class<T> detailType) {

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
     * @see AbstractBbTableMasterForm#setVisibleEntities(Collection, boolean)
     */
    @Override
    public final void setVisibleEntities(Collection<T> entities, boolean attach) {

	super.setVisibleEntities(entities, attach);

	// Si una vez establecido el listado de entidades no hay ninguna seleccionada entonces notificar a los
	// formularios hijos.
	// Esto se hace necesario porque el método en super desinstala el selectionHandler
	// (JAF), 20081001, esta comprobación debería ser siempre true
	if (this.getMasterTable().getSelectionModel().getMaxSelectionIndex() < 0) {
	    ((ListSelectionHandler) this.getSelectionHandler()).onNoSelection();
	}
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
    protected void afterSelectionChange(int[] originalIdxs, Object[] selection) {

	// (JAF), 20090721, ha sido necesario cambiar la signatura de "T[]
	// selection" a "Object[] selection" para evitar errores en tiempo de
	// ejecución con los castings
    }

    /**
     * Manejador para los cambios del elemento seleccionado. Se ejecuta antes de gestionar el cambio de elemento
     * seleccionado.
     * <p>
     * Esta implementación no realiza tarea alguna. Las subclases pueden sobreescribirla para llevar a cabo un
     * comportamiento adicional antes de que se cambie el elemento seleccionado.
     * 
     * @param originalIdx
     *            el índice del elemento seleccionado relativo a la <em>master event list</em>.
     * @param selection
     *            la entidad seleccionada.
     * 
     * @return la entidad seleccionada modificada (o no) por este manejador.
     */
    @SuppressWarnings("deprecation")
    protected T beforeSelectionChange(int originalIdx, T selection) {

	// WARN, (JAF), 20080523, una vez se elimine #onSelectionChange este
	// método debería devolver el objeto pasado como parámetro.
	return this.onSelectionChange(originalIdx, selection);
    }

    /**
     * Manejador para los cambios de los elementos seleccionados. Se ejecuta antes de gestionar el cambio de los
     * elementos seleccionados.
     * <p>
     * Los mismos principios aplicables a {@link #beforeSelectionChange(int, Object)} son válidos para este método.
     * 
     * @param originalIdxs
     *            los índices de los elementos seleccionados relativos a la <em>master event list</em>.
     * @param selection
     *            las entidades seleccionadas.
     * @return las entidades seleccionadas modificadas (o no) por este manejador.
     * 
     * @see #beforeSelectionChange(int, Object)
     */
    protected Object[] beforeSelectionChange(int[] originalIdxs, Object[] selection) {

	// (JAF), 20090721, ha sido necesario cambiar la signatura de "T[]
	// selection" a "Object[] selection" para evitar errores en tiempo de
	// ejecución con los castings

	return selection;
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
     * Redefine {@link AbstractBbTableMasterForm#createFormControl()} del siguiente modo:
     * <ul>
     * <li>Invoca al método de la clase padre.
     * <li>Da la oportunidad a las clases hijas de modificar el control creado ( {@link #onCreateFormControl()}).
     * <li>Excluye el formulario detalle.
     * <li>Actualiza los controles en función del estado.
     * </ul>
     * 
     * @return el componente con el formulario maestro excluyendo el formulario detalle.
     */
    @Override
    protected final JComponent createFormControl() {

	// Obtener el control del formulario maestro.
	super.createFormControl();

	// El formulario maestro consta de una tabla y botones.
	final JPanel panel = new JPanel(new BorderLayout());
	panel.add(new JScrollPane(this.getMasterTable()), BorderLayout.CENTER);
	panel.add(this.createButtonBar(), BorderLayout.SOUTH);
	// panel.setBorder(BorderFactory.createTitledBorder(//
	// BorderFactory.createEtchedBorder()));

	// Dar la oportunidad a la clase hija de modificar el control.
	this.onCreateFormControl(panel);

	// Actualizar los controles en función del estado.
	super.updateControlsForState();

	return panel;
    }

    /**
     * Crea un <em>list selection listener</em> para la tabla maestra de tipo
     * {@link AbstractBbTableMasterForm.ListSelectionHandler}.
     * 
     * @return el <em>listener</em>.
     */
    @Override
    protected ListSelectionListener createSelectionHandler() {

	return new ListSelectionHandler();
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
     * 	    .cargarCargosAcademicosPorDepartamento(departamento);
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
     * Obtiene un <code>ListSelectionListener</code> que extiende {@link AbstractBbTableMasterForm.ListSelectionHandler}
     * añadiéndole la capacidad de invocar a {@link #onSelectionChange(int, Object)} sobre este formulario, y a
     * {@link AbstractBbChildForm#onSelectionChange(int, Object)} sobre cada uno de los formulario hijos, antes de
     * proceder con el comportamiento heredado, cada vez que se produzca una selección simple.
     * 
     * @return el <em>listener</em>.
     * 
     * @see #onSelectionChange(int, Object)
     * @see AbstractBbChildForm#onSelectionChange(int, Object)
     */
    protected class ListSelectionHandler extends FilterModelAwareListSelectionHandler<T> {

	/**
	 * Construye el <em>selection handler</em>.
	 */
	public ListSelectionHandler() {

	    super(AbstractBb2TableMasterForm.this, AbstractBb2TableMasterForm.this.getDetailForm(),
		    AbstractBb2TableMasterForm.this.getMasterTable());
	}

	/**
	 * Invoca a {@link AbstractBbChildForm#onSelectionChange(int[], Object[])} en cada formulario hijo.
	 * 
	 * @param originalIdxs
	 *            los índices de los elementos seleccionados relativos al modelo original.
	 * @param filteredIdxs
	 *            los índices de los elementos seleccionados relativos al modelo de filtrado.
	 * @param selection
	 *            las entidades seleccionadas.
	 * 
	 * @see FilterModelAwareListSelectionHandler#onMultiSelection(int[], int[], Object[])
	 */
	@Override
	protected void onMultiSelection(int[] originalIdxs, int[] filteredIdxs, Object[] selection) {

	    // Shortcut para acceder al formulario maestro
	    final AbstractBb2TableMasterForm<T> thisForm = AbstractBb2TableMasterForm.this;

	    // Gestión add-hoc PREVIA al cambio de los elementos seleccionados por parte del formulario maestro
	    selection = thisForm.beforeSelectionChange(originalIdxs, selection);

	    // Gestión general del cambio del elemento seleccionado
	    super.onMultiSelection(originalIdxs, filteredIdxs, selection);

	    // Gestión ad hoc del cambio del elemento seleccionado por parte de los formularios hijos
	    for (final AbstractBbChildForm<T> childForm : thisForm.getDetailForms()) {
		childForm.onSelectionChange(originalIdxs, selection);
	    }

	    // Gestión ad hoc POSTERIOR al cambio del elemento seleccionado por parte del formulario maestro
	    thisForm.afterSelectionChange(originalIdxs, selection);
	}

	/**
	 * Notifica a los formularios hijos de que no hay ninguna entidad seleccionada siempre y cuando el usuario lo
	 * haya confirmado.
	 * 
	 * @see org.springframework.richclient.form.AbstractMasterForm.ListSelectionHandler#onNoSelection()
	 */
	@Override
	protected void onNoSelection() {

	    // Shortcut para acceder al formulario maestro
	    final AbstractBb2TableMasterForm<T> thisForm = AbstractBb2TableMasterForm.this;

	    // Realizar la gestión general de la deselección y comprobar si el usuario la ha confirmado.
	    super.onNoSelection();

	    // El índice del elemento seleccionado después de la confirmación del usuario. Determina si se ha de
	    // proceder o no con el cambio.
	    final int currentIndex = this.getDetailForm().getSelectedIndex();

	    final Boolean proceed = (currentIndex == -1);
	    if (proceed) {
		// Gestión add-hoc de la deselección.
		for (final AbstractBbChildForm<T> childForm : thisForm.getDetailForms()) {
		    childForm.onNoSelection();
		}
	    }
	}

	/**
	 * Invoca a {@link #onSelectionChange(int, Object)} sobre el formulario maestro y
	 * {@link AbstractBbChildForm#onSelectionChange(int, Object)} sobre los formularios hijos cada vez que se
	 * produce una selección simple. Finalmente invoca a
	 * {@link AbstractBbTableMasterForm.ListSelectionHandler#onSingleSelection(int)} .
	 * 
	 * @param originalIdx
	 *            el índice del elemento seleccionado relativo al modelo original.
	 * @param filteredIdx
	 *            el índice del elemento seleccionado relativo al modelo de filtrado.
	 * @param selection
	 *            la entidad seleccionada.
	 * 
	 * @see FilterModelAwareListSelectionHandler#onSingleSelection(int, int, Object)
	 */
	@Override
	protected void onSingleSelection(int originalIdx, int filteredIdx, T selection) {

	    // Shortcut para acceder al formulario maestro
	    final AbstractBb2TableMasterForm<T> thisForm = AbstractBb2TableMasterForm.this;

	    // Gestión ad hoc PREVIA al cambio del elemento seleccionado por parte del formulario maestro y formularios
	    // hijos
	    final T newSelection = thisForm.beforeSelectionChange(originalIdx, selection);
	    for (final AbstractBbChildForm<T> childForm : thisForm.getDetailForms()) {
		childForm.beforeSelectionChange(originalIdx, newSelection);
	    }

	    // Gestión general del cambio del elemento seleccionado
	    super.onSingleSelection(originalIdx, filteredIdx, newSelection);

	    // Gestión ad hoc POSTERIOR al cambio del elemento seleccionado por parte del formulario maestro y
	    // formularios hijos
	    thisForm.afterSelectionChange(originalIdx, newSelection);
	    for (final AbstractBbChildForm<T> childForm : thisForm.getDetailForms()) {
		childForm.afterSelectionChange(originalIdx, newSelection);
	    }
	}
    }
}
