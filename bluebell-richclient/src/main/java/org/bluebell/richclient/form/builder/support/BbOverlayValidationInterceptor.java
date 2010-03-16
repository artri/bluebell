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

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.springframework.binding.form.FormModel;
import org.springframework.richclient.core.Guarded;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.core.Severity;
import org.springframework.richclient.dialog.DefaultMessageAreaModel;
import org.springframework.richclient.dialog.Messagable;
import org.springframework.richclient.form.builder.support.ValidationInterceptor;
import org.springframework.richclient.util.Assert;
import org.springframework.richclient.util.RcpSupport;

/**
 * Improved version of <code>org.springframework.richclient.form.builder.support.OverlayValidationInterceptorFactory
 * .OverlayValidationInterceptor</code>.
 * <p>
 * This version employs <code>OverlayService</code> instead of <code>OverlayHelper</code>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbOverlayValidationInterceptor extends AbstractOverlayFormComponentInterceptor {

    /**
     * Creates the form component interceptor given the form model.
     * 
     * @param formModel
     *            the form model.
     */
    public BbOverlayValidationInterceptor(FormModel formModel) {

        super(formModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getPosition() {

        return SwingConstants.SOUTH_WEST;
    }

    /**
     * The validation overlay handler.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class ValidationOverlayHandler extends AbstractOverlayFormComponentInterceptor.AbstractOverlayHandler
            implements Guarded {

        /**
         * Creates the validation overlay handler given the property name and target component.
         * <p>
         * Delegates logic into a <code>ValidationInteceptor</code>.
         * 
         * @param propertyName
         *            the property name.
         * @param targetComponent
         *            the target component.
         */
        public ValidationOverlayHandler(String propertyName, JComponent targetComponent) {

            super(propertyName, targetComponent);

            // Employ a validation interceptor to reuse its validation aware logic.
            new ValidationInterceptor(this.getFormModel()) {

                /**
                 * Initializes the interceptor installing the guarded instance and registering the message receiver.
                 */
                public void init() {

                    final ValidationOverlayHandler thiz = ValidationOverlayHandler.this;

                    this.registerGuarded(thiz.getPropertyName(), thiz);
                    this.registerMessageReceiver(thiz.getPropertyName(), (ValidationOverlay) thiz.getOverlay());
                }
            } // #init (breakline to avoid CS warning)
                    .init();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected JComponent createOverlay() {

            return new ValidationOverlay();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEnabled() {

            return Boolean.TRUE; // foo implementation
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setEnabled(boolean enabled) {

            this.refreshOverlay(!enabled);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractOverlayHandler createOverlayHandler(String propertyName, JComponent component) {

        return new ValidationOverlayHandler(propertyName, component);
    }

    /**
     * The validation overlay component.
     * <p>
     * It's a <code>JLabel</code> that implements <code>Messagable</code> and <code>MayHaveMessagableTab</code>.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private static class ValidationOverlay extends JLabel implements Messagable {

        /**
         * It's a <code>Serializable</code> class.
         */
        private static final long serialVersionUID = 383550730517671431L;

        /**
         * The message buffer.
         */
        private DefaultMessageAreaModel messageBuffer;

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("deprecation")
        public void setMessage(Message message) {

            // TODO, (JAF), 20100315, I think it's better to move this logic to the handler.
            this.getMessageBuffer().setMessage(message);

            final Message theMessage = this.getMessageBuffer().getMessage();
            this.setToolTipText(theMessage.getMessage());

            final Severity severity = theMessage.getSeverity();
            final Icon icon;
            if (severity != null) {
                icon = RcpSupport.getIcon("severity." + severity.getLabel() + ".overlay");
            } else {
                icon = null;
            }
            this.setIcon(icon);
        }

        /**
         * Gets the message buffer and if does not exist then create it.
         * 
         * @return the message buffer.
         */
        private DefaultMessageAreaModel getMessageBuffer() {

            if (this.messageBuffer == null) {
                this.setMessageBuffer(new DefaultMessageAreaModel(this));
            }

            return this.messageBuffer;
        }

        /**
         * Sets the message buffer.
         * 
         * @param messageBuffer
         *            the message buffer to set.
         */
        private void setMessageBuffer(DefaultMessageAreaModel messageBuffer) {

            Assert.notNull(messageBuffer, "messageBuffer");

            this.messageBuffer = messageBuffer;
        }
    }
}
