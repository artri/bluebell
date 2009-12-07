package org.bluebell.richclient.form.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bluebell.richclient.bean.Problem;
import org.bluebell.richclient.bean.ProblemBean;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/**
 * Tabla para matener la colección de errores producidos en una página de la aplicación. La tabla distingue entre los
 * errores relacionados con la correcta estructura de un plan de organización docente, y los errores de validación para
 * los campos de un formulario. Ésto es, porque el primer tipo de errores aparecen resultado de ejecutar un comando, y
 * se añaden desde el mismo de forma explícita a la tabla de errores, mientras que los segundos son originados por
 * Hibernate Validator, e incorporados a la tabla mediante un <code>MultipleValidationResultsReporter</code>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ProblemsTable {

    /**
     * Lista que permite mostrar el contenido de la tabla de problemas en un una <code>JTable</code>.
     */
    private final EventList<ProblemBean> eventList;

    /**
     * Tabla con los errores sobre la correcta estuctura de un plan docente.
     */
    private final Map<String, Collection<ProblemBean>> modelProblems;

    /**
     * Tabla con los errores de validación.
     */
    private final Map<String, Collection<ProblemBean>> validationProblems;

    /**
     * Constructor por defecto.
     */
    public ProblemsTable() {

	this.eventList = new BasicEventList<ProblemBean>();
	this.modelProblems = new HashMap<String, Collection<ProblemBean>>();
	this.validationProblems = new HashMap<String, Collection<ProblemBean>>();
    }

    /**
     * Añade una colección de errores sobre la estructura de un plan de organización docente, o remplaza la colección
     * asociada a la etiqueta <code>id</code>, dentro de la tabla de errores en caso de que esta existiese.
     * 
     * @param id
     *            identificador de la colección dentro de la tabla de errores.
     * @param problems
     *            colección de problemas del modelo a incorporar.
     */
    public final void addModelProblems(final String id, final Collection<Problem> problems) {

	this.modelProblems.put(id, this.wrapProblems(problems));
	this.refreshEventList();
    }

    /**
     * Añade una colección de errores de validación, o remplaza la colección asociada a la etiqueta <code>id</code>,
     * dentro de la tabla de errores de validación en caso de que esta existiese.
     * 
     * @param id
     *            identificador de la colección dentro de la tabla de errores.
     * @param problems
     *            colección de problemas de validación a incorporar.
     */
    public final void addValidationProblems(final String id, final Collection<Problem> problems) {

	this.validationProblems.put(id, this.wrapProblems(problems));
	this.refreshEventList();
    }

    /**
     * Elimina todos los errores de la tabla de errores.
     */
    public final void clear() {

	this.modelProblems.clear();
	this.validationProblems.clear();
	this.eventList.clear();
    }

    /**
     * Elimina todos los errores del modelo de la tabla de errores.
     */
    public final void clearModelErrors() {

	this.modelProblems.clear();
	this.refreshEventList();
    }

    /**
     * Elimina todos los errores de validación de la tabla de errores.
     */
    public final void clearValidationErrors() {

	this.validationProblems.clear();
	this.refreshEventList();
    }

    /**
     * Devuelve la <code>EvetList</code> que facilita la impresión de la tabla de errores en una <code>JTable</code>.
     * 
     * @return <code>EventList</code> con los errores de la tabla de errores.
     */
    public final EventList<ProblemBean> getEventList() {

	return this.eventList;
    }

    /**
     * Matiene sincronizada la tabla de errores y la <code>EventList</code> asociada.
     */
    private void refreshEventList() {

	// Se limpia la EventList
	this.eventList.clear();

	// Se agregan los problemas del Modelo.
	for (final Collection<ProblemBean> problems : this.modelProblems.values()) {
	    this.eventList.addAll(problems);
	}

	// Se agregan los problemas de validación
	for (final Collection<ProblemBean> problems : this.validationProblems.values()) {
	    this.eventList.addAll(problems);
	}
    }

    /**
     * Envuelve una colección de <code>Problem</code>, generando como resultado una colección de Beans de tipo
     * <code>ProblemBean</code>.
     * 
     * @param problems
     *            colección de <code>Problem</code> a envolver.
     * @return colección de tipo <code>ProblemBean</code> envoltorio.
     */
    private Collection<ProblemBean> wrapProblems(final Collection<Problem> problems) {

	final Collection<ProblemBean> problemBeans = new ArrayList<ProblemBean>();

	for (final Problem problem : problems) {
	    problemBeans.add(new ProblemBean(problem));
	}

	return problemBeans;
    }
}
