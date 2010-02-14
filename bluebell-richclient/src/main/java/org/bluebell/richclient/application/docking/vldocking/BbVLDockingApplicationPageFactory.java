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

package org.bluebell.richclient.application.docking.vldocking;

import java.util.HashSet;
import java.util.Set;

import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.PageListener;
import org.springframework.richclient.application.docking.vldocking.VLDockingApplicationPage;
import org.springframework.richclient.application.docking.vldocking.VLDockingApplicationPageFactory;

/**
 * Factoría para la creación de páginas de aplicación que extiende el comportamiento de
 * {@link VLDockingApplicationPageFactory} con el objetivo de:
 * <ul>
 * <li>Evitar que se añadan <em>listeners</em> redundantes.
 * <li>Instanciar páginas detipo {@link BbVLDockingApplicationPage}.
 * <li>Corregir defecto por el que la ventana no se refresca correctamente al cambiar de página.
 * </ul>
 * 
 * @see VLDockingApplicationPageFactory
 * @see BbVLDockingApplicationPage
 * 
 * @param <T>
 *            el tipo de las entidades editadas por las páginas que crea la factoría.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbVLDockingApplicationPageFactory<T> extends VLDockingApplicationPageFactory {

    /**
     * Relación de <em>application windows</em> a las que ya se les ha establecido los <em>listener</em> necesarios.
     * 
     * @see #addPageListenersIfNeeded(ApplicationWindow)
     */
    private final Set<ApplicationWindow> windowsWithListeners;

    /**
     * Construye la factoría.
     */
    public BbVLDockingApplicationPageFactory() {

        super();
        this.windowsWithListeners = new HashSet<ApplicationWindow>();
    }

    /**
     * Crea la página, que a diferencia de
     * {@link VLDockingApplicationPage#createApplicationPage(ApplicationWindow,PageDescriptor)} es de tipo
     * {@link BbVLDockingApplicationPage}.
     * <p>
     * <b>Siempre</b> cachea las páginas creadas.
     * 
     * @param window
     *            la ventana.
     * @param descriptor
     *            el descriptor de página.
     * @return la página.
     */
    @Override
    public ApplicationPage createApplicationPage(ApplicationWindow window, PageDescriptor descriptor) {

        VLDockingApplicationPage page = this.findPage(window, descriptor);
        if (page == null) {
            // Crear y cachear la página.
            page = new BbVLDockingApplicationPage<T>(window, descriptor);
            this.cachePage(page);

            // Añade los listeners necesarios a la ventana.
            this.addPageListenersIfNeeded(window);
        }

        return page;
    }

    /**
     * Registra los <em>listener</em> necesarios para una ventana si sólo si no estaba registrado.
     * 
     * @param window
     *            la ventana de la aplicación.
     */
    private void addPageListenersIfNeeded(ApplicationWindow window) {

        final Boolean alreadyAdded = this.windowsWithListeners.add(window);
        if (alreadyAdded) {
            window.addPageListener(new PageListener() {
                /**
                 * Ocultar el control de la página cuando se cierra.
                 * 
                 * @see org.springframework.richclient.application.PageListener#pageClosed
                 */
                public void pageClosed(ApplicationPage page) {

                    page.getControl().setVisible(Boolean.FALSE);
                }

                /**
                 * Hacer visible el control de la página cuando se abre.
                 * 
                 * @see org.springframework.richclient.application.PageListener#pageOpened
                 */
                public void pageOpened(ApplicationPage page) {

                    page.getControl().setVisible(Boolean.TRUE);
                }
            });
        }
    }
}
