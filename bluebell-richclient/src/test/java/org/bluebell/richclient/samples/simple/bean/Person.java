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

    /**
     * The person name.
     */
    private String name;

    /**
     * The person age.
     */
    private Long age;

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
}
