package org.bluebell.richclient.exceptionhandling;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.bluebell.richclient.test.AbstractBbRichClientTests;
import org.springframework.richclient.application.ApplicationException;
import org.springframework.richclient.exceptionhandling.delegation.ExceptionPurger;

/**
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestExceptionPurger extends AbstractBbRichClientTests {

    /**
     * 
     */
    protected ExceptionPurger exceptionPurger;

    /**
     * Construye el test indicando que se han de popular variables protegidas.
     */
    public TestExceptionPurger() {

	super();
	this.setPopulateProtectedVariables(Boolean.TRUE);
    }

    /**
     * Caso que prueba el correcto funcionamiento de la inyección de dependencias.
     */
    public void testDependencyInjection() {

	TestCase.assertNotNull(this.exceptionPurger);
    }

    /**
     * Caso que prueba el correcto funcionamiento del <code>ExceptionPurger</code>.
     */
    public void testExcludeRuntimeException() {

	Throwable purged = null;
	RuntimeException runtimeException = null;

	// Una excepción checked envuelta
	final ApplicationException applicationException = new ApplicationException("message");

	// RuntimeException sin envoltorio
	runtimeException = new RuntimeException();
	purged = this.exceptionPurger.purge(runtimeException);
	TestCase.assertTrue(runtimeException == purged);

	// RuntimeException con envoltorio
	runtimeException = new RuntimeException(applicationException);
	purged = this.exceptionPurger.purge(runtimeException);
	TestCase.assertTrue(applicationException == purged);
    }

    /**
     * Obtiene las ubicaciones de los ficheros de configuración.
     * 
     * @return las ubicaciones.
     */
    @Override
    protected String[] getConfigLocations() {

	return (String[]) ArrayUtils.add(super.getConfigLocations(), "classpath:/test/TestExceptionPurger.xml");
    }
}
