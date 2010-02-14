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
package org.bluebell.richclient.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.ListSelectionModel;

import junit.framework.TestCase;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.bluebell.richclient.application.support.FormBackedView;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.samples.simple.form.PersonChildForm;
import org.bluebell.richclient.samples.simple.form.PersonMasterForm;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.jdesktop.swingx.JXTable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.Assert;

/**
 * Tests the correct behaviour of {@link AbstractBb0TableMasterForm}.
 * 
 * @see org.bluebell.richclient.form.binding.swing.TableBinding
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration()
public class TestAbstractBb0TableMasterForm extends AbstractBbSamplesTests {

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

    static {
        TestAbstractBb0TableMasterForm.PERSONS_1.add(new Person("0"));
        TestAbstractBb0TableMasterForm.PERSONS_1.add(new Person("1"));
        TestAbstractBb0TableMasterForm.PERSONS_1.add(new Person("2"));
        TestAbstractBb0TableMasterForm.PERSONS_1.add(new Person("3"));

        TestAbstractBb0TableMasterForm.PERSONS_2.add(new Person("A"));
        TestAbstractBb0TableMasterForm.PERSONS_2.add(new Person("B"));
        TestAbstractBb0TableMasterForm.PERSONS_2.add(new Person("C"));
        TestAbstractBb0TableMasterForm.PERSONS_2.add(new Person("D"));

        TestAbstractBb0TableMasterForm.PERSONS_3.add(new Person("3"));
        TestAbstractBb0TableMasterForm.PERSONS_3.add(new Person("4"));
        TestAbstractBb0TableMasterForm.PERSONS_3.add(new Person("D"));
        TestAbstractBb0TableMasterForm.PERSONS_3.add(new Person("E"));
    }

    /**
     * Tests the correct behaviour of <code>showEntities</code>.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testShowEntities() {

        final AbstractBb0TableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        final int size04 = 4;
        final int size08 = 8;
        final int size10 = 10;
        Collection<Person> expectedView = ListUtils.EMPTY_LIST;

        // Master table is empty at the beginning
        TestCase.assertTrue(CollectionUtils.isEqualCollection(expectedView, masterForm.getMasterEventList()));

        // Set visible entities
        expectedView = TestAbstractBb0TableMasterForm.PERSONS_1;
        masterForm.showEntities(TestAbstractBb0TableMasterForm.PERSONS_1);
        TestCase.assertTrue(CollectionUtils.isEqualCollection(expectedView, masterForm.getMasterEventList()));
        TestCase.assertEquals(size04, masterForm.getMasterEventList().size());

        // Attach additional entities
        expectedView = CollectionUtils.union(//
                TestAbstractBb0TableMasterForm.PERSONS_1, TestAbstractBb0TableMasterForm.PERSONS_2);
        masterForm.showEntities(TestAbstractBb0TableMasterForm.PERSONS_2, Boolean.TRUE);
        TestCase.assertTrue(CollectionUtils.isEqualCollection(expectedView, masterForm.getMasterEventList()));
        TestCase.assertEquals(size08, masterForm.getMasterEventList().size());

        // Attach existing and new entities
        expectedView = CollectionUtils.union(expectedView, TestAbstractBb0TableMasterForm.PERSONS_3);
        masterForm.showEntities(TestAbstractBb0TableMasterForm.PERSONS_3, Boolean.TRUE);
        TestCase.assertTrue(CollectionUtils.isEqualCollection(expectedView, masterForm.getMasterEventList()));
        TestCase.assertEquals(size10, masterForm.getMasterEventList().size());

        // Change visible entities
        expectedView = TestAbstractBb0TableMasterForm.PERSONS_3;
        masterForm.showEntities(TestAbstractBb0TableMasterForm.PERSONS_3);
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
        final AbstractBb0TableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        List<Person> expectedView = ListUtils.EMPTY_LIST;
        List<Person> expectedSelection = ListUtils.EMPTY_LIST;

        // Master table is empty at the beginning
        TestCase.assertTrue(masterForm.getMasterEventList().isEmpty());

        // Select an entity that is not shown currently
        expectedView = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, 1);
        expectedSelection = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, 1);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Select other entity that is not shown currently
        expectedView = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, 2);
        expectedSelection = TestAbstractBb0TableMasterForm.PERSONS_1.subList(1, 2);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Select an entity that is shown currently
        expectedView = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, 2);
        expectedSelection = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, 1);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Multiple selection when both entities are shown currently
        expectedView = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, 2);
        expectedSelection = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, 2);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Multiple selection when an entity is not shown currently
        expectedView = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, pos03);
        expectedSelection = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, pos03);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Decrease multiple selection size
        expectedView = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, pos03);
        expectedSelection = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, 2);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Convert multiple selection into single selection
        expectedView = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, pos03);
        expectedSelection = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, 1);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);

        // Clear selection
        expectedSelection = ListUtils.EMPTY_LIST;
        expectedView = TestAbstractBb0TableMasterForm.PERSONS_1.subList(0, pos03);
        this.doTestChangeSelection(masterForm, expectedView, expectedSelection);
    }

    /**
     * Tests the correct behaviour of <code>getSelection</code>.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetSelection() {

        final AbstractBb0TableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        final JXTable masterTable = (JXTable) masterForm.getMasterTable();
        final ListSelectionModel selectionModel = masterTable.getSelectionModel();
        final int column = 0;

        // Show entities (randomly, ascendant and descendant ordered respectively)
        final List<Person> ranEntities = new ArrayList<Person>(TestAbstractBb0TableMasterForm.PERSONS_1);
        final List<Person> ascEntities = new ArrayList<Person>(TestAbstractBb0TableMasterForm.PERSONS_1);
        final List<Person> desEntities = new ArrayList<Person>(TestAbstractBb0TableMasterForm.PERSONS_1);

        Collections.shuffle(ranEntities);
        Collections.sort(ascEntities);
        Collections.sort(desEntities, Collections.reverseOrder());

        // Show entities randomly ordered
        masterForm.showEntities(ranEntities);

        List<Integer> indexes = Arrays.asList(2);

        selectionModel.setSelectionInterval(indexes.get(0), indexes.get(0));
        this.doTestGetSelection(masterForm, ranEntities, indexes);

        // Test #getSelection() works fine after toggling sort order
        masterTable.toggleSortOrder(column);
        selectionModel.setSelectionInterval(indexes.get(0), indexes.get(0));
        this.doTestGetSelection(masterForm, ascEntities, indexes);

        // Test #getSelection() works fine after toggling sort order again
        masterTable.toggleSortOrder(column);
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
    }

    /**
     * Tests the correct behaviour of <code>requestUserConfirmation</code>.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRequestUserConfirmation() {

        final int pos03 = 3;
        final MockPersonMasterForm masterForm = (MockPersonMasterForm) this.getBackingForm(this.getMasterView());
        final PersonChildForm detailForm = (PersonChildForm) this.getBackingForm(this.getDetailView());

        List<Person> oldSelection = ListUtils.EMPTY_LIST;
        List<Person> newSelection = Arrays.asList(TestAbstractBb0TableMasterForm.PERSONS_1.get(1));
        int requestCount = 0;

        // Show entities:
        // EXPECTED: [1,2,3,4]
        masterForm.showEntities(TestAbstractBb0TableMasterForm.PERSONS_1);
        masterForm.changeSelection(newSelection);

        // Change selection: requests count should keep invariable
        // EXPECTED: [1, 2, -->3<--, 4]
        oldSelection = newSelection;
        newSelection = Arrays.asList(TestAbstractBb0TableMasterForm.PERSONS_1.get(2));
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, newSelection, requestCount, newSelection);

        // Change age: detail form should get dirty
        // EXPECTED: [1, 2, -->[D]3<--, 4]
        this.userAction(detailForm, "age", "22");
        TestCase.assertTrue("detailForm.isDirty()", detailForm.isDirty());

        // Abort selection change
        // EXPECTED: [1, 2, -->[D]3<--, 4]
        oldSelection = newSelection;
        newSelection = Arrays.asList(TestAbstractBb0TableMasterForm.PERSONS_1.get(pos03));
        this.doTestRequestUserConfirmation(masterForm, Boolean.FALSE, newSelection, ++requestCount, oldSelection);

        // Confirm selection change
        // EXPECTED: [1, 2, 3, -->4<--]
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, newSelection, ++requestCount, newSelection);
        TestCase.assertFalse("detailForm.isDirty()", detailForm.isDirty());

        // Change age: detail form should get dirty
        // EXPECTED: [1, 2, 3, -->[D]4<--]
        this.userAction(detailForm, "age", "22");
        TestCase.assertTrue("detailForm.isDirty()", detailForm.isDirty());

        // Abort multiple selection
        // EXPECTED: [1, 2, 3, -->[D]4<--]
        oldSelection = newSelection;
        newSelection = Arrays.asList(//
                TestAbstractBb0TableMasterForm.PERSONS_1.get(2), TestAbstractBb0TableMasterForm.PERSONS_1.get(pos03));
        this.doTestRequestUserConfirmation(masterForm, Boolean.FALSE, newSelection, ++requestCount, oldSelection);

        // Confirm multiple selection
        // EXPECTED: [1, 2, -->3<--, -->[D]4<--]
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, newSelection, ++requestCount, newSelection);
        TestCase.assertTrue("detailForm.isDirty()", detailForm.isDirty());

        // Abort single selection again
        oldSelection = newSelection;
        newSelection = Arrays.asList(TestAbstractBb0TableMasterForm.PERSONS_1.get(2));
        this.doTestRequestUserConfirmation(masterForm, Boolean.FALSE, newSelection, ++requestCount, oldSelection);

        // Confirm single selection again
        this.doTestRequestUserConfirmation(masterForm, Boolean.TRUE, newSelection, ++requestCount, newSelection);
        TestCase.assertFalse("detailForm.isDirty()", detailForm.isDirty());
    }

    /**
     * Method invoked at startup.
     * <p/>
     * Initializes test cases.
     */
    @Before
    public void startup() {

        this.initTest(new String[] { TestAbstractBb0TableMasterForm.MOCK_MASTER_VIEW_DESCRIPTOR_BEAN_NAME,
                AbstractBbSamplesTests.DETAIL_VIEW_DESCRIPTOR_BEAN_NAME });
    }

    /**
     * Cleans master event list after every test execution.
     */
    @SuppressWarnings("unchecked")
    @After
    public void cleanMasterEventList() {

        final AbstractBb0TableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        masterForm.showEntities(ListUtils.EMPTY_LIST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FormBackedView<AbstractBb2TableMasterForm<Person>> getMasterView() {

        return this.getApplicationPage().getView(TestAbstractBb0TableMasterForm.MOCK_MASTER_VIEW_DESCRIPTOR_BEAN_NAME);
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
    private void doTestChangeSelection(AbstractBb0TableMasterForm<Person> masterForm, Collection<Person> expectedView,
            List<Person> expectedSelection) {

        masterForm.changeSelection(expectedSelection);
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
    private void doTestGetSelection(AbstractBb0TableMasterForm<Person> masterForm, List<Person> currentOrder,
            List<Integer> indexes) {

        Collections.sort(indexes);

        final List<Person> selection = masterForm.getSelection();
        for (int i = 0; i < selection.size(); ++i) {
            TestCase.assertEquals(currentOrder.get(indexes.get(i)), selection.get(i));
        }
    }

    /**
     * Checks the correct behaviour of requesting user confirmation.
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
     *            the expected selection after proceeding.
     */
    private void doTestRequestUserConfirmation(MockPersonMasterForm masterForm, Boolean confirm,
            List<Person> newSelection, int expectedCount, List<Person> expectedSelection) {

        masterForm.setConfirm(confirm);
        masterForm.changeSelection(newSelection);
        TestCase.assertEquals(expectedCount, masterForm.getCount());
        TestCase.assertTrue(ListUtils.isEqualList(expectedSelection, masterForm.getSelection()));
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
