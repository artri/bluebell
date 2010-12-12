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

import org.bluebell.richclient.application.ApplicationPageException;
import org.springframework.richclient.exceptionhandling.delegation.ExceptionHandlerDelegate;

/**
 * Manejador de errores que limpia el indicador de ocupación.
 * <p>
 * Se hace necesario ya que <code>ActionCommand</code> no ejecuta sus <em>post-interceptors</em> si la ejecución del
 * comando ha sido fallida. En consecuencia nunca se limpiará el interceptor de ocupación si se usa
 * <code>BusyIndicatorActionCommandInterceptor</code>.
 * <p>
 * Este es el código de implicado:
 * 
 * <pre>
 * public final void execute() {
 * 
 *     if (onPreExecute()) {
 *         doExecuteCommand();
 *         onPostExecute();
 *     }
 *     parameters.clear();
 * }
 * </pre>
 * 
 * Y esta es la sugerencia de mejora:
 * 
 * <pre>
 * public final void execute() {
 * 
 *     if (onPreExecute()) {
 * 
 *         try {
 *             doExecuteCommand();
 *         } catch (Exception e) {
 *             // Relaunch de exception
 *         } finally {
 *             onPostExecute();
 *         }
 *     }
 *     parameters.clear();
 * }
 * </pre>
 * <p>
 * Por tanto esta implementación limpia el interceptor en {@link #hasAppropriateHandler(Throwable)} y devuelve siempre
 * <code>false</code> en {@link #uncaughtException(Thread, Throwable)}.
 * 
 * @see BusyIndicatorActionCommandInterceptor
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ClearBusyIndicatorExceptionHandlerDelegate implements ExceptionHandlerDelegate {

    /**
     * {@inheritDoc}
     */
    public boolean hasAppropriateHandler(Throwable thrownTrowable) {

        // TODO, (JAF), 20090913, if page creation could not be accomplished this exception handler tries to create it
        // again due to window.getControl() invocation. This dependency should be removed
        if (!(thrownTrowable instanceof ApplicationPageException)) {
            BusyIndicatorActionCommandInterceptor.clearIndicator();
        }

        return Boolean.FALSE;
    }

    /**
     * {@inheritDoc}
     */
    public void uncaughtException(Thread t, Throwable e) {

        // Nothing to do.
    }
}