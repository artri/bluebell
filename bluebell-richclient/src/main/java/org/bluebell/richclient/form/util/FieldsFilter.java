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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Estrategia para la obtención de los campos de una clase del dominio.
 * 
 * @param <T>
 *            el tipo de la entidad a filtrar.
 * @param <ID>
 *            el tipo del identificador de la entidad a filtrar.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public interface FieldsFilter<T, ID extends Serializable> {

    /**
     * Obtiene los campos de una clase conforme a la política establecida.
     * 
     * @param clazz
     *            la clase.
     * @return los campos de la clase.
     */
    Collection<Field> getFields(Class<T> clazz);
}
