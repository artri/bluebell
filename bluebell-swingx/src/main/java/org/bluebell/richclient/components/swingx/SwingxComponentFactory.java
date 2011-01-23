/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell SwingX.
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
package org.bluebell.richclient.components.swingx;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableModel;

import org.bluebell.richclient.factory.ComponentFactoryDecorator;
import org.jdesktop.swingx.JXTable;
import org.springframework.richclient.factory.ComponentFactory;

/**
 * SwingX based component factory implementation.
 * 
 * @see <a href="https://swingx.dev.java.net/">SwingX</a>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SwingxComponentFactory extends ComponentFactoryDecorator {

    /**
     * Creates de SwingX component factory.
     */
    public SwingxComponentFactory() {

        super();
    }

    /**
     * Creates the SwingX component factory decorator given the decorated component.
     * 
     * @param decoratedComponentFactory
     *            the decorated component factory.
     */
    public SwingxComponentFactory(ComponentFactory decoratedComponentFactory) {

        super(decoratedComponentFactory);
    }

    /**
     * Creates a table.
     * 
     * @return the table.
     * 
     * @see JXTable
     */
    @Override
    public JTable createTable() {

        final JXTable table = new JXTable();

        table.setFillsViewportHeight(Boolean.TRUE);
        table.setColumnControlVisible(Boolean.TRUE);

        // Sizes the scrollpane to be the same size as the table.
        // jTable.setPreferredScrollableViewportSize(jTable.getPreferredSize());

        return table;
    }

    /**
     * Creates a table given its model.
     * 
     * @param tableModel
     *            the table model.
     * @return the table.
     * 
     * @see #createTable()
     * @see JXTable
     */
    @Override
    public JTable createTable(TableModel tableModel) {

        final JTable table = this.createTable();
        table.setModel(tableModel);

        return table;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent createToolBar() {

        final JToolBar toolBar = (JToolBar) this.getDecoratedComponentFactory().createToolBar();
        toolBar.setFloatable(Boolean.TRUE);
        toolBar.setEnabled(Boolean.TRUE);

        return toolBar;
    }
}
