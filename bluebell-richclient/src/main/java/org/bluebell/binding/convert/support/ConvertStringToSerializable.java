package org.bluebell.binding.convert.support;

import java.io.Serializable;

import org.springframework.binding.convert.ConversionContext;
import org.springframework.binding.convert.support.AbstractFormattingConverter;
import org.springframework.binding.format.FormatterFactory;
import org.springframework.binding.format.support.SimpleFormatterFactory;

/**
 * Clase de utilidad para convertir un String a Serializable.
 * 
 * Necesario para binding de componentes Spring RichClient.
 * 
 *@author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ConvertStringToSerializable extends AbstractFormattingConverter {
    /**
     * Booleano para permitir vacios.
     */
    private final boolean allowEmpty;

    /**
     * Constructor por defecto.
     */
    public ConvertStringToSerializable() {

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
    protected ConvertStringToSerializable(final FormatterFactory formatterLocator, final boolean allowEmpty) {

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

        return new Class[] { String.class };
    }

    /**
     * Clase destino.
     * 
     * @return El tipo de la clase destino.
     */
    @SuppressWarnings("unchecked")
    public Class[] getTargetClasses() {

        return new Class[] { Serializable.class };
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
