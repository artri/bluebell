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
package org.bluebell.richclient.samples.simple.service;

import java.util.List;

import org.bluebell.richclient.samples.simple.bean.Person;

/**
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public interface PersonService {

    /**
     * Inserts a person.
     * 
     * @param person
     *            the person to be inserted.
     * @return the inserted person.
     */
    Person insertPerson(Person person);

    /**
     * Updates a person.
     * 
     * @param person
     *            the person to be updated.
     * @return the updated person.
     */
    Person updatePerson(Person person);

    /**
     * Refreshes a person.
     * 
     * @param person
     *            the person to be refreshed.
     * @return the refreshed person.
     */
    Person refreshPerson(Person person);

    /**
     * Deletes a person.
     * 
     * @param person
     *            the person to be deleted.
     * @return the deleted person.
     */
    Person deletePerson(Person person);

    /**
     * Searches person according to the given params.
     * 
     * @param params
     *            the search params.
     * @return the search results.
     */
    List<Person> searchPersons(Person params);
}
