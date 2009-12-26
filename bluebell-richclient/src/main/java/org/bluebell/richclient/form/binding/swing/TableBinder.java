package org.bluebell.richclient.form.binding.swing;

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.binding.Binding;
import org.springframework.richclient.form.binding.support.AbstractBinder;
import org.springframework.util.Assert;

/**
 * <em>Binder</em> que permite vincular colecciones con tablas a través de un <code>TableBinding</code>.
 * <p>
 * Para utilizar este componente se puede proceder de dos modos:
 * <ul>
 * <li>Utilizando la <em>binding factory</em> de Spring RCP para, por ejemplo, asociar este binder a una propiedad.
 * 
 * <pre>
 *     &lt;bean id=&quot;aceTableBinder&quot; class=&quot;org.bluebell.richclient.test.binding.TableBinder&quot;&gt;
 *         &lt;property name=&quot;columnPropertyNames&quot;&gt;
 *             &lt;list&gt;
 *                 &lt;value&gt;string&lt;/value&gt;
 *                 &lt;value&gt;number&lt;/value&gt;
 *             &lt;/list&gt;
 *         &lt;/property&gt;
 *         &lt;property name=&quot;dialogBackingForm&quot;&gt;
 *             &lt;bean class=&quot;es.uniovi.uosec.ui.form.InnerBeanForm&quot; /&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * <li>Instanciando {@link TableBinding} y configurándolo por código:
 * 
 * <pre>
 * TableBinding binding = new TableBinding(formModel, &quot;propertyName&quot;);
 * binding.setColumnPropertyNames(columnPropertyNames);
 * binding.setDialogBackingForm(form);
 * bindingFactory.interceptBinding(binding); // IMPRESCINDIBLE
 * </pre>
 * 
 * </ul>
 * 
 * @see TableBinding
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TableBinder extends AbstractBinder implements InitializingBean {

    /**
     * Los nombres de las columnas de la tabla creada por este <em>binder</em>.
     */
    private String[] columnPropertyNames;

    /**
     * El formulario a utilizar en el diálogo que permite añadir o editar una entidad de la tabla en un
     * <code>TableBinding</code>.
     */
    private Form dialogBackingForm;

    /**
     * El tamaño de alto del diálogo.
     */
    // TODO, (JAF), 20090513, esta variable debería denominarse dialogHeight.
    private Integer heightDialog;

    /**
     * El tamaño de ancho del diálogo.
     */
    // TODO, (JAF), 20090513, esta variable debería denominarse dialogWidth.
    private Integer widthDialog;

    /**
     * El constructor por defecto del <em>binder</em>.
     */
    public TableBinder() {

        this(null, new String[] {});
    }

    /**
     * Construye el <em>binder</em> a partir de la clase requerida y las claves soportadas.
     * 
     * @param requiredSourceClass
     *            la clase requerida.
     * @param supportedContextKeys
     *            las clases soportadas.
     */
    public TableBinder(Class<Object> requiredSourceClass, String[] supportedContextKeys) {

        super(requiredSourceClass, supportedContextKeys);
    }

    /**
     * Comprueba que se hayan establecido los nombres de las columnas.
     * 
     * @throws Exception
     *             en caso de error.
     */
    public void afterPropertiesSet() throws Exception {

        Assert.notEmpty(this.getColumnPropertyNames());
        Assert.notNull(this.getDialogBackingForm());
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
     * Obtiene el formulario a utilizar en el diálogo que permite añadir o editar una entidad de la tabla en un
     * <code>TableBinding</code>.
     * 
     * @return el formulario.
     */
    public Form getDialogBackingForm() {

        return this.dialogBackingForm;
    }

    /**
     * Obtiene el alto del diálogo.
     * 
     * @return el alto del diálogo.
     */
    public Integer getHeightDialog() {

        return this.heightDialog;
    }

    /**
     * Obtiene el ancho del diálogo.
     * 
     * @return el ancho del diálogo.
     */
    public Integer getWidthDialog() {

        return this.widthDialog;
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
     * Establece el formulario a utilizar en el diálogo que permite añadir o editar una entidad de la tabla en un
     * <code>TableBinding</code>.
     * 
     * @param dialogBackingForm
     *            el formulario.
     */
    public void setDialogBackingForm(Form dialogBackingForm) {

        this.dialogBackingForm = dialogBackingForm;
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
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected JComponent createControl(Map context) {

        return this.getComponentFactory().createTable();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Binding doBind(JComponent control, FormModel formModel, String formPropertyPath, Map context) {

        final TableBinding binding = new TableBinding((JTable) control, formModel, formPropertyPath);
        binding.setColumnPropertyNames(this.getColumnPropertyNames());
        binding.setDialogBackingForm(this.getDialogBackingForm());

        if (this.getWidthDialog() != null) {
            binding.setWidthDialog(this.getWidthDialog());
        }

        if (this.getHeightDialog() != null) {
            binding.setHeightDialog(this.getHeightDialog());
        }

        return binding;
    }
}
