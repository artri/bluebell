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
package org.bluebell.richclient.components.jideoss;

import java.awt.Image;

import javax.swing.JTabbedPane;

import org.bluebell.richclient.factory.ComponentFactoryDecorator;
import org.bluebell.richclient.swing.util.SwingUtils;

import com.jidesoft.swing.JideTabbedPane;

/**
 * Jide OSS based component factory implementation.
 * 
 * @see <a href="https://jide-oss.dev.java.net/">Jide OSS</a>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class JideOssComponentFactory extends ComponentFactoryDecorator {

    /**
     * 
     */
    private static final String JIDE_TABBED_PANE_TAB_TRAILING_IMAGE = "jideTabbedPane.tabTrailingImage";

    /**
     * Creates de Jide OSS component factory.
     */
    public JideOssComponentFactory() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JTabbedPane createTabbedPane() {

        final JideTabbedPane tabbedPane = new JideTabbedPane();

        tabbedPane.setShowTabButtons(Boolean.TRUE);
        tabbedPane.setHideOneTab(Boolean.FALSE);
        tabbedPane.setShowTabArea(Boolean.TRUE);
        tabbedPane.setShowTabContent(Boolean.TRUE);
        tabbedPane.setUseDefaultShowIconsOnTab(Boolean.FALSE);
        tabbedPane.setShowIconsOnTab(Boolean.TRUE);
        tabbedPane.setBoldActiveTab(Boolean.TRUE);
        tabbedPane.setScrollSelectedTabOnWheel(Boolean.TRUE);
        tabbedPane.setShowCloseButton(Boolean.FALSE);
        tabbedPane.setUseDefaultShowCloseButtonOnTab(Boolean.FALSE);
        tabbedPane.setShowCloseButtonOnTab(Boolean.TRUE);
        tabbedPane.setTabEditingAllowed(Boolean.FALSE);

        // Install tab trailing component
        final Image image = this.getImageSource().getImage(JideOssComponentFactory.JIDE_TABBED_PANE_TAB_TRAILING_IMAGE);
        tabbedPane.setTabTrailingComponent(SwingUtils.generateComponent(image));

        return tabbedPane;
    }
}
