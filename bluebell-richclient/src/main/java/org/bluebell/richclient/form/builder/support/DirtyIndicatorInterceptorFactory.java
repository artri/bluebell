package org.bluebell.richclient.form.builder.support;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.builder.FormComponentInterceptor;
import org.springframework.richclient.form.builder.support.ConfigurableFormComponentInterceptorFactory;

/**
 * Factoría para la creación de instancias de <code>DirtyIndicatorInterceptor</code>.
 * <p>
 * A diferencia de {@link org.springframework.richclient.form.builder.support.DirtyIndicatorInterceptorFactory} crea
 * instancia de {@link DirtyIndicatorInterceptor} que resuelve algunos problemas con la implementación original.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 * 
 * @see DirtyIndicatorInterceptor
 */
public class DirtyIndicatorInterceptorFactory extends ConfigurableFormComponentInterceptorFactory {
    /**
     * {@inheritDoc}.
     */
    @Override
    protected FormComponentInterceptor createInterceptor(FormModel formModel) {

        return new DirtyIndicatorInterceptor(formModel);
    }
}
