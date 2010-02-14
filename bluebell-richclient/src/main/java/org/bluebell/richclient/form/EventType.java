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

package org.bluebell.richclient.form;

import org.springframework.richclient.application.event.LifecycleApplicationEvent;

/**
 * Tipos de eventos que un formulario maestro puede publicar.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public enum EventType {
    /**
     * Tipo de evento para reflejar que se ha creado una entidad.
     */
    CREATED(LifecycleApplicationEvent.CREATED),
    /**
     * Tipo de evento para reflejar que se ha borrado una entidad.
     */
    DELETED(LifecycleApplicationEvent.DELETED),
    /**
     * Tipo de evento para reflejar que se ha modificado una entidad.
     */
    MODIFIED(LifecycleApplicationEvent.MODIFIED),
    /**
     * Tipo de evento para reflejar que se ha refrescado una entidad.
     */
    REFRESHED("lifecycleEvent.refreshed");

    /**
     * Cadena identificando el tipo del evento.
     */
    private final String type;

    /**
     * Obtiene el tipo del evento.
     * 
     * @param type
     *            el tipo del evento.
     */
    private EventType(String type) {

        this.type = type;
    }

    /**
     * Imprime el tipo del evento.
     * 
     * @return el tipo del evento.
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {

        return this.type;
    }
}
