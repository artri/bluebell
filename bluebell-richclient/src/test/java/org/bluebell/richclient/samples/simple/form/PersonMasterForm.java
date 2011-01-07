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
import org.bluebell.richclient.samples.simple.bean.Vet;
import org.bluebell.richclient.util.ObjectUtils;
import org.springframework.util.Assert;

/**
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 * 
 */
public class PersonMasterForm extends AbstractB2TableMasterForm<Person> {

    /**
     */
    public PersonMasterForm() {

        super("personMasterForm", Person.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Person doDelete(Person person) {

        return this.mockOperation(person);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Person doInsert(Person person) {

        return this.mockOperation(person);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Person> doRefresh(List<Person> persons) {

        final int numberOfVets = 1000;

        for (Person person : persons) {

            final List<Vet> vets = Vet.createVets(numberOfVets);
            person.addVets(vets);
        }

        return persons;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Person doUpdate(Person person) {

        return this.mockOperation(person);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getColumnPropertyNames() {

        return new String[] { "name", "age", "name", "age", "name", "age", "name", "age" };
    }

    /**
     * Makes a shallow copy of the given person. Useful for mocking <code>doXXX</code> operations.
     * 
     * @param person
     *            the target person.
     * @return a shallow copy.
     */
    private Person mockOperation(Person person) {

        Assert.notNull(person, "person");

        final Person newPerson = new Person();

        ObjectUtils.shallowCopy(person, newPerson);

        return newPerson;
    }
}
