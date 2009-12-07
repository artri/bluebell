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

    private String name;

    private Long age;

    public Person() {

    }

    public String getName() {
	return this.name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public Long getAge() {
	return this.age;
    }

    public void setAge(Long age) {
	this.age = age;
    }
}
