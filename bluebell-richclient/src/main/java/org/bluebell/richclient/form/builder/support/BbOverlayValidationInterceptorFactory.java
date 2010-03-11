/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.bluebell.richclient.form.builder.support;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.bluebell.richclient.components.OverlayService;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.validation.ValidationListener;
import org.springframework.binding.validation.ValidationResults;
import org.springframework.binding.validation.ValidationResultsModel;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.components.MayHaveMessagableTab;
import org.springframework.richclient.components.MessagableTab;
import org.springframework.richclient.core.Guarded;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.core.Severity;
import org.springframework.richclient.dialog.DefaultMessageAreaModel;
import org.springframework.richclient.dialog.Messagable;
import org.springframework.richclient.form.builder.FormComponentInterceptor;
import org.springframework.richclient.form.builder.support.AbstractFormComponentInterceptor;
import org.springframework.richclient.form.builder.support.OverlayValidationInterceptorFactory;
import org.springframework.richclient.util.RcpSupport;

public class BbOverlayValidationInterceptorFactory extends OverlayValidationInterceptorFactory {
    public FormComponentInterceptor getInterceptor(FormModel formModel) {

        return new BbOverlayValidationInterceptor(formModel);
    }

    public class BbOverlayValidationInterceptor extends OverlayValidationInterceptor {

        private JComponent targetComponent;

        public BbOverlayValidationInterceptor(FormModel formModel) {

            super(formModel);
        }

        public void processComponent(String propertyName, final JComponent component) {

            final ErrorReportingOverlay overlay = new ErrorReportingOverlay();

            targetComponent = component;

            registerGuarded(propertyName, overlay);
            registerMessageReceiver(propertyName, overlay);

        }

        /**
         * Register a guarded object on a specific property. To keep things in sync, it also triggers a first time
         * check. (validationResultsModel can already be populated)
         * 
         * @param propertyName
         *            property to listen for.
         * @param guarded
         *            component that needs guarding.
         * @return {@link ValidationListener} created during the process.
         */
        protected ValidationListener registerGuarded(String propertyName, Guarded guarded) {

            final ValidationResultsModel validationResults = ((ValidatingFormModel) BbOverlayValidationInterceptor.this
                    .getFormModel()).getValidationResults();
            GuardedValidationListener guardedValidationListener = new GuardedValidationListener(propertyName, guarded);
            validationResults.addValidationListener(propertyName, guardedValidationListener);
            guardedValidationListener.validationResultsChanged(validationResults);
            return guardedValidationListener;
        }

        /**
         * {@link ValidationListener} that will handle the enabling of the guard according to the validation of the
         * given property.
         */
        private class GuardedValidationListener implements ValidationListener {
            private final String propertyName;

            private final Guarded guarded;

            public GuardedValidationListener(String propertyName, Guarded guarded) {

                this.propertyName = propertyName;
                this.guarded = guarded;
            }

            public void validationResultsChanged(ValidationResults results) {
            final OverlayService overlaySvc = (OverlayService) Application.services().getService(OverlayService.class);
            
            final JComponent overlay = (JComponent) guarded;
            if(results.getMessageCount(propertyName) == 0) {
             // FIXME, (JAF) 20100311, overlay needs to be installed every time due to overlay installation is done
                // during interception (and unfortunately before JideBindingFactory proceeds). Solution is make
                // installation after interception
                overlaySvc.installOverlay(targetComponent, overlay);
                overlaySvc.showOverlay(targetComponent, overlay);
            } else {
                overlaySvc.hideOverlay(targetComponent, overlay);
            }
        }
        }
    }

    private static class ErrorReportingOverlay extends JLabel implements Messagable, Guarded, MayHaveMessagableTab {

        private DefaultMessageAreaModel messageBuffer = new DefaultMessageAreaModel(this);
        private MessagableTab messagableTab = null;
        private int tabIndex = 0;

        public boolean isEnabled() {

            return true;
        }

        public void setEnabled(boolean enabled) {

            setVisible(!enabled);
        }

        public void setMessagableTab(MessagableTab messagableTab, int tabIndex) {

            this.messagableTab = messagableTab;
            this.tabIndex = tabIndex;
        }

        public void setMessage(Message message) {

            // geef de messgage door aan de omringende tabbedpane als ie er is
            if (this.messagableTab != null)
                this.messagableTab.setMessage(this, message, this.tabIndex);
            messageBuffer.setMessage(message);
            message = messageBuffer.getMessage();
            setToolTipText(message.getMessage());
            Severity severity = message.getSeverity();
            if (severity != null)
                setIcon(RcpSupport.getIcon("severity." + severity.getLabel() + ".overlay"));
            else
                setIcon(null);
        }
    }
}
