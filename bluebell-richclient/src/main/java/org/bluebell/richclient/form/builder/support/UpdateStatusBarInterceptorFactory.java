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

package org.bluebell.richclient.form.builder.support;



import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.builder.FormComponentInterceptor;
import org.springframework.richclient.form.builder.FormComponentInterceptorFactory;

/**
 * Crea un interceptor que actualiza la <em>status bar</em> con información relativa al componente del formulario
 * seleccionado.
 * <p>
 * <ul>
 * <li>Si el componente no valida muestra el primer error de validación.
 * <li>Si el componente valida muestra su nombre y en caso de que exista la descripción.
 * </ul>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class UpdateStatusBarInterceptorFactory implements FormComponentInterceptorFactory {

    /**
     * Retorna un interceptor de tipo {@link UpdateStatusBarInterceptor}.
     * 
     * @param formModel
     *            el modelo del formulario.
     * @return el interceptor.
     */
    public FormComponentInterceptor getInterceptor(final FormModel formModel) {

        return new UpdateStatusBarInterceptor(formModel);
    }
}
