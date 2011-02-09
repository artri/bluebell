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

package org.bluebell.richclient.form;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;
import org.bluebell.richclient.form.util.BbFormModelHelper;
import org.springframework.binding.validation.ValidationMessage;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.dialog.DefaultMessageAreaModel;
import org.springframework.richclient.dialog.Messagable;
import org.springframework.util.Assert;

/**
 * Formulario genérico para la visualización de errores y conflictos.
 * 
 * @param <T>
 *            the type of the entities being edited.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbValidationForm<T> extends ApplicationWindowAwareForm {

    /**
     * Nombre del formulario.
     */
    private static final String FORM_NAME = "problemsForm";

    /**
     * TODO.
     */
    private Messagable messagable;

    /**
     * The list of validation messages.
     */
    private List<ValidationMessage> validationMessages = new ArrayList<ValidationMessage>();

    /**
     * The associated master form.
     */
    private AbstractBbMasterForm<T> masterForm;

    /**
     * Gets the masterForm.
     * 
     * @return the masterForm
     */
    public AbstractBbMasterForm<T> getMasterForm() {

        return this.masterForm;
    }

    /**
     * Sets the masterForm.
     * 
     * @param masterForm
     *            the masterForm to set
     */
    public void setMasterForm(AbstractBbMasterForm<T> masterForm) {

        // (JAF), 20110128, may be null (i.e.: after removing master view)
        // Assert.notNull(masterForm, "masterForm");

        this.masterForm = masterForm;
    }

    /**
     * Gets the validationMessages.
     * 
     * @return the validationMessages
     */
    public List<ValidationMessage> getValidationMessages() {

        return this.validationMessages;
    }

    /**
     * Sets the validationMessages.
     * 
     * @param validationMessages
     *            the validationMessages to set
     */
    public void setValidationMessages(List<ValidationMessage> validationMessages) {

        Assert.notNull(validationMessages, "validationMessages");

        this.validationMessages = validationMessages;
    }

    /**
     * Constructor por defecto.
     */
    public BbValidationForm() {

        super(BbValidationForm.FORM_NAME);
        this.setFormModel(BbFormModelHelper.createFormModel(StringUtils.EMPTY)); // TODO
        this.setMessagable(new DefaultMessageAreaModel() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void setMessage(Message message) {

                // TODO Auto-generated method stub
                super.setMessage(message);

                if (message == null) {
                    BbValidationForm.this.validationMessages.clear();
                } else if (message instanceof ValidationMessage) {
                    BbValidationForm.this.validationMessages.add((ValidationMessage) message);
                }
            }

        });
    }

    /**
     * Gets the messagable.
     * 
     * @return the messagable
     */
    public Messagable getMessagable() {

        return this.messagable;
    }

    /**
     * Sets the messagable.
     * 
     * @param messagable
     *            the messagable to set
     */
    public void setMessagable(Messagable messagable) {

        Assert.notNull(messagable, "messagable");

        this.messagable = messagable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent createFormControl() {

        return new JPanel();
    }
}
