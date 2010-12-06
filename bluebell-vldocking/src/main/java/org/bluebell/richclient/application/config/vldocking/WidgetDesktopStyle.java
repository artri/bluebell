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
package org.bluebell.richclient.application.config.vldocking;

import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.apache.commons.collections.MapUtils;
import org.bluebell.richclient.application.docking.vldocking.VLDockingUtils;
import org.bluebell.richclient.application.docking.vldocking.VLDockingUtils.DockViewType;
import org.bluebell.richclient.application.docking.vldocking.ui.BbAutoHideExpandPanelUI;
import org.bluebell.richclient.application.docking.vldocking.ui.BbAutoHideButtonUI;
import org.bluebell.richclient.application.docking.vldocking.ui.BbDockViewTitleBarUI;
import org.bluebell.richclient.application.docking.vldocking.ui.BbDockViewUI;
import org.bluebell.richclient.application.docking.vldocking.ui.BbDockingSplitPaneUI;
import org.bluebell.richclient.components.RoundedBorder;

import com.vlsolutions.swing.docking.DockingConstants;
import com.vlsolutions.swing.docking.ui.DockingUISettings;

/**
 * <code>DockingPreferences</code> extension capable of setting a widget like style. Replaces static methods using an OO
 * approach.
 * <p />
 * It's designed with inheritance support thanks to template methods.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class WidgetDesktopStyle {

    /**
     * The default border size.
     * 
     * @see #desktopBorderSize()
     */
    public static final Integer BORDER_SIZE = 3;

    /**
     * The default margin size.
     * 
     * @see #desktopMarginSize()
     */
    public static final Integer MARGIN_SIZE = 5;

    /**
     * The widget like desktop style defaults map.
     */
    private final Map<String, Object> defaults = new HashMap<String, Object>();

    /**
     * Creates the style.
     */
    public WidgetDesktopStyle() {

        super();
    }

    /**
     * Convenience method to use a widget like desktop style.
     * <p>
     * The "widget style" uses a flat style and rounded corners that remembers Web 2.0 style.
     * <p>
     * Proceeds according to <a href= "http://www.vlsolutions.com/en/documentation/docking/tutorial/tutorial8.php"
     * >VLDocking Tutorial</a>:
     * <p>
     * <div> Please note that to avoid having your own UI settings beeing erased by the default ones, you will have to
     * follow the pattern :
     * <ol>
     * <li>pre-install the default ui settings in you main() method, or any method prior DockingDesktop usage
     * <li>put your new settings as UIManager properties
     * </ol>
     * 
     * <pre>
     *  public static void main(String[] args){ // first, preload the UI to avoid erasing your own customizations
     * 
     *          DockingUISettings.getInstance().installUI(); 
     * 
     *          // declare your border
     *          Border myBorder = ... 
     * 
     *          // and start customizing...
     *          UIManager.put(&quot;DockView.maximizedDockableBorder&quot;, myBorder); ... }
     * </pre>
     * 
     * </div>
     */
    public final void setWidgetDesktopStyle() {

        // Install VLDocking defaults
        DockingUISettings.getInstance().installUI();

        // Install widget desktop style defaults
        for (Map.Entry<String, Object> entry : this.getDefaultsMap().entrySet()) {
            UIManager.put(entry.getKey(), entry.getValue());
        }

        // Update component tree UI for all windows
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }

    /**
     * Uninstall previously installed widget desktop style properties.
     * <p/>
     * This method is equivalent to call <code>UIManaget.put(key, null)</code> for every single property.
     */
    public final void uninstallWidgetDesktopStyle() {

        // Clear custom defaults
        this.getDefaultsMap().clear();
    }

    /**
     * Creates the border for auto hide buttons given the expand position.
     * 
     * @param pos
     *            the expand position.
     * @return the border.
     * 
     * @see DockingConstants#INT_HIDE_TOP
     */
    protected Border autoHideButtonExpandBorder(int pos) {

        final Integer b = 2;
        final Integer m = this.desktopMarginSize();
        final Color color = VLDockingUtils.DockingColor.SHADOW.getColor();

        /*
         * (JAF), 20100113, RoundedBorder does not work in this case. I do know why
         */
        // final Border outsideBorder = new PartialLineBorder(color, b, Boolean.TRUE, m);
        // outsideBorder = new RoundedBorder(m, b, color);
        final Border outsideBorder = BorderFactory.createMatteBorder(b, b, b, b, color);

        final Border insideBorder;

        switch (pos) {
            case DockingConstants.INT_HIDE_TOP:
            case DockingConstants.INT_HIDE_BOTTOM:
                insideBorder = BorderFactory.createEmptyBorder(0, m, 0, m);
                break;
            case DockingConstants.INT_HIDE_LEFT:
                insideBorder = BorderFactory.createEmptyBorder(m, 0, m, m);
                break;
            case DockingConstants.INT_HIDE_RIGHT:
                insideBorder = BorderFactory.createEmptyBorder(m, m, m, 0);
                break;
            default:
                throw new IllegalStateException("Position \"" + pos + "\" is unknown");
        }

        return BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
    }

    /**
     * Creates the border for auto hide button panels given the expand position.
     * 
     * @param pos
     *            the expand position.
     * @return the border.
     * 
     * @see DockingConstants#INT_HIDE_TOP
     */
    protected Border autoHideButtonPanelBorder(int pos) {

        final Integer m = this.desktopMarginSize();

        switch (pos) {
            case DockingConstants.INT_HIDE_TOP:
                return BorderFactory.createEmptyBorder(m, m, 0, 0);
            case DockingConstants.INT_HIDE_LEFT:
                return BorderFactory.createEmptyBorder(m, m, 0, 0);
            case DockingConstants.INT_HIDE_BOTTOM:
                return BorderFactory.createEmptyBorder(0, m, m, 0);
            case DockingConstants.INT_HIDE_RIGHT:
                return BorderFactory.createEmptyBorder(m, 0, 0, m);
            default:
                throw new IllegalStateException("Position \"" + pos + "\" is unknown");
        }
    }

    /**
     * Creates the border for auto hide expand panel draggers given the expand position.
     * 
     * @param pos
     *            the expand position.
     * @return the border.
     * 
     * @see DockingConstants#INT_HIDE_TOP
     */
    protected Border autoHideExpandPanelDraggerBorder(int pos) {

        final Integer s = 2;
        final Color color = VLDockingUtils.DockingColor.INACTIVE_WIDGET.getColor();

        switch (pos) {
            case DockingConstants.INT_HIDE_TOP:
                return BorderFactory.createMatteBorder(s, 0, 0, 0, color);
            case DockingConstants.INT_HIDE_LEFT:
                return BorderFactory.createMatteBorder(0, s, 0, 0, color);
            case DockingConstants.INT_HIDE_BOTTOM:
                return BorderFactory.createMatteBorder(0, 0, s, 0, color);
            case DockingConstants.INT_HIDE_RIGHT:
                return BorderFactory.createMatteBorder(0, 0, 0, s, color);
            default:
                throw new IllegalStateException("Position \"" + pos + "\" is unknown");
        }
    }

    /**
     * Gets the desktop border size.
     * 
     * @return {@value #BORDER_SIZE}.
     */
    protected Integer desktopBorderSize() {

        return WidgetDesktopStyle.BORDER_SIZE;
    }

    /**
     * Gets the desktop margin size.
     * 
     * @return {@value #MARGIN_SIZE}.
     */
    protected Integer desktopMarginSize() {

        return WidgetDesktopStyle.MARGIN_SIZE;
    }

    /**
     * Creates the border for dock views.
     * 
     * @param type
     *            the dock view type.
     * @param active
     *            is the dockview active.
     * @param pos
     *            the expand position.
     * @return the border.
     * 
     * @see DockingConstants#INT_HIDE_TOP
     */
    protected Border dockViewBorder(DockViewType type, Boolean active, int pos) {

        final Integer m = this.desktopMarginSize();
        final Integer b = this.desktopBorderSize();

        switch (type) {
            case DOCKED:
                final Color color;
                if (active) {
                    color = VLDockingUtils.DockingColor.ACTIVE_WIDGET.getColor();
                } else {
                    color = VLDockingUtils.DockingColor.INACTIVE_WIDGET.getColor();
                }

                return new RoundedBorder(m, b, color);
                // return new PartialLineBorder(color, b, Boolean.TRUE, m);
            case TABBED:
                return BorderFactory.createEmptyBorder();
            case HIDDEN:
            case MAXIMIZED:
                final Border outsideBorder = this.externalBorder();
                final Border insideBorder = this.dockViewBorder(DockViewType.DOCKED, active, pos);

                return BorderFactory.createCompoundBorder(outsideBorder, insideBorder);

            default:
                throw new IllegalStateException("DockViewType \"" + type + "\" is illegal");
        }
    }

    /**
     * Creates the border for dock view title bars.
     * 
     * @return the border.
     */
    protected Border dockViewTitleBarBorder() {

        return BorderFactory.createEmptyBorder();
    }

    /**
     * Creates the external border that contains every non detached dockable.
     * 
     * @return the border.
     */
    protected Border externalBorder() {

        final Integer m = this.desktopMarginSize();

        return BorderFactory.createEmptyBorder(m, m, m, m);
    }

    /**
     * Creates the border for floating dialogs.
     * 
     * @return the border.
     */
    protected Border floatingDialogBorder() {

        return BorderFactory.createEmptyBorder();
    }

    /**
     * Creates the border for floating dialog titles.
     * 
     * @return the border.
     */
    protected Border floatingDialogTitleBorder() {

        return BorderFactory.createEmptyBorder();
    }

    /**
     * Gets the property values to be installed.
     * <p />
     * This method employs a lazy initialization of UI properties map.
     * 
     * @return a map indexed by property name.
     */
    protected Map<String, Object> getDefaultsMap() {

        final Font internalFrameTitleFont = UIManager.getFont("InternalFrame.titleFont");

        if (!MapUtils.isEmpty(this.defaults)) {
            return this.defaults;
        }

        /*
         * (JAF), 20101205, Very important!!!
         * 
         * Should request focus on tab selection be activated? On one hand in case negative activation will not be
         * triggered correctly. On the other hand (affirmative case) every time a tabbed container is shown request
         * focus, so this is a problem since is better to keep old dockable focused (activated!)
         * 
         * Decission is to keep value as FALSE since is prior to retain old selection.
         */
        this.defaults.put("TabbedContainer.requestFocusOnTabSelection", Boolean.FALSE);

        // UI Delegates
        this.defaults.put("AutoHideButtonUI", BbAutoHideButtonUI.class.getName());
        this.defaults.put("AutoHideExpandPanelUI", BbAutoHideExpandPanelUI.class.getName());
        this.defaults.put("DetachedDockViewUI", BbDockViewUI.class.getName());
        this.defaults.put("DockViewUI", BbDockViewUI.class.getName());
        this.defaults.put("DockViewTitleBarUI", BbDockViewTitleBarUI.class.getName());
        this.defaults.put("DockingSplitPaneUI", BbDockingSplitPaneUI.class.getName());

        // AutoHideButton: expand border
        this.defaults.put("AutoHideButton.expandBorderTop", //
                this.autoHideButtonExpandBorder(DockingConstants.INT_HIDE_TOP));
        this.defaults.put("AutoHideButton.expandBorderLeft", //
                this.autoHideButtonExpandBorder(DockingConstants.INT_HIDE_LEFT));
        this.defaults.put("AutoHideButton.expandBorderBottom", //
                this.autoHideButtonExpandBorder(DockingConstants.INT_HIDE_BOTTOM));
        this.defaults.put("AutoHideButton.expandBorderRight", //
                this.autoHideButtonExpandBorder(DockingConstants.INT_HIDE_RIGHT));

        // Employ a Substance managed font in order to support resizing:
        // @see SubstanceLookAndFeel#initFontDefaults
        this.defaults.put("AutoHideButton.font", internalFrameTitleFont);

        // AutoHideButtonPanel: border
        this.defaults.put("AutoHideButtonPanel.topBorder", //
                this.autoHideButtonPanelBorder(DockingConstants.INT_HIDE_TOP));
        this.defaults.put("AutoHideButtonPanel.leftBorder", //
                this.autoHideButtonPanelBorder(DockingConstants.INT_HIDE_LEFT));
        this.defaults.put("AutoHideButtonPanel.bottomBorder", //
                this.autoHideButtonPanelBorder(DockingConstants.INT_HIDE_BOTTOM));
        this.defaults.put("AutoHideButtonPanel.rightBorder", //
                this.autoHideButtonPanelBorder(DockingConstants.INT_HIDE_RIGHT));

        // AutoHideExpandPanel: dragger border
        this.defaults.put("AutoHideExpandPanel.topDraggerBorder", //
                this.autoHideExpandPanelDraggerBorder(DockingConstants.INT_HIDE_TOP));
        this.defaults.put("AutoHideExpandPanel.leftDraggerBorder", //
                this.autoHideExpandPanelDraggerBorder(DockingConstants.INT_HIDE_LEFT));
        this.defaults.put("AutoHideExpandPanel.bottomDraggerBorder", //
                this.autoHideExpandPanelDraggerBorder(DockingConstants.INT_HIDE_BOTTOM));
        this.defaults.put("AutoHideExpandPanel.rightDraggerBorder", //
                this.autoHideExpandPanelDraggerBorder(DockingConstants.INT_HIDE_RIGHT));

        // AutoHideExpandPanel: expand from border
        this.defaults.put(VLDockingUtils.activationKey("AutoHideExpandPanel.expandFromTopBorder", Boolean.TRUE), //
                this.dockViewBorder(DockViewType.HIDDEN, Boolean.TRUE, DockingConstants.INT_HIDE_TOP));
        this.defaults.put(VLDockingUtils.activationKey("AutoHideExpandPanel.expandFromTopBorder", Boolean.FALSE), //
                this.dockViewBorder(DockViewType.HIDDEN, Boolean.FALSE, DockingConstants.INT_HIDE_TOP));
        this.defaults.put(VLDockingUtils.activationKey("AutoHideExpandPanel.expandFromLeftBorder", Boolean.TRUE), //
                this.dockViewBorder(DockViewType.HIDDEN, Boolean.TRUE, DockingConstants.INT_HIDE_LEFT));
        this.defaults.put(VLDockingUtils.activationKey("AutoHideExpandPanel.expandFromLeftBorder", Boolean.FALSE), //
                this.dockViewBorder(DockViewType.HIDDEN, Boolean.FALSE, DockingConstants.INT_HIDE_LEFT));
        this.defaults.put(VLDockingUtils.activationKey("AutoHideExpandPanel.expandFromBottomBorder", Boolean.TRUE), //
                this.dockViewBorder(DockViewType.HIDDEN, Boolean.TRUE, DockingConstants.INT_HIDE_BOTTOM));
        this.defaults.put(VLDockingUtils.activationKey("AutoHideExpandPanel.expandFromBottomBorder", Boolean.FALSE), //
                this.dockViewBorder(DockViewType.HIDDEN, Boolean.FALSE, DockingConstants.INT_HIDE_BOTTOM));
        this.defaults.put(VLDockingUtils.activationKey("AutoHideExpandPanel.expandFromRightBorder", Boolean.TRUE), //
                this.dockViewBorder(DockViewType.HIDDEN, Boolean.TRUE, DockingConstants.INT_HIDE_RIGHT));
        this.defaults.put(VLDockingUtils.activationKey("AutoHideExpandPanel.expandFromRightBorder", Boolean.FALSE), //
                this.dockViewBorder(DockViewType.HIDDEN, Boolean.FALSE, DockingConstants.INT_HIDE_RIGHT));

        // DockView: active and inactive dockable border
        this.defaults.put(VLDockingUtils.activationKey("DockView.singleDockableBorder", Boolean.TRUE), //
                this.dockViewBorder(DockViewType.DOCKED, Boolean.TRUE, -1));
        this.defaults.put(VLDockingUtils.activationKey("DockView.singleDockableBorder", Boolean.FALSE), //
                this.dockViewBorder(DockViewType.DOCKED, Boolean.FALSE, -1));
        this.defaults.put("DockView.tabbedDockableBorder", //
                this.dockViewBorder(DockViewType.TABBED, Boolean.FALSE, -1));
        this.defaults.put(VLDockingUtils.activationKey("DockView.maximizedDockableBorder", Boolean.TRUE), //
                this.dockViewBorder(DockViewType.MAXIMIZED, Boolean.TRUE, -1));
        this.defaults.put(VLDockingUtils.activationKey("DockView.maximizedDockableBorder", Boolean.FALSE), //
                this.dockViewBorder(DockViewType.MAXIMIZED, Boolean.FALSE, -1));

        // DockViewTitleBar: border and title font
        this.defaults.put("DockViewTitleBar.border", this.dockViewTitleBarBorder());

        // Employ a (Substance) managed font in order to support resizing:
        // @see SubstanceLookAndFeel#initFontDefaults
        // In spite of being a Substance related topic this is no dependant, so keep it here
        this.defaults.put("DockViewTitleBar.titleFont", internalFrameTitleFont);

        // BbFloatingDialog: border and titled border
        this.defaults.put("BbFloatingDialog.dialogBorder", this.floatingDialogBorder());
        this.defaults.put("BbFloatingDialog.titleBorder", this.floatingDialogTitleBorder());

        // SplitContainer: border and divider size
        this.defaults.put("SplitContainer.border", this.externalBorder());
        this.defaults.put("SplitContainer.dividerSize", this.desktopMarginSize());

        // Employ a (Substance) managed font in order to support resizing:
        // @see SubstanceLookAndFeel#initFontDefaults
        // In spite of being a Substance related topic this is no dependant, so keep it here
        this.defaults.put("JTabbedPaneSmartIcon.font", internalFrameTitleFont);

        return this.defaults;
    }

    /**
     * Interface responsible for activating or deactivating components.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public interface ActivationAware {

        /**
         * Activates or deactivates a component.
         * 
         * @param target
         *            the component to be activated/deactivated.
         * @param active
         *            <code>true</code> for activating.
         */
        void activate(JComponent target, Boolean active);
    }
}
