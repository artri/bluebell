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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;

import org.springframework.util.ReflectionUtils;

import com.vlsolutions.swing.docking.FloatingDialog;
import com.vlsolutions.swing.docking.SingleDockableContainer;

/**
 * Floating dialog implementation that employs a single color for the resizer. Activation simply changes widget title
 * pane and borders, not the background itself.
 * 
 * @see CloseDockablesOnWindowClosing
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbFloatingDialog extends FloatingDialog {

    /**
     * This is a <code>Serializable</code> class.
     */
    private static final long serialVersionUID = 1567088727707662859L;

    /**
     * The <code>activeCaptionColor</code> field from {@link FloatingDialog}.
     */
    private static final Field ACTIVE_CAPTION_COLOR_FIELD = ReflectionUtils.findField(//
            FloatingDialog.class, "activeCaptionColor");
    /**
     * The <code>inactiveCaptionColor</code> field from {@link FloatingDialog}.
     */
    private static final Field INACTIVE_CAPTION_COLOR_FIELD = ReflectionUtils.findField(//
            FloatingDialog.class, "inactiveCaptionColor");

    static {
        ReflectionUtils.makeAccessible(BbFloatingDialog.ACTIVE_CAPTION_COLOR_FIELD);
        ReflectionUtils.makeAccessible(BbFloatingDialog.INACTIVE_CAPTION_COLOR_FIELD);
    }

    /**
     * Creates the dialog given its parent dialog.
     * 
     * @param parent
     *            the parent dialog.
     */
    public BbFloatingDialog(Dialog parent) {

        super(parent);
    }

    /**
     * Creates the dialog given its parent frame.
     * 
     * @param parent
     *            the parent frame.
     */
    public BbFloatingDialog(Frame parent) {

        super(parent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Changes the active caption color field before calling super.
     * <p>
     * Also installs a <code>WindowListener</code> to prevent failures while restoring/reopening detached views
     * previously closed.
     */
    public void init() {

        final Color backgroundColor = VLDockingUtils.DockingColor.BACKGROUND.getColor();

        ReflectionUtils.setField(BbFloatingDialog.ACTIVE_CAPTION_COLOR_FIELD, this, backgroundColor);
        ReflectionUtils.setField(BbFloatingDialog.INACTIVE_CAPTION_COLOR_FIELD, this, backgroundColor);

        this.addWindowListener(new CloseDockablesOnWindowClosing());

        super.init();
    }

    /**
     * Closes dockables embedded in a dialog when it is closed. Prevent failures while restoring/reopening detached
     * views that was closed by Alt-F4 or X button (with decorations on).
     * <p>
     * Proceeds according to the issue related at <a
     * href="http://forum.springsource.org/showthread.php?t=73183&page=2">Spring Richclient Forum Topic</a>
     * <p>
     * Thanks to cmadsen_dk !!!
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class CloseDockablesOnWindowClosing extends WindowAdapter {

        /**
         * {@inheritDoc}
         */
        @Override
        public void windowClosing(WindowEvent e) {

            // Make Alt-F4 etc events close the dockable embedded in the dialog
            final Component[] components = ((FloatingDialog) e.getSource()).getContentPane().getComponents();

            for (Component component : components) {
                if (component instanceof SingleDockableContainer) {
                    final SingleDockableContainer singleDockableContainer = (SingleDockableContainer) component;
                    BbFloatingDialog.this.desktop.close(singleDockableContainer.getDockable());
                }
            }

            super.windowClosing(e);
        }
    }
}
