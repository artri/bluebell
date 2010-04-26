/*
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
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

package org.bluebell.richclient.application.docking.vldocking.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import org.bluebell.richclient.application.config.vldocking.WidgetDesktopStyle.ActivationAware;
import org.bluebell.richclient.application.docking.vldocking.VLDockingUtils;

import com.vlsolutions.swing.docking.DockViewTitleBar;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableContainer;
import com.vlsolutions.swing.docking.DockingUtilities;
import com.vlsolutions.swing.docking.ui.DockViewTitleBarUI;

/**
 * A UI for the {@link com.vlsolutions.swing.docking.DockViewTitleBar} that uses a widget like style.
 * <p />
 * Extends {@link DockViewTitleBarUI} in the following way:
 * <ul>
 * <li>Implements <code>ActivationAware</code>.
 * <li>Every time the "active" property is changed the dockable container is activated/deactivated accordinly.
 * <li>Re-installs font and repaint background when painting.
 * </ul>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbDockViewTitleBarUI extends DockViewTitleBarUI implements ActivationAware {

    /**
     * The active property name.
     */
    private static final String ACTIVE_PROPERTY_NAME = "active";

    /**
     * Creates the UI.
     * 
     * @param tb
     *            the dock view title bar to be painted.
     */
    public BbDockViewTitleBarUI(DockViewTitleBar tb) {

        super(tb);
    }

    /**
     * {@inheritDoc}
     * 
     * @see #installLabel()
     * @see #installBackground()
     */
    @Override
    public void paint(Graphics g, JComponent c) {

        this.installLabel(); // Re-install label in order to support font resizing
        this.installBackground();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {

        if (BbDockViewTitleBarUI.ACTIVE_PROPERTY_NAME.equals(e.getPropertyName())) {

            final Boolean active = (Boolean) e.getNewValue();
            final Dockable dockable = this.titleBar.getDockable();

            this.repaintDockable(dockable, active);
        } else {
            super.propertyChange(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void activate(JComponent target, Boolean active) {

        final DockViewTitleBar dockViewTitleBar = (DockViewTitleBar) target;

        final Color color;
        if (active) {
            color = VLDockingUtils.DockingColor.ACTIVE_WIDGET.getColor();
        } else {
            color = VLDockingUtils.DockingColor.INACTIVE_WIDGET.getColor();
        }
        dockViewTitleBar.setBackground(color);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overriden to avoid changing the titleBar background every time.
     */
    protected void installLabel() {

        final JLabel titleLabel = titleBar.getTitleLabel();
        final Font font = UIManager.getFont("DockViewTitleBar.titleFont");
        titleLabel.setFont(font);
        // titleLabel.setForeground(notSelectedTextColor);
        // titleBar.setBackground(notSelectedTitleColor);
    }

    /**
     * {@inheritDoc}
     * <p>
     * A different color scheme is used depending on the title bar is activated or not.
     */
    @Override
    protected void installBackground() {

        this.activate(this.titleBar, this.titleBar.isActive());
    }

    /**
     * Activates or deactivates the given dockable and repaints it.
     * 
     * @param dockable
     *            the dockable to be repainted.
     * @param active
     *            <code>true</code> for activating.
     */
    private void repaintDockable(Dockable dockable, Boolean active) {

        final DockableContainer dockableContainer = DockingUtilities.findSingleDockableContainer(dockable);

        if ((dockableContainer != null) && (dockableContainer instanceof JComponent)) {
            final JComponent jComponent = (JComponent) dockableContainer;
            final ComponentUI componentUI = UIManager.getUI(jComponent);

            if (componentUI instanceof ActivationAware) {
                final ActivationAware activationAware = (ActivationAware) componentUI;

                activationAware.activate(jComponent, active);
                jComponent.repaint();
            }
        }
    }

    /**
     * Factory method for creating the UI.
     * 
     * @param tb
     *            the title bar.
     * @return the UI.
     */
    public static BbDockViewTitleBarUI createUI(JComponent tb) {

        return new BbDockViewTitleBarUI((DockViewTitleBar) tb);
    }
}
