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
package org.bluebell.richclient.factory;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.table.TableModel;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.application.support.ApplicationServicesAccessor;
import org.springframework.richclient.factory.ComponentFactory;
import org.springframework.richclient.util.Alignment;
import org.springframework.util.Assert;

/**
 * <code>ComponentFactory</code> implementation that employs a decorator approach.
 * <p/>
 * Every method has a default implementation that delegates execution into the decorated factory.
 * 
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public abstract class ComponentFactoryDecorator extends ApplicationServicesAccessor implements ComponentFactory,
        InitializingBean {

    /**
     * The decorated component factory.
     */
    private ComponentFactory decoratedComponentFactory;

    /**
     * Creates de component factory decorator.
     */
    public ComponentFactoryDecorator() {

        super();
    }

    /**
     * Creates the component factory decorator given the decorated component.
     * 
     * @param decoratedComponentFactory
     *            the decorated component factory.
     */
    public ComponentFactoryDecorator(ComponentFactory decoratedComponentFactory) {

        this.setDecoratedComponentFactory(decoratedComponentFactory);
    }

    /**
     * Gets the decorated component factory.
     * 
     * @return the decorated component factory.
     */
    public ComponentFactory getDecoratedComponentFactory() {

        return this.decoratedComponentFactory;
    }

    /**
     * Sets the decorated component factory.
     * 
     * @param decoratedComponentFactory
     *            the decorated component factory to set.
     */
    public void setDecoratedComponentFactory(ComponentFactory decoratedComponentFactory) {

        Assert.notNull(decoratedComponentFactory, "decoratedComponentFactory");

        this.decoratedComponentFactory = decoratedComponentFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Assert.notNull(this.getDecoratedComponentFactory(), "this.getDecoratedComponentFactory()");
    }

    /*
     * Delegate methods
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void addConfiguredTab(JTabbedPane tabbedPane, String labelKey, JComponent tabComponent) {

        this.getDecoratedComponentFactory().addConfiguredTab(tabbedPane, labelKey, tabComponent);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void configureForEnum(JComboBox comboBox, Class enumType) {

        this.getDecoratedComponentFactory().configureForEnum(comboBox, enumType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JButton createButton(String labelKey) {

        return this.getDecoratedComponentFactory().createButton(labelKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCheckBox createCheckBox(String labelKey) {

        return this.getDecoratedComponentFactory().createCheckBox(labelKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JCheckBox createCheckBox(String[] labelKeys) {

        return this.getDecoratedComponentFactory().createCheckBox(labelKeys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComboBox createComboBox() {

        return this.getDecoratedComponentFactory().createComboBox();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public JComboBox createComboBox(Class enumType) {

        return this.getDecoratedComponentFactory().createComboBox(enumType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JFormattedTextField createFormattedTextField(AbstractFormatterFactory formatterFactory) {

        return this.getDecoratedComponentFactory().createFormattedTextField(formatterFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JLabel createLabel(String labelKey) {

        return this.getDecoratedComponentFactory().createLabel(labelKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JLabel createLabel(String[] labelKeys) {

        return this.getDecoratedComponentFactory().createLabel(labelKeys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JLabel createLabel(String labelKey, Object[] arguments) {

        return this.getDecoratedComponentFactory().createLabel(labelKey, arguments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JLabel createLabel(String labelKey, ValueModel[] argumentValueHolders) {

        return this.getDecoratedComponentFactory().createLabel(labelKey, argumentValueHolders);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JLabel createLabelFor(String labelKey, JComponent comp) {

        return this.getDecoratedComponentFactory().createLabelFor(labelKey, comp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JLabel createLabelFor(String[] labelKeys, JComponent comp) {

        return this.getDecoratedComponentFactory().createLabelFor(labelKeys, comp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent createLabeledSeparator(String labelKey) {

        return this.getDecoratedComponentFactory().createLabeledSeparator(labelKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent createLabeledSeparator(String labelKey, Alignment alignment) {

        return this.getDecoratedComponentFactory().createLabeledSeparator(labelKey, alignment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JList createList() {

        return this.getDecoratedComponentFactory().createList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComboBox createListValueModelComboBox(ValueModel selectedItemValueModel,
            ValueModel selectableItemsListHolder, String renderedPropertyPath) {

        return this.getDecoratedComponentFactory().createListValueModelComboBox(//
                selectedItemValueModel, selectableItemsListHolder, renderedPropertyPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JMenuItem createMenuItem(String labelKey) {

        return this.getDecoratedComponentFactory().createMenuItem(labelKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JPanel createPanel() {

        return this.getDecoratedComponentFactory().createPanel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JPanel createPanel(LayoutManager layoutManager) {

        return this.getDecoratedComponentFactory().createPanel(layoutManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JPasswordField createPasswordField() {

        return this.getDecoratedComponentFactory().createPasswordField();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JRadioButton createRadioButton(String labelKey) {

        return this.getDecoratedComponentFactory().createRadioButton(labelKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JRadioButton createRadioButton(String[] labelKeys) {

        return this.getDecoratedComponentFactory().createRadioButton(labelKeys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JScrollPane createScrollPane() {

        return this.getDecoratedComponentFactory().createScrollPane();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JScrollPane createScrollPane(Component view) {

        return this.getDecoratedComponentFactory().createScrollPane(view);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JScrollPane createScrollPane(Component view, int vsbPolicy, int hsbPolicy) {

        return this.getDecoratedComponentFactory().createScrollPane(view, vsbPolicy, hsbPolicy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JTabbedPane createTabbedPane() {

        return this.getDecoratedComponentFactory().createTabbedPane();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JTable createTable() {

        return this.getDecoratedComponentFactory().createTable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JTable createTable(TableModel model) {

        return this.getDecoratedComponentFactory().createTable(model);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JTextArea createTextArea() {

        return this.getDecoratedComponentFactory().createTextArea();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JTextArea createTextArea(int row, int columns) {

        return this.getDecoratedComponentFactory().createTextArea(row, columns);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JTextArea createTextAreaAsLabel() {

        return this.getDecoratedComponentFactory().createTextAreaAsLabel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JTextField createTextField() {

        return this.getDecoratedComponentFactory().createTextField();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JLabel createTitleLabel(String labelKey) {

        return this.getDecoratedComponentFactory().createTitleLabel(labelKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent createTitledBorderFor(String labelKey, JComponent comp) {

        return this.getDecoratedComponentFactory().createTitledBorderFor(labelKey, comp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JToggleButton createToggleButton(String labelKey) {

        return this.getDecoratedComponentFactory().createToggleButton(labelKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JToggleButton createToggleButton(String[] labelKeys) {

        return this.getDecoratedComponentFactory().createToggleButton(labelKeys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent createToolBar() {

        return this.getDecoratedComponentFactory().createToolBar();
    }
}
