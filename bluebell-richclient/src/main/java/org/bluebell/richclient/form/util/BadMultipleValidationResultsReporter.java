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

package org.bluebell.richclient.form.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.bluebell.richclient.bean.Problem;
import org.springframework.binding.validation.ValidationMessage;
import org.springframework.binding.validation.ValidationResults;
import org.springframework.binding.validation.ValidationResultsModel;
import org.springframework.richclient.form.ValidationResultsReporter;
import org.springframework.util.Assert;

/**
 * Reproter encargado de almacenar los errores de validación que se producen en una página, en un objeto de tipo
 * <code>ProblemsTable</code>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BadMultipleValidationResultsReporter implements ValidationResultsReporter {

    /**
     * Identificador del formulario para el que se registran sus errores de validación.
     */
    private final String formId;

    /**
     * Tabla de problemas que se empleara como contenedor para los errores de validación capturados.
     */
    private final ProblemsTable messagesReceiver;

    /** ResultsModel containing the messages. */
    private final ValidationResultsModel resultsModel;

    /**
     * Constructor.
     * 
     * @param formId
     *            identificador del formulario observado.
     * @param resultsModel
     *            <code>ValidatingResultsModel</code> monitorizado y sobre el que se realizan reportes.
     * @param messagesReceiver
     *            tabla de errores en la que se registran los errores de validación.
     */
    public BadMultipleValidationResultsReporter(final String formId, final ValidationResultsModel resultsModel,
            final ProblemsTable messagesReceiver) {

        Assert.notNull(resultsModel, "resultsModel is required");
        Assert.notNull(messagesReceiver, "messagePane is required");
        this.formId = formId;
        this.messagesReceiver = messagesReceiver;
        this.resultsModel = resultsModel;
        this.init();
    }

    /**
     * Limpia el messageReceiver.
     */
    public final void clearErrors() {

        this.messagesReceiver.clear();
    }

    /**
     * @see org.springframework.richclient.form.ValidationResultsReporter#hasErrors()
     * 
     * @return indica si existen errores en el modelo.
     */
    public final boolean hasErrors() {

        return this.resultsModel.getHasErrors();
    }

    /**
     * Maneja un cambio en el validation results model reflejandolo en el messageReciver.
     * 
     * @param results
     *            resultados de la validación
     */
    public final void validationResultsChanged(ValidationResults results) {

        if (this.resultsModel.getMessageCount() == 0) {
            this.messagesReceiver.clear();
        } else {
            this.messagesReceiver.clear();
            this.messagesReceiver.addValidationProblems(this.formId, this.getValidationMessages(this.resultsModel));
        }
    }

    /**
     * Genera una colección con los errores de validación que posee actualmente el resultsModel.
     * 
     * @param resultsModel
     *            modelo que contiene los errores de validación.
     * @return colección de problemas de validación en el modelo.
     */
    @SuppressWarnings("unchecked")
    protected final Collection<Problem> getValidationMessages(final ValidationResults resultsModel) {

        final Collection<Problem> problems = new ArrayList<Problem>();

        for (final Iterator<ValidationMessage> i = resultsModel.getMessages().iterator(); i.hasNext();) {
            final ValidationMessage message = i.next();
            if (message != null) {
                // TODO Definir política para la generación de los IDs de
                // los errores.
                problems.add(new Problem(message.getSeverity(), 0, message.getMessage()));

            }
        }

        return problems;

    }

    /**
     * Inicializa el listener y lanza el chequeo inicial.
     */
    private void init() {

        this.resultsModel.addValidationListener(this);
        this.validationResultsChanged(null);
    }
}
