package org.bluebell.richclient.form.util;

import java.util.HashSet;
import java.util.Set;

import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.validation.support.HibernateRulesValidator;

/**
 * Modifica el comportamiento de {@link HibernateRulesValidator} con el <b>único</b> objetivo de <b>no</b> utilizar un
 * {@link org.springframework.binding.validation.support.HibernateRulesMessageInterpolator} ya que no funciona
 * correctamente.
 * <p>
 * Con la implementación original los mensajes no incluyen los valores de las variables de las reglas de validación,
 * sino que sus nombres. Ej.:
 * 
 * <pre>
 * &quot;El campo rango debe ser entre {min} y {max}&quot;
 * </pre>
 * 
 * @see <a href="http://forum.springframework.org/showthread.php?p=188110#post188110">Post in Spring RCP Forum</a>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbHibernateRulesValidator extends HibernateRulesValidator {

    /**
     * El nombre del campo <code>hibernateValidator</code> de la clase {@link HibernateRulesValidator}.
     */
    // private static final String HIBERNATE_VALIDATOR_FIELD_NAME = "hibernateValidator";

    /**
     * Creates a new HibernateRulesValidator with additionally a set of properties that should not be validated.
     * 
     * @param formModel
     *            The {@link ValidatingFormModel} on which validation needs to occur
     * @param clazz
     *            The class of the object this validator needs to check
     * @param ignoredHibernateProperties
     *            properties that should not be checked though are
     */
    @SuppressWarnings("unchecked")
    public BbHibernateRulesValidator(ValidatingFormModel formModel, Class clazz, Set<String> ignoredHibernateProperties) {

	super(formModel, clazz, ignoredHibernateProperties);

	// TODO descomentar
	// Obtener el campo con el validador de Hibernate Validator
	//        
	// final Field field =
	// ReflectionUtils.findField(HibernateRulesValidator.class,
	// BbHibernateRulesValidator.HIBERNATE_VALIDATOR_FIELD_NAME,
	// ClassValidator.class);
	//
	// // Hacer el campo accesible
	// ReflectionUtils.makeAccessible(field);
	//
	// // Establecer el nuevo valor del validador
	// ReflectionUtils.setField(field, this, new ClassValidator(clazz));
    }

    /**
     * Creates a new HibernateRulesValidator without ignoring any properties.
     * 
     * @param formModel
     *            The {@link ValidatingFormModel} on which validation needs to occur
     * @param clazz
     *            The class of the object this validator needs to check
     */
    public BbHibernateRulesValidator(ValidatingFormModel formModel, Class<//
	    ? extends Object> clazz) {

	this(formModel, clazz, new HashSet<String>());
    }
}
