package org.bluebell.binding.convert.support;

import java.io.Serializable;

import org.springframework.binding.convert.ConversionContext;
import org.springframework.binding.convert.support.AbstractFormattingConverter;
import org.springframework.binding.format.FormatterFactory;
import org.springframework.binding.format.support.SimpleFormatterFactory;

/**
 * Clase de utilidad para convertir un Serializable a String.
 * 
 * Necesario para binding de componentes Spring RichClient.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ConvertSerializableToString extends AbstractFormattingConverter {
    /**
     * Booleano para permitir vacios.
     */
    private final boolean allowEmpty;

    /**
     * Constructor por defecto.
     */
    public ConvertSerializableToString() {

        this(new SimpleFormatterFactory(), true);
    }

    /**
     * Constructor con parámetros.
     * 
     * @param formatterLocator
     *            El FormatterFactory.
     * @param allowEmpty
     *            El booleano para permitir vacíos.
     */
    protected ConvertSerializableToString(final FormatterFactory formatterLocator, final boolean allowEmpty) {

        super(formatterLocator);
        this.allowEmpty = allowEmpty;
    }

    /**
     * Clase origen.
     * 
     * @return El tipo de la clase origen.
     */
    @SuppressWarnings("unchecked")
    public Class[] getSourceClasses() {

        return new Class[] { Serializable.class };
    }

    /**
     * Clase destino.
     * 
     * @return El tipo de la clase destino.
     */
    @SuppressWarnings("unchecked")
    public Class[] getTargetClasses() {

        return new Class[] { String.class };
    }

    /**
     * Metodo de conversión.
     * 
     * @param sourceINYOURCLASS
     *            Objeto Origen.
     * @param targetClass
     *            Objeto Destino.
     * @param context
     *            El ConversionContext
     * @return Objeto convertido
     * @throws Exception
     *             Excepción provocada.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Object doConvert(final Object sourceINYOURCLASS, final Class targetClass, final ConversionContext context)
            throws Exception {

        return (!this.allowEmpty || (sourceINYOURCLASS != null)) ? sourceINYOURCLASS.toString() : null;
    }
}
