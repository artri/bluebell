/**
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
package org.bluebell.richclient.components.swingx;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.springframework.richclient.factory.DefaultComponentFactory;

/**
 * Swingx based component factory implementation.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SwingxComponentFactory extends DefaultComponentFactory {

    /**
     * Creates a table.
     * 
     * @return the table.
     * 
     * @see org.springframework.richclient.factory.ComponentFactory#createTable()
     * @see JXTable
     */
    @Override
    public JTable createTable() {

        final JXTable table = new JXTable();

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
     * @see org.springframework.richclient.factory.ComponentFactory#createTable(TableModel)
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

        final JToolBar toolBar = (JToolBar) super.createToolBar();
        toolBar.setFloatable(Boolean.TRUE);
        toolBar.setEnabled(Boolean.TRUE);

        return toolBar;
    }
}
