package org.bluebell.richclient.bean;

import java.io.Serializable;

import org.springframework.richclient.core.Severity;

//FIXME 20081113(AMM) No debería de haber dependencias de richclient
/**
 * Clase de Dominio para representar los errores de la aplicaciÃ³n. Un error consta de una severidad, un cÃ³digo y una
 * descripciÃ³n.
 * 
 * @author <a href="mailto:p.martinez@ceb2b2000.com">Pablo MartÃ­nez Ã�lvarez</a>
 * 
 */
public class Problem implements Serializable {

    /**
     * Clase serializable.
     */
    private static final long serialVersionUID = -9131921796649225972L;

    /**
     * CÃ³digo identificativo del error.
     */
    private int code;

    /**
     * Texto identificativo del error.
     */
    private String description;

    /**
     * Severidad del problema: Error, Warning o Info.
     */
    private final Severity severity;

    /**
     * Constructor por defecto.
     */
    public Problem() {

        this.severity = Severity.ERROR;
    }

    /**
     * Construye un error a partir de su severidad, cÃ³digo de error y descripciÃ³n.
     * 
     * @param severity
     *            severidad del error.
     * @param code
     *            cÃ³digo del error.
     * @param description
     *            descripciÃ³n del error.
     */
    public Problem(final Severity severity, final int code, final String description) {

        super();
        this.severity = severity;
        this.code = code;
        this.description = description;
    }

    /**
     * Produce la descripciÃ³n del error.
     * 
     * @return texto descriptivo del error.
     */
    public final String getDescription() {

        return this.description;
    }

    /**
     * Produce el cÃ³digo del error.
     * 
     * @return cÃ³digo del error.
     */
    public final int getErrorCode() {

        return this.code;
    }

    /**
     * Produce la severidad del problema.
     * 
     * @return severidad del problema.
     */
    public final Severity getSeverity() {

        return this.severity;
    }
}
