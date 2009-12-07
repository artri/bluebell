package org.bluebell.richclient.form.binding.swing.date;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDialog;

import net.sf.nachocalendar.components.DateField;

import org.springframework.richclient.form.binding.swing.date.NachoCalendarDateFieldBinder;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Modifica al <em>binder</em> incluido con Spring RCP para que los calendarios se muestren en la parte superior del
 * <code>DateField</code> y no en la inferior ya que en este caso en muchas ocasiones aparecería fuera del área hábil de
 * la pantalla.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbNachoCalendarDateFieldBinder extends NachoCalendarDateFieldBinder {

    /**
     * Redefine {@link NachoCalendarDateFieldBinder#createControl(Map)} para que el calendario se muestre sobre el
     * <em>input</em> y no por debajo de él ya que eso hace que en ocasiones no se visualice.
     * 
     * @param context
     *            parámetros en forma de contexto.
     * @return el control.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected JComponent createControl(Map context) {

	final int preferredHeight = this.getComponentFactory().createComboBox().getPreferredSize().height;

	final RepositionnableDataField dateField = new RepositionnableDataField();
	dateField.setPreferredHeight(preferredHeight);

	return dateField;
    }

    /**
     * Obtiene un campo de una clase por reflectividad.
     * <p>
     * Es una copia de {@link ReflectionUtils#findField(Class, String, Class)} pero sin el tercer parámetro.
     * 
     * @param clazz
     *            la clase de la que obtener un campo.
     * @param name
     *            el nombre del campo a obtener.
     * @return el campo buscado.
     */
    private static Field findField(final Class<? extends Object> clazz, final String name) {

	Assert.notNull(clazz, "The 'class to introspect' " + "supplied to findField() can not be null.");
	Assert.hasText(name, "The field name supplied to " + "findField() can not be empty.");
	Class<? extends Object> searchType = clazz;
	while (!Object.class.equals(searchType) && (searchType != null)) {
	    final Field[] fields = searchType.getDeclaredFields();
	    for (final Field field : fields) {
		if (name.equals(field.getName())) {
		    return field;
		}
	    }
	    searchType = searchType.getSuperclass();
	}

	return null;
    }

    /**
     * Tipo enumerado para las cuatro esquinas de un control.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static enum Corner {
	/**
	 * La esquina inferior izquierda.
	 */
	BOTTOM_LEFT(Boolean.FALSE, Boolean.TRUE),

	/**
	 * La esquina inferior derecha.
	 */
	BOTTOM_RIGHT(Boolean.FALSE, Boolean.FALSE),

	/**
	 * La esquina superior izquierda.
	 */
	TOP_LEFT(Boolean.TRUE, Boolean.TRUE),

	/**
	 * La esquina superior derecha.
	 */
	TOP_RIGHT(Boolean.TRUE, Boolean.FALSE);

	/**
	 * <em>Flag</em> indicando si la esquina está a la izquierda.
	 */
	private final Boolean onLeft;

	/**
	 * <em>Flag</em> indicando si la esquina está en la parte superior.
	 */
	private final Boolean onTop;

	/**
	 * Construye la esquina.
	 * 
	 * @param top
	 *            <em>flag</em> indicando si la esquina está en la parte superior.
	 * @param left
	 *            <em>flag</em> indicando si la esquina está en la parte izquierda.
	 */
	Corner(Boolean top, Boolean left) {

	    this.onLeft = left;
	    this.onTop = top;
	}

	/**
	 * Indica si la esquina está en la parte izquierda.
	 * 
	 * @return si la esquina está en la izquierda.
	 */
	public Boolean isOnLeft() {

	    return this.onLeft;
	}

	/**
	 * Indica si la esquina está en la parte superior.
	 * 
	 * @return si la esquina está en la parte superior.
	 */
	public Boolean isOnTop() {

	    return this.onTop;
	}
    }

    /**
     * Extensión de {@link DateField} que permite indicar en qué esquina mostrar el calendario.
     */
    protected static class RepositionnableDataField extends DateField {
	/**
	 * Es una clase <code>Serializable</code>.
	 */
	private static final long serialVersionUID = 5655109862378952309L;

	/**
	 * El nombre del campo con el <code>WindowPanel</code> de {@link DateField}.
	 */
	private static final String WINDOW_PANEL_FIELD_NAME = "windowpanel";

	/**
	 * La esquina en la que posicionar el calendario.
	 * <p>
	 * Por defecto {@value Corner#TOP_LEFT};
	 */
	private Corner corner = Corner.TOP_LEFT;

	/**
	 * El alto preferido para el calendario.
	 */
	private Integer preferredHeight;

	/**
	 * Modifica el comportamiento de {@link DateField#actionPerformed(ActionEvent)} para que recoloque el
	 * calendario.
	 * 
	 * @param event
	 *            el evento.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {

	    super.actionPerformed(event);

	    this.positionate(this.getCorner());
	}

	/**
	 * Indica en que esquina se ha de mostrar el calendario.
	 * 
	 * @return la esquina.
	 */
	public Corner getCorner() {

	    return this.corner;
	}

	/**
	 * Modifica el tamaño preferido del control para que utilice como alto el de un combobox.
	 * 
	 * @return el nuevo tamaño preferido.
	 */
	@Override
	public Dimension getPreferredSize() {

	    // FIXME dirty hack so the DateField has the correct height
	    final Dimension size = super.getPreferredSize();

	    if (this.getPreferredHeight() != null) {
		size.height = this.getPreferredHeight();
	    }

	    return size;
	}

	/**
	 * Establece la esquina en que se ha de mostrar el calendario.
	 * 
	 * @param corner
	 *            la esquina.
	 */
	public void setCorner(Corner corner) {

	    this.corner = corner;
	}

	/**
	 * Establece el alto preferido para el calendario.
	 * 
	 * @param preferredHeight
	 *            el alto preferido.
	 */
	public void setPreferredHeight(Integer preferredHeight) {

	    this.preferredHeight = preferredHeight;
	}

	/**
	 * Obtiene el ancho preferido establecido explícitamente por el usuario. Puede ser <code>null</code>.
	 * 
	 * @return el ancho preferido.
	 */
	private Integer getPreferredHeight() {

	    return this.preferredHeight;
	}

	/**
	 * Muestra el calendario en la esquina indicada.
	 * 
	 * @param corner
	 *            la esquina en la que mostrar el calendario.
	 */
	private void positionate(Corner corner) {

	    // Obtener por reflectividad el windowPanel de DateField.
	    // FIXME, (JAF), 20080528, no encontré ninguna forma elegante de
	    // obtener el windowPanel!
	    final Field field = BbNachoCalendarDateFieldBinder.findField(DateField.class,
		    BbNachoCalendarDateFieldBinder.RepositionnableDataField.WINDOW_PANEL_FIELD_NAME);

	    JDialog windowPanel = null;
	    try {
		ReflectionUtils.makeAccessible(field);
		windowPanel = (JDialog) field.get(this);
	    } catch (final IllegalArgumentException e) {
		// TODO loguear??
		return;
	    } catch (final IllegalAccessException e) {
		// TODO loguear??
		return;
	    }

	    // Recalcular las coordenadas del calendario y recolocarlo
	    double x = windowPanel.getX();
	    double y = windowPanel.getY();

	    // Esquina derecha
	    if (!corner.onLeft) {
		x += this.getWidth() - windowPanel.getWidth();
	    }

	    // Esquina superior
	    if (corner.onTop) {
		y -= this.getHeight() + windowPanel.getHeight();
	    }

	    windowPanel.setLocation((int) x, (int) y);
	}
    }
}
