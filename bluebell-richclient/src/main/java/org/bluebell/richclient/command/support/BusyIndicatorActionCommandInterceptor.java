/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
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

package org.bluebell.richclient.command.support;

import java.awt.Container;

import javax.swing.JComponent;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.ActionCommandInterceptor;
import org.springframework.richclient.command.support.ShowPageCommand;
import org.springframework.richclient.progress.BusyIndicator;

/**
 * Interceptor que muestra un indicador de ocupación mientras dure la ejecución del comando.
 * <p>
 * También es un <em>bean post processor</em> que se añade a si mismo a los comandos de tipo
 * <code>ShowPageCommand</code>.
 * <p>
 * <b>Importante</b>: los <em>command interceptor</em> sólo llevan a cabo su post-intercepción si el comando se ejecuta
 * correctamente, ante excepciones nunca se libera el indicador. Explicaciones complementarias en
 * {@link ClearBusyIndicatorExceptionHandlerDelegate}.
 * 
 *@author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class BusyIndicatorActionCommandInterceptor implements ActionCommandInterceptor, BeanPostProcessor {

    /**
     * La instancia de este interceptor.
     */
    private static final ActionCommandInterceptor INSTANCE = new BusyIndicatorActionCommandInterceptor();

    /**
     * El componente en el que mostrar el indicador.
     * <p>
     * Si es nulo se utiliza la ventana activa.
     */
    private JComponent targetComponent;

    /**
     * Constructor privado ya que está clase implementa un <em>singleton</em>.
     */
    private BusyIndicatorActionCommandInterceptor() {

        super();
    }

    /**
     * Construye el interceptor a partir del componente en el que se va a mostrar el indicador.
     * 
     * @param targetComponent
     *            el componente.
     */
    private BusyIndicatorActionCommandInterceptor(JComponent targetComponent) {

        this();
        this.setTargetComponent(targetComponent);
    }

    /**
     * Obtiene el componente en el que mostrar el indicador.
     * 
     * @return el componente.
     */
    public JComponent getTargetComponent() {

        return this.targetComponent;
    }

    /**
     * {@inheritDoc}
     */
    public void postExecution(ActionCommand command) {

        if (this.getTargetComponent() != null) {
            BusyIndicator.clearAt(this.getTargetComponent());
        } else {
            BusyIndicatorActionCommandInterceptor.clearIndicator();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        // throws BeansException {

        // TODO, JAF, 20090413, quizás fuese preferible utilizar un
        // CommandRegistryListener ya que con un bean post-processor no es
        // posible interceptar por ejemplo ShowPageMenu, ni tampoco utilizar la
        // instancia Singleton

        if ((bean != null) && (bean instanceof ShowPageCommand)) {
            final ShowPageCommand command = (ShowPageCommand) bean;

            command.addCommandInterceptor(BusyIndicatorActionCommandInterceptor.getInstance());
        }

        return bean;
    }

    /**
     * {@inheritDoc}
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName) {

        // throws BeansException {

        // Nothing to do.
        return bean;
    }

    /**
     * {@inheritDoc}
     */
    public boolean preExecution(ActionCommand command) {

        if (this.getTargetComponent() != null) {
            BusyIndicator.showAt(this.getTargetComponent());
        } else {
            BusyIndicatorActionCommandInterceptor.showIndicator();
        }

        return Boolean.TRUE;
    }

    /**
     * Establece el componente en el que mostrar el indicador.
     * 
     * @param targetComponent
     *            el componente.
     */
    public void setTargetComponent(JComponent targetComponent) {

        this.targetComponent = targetComponent;
    }

    /**
     * Oculta el indicador de ocupación de la ventana activa.
     */
    public static void clearIndicator() {

        BusyIndicator.clearAt(BusyIndicatorActionCommandInterceptor.getWindowControl());
    }

    /**
     * Obtiene la instancia de este interceptor.
     * 
     * @return la instancia.
     */
    public static ActionCommandInterceptor getInstance() {

        return BusyIndicatorActionCommandInterceptor.INSTANCE;
    }

    /**
     * Crea una instancia de este interceptor a partir del componente en el que se va a mostrar el indicador.
     * <p>
     * A diferencia de {@link #getInstance()} no se trata de un <em>singleton</em>.
     * <p>
     * En el caso de utilizar este método debería de asegurarse que después de ejecutar el comando se limpie el
     * indicador. Por ejemplo:
     * 
     * <pre>
     * try {
     *     longRunningCommand.execute();
     * } catch (Throwable t) {
     *     // handle exception
     * } finally {
     *     BusyIndicator.clearAt(control);
     * }
     * </pre>
     * 
     * @param targetComponent
     *            el componente en el que se va a mostrar el indicador.
     * 
     * @return la instancia.
     */
    public static ActionCommandInterceptor getInstance(JComponent targetComponent) {

        return new BusyIndicatorActionCommandInterceptor(targetComponent);
    }

    /**
     * Muestra el indicador de ocupación en la ventana activa.
     */
    public static void showIndicator() {

        BusyIndicator.showAt(//
                BusyIndicatorActionCommandInterceptor.getWindowControl());

    }

    /**
     * Obtiene el control de la ventana activa.
     * 
     * @return el control.
     */
    private static Container getWindowControl() {

        // (JAF), 20090711, es preferible retornar el control de la página, para
        // permitir por ejemplo abrir una nueva ventana mientras se espera.
        final ApplicationWindow window = Application.instance().getActiveWindow();
        if (window != null) {
            return window.getPage().getControl();
        }

        return null;
        // return Application.instance().getActiveWindow().getControl();
    }
}
