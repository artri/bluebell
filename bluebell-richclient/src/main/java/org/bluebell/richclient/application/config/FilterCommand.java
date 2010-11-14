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

package org.bluebell.richclient.application.config;

import javax.swing.JTable;

import org.springframework.richclient.command.ToggleCommand;
import org.springframework.util.Assert;

/**
 * Comando que muestra u oculta la cabecera de filtrado de una tabla de tipo {@link VLJTable}.
 * <p>
 * Si la tabla pasada como parámetro no es del tipo esperado entonces el comando permanece deshabilitado.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class FilterCommand extends ToggleCommand {

    /**
     * La tabla a filtrar.
     */
    private JTable table;

    /**
     * Construye el comando a partir de la tabla a filtrar.
     * 
     * @param table
     *            la tabla a filtrar.
     */
    public FilterCommand(JTable table) {

        this(null, table);
    }

    /**
     * Construye el comando a partir de su identificador y de la tabla a filtrar.
     * 
     * 
     * @param commandId
     *            el identificador del comando.
     * @param table
     *            la tabla a filtrar.
     */
    public FilterCommand(String commandId, JTable table) {

        super(commandId);

        this.setTable(table);
        // this.setEnabled(this.getTable() instanceof VLJTable);
    }

    /**
     * Obtiene la tabla a filtrar.
     * 
     * @return la tabla a filtrar.
     */
    public JTable getTable() {

        return this.table;
    }

    /**
     * Establece la tabla a filtrar.
     * 
     * @param table
     *            la tabla a filtrar.
     */
    public void setTable(JTable table) {

        this.table = table;
    }

    /**
     * Cambia la visibilidad de la cabecera de filtrado de la tabla.
     * 
     * @param selected
     *            <em>flag</em> indicando si se ha de seleccionar o deseleccionar el comando.
     * 
     * @return el valor devuelto por {@link ToggleCommand#onSelection(boolean)}.
     */
    @Override
    protected boolean onSelection(boolean selected) {

        Assert.isTrue(this.isEnabled());

        // Llegados a este punto el comando está habilitado y por tanto la
        // tabla es una VLJTable
        // ((VLJTable) this.table).setFilterHeaderVisible(selected);

        return super.onSelection(selected);
    }
}
