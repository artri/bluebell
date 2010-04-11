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
 * Generic main implementation capable of launching Bluebell RCP clients.
 * <p>
 * Includes a well known set of default application context locations (AKA Bluebell CoC) consisting on:
 * <dl>
 * <dt>{@value #DEFAULT_APP_CONTEXT_PATH}
 * <dd>The default application context path.
 * <dt>{@value #DEFAULT_COMMANDS_CONTEXT_PATH}
 * <dd>The default commands context path.
 * <dt>{@value #DEFAULT_STARTUP_CONTEXT_PATH}
 * <dd>The default startup context path.
 * <dt>{@value #MAIN_APP_CONTEXT_PATH}
 * <dd>The prior application context path.
 * </dl>
 * 
 * <p>
 * <b>Note</b> quoted Spring Documentation paragraph:
 * 
 * <pre>
 * "Please note that "classpath*:" when combined with Ant-style patterns will only work reliably with at least one
 * root directory before the pattern starts, unless the actual target files reside in the file system"
 * </pre>
 * 
 * @see ApplicationLauncher
 * @see <a
 *      href="http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/resources.html#resources-wildcards-in-path-other-stuff">Resources
 *      wildcards</a>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class RcpMain extends Main {

    /**
     * The default application context path.
     */
    public static final String DEFAULT_APP_CONTEXT_PATH = "classpath*:/META-INF/spring/root/**/*-context.xml";

    /**
     * The default commands context path.
     */
    public static final String DEFAULT_COMMANDS_CONTEXT_PATH = "classpath*:/META-INF/spring/commands/**/*-context.xml";

    /**
     * La ubicación por defecto del contexto de arranque de la aplicación.
     */
    public static final String DEFAULT_STARTUP_CONTEXT_PATH = "classpath*:/META-INF/spring/startup/**/*-context.xml";

    /**
     * The first application context file to be loaded (order is important due to bean dependence hierarchy). This
     * avoids "depend-on" abuse.
     */
    public static final String MAIN_APP_CONTEXT_PATH = "classpath*:/META-INF/spring/root/bluebell-application-context.xml";

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

        // main must be an instance of RcpMain
        Assert.isInstanceOf(RcpMain.class, main);
        final RcpMain rcpMain = (RcpMain) main;

        // Launch applicaiton definitely
        rcpMain.launch(configLocations, baseDirs);
    }

    /**
     * Gets the application context config locations.
     * <p>
     * Default value is {@value #DEFAULT_APP_CONTEXT_PATH}.
     * 
     * @return the config locations.
     */
    @Override
    protected String[] getConfigLocations() {

        return new String[] { RcpMain.MAIN_APP_CONTEXT_PATH, RcpMain.DEFAULT_APP_CONTEXT_PATH };
    }

    /**
     * Gets the startup config location.
     * <p>
     * Default value is {@value #DEFAULT_STARTUP_CONTEXT_PATH}.
     * 
     * @return the startup locations.
     */
    protected String getStartupLocation() {

        return RcpMain.DEFAULT_STARTUP_CONTEXT_PATH;
    }

    /**
     * Launch application.
     * 
     * @param configLocations
     *            application context config locations.
     * @param baseDirs
     *            base dirs for loading environment specific application context.
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
     * Handles any kind of exception employing the registered exception handler.
     * 
     * @param t
     *            the throwable to be handled.
     */
    public static void handleException(Throwable t) {

        Application.instance().getLifecycleAdvisor().getRegisterableExceptionHandler()//
                .uncaughtException(Thread.currentThread(), t);
    }

    /**
     * Handles a failure during application startup.
     * <p>
     * <b>Note</b> this method does not employ registerable exception handler since this may be not loaded.
     * 
     * @param e
     *            the exception.
     */
    private static void handleLaunchFailure(Throwable e) {

        // TODO, (JAF), 20080610, quizás haya que tratar esta excepción de una forma diferente.
        RcpMain.LOGGER.info("Rcp Application will exit");
        RcpMain.LOGGER.error(e.getMessage(), e);

        System.exit(1);
    }
}
