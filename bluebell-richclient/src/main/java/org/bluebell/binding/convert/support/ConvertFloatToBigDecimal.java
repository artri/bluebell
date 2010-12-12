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

package org.bluebell.binding.convert.support;

import java.math.BigDecimal;

import org.springframework.binding.convert.ConversionContext;
import org.springframework.binding.convert.support.AbstractFormattingConverter;
import org.springframework.binding.format.FormatterFactory;
import org.springframework.binding.format.support.SimpleFormatterFactory;

/**
 * Clase de utilidad para convertir un Float a BigDecimal.
 * 
 * Necesario para binding de componentes Spring RichClient, como por ejemplo:
 * {@link org.springframework.richclient.form.binding.swing.NumberBinder}
 * <p>
 * This is a typical use of this class within Spring Application Context:
 * 
 * <pre>
 * <!--
 *         Bean: conversionService
 *         Usage: platform optional
 *         Description: This specifies the component that will supply converters
 *         for property values. Since we are going to add a special formatter for date fields, we need to have a reference to this
 *         service in the context configured with a custom formatter factory.
 * -->
 * <bean id="defaultConversionService" class="org.springframework.richclient.application.DefaultConversionServiceFactoryBean" />
 * <bean class="org.springframework.beans.factory.config.MethodInvokingFactoryBean" p:target-object-ref="defaultConversionService"
 *         p:target-method="addConverters">
 *         <property name="arguments">
 *                 <list>
 *                         <list>
 *                                 <bean class="org.bluebell.binding.convert.support.ConvertSerializableToString" />
 *                                 <bean class="org.bluebell.binding.convert.support.ConvertStringToSerializable" />
 *                                 <bean class="org.bluebell.binding.convert.support.ConvertBigDecimalToFloat" />
 *                                 <bean class="org.bluebell.binding.convert.support.ConvertFloatToBigDecimal" />
 *                         </list>
 *                 </list>
 *         </property>
 * </bean>
 * </pre>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ConvertFloatToBigDecimal extends AbstractFormattingConverter {
    /**
     * Booleano para permitir vacios.
     */
    private final boolean allowEmpty;

    /**
     * Constructor por defecto.
     */
    public ConvertFloatToBigDecimal() {

        this(new SimpleFormatterFactory(), true);
    }

    /**
     * Constructor con parámetros.
     * 
     * @param formatterLocator
     *            El FormatterFactory.
     * @param allowEmpty
     *            El booleano para permitir vacíos.
     */
    protected ConvertFloatToBigDecimal(final FormatterFactory formatterLocator, final boolean allowEmpty) {

        super(formatterLocator);
        this.allowEmpty = allowEmpty;
    }

    /**
     * Clase origen.
     * 
     * @return El tipo de la clase origen.
     */
    @SuppressWarnings("rawtypes")
    public Class[] getSourceClasses() {

        return new Class[] { Float.class };
    }

    /**
     * Clase destino.
     * 
     * @return El tipo de la clase destino.
     */
    @SuppressWarnings("rawtypes")
    public Class[] getTargetClasses() {

        return new Class[] { BigDecimal.class };
    }

    /**
     * Metodo de conversión.
     * 
     * @param sourceINYOURCLASS
     *            Objeto Origen.
     * @param targetClass
     *            Objeto Destino.
     * @param context
     *            El ConversionContext
     * @return Objeto convertido
     * @throws Exception
     *             Excepción provocada.
     */
    @Override
    protected Object doConvert(final Object sourceINYOURCLASS, @SuppressWarnings("rawtypes") final Class targetClass,
            final ConversionContext context) throws Exception {

        return (!this.allowEmpty || (sourceINYOURCLASS != null)) ? ((Float) sourceINYOURCLASS).doubleValue() : null;
    }
}
