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

package org.bluebell.richclient.application.docking.vldocking;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

import com.vlsolutions.swing.docking.DefaultDockableContainerFactory;
import com.vlsolutions.swing.docking.FloatingDockableContainer;
import com.vlsolutions.swing.docking.TabbedDockableContainer;

/**
 * Improved implementation of the <code>DockableContainerFactory</code>.
 * 
 * @see BbDockTabbedPane
 * @see BbFloatingDialog
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbDockableContainerFactory extends DefaultDockableContainerFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public TabbedDockableContainer createTabbedDockableContainer() {

        return new BbDockTabbedPane();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatingDockableContainer createFloatingDockableContainer(Window owner) {

        if (owner instanceof Dialog) {
            return new BbFloatingDialog((Dialog) owner);
        } else if (owner instanceof Frame) {
            return new BbFloatingDialog((Frame) owner);
        } else {
            throw new IllegalStateException("Window is neither a Dialog nor a Frame");
        }
    }
}
