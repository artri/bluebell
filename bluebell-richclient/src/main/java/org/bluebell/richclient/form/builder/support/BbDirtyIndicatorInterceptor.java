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
import java.awt.Dimension;
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
import org.springframework.richclient.application.Application;
import org.springframework.richclient.factory.AbstractControlFactory;
import org.springframework.richclient.util.Assert;
import org.springframework.richclient.util.RcpSupport;

/**
 * Improved version of <code>org.springframework.richclient.form.builder.support.DirtyIndicatorInterceptor</code>.
 * <p>
 * Original version has some errors with <code>ShuttleListBinding</code> (may be due to its bad implementation...) and
 * also with:
 * <ul>
 * <li>After changing backing form object and whenever property value is unchanged internal interceptor state (original,
 * previous and current value) gets damaged.
 * <li>To detect property changes a <code>ValueChangeHandler</code> was used, this is no needed anymore since the own
 * property is aware of its dirty state.
 * <li>Employs <code>OverlayService</code> instead of <code>OverlayHelper</code>.
 * </ul>
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
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbDirtyIndicatorInterceptor extends AbstractOverlayFormComponentInterceptor {

    /**
     * The dirty icon identifier.
     */
    private static final String DIRTY_ICON_KEY = "dirty.overlay";

    /**
     * The dirty message identifier.
     */
    private static final String DIRTY_MESSAGE_KEY = "dirty.message";

    /**
     * The <code>null</code> value message identifier.
     */
    private static final String NULL_MESSAGE_KEY = "null";

    /**
     * The revert icon identifier.
     */
    private static final String REVERT_ICON_KEY = "revert.overlay";

    /**
     * The revert message identifier.
     */
    private static final String REVERT_MESSAGE_KEY = "revert.message";

    /**
     * Creates the interceptor given the form model.
     * 
     * @param formModel
     *            the form model.
     */
    public BbDirtyIndicatorInterceptor(FormModel formModel) {

        super(formModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getPosition() {

        return SwingConstants.NORTH_WEST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractOverlayHandler createOverlayHandler(String propertyName, JComponent component) {

        return new DirtyOverlayHandler(propertyName, component);
    }

    /**
     * The dirty indicator overlay handler.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class DirtyOverlayHandler extends AbstractOverlayFormComponentInterceptor.AbstractOverlayHandler {

        /**
         * Let know if the backing form object has changed ( {@link #objectChanged()}). In such a case should be handled
         * later ({@link #valueChanged(PropertyChangeEvent)}).
         */
        private Boolean reset;

        /**
         * Whether the value model is dirty.
         */
        private Boolean dirty;

        /**
         * Whether the original property value ({@link #originalValue}) is known.
         */
        private Boolean originalValueKnown;

        /**
         * The original property value.
         * <p>
         * Its value should be ignored if {@link #originalValueKnown} is <code>false</code>.
         */
        private Object originalValue;

        /**
         * The previous property value.
         */
        private Object previousValue;

        /**
         * The overlay component factory.
         */
        private DirtyOverlay dirtyOverlay;

        /**
         * Creates the handler given the property name and the component.
         * <p>
         * Installs listeners to handle changes.
         * 
         * @param propertyName
         *            the property name.
         * @param component
         *            the component.
         */
        public DirtyOverlayHandler(final String propertyName, final JComponent component) {

            super(propertyName, component);

            final FormModel formModel = BbDirtyIndicatorInterceptor.this.getFormModel();

            // Listen field metadata dirty property change events
            formModel.getFieldMetadata(this.getPropertyName()).addPropertyChangeListener(FormModel.DIRTY_PROPERTY, //
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {

                            DirtyOverlayHandler.this.dirtyChanged(evt);
                        }
                    });

            // Listen value model changes
            formModel.getValueModel(this.getPropertyName()).addValueChangeListener(//
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {

                            DirtyOverlayHandler.this.valueChanged(evt);
                        }
                    });

            // Listen form objet changes
            formModel.getFormObjectHolder().addValueChangeListener(//
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {

                            DirtyOverlayHandler.this.objectChanged();
                        }
                    });

            // Simulate object changed event
            this.objectChanged();
        }

        /**
         * Handles dirty changes.
         * 
         * @param evt
         *            the event.
         */
        public void dirtyChanged(PropertyChangeEvent evt) {

            this.isDirty((Boolean) evt.getNewValue());

            if (this.isDirty() && !this.isOriginalValueKnown()) {
                this.hasReset(Boolean.FALSE).isOriginalValueKnown(Boolean.TRUE).originalValue(this.getPreviousValue());

            } else if (!this.isDirty()) {
                // HACK, (JAF), 20081020: let user change original value when using
                // DirtyTrackingUtils#setValueWithoutTrackDirtythis
                this.hasReset(Boolean.TRUE);
            }

            this.refreshOverlay(this.isDirty());
        }

        /**
         * Handles object changes.
         */
        public void objectChanged() {

            this.hasReset(true).isDirty(false).isOriginalValueKnown(false).originalValue(null).previousValue(null);

            this.hideOverlay();
        }

        /**
         * Handles property value changes.
         * 
         * @param evt
         *            the event.
         */
        public void valueChanged(PropertyChangeEvent evt) {

            final Boolean show = this.isDirty() && !this.hasReset();

            this.originalValue(null).previousValue(evt.getOldValue());

            if (this.hasReset()) {
                // It's time for updating (after a previous call to "objectChanged")
                this.hasReset(Boolean.FALSE).isOriginalValueKnown(Boolean.FALSE);
            }

            this.refreshOverlay(show);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Boolean showOverlay() {

            final FieldMetadata fMetadata = this.getFieldMetadata();

            // Proceed only if field is writable and enabled
            final Boolean proceed = ((fMetadata != null) && (fMetadata.isReadOnly() || fMetadata.isEnabled()));
            if (proceed) {

                final MessageSource messageSrc = (MessageSource) Application.services().getService(MessageSource.class);
                final Locale l = Locale.getDefault();
                final Object originalV = this.getOriginalValue();
                final String fieldName = this.getFormModel().getFieldFace(this.getPropertyName()).getDisplayName();
                final String nullStr = messageSrc.getMessage(BbDirtyIndicatorInterceptor.NULL_MESSAGE_KEY, //
                        new Object[] {}, l);

                // Update the tooltips
                final String dirtyTooltip = messageSrc.getMessage(BbDirtyIndicatorInterceptor.DIRTY_MESSAGE_KEY, //
                        new Object[] { fieldName, (originalV != null) ? originalV : nullStr }, l);
                final String revertTooltip = messageSrc.getMessage(BbDirtyIndicatorInterceptor.REVERT_MESSAGE_KEY, //
                        new Object[] { fieldName }, l);

                this.getDirtyOverlay().getDirtyLabel().setToolTipText(dirtyTooltip);
                this.getDirtyOverlay().getRevertButton().setToolTipText(revertTooltip);

                // Show definitely the overlay
                return super.showOverlay();
            }

            return Boolean.FALSE;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected JComponent createOverlay() {

            this.dirtyOverlay(new DirtyOverlay());

            this.getDirtyOverlay().getRevertButton().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    final Object valueToSet = DirtyOverlayHandler.this.getOriginalValue();
                    DirtyOverlayHandler.this.getValueModel().setValue(valueToSet);
                }
            });

            return getDirtyOverlay().getControl();
        }

        /**
         * Gets the hasReset.
         * 
         * @return the hasReset.
         */
        private Boolean hasReset() {

            return this.reset;
        }

        /**
         * Sets the hasReset.
         * 
         * @param reset
         *            the hasReset to set.
         * 
         * @return <code> this</code>.
         */
        private DirtyOverlayHandler hasReset(Boolean reset) {

            Assert.notNull(reset, "reset");

            this.reset = reset;

            return this;
        }

        /**
         * Gets the isDirty.
         * 
         * @return the isDirty.
         */
        private Boolean isDirty() {

            return this.dirty;
        }

        /**
         * Sets the isDirty.
         * 
         * @param dirty
         *            the isDirty to set.
         * 
         * @return <code> this</code>.
         */
        private DirtyOverlayHandler isDirty(Boolean dirty) {

            Assert.notNull(dirty, "dirty");

            this.dirty = dirty;

            return this;
        }

        /**
         * Gets the isOriginalValueInitialized.
         * 
         * @return the isOriginalValueInitialized.
         */
        private Boolean isOriginalValueKnown() {

            return this.originalValueKnown;
        }

        /**
         * Sets the isOriginalValueInitialized.
         * 
         * @param originalValueKnown
         *            the isOriginalValueInitialized to set.
         * 
         * @return <code> this</code>.
         */
        private DirtyOverlayHandler isOriginalValueKnown(Boolean originalValueKnown) {

            Assert.notNull(originalValueKnown, "originalValueKnown");

            this.originalValueKnown = originalValueKnown;

            return this;
        }

        /**
         * Gets the originalValue.
         * 
         * @return the originalValue.
         */
        private Object getOriginalValue() {

            return this.originalValue;
        }

        /**
         * Sets the originalValue.
         * 
         * @param originalValue
         *            the originalValue to set.
         * 
         * @return <code> this</code>.
         */
        private DirtyOverlayHandler originalValue(Object originalValue) {

            this.originalValue = originalValue;

            return this;
        }

        /**
         * Gets the dirtyOverlay.
         * 
         * @return the dirtyOverlay.
         */
        private DirtyOverlay getDirtyOverlay() {

            return this.dirtyOverlay;
        }

        /**
         * Sets the dirtyOverlay.
         * 
         * @param dirtyOverlay
         *            the dirtyOverlay to set.
         * 
         * @return <code> this</code>.
         */
        private DirtyOverlayHandler dirtyOverlay(DirtyOverlay dirtyOverlay) {

            Assert.notNull(dirtyOverlay, "dirtyOverlay");

            this.dirtyOverlay = dirtyOverlay;

            return this;
        }

        /**
         * Gets the previousValue.
         * 
         * @return the previousValue.
         */
        private Object getPreviousValue() {

            return this.previousValue;
        }

        /**
         * A la hora de recordar el valor anterior es importante diferenciar el caso de los <em>value model</em> de tipo
         * {@link org.bluebell.richclient.form.BbFormModelHelper.DirtyTrackingDCBCVM}.
         * <p>
         * A tal efecto se crea este método que encapsula su tratamiento.
         * 
         * @param previousValue
         *            el valor a recordar.
         * 
         * @return <code> this</code>.
         */
        @SuppressWarnings("unchecked")
        private DirtyOverlayHandler previousValue(Object previousValue) {

            this.previousValue = previousValue;

            // HACK, (20080106), behaviour must change using DirtyTrackingDCBCVM. In such a case, events have the same
            // old and new value (oldValue == newValue). Perhaps the solution should be modify DirtyTrackingDCBCVM event
            // publishing behaviour

            ValueModel vm = this.getValueModel();
            while (vm instanceof ValueModelWrapper) {

                if (vm instanceof DirtyTrackingDCBCVM) {

                    final ValueModelWrapper vmWrapper = (ValueModelWrapper) vm;
                    this.previousValue = vmWrapper.getWrappedValueModel().getValue();
                    break;
                }
                vm = ((ValueModelWrapper) vm).getWrappedValueModel();
            }

            return this;
        }
    }

    /**
     * The dirty overlay component.
     * <p>
     * It's a <code>JPanel</code> with a dirty label at the left side and a revert button at the right side.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private static class DirtyOverlay extends AbstractControlFactory {
        /**
         * The label.
         */
        private JLabel dirtyLabel;

        /**
         * The revert button.
         */
        private JButton revertButton;

        /**
         * Creates the control.
         * 
         * @return the control.
         */
        @Override
        protected JComponent createControl() {

            // The container
            final JPanel container = new JPanel(new BorderLayout()) {

                /**
                 * Es una clase <code>Serializable</code>.
                 */
                private static final long serialVersionUID = -4784028910581102377L;

                @Override
                public void repaint() {

                    // hack for RCP-426: if the form component is on a tabbed pane, when switching between tabs when the
                    // overlay is visible, the overlay is not correctly repainted. When we trigger a revalidate here,
                    // everything is ok.
                    this.revalidate();
                    super.repaint();
                }
            };
            container.setName("dirtyOverlay");
            container.setOpaque(Boolean.FALSE);

            container.add(this.getDirtyLabel(), BorderLayout.CENTER);
            container.add(this.getRevertButton(), BorderLayout.EAST);

            return container;
        }

        /**
         * Gets the dirty label.
         * 
         * @return the dirty label.
         */
        private JLabel getDirtyLabel() {

            if (this.dirtyLabel == null) {

                final Icon icon = RcpSupport.getIcon(BbDirtyIndicatorInterceptor.DIRTY_ICON_KEY);
                final JLabel label = new JLabel(icon);

                this.setDirtyLabel(label);
            }

            return this.dirtyLabel;
        }

        /**
         * Sets the dirty label.
         * 
         * @param dirtyLabel
         *            the dirty label to set.
         */
        private void setDirtyLabel(JLabel dirtyLabel) {

            Assert.notNull(dirtyLabel, "dirtyLabel");

            this.dirtyLabel = dirtyLabel;
        }

        /**
         * Gets the revert button.
         * 
         * @return the revert button.
         */
        private JButton getRevertButton() {

            if (this.revertButton == null) {

                final Icon icon = RcpSupport.getIcon(BbDirtyIndicatorInterceptor.REVERT_ICON_KEY);

                final JButton button = new JButton(icon);
                button.setBorderPainted(Boolean.FALSE);
                button.setContentAreaFilled(Boolean.FALSE);
                button.setFocusable(Boolean.FALSE);
                if (icon != null) {
                    button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
                }

                this.setRevertButton(button);
            }

            return this.revertButton;
        }

        /**
         * Sets the revert button.
         * 
         * @param revertButton
         *            the revert button to set.
         */
        private void setRevertButton(JButton revertButton) {

            Assert.notNull(revertButton, "revertButton");

            this.revertButton = revertButton;
        }
    }
}
