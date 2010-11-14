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

package org.bluebell.richclient.command.support;

import javax.swing.JComponent;

import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.application.support.ApplicationServicesAccessor;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.config.CommandConfigurer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Clase de utilidad para la gestión de comandos.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class CommandUtils extends ApplicationServicesAccessor {

    /**
     * Una cadena con una coma ({@value COMMA}).
     */
    private static final String COMMA = ",";

    /**
     * El sufijo de los identificadores de los comandos.
     */
    private static final String COMMAND_SUFIX = "Command";

    /**
     * Una cadena con una punto ({@value DOT}).
     */
    private static final String DOT = ".";

    /**
     * Configura el comando dado previo establecimiento de un identificador del controlador de la seguridad.
     * 
     * @param command
     *            el comando.
     * @param formModel
     *            el modelo del formulario al que pertenece el comando.
     * @return el comando pasado como parámetro una vez configurado.
     * 
     * @see #configureCommand(ActionCommand, ValidatingFormModel, boolean)
     */
    public static ActionCommand configureCommand(ActionCommand command, ValidatingFormModel formModel) {

        return CommandUtils.configureCommand(command, formModel, Boolean.FALSE);
    }

    /**
     * Configura el comando dado previo establecimiento de un identificador del controlador de la seguridad.
     * <p>
     * Además le añade opcionalmente el interceptor {@link BusyIndicatorActionCommandInterceptor}.
     * 
     * @param command
     *            el comando.
     * @param formModel
     *            el modelo del formulario al que pertenece el comando.
     * @param busyIndicated
     *            indica si se ha de habilitar el indicador de ocupación.
     * @return el comando pasado como parámetro una vez configurado.
     * 
     * @see #constructSecurityControllerId(String)
     * @see org.springframework.richclient.command.config.CommandConfigurer
     * @see BusyIndicatorActionCommandInterceptor
     */
    public static ActionCommand configureCommand(ActionCommand command, ValidatingFormModel formModel,
            Boolean busyIndicated) {

        Assert.notNull(command);
        Assert.notNull(busyIndicated);

        // Securizar el comando
        final String scid = CommandUtils.constructSecurityControllerId(//
                command.getId(), formModel);
        command.setSecurityControllerId(scid);

        // Configurar el comando
        final CommandConfigurer commandConfigurer = (CommandConfigurer) //
        ApplicationServicesLocator.services().getService(CommandConfigurer.class);

        // Añade al comando un interceptor indicador de ocupación
        if (busyIndicated) {
            command.addCommandInterceptor(//
                    BusyIndicatorActionCommandInterceptor.getInstance());
        }

        return (ActionCommand) commandConfigurer.configure(command);
    }

    /**
     * Configura el comando dado previo establecimiento de un identificador del controlador de la seguridad.
     * <p>
     * Además le añade opcionalmente el interceptor {@link BusyIndicatorActionCommandInterceptor} para el componente
     * especificado.
     * 
     * @param command
     *            el comando.
     * @param formModel
     *            el modelo del formulario al que pertenece el comando.
     * @param targetComponent
     *            el componente en el que mostrar el indicador de ocupación.
     * @return el comando pasado como parámetro una vez configurado.
     * 
     * @see #configureCommand(ActionCommand, ValidatingFormModel, Boolean)
     */
    public static ActionCommand configureCommand(ActionCommand command, ValidatingFormModel formModel,
            JComponent targetComponent) {

        Assert.notNull(targetComponent);

        final ActionCommand configuredCommand = //
        CommandUtils.configureCommand(command, formModel, Boolean.FALSE);

        // Añade al comando un interceptor indicador de ocupación
        configuredCommand.addCommandInterceptor(//
                BusyIndicatorActionCommandInterceptor.getInstance(//
                        targetComponent));

        return configuredCommand;
    }

    /**
     * Construye un identificador del controlador de seguridad para un comando dados su identificador y el modelo del
     * formulario al que pertenece.
     * <p>
     * Parsea en primer lugar el identificador pasado como parámetro, por si se tratase de una secuencia de
     * identificadores separados por coma, para utilizar el más prioritario.
     * <p>
     * El valor devuelto será:
     * <ul>
     * <li><code>[formModel.id] + "." + [priorCommandFaceId]</code> si el el modelo del formulario no es nulo.
     * <li><code>[commandFaceId]</code> si el modelo es nulo.
     * </ul>
     * 
     * @param commandFaceId
     *            el identificador original (o secuencia de identificadores separados por coma, donde el primero es el
     *            más prioritario).
     * @param formModel
     *            el modelo del formulario al que pertenece el comando.
     * @return el identificador del controlador de seguridad para el identificador de comando dado.
     */
    public static String constructSecurityControllerId(String commandFaceId, ValidatingFormModel formModel) {

        Assert.notNull(commandFaceId);

        String id = null;

        final String[] ids = StringUtils.commaDelimitedListToStringArray(commandFaceId);
        final String formModelId = formModel.getId();

        if (ids[0] != null) {
            id = (formModelId != null) ? //
            formModelId + CommandUtils.DOT + ids[0]
                    : //
                    ids[0];
        }

        return id;
    }

    /**
     * Obtiene el <em>face descriptor id</em> para un comando a partir del identificador por defecto, de la forma
     * "xxxCommand".
     * <p>
     * El nombre resultante es "xxxYyyCommand,xxxCommand" donde "yyy" es la cadena intermedia pasado por parámetro.
     * 
     * @param defaultCommandFaceDescriptorId
     *            el identificador por defecto.
     * @param innerString
     *            la cadena intermedia.
     * @return el nuevo valor del identificador.
     */
    public static String getCommandFaceDescriptorId(String defaultCommandFaceDescriptorId, String innerString) {

        Assert.notNull(defaultCommandFaceDescriptorId);
        Assert.notNull(innerString);

        final String capitalizedInnerString = StringUtils.capitalize(innerString);
        final String specializedCommandName = StringUtils.replace(defaultCommandFaceDescriptorId,
                CommandUtils.COMMAND_SUFIX, //
                capitalizedInnerString + CommandUtils.COMMAND_SUFIX);

        return specializedCommandName + CommandUtils.COMMA + defaultCommandFaceDescriptorId;
    }
}
