package org.bluebell.richclient.bean;

import javax.swing.ImageIcon;

import org.springframework.richclient.application.support.ApplicationServicesAccessor;
import org.springframework.richclient.core.Severity;

/**
 * Bean que envuelve un objeto de tipo <code>Problem</code>, para adaptarlo a las necesidades de la vista.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ProblemBean extends ApplicationServicesAccessor {

    /**
     * Objeto de tipo <code>Problem</code> envuelto.
     */
    private final Problem problem;

//    /**
//     * Manejador de recursos empleado para recuperar la ruta de los iconos.
//     */
//    private ResourceBundle resources;

    /**
     * Icono asociado al problema.
     */
    private ImageIcon tipeProblemIcon;

    /**
     * Crea el <em>bean</em> instanciando un nuevo {@link Problem}.
     */
    public ProblemBean() {

	this.problem = new Problem();
    }

    /**
     * Construye un bean <code>ProblemBean</code> a partir del objeto <code>Problem</code> envuelto.
     * 
     * @param poblem
     *            objeto <code>Problem</code> envuelto.
     */
    public ProblemBean(Problem poblem) {

	this.problem = poblem;
	// this.resources = ResourceBundle.getBundle("org.bluebell.richclient.test.images.bluebell");

	// final String problemsFormErrorIcon = null;
	// String description = null;

	// Se recupera el icono en función del tipo de error.
	if (Severity.ERROR.equals(this.problem.getSeverity())) {
	    // problemsFormErrorIcon =
	    // this.resources.getString("problemsForm.error.icon");
//	    description = "Error";
	} else if (Severity.WARNING.equals(this.problem.getSeverity())) {
	    // problemsFormErrorIcon =
	    // this.resources.getString("problemsForm.warning.icon");
//	    description = "Advertencia";
	}

	// this.tipeProblemIcon = this.createImageIcon(problemsFormErrorIcon, description);
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
    public ProblemBean(Severity severity, int code, String message) {

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

    /**
     * Produce el icono asociado al tipo error.
     * 
     * @return icono asociado al tipo de error.
     */
    public ImageIcon getTipeProblemIcon() {

	return this.tipeProblemIcon;
    }

//    /**
//     * Retorna un icono, o null si el path es inválido.
//     * 
//     * @param path
//     *            el <em>path</em>.
//     * @param description
//     *            la descripción.
//     * @return el icono.
//     */
//    private ImageIcon createImageIcon(String path, String description) {
//
//	final java.net.URL imgURL = this.getClass().getResource(path);
//	if (imgURL != null) {
//	    return new ImageIcon(imgURL, description);
//	} else {
//	    this.logger.warn("Couldn't find file: " + path);
//	    return null;
//	}
//    }
}
