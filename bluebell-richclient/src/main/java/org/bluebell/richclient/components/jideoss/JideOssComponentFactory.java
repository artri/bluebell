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

import org.bluebell.richclient.factory.ComponentFactoryDecorator;

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

    
    
    // private JPanel createSearchableTextArea() {
    // final JTextComponent textArea = SearchableBarDemo.createEditor("Readme.txt");
    // final JPanel panel = new JPanel(new BorderLayout());
    // panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
    // Searchable searchable = SearchableUtils.installSearchable(textArea);
    // searchable.setRepeats(true);
    // _textAreaSearchableBar = SearchableBar.install(searchable, KeyStroke.getKeyStroke(KeyEvent.VK_F,
    // KeyEvent.CTRL_DOWN_MASK), new SearchableBar.Installer() {
    // public void openSearchBar(SearchableBar searchableBar) {
    // panel.add(searchableBar, BorderLayout.AFTER_LAST_LINE);
    // panel.invalidate();
    // panel.revalidate();
    // }
    //
    // public void closeSearchBar(SearchableBar searchableBar) {
    // panel.remove(searchableBar);
    // panel.invalidate();
    // panel.revalidate();
    // }
    // });
    // _textAreaSearchableBar.getInstaller().openSearchBar(_textAreaSearchableBar);
    // return panel;
    // }

    // private JPanel createSearchableTable() {
    // JTable table = null;
    // Searchable searchable = SearchableUtils.installSearchable(table);
    // searchable.setRepeats(true);
    // SearchableBar.install(searchable, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), new
    // SearchableBar.Installer() {
    // public void openSearchBar(SearchableBar searchableBar) {
    // panel.add(searchableBar, BorderLayout.AFTER_LAST_LINE);
    // panel.invalidate();
    // panel.revalidate();
    // }
    //
    // public void closeSearchBar(SearchableBar searchableBar) {
    // panel.remove(searchableBar);
    // panel.invalidate();
    // panel.revalidate();
    // }
    // });
    // _tableSearchableBar.setName("TableSearchableBar");
    // return panel;
    // }
}
