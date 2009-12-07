/**
 * 
 */
package org.bluebell.richclient.samples.simple.form;

import org.bluebell.richclient.form.AbstractBb2TableMasterForm;
import org.bluebell.richclient.samples.simple.bean.Person;

/**
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 * 
 */
public class PersonMasterForm extends AbstractBb2TableMasterForm<Person> {

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

	return person;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Person doInsert(Person person) {

	return person;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Person doRefresh(Person person) {

	return person;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Person doUpdate(Person person) {

	return person;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getColumnPropertyNames() {

	return new String[] { "name", "age", "name", "age", "name", "age", "name", "age" };
    }
}
