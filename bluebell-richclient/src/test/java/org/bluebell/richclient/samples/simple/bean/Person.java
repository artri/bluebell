/**
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
package org.bluebell.richclient.samples.simple.bean;

/**
 * A simple entity.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class Person {

    private enum Sex {
        MALE, FEMALE
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
     * Constructs the person.
     */
    public Person() {

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
}
