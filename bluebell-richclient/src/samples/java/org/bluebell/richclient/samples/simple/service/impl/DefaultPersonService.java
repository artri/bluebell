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
package org.bluebell.richclient.samples.simple.service.impl;

import java.util.List;

import org.bluebell.richclient.samples.simple.bean.Person;
import org.bluebell.richclient.samples.simple.bean.Vet;
import org.bluebell.richclient.samples.simple.service.PersonService;
import org.bluebell.richclient.util.ObjectUtils;
import org.springframework.util.Assert;

/**
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class DefaultPersonService implements PersonService {

    /**
     * The number of entities to be mocked.
     */
    private static final Integer NUMBER_OF_ENTITIES = 1000;

    /**
     * {@inheritDoc}
     */
    @Override
    public Person insertPerson(Person person) {

        return this.mockOperation(person, "doInsert");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person updatePerson(Person person) {

        return this.mockOperation(person, "doUpdate");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person refreshPerson(Person person) {

        return this.mockOperation(person, "doRefresh");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person deletePerson(Person person) {

        return this.mockOperation(person, "doDelete");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Person> searchPersons(Person params) {

        return Person.createPersons(DefaultPersonService.NUMBER_OF_ENTITIES);
    }

    /**
     * Makes a shallow copy of the given person and fills some parameters.
     * <p>
     * Useful for mocking <code>doXXX</code> operations.
     * </p>
     * 
     * @param person
     *            the target person.
     * @return a shallow copy.
     */
    private Person mockOperation(Person person, String operation) {

        Assert.notNull(person, "person");
        Assert.notNull(person, "operation");

        final Person target = new Person();

        ObjectUtils.shallowCopy(person, target);

        target.setAddress(target.getName().concat(operation));
        target.addVets(Vet.createVets(DefaultPersonService.NUMBER_OF_ENTITIES));
        // target.addVets(Vet.createVets(1));

        return target;
    }
}
