/*
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
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

package org.bluebell.richclient.application.docking.vldocking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.UIManager;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockablePanel;

/**
 * Class that test the correct behaviour of {@link BbDockTabbedPane}.
 * 
 *@author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestBbDockTabbedPane extends TestCase {

    /**
     * The initial number of tabs.
     */
    private static final int SIZE = 5;

    static {
        // Required property for this tests
        UIManager.put("TabbedDockableContainer.tabPlacement", 1);
    }

    /**
     * The container pane for tabs.
     */
    private BbDockTabbedPane tabbedPane;

    /**
     * The tabs.
     */
    private final Dockable[] tabs = new Dockable[TestBbDockTabbedPane.SIZE];

    /**
     * Tests the correct behaviour of basic tabs insertion.
     * 
     * <ul>
     * <li>[]
     * <li>[0,1,2,3,4]
     * </ul>
     */
    public void testSimplestTabInsertion() {

        this.checkCorrectness();
    }

    /**
     * Tests that after havind removed tabs in ascendant order the system is able to add them again in ascendant order
     * keeping the original position.
     * 
     * <ul>
     * <li>[]
     * <li>[0,1,2,3,4]
     * <li>[1,2,3,4]
     * <li>[2,3,4]
     * <li>[3,4]
     * <li>[4]
     * <li>[]
     * <li>[0]
     * <li>[0,1]
     * <li>[0,1,2]
     * <li>[0,1,2,3]
     * <li>[0,1,2,3,4]
     * </ul>
     */
    public void testTabRemovalAndInsertingForeward() {

        // Remove tabs in ascendant order
        for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
            this.tabbedPane.remove(0);
        }

        // Add tabs in ascendant order
        for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {

            TestCase.assertEquals(i, this.getIndexForDockable(i));
            this.insertTab(this.tabs[i], this.getIndexForDockable(i));
        }

        this.checkCorrectness();
    }

    /**
     * Tests that after havind removed tabs in descendant order the system is able to add them again in descendant order
     * keeping the original position.
     * 
     * <ul>
     * <li>[]
     * <li>[0,1,2,3,4]
     * <li>[0,1,2,3]
     * <li>[0,1,2]
     * <li>[0,1]
     * <li>[0]
     * <li>[]
     * <li>[4]
     * <li>[3,4]
     * <li>[2,3,4]
     * <li>[1,2,3,4]
     * <li>[0,1,2,3,4]
     * </ul>
     */
    public void testTabRemovalAndInsertingBackward() {

        // Remove tabs in descendant order
        for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
            this.tabbedPane.remove(this.tabbedPane.getTabCount() - 1);
        }

        // Add tabs in descendant order
        for (int i = TestBbDockTabbedPane.SIZE - 1; i >= 0; --i) {
            TestCase.assertEquals(0, this.getIndexForDockable(i));
            this.insertTab(this.tabs[i], this.getIndexForDockable(i));
        }

        this.checkCorrectness();
    }

    /**
     * Tests that after havind removed tabs in ascendant order the system is able to attach new tabas before adding
     * originals again in ascendant order keeping the original position.
     * 
     * <ul>
     * <li>[]
     * <li>[0,1,2,3,4]
     * <li>[1,2,3,4]
     * <li>[2,3,4]
     * <li>[3,4]
     * <li>[4]
     * <li>[]
     * <li>[0bis]
     * <li>[0bis,1bis]
     * <li>[0bis,1bis,2bis]
     * <li>[0bis,1bis,2bis,3bis]
     * <li>[0bis,1bis,2bis,3bis,4bis]
     * <li>[0bis,1bis,2bis,3bis,4bis,0]
     * <li>[0bis,1bis,2bis,3bis,4bis,0,1]
     * <li>[0bis,1bis,2bis,3bis,4bis,0,1,2]
     * <li>[0bis,1bis,2bis,3bis,4bis,0,1,2,3]
     * <li>[0bis,1bis,2bis,3bis,4bis,0,1,2,3,4]
     * </ul>
     */
    public void testAditionalInsertion() {

        // The attached tabs
        final Dockable[] aditionalTabs = new Dockable[TestBbDockTabbedPane.SIZE];
        for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
            aditionalTabs[i] = this.createDockable(i + "bis");
        }

        // Remove tabs in ascendant order
        for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
            this.tabbedPane.remove(0);
        }

        // Add tabs in ascendant order
        for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
            this.insertTab(aditionalTabs[i], i);
        }

        // Attach tabs in ascendant order
        for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
            TestCase.assertEquals(i + TestBbDockTabbedPane.SIZE, this.getIndexForDockable(i));
            this.insertTab(this.tabs[i], this.getIndexForDockable(i));
        }

        for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
            TestCase.assertEquals(aditionalTabs[i], this.tabbedPane.getComponentAt(i));

            TestCase.assertEquals(this.tabs[i], this.tabbedPane.getComponentAt(i + TestBbDockTabbedPane.SIZE));
        }
    }

    /**
     * Tests multiple removals and additions randomly.
     * 
     * @see #doTestTabRemovalAndInsertingRandom()
     */
    public void testTabRemovalAndInsertingRandom() {

        final int iterations = 10000;
        for (int i = 0; i < iterations; ++i) {
            this.doTestTabRemovalAndInsertingRandom();
        }
    }

    /**
     * Single test operation with tabs removed and added randomly.
     */
    protected void doTestTabRemovalAndInsertingRandom() {

        final List<Integer> removalOrder = new ArrayList<Integer>();
        final List<Integer> insertionOrder = Arrays.asList(new Integer[] { 0, 1, 2, 3, 4 });

        // Remove tabs randomly
        for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
            final int maxValue = TestBbDockTabbedPane.SIZE - i;
            removalOrder.add(i, maxValue != 0 ? RandomUtils.nextInt(maxValue) : 0);
            this.tabbedPane.remove(removalOrder.get(i));
        }

        // Add tabs randomly
        Collections.shuffle(insertionOrder);
        for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
            this.insertTab(this.tabs[insertionOrder.get(i)], this.getIndexForDockable(insertionOrder.get(i)));
        }

        this.checkCorrectness();
    }

    /**
     * Creates a new tab container pane before each test.
     * <p>
     * The pane contains {@value #SIZE} tabs, consisting on a <code>JLabel</code> with text..
     * 
     * @throws Exception
     *             if error.
     */
    @Override
    protected void setUp() throws Exception {

        this.tabbedPane = new BbDockTabbedPane();

        for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
            this.tabs[i] = this.createDockable(((Integer) i).toString());
            this.insertTab(this.tabs[i], i);
        }
    }

    /**
     * Gets the more suitable index for inserting a tab.
     * 
     * @param i
     *            the index (<code>tabs</code> relative) of the target tab.
     * @return the panel relative index.
     */
    private int getIndexForDockable(int i) {

        final int indexForDockable = this.tabbedPane.getIndexForDockable(this.tabs[i]);

        return indexForDockable != -1 ? indexForDockable : 0;
    }

    /**
     * Adds a tab into {@link #tabbedPane} given its position.
     * 
     * @param tab
     *            the tab.
     * @param position
     *            the position.
     */
    private void insertTab(Dockable tab, int position) {

        this.tabbedPane.insertTab(tab.getDockKey().getName(), null, tab.getComponent(), StringUtils.EMPTY, position);
    }

    /**
     * Checks that every tab is in the correct position.
     */
    private void checkCorrectness() {

        for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
            TestCase.assertEquals(i, this.getIndexForDockable(i));
        }
    }

    /**
     * Creates a <code>Dockable</code> given its name.
     * 
     * @param name
     *            the <code>Dockable</code> name.
     * @return the <code>Dockable</code>.
     */
    private Dockable createDockable(String name) {

        final DockKey dockKey = new DockKey(name, name);

        return new DockablePanel(new JLabel(name), dockKey);
    }
}
