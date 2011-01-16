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
import java.util.List;

import javax.swing.JTextField;

import junit.framework.TestCase;

import org.apache.commons.collections.ListUtils;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.samples.simple.form.PersonChildForm;
import org.bluebell.richclient.samples.simple.form.PersonMasterForm;
import org.bluebell.richclient.samples.simple.form.PersonSearchForm;
import org.bluebell.richclient.samples.simple.service.PersonService;
import org.bluebell.richclient.swing.util.SwingUtils;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.command.ActionCommand;

/**
 * Tests the correct behaviour of {@link AbstractBbTableMasterForm}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestMockAbstractBbTableMasterForm extends AbstractBbSamplesTests {

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
     * The imocks control.
     */
    private IMocksControl iMocksControl = EasyMock.createStrictControl();

    static {
        TestMockAbstractBbTableMasterForm.PERSONS_1.add(new Person("0"));
        TestMockAbstractBbTableMasterForm.PERSONS_1.add(new Person("1"));
        TestMockAbstractBbTableMasterForm.PERSONS_1.add(new Person("2"));
        TestMockAbstractBbTableMasterForm.PERSONS_1.add(new Person("3"));

        TestMockAbstractBbTableMasterForm.PERSONS_2.add(new Person("A"));
        TestMockAbstractBbTableMasterForm.PERSONS_2.add(new Person("B"));
        TestMockAbstractBbTableMasterForm.PERSONS_2.add(new Person("C"));
        TestMockAbstractBbTableMasterForm.PERSONS_2.add(new Person("D"));

        TestMockAbstractBbTableMasterForm.PERSONS_3.add(new Person("3"));
        TestMockAbstractBbTableMasterForm.PERSONS_3.add(new Person("4"));
        TestMockAbstractBbTableMasterForm.PERSONS_3.add(new Person("D"));
        TestMockAbstractBbTableMasterForm.PERSONS_3.add(new Person("E"));
    }

    /**
     * Installs a mock person service instance into the master and search forms.
     */
    @Before
    public void upgradePersonService() {

        this.initializeVariables(this.getPersonPageDescriptor());

        final PersonService personService = this.iMocksControl.createMock(PersonService.class);

        final PersonMasterForm masterForm = (PersonMasterForm) this.getBackingForm(this.getMasterView());
        final PersonSearchForm searchForm = (PersonSearchForm) this.getBackingForm(this.getSearchView());

        masterForm.setPersonService(personService);
        searchForm.setPersonService(personService);
    }

    /**
     * Resets the iMocks control.
     */
    @After
    public void resetMock() {

        this.iMocksControl.resetToStrict();
    }

    /**
     * {@inheritDoc}
     */
    @Test
    public void testDependencyInjection() {

        super.testDependencyInjection();
    }

    /**
     * Tests the correct behaviour of save command ensuring raised operations (<code>doXXX</code>) are expected.
     * 
     * @see <a href="http://jirabluebell.b2b2000.com/browse/BLUE-61">BLUE-61</a>
     */
    @Test
    public void testSaveCommand() {

        final PersonMasterForm masterForm = (PersonMasterForm) this.getBackingForm(this.getMasterView());
        final PersonChildForm childForm = (PersonChildForm) this.getBackingForm(this.getChildView());
        final PersonService mockPersonService = masterForm.getPersonService();

        final ActionCommand newFormObjectCommand = masterForm.getNewFormObjectCommand();
        final ActionCommand saveCommand = masterForm.getSaveCommand();

        final String property = "name";
        final ValueModel valueModel = childForm.getFormModel().getValueModel(property);
        final JTextField nameComponent = ((JTextField) SwingUtils.getDescendantNamed(property, childForm.getControl()));

        // (1). Begin new person creation
        SwingUtils.runInEventDispatcherThread(newFormObjectCommand);

        // Record expected behaviour
        final String nameBeforeInsert = "nameBeforeInsert";
        final String nameAfterInsert = "nameAfterInsert";
        final String nameAfterUpdate = "nameAfterUpdate";
        final Person personBeforeInsert = (Person) childForm.getFormObject();
        final Person personAfterInsert = Person.createPerson(nameAfterInsert);
        final Person personAfterUpdate = Person.createPerson(nameAfterUpdate);
        EasyMock.expect(mockPersonService.insertPerson(personBeforeInsert)).andReturn(personAfterInsert);
        EasyMock.expect(mockPersonService.updatePerson(personAfterInsert)).andReturn(personAfterUpdate);

        this.iMocksControl.replay();

        // (2). Change name
        this.userAction(childForm, property, nameBeforeInsert);

        // (3). Insert person and make assertions
        SwingUtils.runInEventDispatcherThread(saveCommand);

        TestCase.assertEquals(personAfterInsert, childForm.getFormObject());
        TestCase.assertEquals(nameAfterInsert, valueModel.getValue());
        TestCase.assertEquals(nameAfterInsert, nameComponent.getText());

        // (4). Change name again
        this.userAction(childForm, property, nameBeforeInsert);

        // (5). Update person and make assertions
        SwingUtils.runInEventDispatcherThread(saveCommand);

        TestCase.assertEquals(personAfterUpdate, childForm.getFormObject());
        TestCase.assertEquals(nameAfterUpdate, valueModel.getValue());
        TestCase.assertEquals(nameAfterUpdate, nameComponent.getText());
    }

    /**
     * Tests the correct behaviour of selection change ensuring raised operations (<code>doRefresh</code>) are expected.
     */
    @Test
    public void testRefreshCommandAfterSelectionChange() {

        final PersonMasterForm masterForm = (PersonMasterForm) this.getBackingForm(this.getMasterView());
        final PersonSearchForm searchForm = (PersonSearchForm) this.getBackingForm(this.getSearchView());
        final PersonChildForm childForm = (PersonChildForm) this.getBackingForm(this.getChildView());
        final PersonService mockPersonService = masterForm.getPersonService();

        final ActionCommand searchCommand = searchForm.getSearchCommand();

        final String property = "name";
        final ValueModel valueModel = childForm.getFormModel().getValueModel(property);
        final JTextField nameComponent = ((JTextField) SwingUtils.getDescendantNamed(property, childForm.getControl()));

        // Record expected behaviour
        final String nameAfterRefresh = "nameAfterRefresh";
        final Person personAfterRefresh = Person.createPerson(nameAfterRefresh);
        final List<Person> searchResults = TestMockAbstractBbTableMasterForm.PERSONS_1;
        final Person personToSelect = searchResults.get(0);

        EasyMock.expect(mockPersonService.searchPersons((Person) searchForm.getFormObject())).andReturn(searchResults);
        EasyMock.expect(mockPersonService.refreshPerson(personToSelect)).andReturn(personAfterRefresh);

        this.iMocksControl.replay();

        // (1). Begin new person creation
        SwingUtils.runInEventDispatcherThread(searchCommand);

        // (2). Change selection and make assertions
        masterForm.changeSelection(Arrays.asList(personToSelect));

        TestCase.assertTrue(ListUtils.isEqualList(Arrays.asList(personAfterRefresh), masterForm.getSelection()));
        TestCase.assertEquals(personAfterRefresh, childForm.getFormObject());
        TestCase.assertEquals(nameAfterRefresh, valueModel.getValue());
        TestCase.assertEquals(nameAfterRefresh, nameComponent.getText());
    }
    
    /**
     * Tests the correct behaviour of refresh command.
     */
    @Test
    public void testRefreshCommand() {

        final PersonMasterForm masterForm = (PersonMasterForm) this.getBackingForm(this.getMasterView());
        final PersonSearchForm searchForm = (PersonSearchForm) this.getBackingForm(this.getSearchView());
        final PersonChildForm childForm = (PersonChildForm) this.getBackingForm(this.getChildView());
        final PersonService mockPersonService = masterForm.getPersonService();

        final ActionCommand searchCommand = searchForm.getSearchCommand();
        final ActionCommand refreshCommand = masterForm.getRefreshCommand();

        final String property = "name";
        final ValueModel valueModel = childForm.getFormModel().getValueModel(property);
        final JTextField nameComponent = ((JTextField) SwingUtils.getDescendantNamed(property, childForm.getControl()));

        // Record expected behaviour
        final String nameAfter1stRefresh = "nameAfter1stRefresh";
        final String nameAfter2ndRefresh = "nameAfter2ndRefresh";
        final Person personAfter1stRefresh = Person.createPerson(nameAfter1stRefresh);
        final Person personAfter2ndRefresh = Person.createPerson(nameAfter2ndRefresh);
        final List<Person> searchResults = TestMockAbstractBbTableMasterForm.PERSONS_1;
        final Person personToSelect = searchResults.get(0);

        EasyMock.expect(mockPersonService.searchPersons((Person) searchForm.getFormObject())).andReturn(searchResults);
        EasyMock.expect(mockPersonService.refreshPerson(personToSelect)).andReturn(personAfter1stRefresh);
        EasyMock.expect(mockPersonService.refreshPerson(personAfter1stRefresh)).andReturn(personAfter2ndRefresh);

        this.iMocksControl.replay();

        // (1). Begin new person creation
        SwingUtils.runInEventDispatcherThread(searchCommand);

        // (2). Change selection and make assertions
        masterForm.changeSelection(Arrays.asList(personToSelect));

        TestCase.assertTrue(ListUtils.isEqualList(Arrays.asList(personAfter1stRefresh), masterForm.getSelection()));
        TestCase.assertEquals(personAfter1stRefresh, childForm.getFormObject());
        TestCase.assertEquals(nameAfter1stRefresh, valueModel.getValue());
        TestCase.assertEquals(nameAfter1stRefresh, nameComponent.getText());
        
        // (3). Call refresh and make assertions
        SwingUtils.runInEventDispatcherThread(refreshCommand);

        TestCase.assertTrue(ListUtils.isEqualList(Arrays.asList(personAfter2ndRefresh), masterForm.getSelection()));
        TestCase.assertEquals(personAfter2ndRefresh, childForm.getFormObject());
        TestCase.assertEquals(nameAfter2ndRefresh, valueModel.getValue());
        TestCase.assertEquals(nameAfter2ndRefresh, nameComponent.getText());
    }
}
