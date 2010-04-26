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

import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.richclient.text.TimeFormat;

/**
 * Load test of {@link AbstractBbTableMasterForm}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestStressOnAbstractBb0TableMasterForm extends AbstractBbSamplesTests {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestStressOnAbstractBb0TableMasterForm.class);

    /**
     * Tests the correct behaviour of <code>showEntities</code> when leading with a huge amount of rows.
     */
    @Test
    public void testLoadShowEntities() {

        final int numberOfPersons = 100;
        final List<Person> persons = Person.createPersons(numberOfPersons);
        final AbstractBbTableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());

        if (TestStressOnAbstractBb0TableMasterForm.LOGGER.isDebugEnabled()) {
            TestStressOnAbstractBb0TableMasterForm.LOGGER.debug("BEFORE showing entities");
        }
        final long before = System.nanoTime();
        masterForm.showEntities(persons);
        final long after = System.nanoTime();

        final String employedTime = TimeFormat.getMillisecondsInstance().format((after - before) / 1000000);
        if (TestStressOnAbstractBb0TableMasterForm.LOGGER.isDebugEnabled()) {
            TestStressOnAbstractBb0TableMasterForm.LOGGER.debug(//
                    "After showing entities. Time employed is " + employedTime);
        }
    }

    /**
     * Method invoked at startup.
     * <p/>
     * Initializes test cases.
     */
    @Before
    public void startup() {

        this.initializeVariables(this.getPersonPageDescriptor());
    }

    /**
     * Cleans master event list after every test execution.
     */
    @SuppressWarnings("unchecked")
    @After
    public void cleanMasterEventList() {

        System.out.println("BEFORE: cleanMasterEventList");
        final AbstractBbTableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());
        masterForm.showEntities(ListUtils.EMPTY_LIST);
        System.out.println("AFTER: cleanMasterEventList");
    }
}
