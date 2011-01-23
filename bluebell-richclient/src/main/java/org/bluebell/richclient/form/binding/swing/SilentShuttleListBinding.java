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

/**
 * 
 */
package org.bluebell.richclient.form.binding.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.components.ShuttleList;
import org.springframework.richclient.form.binding.swing.ShuttleListBinding;

/**
 * Extiende {@link ShuttleListBinding} con el objetivo de evitar que un simple cambio en la selección genere múltiples
 * notificaciones, una por elemento de la selección.
 * 
 * selectedItemsHolder listSelectedValueMediator valueChangeHandler
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SilentShuttleListBinding extends ShuttleListBinding {

    /**
     * Un <em>listener</em> registrado sobre {@link #shuttleList} que actualiza el <em>value model</em>
     * {@link #selectedItemsHolder} ante cambios en el componente.
     */
    private ListSelectedValueMediator listSelectedValueMediator;

    /**
     * Recuerda el <em>value model</em> de los elementos seleccionados ya que la clase padre no proporcionada método
     * <em>getter</em>.
     */
    private ValueModel selectedItemsHolder;

    /**
     * El control de este <em>binding</em>.
     */
    private ShuttleList shuttleList;

    /**
     * Un <em>listener</em> registrado sobre {@link #selectedItemsHolder} que actualiza el componente
     * {@link #shuttleList} ante cambios en el <em>value model</em>.
     */
    private PropertyChangeListener valueChangeHandler;

    /**
     * Construye el binding.
     * 
     * @param list
     *            la <em>shuttle list</em> a vincular.
     * @param formModel
     *            el <em>form model</em> con la propiedad vinculada.
     * @param formPropertyPath
     *            el <em>path</em> de la propiedad a vincular.
     */
    public SilentShuttleListBinding(final SilentShuttleList list, final FormModel formModel,
            final String formPropertyPath) {

        super(list, formModel, formPropertyPath);

        list.setBinding(this);
    }

    /**
     * Recuerda el <em>value model</em> que almacena los elementos seleccionados, ya que la clase padre no proporciona
     * método <em>getter</em>.
     * 
     * @param selectedItemsHolder
     *            el <em>value model</em> que almacena los elementos seleccionados.
     * 
     * @see ShuttleListBinding#setSelectedItemsHolder(ValueModel)
     */
    @Override
    public void setSelectedItemsHolder(ValueModel selectedItemsHolder) {

        super.setSelectedItemsHolder(selectedItemsHolder);

        this.selectedItemsHolder = selectedItemsHolder;
        if (this.selectedItemsHolder != null) {
            this.selectedItemsHolder.addValueChangeListener(this.getValueChangeHandler());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean collectionsEqual(@SuppressWarnings("rawtypes") Collection a1,
            @SuppressWarnings("rawtypes") Collection a2) {

        // TODO, 20080916, evaluar la necesidad de utilizar apache commons
        return super.collectionsEqual(a1, a2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent doBindControl() {

        final ValueModel currentSelectedItemsHolder = this.selectedItemsHolder;

        // Invocar a super asegurándose de que no establece el value model mediador, ya que será este método quien lo
        // haga
        this.setSelectedItemsHolder(null);

        this.setShuttleList((ShuttleList) super.doBindControl());
        if (this.selectedItemsHolder != null) {
            this.setSelectedValue(null);
            this.getShuttleList().addListSelectionListener(//
                    this.getListSelectedValueMediator());
        }

        this.setSelectedItemsHolder(currentSelectedItemsHolder);

        return this.getShuttleList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setSelectedValue(final PropertyChangeListener silentValueChangeHandler) {

        final int[] indices = this.indicesOf(this.getSelectedItemsHolder().getValue());

        if (indices.length < 1) {
            this.getShuttleList().clearSelection();
        } else {
            final ListSelectionListener listener = this.getListSelectedValueMediator();

            this.getShuttleList().removeListSelectionListener(listener);
            this.getShuttleList().setSelectedIndices(indices);
            this.getShuttleList().addListSelectionListener(listener);

            // The selection may now be different than what is reflected in collection property if this is
            // SINGLE_INTERVAL_SELECTION, so modify if needed...
            this.updateSelectionHolderFromList(silentValueChangeHandler);
        }
    }

    /**
     * Obtiene el <em>listener</em> registrado sobre {@link #shuttleList} y si no existe lo crea.
     * 
     * @return el <em>listener</em> registrado sobre {@link #shuttleList}.
     * 
     * @see #listSelectedValueMediator
     */
    private ListSelectedValueMediator getListSelectedValueMediator() {

        if (this.listSelectedValueMediator == null) {
            this.listSelectedValueMediator = new ListSelectedValueMediator();
        }

        return this.listSelectedValueMediator;
    }

    /**
     * Obtiene el <em>value model</em> de los elementos seleccionados.
     * 
     * @return el selectedItemsHolder el <em>value model</em> de los elementos seleccionados.
     */
    private ValueModel getSelectedItemsHolder() {

        return this.selectedItemsHolder;
    }

    /**
     * Obtiene el control de este <em>binding</em>.
     * 
     * @return el control de este <em>binding</em>.
     */
    private ShuttleList getShuttleList() {

        return this.shuttleList;
    }

    /**
     * Obtiene el <em>listener</em> registrado sobre {@link #selectedItemsHolder} y si no existe lo crea.
     * 
     * @return el <em>listener</em> registrado sobre {@link #selectedItemsHolder}.
     * 
     * @see #valueChangeHandler
     */
    private PropertyChangeListener getValueChangeHandler() {

        if (this.valueChangeHandler == null) {
            this.valueChangeHandler = new ValueChangeHandler();
        }

        return this.valueChangeHandler;
    }

    /**
     * Establece el control de este <em>binding</em>.
     * 
     * @param shuttleList
     *            el control de este <em>binding</em>.
     */
    private void setShuttleList(ShuttleList shuttleList) {

        this.shuttleList = shuttleList;
    }

    /**
     * Especialización de {@link ShuttleList} que ante cambios en la selección produce un único evento.
     * <p>
     * La implementación original provoca un evento por cada elemento de la selección.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class SilentShuttleList extends ShuttleList {

        /**
         * Es una clase <code>Serializable</code>.
         */
        private static final long serialVersionUID = -5739305710971644175L;

        /**
         * El binding al que pertenece esta lista.
         */
        private SilentShuttleListBinding binding;

        /**
         * El <em>listener</em> a evitar.
         */
        private ListSelectionListener listenerToSkip;

        /**
         * Construye el control con el botón de edición activo.
         */
        public SilentShuttleList() {

            super(Boolean.TRUE);
        }

        /**
         * Obtiene el <em>binding</em> al que pertenece esta lista.
         * 
         * @return el <em>binding</em> al que pertenece esta lista.
         */
        public SilentShuttleListBinding getBinding() {

            return this.binding;
        }

        /**
         * Obtiene el <em>listener</em> a saltarse.
         * 
         * @return el <em>listener</em> a saltarse.
         */
        public ListSelectionListener getListenerToSkip() {

            return this.listenerToSkip;
        }

        /**
         * Establece el <em>binding</em> al que pertenece esta lista.
         * 
         * @param binding
         *            el <em>binding</em> al que pertenece esta lista.
         */
        public void setBinding(SilentShuttleListBinding binding) {

            this.binding = binding;
        }

        /**
         * Establece el <em>listener</em> a saltarse.
         * 
         * @param listenerToSkip
         *            el <em>listener</em> a saltarse.
         */
        public void setListenerToSkip(ListSelectionListener listenerToSkip) {

            this.listenerToSkip = listenerToSkip;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void moveAllLeftToRight() {

            final SilentShuttleListBinding theBinding = this.getBinding();

            // Realizar el cambio de forma silenciosa
            this.removeListSelectionListener(theBinding.getListSelectedValueMediator());
            super.moveAllLeftToRight();
            this.addListSelectionListener(theBinding.getListSelectedValueMediator());

            // Actualizar el value model con los elementos seleccionados
            theBinding.updateSelectionHolderFromList(theBinding.getValueChangeHandler());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void moveAllRightToLeft() {

            final SilentShuttleListBinding theBinding = this.getBinding();

            // Realizar el cambio de forma silenciosa
            this.removeListSelectionListener(theBinding.getListSelectedValueMediator());
            super.moveAllRightToLeft();
            this.addListSelectionListener(theBinding.getListSelectedValueMediator());

            // Actualizar el value model con los elementos seleccionados
            theBinding.updateSelectionHolderFromList(theBinding.getValueChangeHandler());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void moveLeftToRight() {

            final SilentShuttleListBinding theBinding = this.getBinding();

            // Realizar el cambio de forma silenciosa
            this.removeListSelectionListener(theBinding.getListSelectedValueMediator());
            super.moveLeftToRight();
            this.addListSelectionListener(theBinding.getListSelectedValueMediator());

            // Actualizar el value model con los elementos seleccionados
            theBinding.updateSelectionHolderFromList(theBinding.getValueChangeHandler());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void moveRightToLeft() {

            final SilentShuttleListBinding theBinding = this.getBinding();

            // Realizar el cambio de forma silenciosa
            this.removeListSelectionListener(theBinding.getListSelectedValueMediator());
            super.moveRightToLeft();
            this.addListSelectionListener(theBinding.getListSelectedValueMediator());

            // Actualizar el value model con los elementos seleccionados
            theBinding.updateSelectionHolderFromList(theBinding.getValueChangeHandler());
        }
    }

    /**
     * <em>Listener</em> que reproduce los eventos de selección en la {@link SilentShuttleListBinding#shuttleList}
     * actualizando el <em>value model</em> {@link SilentShuttleListBinding#selectedItemsHolder} ante cambios en el
     * componente.
     * 
     * @see ShuttleListBinding#updateSelectionHolderFromList
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class ListSelectedValueMediator implements ListSelectionListener {

        /**
         * {@inheritDoc}
         */
        public void valueChanged(ListSelectionEvent e) {

            if (!e.getValueIsAdjusting()) {
                SilentShuttleListBinding.this.updateSelectionHolderFromList(//
                        SilentShuttleListBinding.this.getValueChangeHandler());
            }
        }
    }

    /**
     * 
     * <em>Listener</em> registrado sobre {@link SilentShuttleListBinding#selectedItemsHolder} que actualiza el
     * componente {@link SilentShuttleListBinding#shuttleList} ante cambios en el <em>value model</em>.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class ValueChangeHandler implements PropertyChangeListener {

        /**
         * {@inheritDoc}
         */
        public void propertyChange(PropertyChangeEvent evt) {

            SilentShuttleListBinding.this.setSelectedValue(SilentShuttleListBinding.this.getValueChangeHandler());
        }
    }
}
