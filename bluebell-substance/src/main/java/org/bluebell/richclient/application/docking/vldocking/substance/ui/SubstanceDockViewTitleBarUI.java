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

package org.bluebell.richclient.application.docking.vldocking.substance.ui;

import javax.swing.JComponent;

import org.bluebell.richclient.application.docking.vldocking.ui.BbDockViewTitleBarUI;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceSkin;
import org.jvnet.substance.painter.decoration.DecorationAreaType;
import org.jvnet.substance.utils.SubstanceCoreUtilities;

import com.vlsolutions.swing.docking.DockViewTitleBar;

/**
 * A UI for the {@link com.vlsolutions.swing.docking.DockViewTitleBar} that uses a Substance decoration painter.
 * <p />
 * Extends {@link BbDockViewTitleBarUI} in the following way:
 * <ul>
 * <li>Re-installs font and repaint background when painting.
 * </ul>
 * 
 * @see <a href="http://forum.springsource.org/showthread.php?t=73183">Related Spring forum topic</a>
 * @see <a href="http://carsten-oland.blogspot.com/2009/08/substance-with-vldocking-framework.html">Related blog
 *      entry</a>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SubstanceDockViewTitleBarUI extends BbDockViewTitleBarUI {

    /**
     * Creates the UI.
     * 
     * @param tb
     *            the dock view title bar to be painted.
     */
    public SubstanceDockViewTitleBarUI(DockViewTitleBar tb) {

        super(tb);
    }

    /**
     * {@inheritDoc}
     * <p />
     * Also registers the toolbar as Substance secondary title pane.
     */
    @Override
    public void installUI(JComponent c) {

        super.installUI(c);

        SubstanceLookAndFeel.setDecorationType(c, DecorationAreaType.SECONDARY_TITLE_PANE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstallUI(JComponent c) {

        super.uninstallUI(c);

        SubstanceLookAndFeel.setDecorationType(c, DecorationAreaType.NONE);
    }

    /**
     * {@inheritDoc}
     */
    public void activate(JComponent target, Boolean active) {

        final SubstanceSkin skin = SubstanceCoreUtilities.getSkin((DockViewTitleBar) target);
        if (skin != null) {
            super.activate(target, active);
        }
    }

    /**
     * Factory method for creating the UI.
     * 
     * @param tb
     *            the title bar.
     * @return the UI.
     */
    public static SubstanceDockViewTitleBarUI createUI(JComponent tb) {

        return new SubstanceDockViewTitleBarUI((DockViewTitleBar) tb);
    }
}
