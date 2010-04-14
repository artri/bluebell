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

package org.bluebell.richclient.table.support;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;

import org.apache.commons.collections.CollectionUtils;
import org.bluebell.richclient.swing.util.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.richclient.table.support.GlazedTableModel;
import org.springframework.richclient.util.Assert;
import org.springframework.util.ReflectionUtils;

import ca.odell.glazedlists.EventList;

/**
 * Utility class for dealing with tables: index translation (view and model), selection...
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class TableUtils {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TableUtils.class);

    /**
     * The field with the source of the <code>GlazedTableModel</code>.
     */
    private static Field glazedTableModelSourceField;

    static {
        TableUtils.glazedTableModelSourceField = //
        ReflectionUtils.findField(GlazedTableModel.class, "source", EventList.class);
        ReflectionUtils.makeAccessible(TableUtils.glazedTableModelSourceField);
    }

    /**
     * Gets the source event list of a <code>GlazedTableModel</code> using reflection.
     * 
     * @param <Q>
     *            the type of elements of the event list.
     * @param tableModel
     *            the table model.
     * @return the source event list.
     */
    @SuppressWarnings("unchecked")
    private static <Q> EventList<Q> getSource(GlazedTableModel tableModel) {

        try {
            return (EventList<Q>) TableUtils.glazedTableModelSourceField.get(tableModel);
        } catch (IllegalArgumentException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Utility classes should not have a public or default constructor.
     */
    private TableUtils() {

        super();
    }

    /**
     * Gets the view index for a given model index.
     * 
     * @param table
     *            the table.
     * @param modelIndex
     *            the model index.
     * @return the view index.
     * 
     * @see #getModelIndex(JTable, Integer)
     */
    public static Integer getViewIndex(JTable table, Integer modelIndex) {

        Assert.notNull(table, "table");
        Assert.notNull(modelIndex, "modelIndex");

        if (modelIndex < 0) {
            return modelIndex;
        } else {
            return table.convertRowIndexToView(modelIndex);
        }
    }

    /**
     * Gets the view indexes for the given model indexes.
     * 
     * @param table
     *            the table.
     * @param modelIndexes
     *            the model indexes.
     * @return the view indexes.
     * 
     * @see #getViewIndex(JTable, Integer)
     * @see #getModelIndexes(JTable, List)
     */
    public static List<Integer> getViewIndexes(JTable table, List<Integer> modelIndexes) {

        Assert.notNull(table, "table");
        Assert.notNull(modelIndexes, "modelIndexes");

        final List<Integer> viewIndexes = new ArrayList<Integer>(modelIndexes.size());
        for (int i = 0; i < modelIndexes.size(); ++i) {
            viewIndexes.add(TableUtils.getViewIndex(table, modelIndexes.get(i)));
        }

        return viewIndexes;
    }

    /**
     * Gets the model index for a given row index.
     * 
     * @param table
     *            the table.
     * @param viewIndex
     *            the view index.
     * @return the model index.
     * 
     * @see #getViewIndex(JTable, Integer)
     */
    public static Integer getModelIndex(JTable table, Integer viewIndex) {

        Assert.notNull(table, "table");
        Assert.notNull(viewIndex, "viewIndex");

        if (viewIndex < 0) {
            return viewIndex;
        } else {
            return table.convertRowIndexToModel(viewIndex);
        }
    }

    /**
     * Gets the model indexes for the given row indexes.
     * 
     * @param table
     *            the table.
     * @param viewIndexes
     *            the view indexes.
     * @return the model indexes.
     * 
     * @see #getModelIndex(JTable, Integer)
     * @see #getViewIndexes(JTable, List)
     */
    public static List<Integer> getModelIndexes(JTable table, List<Integer> viewIndexes) {

        Assert.notNull(table, "table");
        Assert.notNull(viewIndexes, "viewIndexes");

        final List<Integer> modelIndexes = new ArrayList<Integer>(viewIndexes.size());
        for (int i = 0; i < viewIndexes.size(); ++i) {
            modelIndexes.add(TableUtils.getModelIndex(table, viewIndexes.get(i)));
        }

        return modelIndexes;
    }

    /**
     * Gets the model indexes for the given rows.
     * 
     * @param <Q>
     *            the type of the rows.
     * @param tableModel
     *            the table model.
     * @param entities
     *            the entities to be queried.
     * @return the model indexes.
     */
    public static <Q> List<Integer> getModelIndexes(GlazedTableModel tableModel, List<Q> entities) {

        Assert.notNull(tableModel, "tableModel");
        Assert.notNull(entities, "entities");

        final EventList<Q> eventList = TableUtils.getSource(tableModel);
        final List<Integer> modelIndexes = new ArrayList<Integer>(entities.size());

        for (int i = 0; i < entities.size(); ++i) {
            modelIndexes.add(eventList.indexOf(entities.get(i)));
        }

        return modelIndexes;
    }

    /**
     * Gets the selected rows view indexes.
     * 
     * @param table
     *            the table.
     * 
     * @return the view indexes.
     */
    public static List<Integer> getSelectedViewIndexes(JTable table) {

        Assert.notNull(table, "table");

        final List<Integer> viewIndexes = new ArrayList<Integer>();
        final ListSelectionModel selectionModel = table.getSelectionModel();

        // There should be unless one selected entity
        if (selectionModel.isSelectionEmpty()) {
            return viewIndexes;
        }

        // Iterate between selected entities
        final int min = selectionModel.getMinSelectionIndex();
        final int max = selectionModel.getMaxSelectionIndex();

        for (int idx = min; idx <= max; ++idx) {
            if (selectionModel.isSelectedIndex(idx)) {
                viewIndexes.add(idx);
            }
        }

        return viewIndexes;
    }

    /**
     * Gets the selected rows model indexes.
     * 
     * @param table
     *            the table.
     * 
     * @return the model indexes.
     */
    public static List<Integer> getSelectedModelIndexes(JTable table) {

        Assert.notNull(table, "table");

        final List<Integer> viewIndexes = TableUtils.getSelectedViewIndexes(table);
        final List<Integer> modelIndexes = TableUtils.getModelIndexes(table, viewIndexes);

        return modelIndexes;
    }

    /**
     * Gets the entities currently being shown.
     * 
     * @param <Q>
     *            the type of the rows.
     * @param table
     *            the table.
     * 
     * @return the visible entities.
     */
    @SuppressWarnings("unchecked")
    public static <Q> List<Q> getVisibleEntities(JTable table) {

        Assert.notNull(table, "table");
        Assert.notNull(table.getModel(), "table.getModel()");
        Assert.isTrue(table.getModel() instanceof GlazedTableModel, "table.getModel() instanceof GlazedTableModel");

        final GlazedTableModel tableModel = (GlazedTableModel) table.getModel();

        final List<Q> rows = new ArrayList<Q>(table.getRowCount());
        for (int i = 0; i < table.getRowCount(); ++i) {

            // JAF, 20100411, fixed a bug retrieving elements: rows.add((Q) tableModel.getElementAt(i));
            rows.add((Q) tableModel.getElementAt(TableUtils.getModelIndex(table, i)));
        }

        return rows;
    }

    /**
     * Show the given entities in a table.
     * <p>
     * When dealing with <code>EventList</code> performance is important. This method ensures table model events are
     * launched just once at the end.
     * 
     * @param <Q>
     *            the type of the rows.
     * @param tableModel
     *            the table model.
     * @param entities
     *            the entities to be shown.
     * @param attach
     *            whether to attach new entities to currents. If <code>false</code> currents are replaced.
     */
    @SuppressWarnings("unchecked")
    public static <Q> void showEntities(GlazedTableModel tableModel, final List<Q> entities, final Boolean attach) {

        Assert.notNull(tableModel, "tableModel");
        Assert.notNull(entities, "entities");
        Assert.notNull(attach, "attach");

        final EventList<Q> eventList = TableUtils.getSource(tableModel);

        // Proceed only if ...
        final Boolean proceed;
        if (attach) {
            // ... must attach and entities are not contained in eventList or...
            proceed = !CollectionUtils.isSubCollection(entities, eventList);
        } else {
            // ... must replace and entities are not equals to eventList
            proceed = !CollectionUtils.isEqualCollection(entities, eventList);
        }

        if (proceed) {

            if (TableUtils.LOGGER.isDebugEnabled()) {
                TableUtils.LOGGER.debug("About to show entities " + entities);
            }

            // Avoid notifying in every single change, instead do it at the end
            final TableModelListener[] listeners = tableModel.getTableModelListeners();
            for (final TableModelListener listener : listeners) {
                tableModel.removeTableModelListener(listener);
            }

            SwingUtils.runInEventDispatcherThread(new Runnable() {

                @Override
                public void run() {

                    // Update the contents of the event list
                    eventList.getReadWriteLock().writeLock().lock();
                    try {
                        if (!attach) {
                            eventList.clear();
                        }

                        eventList.addAll(eventList.size(), CollectionUtils.subtract(entities, eventList));
                    } finally {
                        // Since Swing is multithread we need to lock before and unlock later
                        // http://sites.google.com/site/glazedlists/documentation/faq
                        eventList.getReadWriteLock().writeLock().unlock();
                    }
                }
            });

            // Enable notifying: install listeners again
            for (final TableModelListener listener : listeners) {
                tableModel.addTableModelListener(listener);
            }

            // Since listeners were uninstalled notification should be explicit
            tableModel.fireTableDataChanged();
        }
    }

    /**
     * Gets the current selection.
     * <p>
     * Ensures entities are returned in the same order that are viewed.
     * 
     * @param <Q>
     *            the type of the rows.
     * @param table
     *            the table.
     * @param tableModel
     *            the table model.
     * @return the selected entities.
     */
    public static <Q> List<Q> getSelection(JTable table, GlazedTableModel tableModel) {

        Assert.notNull(table, "table");
        Assert.notNull(tableModel, "tableModel");

        final EventList<Q> eventList = TableUtils.getSource(tableModel);
        final List<Integer> modelIndexes = TableUtils.getSelectedModelIndexes(table);
        final List<Q> selectedEntities = new ArrayList<Q>(modelIndexes.size());

        for (Integer modelIndex : modelIndexes) {
            selectedEntities.add(eventList.get(modelIndex));
        }

        return selectedEntities;
    }

    /**
     * Changes selection.
     * 
     * @param <Q>
     *            the type of the rows.
     * @param table
     *            the table.
     * @param tableModel
     *            the table model.
     * @param newSelection
     *            the entities to be selected.
     */
    public static <Q> void changeSelection(final JTable table, final GlazedTableModel tableModel,
            final List<Q> newSelection) {

        Assert.notNull(table, "table");
        Assert.notNull(tableModel, "tableModel");
        Assert.notNull(newSelection, "entities");

        // Replace current selection (do it in the event dispatcher thread to avoid race conditions)
        SwingUtils.runInEventDispatcherThread(new Runnable() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {

                final List<Q> currentSelection = TableUtils.getSelection(table, tableModel);
                final Boolean proceed = !CollectionUtils.isEqualCollection(currentSelection, newSelection);

                if (proceed) {
                    final List<Q> visibleEntities = TableUtils.getVisibleEntities(table);

                    table.clearSelection();

                    for (final Q entity : newSelection) {
                        final int viewIndexToSelect = visibleEntities.indexOf(entity);
                        if (viewIndexToSelect >= 0) {
                            table.addRowSelectionInterval(viewIndexToSelect, viewIndexToSelect);
                        }
                    }
                }
            }
        });
    }

    /**
     * Change current selection.
     * 
     * @param <Q>
     *            the type of the rows.
     * @param table
     *            the table.
     * @param viewIndexes
     *            the indexes to select.
     */
    public static <Q> void changeSelection(final JTable table, final List<Integer> viewIndexes) {

        Assert.notNull(table, "table");
        Assert.notNull(viewIndexes, "viewIndexes");

        // Replace current selection (do it in the event dispatcher thread to avoid race conditions)
        SwingUtils.runInEventDispatcherThread(new Runnable() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {

                final List<Integer> currentViewIndexes = TableUtils.getSelectedViewIndexes(table);
                final Boolean proceed = !CollectionUtils.isEqualCollection(currentViewIndexes, viewIndexes);

                if (proceed) {

                    table.clearSelection();

                    for (final Integer viewIndex : viewIndexes) {
                        if (viewIndex >= 0) {
                            table.addRowSelectionInterval(viewIndex, viewIndex);
                        }
                    }
                }
            }
        });
    }
}
