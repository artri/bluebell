/*
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
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

/**
 * 
 */
package org.bluebell.richclient.form.util;

import java.lang.reflect.Field;
import java.util.Collection;

import org.springframework.richclient.form.binding.BindingFactory;
import org.springframework.richclient.form.builder.AbstractFormBuilder;

/**
 * Renderiza los campos de una entidad del dominio.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public interface EntityRenderer {

    /**
     * Renderiza una entidad utilizando los campos devueltos por {@link #getFields}.
     * 
     * @param bindingFactory
     *            la factoría para la creación de componentes.
     * @param fields
     *            los campos a renderizar.
     * @return un <em>builder</em> para la creación del formulario.
     */
    AbstractFormBuilder render(BindingFactory bindingFactory, Collection<Field> fields);
}
