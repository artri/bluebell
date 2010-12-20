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

package org.bluebell.richclient.form.builder.support;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.JComponent;

import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.validation.ValidationMessage;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.statusbar.StatusBar;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.form.builder.support.AbstractFormComponentInterceptor;

/**
 * Interceptor que actualiza la <em>status bar</em> con información relativa al componente del formulario seleccionado.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class UpdateStatusBarInterceptor extends AbstractFormComponentInterceptor {

    /**
     * Construye el interceptor a partir del modelo del formulario.
     * 
     * @param formModel
     *            el modelo del formulario.
     */
    public UpdateStatusBarInterceptor(FormModel formModel) {

        super(formModel);
    }

    /**
     * Registra un <em>listener</em> contra el <code>ValueModel</code> de la propiedad dada para cambiar su mensaje.
     * 
     * @param propertyName
     *            la propiedad.
     * @param component
     *            el componente que representa la propiedad.
     */
    @Override
    public void processComponent(final String propertyName, final JComponent component) {

        final ValueModel valueModel = this.getFormModel().getValueModel(propertyName);

        // Actualizar la status bar cuando cambie un valor
        valueModel.addValueChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {

                UpdateStatusBarInterceptor.//
                this.updateStatusBar(propertyName);
            }
        });

        // Actualizar la status bar cuando cambie el componente seleccionado
        component.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {

                UpdateStatusBarInterceptor.//
                this.updateStatusBar(propertyName);
            }

            public void focusLost(FocusEvent e) {

                final ApplicationWindow window = Application.instance().getActiveWindow();
                final StatusBar statusBar = (window == null) ? null : window.getStatusBar();

                if (statusBar != null) {
                    statusBar.setErrorMessage((String) null);
                }
            }
        });
    }

    /**
     * Obtiene el primer mensaje de validación asociado a la propiedad dada.
     * 
     * @param propertyName
     *            el nombre de la propiedad.
     * @return el mensaje de validación y <code>null</code> en caso de que no hubiera ningún error.
     */
    @SuppressWarnings("unchecked")
    private Message getValidationMessage(String propertyName) {

        if (this.getFormModel() instanceof ValidatingFormModel) {
            final ValidatingFormModel validatingFormModel = (ValidatingFormModel) this.getFormModel();

            // Los mensajes de validación de la propiedad dada
            final Set<ValidationMessage> messages = validatingFormModel.//
                    getValidationResults().getMessages(propertyName);

            if (!messages.isEmpty()) {
                return messages.iterator().next();
            }
        }

        return null;
    }

    /**
     * Actualiza la barra de estado de la ventana activa con información relativa a la propiedad dada.
     * 
     * @param propertyName
     *            el nombre de la propiedad.
     */
    private void updateStatusBar(String propertyName) {

        final ApplicationWindow window = Application.instance().getActiveWindow();
        final StatusBar statusBar = (window == null) ? null : window.getStatusBar();
        final String displayName = this.getFormModel().getFieldFace(propertyName).getDisplayName();
        final String caption = this.getFormModel().getFieldFace(propertyName).getCaption();

        if (statusBar != null) {
            final Message validatingMessage = UpdateStatusBarInterceptor.//
            this.getValidationMessage(propertyName);

            statusBar.setErrorMessage(validatingMessage);
            if (validatingMessage == null) {
                statusBar.setMessage((caption == null) ? displayName : caption);
            }
        }
    }
}
