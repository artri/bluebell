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
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.util.Assert;

/**
 * A simple abstraction for a vet.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class Vet implements Comparable<Vet>, Serializable {

    /**
     * This is a <code>Serializable</code> class.
     */
    private static final long serialVersionUID = 5168403607370095869L;

    /**
     * The vet name.
     */
    private String name;

    /**
     * The name of the vet. /** The owner of the vet.
     */
    private Person owner;

    /**
     * Creates the vet.
     */
    public Vet() {

        super();
    }

    /**
     * Creates the vet given its name.
     * 
     * @param name
     *            the vet name.
     */
    public Vet(String name) {

        super();

        this.setName(name);
    }

    /**
     * Gets the name.
     * 
     * @return the name.
     */
    public String getName() {

        return this.name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the name to set.
     */
    public void setName(String name) {

        Assert.notNull(name, "name");

        this.name = name;
    }

    /**
     * Gets the owner.
     * 
     * @return the owner.
     */
    public Person getOwner() {

        return this.owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {

        if (!(object instanceof Vet)) {
            return Boolean.FALSE;
        }
        final Vet vet = (Vet) object;

        return new EqualsBuilder().append(this.getName(), vet.getName()).isEquals();
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
    public int compareTo(Vet object) {

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
     * Sets the owner.
     * 
     * @param owner
     *            the owner to set.
     */
    void setOwner(Person owner) {

        // Assert.notNull(owner, "owner"); // The owner may be null

        this.owner = owner;
    }

    /**
     * Creates arbitrary vets.
     * 
     * @param size
     *            the number of vets to create.
     * 
     * @return the persons.
     */
    public static List<Vet> createVets(Integer size) {

        Assert.isTrue(size > 0, "size>0");

        final List<Vet> vets = new ArrayList<Vet>(size);
        for (int i = 0; i < size; ++i) {
            vets.add(Vet.createVet());
        }

        return vets;
    }

    /**
     * Creates an arbitrary vet.
     * 
     * @return the vet.
     */
    public static Vet createVet() {

        final Long number = RandomUtils.nextLong();
        final String string = number.toString();

        final Vet vet = new Vet(string);

        return vet;
    }
}
