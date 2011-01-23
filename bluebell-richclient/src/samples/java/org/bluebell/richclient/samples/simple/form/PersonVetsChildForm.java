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
package org.bluebell.richclient.samples.simple.form;

import javax.swing.JComponent;

import org.bluebell.richclient.form.AbstractBbChildForm;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.form.binding.BindingFactory;
import org.springframework.richclient.form.builder.TableFormBuilder;

/**
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class PersonVetsChildForm extends AbstractBbChildForm<Person> {

    /**
     * 
     */
    private static final String FORM_ID = "personVetsDetailForm";

    /**
     */
    public PersonVetsChildForm() {

        this(PersonVetsChildForm.FORM_ID);
    }

    /**
     * Creates the form given its id.
     * 
     * @param formId
     *            the form id.
     */
    public PersonVetsChildForm(String formId) {

        super(formId);
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public Class<Person> getManagedType() {
         
        return Person.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFormModel(ValidatingFormModel formModel) {

        super.setFormModel(formModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent createFormControl() {

        final BindingFactory bindingFactory = this.getBindingFactory();
        final TableFormBuilder formBuilder = new TableFormBuilder(bindingFactory);

        formBuilder.add("vets");
        formBuilder.row();

        return formBuilder.getForm();
    }   
}
