/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell VLDocking.
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

package org.bluebell.richclient.application.docking.vldocking;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import org.springframework.util.Assert;

import com.vlsolutions.swing.docking.DockTabbedPane;
import com.vlsolutions.swing.docking.DockView;
import com.vlsolutions.swing.docking.Dockable;

/**
 * Extensión de {@link DockTabbedPane} que recuerda las posiciones relativas de las pestañas.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbDockTabbedPane extends DockTabbedPane {

    /**
     * Valor devuelto en caso de que no se encuentre un índice.
     */
    private static final int NOT_FOUND = -1;

    /**
     * Es una clase <code>Serializable</code>.
     */
    private static final long serialVersionUID = 8775728460094474504L;

    /**
     * Todas las pestañas que pertenecen o han pertenecido al panel, respetando su orden.
     */
    private final List<Dockable> allTabs = new ArrayList<Dockable>();

    /**
     * Construye el componente.
     */
    public BbDockTabbedPane() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dockable getDockableAt(int index) {

        Dockable dockable = super.getDockableAt(index);

        // TODO, (JAF), 20090305, ¿por qué hay que hacer esto?
        if (dockable == null) {
            final Component component = this.getComponentAt(index);
            if (component instanceof Dockable) {
                dockable = (Dockable) component;
            }
        }

        return dockable;
    }

    /**
     * Obtiene la posición más apropiada en la que añadir un <code>Dockable</code>.
     * 
     * @param dockable
     *            el <code>Dockable</code> a consultar.
     * 
     * @return la posición de inserción del componente. Nunca {@value #NOT_FOUND}.
     */
    public int getIndexForDockable(Dockable dockable) {

        final int rIndex = this.indexOfDockable(dockable);
        if (rIndex != BbDockTabbedPane.NOT_FOUND) {
            return rIndex;
        }

        final int vIndex = this.getAllTabs().indexOf(dockable);

        // Las posiciones reales
        final int rBef = this.searchBackward(vIndex);
        final int rAft = BbDockTabbedPane.NOT_FOUND;
        final int rCur = BbDockTabbedPane.NOT_FOUND;
        final int size = this.getTabCount();
        final int rPos = this.calculatePosition(rBef, rAft, rCur, size);

        // Si no se ha encontrado la posición real entonces añadir al principio
        // si es un dockable conocido y al final en caso contrario
        if (rPos == BbDockTabbedPane.NOT_FOUND) {
            return (this.getAllTabs().contains(dockable)) ? 0 : size;
        }

        return rPos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOfDockable(Dockable dockable) {

        int index = super.indexOfDockable(dockable);

        // TODO, (JAF), 20090305, ¿por qué hay que hacer esto?
        if (index == BbDockTabbedPane.NOT_FOUND) {
            index = super.indexOfComponent(dockable.getComponent());
        }

        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index) {

        final Dockable dockable = BbDockTabbedPane.getDockable(component);

        // Si se está añadiendo un tab al final entonces la operación anterior
        // probablemente haya sido un addTab. En ese caso comprobar si hay una
        // posición mejor donde insertarlo
        if ((index == this.getTabCount()) && (dockable != null)) {
            index = this.getIndexForDockable(dockable);
        }

        // Comprobar si se está moviendo una pestaña
        final int rFoundAt = (dockable != null) ? this.indexOfDockable(dockable) : BbDockTabbedPane.NOT_FOUND;
        final int rIndex = ((rFoundAt != BbDockTabbedPane.NOT_FOUND) && (rFoundAt < index)) ? index - 1 : index;

        super.insertTab(title, icon, component, tip, index);

        // Recordar la posición de ola pestaña
        this.afterInsertTab(rIndex);
    }

    /**
     * Mantiene la lista de todas las pestañas que pertenecen o han pertenecido al panel.
     * 
     * @param rIndex
     *            el índice relativo a las pestañas reales de la última pestaña insertada.
     */
    protected void afterInsertTab(int rIndex) {

        Assert.isTrue((rIndex >= 0) && (rIndex < this.getTabCount()));

        final Dockable dockable = this.getDockableAt(rIndex);

        if (dockable == null) {
            // 20090324, (JAF), sino hay problemas al maximizar
            return;
        }

        // Las posiciones virtuales
        final int vBef = this.getVirtualIndex(rIndex - 1);
        final int vAft = this.getVirtualIndex(rIndex + 1);
        final int vCur = this.getVirtualIndex(rIndex);
        final int size = this.getAllTabs().size();
        int vPos = this.calculatePosition(vBef, vAft, vCur, size);
        if (vPos == BbDockTabbedPane.NOT_FOUND) {
            vPos = 0;
        }

        // Añadir la pestaña en la posición adecuada
        this.getAllTabs().add(vPos, dockable);

        // Eliminar la pestaña duplicada, si es que la hubiese
        if (vCur != BbDockTabbedPane.NOT_FOUND) {
            final int vPosToRemove = (vCur >= vPos) ? vCur + 1 : vCur;
            this.getAllTabs().remove(vPosToRemove);
        }
    }

    /**
     * Obtiene la relación de pestañas que pertenecen o han pertenecido al panel respetando su orden.
     * 
     * @return las pestañas.
     */
    protected List<Dockable> getAllTabs() {

        return this.allTabs;
    }

    /**
     * Calcula la posición en la que colocar una pestaña en una lista.
     * 
     * @param before
     *            la posición de la pestaña más cercana a la izquierda.
     * @param after
     *            la posición de la pestaña más cercana a la izquierda.
     * @param current
     *            la posición que ocupa actualmente la pestaña.
     * @param size
     *            el tamaño de la lista.
     * @return la posición o {@value #NOT_FOUND} en caso de que no se pueda calcular.
     */
    private int calculatePosition(int before, int after, int current, int size) {

        // Comprobar el invariante

        Assert.isTrue((before == BbDockTabbedPane.NOT_FOUND) || (after == BbDockTabbedPane.NOT_FOUND)
                || ((Integer) before < after));

        // Convertir after para simplificar los cálculos
        int positiveAfter = after;
        if (positiveAfter == BbDockTabbedPane.NOT_FOUND) {
            positiveAfter = (size > 0) ? size : 0;
        }

        // Calcular la posición virtual de inserción
        int pos = BbDockTabbedPane.NOT_FOUND;
        if ((current != BbDockTabbedPane.NOT_FOUND) && (current > before) && (current < positiveAfter)) {
            pos = current;
        } else if (before != BbDockTabbedPane.NOT_FOUND) {
            pos = before + 1;
        } else if (after != BbDockTabbedPane.NOT_FOUND) {
            pos = after;
        }

        return pos;
    }

    /**
     * Transforma un índice virtual en un índice real.
     * 
     * @param vIndex
     *            el índice virtual.
     * @return el índice real o {@value #NOT_FOUND} si el índice virtual no es válido o su pestaña asociada no pertenece
     *         a las pestañas reales.
     * 
     * @see #allTabs
     */
    private int getRealIndex(int vIndex) {

        if ((vIndex < 0) || (vIndex >= this.getAllTabs().size())) {
            return BbDockTabbedPane.NOT_FOUND;
        }

        final Dockable dockable = this.getAllTabs().get(vIndex);

        return this.indexOfDockable(dockable);
    }

    /**
     * Transforma un índice real en un índice virtual.
     * 
     * @param rIndex
     *            el índice real.
     * @return el índice virtual o {@value #NOT_FOUND} si el índice real no es válido o su pestaña asociada no estaba
     *         registrada anteriormente.
     * @see #allTabs
     */
    private int getVirtualIndex(int rIndex) {

        if ((rIndex < 0) || (rIndex >= this.getTabCount())) {
            return BbDockTabbedPane.NOT_FOUND;
        }

        final Dockable dockable = this.getDockableAt(rIndex);

        return this.getAllTabs().indexOf(dockable);
    }

    /**
     * Busca entre las pestañas reales la más cercana a la izquierda de uno dado.
     * 
     * @param vIndex
     *            el índice virtual de la pestaña objetivo.
     * @return el índice real de la pestaña más cercana a la izquierda y {@value #NOT_FOUND} en caso de que no exista.
     */
    private int searchBackward(int vIndex) {

        for (int i = vIndex - 1; i >= 0; --i) {
            final int pos = this.getRealIndex(i);
            if (pos != BbDockTabbedPane.NOT_FOUND) {
                return pos;
            }
        }

        return BbDockTabbedPane.NOT_FOUND;
    }

    /**
     * Obtiene el <code>Dockable</code> asociado a un componente.
     * 
     * @param component
     *            el componente.
     * @return el <code>Dockable</code> y <code>null</code> si no existe
     */
    private static Dockable getDockable(Component component) {

        return (component instanceof DockView) ? ((DockView) component).getDockable() : null;
    }
}
