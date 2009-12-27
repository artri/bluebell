/**
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

package org.bluebell.richclient.command.support;

import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.command.ActionCommand;

/**
 * Comando especialmente indicado para tareas de depuración ya que permite modificar los controles de la aplicación y
 * visualizar los cambios sin necesidad de reiniciar.
 * <p>
 * Está basado en el siguiente <em>post</em> del foro de <b>Spring</b>: {@link http
 * ://forum.springframework.org/showthread.php?t=33511&highlight=global+command}.
 * <p>
 * La forma de trabajar es:
 * <ol>
 * <li>Arrancar la aplicación Spring-RCP desde el IDE en modo <em>debug</em> (no es necesario establecer ningún
 * <em>breakpoint</em>).
 * <li>Modificar libremente el código de <code>createControl()</code>. El IDE recompilará automáticamente la clase.
 * <li>Al ejecutar este comando, directamente se verán los cambios en la vista, sin necesidad de reiniciar el gui.
 * </ol>
 * <p>
 * Para ello es necesario crear el comando en el contexto de aplicación y referenciarlo, por ejemplo, desde la
 * <em>toolbar</em>:
 * 
 * <pre>
 * &lt;bean id=&quot;refreshCommand&quot;
 *  class=&quot;org.bluebell.richclient.test.command.RefreshViewCommand&quot; /&gt;
 * </pre>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class RefreshViewCommand extends ActionCommand {

    /**
     * El identificador del comando.
     */
    private static final String PAGE_ID = "refreshViewCommand";

    /**
     * El constructor por defecto.
     * <p>
     * El identificador del comando es {@link #PAGE_ID}.
     */
    public RefreshViewCommand() {

        super(RefreshViewCommand.PAGE_ID);
    }

    /**
     * Recarga la vista que se está mostrando actualmente.
     */
    @Override
    protected void doExecuteCommand() {

        final ApplicationPage page = Application.instance().getActiveWindow().getPage();
        final PageComponent pageComponent = page.getActiveComponent();
        // page.close();

        page.showView(pageComponent.getId());
    }
}
