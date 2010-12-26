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

import java.text.MessageFormat;

import org.apache.commons.lang.SystemUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.docking.vldocking.VLDockingApplicationPage;
import org.springframework.richclient.application.docking.vldocking.VLDockingApplicationPageFactory;
import org.springframework.util.Assert;

/**
 * Factoría para la creación de páginas de aplicación que extiende el comportamiento de
 * {@link VLDockingApplicationPageFactory} con el objetivo de:
 * <ul>
 * <li>Evitar que se añadan <em>listeners</em> redundantes.
 * <li>Instanciar páginas detipo {@link BbVLDockingApplicationPage}.
 * <li>Corregir defecto por el que la ventana no se refresca correctamente al cambiar de página.
 * </ul>
 * 
 * @see VLDockingApplicationPageFactory
 * @see BbVLDockingApplicationPage
 * 
 * @param <T>
 *            el tipo de las entidades editadas por las páginas que crea la factoría.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbVLDockingApplicationPageFactory<T> extends VLDockingApplicationPageFactory implements InitializingBean {

    /**
     * A message format with the user layout location template to be propagated to pages.
     */
    private MessageFormat userLayoutLocationFmt;

    /**
     * A message format with the initial layout location template to be propagated to pages.
     */
    private MessageFormat initialLayoutLocationFmt;

    /**
     * The velocity template required by {@link BbVLDockingApplicationPage}.
     * 
     * @see BbVLDockingApplicationPage#setVelocityTemplate(Resource).
     */
    private Resource autoLayoutTemplate;

    /**
     * Crea la página, que a diferencia de
     * {@link VLDockingApplicationPage#createApplicationPage(ApplicationWindow,PageDescriptor)} es de tipo
     * {@link BbVLDockingApplicationPage}.
     * <p>
     * <b>Siempre</b> cachea las páginas creadas.
     * 
     * @param window
     *            la ventana.
     * @param descriptor
     *            el descriptor de página.
     * @return la página.
     */
    @Override
    public ApplicationPage createApplicationPage(ApplicationWindow window, PageDescriptor descriptor) {

        VLDockingApplicationPage page = this.findPage(window, descriptor);
        if (page == null) {
            page = new BbVLDockingApplicationPage<T>(window, descriptor)//
                    .setUserLayoutLocationFmt(this.getUserLayoutLocationFmt()) //
                    .setInitialLayoutLocationFmt(this.getInitialLayoutLocationFmt()) //
                    .setAutoLayoutTemplate(this.getAutoLayoutTemplate());

            this.cachePage(page);
        }

        return page;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Assert.notNull(this.getAutoLayoutTemplate(), "this.getVelocityTemplate()");
        Assert.notNull(this.getInitialLayoutLocationFmt(), "this.getInitialLayoutFmt()");
        Assert.notNull(this.getUserLayoutLocationFmt(), "this.getUserLayoutFmt()");
    }

    /**
     * Sets the user layout location pattern.
     * 
     * @param userLayoutLocation
     *            the user layout location to set.
     */
    public final void setUserLayoutLocation(String userLayoutLocation) {

        Assert.notNull(userLayoutLocation, "userLayoutLocation");

        this.userLayoutLocationFmt = BbVLDockingApplicationPageFactory.patternToMessageFormat(userLayoutLocation);
    }

    /**
     * Sets the initial layout location pattern.
     * 
     * @param initialLayoutLocation
     *            the initial layout location to set.
     */
    public final void setInitialLayoutLocation(String initialLayoutLocation) {

        Assert.notNull(initialLayoutLocation, "initialLayoutLocation");

        this.initialLayoutLocationFmt = BbVLDockingApplicationPageFactory.patternToMessageFormat(initialLayoutLocation);
    }

    /**
     * Sets the velocity template.
     * 
     * @param autoLayoutTemplate
     *            the velocity template to set.
     */
    public final void setAutoLayoutTemplate(Resource autoLayoutTemplate) {

        Assert.notNull(autoLayoutTemplate, "autoLayoutTemplate");

        this.autoLayoutTemplate = autoLayoutTemplate;
    }

    /**
     * Gets the user layout message format.
     * <p>
     * Never returns <code>null</code>. Default value is:
     * <blockquote>${user.home}/.${richclient.displayName}/vldocking/{0}.xml</blockquote>, with the following
     * convention:
     * 
     * <dl>
     * <dt>{0}
     * <dd>The page descriptor identifier.
     * </dl>
     * 
     * @return the user layout message format.
     */
    protected final MessageFormat getUserLayoutLocationFmt() {

        if (this.userLayoutLocationFmt == null) {

            // "${user.home}/.${richclient.displayName}/vldocking/{2}.xml"
            final StringBuffer sb = new StringBuffer() //
                    .append(SystemUtils.getUserHome())//
                    .append("/.")//
                    .append(Application.instance().getName())//
                    .append("/vldocking/{0}.xml");

            final String userLayoutLocation = sb.toString().replaceAll("\\Q${\\E(.*?)}", "$1");

            this.setUserLayoutLocation(userLayoutLocation);
        }

        return this.userLayoutLocationFmt;
    }

    /**
     * Gets the initial layout message format.
     * <p>
     * Never returns <code>null</code>. Default value is: <blockquote>/META-INF/vldocking/{0}.xml</blockquote>, with the
     * following convention:
     * <dl>
     * <dt>{0}
     * <dd>The page descriptor identifier.
     * </dl>
     * 
     * @return the initial layout message format.
     */
    protected final MessageFormat getInitialLayoutLocationFmt() {

        if (this.initialLayoutLocationFmt == null) {
            this.setInitialLayoutLocation("/META-INF/vldocking/{0}.xml");
        }

        return this.initialLayoutLocationFmt;
    }

    /**
     * Gets the velocity template.
     * 
     * @return the velocity template.
     */
    protected final Resource getAutoLayoutTemplate() {
    
        return this.autoLayoutTemplate;
    }

    /**
     * Transforms placeholder like expressions into the associated text (i.e.: ${key} --> key).
     * <p>
     * Otherwise message format creation will not be triggered
     * 
     * @param pattern
     *            the pattern.
     * @return a message format having escaped invalid expressions.
     */
    private static MessageFormat patternToMessageFormat(String pattern) {
    
        Assert.notNull(pattern, "pattern");
    
        final MessageFormat messageFormat = new MessageFormat(pattern.replaceAll("\\Q${\\E(.*?)}", "$1"));
    
        return messageFormat;
    }
}
