/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell Substance.
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

package org.bluebell.richclient.application.docking.vldocking.substance;

import java.awt.Window;

import javax.swing.JDialog;

import org.bluebell.richclient.application.docking.vldocking.BbDockableContainerFactory;

import com.vlsolutions.swing.docking.FloatingDockableContainer;

/**
 * Substance specific implementation of the <code>DockableContainerFactory</code>.
 * <p>
 * Fixes a known Substance bug working with detached dockables.
 * 
 * @see <a href="https://substance.dev.java.net/issues/show_bug.cgi?id=284">Related bug</a>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SubstanceDockableContainerFactory extends BbDockableContainerFactory {

    /**
     * {@inheritDoc}
     */
    public FloatingDockableContainer createFloatingDockableContainer(Window owner) {

        final Boolean defaultLookAndFeelDecorated = JDialog.isDefaultLookAndFeelDecorated();

        JDialog.setDefaultLookAndFeelDecorated(Boolean.FALSE);
        final FloatingDockableContainer floatingDockableContainer = super.createFloatingDockableContainer(owner);
        JDialog.setDefaultLookAndFeelDecorated(defaultLookAndFeelDecorated);

        return floatingDockableContainer;
    }
}
