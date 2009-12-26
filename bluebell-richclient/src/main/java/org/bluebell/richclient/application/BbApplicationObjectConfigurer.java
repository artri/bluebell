package org.bluebell.richclient.application;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.richclient.application.config.DefaultApplicationObjectConfigurer;
import org.springframework.util.StringUtils;

/**
 * Extiende el comportamiento de {@link DefaultApplicationObjectConfigurer} permitiendo configurar un objeto utilizando
 * múltiples nombres incluidos en una cadena separados por comas y por orden de prioridad.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbApplicationObjectConfigurer extends DefaultApplicationObjectConfigurer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Object object, String objectName) {

        // Obtener los nombres ordenados por su prioridad
        final String[] names = StringUtils.commaDelimitedListToStringArray(objectName);

        // Configurar los objetos con cada nombre en orden inverso a su
        // prioridad
        ArrayUtils.reverse(names);
        for (final String name : names) {
            super.configure(object, name);
        }
    }
}
