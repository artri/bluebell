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

/**
 * 
 */
package org.bluebell.richclient.application.docking.vldocking.ui;

import java.lang.reflect.Field;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.ComponentUI;

import org.bluebell.richclient.application.config.vldocking.WidgetDesktopStyle.ActivationAware;
import org.bluebell.richclient.application.docking.vldocking.VLDockingUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.vlsolutions.swing.docking.AutoHideButton;
import com.vlsolutions.swing.docking.AutoHideExpandPanel;
import com.vlsolutions.swing.docking.DockingConstants;
import com.vlsolutions.swing.docking.ui.AutoHideExpandPanelUI;

/**
 * A UI for the auto hide expand panel with a widget like style.
 * <p />
 * Extends {@link AutoHideExpandPanelUI} in the following way:
 * <ul>
 * <li>Implements <code>ActivationAware</code>.
 * <li>Attachs installing dragger borders to <code>installUI</code> method.
 * <li>Installs content borders every time parent component changes.
 * </ul>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BAutoHideExpandPanelUI extends AutoHideExpandPanelUI implements ActivationAware {

    /**
     * The singleton UI instance.
     */
    private static final BAutoHideExpandPanelUI INSTANCE = new BAutoHideExpandPanelUI();

    /**
     * The field "content" from <code>AutoHideExpandPanel</code>.
     */
    private static final Field CONTENT_FIELD = ReflectionUtils.findField(//
            AutoHideExpandPanel.class, "content", JPanel.class);

    /**
     * The field "selectedButton" from <code>AutoHideExpandPanel</code>.
     */
    private static final Field SELECTED_BUTTON_FIELD = ReflectionUtils.findField(
    //
            AutoHideExpandPanel.class, "selectedButton", AutoHideButton.class);

    static {
        ReflectionUtils.makeAccessible(BAutoHideExpandPanelUI.CONTENT_FIELD);
        ReflectionUtils.makeAccessible(BAutoHideExpandPanelUI.SELECTED_BUTTON_FIELD);
    }

    /**
     * Ancestor listener used to install borders.
     */
    private ViewAncestorListener ancestorListener = new ViewAncestorListener();

    /**
     * Creates the UI.
     */
    public BAutoHideExpandPanelUI() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    public void activate(JComponent target, Boolean active) {

        Assert.isInstanceOf(AutoHideExpandPanel.class, target, "target");
        Assert.notNull(active, "active");

        this.installBorder((AutoHideExpandPanel) target, active);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void installUI(JComponent comp) {

        final AutoHideExpandPanel autoHideExpandPanel = (AutoHideExpandPanel) comp;

        super.installUI(comp);

        autoHideExpandPanel.addAncestorListener(this.ancestorListener);
        this.installDraggerBorders(autoHideExpandPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstallUI(JComponent comp) {

        final AutoHideExpandPanel autoHideExpandPanel = (AutoHideExpandPanel) comp;

        super.uninstallUI(autoHideExpandPanel);

        comp.removeAncestorListener(this.ancestorListener);

        this.uninstallBorder(autoHideExpandPanel);
        this.uninstallDraggerBorders(autoHideExpandPanel);
    }

    /**
     * Installs the border of the <code>AutoHideExpandPanel</code>.
     * 
     * @param autoHideExpandPanel
     *            the panel.
     * @param active
     *            <code>true</code> for active dockables.
     */
    protected void installBorder(AutoHideExpandPanel autoHideExpandPanel, Boolean active) {

        final Integer zone = this.getZone(autoHideExpandPanel);
        final JPanel content = this.getContentPane(autoHideExpandPanel);

        if ((zone != null) && (content != null)) {
            Border border = null;

            switch (zone) {
                case DockingConstants.INT_HIDE_TOP:
                    border = UIManager.getBorder(//
                            VLDockingUtils.activationKey("AutoHideExpandPanel.expandFromTopBorder", active));
                    break;
                case DockingConstants.INT_HIDE_LEFT:
                    border = UIManager.getBorder(//
                            VLDockingUtils.activationKey("AutoHideExpandPanel.expandFromLeftBorder", active));
                    break;
                case DockingConstants.INT_HIDE_BOTTOM:
                    border = UIManager.getBorder(//
                            VLDockingUtils.activationKey("AutoHideExpandPanel.expandFromBottomBorder", active));
                    break;
                case DockingConstants.INT_HIDE_RIGHT:
                    border = UIManager.getBorder(//
                            VLDockingUtils.activationKey("AutoHideExpandPanel.expandFromRightBorder", active));
                    break;
                default:
                    throw new IllegalArgumentException("Illegal zone: \"" + zone + "\"");
            }
            content.setBorder(border);
        }
    }

    /**
     * Installs the dragger borders.
     * 
     * @param autoHideExpandPanel
     *            the panel.
     */
    protected void installDraggerBorders(AutoHideExpandPanel autoHideExpandPanel) {

        final Border topDraggerBorder = UIManager.getBorder("AutoHideExpandPanel.topDraggerBorder");
        final Border leftDraggerBorder = UIManager.getBorder("AutoHideExpandPanel.leftDraggerBorder");
        final Border bottomDraggerBorder = UIManager.getBorder("AutoHideExpandPanel.bottomDraggerBorder");
        final Border rightDraggerBorder = UIManager.getBorder("AutoHideExpandPanel.rightDraggerBorder");

        if (topDraggerBorder != null) {
            autoHideExpandPanel.getTopDragger().setBorder(topDraggerBorder);
        }
        if (leftDraggerBorder != null) {
            autoHideExpandPanel.getLeftDragger().setBorder(leftDraggerBorder);
        }
        if (bottomDraggerBorder != null) {
            autoHideExpandPanel.getBottomDragger().setBorder(bottomDraggerBorder);
        }
        if (rightDraggerBorder != null) {
            autoHideExpandPanel.getRightDragger().setBorder(rightDraggerBorder);
        }
    }

    /**
     * Uninstalls the border of the <code>AutoHideExpandPanel</code>.
     * 
     * @param autoHideExpandPanel
     *            the panel.
     */
    protected void uninstallBorder(AutoHideExpandPanel autoHideExpandPanel) {

        final JPanel content = this.getContentPane(autoHideExpandPanel);
        content.setBorder(null);
    }

    /**
     * Uninstalls the dragger borders.
     * 
     * @param autoHideExpandPanel
     *            the panel.
     */
    protected void uninstallDraggerBorders(AutoHideExpandPanel autoHideExpandPanel) {

        autoHideExpandPanel.getTopDragger().setBorder(null);
        autoHideExpandPanel.getLeftDragger().setBorder(null);
        autoHideExpandPanel.getBottomDragger().setBorder(null);
        autoHideExpandPanel.getRightDragger().setBorder(null);
    }

    /**
     * Gets the content pane of an <code>AutoHideExpandPanel</code>.
     * <p />
     * This method uses reflection.
     * 
     * @param autoHideExpandPanel
     *            the panel.
     * @return its content pane.
     */
    private JPanel getContentPane(AutoHideExpandPanel autoHideExpandPanel) {

        JPanel content = null;
        try {
            content = (JPanel) //
            BAutoHideExpandPanelUI.CONTENT_FIELD.get(autoHideExpandPanel);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Error accessing selected button field", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Error accessing selected button field", e);
        }

        return content;
    }

    /**
     * Gets the zone an <code>AutoHideExpandPanel</code> belongs to.
     * <p/>
     * May be:
     * <ul>
     * <li><code>DockingConstants.INT_HIDE_TOP</code>: top.
     * <li><code>DockingConstants.INT_HIDE_LEFT</code>: left.
     * <li><code>DockingConstants.INT_HIDE_BOTTOM</code>: bottom.
     * <li><code>DockingConstants.INT_HIDE_RIGHT</code>: right.
     * </ul>
     * 
     * @param autoHideExpandPanel
     *            the panel.
     * @return the zone. May be <code>null</code>.
     */
    private Integer getZone(AutoHideExpandPanel autoHideExpandPanel) {

        Integer zone = null;

        try {
            final AutoHideButton selectedButton = (AutoHideButton) //
            BAutoHideExpandPanelUI.SELECTED_BUTTON_FIELD.get(autoHideExpandPanel);

            if (selectedButton != null) {
                zone = selectedButton.getZone();
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Error accessing selected button field", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Error accessing selected button field", e);
        }

        return zone;
    }

    /**
     * Factory method for creating the UI.
     * 
     * @param c
     *            the button.
     * @return the UI.
     */
    public static ComponentUI createUI(JComponent c) {

        return BAutoHideExpandPanelUI.INSTANCE;
    }

    /**
     * Ancestor listener used to install borders.
     */
    protected class ViewAncestorListener implements AncestorListener {

        /**
         * {@inheritDoc}
         */
        public void ancestorAdded(AncestorEvent ancestorEvent) {

            final AutoHideExpandPanel autoHideExpandPanel = (AutoHideExpandPanel) ancestorEvent.getComponent();
            BAutoHideExpandPanelUI.this.installBorder(autoHideExpandPanel, Boolean.FALSE);
        }

        /**
         * {@inheritDoc}
         */
        public void ancestorMoved(AncestorEvent ancestorEvent) {

        }

        /**
         * {@inheritDoc}
         */
        public void ancestorRemoved(AncestorEvent ancestorEvent) {

        }
    }

}
