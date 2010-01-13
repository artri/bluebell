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
package org.bluebell.richclient.components.jideoss;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

import org.bluebell.richclient.factory.ComponentFactoryDecorator;
import org.springframework.richclient.factory.ComponentFactory;

import com.jidesoft.swing.SearchableUtils;
import com.jidesoft.swing.TableSearchable;

/**
 * Jide OSS based component factory implementation.
 * 
 * @see <a href="https://jide-oss.dev.java.net/">Jide OSS</a>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class JideOssComponentFactory extends ComponentFactoryDecorator {

    /**
     * Creates de Jide OSS component factory.
     */
    public JideOssComponentFactory() {

        super();
    }

    /**
     * Creates the Jide OSS component factory decorator given the decorated component.
     * 
     * @param decoratedComponentFactory
     *            the decorated component factory.
     */
    public JideOssComponentFactory(ComponentFactory decoratedComponentFactory) {

        super(decoratedComponentFactory);
    }

    /**
     * Creates a combobox and installs a <code>Searchable</code> on it.
     * 
     * @return the combobox.
     */
    @Override
    public JComboBox createComboBox() {

        return this.installSearchable(super.createComboBox());
    }

    /**
     * Creates a combobox for an enum type and installs a <code>Searchable</code> on it.
     * 
     * @return the combobox.
     */
    @SuppressWarnings("unchecked")
    @Override
    public JComboBox createComboBox(Class enumType) {

        return this.installSearchable(super.createComboBox(enumType));
    }

    /**
     * Creates a formatted text field and installs a <code>Searchable</code> on it.
     * 
     * @return the table.
     */
    @Override
    public JFormattedTextField createFormattedTextField(AbstractFormatterFactory formatterFactory) {

        return this.installSearchable(super.createFormattedTextField(formatterFactory));
    }

    /**
     * Creates a list and installs a <code>Searchable</code> on it.
     * 
     * @return the list.
     */
    @Override
    public JList createList() {

        return this.installSearchable(super.createList());
    }

    /**
     * Creates a table and installs a <code>Searchable</code> on it.
     * 
     * @return the table.
     */
    @Override
    public JTable createTable() {

        return this.installSearchable(super.createTable());
    }

    /**
     * Creates a table given its model and installs a <code>Searchable</code> on it.
     * 
     * @param tableModel
     *            the table model.
     * @return the table.
     */
    @Override
    public JTable createTable(TableModel tableModel) {

        return this.installSearchable(super.createTable(tableModel));
    }

    /**
     * Creates a text area and installs a <code>Searchable</code> on it.
     * 
     * @return the text area.
     */
    @Override
    public JTextArea createTextArea() {

        return this.installSearchable(super.createTextArea());
    }

    /**
     * Creates a text area and installs a <code>Searchable</code> on it.
     * 
     * @param row
     *            the number of rows.
     * @param columns
     *            the number of columns.
     * 
     * @return the text area.
     */
    @Override
    public JTextArea createTextArea(int row, int columns) {

        return this.installSearchable(super.createTextArea(row, columns));
    }

    /**
     * Creates a text area as label and installs a <code>Searchable</code> on it.
     * 
     * @return the text area.
     */
    @Override
    public JTextArea createTextAreaAsLabel() {

        return this.installSearchable(super.createTextAreaAsLabel());
    }

    /**
     * Installs a <code>Searchable</code> into the given combobox.
     * 
     * @param combobox
     *            the target combobox.
     * @return the target combobox.
     */
    private JComboBox installSearchable(JComboBox comboBox) {

        SearchableUtils.installSearchable(comboBox);

        return comboBox;
    }

    /**
     * Installs a <code>Searchable</code> into the given list.
     * 
     * @param list
     *            the target list.
     * @return the target list.
     */
    private JList installSearchable(JList list) {

        SearchableUtils.installSearchable(list);

        return list;
    }

    /**
     * Installs a <code>Searchable</code> into the given table.
     * 
     * @param table
     *            the target table.
     * @return the target table.
     */
    private JTable installSearchable(JTable table) {

        final TableSearchable tableSearchable = SearchableUtils.installSearchable(table);

        // Search for all columns
        tableSearchable.setMainIndex(-1);

        return table;
    }

    /**
     * Installs a <code>Searchable</code> into the given text component.
     * 
     * @param textComponent
     *            the target text component.
     * @return the text component.
     */
    private <T extends JTextComponent> T installSearchable(T textComponent) {

        SearchableUtils.installSearchable(textComponent);

        return textComponent;
    }
    
//    private JPanel createSearchableTextArea() {
//        final JTextComponent textArea = SearchableBarDemo.createEditor("Readme.txt");
//        final JPanel panel = new JPanel(new BorderLayout());
//        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
//        Searchable searchable = SearchableUtils.installSearchable(textArea);
//        searchable.setRepeats(true);
//        _textAreaSearchableBar = SearchableBar.install(searchable, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), new SearchableBar.Installer() {
//            public void openSearchBar(SearchableBar searchableBar) {
//                panel.add(searchableBar, BorderLayout.AFTER_LAST_LINE);
//                panel.invalidate();
//                panel.revalidate();
//            }
//
//            public void closeSearchBar(SearchableBar searchableBar) {
//                panel.remove(searchableBar);
//                panel.invalidate();
//                panel.revalidate();
//            }
//        });
//        _textAreaSearchableBar.getInstaller().openSearchBar(_textAreaSearchableBar);
//        return panel;
//    }

//    private JPanel createSearchableTable() {
//        JTable table = null;
//        Searchable searchable = SearchableUtils.installSearchable(table);
//        searchable.setRepeats(true);
//        SearchableBar.install(searchable, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), new SearchableBar.Installer() {
//            public void openSearchBar(SearchableBar searchableBar) {
//                panel.add(searchableBar, BorderLayout.AFTER_LAST_LINE);
//                panel.invalidate();
//                panel.revalidate();
//            }
//
//            public void closeSearchBar(SearchableBar searchableBar) {
//                panel.remove(searchableBar);
//                panel.invalidate();
//                panel.revalidate();
//            }
//        });
//        _tableSearchableBar.setName("TableSearchableBar");
//        return panel;
//    }
}
