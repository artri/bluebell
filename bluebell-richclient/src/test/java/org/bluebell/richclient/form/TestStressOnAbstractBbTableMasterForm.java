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

import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.commons.collections.ListUtils;
import org.bluebell.richclient.application.support.FormBackedView;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.samples.simple.bean.Vet;
import org.bluebell.richclient.samples.simple.form.PersonMasterForm;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.text.TimeFormat;
import org.springframework.test.context.ContextConfiguration;

/**
 * Load test of {@link AbstractBbTableMasterForm}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration
public class TestStressOnAbstractBbTableMasterForm extends AbstractBbSamplesTests {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestStressOnAbstractBbTableMasterForm.class);

    /**
     * The mock master view descriptor bean name.
     */
    protected static final String MOCK_MASTER_VIEW_DESCRIPTOR_BEAN_NAME = "mockPersonMasterViewDescriptor";

    /**
     * The page descriptor used for testing.
     */
    @Autowired
    private PageDescriptor pageDescriptor;

    /**
     * {@inheritDoc}
     */
    @Test
    public void testDependencyInjection() {

        TestCase.assertNotNull("pageDescriptor", this.pageDescriptor);
    }

    /**
     * Tests the correct behaviour of <code>showEntities</code> when leading with a huge amount of rows.
     */
    @Test
    public void testStressOnShowEntities() {

        final int numberOfPersons = 10000;

        final List<Person> persons = Person.createPersons(numberOfPersons);
        final AbstractBbTableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());

        if (TestStressOnAbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
            TestStressOnAbstractBbTableMasterForm.LOGGER.debug("Before showing entities");
        }

        final Long before = System.nanoTime();
        masterForm.showEntities(persons);
        final Long after = System.nanoTime();

        final String employedTime = TimeFormat.getMillisecondsInstance().format(//
                TimeUnit.NANOSECONDS.toMillis(after - before));

        if (TestStressOnAbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
            TestStressOnAbstractBbTableMasterForm.LOGGER.debug(//
                    "After showing entities. Time employed is " + employedTime);
        }
    }

    /**
     * Tests the correct behaviour of <code>changeSelection</code> when leading with a huge amount of rows.
     */
    @Test
    public void testStressOnRefresh() {

        final int numberOfPersons = 1;

        final List<Person> persons = Person.createPersons(numberOfPersons);
        final AbstractBbTableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());

        masterForm.showEntities(persons);

        if (TestStressOnAbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
            TestStressOnAbstractBbTableMasterForm.LOGGER.debug("BEFORE refreshing entity");
        }

        final Long before = System.nanoTime();
        masterForm.changeSelection(persons);
        final Long after = System.nanoTime();

        final String employedTime = TimeFormat.getMillisecondsInstance().format(//
                TimeUnit.NANOSECONDS.toMillis(after - before));

        if (TestStressOnAbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
            TestStressOnAbstractBbTableMasterForm.LOGGER.debug(//
                    "After refreshing entity. Time employed is " + employedTime);
        }
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

        if (TestStressOnAbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
            TestStressOnAbstractBbTableMasterForm.LOGGER.debug("BEFORE cleaning master event list");
        }

        final AbstractBbTableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        masterForm.showEntities(ListUtils.EMPTY_LIST);

        if (TestStressOnAbstractBbTableMasterForm.LOGGER.isDebugEnabled()) {
            TestStressOnAbstractBbTableMasterForm.LOGGER.debug("AFTER cleaning master event list");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FormBackedView<AbstractB2TableMasterForm<Person>> getMasterView() {

        return this.getApplicationPage().getView(TestAbstractBbTableMasterForm.MOCK_MASTER_VIEW_DESCRIPTOR_BEAN_NAME);
    }

    /**
     * Mock version of person master form.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class MockPersonMasterForm extends PersonMasterForm {

        /**
         * Default constructor.
         */
        public MockPersonMasterForm() {

            super();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<Person> doRefresh(List<Person> persons) {

            final int numberOfVets = 10000;

            for (Person person : persons) {

                final List<Vet> vets = Vet.createVets(numberOfVets);
                person.addVets(vets);
            }

            return persons;
        }
    }
}
