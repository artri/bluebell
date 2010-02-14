/*
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell Rich Client.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
