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
package org.bluebell.richclient.samples.simple.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;

import org.bluebell.richclient.form.AbstractBbSearchForm;
import org.bluebell.richclient.form.util.BbFormModelHelper;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.springframework.richclient.form.binding.swing.SwingBindingFactory;
import org.springframework.richclient.form.builder.TableFormBuilder;

/**
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class PersonSearchForm extends AbstractBbSearchForm<Person, Person> {

    /**
     */
    public PersonSearchForm() {

        super("personSearchForm");
        this.setFormModel(BbFormModelHelper.createValidatingFormModel(new Person(), this.getId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Person> doSearch(Person searchParams) {

        final List<Person> searchResults = new ArrayList<Person>();
        searchResults.add(searchParams);
        searchParams.setName(searchParams.getName().concat("foo"));
        searchResults.add(searchParams);
        searchParams.setName(searchParams.getName().concat("foo"));
        searchResults.add(searchParams);
        searchParams.setName(searchParams.getName().concat("foo"));
        searchResults.add(searchParams);
        searchParams.setName(searchParams.getName().concat("foo"));
        searchResults.add(searchParams);
        searchParams.setName(searchParams.getName().concat("foo"));

        return searchResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent createSearchParamsControl() {

        final SwingBindingFactory bindingFactory = (SwingBindingFactory) this.getBindingFactory();
        final TableFormBuilder formBuilder = new TableFormBuilder(bindingFactory);

        formBuilder.add("name");
        formBuilder.row();
        formBuilder.add("age");
        formBuilder.row();

        return formBuilder.getForm();
    }
}
