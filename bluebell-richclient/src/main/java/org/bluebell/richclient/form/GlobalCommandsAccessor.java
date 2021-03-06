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

package org.bluebell.richclient.form;

import org.springframework.richclient.command.ActionCommandExecutor;
import org.springframework.richclient.form.Form;

/**
 * Interfaz que permite obtener los ejecutores de los comandos globales. Los comandos globales son:
 * <ul>
 * <li>
 * {@link org.springframework.richclient.command.support.GlobalCommandIds#PROPERTIES}
 * <li>
 * {@link org.springframework.richclient.command.support.GlobalCommandIds#SAVE}
 * <li>
 * {@link org.springframework.richclient.command.support.GlobalCommandIds#DELETE}
 * <li>
 * {@link org.springframework.richclient.command.support.GlobalCommandIds#UNDO}
 * </ul>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public interface GlobalCommandsAccessor extends Form {

    /**
     * El identificador del comando global {@value GlobalCommandsAccessor#CANCEL} que permite cancelar la creación de
     * una nueva entidad. sobre una entidad.
     */
    String CANCEL = "cancelCommand";

    /**
     * El identificador del comando global {@value GlobalCommandsAccessor#REFRESH} que permite recargar entidades.
     */
    String REFRESH = "refreshCommand";

    /**
     * El identificador del comando global {@value GlobalCommandsAccessor#REVERT} que permite deshacer
     * <b>parcialmente</b> los cambios sobre una entidad.
     */
    String REVERT = "revertCommand";

    /**
     * El identificador del comando global {@value GlobalCommandsAccessor#REVERT_ALL} que permite deshacer los cambios
     * sobre una entidad.
     */
    String REVERT_ALL = "revertAllCommand";

    /**
     * El identificador del comando global {@value GlobalCommandsAccessor#SELECT_ALL_ENTITIES} que permite seleccionar
     * todas las entidades.
     */
    String SELECT_ALL_ENTITIES = "selectAllEntitiesCommand";

    /**
     * Obtiene el ejecutor del comando global
     * {@link org.springframework.richclient.command.support.GlobalCommandIds#PROPERTIES} .
     * 
     * @return el <em>command executor</em>.
     */
    ActionCommandExecutor getCancelCommand();

    /**
     * Obtiene el ejecutor del comando global
     * {@link org.springframework.richclient.command.support.GlobalCommandIds#DELETE} .
     * 
     * @return el <em>command executor</em>.
     */
    ActionCommandExecutor getDeleteCommand();

    /**
     * Obtiene el ejecutor del comando global
     * {@link org.springframework.richclient.command.support.GlobalCommandIds#PROPERTIES} .
     * 
     * @return el <em>command executor</em>.
     */
    ActionCommandExecutor getNewFormObjectCommand();

    /**
     * Obtiene el ejecutor del comando global {@link GlobalCommandsAccessor#REFRESH}.
     * 
     * @return el <em>command executor</em>.
     */
    ActionCommandExecutor getRefreshCommand();

    /**
     * Obtiene el ejecutor del comando global {@link GlobalCommandsAccessor#REVERT_ALL}.
     * 
     * @return el <em>command executor</em>.
     */
    ActionCommandExecutor getRevertAllCommand();

    /**
     * Obtiene el ejecutor del comando global {@link GlobalCommandsAccessor#REVERT}.
     * 
     * @return el <em>command executor</em>.
     */
    ActionCommandExecutor getRevertCommand();

    /**
     * Obtiene el ejecutor del comando global
     * {@link org.springframework.richclient.command.support.GlobalCommandIds#SAVE} .
     * 
     * @return el <em>command executor</em>.
     */
    ActionCommandExecutor getSaveCommand();

    /**
     * Obtiene el ejecutor del comando global
     * {@link org.springframework.richclient.command.support.GlobalCommandIds#SELECT_ALL} .
     * 
     * @return el <em>command executor</em>.
     */
    ActionCommandExecutor getSelectAllCommand();
}
