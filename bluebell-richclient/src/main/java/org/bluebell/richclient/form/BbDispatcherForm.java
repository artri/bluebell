package org.bluebell.richclient.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

import org.bluebell.richclient.form.util.BbDefaultFormModel;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.Form;
import org.springframework.util.Assert;

/**
 * Esta clase extiende {@link org.springframework.richclient.form.AbstractDetailForm} con el fin de reutilizar la
 * infraestructura "maestro - detalle" proporcionada por Spring RCP para el caso de múltiples relaciones
 * "parent - child".
 * <p>
 * El modelo de este formulario es de tipo {@link DispatcherFormModel}.
 * <p>
 * Cada formulario hijo tiene como <em>parent form</em> a este formulario y sus modelos son también hijos del módelo de
 * este formulario.
 * <p>
 * Algunas consideraciones a tener en cuenta son:
 * <ol>
 * <li>La gestión de los índices (del elemento siendo editado con respecto a la lista mantenida por el formulario
 * maestro) la lleva a cabo este formulario. Los hijos simplemente son notificados ante cambios de forma silenciosa.
 * <li>Las siguientes invocaciones pueden tener lugar sobre el formulario detalle o bien sobre el modelo del formulario
 * detalle. El modelo las delega en el formulario y éste las redirige pertinentemente a cada uno de los hijos:
 * <ul>
 * <li>{@link org.springframework.richclient.form.AbstractForm#reset()}
 * <li>{@link org.springframework.richclient.form.AbstractForm#revert()}
 * <li>{@link org.springframework.richclient.form.AbstractForm#isDirty()}
 * <li>
 * {@link org.springframework.richclient.form.AbstractForm#setEnabled(boolean)}
 * <li>
 * {@link org.springframework.richclient.form.AbstractForm#setFormObject(Object)}
 * </ul>
 * <li>Casos especiales:
 * <ul>
 * <li>{@link org.springframework.richclient.form.AbstractForm#commit()}: provoca un <em>commit</em> en el modelo
 * compuesto {@link DispatcherFormModel} . La operación <code>postCommit</code> se redirige al formulario maestro
 * distinguiendo entre inserciones y actualizaciones.
 * <li>
 * {@link org.springframework.richclient.form.AbstractDetailForm#setMasterList}: este método es protegido y no es
 * posible invocarlo sobre los formularios hijos directamente. No obstante se ha redefinido
 * {@link org.springframework.richclient.form.AbstractForm#setEditableFormObjects(ObservableList)} que establece la
 * lista de objetos editables también en los formularios hijos.
 * <li>
 * {@link org.springframework.richclient.form.AbstractDetailForm#addPropertyChangeListener} : No es necesario hacer
 * nada. Anteriormente se le añadía un <em>property change listener</em> inmediatamente después de crear un formulario
 * detalle, simulando el comportamiento de
 * {@link org.springframework.richclient.form.AbstractMasterForm.EditStateMonitor}.
 * </ul>
 * </ol>
 * 
 * @param <T>
 *            el tipo de las entidades editadas por el formulario maestro.
 * 
 * @see DispatcherFormModel
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbDispatcherForm<T> extends AbstractBbDetailForm<T> {

    /**
     * El nombre de la página compuesta de los formularios detalle.
     */
    private static final String FORM_ID = "compositeDetailForm";

    /**
     * Importante que sea ordenada.
     */
    private final List<AbstractBbChildForm<T>> childForms = new ArrayList<AbstractBbChildForm<T>>();

    /**
     * Crea el formulario compuesto a partir del formulario maestro y un <em>value model</em>.
     * <p>
     * Una vez invocado el constructor de la clase padre se establece un nuevo <em>form model</em> con tipo
     * {@link DispatcherFormModel} a partir del <em>value model</em>.
     * 
     * @param masterForm
     *            el formulario maestro.
     * @param valueModel
     *            <em>value model</em> a partir del cual crear el modelo de este formulario.
     */
    public BbDispatcherForm(AbstractBb2TableMasterForm<T> masterForm, ValueModel valueModel) {

        super(masterForm, BbDispatcherForm.FORM_ID, valueModel);

        // Establecer el nuevo form model.
        this.setFormModel(new DispatcherFormModel(valueModel));
    }

    /**
     * Añade un nuevo formulario hijo, de tipo {@link AbstractBbChildForm}.
     * <p>
     * Para ello procede del siguiente modo:
     * <ol>
     * <li>Actualiza el modelo del formulario hijo (
     * {@link AbstractBbChildForm#updateFormModelUsingParentForm(BbDispatcherForm)} ).
     * <li>Invoca a {@link org.springframework.richclient.form.AbstractDetailForm#addChildForm(Form)}.
     * </ol>
     * <p>
     * Provoca una excepción en caso de que el formulario hijo no sea de tipo {@link AbstractBbChildForm}.
     * 
     * @param form
     *            el formulario hijo.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void addChildForm(Form form) {

        Assert.notNull(form);
        Assert.isInstanceOf(AbstractBbChildForm.class, form);
        Assert.isNull(this.getChildForm(form.getId()), "this.getChildForm(form.getId())");

        super.addChildForm(form);

        // Añadir el formulario hijo e indicarle cual es su maestro
        final AbstractBbChildForm<T> childForm = (AbstractBbChildForm<T>) form;
        childForm.updateFormModelUsingParentForm(this);
        this.childForms.add(childForm);

    }

    /**
     * Obtiene los formularios hijos de este formulario.
     * 
     * @return una colección <em>unmodifiable</em> con los formularios hijos de este formulario.
     */
    public List<AbstractBbChildForm<T>> getChildForms() {

        return Collections.unmodifiableList(this.childForms);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirty() {

        for (final AbstractForm childForm : this.childForms) {
            if (childForm.isDirty()) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    /**
     * Elimina formulario hijo, de tipo {@link AbstractBbChildForm}.
     * <p>
     * Provoca una excepción en caso de que el formulario hijo no sea de tipo {@link AbstractBbChildForm}.
     * 
     * @param form
     *            el formulario hijo.
     */
    @Override
    public void removeChildForm(Form form) {

        Assert.isTrue(this.childForms.contains(form), "The form to remove must be a children of this form");

        // Eliminar el formulario hijo.
        super.removeChildForm(form);

        // Eliminar el formulario hijo de la colección.
        this.childForms.remove(form.getId());
    }

    /**
     * {@inheritDoc}
     * 
     * @see DispatcherFormModel#doInternalReset()
     */
    @Override
    public void reset() {

        // for (Form childForm : this.formsById.values()) { childForm.reset(); }

        // (JAF), 20080914, AbstractFormModel#reset internally executes
        // #setFormObject(null) over this form and its
        // children. To avoid redundant calls its better to make a single
        // invocation
        ((DispatcherFormModel) this.getFormModel()).doInternalReset();
    }

    /**
     * {@inheritDoc}
     * 
     * @see DispatcherFormModel#doInternalRevert()
     */
    @Override
    public void revert() {

        for (final Form childForm : this.childForms) {
            childForm.revert();
        }

        ((DispatcherFormModel) this.getFormModel()).doInternalRevert();
    }

    /**
     * {@inheritDoc}
     * 
     * @see DispatcherFormModel#doInternalSetEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {

        for (final AbstractForm childForm : this.childForms) {
            childForm.setEnabled(enabled);
        }
        ((DispatcherFormModel) this.getFormModel()).doInternalSetEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     * 
     * @see DispatcherFormModel#doInternalSetFormObject(Object)
     */
    @Override
    public void setFormObject(Object formObject) {

        for (final AbstractForm childForm : this.childForms) {
            childForm.setFormObject(formObject);
        }
    }

    /**
     * Crea el control del formulario, formado por la botonera con los comandos <code>commit</code>, <code>cancel</code>
     * y <code>revert</code>.
     * 
     * @see org.springframework.richclient.form.AbstractDetailForm #createFormControl()
     * @see #createButtonBar()
     * 
     * @return el control del formulario.
     */
    @Override
    protected JComponent createFormControl() {

        return this.createButtonBar();
    }

    /**
     * Devuelve el formulario hijo con el identificador dado y <code>null</code> si no existe.
     * 
     * @param formId
     *            el identificador del formulario.
     * 
     * @return el formulario hijo.
     */
    @Override
    protected AbstractBbChildForm<T> getChildForm(String formId) {

        for (final AbstractBbChildForm<T> childForm : this.childForms) {
            if (formId.equals(childForm.getId())) {
                return childForm;
            }
        }

        return null;
    }

    /**
     * Estable la lista de objetos editables en este formulario a continuación de sobre sus hijos.
     * <p>
     * Todos los formularios hijos comparten la lista de objetos editables; este método la establece.
     * 
     * @param editableFormObjects
     *            la lista de objetos editables.
     * 
     * @see AbstractBbChildForm#setEditableFormObjectFromDispatcherForm(ObservableList)
     */
    @Override
    protected void setEditableFormObjects(ObservableList editableFormObjects) {

        if (this.isControlCreated()) {
            for (final AbstractBbChildForm<T> childForm : this.childForms) {
                childForm.setEditableFormObjectFromDispatcherForm(editableFormObjects);
            }
        }

        super.setEditableFormObjects(editableFormObjects);
    }

    /**
     * For model used as backing form model of <code>BbDispatcherForm</code> in order to propagate invocations to child
     * form and form models.
     * <p>
     * There are well known methods whose execution is delegated from form to form model. There is no consistency at
     * this point, so invokers could call either of them.
     * <ul>
     * <li>{@link FormModel#reset()}
     * <li>{@link FormModel#revert()}
     * <li>{@link FormModel#isDirty()}
     * <li>{@link FormModel#setEnabled(boolean)}
     * <li>{@link FormModel#setFormObject(Object)}
     * </ul>
     * Other important methods are:
     * <ul>
     * <li>{@link ValidatingFormModel#validate()}
     * <li>{@link ValidatingFormModel#setValidating(boolean)}
     * <li>{@link ValidatingFormModel#isDirty()}
     * </ul>
     * <p>
     * There are the following to scenarios:
     * <ol>
     * <li>
     * Invokers call directly form model method:
     * 
     * <pre>
     *             Invoker        Form Model        Form         Child Form
     *                |               |               |               | 
     *                |--------------&gt;|               |               |
     *                |               |--------------&gt;|               |
     *                |               |               |--------------&gt;|
     *                |               |               |*              |
     *                |               |doInternalXXX()|--------------&gt;|
     *                |               |&lt;--------------|               |
     *                |               |               |               |
     * </pre>
     * 
     * </li>
     * <li>
     * Invokers call form method:
     * 
     * <pre>
     *             Invoker        Form Model        Form         Child Form
     *                |               |               |               | 
     *                |------------------------------&gt;|               |
     *                |               |               |--------------&gt;|
     *                |               |               |*              |
     *                |               |doInternalXXX()|--------------&gt;|
     *                |               |&lt;--------------|               |
     *                |               |               |               |
     * </pre>
     * 
     * 
     * 
     * &lt;p&gt; The chosen approach is to introduce &lt;code&gt;doInternalXXX&lt;/code&gt; methods responsible for
     * executing the intended form model logic (calls to &lt;code&gt;super&lt;/code&gt;). Original methods are delegated
     * to dispatcher form that invokes later &lt;code&gt;formModel#doInternalXXX&lt;/code&gt; method.
     * <p>
     * Note that child form invocations are made to form method, on this way we ensure child forms override methods are
     * deal correctly.
     * 
     * </li>
     * </ol>
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public class DispatcherFormModel extends BbDefaultFormModel {

        /**
         * El identificador del modelo.
         */
        private static final String FORM_MODEL_ID = "delegateFormModel";

        /**
         * Construye el modelo a partir del value model sobre el que construir el modelo.
         * 
         * @param valueModel
         *            el <em>value model</em> sobre el que construir el modelo.
         * 
         * @see AbstractForm(ValueModel)
         */
        public DispatcherFormModel(ValueModel valueModel) {

            super(valueModel);

            this.setId(DispatcherFormModel.FORM_MODEL_ID);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDirty() {

            // (JAF), 20080914, BbDispatcherForm.this.isDirty() executes
            // ValidatingFormModel#isDirty() for every child
            return BbDispatcherForm.this.isDirty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {

            // (JAF), 20080914, BbDispatcherForm.this.reset() executes
            // this.doInternalReset()
            BbDispatcherForm.this.reset();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void revert() {

            // (JAF), 20080914, BbDispatcherForm.this.revert() executes
            // this.doInternalRevert();
            BbDispatcherForm.this.revert();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setEnabled(boolean enabled) {

            // (JAF), 20080914, BbDispatcherForm.this.setEnabled() executes
            // this.doInternalSetEnabled(Boolean);
            BbDispatcherForm.this.setEnabled(enabled);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setFormObject(Object formObject) {

            // (JAF), 20080914, delegateForm#setFormObject() executes
            // this.doInternalSetFormObject(Object);
            BbDispatcherForm.this.setFormObject(formObject);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValidating(boolean validating) {

            // Child form models checks parent form model validating state at
            // first
            super.setValidating(validating);

            for (final FormModel childFormModel : this.getChildren()) {
                if (childFormModel instanceof ValidatingFormModel) {
                    ((ValidatingFormModel) childFormModel).setValidating(validating);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void validate() {

            // (JAF), 20090914, this call is not needed anymore: delegate form
            // model should not validate itself, it's
            // just a dispatcher to the child form models
            // super.validate();
            for (final FormModel childFormModel : this.getChildren()) {
                if (childFormModel instanceof ValidatingFormModel) {
                    ((ValidatingFormModel) childFormModel).validate();
                }
            }
        }

        /**
         * Executes {@link org.springframework.binding.form.support.AbstractFormModel#reset()} .
         */
        protected final void doInternalReset() {

            super.reset();
        }

        /**
         * Executes {@link org.springframework.binding.form.support.AbstractFormModel#revert()} .
         */
        protected final void doInternalRevert() {

            super.revert();
        }

        /**
         * Executes {@link org.springframework.binding.form.support.AbstractFormModel#setEnabled(boolean)} .
         * 
         * @param enabled
         *            the value to apply.
         */
        protected final void doInternalSetEnabled(boolean enabled) {

            super.setEnabled(enabled);
        }

        /**
         * Executes {@link org.springframework.binding.form.support.AbstractFormModel#setFormObject(Object)} .
         * 
         * @param formObject
         *            the value to apply.
         */
        protected final void doInternalSetFormObject(Object formObject) {

            super.setFormObject(formObject);
        }
    }
}
