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

/**
 * 
 */
package org.bluebell.richclient.application.docking.vldocking.substance;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;

import org.bluebell.richclient.application.docking.vldocking.VLDockingUtils;
import org.bluebell.richclient.application.docking.vldocking.DockingPreferencesWidgetExtension.ActivationAware;
import org.springframework.util.Assert;

import com.vlsolutions.swing.docking.DockView;
import com.vlsolutions.swing.docking.DockViewTitleBar;
import com.vlsolutions.swing.docking.DockingPanel;
import com.vlsolutions.swing.docking.SplitContainer;
import com.vlsolutions.swing.docking.TabbedDockableContainer;
import com.vlsolutions.swing.docking.ui.DockViewUI;

/**
 * A UI for the dock view that uses Substance color scheme.
 * <p />
 * Extends {@link DockViewUI} in the following way:
 * <ul>
 * <li>Implements <code>ActivationAware</code>.
 * <li>Attachs activation/deactivation to <code>installUI</code> method.
 * <li>Redefines <code>installXXXBorder</code> for looking deactivated after launching.
 * </ul>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SubstanceDockViewUI extends DockViewUI implements ActivationAware {

    /**
     * The singleton UI instance.
     */
    private static final SubstanceDockViewUI INSTANCE = new SubstanceDockViewUI();

    /**
     * Creates the UI.
     */
    public SubstanceDockViewUI() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void installUI(JComponent c) {

        super.installUI(c);

        final DockView dockView = (DockView) c;
        this.activate(dockView, dockView.getTitleBar().isActive());

        // (JAF), 20100226, maximized dockables lose their background color under Substance 5.3 so set it explicitly
        c.setBackground(VLDockingUtils.DockingColor.BACKGROUND.getColor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstallUI(JComponent c) {

        super.uninstallUI(c);
        c.setBorder(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activate(JComponent target, Boolean active) {

        Assert.isInstanceOf(DockView.class, target, "target");
        Assert.notNull(active, "active");

        this.installBorder((DockView) target, active);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void installMaximizedDockableBorder(DockView dockView) {

        this.installBorder(dockView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void installSingleDockableBorder(DockView dockView) {

        this.installBorder(dockView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void installTabbedDockableBorder(DockView dockView) {

        this.installBorder(dockView);
    }

    /**
     * Install different borders to the dockView depending on its selection state {@link #isActive(DockView)}.
     * 
     * @param dockView
     *            the dock view.
     * 
     * @see #installBorder(DockView, Boolean)
     */
    protected final void installBorder(DockView dockView) {

        this.installBorder(dockView, this.isActive(dockView));
    }

    /**
     * Install different borders to the dockView depending on its selection state.
     * 
     * @param dockView
     *            the dock view.
     * @param active
     *            <code>true</code> for active dockables.
     * 
     * @see DockViewUI.ViewAncestorListener
     */
    protected final void installBorder(DockView dockView, Boolean active) {

        final Container parent = dockView.getParent();

        final Border border;
        if (parent instanceof TabbedDockableContainer) {
            final Container grandParent = parent.getParent();
            if (grandParent instanceof DockingPanel) {
                border = UIManager.getBorder(VLDockingUtils.activationKey("DockView.maximizedDockableBorder", active));
            } else {
                border = UIManager.getBorder(VLDockingUtils.activationKey("DockView.singleDockableBorder", active));
            }
        } else if (parent instanceof SplitContainer) {
            border = UIManager.getBorder(VLDockingUtils.activationKey("DockView.singleDockableBorder", active));
        } else if (parent instanceof DockingPanel) {
            border = UIManager.getBorder(VLDockingUtils.activationKey("DockView.maximizedDockableBorder", active));
        } else if (parent instanceof JPanel) { // FloatingDialog
            border = UIManager.getBorder(VLDockingUtils.activationKey("DockView.singleDockableBorder", active));
        } else {
            border = UIManager.getBorder(VLDockingUtils.activationKey("DockView.maximizedDockableBorder", active));
        }

        dockView.setBorder(border);
    }

    /**
     * Returns whether the target dock view is currently active or not.
     * 
     * @param dockView
     *            the dock view.
     * @return <code>true</code> if active and <code>false</code> in other case.
     */
    private Boolean isActive(DockView dockView) {

        final DockViewTitleBar titleBar = dockView.getTitleBar();
        final Boolean active = (titleBar != null) ? titleBar.isActive() : Boolean.FALSE;

        return active;
    }

    /**
     * Factory method for creating the UI.
     * 
     * @param c
     *            the dock view.
     * @return the UI.
     */
    public static ComponentUI createUI(JComponent c) {

        return SubstanceDockViewUI.INSTANCE;
    }
}
