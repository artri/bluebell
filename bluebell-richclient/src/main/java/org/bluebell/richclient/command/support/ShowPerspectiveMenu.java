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

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.bluebell.richclient.exceptionhandling.BbApplicationException;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.PageDescriptorRegistry;
import org.springframework.richclient.application.ViewDescriptor;
import org.springframework.richclient.application.ViewDescriptorRegistry;
import org.springframework.richclient.application.config.ApplicationWindowAware;
import org.springframework.richclient.application.support.MultiViewPageDescriptor;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.config.CommandConfigurer;

/**
 * Menú con dos niveles, en el primero se encuentran las páginas y en el segundo sus vistas.
 * <p>
 * Al pinchar sobre una página se despliegan sus vistas y al pinchar sobre una vista se carga su contenido.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ShowPerspectiveMenu extends CommandGroup implements ApplicationWindowAware {

    /**
     * The error code for this command.
     */
    public static final String ERROR_CODE = ShowPerspectiveMenu.ID + "Exception";

    /**
     * El identificador del menú.
     */
    public static final String ID = "showPerspectiveMenu";

    /**
     * La ventana en la que se renderiza el menú.
     */
    private ApplicationWindow window;

    /**
     * Crea un nuevo menú de tipo {@code ShowCustomViewMenu} con identificador {@value #ID}.
     */
    public ShowPerspectiveMenu() {

        this(ShowPerspectiveMenu.ID);
    }

    /**
     * Crea un nuevo menú de tipo {@code ShowCustomViewMenu} con identificador dado.
     * 
     * @param id
     *            el identificador del menú.
     */
    public ShowPerspectiveMenu(String id) {

        super(id);
    }

    /**
     * Crea los comandos que conforman este menú una vez fijadas las dependencias.
     */
    @Override
    public void afterPropertiesSet() {

        super.afterPropertiesSet();
        this.populate();
    }

    /**
     * {@inheritDoc}
     */
    public void setApplicationWindow(ApplicationWindow window) {

        this.window = window;
    }

    /**
     * Crea un menú para mostrar todas las vistas de una página.
     * 
     * @param pageDescriptor
     *            el descriptor de la página.
     * @return el menú.
     */
    private CommandGroup createShowViewsCommand(MultiViewPageDescriptor pageDescriptor) {

        // Los servicios "ViewDescriptorRegistry" y "CommandConfigurer".
        final ViewDescriptorRegistry viewDescriptorRegistry = (ViewDescriptorRegistry) ApplicationServicesLocator
                .services().getService(ViewDescriptorRegistry.class);
        final CommandConfigurer commandConfigurer = (CommandConfigurer) ApplicationServicesLocator.services()
                .getService(CommandConfigurer.class);

        // El grupo de comandos para las vistas de esta página.
        final CommandGroup commandGroup = new CommandGroup(pageDescriptor.getId());

        // Añadir los comandos al command group
        CollectionUtils.forAllDo(pageDescriptor.getViewDescriptors(), new Closure() {
            public void execute(Object viewDescriptorId) {

                // El descriptor de la vista.
                final ViewDescriptor viewDescriptor = viewDescriptorRegistry//
                        .getViewDescriptor((String) viewDescriptorId);

                if (viewDescriptor == null) {
                    throw new BbApplicationException("No view descriptor found with name " + viewDescriptorId,
                            ShowPerspectiveMenu.ERROR_CODE);
                }

                // TODO, (JAF), 20080417, hacer que si la vista no está
                // en la página añadirla
                commandGroup.add(viewDescriptor.createShowViewCommand(ShowPerspectiveMenu.this.window));
            }
        });

        // Configurar el comando
        commandConfigurer.configure(commandGroup);

        return commandGroup;
    }

    /**
     * Crea el menú.
     */
    private void populate() {

        // El servicio "PageDescriptorRegistry"
        final PageDescriptorRegistry pageDescriptorRegistry = (PageDescriptorRegistry) ApplicationServicesLocator
                .services().getService(PageDescriptorRegistry.class);

        // Iterar sobre los descriptores de página y crear los menús
        for (final PageDescriptor pageDescriptor : pageDescriptorRegistry.getPageDescriptors()) {
            if (pageDescriptor instanceof MultiViewPageDescriptor) {
                // Menú con todas las vistas que componen la página.
                this.addInternal(this.createShowViewsCommand(//
                        (MultiViewPageDescriptor) pageDescriptor));
            } else {
                // Opción para mostrar la página.
                this.addInternal(pageDescriptor.createShowPageCommand(this.window));
            }
        }
    }
}
