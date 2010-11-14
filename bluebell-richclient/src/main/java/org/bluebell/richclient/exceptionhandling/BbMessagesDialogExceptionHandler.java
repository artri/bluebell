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

package org.bluebell.richclient.exceptionhandling;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.ErrorCoded;
import org.springframework.richclient.exceptionhandling.MessagesDialogExceptionHandler;

/**
 * FIXME Clase temporal hasta que se investigue el otros manejadores que proporciona Spring RCP.
 * 
 * Manejador de excepción para la vista RCP.
 * 
 * Fue necesaria la realización de esta clase porque el manejador
 * {@link org.springframework.richclient.exceptionhandling.MessagesDialogExceptionHandler} no maneja las excepciones de
 * tipo <code>ErrorCoded</code>.
 * 
 * Post del <em>foro</em> de <b>Spring</b> en el que se hace la sugerencia de incluir el tratamiento de excepciones de
 * tipo <code>ErrorCoded</code>: {@link http://forum.springframework.org/showthread.php?t=49277}.
 * 
 * Tarea Jira en el que se muestra la tarea: {@link http ://jira.springframework.org/browse/RCP-534}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 * 
 */
public class BbMessagesDialogExceptionHandler extends MessagesDialogExceptionHandler {

    /**
     * El tamaño por defecto del identado.
     */
    private static final int DEFAULT_IDENT_LENGTH = 2;

    /**
     * El tamaño por defecto de una línea del mensaje.
     */
    private static final int DEFAULT_WRAP_LENGTH = 120;

    /**
     * El identado a aplicar al maestro.
     */
    private int identLength = BbMessagesDialogExceptionHandler.DEFAULT_IDENT_LENGTH;

    /**
     * El código del mensaje a mostrar.
     */
    private String messagesKey = null;

    /**
     * La longitud del una línea del mensaje a mostrar.
     */
    private int wrapLength = BbMessagesDialogExceptionHandler.DEFAULT_WRAP_LENGTH;

    /**
     * Obtiene el mensaje a mostrar en el diálogo.
     * 
     * @param throwable
     *            la excepción producida.
     * @return el mensaje a mostrar.
     */
    @Override
    public Object createExceptionContent(Throwable throwable) {

        final String[] messagesKeys = this.getMessagesKeys(throwable, ".description");
        final String[] parameters = new String[] { this.formatMessage(throwable.getMessage()) };
        return this.messageSourceAccessor.getMessage(new DefaultMessageSourceResolvable(messagesKeys, parameters,
                messagesKeys[0]));
    }

    /**
     * Obtiene le título a mostrar en el diálogo.
     * 
     * @param throwable
     *            la excepción producida.
     * @return el título a mostrar en el diálogo.
     */
    @Override
    public String resolveExceptionCaption(Throwable throwable) {

        final String[] messagesKeys = this.getMessagesKeys(throwable, ".caption");
        return this.messageSourceAccessor.getMessage(new DefaultMessageSourceResolvable(messagesKeys, messagesKeys[0]));
    }

    /**
     * Establece el identado aplicado al mensaje de la excepción. El valor por defecto es 2.
     * 
     * @param identLength
     *            el identado aplicado al mensaje de la excepción.
     */
    @Override
    public void setIdentLength(int identLength) {

        this.identLength = identLength;
    }

    /**
     * Establece el código del mensaje a mostrar. Si se establece no se muestra el mensaje obtenido dinámicamente a
     * partir del <code>throwable</code>. Los mensajes a obtener son <em>messageKey.caption</em> y
     * <em>messageKey.description</em>.
     * 
     * @param messagesKey
     *            el código del mensaje a mostrar.
     */
    @Override
    public void setMessagesKey(String messagesKey) {

        this.messagesKey = messagesKey;
    }

    /**
     * Establece la longitud del mensaje de la excepción. El valor por defecto es 120.
     * 
     * @param wrapLength
     *            la longitud del mensaje a mostrar en la excepción.
     */
    @Override
    public void setWrapLength(int wrapLength) {

        this.wrapLength = wrapLength;
    }

    /**
     * Formatea el mensaje a mostrar.
     * 
     * @param message
     *            el mensaje a mostrar.
     * @return el mensaje formateado.
     */
    @Override
    protected String formatMessage(String message) {

        if (message == null) {
            return "";
        }
        final String identString = StringUtils.leftPad("", this.identLength);
        final String newLineWithIdentString = "\n" + identString;
        final StringBuilder formattedMessageBuilder = new StringBuilder(identString);
        final StringTokenizer messageTokenizer = new StringTokenizer(message, "\n");
        while (messageTokenizer.hasMoreTokens()) {
            final String messageToken = messageTokenizer.nextToken();
            formattedMessageBuilder.append(WordUtils.wrap(messageToken, this.wrapLength, newLineWithIdentString, true));
            if (messageTokenizer.hasMoreTokens()) {
                formattedMessageBuilder.append(newLineWithIdentString);
            }
        }
        return formattedMessageBuilder.toString();
    }

    /**
     * Obtiene los códigos de los mensajes de la excepción producida y de sus clases padre. Si la excepción es de tipo
     * <code>ErrorCoded</code> se obtiene el <code>errorCode</code> de la causa de la excepción.
     * 
     * @param throwable
     *            la excepción producida.
     * @param keySuffix
     *            el sufijo que se añade al código del mensaje.
     * @return un array de cadenas con los códigos de los mensajes de la excepción producida y de sus clases padre.
     */
    protected String[] getMessagesKeys(Throwable throwable, String keySuffix) {

        if (this.messagesKey != null) {
            return new String[] { this.messagesKey };
        }
        final List<String> messageKeyList = new ArrayList<String>();

        if (throwable instanceof ErrorCoded) {
            final ErrorCoded errorCoded = (ErrorCoded) throwable;
            messageKeyList.add(errorCoded.getErrorCode() + keySuffix);
        }

        Class<?> clazz = throwable.getClass();
        while (clazz != Object.class) {
            messageKeyList.add(clazz.getName() + keySuffix);
            clazz = clazz.getSuperclass();
        }
        return messageKeyList.toArray(new String[messageKeyList.size()]);
    }
}
