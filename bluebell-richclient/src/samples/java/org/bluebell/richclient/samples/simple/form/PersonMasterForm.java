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
package org.bluebell.richclient.samples.simple.form;

import java.util.List;

import org.bluebell.richclient.form.AbstractB2TableMasterForm;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.samples.simple.service.PersonService;
import org.springframework.util.Assert;

/**
 * The master form implementation for managing <code>Person</code>'s.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class PersonMasterForm extends AbstractB2TableMasterForm<Person> {

    /**
     * The default form id.
     */
    private static final String FORM_ID = "personMasterForm";

    /**
     * The person service.
     */
    private PersonService personService;

    /**
     * Creates the form.
     */
    public PersonMasterForm() {

        super(PersonMasterForm.FORM_ID, Person.class);
    }

    /**
     * Gets the person service.
     * 
     * @return the person service.
     */
    public final PersonService getPersonService() {

        return this.personService;
    }

    /**
     * Sets the person service.
     * 
     * @param personService
     *            the person service to set.
     */
    public final void setPersonService(PersonService personService) {

        Assert.notNull(personService, "personService");

        this.personService = personService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Person doDelete(Person person) {

        return this.getPersonService().deletePerson(person);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Person doInsert(Person person) {

        return this.getPersonService().insertPerson(person);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Person> doRefresh(List<Person> persons) {

        if (persons.size() == 1) {
            /*
             * Implementors should decide what to do with multiple selection
             * http://jirabluebell.b2b2000.com/browse/BLUE-58
             */
            persons.set(0, this.getPersonService().refreshPerson(persons.get(0)));
        }

        return persons;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Person doUpdate(Person person) {

        return this.getPersonService().updatePerson(person);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getColumnPropertyNames() {

        return new String[] { "name", "age", "name", "age", "name", "age", "name", "age" };
    }
}
