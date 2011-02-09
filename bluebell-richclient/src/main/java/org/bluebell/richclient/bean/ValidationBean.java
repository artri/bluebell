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

package org.bluebell.richclient.bean;

import org.springframework.richclient.application.support.ApplicationServicesAccessor;
import org.springframework.richclient.core.Severity;

/**
 * Bean que envuelve un objeto de tipo <code>Problem</code>, para adaptarlo a las necesidades de la vista.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ValidationBean extends ApplicationServicesAccessor {

    // TODO, (JAF), 20091229, to be removed
    /**
     * Objeto de tipo <code>Problem</code> envuelto.
     */
    private final Problem problem;

    // /**
    // * Manejador de recursos empleado para recuperar la ruta de los iconos.
    // */
    // private ResourceBundle resources;

    /**
     * Icono asociado al problema.
     */
    // private ImageIcon tipeProblemIcon;

    /**
     * Crea el <em>bean</em> instanciando un nuevo {@link Problem}.
     */
    public ValidationBean() {

        this.problem = new Problem();
    }

    /**
     * Construye un bean <code>ProblemBean</code> a partir del objeto <code>Problem</code> envuelto.
     * 
     * @param poblem
     *            objeto <code>Problem</code> envuelto.
     */
    @SuppressWarnings("deprecation")
    public ValidationBean(Problem poblem) {

        this.problem = poblem;
        // this.resources =
        // ResourceBundle.getBundle("org.bluebell.richclient.test.images.bluebell");

        // final String problemsFormErrorIcon = null;
        // String description = null;

        // Se recupera el icono en función del tipo de error.
        if (Severity.ERROR.equals(this.problem.getSeverity())) {
            this.getClass(); // "Avoid CS warnings"
            // problemsFormErrorIcon =
            // this.resources.getString("problemsForm.error.icon");
            // description = "Error";
        } else if (Severity.WARNING.equals(this.problem.getSeverity())) {
            // problemsFormErrorIcon =
            // this.resources.getString("problemsForm.warning.icon");
            // description = "Advertencia";
            this.getClass(); // Avoids CS warning
        }

        // this.tipeProblemIcon = this.createImageIcon(problemsFormErrorIcon,
        // description);
    }

    /**
     * Crea el <em>bean</em> con una severidad, código y mensaje.
     * 
     * @param severity
     *            la severidad del error.
     * @param code
     *            el código del error. Para internacionalización.
     * @param message
     *            el mensaje asociado al error.
     */
    public ValidationBean(Severity severity, int code, String message) {

        this(new Problem(severity, code, message));
    }

    /**
     * Produce el mensaje descriptivo del error.
     * 
     * @return mensaje descriptivo del error.
     */
    public String getDescription() {

        return this.problem.getDescription();
    }

    // /**
    // * Produce el icono asociado al tipo error.
    // *
    // * @return icono asociado al tipo de error.
    // */
    // public ImageIcon getTipeProblemIcon() {
    //
    // return this.tipeProblemIcon;
    // }

    // /**
    // * Retorna un icono, o null si el path es inválido.
    // *
    // * @param path
    // * el <em>path</em>.
    // * @param description
    // * la descripción.
    // * @return el icono.
    // */
    // private ImageIcon createImageIcon(String path, String description) {
    //
    // final java.net.URL imgURL = this.getClass().getResource(path);
    // if (imgURL != null) {
    // return new ImageIcon(imgURL, description);
    // } else {
    // this.logger.warn("Couldn't find file: " + path);
    // return null;
    // }
    // }
}
