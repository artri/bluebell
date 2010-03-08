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

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Clase plantilla que facilita la creación de clases Main que requieran la carga de contextos de aplicación de Spring.
 * <p>
 * Ejemplo de uso:
 * 
 * <pre>
 *  org.bluebell.security.impl.MainClass
 *      -class org.bluebell.security.impl.MainClass
 *      -baseDir sies_gen_env
 *      -context classpath*:/file1.xml,classpath*:/file2.xml
 * </pre>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public abstract class Main {

    /**
     * El separador a emplear durante el parseo de los argumentos del <code>main</code>.
     */
    protected static final String SEPARATOR = ",";

    /**
     * El nombre del parámetro del <code>main</code> especificando el directorio base desde el que cargar la
     * configuración específica del entorno.
     */
    private static final String BASE_DIR_ARG = "-baseDir";

    /**
     * El nombre del parámetro del <code>main</code> especificando la clase a instanciar para arrancar la aplicación.
     */
    private static final String CLASS_ARG = "-class";

    /**
     * El nombre del parámetro del <code>main</code> especificando los recursos con el contexto de aplicación de Spring
     * a utilizar.
     */
    private static final String CTX_ARG = "-context";

    /**
     * El <em>logger</em>.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Crea un contexto de aplicación con los ficheros específicos del entorno además de las ubicaciones de
     * configuración espcificadas.
     * 
     * 
     * @param configLocations
     *            las ubicaciones con los ficheros de configuración de Spring.
     * @param baseDirs
     *            los directorios base con la configuración específica del entorno a emplear.
     * 
     * @return el contexto de aplicación de Spring creado.
     */
    protected ApplicationContext createApplicationContext(String[] configLocations, String[] baseDirs) {

        // Cargar el contexto de aplicación de Spring necesario
        return new ClassPathXmlApplicationContext(this.getContextConfigLocations(configLocations, baseDirs));
    }

    /**
     * Obtiene las ubicaciones de los ficheros con el contexto de aplicación de Spring.
     * <p>
     * Esta implementación devuelve un array vacío.
     * 
     * @return la ubicaciones de los ficheros con el contexto de aplicación de Spring.
     */
    protected String[] getConfigLocations() {

        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    /**
     * Obtiene las ubicaciones definitas con el contexto de aplicación de Spring que se construyen como la unión de las
     * ubicaciones especificadas más las existentes en los directorios específicos del entorno.
     * 
     * @param configLocations
     *            las ubicaciones con los ficheros de configuración de Spring.
     * @param baseDirs
     *            los directorios base con la configuración específica del entorno a emplear.
     * @return las ubicacines definitivas.
     */
    protected String[] getContextConfigLocations(String[] configLocations, String[] baseDirs) {

        // Obtener las ubicaciones del contexto de aplicación de Spring.
        final Collection<String> envLocations = new HashSet<String>();
        CollectionUtils.addAll(envLocations, configLocations);

        return envLocations.toArray(new String[envLocations.size()]);
    }

    /**
     * Obtiene los directorios base desde los que cargar la configuración específica del entorno.
     * <p>
     * Esta implementación devuelve un array con el único valor {@value EnvironmentUtils#DEFAULT_BASE_DIR}.
     * 
     * @return los directorios base.
     * 
     * @see EnvironmentUtils
     */
    protected String[] getEnvironmentBaseDirs() {

        return new String[] {};
    }

    /**
     * Arranca la aplicación conocidos los siguientes parámetros.
     * 
     * @param main
     *            la instancia de <code>Main</code> a utilizar.
     * @param args
     *            los argumentos del <code>main</code> introducidos por el usuario.
     * @param configLocations
     *            las ubicaciones con los ficheros de configuración de Spring.
     * @param baseDirs
     *            los directorios base con la configuración específica del entorno a emplear.
     */
    protected abstract void launch(Main main, String[] args, String[] configLocations, String[] baseDirs);

    /**
     * El método principal, arranca la aplicación utilizando una instancia {@link Main}.
     * <p>
     * Si se especifica el parámetro {@value #CLASS_ARG} instanciará la clase especificada con el fin de obtener el
     * <code>Main</code> más apropiada para arrancar la aplicación.
     * <p>
     * Ej.:
     * 
     * <pre>
     * Main -class org.bluebell.xxx.ui.app.MyMain
     * </pre>
     * 
     * @param args
     *            los argumentos del método <em>main</em>.
     */
    public static void main(String[] args) {

        String className = Main.class.getName();
        String[] baseDirs = null;
        String[] configLocations = null;

        // Parsear los parámetros del Main
        for (int i = 0; i < args.length - 1; ++i) {
            if (args[i].equals(Main.CLASS_ARG)) {
                className = args[i + 1];
            } else if (args[i].equals(Main.BASE_DIR_ARG)) {
                baseDirs = StringUtils.split(args[i + 1], Main.SEPARATOR);
            } else if (args[i].equals(Main.CTX_ARG)) {
                configLocations = StringUtils.split(args[i + 1], Main.SEPARATOR);
            }
        }

        // Instanciar el launcher de la aplicación
        Main main = null;
        try {
            main = (Main) Class.forName(className).newInstance();
        } catch (final ClassNotFoundException e) {
            Main.LOGGER.error(e.getMessage(), e);
        } catch (final InstantiationException e) {
            Main.LOGGER.error(e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            Main.LOGGER.error(e.getMessage(), e);
        }

        // Si no se han especificado las ubicaciones con la configuración
        // entonces utilizar las definidas programáticamente.
        if (configLocations == null) {
            configLocations = main.getConfigLocations();
        }

        // Si no se han especificado los directorios base con la configuración
        // específica del entorno entonces utilizar las definidas
        // programáticamente.
        if (baseDirs == null) {
            baseDirs = main.getEnvironmentBaseDirs();
        }

        // Ejecutar la carga de las autoridades
        main.launch(main, args, configLocations, baseDirs);
    }
}
