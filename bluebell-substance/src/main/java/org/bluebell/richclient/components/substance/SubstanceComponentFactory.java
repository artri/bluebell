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

/**
 * 
 */
package org.bluebell.richclient.components.substance;

import javax.swing.JTabbedPane;

import org.bluebell.richclient.factory.ComponentFactoryDecorator;
import org.springframework.richclient.factory.ComponentFactory;

/**
 * Substance based component factory implementation.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SubstanceComponentFactory extends ComponentFactoryDecorator {

    /**
     * Creates de Substance component factory.
     */
    public SubstanceComponentFactory() {

        super();
    }

    /**
     * Creates the Substance component factory decorator given the decorated component.
     * 
     * @param decoratedComponentFactory
     *            the decorated component factory.
     */
    public SubstanceComponentFactory(ComponentFactory decoratedComponentFactory) {

        super(decoratedComponentFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JTabbedPane createTabbedPane() {

        // This is just an example
        final JTabbedPane tabbedPane = super.createTabbedPane();

        // This is already done at BbLookAndFeelConfigurer
        // tabbedPane.putClientProperty(LafWidget.TABBED_PANE_PREVIEW_PAINTER, new DefaultTabPreviewPainter());

        return tabbedPane;
    }
}
