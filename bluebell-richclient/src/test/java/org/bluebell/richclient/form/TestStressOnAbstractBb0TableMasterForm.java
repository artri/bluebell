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
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.samples.simple.bean.Person.Sex;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

/**
 * Load test of {@link AbstractBbTableMasterForm}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestStressOnAbstractBb0TableMasterForm extends AbstractBbSamplesTests {
    
    /**
     * Tests the correct behaviour of <code>showEntities</code> when leading with a huge amount of rows.
     */
    @Test
    public void testLoadShowEntities() {

        final List<Person> persons = this.createPersons(2);
        final AbstractBbTableMasterForm<Person> masterForm = this.getBackingForm(this.getMasterView());

        System.out.println("BEFORE: testLoadShowEntities");
        
        final long before = System.nanoTime();
        masterForm.showEntities(persons);
        final long after = System.nanoTime();
        
        System.out.println(after - before);
        System.out.println("AFTER: testLoadShowEntities");
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

    /**
     * Creates aribitrary persons.
     * 
     * @param size
     *            the number of persons to create.
     * 
     * @return the persons.
     */
    private List<Person> createPersons(Integer size) {

        Assert.isTrue(size > 0, "size>0");

        final List<Person> persons = new ArrayList<Person>(size);
        for (int i = 0; i < size; ++i) {
            persons.add(this.createPerson());
        }

        return persons;
    }

    /**
     * Creates an aribitrary person.
     * 
     * @return the person.
     */
    private Person createPerson() {

        final Long number = RandomUtils.nextLong();
        final String string = number.toString();

        final Person person = new Person(string);
        person.setAddress(string);
        person.setAge(number);
        person.setSex((number % 2 == 0) ? Sex.MALE : Sex.FEMALE);

        return person;
    }
}
