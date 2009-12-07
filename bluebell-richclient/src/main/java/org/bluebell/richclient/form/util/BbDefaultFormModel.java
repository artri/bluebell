package org.bluebell.richclient.form.util;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bluebell.richclient.form.builder.support.DirtyTrackingUtils;
import org.springframework.binding.MutablePropertyAccessStrategy;
import org.springframework.binding.form.FieldMetadata;
import org.springframework.binding.form.support.AbstractFormModel;
import org.springframework.binding.form.support.DefaultFormModel;
import org.springframework.binding.form.support.FormModelMediatingValueModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.util.ReflectionUtils;

/**
 * Extiende <code>DefaultFormModel</code> para resolver ciertos problemas con la implementación base:
 * <ul>
 * <li>El constructor {@link DefaultFormModel#DefaultFormModel(MutablePropertyAccessStrategy)} no prepara el
 * <em>value model</em>.
 * <li>El método {@link AbstractFormModel#add(String, ValueModel, FieldMetadata)} no permanece a la escucha de cambios
 * en la metainformación del campo.
 * <li>Después del <em>commit</em> no se limpian los campos <em>dirty</em> del <em>form model</em>.
 * </ul>
 * 
 * @see <a href="http://forum.springframework.org/showthread.php?t=53979">Bug en <code>AbstractFormModel</code>
 *      relacionado con <code>prepareValueModel</code></a>
 * 
 * @see <a href="http://forum.springframework.org/showthread.php?t=57378">Bug en <code>AbstractFormModel</code>
 *      relacionado con la <em>FieldMetadata</em></a>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbDefaultFormModel extends DefaultFormModel {

    // TODO, (JAF), 20091001, revisar el post http://forum.springframework.org/showthread.php?t=57378 a ver si han
    // solucionado el problema. Después de eso, crear un test que lo demuestre (quizás con el de TableBinding sea
    // suficiente) y mover el javadoc siguiente a un sitio mejor (no se si el test u otro clase).

    /**
     * According to <a href="http://forum.springsource.org/showthread.php?p=263013">Binding error messages</a>
     * 
     * <pre>
     *   --&gt;(1) DefaultFormModel$ValidatingFormValueModel &lt;---------------- bindingErrorMessages
     * wraps |
     *       +--&gt;(2) TypeConverter
     *      wraps |
     *            +--&gt;(3) DefaultFormModel$ValidatingFormValueModel &lt;------ formModel.getValueModel(propertyPath);
     *           wraps |      
     *                 +--&gt;(4) FormModelMediatingValueModel
     *                wraps |
     *                      +--&gt;(5) BufferedValueModel                                     +---&gt;ValueHolder
     *                     wraps |                                                         | parent    
     *                           +--&gt;(6)AbstractPropertyAccessStrategy$PropertyValueModel--+
     * </pre>
     * 
     * Mensaje de error indicando que no se puede limpiar el dirty de un formulario.
     */
    private static final MessageFormat FMT_ERROR_CLEARING = new MessageFormat(//
	    "Error clearing dirty form and value models from form model with id {0}");

    /**
     * Log para la clase {@link BbFormModelHelper}.
     */
    private static final Log LOGGER = LogFactory.getLog(BbDefaultFormModel.class);

    /**
     * Crea un <em>form model</em> especificando la estrategia para el acceso a las propiedades.
     * <p>
     * Modifica el comportamiento de {@link DefaultFormModel#DefaultFormModel(MutablePropertyAccessStrategy)}
     * añadiéndole la capacidad de preparar el <em>value model</em>.
     * 
     * @param domainObjectAccessStrategy
     *            la estrategia para el acceso a las propiedades del objeto utilizado por este formulario.
     * @param bufferChanges
     *            <em>flag</em> indicando si el <em>buffering</em> está activado (<code>true</code>).
     * 
     * @see #prepareValueModel(ValueModel)
     */
    public BbDefaultFormModel(MutablePropertyAccessStrategy domainObjectAccessStrategy, boolean bufferChanges) {

	super(domainObjectAccessStrategy, bufferChanges);

	// (JAF), 20091001, the failure has been already solved by Spring RCP guys
	// this.prepareValueModel(domainObjectAccessStrategy.getDomainObjectHolder());
    }

    /**
     * Construye el <em>form model</em> a partir de un <em>value model</em>.
     * 
     * @param valueModel
     *            el <em>value model</em> que alberga el <em>form object</em>.
     */
    public BbDefaultFormModel(ValueModel valueModel) {

	super(valueModel);
    }

    /**
     * Añade el <em>listener</em> {@link #childStateChangeHandler} a la metainformación ya que
     * <code>AbstractFormModel</code> no lo hace.
     * 
     * @param propertyName
     *            el nombre de la propiedad.
     * @param valueModel
     *            el <em>value model</em>.
     * @param metadata
     *            la metainformación.
     * @return el <em>value model</em> añadido al <em>form model</em>.
     * 
     * @see org.springframework.binding.form.support.AbstractFormModel#add(String, ValueModel)
     * @see org.springframework.binding.form.support.AbstractFormModel#add(String, ValueModel, FieldMetadata)
     */
    @Override
    public ValueModel add(String propertyName, ValueModel valueModel, FieldMetadata metadata) {

	// Si este método ha sido invocado por add(String, ValueModel)
	// con esta comprobación se evita añadir dos listeners
	if (!(valueModel instanceof FormModelMediatingValueModel)) {
	    metadata.addPropertyChangeListener(FieldMetadata.DIRTY_PROPERTY, this.childStateChangeHandler);
	}

	return super.add(propertyName, valueModel, metadata);
    }

    /**
     * Añade al comportamiento original la capacidad de limpiar los <em>dirties</em> del <em>form model</em> una vez
     * <em>commiteado</em>.
     * <p>
     * Esto no debería de ser necesaro pero se incorpora esta funcionalidad con el fin de evitar problemas derivados de
     * operaciones cuando menos "dudosas" sobre el <em>form model</em>.
     */
    @Override
    public void commit() {

	super.commit();

	this.clearDirtyFormAndValueModels();
    }

    /**
     * Cada vez que se establece un nuevo <em>form object</em> se resetea el <em>dirty</em> del modelo.
     * 
     * @param formObject
     *            el nuevo objeto.
     */
    @Override
    public void setFormObject(Object formObject) {

	// TODO, (JAF), this class should dissapear once Spring RCP guys solve related bugs
	this.clearBindingErrorMessages();

	super.setFormObject(formObject);

	this.clearDirtyFormAndValueModels();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

	return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", this.getId()).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void childStateChanged(PropertyChangeEvent evt) {

	super.childStateChanged(evt);

	// Información de depuración acerca del estado dirty
	DirtyTrackingUtils.handle(evt);
    }

    /**
     * Limpia los <em>dirties</em> del modelo.
     * 
     * @see <a href="http://forum.springframework.org/showthread.php?t=57378">Bug en <code>DefaultFormModel</code>
     *      relacionado con los errores de binding</a>
     */
    private void clearBindingErrorMessages() {

	final Field bindingErrorMessagesField = ReflectionUtils.findField(DefaultFormModel.class,
		"bindingErrorMessages");
	ReflectionUtils.makeAccessible(bindingErrorMessagesField);
	try {
	    ((Map<?, ?>) bindingErrorMessagesField.get(this)).clear();
	} catch (IllegalArgumentException e) {
	    e.printStackTrace();
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	}

    }

    /**
     * Limpia los <em>dirties</em> del modelo.
     */
    private void clearDirtyFormAndValueModels() {

	// Limpiar los campos dirty
	try {
	    final Set<?> dirtyValueAndFormModels = (Set<?>) BbFormModelHelper.dirtyValueAndFormModelsField.get(this);
	    dirtyValueAndFormModels.clear();
	} catch (final IllegalArgumentException e) {
	    BbDefaultFormModel.LOGGER.error(BbDefaultFormModel.FMT_ERROR_CLEARING.format(//
		    new String[] { this.getId() }), e);
	} catch (final IllegalAccessException e) {
	    BbDefaultFormModel.LOGGER.error(BbDefaultFormModel.FMT_ERROR_CLEARING.format(//
		    new String[] { this.getId() }), e);
	}
    }
}