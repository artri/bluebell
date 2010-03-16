package org.bluebell.richclient.form.builder.support;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.builder.FormComponentInterceptor;
import org.springframework.richclient.form.builder.support.ConfigurableFormComponentInterceptorFactory;

/**
 * Factory for creating {@link BbOverlayValidationInterceptor} instances.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbOverlayValidationInterceptorFactory extends ConfigurableFormComponentInterceptorFactory {

    /**
     * {@inheritDoc}
     */
    public FormComponentInterceptor createInterceptor(FormModel formModel) {

        return new BbOverlayValidationInterceptor(formModel);
    }
}
