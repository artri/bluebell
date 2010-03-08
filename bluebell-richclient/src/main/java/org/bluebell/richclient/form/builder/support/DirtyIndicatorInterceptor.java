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

package org.bluebell.richclient.form.builder.support;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.bluebell.binding.value.support.DirtyTrackingDCBCVM;
import org.springframework.binding.form.FieldMetadata;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ValueModelWrapper;
import org.springframework.context.MessageSource;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.factory.AbstractControlFactory;
import org.springframework.richclient.form.builder.support.AbstractFormComponentInterceptor;
import org.springframework.richclient.form.builder.support.InterceptorOverlayHelper;
import org.springframework.richclient.image.IconSource;

/**
 * <b>Modificación de <code>org.springframework.richclient.form.builder.support.DirtyIndicatorInterceptor</code> con
 * funcionamiento mejorado</b>.
 * <p>
 * Por una parte la versión original daba lugar a comportamientos erróneos, por ejemplo con la <code>ShuttleList</code>
 * (quizás propiciados por su mala implementación) y con los atributos internacionalizados. No obstante sus principales
 * defectos, ya solventados, consistían en:
 * <ul>
 * <li>Al cambiar el <em>backing object</em> del modelo y si la propiedad no varía su valor entonces el interceptor deja
 * de reconocer correctamente los valores original, previo y actual.
 * <li>Para detectar cambios en las propiedades se utilizaba un <code>ValueChangeHandler</code>, esa penalización no es
 * necesaria ya que debe ser la propia propiedad quien nos indique si está o no <em>dirty</em>.
 * </ul>
 * <p>
 * <b>Nótese</b> que no se ha extendido la implementación original dado que eso implicaría redifinir la mayor parte de
 * sus métodos con los problemas que ello conlleva.
 * <p>
 * 
 * <pre>
 * Adds a &quot;dirty overlay&quot; to a component that is triggered by user editing. The
 * overlaid image is retrieved by the image key &quot;dirty.overlay&quot;. The image is
 * placed at the top-left corner of the component, and the image's tooltip is
 * set to a message (retrieved with key &quot;dirty.message&quot;) such as &quot;{field} has
 * changed, original value was {value}.&quot;. It also adds a small revert button
 * that resets the value of the field.
 * </pre>
 * 
 * @author Peter De Bruycker
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a> modificada toda la lógica de detección
 *         de propiedades <em>dirty</em>, se conserva el control con el <em>overlay</em>.
 * 
 * @see #processComponent(String, JComponent)
 */
public class DirtyIndicatorInterceptor extends AbstractFormComponentInterceptor {
    /**
     * El identificador del icono indicando que la propiedad está <em>dirty</em> .
     */
    private static final String DIRTY_ICON_KEY = "dirty.overlay";

    /**
     * El identificador del mensaje indicando que la propiedad está <em>dirty</em>.
     */
    private static final String DIRTY_MESSAGE_KEY = "dirty.message";

    /**
     * El identificador del mensaje a incorporar cuando aparezca <code>null</code>.
     */
    private static final String NULL_MESSAGE_KEY = "null";

    /**
     * El identificador del icono para hacer <em>revert</em>.
     */
    private static final String REVERT_ICON_KEY = "revert.overlay";

    /**
     * El identificador del mensaje de <em>revert</em>.
     */
    private static final String REVERT_MESSAGE_KEY = "revert.message";

    /**
     * Construye el interceptor a partir del modelo del formulario.
     * 
     * @param formModel
     *            el modelo.
     */
    public DirtyIndicatorInterceptor(FormModel formModel) {

        super(formModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processComponent(final String propertyName, final JComponent component) {

        // Clase que gestiona el estado de la propiedad
        final OverlayHandler overlayHandler = new OverlayHandler(propertyName, component);

        // Escuchar cambios en la propiedad "dirty" de la metainfo. del campo
        this.getFormModel().getFieldMetadata(propertyName).addPropertyChangeListener(FormModel.DIRTY_PROPERTY, //
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {

                        overlayHandler.dirtyChanged(evt);
                    }
                });

        // Escuchar cambios en el valor de la propiedad
        this.getFormModel().getValueModel(propertyName).addValueChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {

                overlayHandler.valueChanged(evt);
            }
        });

        // Escuchar cambios en el objeto del formulario
        this.getFormModel().getFormObjectHolder().addValueChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {

                overlayHandler.objectChanged();
            }
        });
    }

    /**
     * Clase que crea el control para indicar si una propiedad ha cambiado o no su valor original.
     * 
     * @author Peter De Bruycker
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private static class DirtyOverlay extends AbstractControlFactory {
        /**
         * La etiqueta con el mensaje.
         */
        private JLabel dirtyLabel;

        /**
         * El modelo del formulario.
         */
        private final FormModel formModel;

        /**
         * El manejador que gestiona el <em>overlay</em> y mantiene el estado de la propiedad.
         */
        private final OverlayHandler overlayHandler;

        /**
         * La propiedad objeto de <em>dirty tracking</em>.
         */
        private final String propertyName;

        /**
         * El botón de <em>rever</em>.
         */
        private JButton revertButton;

        /**
         * Construye la factoría.
         * 
         * @param formModel
         *            el modelo del formulario.
         * @param propertyName
         *            el nombre de la propiedad a chequear.
         * @param overlayHandler
         *            el manejador que gestiona este control.
         */
        public DirtyOverlay(FormModel formModel, String propertyName, OverlayHandler overlayHandler) {

            this.formModel = formModel;
            this.propertyName = propertyName;
            this.overlayHandler = overlayHandler;
        }

        /**
         * Cambia la visibilidad del control.
         * 
         * @param visible
         *            <code>flag</code> indicando la visibilidad del control, <code>true</code> es visible.
         */
        public void setVisible(boolean visible) {

            this.getControl().setVisible(visible);
            // manually set the size, otherwise sometimes the overlay is not
            // shown (it has size 0,0)
            this.getControl().setSize(this.getControl().getPreferredSize());

            if (visible) {
                final FormModel theFormModel = DirtyIndicatorInterceptor.DirtyOverlay.this.formModel;
                final String thePropertyName = DirtyIndicatorInterceptor.DirtyOverlay.this.propertyName;
                final MessageSource messageSource = (MessageSource) ApplicationServicesLocator.services().getService(
                        MessageSource.class);
                final Locale locale = Locale.getDefault();
                final String displayName = theFormModel.getFieldFace(thePropertyName).getDisplayName();

                Object originalValue = DirtyIndicatorInterceptor.//
                DirtyOverlay.this.overlayHandler.originalValue;

                // Si el valor original es nulo internacionalizar su valor (ej.:
                // vacío).
                if (originalValue == null) {
                    originalValue = messageSource.getMessage(DirtyIndicatorInterceptor.NULL_MESSAGE_KEY, //
                            new Object[] {}, locale);
                }

                // El dirtyTooltip
                final String dirtyTooltip = messageSource.getMessage(//
                        DirtyIndicatorInterceptor.DIRTY_MESSAGE_KEY, //
                        new Object[] { displayName, originalValue }, locale);
                this.dirtyLabel.setToolTipText(dirtyTooltip);

                // El revertTooltip
                final String revertTooltip = messageSource.getMessage(//
                        DirtyIndicatorInterceptor.REVERT_MESSAGE_KEY, //
                        new Object[] { displayName }, locale);
                this.revertButton.setToolTipText(revertTooltip);
            }
        }

        /**
         * Creates the control.
         * 
         * @return the control.
         */
        @Override
        protected JComponent createControl() {

            final JPanel control = new JPanel(new BorderLayout()) {

                /**
                 * Es una clase <code>Serializable</code>.
                 */
                private static final long serialVersionUID = -4784028910581102377L;

                @Override
                public void repaint() {

                    // hack for RCP-426: if the form component is on a tabbed
                    // pane, when switching between tabs when the overlay is
                    // visible, the overlay is not correctly repainted. When we
                    // trigger a revalidate here, everything is ok.
                    this.revalidate();
                    super.repaint();
                }
            };

            control.setName("dirtyOverlay");
            control.setOpaque(true);
            final IconSource iconSource = (IconSource) //
            ApplicationServicesLocator.services().getService(IconSource.class);
            final Icon icon = iconSource.getIcon(DirtyIndicatorInterceptor.DIRTY_ICON_KEY);
            this.dirtyLabel = new JLabel(icon);
            control.add(this.dirtyLabel, BorderLayout.CENTER);

            this.createRevertButton();
            control.add(this.revertButton, BorderLayout.LINE_END);

            return control;
        }

        /**
         * Crea el botón para deshacer los cambios sobre un campo.
         */
        private void createRevertButton() {

            final int margin = -3;
            final IconSource iconSource = (IconSource) //
            ApplicationServicesLocator.services().getService(IconSource.class);
            final Icon icon = iconSource.getIcon(//
                    DirtyIndicatorInterceptor.REVERT_ICON_KEY);

            this.revertButton = new JButton(icon);
            this.revertButton.setBorderPainted(Boolean.FALSE);
            this.revertButton.setContentAreaFilled(Boolean.FALSE);
            this.revertButton.setFocusable(Boolean.FALSE);
            this.revertButton.setMargin(new Insets(margin, margin, margin, margin));
            this.revertButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    final FormModel theFormModel = DirtyIndicatorInterceptor.DirtyOverlay.this.formModel;
                    final String thePropertyName = DirtyIndicatorInterceptor.DirtyOverlay.this.propertyName;
                    final Object originalValue = //
                    DirtyIndicatorInterceptor.DirtyOverlay.this.overlayHandler.originalValue;

                    // Establecer el nuevo valor original.
                    theFormModel.getValueModel(thePropertyName).setValue(originalValue);
                }
            });
        }
    }

    /**
     * Clase capaz de gestionar la visualización y contenidos de un control <em>overlay</em> que se debe mostrar cada
     * vez que cambie el valor de una propiedad.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class OverlayHandler {

        /**
         * Indica si el objeto con la propiedad ha cambiado ( {@link #objectChanged()}) y que por tanto hay que tratarlo
         * más adelante ({@link #valueChanged(PropertyChangeEvent)}).
         */
        private Boolean hasReset;

        /**
         * Indica si el valor de la propiedad ha cambiado.
         */
        private Boolean isDirty;

        /**
         * Indica si el valor de {@link #originalValue} es fidedigno.
         */
        private Boolean isOriginalValueInitialized;

        /**
         * El valor original de la propiedad.
         * <p>
         * Su valor no debe ser tenido en cuenta mientra {@link #isOriginalValueInitialized} sea <code>false</code>.
         */
        private Object originalValue;

        /**
         * El control ha mostrar en caso de que la propiedad haya variado su valor.
         */
        private final DirtyOverlay overlay;

        /**
         * El valor anteriro de la propiedad.
         */
        private Object previousValue;

        /**
         * El nombre de la propiedad para la que vigilar el <em>dirty</em>.
         */
        private final String propertyName;

        /**
         * Construye el manejador a partir del nombre de la propiedad y el componente que la representa.
         * 
         * @param propertyName
         *            el nombre de la propiedad.
         * @param component
         *            el componente que la representa.
         */
        public OverlayHandler(final String propertyName, final JComponent component) {

            super();

            this.propertyName = propertyName;

            // Pintar o no el overlay
            final int xOffset = 5;
            final int yOffset = 0;

            this.overlay = new DirtyOverlay(DirtyIndicatorInterceptor.this.getFormModel(), propertyName, this);
            InterceptorOverlayHelper.attachOverlay(this.overlay.getControl(), component, SwingConstants.NORTH_WEST,
                    xOffset, yOffset);

            this.objectChanged();
        }

        /**
         * Método a invocar cada vez que cambie el estado del <em>flag</em> <code>dirty</code> de una propiedad.
         * 
         * @param evt
         *            el evento que notifica el cambio.
         */
        public void dirtyChanged(PropertyChangeEvent evt) {

            this.isDirty = ((Boolean) evt.getNewValue());

            if (this.isDirty && !this.isOriginalValueInitialized) {
                this.originalValue = this.previousValue;
                this.isOriginalValueInitialized = Boolean.TRUE;
                this.hasReset = Boolean.FALSE;
            } else if (!this.isDirty) {

                // HACK, (JAF), 20081020: para dar la oportunidad de cambiar el
                // valor original si se utiliza
                // DirtyTrackingUtils#setValueWithoutTrackDirty
                this.hasReset = Boolean.TRUE;
            }

            // Refrescar el overlay
            this.showOverlay(this.isDirty);
        }

        /**
         * Método a invocar cada vez que cambie el <em>backing object</em> del modelo del formulario.
         */
        public void objectChanged() {

            // Demorar la actualización hasta el próximo value changed
            this.hasReset = Boolean.TRUE;

            // Si hay un nuevo objeto el value model está "limpio" y el valor
            // original es inconsistente
            this.isDirty = Boolean.FALSE;
            this.isOriginalValueInitialized = Boolean.FALSE;

            // Actualizar el histórico de valores de la propiedad
            this.originalValue = null;
            this.setPreviousValue(null);

            // OCULTAR el overlay
            this.showOverlay(Boolean.FALSE);
        }

        /**
         * Método a invocar cada vez que cambie el valor de una propiedad.
         * 
         * @param evt
         *            el evento que notifica el cambio.
         */
        public void valueChanged(PropertyChangeEvent evt) {

            Boolean show = this.isDirty;

            this.setPreviousValue(evt.getOldValue());

            if (this.hasReset) {
                // Retomar la actualización demorada en "objectChanged"
                this.originalValue = null;
                this.isOriginalValueInitialized = Boolean.FALSE;
                this.hasReset = Boolean.FALSE;

                // Se debe OCULTAR el overlay
                show = Boolean.FALSE;
            } else if (!this.isOriginalValueInitialized) {
                // Establecer el valor original
                this.originalValue = null;
            }

            // Refrescar el overlay
            this.showOverlay(show);
        }

        /**
         * A la hora de recordar el valor anterior es importante diferenciar el caso de los <em>value model</em> de tipo
         * {@link org.bluebell.richclient.form.BbFormModelHelper.DirtyTrackingDCBCVM}.
         * <p>
         * A tal efecto se crea este método que encapsula su tratamiento.
         * 
         * @param value
         *            el valor a recordar.
         */
        @SuppressWarnings("unchecked")
        private void setPreviousValue(Object value) {

            this.previousValue = value;

            // HACK, (20080106), si se usa DirtyTrackingDCBCVM el comportamiento
            // ha de ser diferente, ya que ante cambios de valores los eventos
            // publican el mismo valor anterior y actual (una eventList)
            // FIXME, (20080106), quizás la solución sea modificar la
            // publicación de eventos de DirtyTrackingDCBCVM, de momento se
            // mantiene este hack.

            final FormModel formModel = DirtyIndicatorInterceptor.this.getFormModel();

            for (ValueModel vm = formModel.getValueModel(this.propertyName); //
            vm instanceof ValueModelWrapper; //
            vm = ((ValueModelWrapper) vm).getWrappedValueModel()) {
                if (vm instanceof DirtyTrackingDCBCVM) {
                    this.previousValue = ((DirtyTrackingDCBCVM) vm).getWrappedValueModel().getValue();
                    break;
                }
            }
        }

        /**
         * Muestra u oculta el <em>overlay</em>.
         * <p>
         * Para mostrar el <em>overlay</em> la propiedad ha de estar habilitada.
         * 
         * @param show
         *            <em>flag</em> indicando si se ha de mostrar o no el <em>overlay</em>.
         */
        private void showOverlay(Boolean show) {

            final FormModel formModel = DirtyIndicatorInterceptor.this.getFormModel();
            final FieldMetadata fieldMetadata = formModel.getFieldMetadata(this.overlay.propertyName);

            final Boolean overlayEnabled = (fieldMetadata != null) ? //
            !fieldMetadata.isReadOnly() || fieldMetadata.isEnabled() //
                    : Boolean.TRUE;

            this.overlay.setVisible(overlayEnabled && show);
        }
    }
}
