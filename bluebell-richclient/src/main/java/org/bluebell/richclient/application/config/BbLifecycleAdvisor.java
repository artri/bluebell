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

package org.bluebell.richclient.application.config;

import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.Locale;

import org.bluebell.richclient.application.RcpMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageListener;
import org.springframework.richclient.application.config.ApplicationWindowConfigurer;
import org.springframework.richclient.application.config.DefaultApplicationLifecycleAdvisor;
import org.springframework.richclient.application.statusbar.StatusBar;
import org.springframework.util.Assert;

/**
 * Extiende el comportamiento de {@link DefaultApplicationLifecycleAdvisor} con tres finalidades:
 * <ol>
 * <li>Loguear con nivel <code>DEBUG</code> cada uno de los puntos definidos como ciclo de vida de la aplicación.
 * <li>Ejecuta el comando de <em>login</em> justo a continuación de la creación definitiva de los comandos.
 * <li>Cambiar el título de la ventana cada vez que cambia la página seleccionada.
 * </ol>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbLifecycleAdvisor extends DefaultApplicationLifecycleAdvisor implements PageListener, MessageSourceAware {

    /**
     * El alto por defecto de la ventana.
     */
    private static final Integer DEFAULT_WINDOW_HEIGHT = 600;

    /**
     * El ancho por defecto de la ventana.
     */
    // TODO, (JAF) 20091229, externalize default resolution (1024*768)
    private static final Integer DEFAULT_WINDOW_WIDTH = 800;

    /**
     * Formato de mensaje para el título de la ventana.
     */
    private static final MessageFormat FMT_WINDOW_TITLE = new MessageFormat("{0} - {1}");

    /**
     * Sufijo utilizado para la obtención de la etiqueta de la página.
     */
    private static final String LABEL_SUFIX = ".label";

    /**
     * El <em>logger</em>.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BbLifecycleAdvisor.class);

    /**
     * El nombre del comando de <em>login</em>.
     */
    // private static final String LOGIN_COMMAND_NAME = "loginCommand";

    /**
     * The statusBar for the application.
     * 
     * @return a statusBar.
     */
    private StatusBar statusBar;

    /**
     * La fuente para la obtención de mensajes.
     */
    private MessageSource messageSource;

    /**
     * Construye el <em>advisor</em> y establece la propiedad <code>windowCommandBarDefinitions</code> que por defecto
     * es {@value org.bluebell.richclient.test.exceptionhandling.RcpMain#DEFAULT_COMMANDS_CONTEXT_PATH} .
     */
    public BbLifecycleAdvisor() {

        this.setWindowCommandBarDefinitions(RcpMain.DEFAULT_COMMANDS_CONTEXT_PATH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        // Validación de parámetros
        super.afterPropertiesSet();
        Assert.notNull(this.getMessageSource());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCommandsCreated(ApplicationWindow window) {

        // Log del lifecycle aplicación
        if (BbLifecycleAdvisor.LOGGER.isDebugEnabled()) {
            BbLifecycleAdvisor.LOGGER.debug("onCommandsCreated( windowNumber=" + window.getNumber() + " )");
        }

        // Realiza el login siempre y cuando sea necesario
        this.doLoginIfNeeded(window);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPostStartup() {

        // Log del lifecycle aplicación
        if (BbLifecycleAdvisor.LOGGER.isDebugEnabled()) {
            BbLifecycleAdvisor.LOGGER.debug("onPostStartup()");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onPreWindowClose(ApplicationWindow window) {

        // Log del lifecycle aplicación
        if (BbLifecycleAdvisor.LOGGER.isDebugEnabled()) {
            BbLifecycleAdvisor.LOGGER.debug("onPreWindowClose( windowNumber=" + window.getNumber() + " )");
        }

        // Desinstalar el listener a la escucha de cambios de página
        window.removePageListener(this);

        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPreWindowOpen(ApplicationWindowConfigurer configurer) {

        // Log del lifecycle aplicación
        if (BbLifecycleAdvisor.LOGGER.isDebugEnabled()) {
            BbLifecycleAdvisor.LOGGER.debug("onPreWindowOpen( windowNumber=" + configurer.getWindow().getNumber()
                    + " )");
        }

        // Invocar al método de la clase padre
        super.onPreWindowOpen(configurer);

        // Instalar el listener a la escucha de cambios de página
        configurer.getWindow().addPageListener(this);

        // Cambiar el tamaño de la ventana
        configurer.setInitialSize(new Dimension(BbLifecycleAdvisor.DEFAULT_WINDOW_WIDTH,
                BbLifecycleAdvisor.DEFAULT_WINDOW_HEIGHT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onWindowCreated(ApplicationWindow window) {

        // Log del lifecycle aplicación
        if (BbLifecycleAdvisor.LOGGER.isDebugEnabled()) {
            BbLifecycleAdvisor.LOGGER.debug("onWindowCreated( windowNumber=" + window.getNumber() + " )");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onWindowOpened(ApplicationWindow window) {

        // Log del lifecycle aplicación
        if (BbLifecycleAdvisor.LOGGER.isDebugEnabled()) {
            BbLifecycleAdvisor.LOGGER.debug("onWindowOpened( windowNumber=" + window.getNumber() + " )");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void pageClosed(ApplicationPage page) {

        // Log del lifecycle aplicación
        if (BbLifecycleAdvisor.LOGGER.isDebugEnabled()) {
            BbLifecycleAdvisor.LOGGER.debug("pageClosed( pageId=" + page.getId() + " )");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void pageOpened(ApplicationPage page) {

        // Log del lifecycle aplicación
        if (BbLifecycleAdvisor.LOGGER.isDebugEnabled()) {
            BbLifecycleAdvisor.LOGGER.debug("pageOpened( pageId=" + page.getId() + " )");
        }

        // El título de la ventana se compone del nombre de la aplicación y del
        // nombre de la página.
        final String applicationName = this.getApplication().getName();
        final String messageCode = page.getId() + BbLifecycleAdvisor.LABEL_SUFIX;
        final String pageName = this.getMessageSource().getMessage(//
                messageCode, null, messageCode, Locale.getDefault());
        final String title = BbLifecycleAdvisor.FMT_WINDOW_TITLE.format(//
                new String[] { applicationName, pageName });

        page.getWindow().getControl().setTitle(title);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StatusBar getStatusBar() {

        if (statusBar == null) {
            this.setStatusBar(super.getStatusBar());
        }

        return this.statusBar;
    }

    /**
     * Sets the statusBar.
     * 
     * @param statusBar
     *            the statusBar to set
     */
    public void setStatusBar(StatusBar statusBar) {

        Assert.notNull(statusBar, "statusBar");

        this.statusBar = statusBar;
    }

    /**
     * {@inheritDoc}
     */
    public void setMessageSource(MessageSource messageSource) {

        this.messageSource = messageSource;

    }

    /**
     * Obtiene la fuente para la obtención de mensajes.
     * 
     * @return la fuente para la obtención de mensajes.
     */
    protected MessageSource getMessageSource() {

        return this.messageSource;
    }

    /**
     * Realiza el <em>login</em> siempre y cuando no exista un usuario previamente loguedo.
     * 
     * @param window
     *            la ventana a partir de la cual obtener el comando de <em>login</em>.
     */
    private void doLoginIfNeeded(ApplicationWindow window) {

        // (JAF), 20090720, comprobación añadida para poder crear contextos de
        // aplicación stateless con Spring RCP
        // final Boolean isAuthenticationManagerDefined =
        // Application.instance().getApplicationContext().containsBean(
        // "authenticationManager");

        // final LoginCommand loginCommand = (LoginCommand)
        // window.getCommandManager().getCommand(
        // BbLifecycleAdvisor.LOGIN_COMMAND_NAME);

        // if (loginCommand != null &&
        // SecurityContextHolder.getContext().getAuthentication() == null
        // && isAuthenticationManagerDefined) {
        // TODO, (JAF), 20090910, the login command automatic execution should
        // be only in the case the global rich
        // client application context is loaded
        // loginCommand.execute();
        // }
    }
}
