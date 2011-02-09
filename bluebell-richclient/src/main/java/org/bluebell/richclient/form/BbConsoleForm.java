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

import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.apache.commons.lang.StringUtils;
import org.bluebell.richclient.application.config.BbApplicationConfig;
import org.bluebell.richclient.form.util.BbFormModelHelper;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.richclient.factory.ComponentFactory;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

/**
 * Form used under development in order to show a debugging console component.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbConsoleForm extends ApplicationWindowAwareForm {

    /*
     * TODO, (JAF), 20101116, make this form useful, maybe listening application events, maybe working as a log4j (sl4j)
     * appender... and making it non "applicationConfig" dependent
     */

    /**
     * Nombre del formulario.
     */
    private static final String FORM_NAME = "consoleForm";

    /**
     * TODO, there should be a way to publish console contents. By now this is "hardcoded".
     */
    private MultiValueMap<String, String[]> applicationConfig;

    /**
     * Gets the application config.
     * 
     * @return the application config.
     */
    protected final MultiValueMap<String, String[]> getApplicationConfig() {

        return this.applicationConfig;
    }

    /**
     * Sets the application config.
     * 
     * @param applicationConfig
     *            the application config to set.
     */
    public final void setApplicationConfig(MultiValueMap<String, String[]> applicationConfig) {

        Assert.notNull(applicationConfig, "applicationConfig");

        this.applicationConfig = applicationConfig;
    }

    /**
     * Constructor por defecto.
     */
    public BbConsoleForm() {

        super(BbConsoleForm.FORM_NAME);
        this.setFormModel(BbFormModelHelper.createFormModel(StringUtils.EMPTY)); // TODO
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent createFormControl() {

        final ComponentFactory componentFactory = (ComponentFactory) //
        this.getApplicationServices().getService(ComponentFactory.class);

        final JTextArea console = componentFactory.createTextArea();
        console.append(BbApplicationConfig.debugPrint((ConfigurableApplicationContext) this.getApplicationContext()));
        console.append(BbApplicationConfig.debugPrint(this.getApplicationConfig()));

        return console;
    }

}
