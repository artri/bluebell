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

package org.bluebell.richclient.command.support;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.springframework.richclient.command.support.ShowPageCommand;

/**
 * Extension de <code>ShowPageCommand</code> que permite especificar las entidades a seleccionar en la página destino.
 * <p>
 * Abre una nueva pagina (conocido su <code>pageDescriptorId</code> ) y muestra las entidades del tipo <code>U</code>
 * relaccionadas con las entidades de tipo <code>T</code> seleccionadas en origen.
 * <p>
 * Aunque el comportamiento por defecto obliga a especificar una tabla a partir de la cual obtener las entidades
 * seleccionadas en origen, es posible redefinir el método {@link #getSelectedEntities()} para que utilice otra
 * política.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 * 
 * @param <T>
 *            el tipo de las entidades mostradas en la página origen.
 * @param <U>
 *            el tipo de las entidades a mostrar en la página destino.
 */
public abstract class ExtendedShowPageCommand<T, U> extends ShowPageCommand implements MouseListener,
        ListSelectionListener {

    /**
     * La tabla de la página de origen.
     */
    private JTable targetTable;

    /**
     * El constructor por defecto de la clase, en él únicamente se llama al constructor de la clase padre.
     */
    public ExtendedShowPageCommand() {

        super();
    }

    /**
     * Ejecuta el comando al hacer doble click.
     * 
     * @param event
     *            Evento capturado.
     */
    public final void mouseClicked(MouseEvent event) {

        if (event.getClickCount() == 2) {
            this.execute();
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void mouseEntered(MouseEvent arg0) {

        // Nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public final void mouseExited(MouseEvent arg0) {

        // Nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public final void mousePressed(MouseEvent arg0) {

        // Nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public final void mouseReleased(MouseEvent arg0) {

        // Nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBeanName(String name) {

        this.setId(name);
    }

    /**
     * Establece la tabla de origen.
     * 
     * @param targetTable
     *            la tabla de origen.
     */
    public void setTargetTable(JTable targetTable) {

        org.springframework.util.Assert.notNull(targetTable);

        this.targetTable = targetTable;

        // El comando se activará o desactivará automáticamente
        this.getTargetTable().getSelectionModel().addListSelectionListener(this);

        // (JAF), 20090128, Mejor hacer que este no sea el comportamiento por
        // defecto. Se ofrece la posibilidad pero hay que hacerlo de forma
        // explícita
        // this.getTargetTable().addMouseListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public void valueChanged(ListSelectionEvent event) {

        if (event.getValueIsAdjusting()) {
            return;
        }

        final int rowCount = this.getTargetTable().getSelectedRowCount();
        ExtendedShowPageCommand.this.setEnabled(rowCount > 0);
    }

    /**
     * Obtiene las entidades seleccionadas en origen, calcula las entidades a seleccionar en destino y finalmente las
     * muestra.
     */
    @Override
    protected void doExecuteCommand() {

        // TODO FIXME
        
        // final ApplicationWindow window = this.getApplicationWindow();

        // 1. Obtener las entidades seleccionadas en origen
        // final Collection<T> selectedEntities = this.getSelectedEntities();

        // 2. Obtener las entidades a seleccionar en destino
        // final Collection<U> entitiesToSelect =
        // this.getEntitiesToSelect(selectedEntities);

        // 3. Mostrar la página destino
        super.doExecuteCommand();

        // 4. Cargar las entidades a mostrar en la página destino
        // final ApplicationPage currentApplicationPage = window.getPage(); //
        // FIXME
        // (((BbVLDockingApplicationPage<U>)
        // currentApplicationPage).getLastMasterForm())
        // .setVisibleEntities(entitiesToSelect);
    }

    /**
     * Obtiene las entidades a mostrar en la página destino a partir de las entidades seleccionadas en la página origen.
     * 
     * @param entities
     *            las entidades seleccionadas en la página origen.
     * @return las entidades a mostrar en la página destino.
     */
    protected abstract Collection<U> getEntitiesToSelect(Collection<T> entities);

    /**
     * Obtiene las entidades seleccionadas en origen.
     * <p>
     * Esta implementación obtiene las entidades seleccionadas en la tabla maestra de la página origen.
     * 
     * @return las entidades seleccionadas en origen.
     */
    // @SuppressWarnings("unchecked")
    protected Collection<T> getSelectedEntities() {

        // final ApplicationWindow window = this.getApplicationWindow();
        // final ApplicationPage previousApplicationPage = window.getPage();

//        org.springframework.util.Assert.isInstanceOf(BbVLDockingApplicationPage.class, previousApplicationPage);

        return null; // FIXME
        // return (((BbVLDockingApplicationPage<T>)
        // previousApplicationPage).getLastMasterForm()).getSelectedEntities();
    }

    /**
     * Obtiene la tabla de origen.
     * 
     * @return la tabla de origen.
     */
    private JTable getTargetTable() {

        return this.targetTable;
    }
}
