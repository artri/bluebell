/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell VLDocking.
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

/**
 * 
 */
package org.bluebell.richclient.application.docking.vldocking;

import org.springframework.richclient.application.docking.vldocking.VLDockingLayoutManager;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * A layout manager that unregisters dockables during removal instead of removing by default
 * {@link DockingDesktop#remove(Dockable)}.
 * <p>
 * This is needed in order to ensure docking context does not remember old dockables after having closed them.
 * </p>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 * 
 * @since 20110119
 */
public final class BbVLDockingLayoutManager implements VLDockingLayoutManager {

    /**
     * The singleton instance.
     */
    private static final BbVLDockingLayoutManager INSTANCE = new BbVLDockingLayoutManager();

    /**
     * Constructs the layout manager.
     */
    private BbVLDockingLayoutManager() {

    }

    /**
     * {@inheritDoc}
     */
    public void addDockable(DockingDesktop desktop, Dockable dockable) {

        desktop.addDockable(dockable);
    }

    /**
     * {@inheritDoc}
     */
    public void removeDockable(DockingDesktop desktop, Dockable dockable) {

        /*
         * (JAF), 20110119, prevent a NPE when dockable state is null within DockingDesktop#close method
         */
        VLDockingUtils.fixVLDockingBug(desktop, dockable);

        /*
         * Dockables must be unregistered during removal. Otherwise closing individual dockables or resetting a
         * perspective may not work as expected.
         * 
         * @see http://jirabluebell.b2b2000.com/browse/BLUE-63
         */
        desktop.remove(dockable);
        desktop.unregisterDockable(dockable);
    }

    /**
     * Returns the singleton instance.
     * 
     * @return the singleton instance.
     */
    public static BbVLDockingLayoutManager getInstance() {

        return BbVLDockingLayoutManager.INSTANCE;
    }
}
