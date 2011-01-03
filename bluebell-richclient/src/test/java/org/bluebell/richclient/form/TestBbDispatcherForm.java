/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
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
package org.bluebell.richclient.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import junit.framework.TestCase;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.samples.simple.form.PersonMasterForm;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

/**
 * Tests the correct behaviour of {@link BbDispatcherForm}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration
public class TestBbDispatcherForm extends AbstractBbSamplesTests {

    /**
     * The first list with persons.
     * <p>
     * 
     * <pre>
     * PERSONS_1 ^ PERSONS_2 = {}
     * PERSONS_1 ^ PERSONS_3 = {3, 4}
     * </pre>
     */
    private static final List<Person> PERSONS_1 = new ArrayList<Person>();

    /**
     * The second list with persons.
     * <p>
     * 
     * <pre>
     * PERSONS_2 ^ PERSONS_1 = {}
     * PERSONS_2 ^ PERSONS_3 = {D, E}
     * </pre>
     */
    private static final List<Person> PERSONS_2 = new ArrayList<Person>();

    /**
     * The third list with persons.
     * <p>
     * 
     * <pre>
     * PERSONS_3 ^ PERSONS_1 = {3, 4}
     * PERSONS_3 ^ PERSONS_2 = {D, E}
     * </pre>
     */
    private static final List<Person> PERSONS_3 = new ArrayList<Person>();

    /**
     * The page descriptor used for testing.
     */
    @Autowired
    private PageDescriptor pageDescriptor;

    static {
        TestBbDispatcherForm.PERSONS_1.add(new Person("0"));
        TestBbDispatcherForm.PERSONS_1.add(new Person("1"));
        TestBbDispatcherForm.PERSONS_1.add(new Person("2"));
        TestBbDispatcherForm.PERSONS_1.add(new Person("3"));

        TestBbDispatcherForm.PERSONS_2.add(new Person("A"));
        TestBbDispatcherForm.PERSONS_2.add(new Person("B"));
        TestBbDispatcherForm.PERSONS_2.add(new Person("C"));
        TestBbDispatcherForm.PERSONS_2.add(new Person("D"));

        TestBbDispatcherForm.PERSONS_3.add(new Person("3"));
        TestBbDispatcherForm.PERSONS_3.add(new Person("4"));
        TestBbDispatcherForm.PERSONS_3.add(new Person("D"));
        TestBbDispatcherForm.PERSONS_3.add(new Person("E"));
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testDependencyInjection() {

        TestCase.assertNotNull("pageDescriptor", this.pageDescriptor);
    }

    /**
     * Tests the correct behaviour of detail form integration and associated listeners.
     * <p>
     * This test is related to <a href="http://jirabluebell.b2b2000.com/browse/BLUE-41">BLUE-41 issue</a>
     */
    @Test
    public void testDetailFormAndAssociatedListeners() {

        final PersonMasterForm masterForm = (PersonMasterForm) this.getBackingForm(this.getMasterView());
        final BbDispatcherForm<Person> dispatcherForm = masterForm.getDispatcherForm();
        final AbstractBbChildForm<Person> childForm = this.getBackingForm(this.getChildView());

        this.doTestDetailFormAndAssociatedListeners(masterForm, dispatcherForm);
        this.cleanMasterEventList();
        this.doTestDetailFormAndAssociatedListeners(masterForm, childForm);
    }

    /**
     * Method invoked at startup.
     * <p/>
     * Initializes test cases.
     */
    @Before
    public void startup() {

        this.initializeVariables(this.pageDescriptor);
    }

    /**
     * Cleans master event list after every test execution.
     */
    @SuppressWarnings("unchecked")
    @After
    public void cleanMasterEventList() {

        final AbstractBbTableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        masterForm.showEntities(ListUtils.EMPTY_LIST);
    }

    @SuppressWarnings("unchecked")
    protected void doTestDetailFormAndAssociatedListeners(PersonMasterForm masterForm, AbstractForm formObjectTarget) {

        final Map<CountersListener.CounterName, Integer> expectedCounters = new HashMap<CountersListener.CounterName, Integer>();
        final CountersListener countersListener = new CountersListener(masterForm, formObjectTarget);
        final BbDispatcherForm<Person> dispatcherForm = masterForm.getDispatcherForm();

        List<Person> entities;
        List<Person> selection;

        /*
         * 0. Ensures master table is empty at the beginning
         */
        TestCase.assertTrue("masterForm.getMasterEventList().isEmpty()", masterForm.getMasterEventList().isEmpty());

        /*
         * 1. Show entities.
         * 
         * EXPECTED: [1,2,3,4]
         */
        entities = TestBbDispatcherForm.PERSONS_1;
        CountersListener.increment(expectedCounters, CountersListener.CounterName.TABLE);

        masterForm.showEntities(entities);

        TestCase.assertEquals(expectedCounters, countersListener.getCounters());
        TestBbDispatcherForm.assertDispatcherFormPropagatesChanges(dispatcherForm);

        /*
         * 2.Select a single entity.
         * 
         * EXPECTED: [-->1<--,2,3,4]
         */
        selection = entities.subList(0, 1);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.INDEX_HOLDER);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.FORM_OBJECT);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 3.Select all entities: detail form
         * 
         * EXPECTED: [-->1<--,-->2<--,-->3<--,-->4<--]
         */
        selection = entities;
        CountersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.INDEX_HOLDER);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.FORM_OBJECT); // reset
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 4.Select all entities but last one
         * 
         * EXPECTED: [-->1<--,-->2<--,-->3<--,4]
         */
        selection = entities.subList(0, entities.size() - 1);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 5.Select all entities but first one
         * 
         * EXPECTED: [1,-->2<--,-->3<--,-->4<--]
         */
        selection = entities.subList(1, entities.size());
        CountersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 6.Single selection again
         * 
         * EXPECTED: [1,-->2<--,3,4]
         */
        selection = entities.subList(1, 2);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.INDEX_HOLDER);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.FORM_OBJECT);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 7.Empty selection
         * 
         * EXPECTED: [1,2,3,4]
         */
        selection = ListUtils.EMPTY_LIST;
        CountersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.INDEX_HOLDER);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.FORM_OBJECT);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 8.Multiple selection again
         * 
         * EXPECTED: [-->1<--,-->2<--,-->3<--,-->4<--]
         */
        selection = entities;
        CountersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 9.Single selection of a non present entity.
         * 
         * Two selection changes are expected: The first one when table changes because selection is lost and the second
         * as required
         * 
         * EXPECTED: [1,2,3,4,-->5<--]
         */
        selection = TestBbDispatcherForm.PERSONS_2.subList(0, 1);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.TABLE);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION); // <-- expected
        CountersListener.increment(expectedCounters, CountersListener.CounterName.INDEX_HOLDER);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.FORM_OBJECT);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 10.Multiple selection of non present entities.
         * 
         * Two selection changes are expected: The first one when table changes because selection is lost and the second
         * as required
         * 
         * EXPECTED: [1,2,3,4,5,-->6<--,-->7<--]
         */
        selection = TestBbDispatcherForm.PERSONS_2.subList(1, 3);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.TABLE);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION); // <-- expected
        CountersListener.increment(expectedCounters, CountersListener.CounterName.INDEX_HOLDER);
        CountersListener.increment(expectedCounters, CountersListener.CounterName.FORM_OBJECT);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);
    }

    /**
     * Changes selection and tests everything works as expected:
     * <ul>
     * <li>Counters values are as expected.
     * <li>{@link #assertDispatcherFormPropagatesChanges(BbDispatcherForm)}.
     * </ul>
     * 
     * @param masterForm
     *            the master form.
     * @param countersListener
     *            the counters listener.
     * @param selection
     *            the new selection.
     * @param expectedCounters
     *            the expected counters values.
     */
    private void changeSelectionAndTestAssertions(PersonMasterForm masterForm, CountersListener countersListener,
            List<Person> selection, Map<CountersListener.CounterName, Integer> expectedCounters) {

        final BbDispatcherForm<Person> dispatcherForm = masterForm.getDispatcherForm();

        masterForm.changeSelection(selection);

        TestCase.assertEquals(expectedCounters, countersListener.getCounters());
        TestBbDispatcherForm.assertDispatcherFormPropagatesChanges(dispatcherForm);
    }

    /**
     * Ensures changes are propagated from dispatcher form to every child form.
     * 
     * @param dispatcherForm
     *            the dispatcher form.
     */
    private static void assertDispatcherFormPropagatesChanges(BbDispatcherForm<?> dispatcherForm) {

        Assert.notNull(dispatcherForm, "dispatcherForm");

        for (AbstractBbChildForm<?> childForm : dispatcherForm.getChildForms()) {
            TestCase.assertEquals(dispatcherForm.getFormObject(), childForm.getFormObject());
            TestCase.assertEquals(FormUtils.getSelectedIndex(dispatcherForm), FormUtils.getSelectedIndex(childForm));
            TestCase.assertEquals(FormUtils.isEditingNewFormObject(dispatcherForm),
                    FormUtils.isEditingNewFormObject(childForm));
            TestCase.assertTrue(ListUtils.isEqualList(//
                    FormUtils.getEditableFormObjects(dispatcherForm), FormUtils.getEditableFormObjects(childForm)));
        }
    }

    /**
     * A listener that installed to be installed on a master form.
     * <p>
     * Reports the number of events raised within:
     * <ul>
     * <li>Master table model changes.
     * <li>Master table model selection changes.
     * <li>Dispatcher form editing index holder changes.
     * <li>Child form object changes.
     * </ul>
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private static class CountersListener implements TableModelListener, ListSelectionListener, PropertyChangeListener {

        /**
         * The kind of counters handled by this listener.
         * 
         * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
         */
        public enum CounterName {
            /**
             * Number of <code>tableChanged</code> events on master table.
             */
            TABLE,

            /**
             * Number of <code>listSelection</code> events on master table selection model.
             */
            SELECTION,

            /**
             * Number of <code>propertyChange</code> events on <code>editingIndexHolder</code> of dispatcher form.
             */
            INDEX_HOLDER,

            /**
             * Number of <code>propertyChange</code> events on <code>formObject</code> of child form.
             */
            FORM_OBJECT
        };

        /**
         * The counter handled by this class.
         */
        private Map<CounterName, Integer> counters = new HashMap<CounterName, Integer>();

        /**
         * Creates the listener that listen for master form events.
         * 
         * @param masterFormTarget
         *            the target form.
         */
        public CountersListener(AbstractB2TableMasterForm<?> masterForm, AbstractForm formObjectTarget) {

            Assert.notNull(masterForm, "masterForm");
            Assert.notNull(formObjectTarget, "formObjectTarget");
            Assert.notNull(masterForm.getDispatcherForm(), "masterForm.getDispatcherForm()");

            masterForm.getMasterTableModel().addTableModelListener(this);
            masterForm.getMasterTable().getSelectionModel().addListSelectionListener(this);
            masterForm.getDispatcherForm().getEditingIndexHolder().addPropertyChangeListener("value", this);
            formObjectTarget.addFormObjectChangeListener(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void tableChanged(TableModelEvent e) {

            CountersListener.increment(this.getCounters(), CounterName.TABLE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {

            if (!e.getValueIsAdjusting()) {
                CountersListener.increment(this.getCounters(), CounterName.SELECTION);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {

            final Object newValue = evt.getNewValue();

            if (newValue instanceof Integer) {
                CountersListener.increment(this.getCounters(), CounterName.INDEX_HOLDER);
            } else {
                CountersListener.increment(this.getCounters(), CounterName.FORM_OBJECT);
            }
        }

        /**
         * Gets the counters.
         * 
         * @return the counters.
         */
        protected final Map<CounterName, Integer> getCounters() {

            return this.counters;
        }

        /**
         * Get the count value for a given counter, never mind if counter is not already defined, in such a case returns
         * 0.
         * 
         * @param counters
         *            the map.
         * @param counterName
         *            the counter name.
         * @return the new counter value.
         */
        public static Integer getCount(Map<CounterName, Integer> counters, CounterName counterName) {

            return MapUtils.getInteger(counters, counterName, 0);
        }

        /**
         * Increments a counter into a given map, never mind if counter is not already defined.
         * 
         * @param counters
         *            the map.
         * @param counterName
         *            the counter name.
         * @return the new counter value.
         */
        public static Integer increment(Map<CounterName, Integer> counters, CounterName counterName) {

            Integer count = CountersListener.getCount(counters, counterName);

            counters.put(counterName, ++count);

            return count;
        }
    }
}
