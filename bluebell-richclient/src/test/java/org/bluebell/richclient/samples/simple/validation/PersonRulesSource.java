/**
 * 
 */
package org.bluebell.richclient.samples.simple.validation;

import org.bluebell.richclient.samples.simple.bean.Person;
import org.springframework.rules.Rules;
import org.springframework.rules.support.DefaultRulesSource;

/**
 * Validation rules for class <code>Person</code>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class PersonRulesSource extends DefaultRulesSource {

    /**
     * Creates the validation rules.
     */
    public PersonRulesSource() {

        this.addRules(new Rules(Person.class) {

            /**
             * {@inheritDoc}
             */
            @Override
            protected void initRules() {

                this.addMinLength("name", 1);
            }
        });
    }
}
