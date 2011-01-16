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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import junit.framework.TestCase;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.bluebell.richclient.application.support.FormBackedView;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.samples.simple.form.PersonChildForm;
import org.bluebell.richclient.samples.simple.form.PersonMasterForm;
import org.bluebell.richclient.samples.simple.form.PersonSearchForm;
import org.bluebell.richclient.swing.util.SwingUtils;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

/**
 * Tests the correct behaviour of {@link AbstractBbTableMasterForm}.
 * 
 * @see org.bluebell.richclient.form.binding.swing.TableBinding
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration
public class TestAbstractBbTableMasterForm extends AbstractBbSamplesTests {

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
     * The mock master view descriptor bean name.
     */
    protected static final String MOCK_MASTER_VIEW_DESCRIPTOR_BEAN_NAME = "mockPersonMasterViewDescriptor";

    /**
     * The page descriptor used for testing.
     */
    @Autowired
    private PageDescriptor pageDescriptor;

    static {
        TestAbstractBbTableMasterForm.PERSONS_1.add(new Person("0"));
        TestAbstractBbTableMasterForm.PERSONS_1.add(new Person("1"));
        TestAbstractBbTableMasterForm.PERSONS_1.add(new Person("2"));
        TestAbstractBbTableMasterForm.PERSONS_1.add(new Person("3"));

        TestAbstractBbTableMasterForm.PERSONS_2.add(new Person("A"));
        TestAbstractBbTableMasterForm.PERSONS_2.add(new Person("B"));
        TestAbstractBbTableMasterForm.PERSONS_2.add(new Person("C"));
        TestAbstractBbTableMasterForm.PERSONS_2.add(new Person("D"));

        TestAbstractBbTableMasterForm.PERSONS_3.add(new Person("3"));
        TestAbstractBbTableMasterForm.PERSONS_3.add(new Person("4"));
        TestAbstractBbTableMasterForm.PERSONS_3.add(new Person("D"));
        TestAbstractBbTableMasterForm.PERSONS_3.add(new Person("E"));
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testDependencyInjection() {

        TestCase.assertNotNull("pageDescriptor", this.pageDescriptor);
    }

    /**
     * Tests the correct behaviour of <code>showEntities</code>.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testShowEntities() {

        final AbstractBbTableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        final int size04 = 4;
        final int size08 = 8;
        final int size10 = 10;
        Collection<Person> expectedView = ListUtils.EMPTY_LIST;

        // Master table is empty at the beginning
        TestCase.assertTrue(CollectionUtils.isEqualCollection(expectedView, masterForm.getMasterEventList()));

        // Set visible entities
        expectedView = TestAbstractBbTableMasterForm.PERSONS_1;
        masterForm.showEntities(TestAbstractBbTableMasterForm.PERSONS_1);
        TestCase.assertTrue(CollectionUtils.isEqualCollection(expectedView, masterForm.getMasterEventList()));
        TestCase.assertEquals(size04, masterForm.getMasterEventList().size());

        // Attach additional entities
        expectedView = CollectionUtils.union(//
                TestAbstractBbTableMasterForm.PERSONS_1, TestAbstractBbTableMasterForm.PERSONS_2);
        masterForm.showEntities(TestAbstractBbTableMasterForm.PERSONS_2, Boolean.TRUE);
        TestCase.assertTrue(CollectionUtils.isEqualCollection(expectedView, masterForm.getMasterEventList()));
        TestCase.assertEquals(size08, masterForm.getMasterEventList().size());

        // Attach existing and new entities
        expectedView = CollectionUtils.union(expectedView, TestAbstractBbTableMasterForm.PERSONS_3);
        masterForm.showEntities(TestAbstractBbTableMasterForm.PERSONS_3, Boolean.TRUE);
        TestCase.assertTrue(CollectionUtils.isEqualCollection(expectedView, masterForm.getMasterEventList()));
        TestCase.assertEquals(size10, masterForm.getMasterEventList().size());

        // Change visible entities
        expectedView = TestAbstractBbTableMasterForm.PERSONS_3;
        masterForm.showEntities(TestAbstractBbTableMasterForm.PERSONS_3);
        TestCase.assertTrue(CollectionUtils.isEqualCollection(expectedView, masterForm.getMasterEventList()));
        TestCase.assertEquals(size04, masterForm.getMasterEventList().size());
    }

    /**
     * Tests the correct behaviour of <code>changeSelection</code>.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testChangeSelection() {

        final int pos03 = 3;
        final AbstractBbTableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        List<Person> expectedView = ListUtils.EMPTY_LIST;
        List<Person> expectedSelection = ListUtils.EMPTY_LIST;

        // Master table is empty at the beginning
        TestCase.assertTrue(masterForm.getMasterEventList().isEmpty());

        // Select an entity that is not shown currently
        expectedView = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, 1);
        expectedSelection = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, 1);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Select other entity that is not shown currently
        expectedView = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, 2);
        expectedSelection = TestAbstractBbTableMasterForm.PERSONS_1.subList(1, 2);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Select an entity that is shown currently
        expectedView = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, 2);
        expectedSelection = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, 1);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Multiple selection when both entities are shown currently
        expectedView = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, 2);
        expectedSelection = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, 2);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Multiple selection when an entity is not shown currently
        expectedView = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, pos03);
        expectedSelection = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, pos03);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Decrease multiple selection size
        expectedView = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, pos03);
        expectedSelection = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, 2);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Convert multiple selection into single selection
        expectedView = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, pos03);
        expectedSelection = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, 1);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Clear selection
        expectedSelection = ListUtils.EMPTY_LIST;
        expectedView = TestAbstractBbTableMasterForm.PERSONS_1.subList(0, pos03);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);
    }

    /**
     * Tests the correct behaviour of <code>getSelection</code>.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetSelection() {

        final AbstractBbTableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        final JTable masterTable = (JTable) masterForm.getMasterTable();
        final ListSelectionModel selectionModel = masterTable.getSelectionModel();
        final int column = 0;

        // Show entities (randomly, ascendant and descendant ordered respectively)
        final List<Person> ranEntities = new ArrayList<Person>(TestAbstractBbTableMasterForm.PERSONS_1);
        final List<Person> ascEntities = new ArrayList<Person>(TestAbstractBbTableMasterForm.PERSONS_1);
        final List<Person> desEntities = new ArrayList<Person>(TestAbstractBbTableMasterForm.PERSONS_1);

        Collections.shuffle(ranEntities);
        Collections.sort(ascEntities);
        Collections.sort(desEntities, Collections.reverseOrder());

        // Show entities randomly ordered
        masterForm.showEntities(ranEntities);

        List<Integer> indexes = Arrays.asList(2);

        selectionModel.setSelectionInterval(indexes.get(0), indexes.get(0));
        this.doTestGetSelection(masterForm, ranEntities, indexes);

        // Test #getSelection() works fine after toggling sort order
        this.sortTable(masterTable, column, Boolean.TRUE);

        selectionModel.setSelectionInterval(indexes.get(0), indexes.get(0));
        this.doTestGetSelection(masterForm, ascEntities, indexes);

        // Test #getSelection() works fine after toggling sort order again
        this.sortTable(masterTable, column, Boolean.FALSE);
        selectionModel.setSelectionInterval(indexes.get(0), indexes.get(0));
        this.doTestGetSelection(masterForm, desEntities, indexes);

        // Test #getSelection() works fine with multiple (non consecutive) selection
        indexes = Arrays.asList(2, 0);
        selectionModel.addSelectionInterval(indexes.get(1), indexes.get(1));
        this.doTestGetSelection(masterForm, desEntities, indexes);

        // Test #getSelection() works fine with empty selection
        indexes = ListUtils.EMPTY_LIST;
        selectionModel.clearSelection();
        this.doTestGetSelection(masterForm, desEntities, indexes);

        // (JAF), 20110103, keep table unchanged
        this.sortTable(masterTable, column, Boolean.TRUE);
    }

    /**
     * Tests the correct behaviour of <code>requestUserConfirmation</code>.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRequestUserConfirmationOnChangeSelection() {

        final int pos03 = 3;
        final MockPersonMasterForm masterForm = (MockPersonMasterForm) this.getBackingForm(this.getMasterView());
        final PersonChildForm childForm = (PersonChildForm) this.getBackingForm(this.getChildView());

        List<Person> oldSelection = ListUtils.EMPTY_LIST;
        List<Person> newSelection = Arrays.asList(TestAbstractBbTableMasterForm.PERSONS_1.get(1));
        int requestCount = 0;

        // Show entities:
        // EXPECTED: [1,2,3,4]
        masterForm.showEntities(TestAbstractBbTableMasterForm.PERSONS_1);
        masterForm.changeSelection(newSelection);

        // Change selection: requests count should keep invariable
        // EXPECTED: [1, 2, -->3<--, 4]
        oldSelection = newSelection;
        newSelection = Arrays.asList(TestAbstractBbTableMasterForm.PERSONS_1.get(2));
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, newSelection, requestCount, newSelection);

        // Change age: child form should get dirty
        // EXPECTED: [1, 2, -->[D]3<--, 4]
        this.userAction(childForm, "age", "22");
        TestCase.assertTrue("childForm.isDirty()", childForm.isDirty());

        // Abort selection change
        // EXPECTED: [1, 2, -->[D]3<--, 4]
        oldSelection = newSelection;
        newSelection = Arrays.asList(TestAbstractBbTableMasterForm.PERSONS_1.get(pos03));
        this.doTestRequestUserConfirmation(masterForm, Boolean.FALSE, newSelection, ++requestCount, oldSelection);

        // Confirm selection change
        // EXPECTED: [1, 2, 3, -->4<--]
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, newSelection, ++requestCount, newSelection);
        TestCase.assertFalse("childForm.isDirty()", childForm.isDirty());

        // Change age: child form should get dirty
        // EXPECTED: [1, 2, 3, -->[D]4<--]
        this.userAction(childForm, "age", "22");
        TestCase.assertTrue("childForm.isDirty()", childForm.isDirty());

        // Abort multiple selection
        // EXPECTED: [1, 2, 3, -->[D]4<--]
        oldSelection = newSelection;
        newSelection = Arrays.asList(//
                TestAbstractBbTableMasterForm.PERSONS_1.get(2), TestAbstractBbTableMasterForm.PERSONS_1.get(pos03));
        this.doTestRequestUserConfirmation(masterForm, Boolean.FALSE, newSelection, ++requestCount, oldSelection);

        // Confirm multiple selection
        // EXPECTED: [1, 2, -->3<--, -->4<--]
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, newSelection, ++requestCount, newSelection);
        TestCase.assertFalse("childForm.isDirty()", childForm.isDirty());

        // Change age again: child form should get dirty
        // EXPECTED: [1, 2, 3, -->[D]4<--]
        this.userAction(childForm, "age", "220");
        TestCase.assertTrue("childForm.isDirty()", childForm.isDirty());

        // Abort single selection again
        oldSelection = newSelection;
        newSelection = Arrays.asList(TestAbstractBbTableMasterForm.PERSONS_1.get(2));
        this.doTestRequestUserConfirmation(masterForm, Boolean.FALSE, newSelection, ++requestCount, oldSelection);

        // Confirm single selection again
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, newSelection, ++requestCount, newSelection);
        TestCase.assertFalse("childForm.isDirty()", childForm.isDirty());
    }

    /**
     * Tests the correct behaviour of <code>requestUserConfirmation</code> on search.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRequestUserConfirmationOnSearch() {

        final MockPersonMasterForm masterForm = (MockPersonMasterForm) this.getBackingForm(this.getMasterView());
        final PersonChildForm childForm = (PersonChildForm) this.getBackingForm(this.getChildView());
        final PersonSearchForm searchForm = (PersonSearchForm) this.getBackingForm(this.getSearchView());

        List<Person> newSelection = ListUtils.EMPTY_LIST;
        ActionCommand actionCommand = null;
        int requestCount = masterForm.getCount();

        // Show entities:
        // EXPECTED: [1,2,3,4]
        masterForm.showEntities(TestAbstractBbTableMasterForm.PERSONS_1);
        masterForm.changeSelection(newSelection);

        /*
         * (A) SEARCH COMMAND
         */
        actionCommand = searchForm.getSearchCommand();

        // (a.1) Change selection: requests count should keep invariable
        // EXPECTED: [1, 2, -->3<--, 4]
        newSelection = Arrays.asList(TestAbstractBbTableMasterForm.PERSONS_1.get(2));
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, newSelection, requestCount, newSelection);

        // (a.2) Change age: child form should get dirty
        // EXPECTED: [1, 2, -->[D]3<--, 4]
        this.userAction(childForm, "age", "22");
        TestCase.assertTrue("childForm.isDirty()", childForm.isDirty());

        // (a.3) Abort search command execution
        // EXPECTED: [1, 2, -->[D]3<--, 4]
        this.doTestRequestUserConfirmation(masterForm, Boolean.FALSE, actionCommand, ++requestCount, newSelection);

        // (a.4) Confirm search command execution
        // EXPECTED: [X, Y, ... Z]
        newSelection = ListUtils.EMPTY_LIST;
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, actionCommand, ++requestCount, newSelection);

        /*
         * (B) REFRESH LAST SEARCH COMMAND
         */
        actionCommand = searchForm.getRefreshLastSearchCommand();

        // (b.1) Change selection again: requests count should keep invariable
        // EXPECTED: [-->X<--, Y, ... Z]
        newSelection = masterForm.getMasterEventList().subList(0, 1);
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, newSelection, requestCount, newSelection);

        // (b.2) Change age: child form should get dirty
        // EXPECTED: [-->[D]X<--, Y, ... Z]
        this.userAction(childForm, "age", "22");
        TestCase.assertTrue("childForm.isDirty()", childForm.isDirty());

        // (b.3) Abort refresh last search command execution
        // EXPECTED: [-->[D]X<--, Y, ... Z]
        this.doTestRequestUserConfirmation(masterForm, Boolean.FALSE, actionCommand, ++requestCount, newSelection);

        // (b.4) Confirm refresh last search command execution
        // EXPECTED: [X, Y, ... Z]
        newSelection = ListUtils.EMPTY_LIST;
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, actionCommand, ++requestCount, newSelection);
    }

    /**
     * Tests the correct behaviour of <code>requestUserConfirmation</code> on new form object creation.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRequestUserConfirmationOnNewFormObject() {

        final MockPersonMasterForm masterForm = (MockPersonMasterForm) this.getBackingForm(this.getMasterView());
        final PersonChildForm childForm = (PersonChildForm) this.getBackingForm(this.getChildView());
        final PersonSearchForm searchForm = (PersonSearchForm) this.getBackingForm(this.getSearchView());
        final String name = "JAF";

        List<Person> newSelection = ListUtils.EMPTY_LIST;
        ActionCommand actionCommand = null;
        int requestCount = masterForm.getCount();

        /*
         * (A) NEW FORM OBJECT COMMAND
         */
        actionCommand = masterForm.getNewFormObjectCommand();

        // (a.1) Create a new entity: requests count should keep invariable
        // EXPECTED: []
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, actionCommand, requestCount, newSelection);

        // (a.2) Change name: child form should get dirty
        // EXPECTED: [] Child gets dirty
        this.userAction(childForm, "name", name);
        TestCase.assertTrue("childForm.isDirty()", childForm.isDirty());

        /*
         * (B) SEARCH COMMAND
         */
        actionCommand = searchForm.getSearchCommand();

        // (b.1) Abort search command execution
        // EXPECTED: [] Child is dirty and editing new form object
        this.doTestRequestUserConfirmation(masterForm, Boolean.FALSE, actionCommand, ++requestCount, newSelection);
        TestCase.assertTrue("childForm.isDirty()", childForm.isDirty());
        TestCase.assertTrue("childForm.isEditingNewFormObject()", childForm.isEditingNewFormObject());

        // (b.1) Confirm search command execution
        // EXPECTED: [] Child is dirty and editing new form object
        // Note JIRA issue http://jirabluebell.b2b2000.com/browse/BLUE-55 was wrong since this is the expected behaviour
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, actionCommand, ++requestCount, newSelection);
        TestCase.assertTrue("childForm.isDirty()", childForm.isDirty());
        TestCase.assertTrue("childForm.isEditingNewFormObject()", childForm.isEditingNewFormObject());

        /*
         * (C) SAVE COMMAND
         */
        actionCommand = masterForm.getSaveCommand();

        // (c.1) Save command execution
        // EXPECTED: [P] Child is neither dirty nor editing new form object
        newSelection = Arrays.asList(new Person(name));
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, actionCommand, requestCount, newSelection);
        TestCase.assertFalse("childForm.isEditingNewFormObject()", childForm.isEditingNewFormObject());
        TestCase.assertFalse("childForm.isDirty()", childForm.isDirty());
    }

    /**
     * Tests the correct behaviour of <code>requestUserConfirmation</code> on select all command execution.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRequestUserConfirmationOnSelectAllEntities() {

        final MockPersonMasterForm masterForm = (MockPersonMasterForm) this.getBackingForm(this.getMasterView());
        final PersonChildForm childForm = (PersonChildForm) this.getBackingForm(this.getChildView());

        List<Person> newSelection = ListUtils.EMPTY_LIST;
        ActionCommand actionCommand = null;
        int requestCount = masterForm.getCount();

        // Show entities:
        // EXPECTED: [1,2,3,4]
        masterForm.showEntities(TestAbstractBbTableMasterForm.PERSONS_1);
        masterForm.changeSelection(newSelection);

        /*
         * (A) SELECT ALL COMMAND
         */
        actionCommand = masterForm.getSelectAllCommand();

        // (a.1) Change selection: requests count should keep invariable
        // EXPECTED: [1, 2, -->3<--, 4]
        newSelection = Arrays.asList(TestAbstractBbTableMasterForm.PERSONS_1.get(2));
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, newSelection, requestCount, newSelection);

        // (a.2) Change age: child form should get dirty
        // EXPECTED: [1, 2, -->[D]3<--, 4]
        this.userAction(childForm, "age", "22");
        TestCase.assertTrue("childForm.isDirty()", childForm.isDirty());

        // (a.3) Abort selectAll command execution
        // EXPECTED: [1, 2, -->[D]3<--, 4]
        this.doTestRequestUserConfirmation(masterForm, Boolean.FALSE, actionCommand, ++requestCount, newSelection);

        // (a.4) Confirm selectAll command execution
        // EXPECTED: [-->1<--, -->2<--, -->3<--, -->4<--]
        newSelection = TestAbstractBbTableMasterForm.PERSONS_1;
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, actionCommand, ++requestCount, newSelection);
        TestCase.assertFalse("childForm.isDirty()", childForm.isDirty());        
    }

    @Test
    public void testRequestUserConfirmationOnRefresh() {

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

    /**
     * {@inheritDoc}
     */
    @Override
    protected FormBackedView<AbstractB2TableMasterForm<Person>> getMasterView() {

        return this.getInitializedPage().getView(TestAbstractBbTableMasterForm.MOCK_MASTER_VIEW_DESCRIPTOR_BEAN_NAME);
    }

    /**
     * Checks view and selection are as expected.
     * 
     * @param masterForm
     *            the master form.
     * @param expectedView
     *            the expected view.
     * @param expectedSelection
     *            the expected selection.
     */
    private void doTestChangeSelection(AbstractBbTableMasterForm<Person> masterForm, Collection<Person> expectedView,
            List<Person> expectedSelection) {

        masterForm.changeSelection(expectedSelection);
        TestCase.assertNotSame(expectedSelection, masterForm.getSelection());
        TestCase.assertNotSame(expectedSelection, masterForm.getMasterEventList());
        TestCase.assertTrue(CollectionUtils.isEqualCollection(expectedSelection, masterForm.getSelection()));
        TestCase.assertTrue(CollectionUtils.isEqualCollection(expectedView, masterForm.getMasterEventList()));
    }

    /**
     * Checks selection has the expected order.
     * 
     * @param masterForm
     *            the master form.
     * @param currentOrder
     *            the current view order.
     * @param indexes
     *            the view selected indexes.
     */
    private void doTestGetSelection(AbstractBbTableMasterForm<Person> masterForm, List<Person> currentOrder,
            List<Integer> indexes) {

        Collections.sort(indexes);

        final List<Person> selection = masterForm.getSelection();
        for (int i = 0; i < selection.size(); ++i) {
            TestCase.assertEquals(currentOrder.get(indexes.get(i)), selection.get(i));
        }
    }

    /**
     * Checks the correct behaviour of requesting user confirmation after changing selection.
     * 
     * @param masterForm
     *            the master form.
     * @param confirm
     *            whether to confirm selection change.
     * @param newSelection
     *            the elements to select.
     * @param expectedCount
     *            the expected number of user requests.
     * @param expectedSelection
     *            the expected selection after isProceeding.
     */
    private void doTestRequestUserConfirmation(final MockPersonMasterForm masterForm, Boolean confirm,
            final List<Person> newSelection, int expectedCount, List<Person> expectedSelection) {

        Assert.notNull(masterForm, "masterForm");
        Assert.notNull(confirm, "confirm");
        Assert.notNull(newSelection, "newSelection");
        Assert.notNull(expectedCount, "expectedCount");
        Assert.notNull(expectedSelection, "expectedSelection");

        this.doTestRequestUserConfirmation(masterForm, confirm, new ActionCommand() {

            @Override
            protected void doExecuteCommand() {

                masterForm.changeSelection(newSelection);

            }
        }, expectedCount, expectedSelection);
    }

    /**
     * Checks the correct behaviour of requesting user confirmation after executing a command.
     * 
     * @param masterForm
     *            the master form.
     * @param confirm
     *            whether to confirm selection change.
     * @param actionCommand
     *            the command to execute.
     * @param expectedCount
     *            the expected number of user requests.
     * @param expectedSelection
     *            the expected selection after isProceeding.
     */
    private void doTestRequestUserConfirmation(MockPersonMasterForm masterForm, Boolean confirm,
            final ActionCommand actionCommand, int expectedCount, List<Person> expectedSelection) {

        Assert.notNull(masterForm, "masterForm");
        Assert.notNull(confirm, "confirm");
        Assert.notNull(actionCommand, "actionCommand");
        Assert.notNull(expectedCount, "expectedCount");
        Assert.notNull(expectedSelection, "expectedSelection");

        masterForm.setConfirm(confirm);
        
        SwingUtils.runInEventDispatcherThread(actionCommand);

        TestCase.assertEquals(expectedCount, masterForm.getCount());
        TestCase.assertTrue("ListUtils.isEqualList(expectedSelection, masterForm.getSelection())\n" + expectedSelection
                + "\n" + masterForm.getSelection(), ListUtils.isEqualList(expectedSelection, masterForm.getSelection()));
    }

    /**
     * Mock version of person master form.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class MockPersonMasterForm extends PersonMasterForm {

        /**
         * Whether to confirm request.
         */
        private Boolean confirm = Boolean.TRUE;

        /**
         * Counts user confirmation requests.
         */
        private int count = 0;

        /**
         * Default constructor.
         */
        public MockPersonMasterForm() {

            super();
        }

        /**
         * Gets the count of user confirmation requests.
         * 
         * @return the count.
         */
        public int getCount() {

            return this.count;
        }

        /**
         * Sets whether to confirm.
         * 
         * @param confirm
         *            whether to confirm.
         */
        public void setConfirm(Boolean confirm) {

            Assert.notNull(confirm, "confirm");

            this.confirm = confirm;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean requestUserConfirmation() {

            ++count;

            return this.confirm;
        }
    }
}
