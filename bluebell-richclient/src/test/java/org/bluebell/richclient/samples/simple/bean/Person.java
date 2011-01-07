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
package org.bluebell.richclient.samples.simple.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.util.Assert;

/**
 * A simple entity.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class Person implements Comparable<Person>, Serializable {

    /**
     * This is a <code>Serializable</code> class.
     */
    private static final long serialVersionUID = -8022101507400932844L;

    /**
     * The sex.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public enum Sex {
        /**
         * Male.
         */
        MALE,
        /**
         * Female.
         */
        FEMALE
    };
    
    /**
     * The person name.
     */
    private String name;

    /**
     * The person age.
     */
    private Long age;

    /**
     * The person sex.
     */
    private Sex sex;

    /**
     * The person address.
     */
    private String address;

    /**
     * The vets of this person.
     */
    private Collection<Vet> vets;

    /**
     * Constructs the person.
     */
    public Person() {

        super();
        this.vets = new ArrayList<Vet>();
    }

    /**
     * Constructs the person given its name.
     * 
     * @param name
     *            the name.
     */
    public Person(String name) {

        this();
        this.setName(name);
    }
    
    /**
     * Adds vets to this person.
     * 
     * @param vets
     *            the vets to be added.
     * @return <code>this</code>.
     */
    public Person addVets(Collection<Vet> vets) {

        Assert.notNull(vets, "vets");

        for (Vet vet : vets) {
            this.addVet(vet);
        }

        return this;
    }

    /**
     * Adds a vet to this person.
     * 
     * @param vet
     *            the vet to be added.
     * @return <code>this</code>.
     */
    public Person addVet(Vet vet) {

        vet.setOwner(this);
        this.vets.add(vet);

        return this;
    }

    /**
     * Removes a vet from this person.
     * 
     * @param vet
     *            the vet to be removed.
     * @return <code>this</code>.
     */
    public Person removeVet(Vet vet) {

        this.vets.remove(vet);
        vet.setOwner(null);

        return this;
    }

    /**
     * Gets the person name.
     * 
     * @return the person name.
     */
    public String getName() {

        return this.name;
    }

    /**
     * Sets the person name.
     * 
     * @param name
     *            the person name.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Gets the person age.
     * 
     * @return the person age.
     */
    public Long getAge() {

        return this.age;
    }

    /**
     * Sets the person age.
     * 
     * @param age
     *            the person age.
     */
    public void setAge(Long age) {

        this.age = age;
    }

    /**
     * Gets the sex.
     * 
     * @return the sex.
     */
    public Sex getSex() {

        return this.sex;
    }

    /**
     * Sets the sex.
     * 
     * @param sex
     *            the sex to set.
     */
    public void setSex(Sex sex) {

        this.sex = sex;
    }

    /**
     * Gets the address.
     * 
     * @return the address.
     */
    public String getAddress() {

        return this.address;
    }

    /**
     * Sets the address.
     * 
     * @param address
     *            the address to set.
     */
    public void setAddress(String address) {

        this.address = address;
    }

    /**
     * Gets the vets.
     * 
     * @return the vets.
     */
    public Collection<Vet> getVets() {

        return Collections.unmodifiableCollection(this.vets);
    }

    /**
     * Sets the vets.
     * 
     * @param vets
     *            the vets to set.
     */
    public void setVets(Collection<Vet> vets) {

        Assert.notNull(vets, "vets");

        this.vets.clear();
        this.addVets(vets);
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object object) {

        if (!(object instanceof Person)) {
            return Boolean.FALSE;
        }
        final Person person = (Person) object;

        return new EqualsBuilder().append(this.getName(), person.getName()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        final int hashCode1 = -1337536253;
        final int hashCode2 = -2021982281;

        return new HashCodeBuilder(hashCode1, hashCode2).append(this.getName()).toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Person object) {

        return new CompareToBuilder().append(this.getName(), object.getName()).toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", this.getName()).toString();
    }

    /**
     * Creates arbitrary persons.
     * 
     * @param size
     *            the number of persons to create.
     * 
     * @return the persons.
     */
    public static List<Person> createPersons(Integer size) {

        Assert.isTrue(size > 0, "size>0");

        final List<Person> persons = new ArrayList<Person>(size);
        for (int i = 0; i < size; ++i) {
            persons.add(Person.createPerson());
        }

        return persons;
    }

    /**
     * Creates an arbitrary person.
     * 
     * @return the person.
     */
    public static Person createPerson() {

        final Long number = RandomUtils.nextLong();
        final String string = number.toString();

        final Person person = new Person(string);
        person.setAddress(string);
        person.setAge(number);
        person.setSex((number % 2 == 0) ? Sex.MALE : Sex.FEMALE);

        return person;
    }
}
