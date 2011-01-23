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

package org.bluebell.richclient.text;

import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.springframework.binding.form.CommitListener;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.value.CommitTrigger;
import org.springframework.richclient.form.builder.FormComponentInterceptor;
import org.springframework.richclient.form.builder.FormComponentInterceptorFactory;
import org.springframework.richclient.form.builder.support.AbstractFormComponentInterceptor;
import org.springframework.richclient.text.TextComponentContainer;
import org.springframework.richclient.text.TextComponentPopup;

/**
 * Reescribe {@link org.springframework.richclient.text.TextComponentPopupInterceptorFactory} con el <b>único</b>
 * objetivo de no romper la suscripción de los comandos globales (copiar, pegar, etc.) cada vez que el componente de
 * tipo texto asociado pierda el foco.
 * 
 * @see org.springframework.richclient.text.TextComponentPopupInterceptorFactory
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TextComponentPopupInterceptorFactory implements FormComponentInterceptorFactory {

    /**
     * Construye la factoría para la creación de interceptores de tipo <em>text component popup</em>.
     */
    public TextComponentPopupInterceptorFactory() {

        super();
    }

    /**
     * Obtiene un interceptor para un modelo de formulario dado.
     * 
     * @param formModel
     *            el modelo del formulario.
     * @return un nuevo interceptor de tipo {@link TextComponentPopupInterceptorFactory}.
     */
    public FormComponentInterceptor getInterceptor(FormModel formModel) {

        return new TextComponentPopupInterceptor(formModel);
    }

    /**
     * Extensión de {@link TextComponentPopup} que no rompe la suscripcion de sus comandos globales al perder el foco,
     * ya que de este modo no es posible ejecutarlos desde una <em>toolbar</em> por ejemplo.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private static final class ExtendedTextComponentPopup extends TextComponentPopup {

        /**
         * Construye el popup.
         * 
         * @param textComponent
         *            el componente de texto asociado al popup.
         * @param resetUndoHistoryTrigger
         *            un manejador para gestionar la mecánica de <em>UNDO</em>.
         */
        private ExtendedTextComponentPopup(JTextComponent textComponent, CommitTrigger resetUndoHistoryTrigger) {

            super(textComponent, resetUndoHistoryTrigger);
        }

/**
         * Invocado cada vez que el componente de texto pierde el foco.
         * <p>
         * A diferencia de {@link TextComponentPopup no hace nada.
         * 
         * @param e the focus event.
         */
        @Override
        public void focusLost(FocusEvent e) {

            // Nothing to do
        }

        /**
         * Añade un nuevo menú popup a un componente dado.
         * 
         * @param textComponent
         *            el componente de texto.
         * @param resetUndoHistoryTrigger
         *            un manejador para gestionar la mecánica de <em>UNDO</em>.
         * @see TextCom
         */
        public static void attachPopup(JTextComponent textComponent, CommitTrigger resetUndoHistoryTrigger) {

            new ExtendedTextComponentPopup(textComponent, resetUndoHistoryTrigger);
        }
    }

    /**
     * El interceptor creado por la factoría.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private class TextComponentPopupInterceptor extends AbstractFormComponentInterceptor implements
            PropertyChangeListener, CommitListener {

        /**
         * Un manejador.
         */
        private CommitTrigger resetTrigger;

        /**
         * Construye el interceptor dado el modelo del formulario.
         * 
         * @param formModel
         *            el modelo del formulario.
         */
        protected TextComponentPopupInterceptor(FormModel formModel) {

            super(formModel);
        }

        /**
         * {@inheritDoc}
         */
        public void postCommit(FormModel formModel) {

            this.getResetTrigger().commit();
        }

        /**
         * {@inheritDoc}
         */
        public void preCommit(FormModel formModel) {

            // Nothing to do
        }

        /**
         * Procesa cada propiedad del formulario asociándole un menú popup si sólo si su componente es de tipo texto o
         * incluye un componente de tipo texto.
         * 
         * @param propertyName
         *            el nombre de la propiedad.
         * @param component
         *            el componente representando la propiedad.
         */
        @Override
        public void processComponent(String propertyName, JComponent component) {

            final JComponent innerComp = this.getInnerComponent(component);
            if (innerComp instanceof JTextComponent) {
                TextComponentPopupInterceptorFactory.ExtendedTextComponentPopup.attachPopup(//
                        (JTextComponent) innerComp, this.getResetTrigger());
            } else if (innerComp instanceof TextComponentContainer) {
                TextComponentPopupInterceptorFactory.ExtendedTextComponentPopup.attachPopup(//
                        ((TextComponentContainer) innerComp).getComponent(), this.getResetTrigger());
            }
        }

        /**
         * {@inheritDoc}
         */
        public void propertyChange(PropertyChangeEvent evt) {

            this.getResetTrigger().commit();
        }

        /**
         * Obtiene el manejador para resetear y si no existe lo crea y registra los <em>listeners</em> correspondientes.
         * 
         * @return el manejador.
         */
        private CommitTrigger getResetTrigger() {

            if (this.resetTrigger == null) {
                this.resetTrigger = new CommitTrigger();
                this.registerListeners();
            }
            return this.resetTrigger;
        }

        /**
         * Registra los <em>listeners</em> sobre el modelo del formulario.
         */
        private void registerListeners() {

            final FormModel formModel = this.getFormModel();
            formModel.addCommitListener(this);
            formModel.getFormObjectHolder().addValueChangeListener(this);
            // SwingUtils.weakPropertyChangeListener(this));
        }
    }
}
