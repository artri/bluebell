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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationLauncher;
import org.springframework.util.Assert;

/**
 * Clase <code>main</code> genérica para el arranque de aplicaciones Spring RCP.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class RcpMain extends Main {

    /**
     * La ubicación por defecto del contexto de aplicación.
     */
    public static final String DEFAULT_APPLICATION_CONTEXT_PATH = //
    "classpath*:/**/richclient/**/richclient-*-context.xml";

    /**
     * La ubicación por defecto de los comandos del contexto de aplicación.
     */
    public static final String DEFAULT_COMMANDS_CONTEXT_PATH = "classpath*:/**/richclient/**/commands-context.xml";

    /**
     * La ubicación por defecto del contexto de arranque de la aplicación.
     */
    public static final String DEFAULT_STARTUP_CONTEXT_PATH = "classpath*:/**/richclient/**/startup-context.xml";

    /**
     * The first application context file to be loaded (order is important due to bean dependence hierarchy). This
     * avoids "depend-on" abuse.
     */
    public static final String MAIN_APPLICATION_CONTEXT_PATH = //
    "classpath*:/org/bluebell/richclient/application/richclient-application-context.xml";

    /**
     * El <em>logger</em>.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RcpMain.class);

    /**
     * {@inheritDoc}
     * 
     * @see #launch(String[], String[])
     */
    @Override
    public void launch(Main main, String[] args, final String[] configLocations, final String[] baseDirs) {

        // main debe ser una instancia de RcpMain
        Assert.isInstanceOf(RcpMain.class, main);
        final RcpMain rcpMain = (RcpMain) main;

        // Lanzar definitivamente la aplicación
        rcpMain.launch(configLocations, baseDirs);
    }

    /**
     * Obtiene la ubicación del contexto de la aplicación.
     * <p>
     * Por defecto es {@value #DEFAULT_APPLICATION_CONTEXT_PATH}.
     * 
     * @return la ubicación.
     */
    @Override
    protected String[] getConfigLocations() {

        return new String[] { RcpMain.MAIN_APPLICATION_CONTEXT_PATH, RcpMain.DEFAULT_APPLICATION_CONTEXT_PATH };
    }

    /**
     * Obtiene la ubicación del contexto de arranque de la aplicación.
     * <p>
     * Por defecto es {@value #DEFAULT_STARTUP_CONTEXT_PATH}.
     * 
     * @return la ubicación.
     */
    protected String getStartupLocation() {

        return RcpMain.DEFAULT_STARTUP_CONTEXT_PATH;
    }

    /**
     * Arranca la aplicación. Para ello utilizando las ubicaciones devueltas por:
     * <ul>
     * <li>{@link #getConfigLocations()}
     * <li>{@link #getStartupLocation()}
     * </ul>
     * 
     * @param configLocations
     *            las ubicaciones con los ficheros de configuración de Spring.
     * @param baseDirs
     *            los directorios base desde los que cargar la configuración específica del entorno.
     */
    protected void launch(String[] configLocations, String[] baseDirs) {

        RcpMain.LOGGER.info("Rcp Application starting up");

        try {
            // Para lanzar la plataforma, hay que construir por una parte el
            // contexto de arranque y por otra el contexto de aplicación
            // propiamente dicho, incluyendo básicamente páginas y vistas.

            // El ApplicationLauncher es responsable de cargar los contextos
            // (arranque y aplicación), presentar la "Splash Screen",
            // inicializar una instancia singleton de la aplicación y crear la
            // ventana de la aplicación que exhibirá la página inicial.

            new ApplicationLauncher(this.getStartupLocation(), //
                    this.getContextConfigLocations(configLocations, baseDirs));
        } catch (final Exception e) {
            RcpMain.handleLaunchFailure(e);
        }

        RcpMain.LOGGER.info("Rcp Application shutting down");
    }

    /**
     * Maneja las excepciones de tipo <code>Throwable</code> delegando en el manejador de excepciones registrado en el
     * contexto de aplicación.
     * 
     * @param t
     *            la excepción a tratar.
     */
    public static void handleException(Throwable t) {

        Application.instance().getLifecycleAdvisor().getRegisterableExceptionHandler()//
                .uncaughtException(Thread.currentThread(), t);
    }

    /**
     * Maneja los fallos en el arranque de la aplicación.
     * <p>
     * <b>Nótese</b> que este tratamiento no puede beneficiarse del tratamiento de excepciones proveido por el
     * framework, ya que muy probablemente, una vez llegados a este punto, no se haya cargado el contexto de aplicación.
     * 
     * @param e
     *            la excepción que provocó el fallo.
     */
    private static void handleLaunchFailure(Throwable e) {

        // TODO, (JAF), 20080610, quizás haya que tratar esta excepción de una
        // forma diferente.
        RcpMain.LOGGER.info("Rcp Application will exit");
        RcpMain.LOGGER.error(e.getMessage(), e);

        System.exit(1);
    }
}
