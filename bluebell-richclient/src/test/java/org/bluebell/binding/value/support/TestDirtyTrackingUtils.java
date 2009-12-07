package org.bluebell.binding.value.support;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bluebell.richclient.form.builder.support.DirtyTrackingUtils;
import org.bluebell.richclient.test.AbstractBbRichClientTests;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.form.support.DefaultFormModel;


/**
 * Clase que prueba el correcto funcionamiento de la clase
 * <code>DirtyTrackingUtils</code>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestDirtyTrackingUtils extends AbstractBbRichClientTests {

    /**
     * El identificador del <em>form model</em> padre utilizado durante las
     * pruebas.
     */
    private static final String PARENT_FORM_MODEL_ID = "parentFormModel";

    /**
     * El identificador del <em>form model</em> hijo utilizado durante las
     * pruebas.
     */
    private static final String CHILD_FORM_MODEL_ID = "childFormModel";

    /**
     * Las propiedades con las labels de los formularios.
     */
    protected Properties testUDirtyTrackingUtilsProperties;

    /**
     * Construye el test indicando que se han de popular variables protegidas.
     */
    public TestDirtyTrackingUtils() {

        super();
        this.setPopulateProtectedVariables(Boolean.TRUE);
    }

    /**
     * Caso que prueba el correcto funcionamiento de la inyección de
     * dependencias.
     */
    public void testDependencyInjection() {

        TestCase.assertNotNull(this.testUDirtyTrackingUtilsProperties);
    }

    /**
     * Caso que prueba el correcto funcionamiento de
     * {@link DirtyTrackingUtils#getI18nDirtyProperties(org.springframework.binding.form.FormModel)}
     * .
     */
    public void testGetI18nDirtyProperties() {

        final int dirtyValueModels = 4;

        final FormModel formModel = this.createFormModel();

        // Comprobar que hay exactamente 4 value models dirty
        final Set<String[]> i18nDirtyProperties =
            DirtyTrackingUtils.getI18nDirtyProperties(formModel);
        TestCase.assertEquals(dirtyValueModels, i18nDirtyProperties.size());
    }

    /**
     * Caso que prueba el correcto funcionamiento de
     * {@link DirtyTrackingUtils#clearDirty(org.springframework.binding.form.FormModel)}
     * .
     */
    public void testClearDirtyFormModel() {

        final FormModel formModel = this.createFormModel();

        // Limpiar el dirty y comprobar que no haya ningún value model dirty
        DirtyTrackingUtils.clearDirty(formModel);

        final Set<String[]> i18nDirtyProperties =
            DirtyTrackingUtils.getI18nDirtyProperties(formModel);

        TestCase.assertTrue(i18nDirtyProperties.isEmpty());
    }

    /**
     * Caso que prueba el correcto funcionamiento de
     * {@link DirtyTrackingUtils#clearDirty(org.springframework.binding.form.FormModel)}
     * .
     */
    public void testClearDirtyValueModel() {

        final int total = 4;
        final FormModel parentFormModel = this.createFormModel();
        final FormModel childFormModel =
            ((HierarchicalFormModel) parentFormModel).getChildren()[0];

        Collection<String[]> dirtyProperties =
            DirtyTrackingUtils.getDirtyProperties(parentFormModel);

        int iter = 0;
        for (String[] dirtyProperty : dirtyProperties) {
            final String formModelId = dirtyProperty[0];
            final String propertyPath = dirtyProperty[1];

            FormModel formModel = childFormModel;
            if (TestDirtyTrackingUtils.PARENT_FORM_MODEL_ID.equals(//
                formModelId)) {
                formModel = parentFormModel;
            }

            // Limpiar el dirty en una propiedad
            DirtyTrackingUtils.clearDirty(//
                formModel.getValueModel(propertyPath));

            // Comprobar que el número de value models dirty haya disminuido
            dirtyProperties =
                DirtyTrackingUtils.getDirtyProperties(parentFormModel);
            TestCase.assertEquals(total - (++iter), dirtyProperties.size());
        }
    }

    /**
     * Caso que prueba el correcto funcionamiento de
     * {@link DirtyTrackingUtils#getI18nDirtyPropertiesHtmlString(org.springframework.binding.form.FormModel)}
     * .
     */
    public void testGetI18nDirtyPropertiesHtmlString() {

        final String parentFormModelId =
            TestDirtyTrackingUtils.PARENT_FORM_MODEL_ID;
        final String childFormModelId =
            TestDirtyTrackingUtils.CHILD_FORM_MODEL_ID;

        final String parentFormModelMsg =
            this.testUDirtyTrackingUtilsProperties.getProperty(//
                parentFormModelId + ".caption");
        final String childFormModelMsg =
            this.testUDirtyTrackingUtilsProperties.getProperty(//
                childFormModelId + ".caption");
        final String simplePropertyParentFormModelMsg =
            this.testUDirtyTrackingUtilsProperties.getProperty(//
                parentFormModelId + ".message.label");
        final String compoundPropertyParentFormModelMsg =
            this.testUDirtyTrackingUtilsProperties.getProperty(//
                parentFormModelId + ".foo.message.label");
        final String simplePropertyChildFormModelMsg =
            this.testUDirtyTrackingUtilsProperties.getProperty(//
                childFormModelId + ".message.label");
        final String compoundPropertyChildFormModelMsg =
            this.testUDirtyTrackingUtilsProperties.getProperty(//
                childFormModelId + ".foo.message.label");

        // Obtener la representación en HTML de los vm dirty
        final FormModel formModel = this.createFormModel();
        final String i18nDirtyPropertiesHtmlString =
            DirtyTrackingUtils.getI18nDirtyPropertiesHtmlString(formModel);

        // Comprobar que todo haya ido bien
        TestCase.assertEquals(2, StringUtils.countMatches(//
            i18nDirtyPropertiesHtmlString, parentFormModelMsg));
        TestCase.assertEquals(2, StringUtils.countMatches(//
            i18nDirtyPropertiesHtmlString, childFormModelMsg));
        TestCase.assertEquals(1, StringUtils.countMatches(//
            i18nDirtyPropertiesHtmlString, simplePropertyParentFormModelMsg));
        TestCase.assertEquals(1, StringUtils.countMatches(//
            i18nDirtyPropertiesHtmlString, compoundPropertyParentFormModelMsg));
        TestCase.assertEquals(1, StringUtils.countMatches(//
            i18nDirtyPropertiesHtmlString, simplePropertyChildFormModelMsg));
        TestCase.assertEquals(1, StringUtils.countMatches(//
            i18nDirtyPropertiesHtmlString, compoundPropertyChildFormModelMsg));

        System.out.println(i18nDirtyPropertiesHtmlString);
    }

    /**
     * Obtiene las ubicaciones de los ficheros de configuración.
     * 
     * @return las ubicaciones.
     */
    @Override
    protected String[] getConfigLocations() {

        return (String[]) ArrayUtils.add(super.getConfigLocations(),
            "classpath:/test/TestUDirtyTrackingUtils.xml");
    }

    /**
     * Crea el <em>form model</em> utilizado durante las pruebas.
     * 
     * @return el <em>form model</em>.
     */
    private FormModel createFormModel() {

        // Construir y vincular los form model
        final DefaultFormModel parentFormModel =
            new DefaultFormModel(new Foo());
        final DefaultFormModel childFormModel = new DefaultFormModel(new Foo());

        parentFormModel.setId(TestDirtyTrackingUtils.PARENT_FORM_MODEL_ID);
        childFormModel.setId(TestDirtyTrackingUtils.CHILD_FORM_MODEL_ID);
        parentFormModel.addChild(childFormModel);

        // Cambiar el valor de propiedades de los form model padre e hijo
        parentFormModel.getValueModel("message").setValue("B");
        parentFormModel.getValueModel("foo.message").setValue("B");
        childFormModel.getValueModel("message").setValue("B");
        childFormModel.getValueModel("foo.message").setValue("B");

        return parentFormModel;
    }

    /**
     * Una clase utilizada durante las pruebas.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class Foo {
        /**
         * La propiedad.
         */
        private String message;

        /**
         * Una referencia reflexiva.
         */
        private Foo foo;

        /**
         * Obtiene el mensaje.
         * 
         * @return el mensaje.
         */
        public String getMessage() {

            return this.message;
        }

        /**
         * Establece el mensaje.
         * 
         * @param message
         *            el mensaje.
         */
        public void setMessage(String message) {

            this.message = message;
        }

        /**
         * Obtiene la relación reflexiva y si no existe la crea.
         * 
         * @return la relación reflexiva.
         */
        public Foo getFoo() {

            if (this.foo == null) {
                this.setFoo(new Foo());
            }

            return this.foo;
        }

        /**
         * Establece la relación reflexiva.
         * 
         * @param foo
         *            la relación reflexiva.
         */
        public void setFoo(Foo foo) {

            this.foo = foo;
        }
    }
}
