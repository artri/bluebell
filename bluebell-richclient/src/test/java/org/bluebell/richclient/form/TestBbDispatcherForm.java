/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
 * 
 * This file is part of Bluebell Rich Client.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

/**
 * 
 */
package org.bluebell.richclient.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import junit.framework.TestCase;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.bluebell.richclient.form.TestBbDispatcherForm.CountersListener.CounterName;
import org.bluebell.richclient.form.builder.support.DirtyTrackingUtils;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.samples.simple.form.PersonMasterForm;
import org.bluebell.richclient.swing.util.SwingUtils;
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

//    @Test
//    public void testfoo() {
//
//        SwingUtils.runInEventDispatcherThread(new Runnable() {
//
//            @Override
//            public void run() {
//
//                TestCase.fail();
//            }
//        });
//    }

    /**
     * Tests the correct behaviour of dispatcher form and child forms synchronization.
     * <p>
     * This test is related to <a href="http://jirabluebell.b2b2000.com/browse/BLUE-41">BLUE-41 issue</a>
     */
    @Test
    public void testDispatching() {

        final PersonMasterForm masterForm = (PersonMasterForm) this.getBackingForm(this.getMasterView());
        final BbDispatcherForm<Person> dispatcherForm = masterForm.getDispatcherForm();
        final AbstractBbChildForm<Person> childForm = this.getBackingForm(this.getChildView());

        this.doTestDispatching(masterForm, new CountersListener(masterForm, dispatcherForm, "name"));
        this.cleanMasterEventList();
        this.doTestDispatching(masterForm, new CountersListener(masterForm, childForm, "name"));
    }

    /**
     * Tests the correct behaviour of commit process according to <a
     * href="http://jirabluebell.b2b2000.com/browse/BLUE-22">BLUE-22</a>.
     */
    @Test
    public void testCommit() {

        final PersonMasterForm masterForm = (PersonMasterForm) this.getBackingForm(this.getMasterView());
        final BbDispatcherForm<Person> dispatcherForm = masterForm.getDispatcherForm();
        final AbstractBbChildForm<Person> childForm = this.getBackingForm(this.getChildView());

        // Test insert operations listening to dispatcher form and child form respectively
        this.doTestCommit(masterForm, childForm, new CountersListener(masterForm, dispatcherForm), Boolean.TRUE);
        this.cleanMasterEventList();
        this.doTestCommit(masterForm, childForm, new CountersListener(masterForm, childForm, "name"), Boolean.TRUE);
        this.cleanMasterEventList();

        // Test update operations listening to dispatcher form and child form respectively
        this.doTestCommit(masterForm, childForm, new CountersListener(masterForm, dispatcherForm), Boolean.FALSE);
        this.cleanMasterEventList();
        this.doTestCommit(masterForm, childForm, new CountersListener(masterForm, childForm, "name"), Boolean.FALSE);
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

        final AbstractB2TableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        final BbDispatcherForm<Person> dispatcherForm = masterForm.getDispatcherForm();

        DirtyTrackingUtils.clearDirty(dispatcherForm.getFormModel());
        masterForm.showEntities(ListUtils.EMPTY_LIST);
    }

    /**
     * Makes different types of selections and tests that dispatcher form and child forms are synchronized.
     * 
     * @param masterForm
     *            the master form.
     * @param countersListener
     *            the counter listener to be employed.
     */
    @SuppressWarnings("unchecked")
    protected void doTestDispatching(PersonMasterForm masterForm, CountersListener countersListener) {

        final Map<CountersListener.CounterName, Integer> expectedCounters = new HashMap<CountersListener.CounterName, Integer>();
        final BbDispatcherForm<Person> dispatcherForm = masterForm.getDispatcherForm();

        List<Person> entities;
        List<Person> selection;

        /*
         * 0. Ensures master table is empty at the beginning
         */
        TestCase.assertTrue("masterForm.getMasterEventList().isEmpty()", masterForm.getMasterEventList().isEmpty());
        TestBbDispatcherForm.assertDispatcherFormPropagatesChanges(dispatcherForm);

        /*
         * 1. Show entities.
         * 
         * EXPECTED: [1,2,3,4]
         */
        entities = TestBbDispatcherForm.PERSONS_1;
        countersListener.increment(expectedCounters, CounterName.TABLE);

        masterForm.showEntities(entities);

        TestCase.assertEquals(expectedCounters, countersListener.getCounters());
        TestBbDispatcherForm.assertDispatcherFormPropagatesChanges(dispatcherForm);

        /*
         * 2.Select a single entity.
         * 
         * EXPECTED: [-->1<--,2,3,4]
         */
        selection = entities.subList(0, 1);
        countersListener.increment(expectedCounters, //
                CounterName.SELECTION, CounterName.INDEX_HOLDER, CounterName.FORM_OBJECT, CounterName.VALUE_MODEL);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 3.Select all entities: child form
         * 
         * EXPECTED: [-->1<--,-->2<--,-->3<--,-->4<--]
         */
        selection = entities;
        countersListener.increment(expectedCounters, // form object is incremented due to a form reset operation
                CounterName.SELECTION, CounterName.INDEX_HOLDER, CounterName.FORM_OBJECT, CounterName.VALUE_MODEL);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 4.Select all entities but last one
         * 
         * EXPECTED: [-->1<--,-->2<--,-->3<--,4]
         */
        selection = entities.subList(0, entities.size() - 1);
        countersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 5.Select all entities but first one
         * 
         * EXPECTED: [1,-->2<--,-->3<--,-->4<--]
         */
        selection = entities.subList(1, entities.size());
        countersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 6.Single selection again
         * 
         * EXPECTED: [1,-->2<--,3,4]
         */
        selection = entities.subList(1, 2);
        countersListener.increment(expectedCounters, //
                CounterName.SELECTION, CounterName.INDEX_HOLDER, CounterName.FORM_OBJECT, CounterName.VALUE_MODEL);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 7.Empty selection
         * 
         * EXPECTED: [1,2,3,4]
         */
        selection = ListUtils.EMPTY_LIST;
        countersListener.increment(expectedCounters, //
                CounterName.SELECTION, CounterName.INDEX_HOLDER, CounterName.FORM_OBJECT, CounterName.VALUE_MODEL);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);

        /*
         * 8.Multiple selection again
         * 
         * EXPECTED: [-->1<--,-->2<--,-->3<--,-->4<--]
         */
        selection = entities;
        countersListener.increment(expectedCounters, CountersListener.CounterName.SELECTION);
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
        countersListener.increment(expectedCounters, CounterName.TABLE, CounterName.SELECTION,// 2 selects are expected
                CounterName.SELECTION, CounterName.INDEX_HOLDER, CounterName.FORM_OBJECT, CounterName.VALUE_MODEL);
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
        countersListener.increment(expectedCounters, CounterName.TABLE, CounterName.SELECTION,// 2 selects are expected
                CounterName.SELECTION, CounterName.INDEX_HOLDER, CounterName.FORM_OBJECT, CounterName.VALUE_MODEL);
        this.changeSelectionAndTestAssertions(masterForm, countersListener, selection, expectedCounters);
    }

    /**
     * Test commit behaviour on insert and update operations.
     * 
     * @param masterForm
     *            the master form.
     * @param childForm
     *            the child form.
     * @param countersListener
     *            the counter listener to be employed.
     * @param inserting
     *            wheter test should <em>insert</em> (<code>true</code>) or <em>update</em> (<code>false</code>) an
     *            entity.
     */
    protected void doTestCommit(final PersonMasterForm masterForm, AbstractBbChildForm<Person> childForm,
            CountersListener countersListener, Boolean inserting) {

        Assert.notNull(masterForm, "masterForm");
        Assert.notNull(childForm, "childForm");
        Assert.notNull(countersListener, "countersListener");
        Assert.notNull(inserting, "insert");

        final String name = "JAF";
        final String age = "28";
        final Map<CountersListener.CounterName, Integer> expectedCounters = new HashMap<CountersListener.CounterName, Integer>();
        final BbDispatcherForm<Person> dispatcherForm = masterForm.getDispatcherForm();

        /*
         * 0. Ensures master table is empty at the beginning
         */
        TestCase.assertTrue("masterForm.getMasterEventList().isEmpty()", masterForm.getMasterEventList().isEmpty());

        // 1.Insert or update
        TestCase.assertFalse("dispatcherForm.isEditingNewFormObject()", dispatcherForm.isEditingNewFormObject());
        TestCase.assertFalse("childForm.isEditingNewFormObject()", childForm.isEditingNewFormObject());

        if (inserting) {
            // 1a.Execute newFormObjectCommand
            SwingUtils.runInEventDispatcherThread(new Runnable() {

                @Override
                public void run() {

                    masterForm.getNewFormObjectCommand().execute();
                }
            });

            countersListener.increment(expectedCounters, CountersListener.CounterName.FORM_OBJECT);
        } else {
            // 1b.Select an existing entity (#changeSelection ensures operation is done in the EDT)
            masterForm.changeSelection(Arrays.asList(new Person(StringUtils.EMPTY)));

            countersListener.increment(expectedCounters, CounterName.TABLE, //
                    CounterName.SELECTION, CounterName.INDEX_HOLDER, CounterName.FORM_OBJECT, CounterName.VALUE_MODEL);
        }

        TestCase.assertTrue("dispatcher.isEditingNewFormObject", dispatcherForm.isEditingNewFormObject() || !inserting);
        TestCase.assertTrue("child.isEditingNewFormObject()", childForm.isEditingNewFormObject() || !inserting);
        TestCase.assertEquals(expectedCounters, countersListener.getCounters());
        TestBbDispatcherForm.assertDispatcherFormPropagatesChanges(dispatcherForm);

        // 2.Make some changes
        TestBbDispatcherForm.this.userAction(childForm, "name", name);
        TestBbDispatcherForm.this.userAction(childForm, "age", age);

        countersListener.increment(expectedCounters, CountersListener.CounterName.VALUE_MODEL);
        TestCase.assertTrue("dispatcher.isEditingNewFormObject", dispatcherForm.isEditingNewFormObject() || !inserting);
        TestCase.assertTrue("child.EditingNewFormObject()", childForm.isEditingNewFormObject() || !inserting);
        TestCase.assertEquals(expectedCounters, countersListener.getCounters());
        TestBbDispatcherForm.assertDispatcherFormPropagatesChanges(dispatcherForm);

        // 3.Execute saveCommand
        SwingUtils.runInEventDispatcherThread(new Runnable() {

            @Override
            public void run() {

                masterForm.getSaveCommand().execute();
            }
        });

        // Table changes in any case since a #set(i, T) operation is done even when updating on postCommit
        countersListener.increment(expectedCounters, CounterName.TABLE, CounterName.FORM_OBJECT);
        if (inserting) {
            countersListener.increment(expectedCounters, CounterName.SELECTION, CounterName.INDEX_HOLDER);
        }
        TestCase.assertFalse("dispatcherForm.isEditingNewFormObject()", dispatcherForm.isEditingNewFormObject());
        TestCase.assertFalse("childForm.isEditingNewFormObject()", childForm.isEditingNewFormObject());
        TestCase.assertEquals(expectedCounters, countersListener.getCounters());
        TestBbDispatcherForm.assertDispatcherFormPropagatesChanges(dispatcherForm);

        // 4.Commit works as expected
        final Person person = (Person) dispatcherForm.getFormObject();
        TestCase.assertEquals(name, person.getName());
        TestCase.assertEquals(age, person.getAge().toString());
        TestCase.assertSame(dispatcherForm.getFormObject(), childForm.getFormObject());
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
            TestCase.assertSame(dispatcherForm.getFormObject(), childForm.getFormObject());
            TestCase.assertEquals(FormUtils.getSelectedIndex(dispatcherForm), FormUtils.getSelectedIndex(childForm));
            TestCase.assertNotSame(//
                    FormUtils.getEditableFormObjects(dispatcherForm), FormUtils.getEditableFormObjects(childForm));
            TestCase.assertTrue(ListUtils.isEqualList(//
                    FormUtils.getEditableFormObjects(dispatcherForm), FormUtils.getEditableFormObjects(childForm)));

            TestCase.assertEquals(dispatcherForm.isDirty(), childForm.isDirty());
            TestCase.assertEquals(dispatcherForm.isEditingNewFormObject(), childForm.isEditingNewFormObject());
            TestCase.assertEquals(dispatcherForm.isEnabled(), childForm.isEnabled());
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
    protected static class CountersListener implements TableModelListener, ListSelectionListener,
            PropertyChangeListener {

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
             * Number of <code>propertyChange</code> events on <code>formObject</code> of a form.
             */
            FORM_OBJECT,

            /**
             * Number of <code>propertyChange</code> events on a <code>valueModel</code> of a form.
             */
            VALUE_MODEL
        };

        /**
         * The counters managed by this class.
         */
        private Map<CounterName, Integer> counters = new HashMap<CounterName, Integer>();

        /**
         * The counters being ignored.
         */
        private Set<CounterName> ignoredCounters = new HashSet<CounterName>();

        /**
         * Creates the listener ignoring value model changes.
         * 
         * @param masterForm
         *            the master form.
         * @param listened
         *            a form to be listened for object changes.
         */
        public CountersListener(AbstractB2TableMasterForm<?> masterForm, AbstractForm listened) {

            this(masterForm, listened, StringUtils.EMPTY, CounterName.VALUE_MODEL);
        }

        /**
         * Creates the listener.
         * 
         * @param masterFormTarget
         *            the master form.
         * @param listened
         *            a form to be listened for object and value changes.
         * @param propertyName
         *            the property to be listened.
         * @param ignoredCounters
         *            the counters to be ignored (i.e.: user actions are not tracked by dispatcher form).
         */
        public CountersListener(AbstractB2TableMasterForm<?> masterForm, AbstractForm listened, String propertyName,
                CounterName... counterNamesToIgnore) {

            Assert.notNull(masterForm, "masterForm");
            Assert.notNull(listened, "formObjectTarget");
            Assert.notNull(masterForm.getDispatcherForm(), "masterForm.getDispatcherForm()");

            this.ignoredCounters.addAll(Arrays.asList(counterNamesToIgnore));

            final BbDispatcherForm<?> dispatcherForm = masterForm.getDispatcherForm();

            masterForm.getMasterTableModel().addTableModelListener(this);
            masterForm.getMasterTable().getSelectionModel().addListSelectionListener(this);
            dispatcherForm.getEditingIndexHolder().addPropertyChangeListener("value", this);
            listened.addFormObjectChangeListener(this);
            if (!StringUtils.isEmpty(propertyName)) {
                listened.getValueModel(propertyName).addValueChangeListener(this);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void tableChanged(TableModelEvent e) {

            this.increment(CounterName.TABLE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void valueChanged(ListSelectionEvent e) {

            if (!e.getValueIsAdjusting()) {
                this.increment(CounterName.SELECTION);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final void propertyChange(PropertyChangeEvent evt) {

            final Object newValue = evt.getNewValue();

            if (newValue instanceof Integer) {
                this.increment(CounterName.INDEX_HOLDER);
            } else if (newValue instanceof String) {
                this.increment(CounterName.VALUE_MODEL);
            } else if (newValue == null) {
                this.increment(CounterName.VALUE_MODEL);
            } else {
                this.increment(CounterName.FORM_OBJECT);
            }
        }

        /**
         * Get the value of a given counter, never mind if counter is not already defined, in such a case returns 0.
         * 
         * @param counters
         *            the map.
         * @param counterName
         *            the counter name.
         * @return the new counter value.
         */
        public final Integer getCount(Map<CounterName, Integer> counters, CounterName counterName) {

            return MapUtils.getInteger(counters, counterName, 0);
        }

        /**
         * Gets the counters.
         * 
         * @return the counters.
         */
        public final Map<CounterName, Integer> getCounters() {

            return this.counters;
        }

        /**
         * Gets the counters being ignored.
         * 
         * @return the counters.
         */
        public final Set<CounterName> getIgnoredCounters() {

            return this.ignoredCounters;
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
        public final void increment(Map<CounterName, Integer> counters, CounterName... counterNames) {

            for (CounterName counterName : counterNames) {
                if (!this.getIgnoredCounters().contains(counterName)) {

                    Integer count = 0;
                    count = this.getCount(counters, counterName);
                    counters.put(counterName, ++count);
                }
            }
        }

        /**
         * Increments a counter, never mind if counter is not already defined.
         * 
         * @param counterName
         *            the counter name.
         * @return the new counter value.
         */
        private void increment(CounterName counterName) {

            Assert.notNull(counterName, "counterName");

            this.increment(this.getCounters(), counterName);
        }
    }
}
