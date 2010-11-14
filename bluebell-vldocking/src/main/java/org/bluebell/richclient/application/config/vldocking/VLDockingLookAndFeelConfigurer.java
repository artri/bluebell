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

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.bluebell.richclient.application.config.BbLookAndFeelConfigurer;
import org.bluebell.richclient.application.docking.vldocking.VLDockingUtils;
import org.springframework.util.Assert;

/**
 * VLDocking look and feel configurer that installs the widget desktop style after installing a new look and feel.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class VLDockingLookAndFeelConfigurer extends BbLookAndFeelConfigurer {

    /**
     * The widget desktop style to be employed.
     */
    private WidgetDesktopStyle widgetDesktopStyle;

    /**
     * Creates the look and feel configurer.
     */
    public VLDockingLookAndFeelConfigurer() {

        super();
    }

    /**
     * Creates the look and feel configurer.
     * <p>
     * Specifying look and feel name in constructor ensures look and feel is stablished before splash screen is shown.
     * 
     * @param lookAndFeelName
     *            the look and feel name to be used.
     */
    public VLDockingLookAndFeelConfigurer(String lookAndFeelName) {

        super(lookAndFeelName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLookAndFeelChange(LookAndFeel oldLookAndFeel, LookAndFeel newLookAndFeel) {

        this.installWidgetDesktopStyle();
    }

    /**
     * Sets the widget desktop style instance.
     * 
     * @param widgetDesktopStyle
     *            the widget desktop style instance to set.
     */
    public final void setWidgetDesktopStyle(WidgetDesktopStyle widgetDesktopStyle) {

        Assert.notNull(widgetDesktopStyle, "widgetDesktopStyle");

        this.widgetDesktopStyle = widgetDesktopStyle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        super.afterPropertiesSet();
        
        this.installColors();
        this.installWidgetDesktopStyle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInstallCustomDefaults() throws Exception {

        super.doInstallCustomDefaults();
        
        this.installColors();
    }

    /**
     * Installs the VLDocking colors.
     * 
     * @see VLDockingUtils.DockingColor
     */
    protected void installColors() {

        UIManager.put(VLDockingUtils.DockingColor.ACTIVE_WIDGET.getKey(), UIManager.getColor("activeCaption"));
        UIManager.put(VLDockingUtils.DockingColor.INACTIVE_WIDGET.getKey(), UIManager.getColor("controlLtHighlight"));
        UIManager.put(VLDockingUtils.DockingColor.BACKGROUND.getKey(), UIManager.getColor("control"));
        UIManager.put(VLDockingUtils.DockingColor.HIGHLIGHT.getKey(), UIManager.getColor("controlHighlight"));
        UIManager.put(VLDockingUtils.DockingColor.SHADOW.getKey(), UIManager.getColor("controlDkShadow"));
    }

    /**
     * Uninstall previously installed widget desktop style setting and re-installs them again.
     */
    protected final void installWidgetDesktopStyle() {

        final WidgetDesktopStyle styler = this.getWidgetDesktopStyle();

        if (styler != null) {
            styler.uninstallWidgetDesktopStyle();
            styler.setWidgetDesktopStyle();
        }
    }

    /**
     * Gets the widget desktop style instance.
     * 
     * @return the widget desktop style instance.
     */
    protected final WidgetDesktopStyle getWidgetDesktopStyle() {

        return this.widgetDesktopStyle;
    }
}
