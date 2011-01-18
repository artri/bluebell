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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.velocity.app.VelocityEngine;
import org.bluebell.richclient.application.ApplicationPageConfigurer;
import org.bluebell.richclient.application.ApplicationPageException;
import org.bluebell.richclient.application.support.ApplicationUtils;
import org.bluebell.richclient.application.support.DefaultApplicationPageConfigurer;
import org.bluebell.richclient.application.support.DefaultApplicationPageConfigurer.BbViewType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.docking.vldocking.VLDockingApplicationPage;
import org.springframework.richclient.application.docking.vldocking.VLDockingPageDescriptor;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.util.Assert;
import org.xml.sax.SAXException;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingContext;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * VLDocking application page implementation with support for saving user layouts.
 * <p>
 * Distinguishes between the following (priority ordered) layouts:
 * <ol>
 * <li><b>User layout</b>: the last -user specific- remembered layout.
 * <li><b>Explicit initial layout</b>: a layout specified by the user within the page descriptor.
 * <li><b>Implicit initial layout</b>: a layout in a conventioned location.
 * <li><b>Auto layout</b>: an automatically build layout using Velocity engine.
 * </ol>
 * 
 * @param <T>
 *            the type of entities managed by this page.
 * 
 * @see VLDockingApplicationPage
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbVLDockingApplicationPage<T> extends VLDockingApplicationPage {

    /**
     * Message for page creation exceptions.
     */
    private static final MessageFormat PAGE_CREATION_FAILED_FMT = new MessageFormat(
            "Failed to build page with id \"{0}\"");

    /**
     * Message for page closing exception.
     */
    private static final MessageFormat PAGE_CLOSING_FAILED_FMT = new MessageFormat(
            "Failed to close page with id \"{0}\" using layout \"{1}\"");

    /**
     * Message for layout building exceptions.
     */
    private static final MessageFormat LAYOUT_BUILDING_FAILED_FMT = new MessageFormat(
            "Error reading workspace layout \"{0}\"");

    /**
     * The identifier of the page to be used when page descriptor identifier is null.
     */
    private static final String PAGE_ID_IF_NULL = "emptyPage";

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BbVLDockingApplicationPage.class);

    /**
     * The successfully build layout resource.
     */
    private Resource layout;

    /**
     * A message format with the user layout location template to be propagated to pages.
     */
    private MessageFormat userLayoutLocationFmt;

    /**
     * A message format with the initial layout location template to be propagated to pages.
     */
    private MessageFormat initialLayoutLocationFmt;

    /**
     * The velocity template to be merged when building auto layout.
     * <p>
     * Note in order to make this work <code>velocityEngine</code> should be configured and accessible. Pay attention to
     * property named <code>resourceLoaderPath</code> since velocity template should be the folder where this
     * <code>velocity template</code> is stored.
     * 
     * <pre>
     * {@code  
     *          <bean id="velocityEngine" class="org.springframework.ui.velocity.VelocityEngineFactoryBean"
     *                  p:preferFileSystemAccess="false" 
     *                  p:configLocation="${richclient.vmConfigLocation}"
     *                  p:resourceLoaderPath="${richclient.vmResourceLoaderPath}" />
     * }
     * </pre>
     * 
     * @since 20101223, due to <a href="http://jirabluebell.b2b2000.com/browse/BLUE-34">BLUE-34</a>
     */
    private Resource autoLayoutTemplate;

    /**
     * Creates the page given its window and page descriptor.
     * 
     * @param window
     *            the application window.
     * @param pageDescriptor
     *            the page descriptor.
     */
    public BbVLDockingApplicationPage(ApplicationWindow window, PageDescriptor pageDescriptor) {

        super(window, pageDescriptor);
    }

    /**
     * Gets the layout.
     * 
     * @return the layout.
     */
    public final Resource getLayout() {

        return this.layout;
    }

    /**
     * Builds a desktop layout given a list of candidate resources.
     * 
     * @param dockingDesktop
     *            the docking desktop.
     * @param layouts
     *            the list of candidate layouts to be used.
     * @param exceptions
     *            the exceptions registered from previous invocations.
     * 
     * @return <code>true</code> if success and <code>false</code> in other case.
     * 
     * @see #buildLayout(DockingDesktop, Resource, List)
     */
    public Boolean buildLayout(DockingDesktop dockingDesktop, List<Resource> layouts, List<Exception> exceptions) {

        Assert.notNull(layouts, "layouts");

        Boolean layoutSuccess = Boolean.FALSE;

        final Iterator<Resource> itr = layouts.iterator();
        while (itr.hasNext() && !layoutSuccess) {

            final Resource tryWithLayout = itr.next();
            if ((tryWithLayout == null) || tryWithLayout.exists()) {
                layoutSuccess = this.buildLayout(dockingDesktop, tryWithLayout, exceptions);
            }
        }

        return layoutSuccess;
    }

    /**
     * Builds a desktop layout given the appropiate resource.
     * <p />
     * If an exception is raised then it's wrapped and rethrown using an <code>ApplicationPageException</code>.
     * 
     * @param dockingDesktop
     *            the docking desktop.
     * @param layout
     *            the layout to be used.
     * @param exceptions
     *            the exceptions registered from previous invocations.
     * 
     * @return <code>true</code> if success and <code>false</code> in other case.
     * 
     * @see #closeUndeclaredPageComponents()
     * 
     */
    public Boolean buildLayout(DockingDesktop dockingDesktop, Resource layout, List<Exception> exceptions) {

        Assert.notNull(dockingDesktop, "dockingDesktop");
        Assert.notNull(exceptions, "exceptions");

        // (JAF), 20101205, close undeclared page components
        this.closeUndeclaredPageComponents();

        if (layout == null) {

            this.getPageDescriptor().buildInitialLayout(this);

            this.setLayout(null);

            return Boolean.TRUE;
        }

        try {
            // (JAF), 20100411, deprecated, resource may be a ByteArrayResource
            // layout.getFile(); // Force an exception if file doesn't exist

            // Read layout file
            final InputStream in = layout.getInputStream();
            dockingDesktop.getContext().readXML(in);
            in.close();

            // Register the employed layout (useful for testing purposes)
            this.setLayout(layout);

        } catch (IOException e) {
            final String msg = BbVLDockingApplicationPage.LAYOUT_BUILDING_FAILED_FMT.format(new Object[] { layout });
            exceptions.add(new ApplicationPageException(msg, e, this.getId()));

            return Boolean.FALSE;

        } catch (SAXException e) {
            final String msg = BbVLDockingApplicationPage.LAYOUT_BUILDING_FAILED_FMT.format(new Object[] { layout });
            exceptions.add(new ApplicationPageException(msg, e, this.getId()));

            return Boolean.FALSE;

        } catch (ParserConfigurationException e) {
            final String msg = BbVLDockingApplicationPage.LAYOUT_BUILDING_FAILED_FMT.format(new Object[] { layout });
            exceptions.add(new ApplicationPageException(msg, e, this.getId()));

            return Boolean.FALSE;

        } finally {
            if (ArrayUtils.isEmpty(dockingDesktop.getDockables())) {
                this.getPageDescriptor().buildInitialLayout(this);
            }
        }

        return Boolean.TRUE;
    }

    /**
     * Saves a desktop layout given the appropiate resource destination.
     * <p />
     * If an exception is raised then its envolved and rethrown using an <code>ApplicationPageException</code>.
     * 
     * @param dockingDesktop
     *            the docking desktop.
     * @param dest
     *            the destination resource.
     */
    public void saveLayout(DockingDesktop dockingDesktop, Resource dest) {

        Assert.notNull(dockingDesktop, "dockingDesktop");
        Assert.notNull(dest, "dest");

        try {
            // Create file if doesn't exist
            final File userLayoutFile = dest.getFile();
            if (!dest.exists()) {
                FileUtils.touch(userLayoutFile);
            }

            // Write XML
            final OutputStream out = new FileOutputStream(userLayoutFile);
            dockingDesktop.getContext().writeXML(out);
            out.close();
        } catch (final IOException e) {
            throw new ApplicationPageException("Error writing workspace layout \"" + dest + "\"", e, this.getId());
        }
    }

    /**
     * Resets the page layout saving actual state and restoring the initial one.
     * <p>
     * <b>Note</b> when there is no initial layout then auto layout must be remembered from the beginning (since
     * re-calculating it may don't take into account all the page components).
     * 
     * @see #saveLayout(DockingDesktop, Resource)
     * @see #buildLayout(DockingDesktop, List, List)
     */
    public void resetLayout() {

        Assert.state(this.isControlCreated(), "this.isControlCreated() should be true");

        final List<Exception> exceptions = new ArrayList<Exception>();

        final DockingDesktop dockingDesktop = (DockingDesktop) this.getControl();

        // 1.Save current layout
        // if (this.getUserLayout() != null) {
        // this.saveLayout(dockingDesktop, this.getUserLayout());
        // }

        this.close();
        final List<String> pageComponentDescriptors = ApplicationUtils.getDeclaredPageComponentDescriptors(this);
        for (String pageComponentDescriptor : pageComponentDescriptors) {
            this.showView(pageComponentDescriptor);
        }

        // 2.Restore layout
        final List<Resource> layouts = Arrays.asList(//
                new Resource[] { this.getInitialLayout(), this.getAutoLayout(), null });

        final Boolean layoutSuccess = this.buildLayout(dockingDesktop, layouts, exceptions);
        if (!layoutSuccess && !exceptions.isEmpty()) {
            throw new ApplicationPageException(exceptions.get(0), this.getId());
        }

        if (layoutSuccess) {
            // (JAF), 20101206, force a "pageOpened" event (i.e. for page ApplicationConfigAspect to be invoked)
            this.getActiveWindow().showPage(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {

        final String id = super.getId();

        return (id != null) ? id : BbVLDockingApplicationPage.PAGE_ID_IF_NULL;
    }

    /**
     * Sets the user layout message format.
     * 
     * @param userLayoutLocationFmt
     *            the user layout message format to set.
     * 
     * @return <code>this</code>.
     */
    public final BbVLDockingApplicationPage<T> setUserLayoutLocationFmt(MessageFormat userLayoutLocationFmt) {

        Assert.notNull(userLayoutLocationFmt, "userLayoutLocationFmt");

        this.userLayoutLocationFmt = userLayoutLocationFmt;

        return this;
    }

    /**
     * Sets the initial layout location template.
     * 
     * @param initialLayoutLocationFmt
     *            the initial layout location template.
     * 
     * @return <code>this</code>.
     */
    public final BbVLDockingApplicationPage<T> setInitialLayoutLocationFmt(MessageFormat initialLayoutLocationFmt) {

        Assert.notNull(initialLayoutLocationFmt, "initialLayoutLocationFmt");

        this.initialLayoutLocationFmt = initialLayoutLocationFmt;

        return this;
    }

    /**
     * Sets the velocity template.
     * 
     * @param autoLayoutTemplate
     *            the velocity template to set.
     * 
     * @see #autoLayoutTemplate
     * 
     * @return <code>this</code>.
     */
    public final BbVLDockingApplicationPage<T> setAutoLayoutTemplate(Resource autoLayoutTemplate) {

        Assert.notNull(autoLayoutTemplate, "autoLayoutTemplate");
        Assert.isTrue(autoLayoutTemplate.exists(), "autoLayoutTemplate.exists()");

        this.autoLayoutTemplate = autoLayoutTemplate;

        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see #doClose()
     */
    @Override
    public boolean close() {

        return this.doClose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", this.getId()).toString();
    }

    /**
     * Creates the page control. Later tries to build the layout in the following order:
     * <ol>
     * <li>{@link #getUserLayout()}
     * <li>{@link #getInitialLayout()}
     * <li>{@link #getAutoLayout()}
     * <li><code>null</code>
     * </ol>
     * 
     * @return the page control.
     * 
     * @see VLDockingApplicationPage#createControl
     * @see #buildLayout(DockingDesktop, List, List)
     */
    @Override
    protected JComponent createControl() {

        final List<Exception> exceptions = new ArrayList<Exception>();

        // 1.Create the page control (AKA docking desktop)
        final DockingDesktop dockingDesktop = this.doCreateControl(exceptions);
        if (dockingDesktop != null) {

            // 2.Build page layouts
            final List<Resource> layouts = Arrays.asList(new Resource[] {//
                    this.getUserLayout(), this.getInitialLayout(), this.getAutoLayout(), null });

            final Boolean layoutSuccess = this.buildLayout(dockingDesktop, layouts, exceptions);
            if (!layoutSuccess && !exceptions.isEmpty()) {
                throw new ApplicationPageException(exceptions.get(0), this.getId());
            }

        } else if (!exceptions.isEmpty()) {
            throw new ApplicationPageException(exceptions.get(0), this.getId());
        }

        return dockingDesktop;
    }

    /**
     * Gets the user layout.
     * <p>
     * Never returns <code>null</code> but note returned resource may not exist.
     * 
     * @return the user layout.
     */
    protected final Resource getUserLayout() {

        final String pageId = this.getPageDescriptor().getId();
        final String userLayoutLocation = this.getUserLayoutLocationFmt().format(new String[] { pageId });

        final Resource userLayout = new FileSystemResource(userLayoutLocation);

        return userLayout;
    }

    /**
     * Gets the initial layout.
     * <p>
     * Never returns <code>null</code> but note returned resource may not exist.
     * 
     * @return the initial layout.
     */
    protected final Resource getInitialLayout() {

        Resource initialLayout = null;

        if (this.getPageDescriptor() instanceof VLDockingPageDescriptor) {

            initialLayout = ((VLDockingPageDescriptor) this.getPageDescriptor()).getInitialLayout();
        }

        // If initial layout keeps being null
        if (initialLayout == null) {
            final String pageId = this.getPageDescriptor().getId();
            final String path = this.getInitialLayoutLocationFmt().format(new String[] { pageId });

            initialLayout = new ClassPathResource(path);
        }

        return initialLayout;
    }

    /**
     * Gets the auto layout.
     * <p>
     * Never returns <code>null</code> but note returned resource may be useless (i.e.: velocity engine fails).
     * 
     * @return the auto layout.
     */
    protected final Resource getAutoLayout() {

        Assert.notNull(this.getAutoLayoutTemplate(), "this.getVelocityTemplate()");

        // No me gusta esta forma de obtener el contexto
        final VelocityEngine vm = this.getApplicationContext().getBean("velocityEngine", VelocityEngine.class);

        // TODO, (JAF), 20100408, applicationPageConfigurer is not compulsory
        final ApplicationPageConfigurer<?> pageConfigurer = (ApplicationPageConfigurer<?>) this.getService(//
                ApplicationPageConfigurer.class);
        final Map<String, List<? extends PageComponent>> classification = pageConfigurer.classifyApplicationPage(this);

        /*
         * Trait unknown views as master views: *This code should be moved to the template*
         */
        final List<PageComponent> newMasterViews = new ArrayList<PageComponent>();
        newMasterViews.addAll(classification.get(DefaultApplicationPageConfigurer.BbViewType.MASTER_TYPE.name()));
        newMasterViews.addAll(classification.get(DefaultApplicationPageConfigurer.BbViewType.UNKNOWN_TYPE.name()));
        classification.put(DefaultApplicationPageConfigurer.BbViewType.MASTER_TYPE.name(), newMasterViews);

        // Merge context
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put("classification", classification);
        context.put(BbViewType.MASTER_TYPE.name(), BbViewType.MASTER_TYPE.name());
        context.put(BbViewType.CHILD_TYPE.name(), BbViewType.CHILD_TYPE.name());
        context.put(BbViewType.SEARCH_TYPE.name(), BbViewType.SEARCH_TYPE.name());
        context.put(BbViewType.VALIDATION_TYPE.name(), BbViewType.VALIDATION_TYPE.name());
        context.put(BbViewType.UNKNOWN_TYPE.name(), BbViewType.UNKNOWN_TYPE.name());

        Resource resource;
        try {
            final String templateLocation = this.getAutoLayoutTemplate().getFilename();
            final String string = VelocityEngineUtils.mergeTemplateIntoString(vm, templateLocation, context);

            resource = new ByteArrayResource(string.getBytes());
        } catch (Throwable e) {
            // (JAF), 20101224, this resource is useless but doesn't break the contract!
            // VLDocking will fail to build this layout: never mind, handlers on this class will treat the exception
            resource = new ByteArrayResource(new byte[] {});
        }

        return resource;
    }

    /**
     * Gets the user layout message format.
     * 
     * @return the user layout message format. Never returns <code>null</code>.
     */
    protected final MessageFormat getUserLayoutLocationFmt() {

        return this.userLayoutLocationFmt;
    }

    /**
     * Gets the initial layout message format.
     * 
     * @return the initial layout message format. Never returns <code>null</code>.
     */
    protected final MessageFormat getInitialLayoutLocationFmt() {

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
     * Close existing page components non declared on page descriptor registry.
     */
    private void closeUndeclaredPageComponents() {

        final List<String> declaredDescriptors = ApplicationUtils.getDeclaredPageComponentDescriptors(this);

        // (JAF), 20101205, close every "non declared" page component after proceed (i.e.: clearing active component)
        final List<PageComponent> pageComponents = new ArrayList<PageComponent>(this.getPageComponents());
        for (PageComponent pageComponent : pageComponents) {
            if (!declaredDescriptors.contains(pageComponent.getId())) {
                this.close(pageComponent);
            }
        }
    }

    /**
     * Creates the page control without layout building.
     * 
     * @param exceptions
     *            the exceptions registered from previous invocations.
     * 
     * @return the page control.
     */
    @SuppressWarnings("unchecked")
    private DockingDesktop doCreateControl(List<Exception> exceptions) {

        Assert.notNull(exceptions, "exceptions");

        DockingDesktop control = null;

        // Recover from previous failures
        final DockingContext dockingContext = this.getDockingContext();
        final List<DockingDesktop> dockingDesktops = new ArrayList<DockingDesktop>(dockingContext.getDesktopList());
        for (final DockingDesktop oldDockingDesktop : dockingDesktops) {
            dockingContext.removeDesktop(oldDockingDesktop);
        }

        // 20091223, HACK, set temporally a null initial layout before calling super and restore the value later.
        // This will avoid reading the same layout resource twice.
        final Resource previousInitialLayout = this.getInitialLayout();
        super.setInitialLayout(null);
        try {
            // (JAF), 20100411, super.createControl() does not trigger layout building since initial layout is
            // intentionally set to null. Instead an explicit layout is specified throw #buildLayout
            control = (DockingDesktop) super.createControl();
        } catch (Exception e) {
            final String pageId = this.getPageDescriptor().getId();
            final String message = BbVLDockingApplicationPage.PAGE_CREATION_FAILED_FMT.format(new String[] { pageId });

            BbVLDockingApplicationPage.LOGGER.error(message, e);

            // (JAF), 20090720, remember exception to be handled later
            exceptions.add(e);
        } finally {
            this.setInitialLayout(previousInitialLayout);
        }

        return control;
    }

    /**
     * Saves this page layout and close it respectively.
     * 
     * @return <code>true</code> if success and <code>false</code> in any other case.
     */
    private Boolean doClose() {

        // 20090913, (JAF), prevents attemps to close a non existing control
        // (i.e.: on exit command execution after page creation exception)
        if (!this.isControlCreated()) {
            return Boolean.TRUE;
        }

        // HACK, set temporally a null initial layout before calling super#close and restore the value later.
        // This will avoid writing the layout resource twice.
        super.setInitialLayout(null);

        final Resource theUserLayout = this.getUserLayout();
        Boolean success = null;
        try {

            final DockingDesktop dockingDesktop = (DockingDesktop) this.getControl();

            if (theUserLayout != null) {
                this.saveLayout(dockingDesktop, theUserLayout);
            }

            success = super.close();

            /*
             * (JAF), 20110118, dockables must be unregistered. Otherwise closing individual dockables or resetting a
             * perspective may not work as expected.
             * 
             * @see http://jirabluebell.b2b2000.com/browse/BLUE-63
             */
            if (success) {
                for (DockableState dockableState : dockingDesktop.getDockables()) {
                    final Dockable dockable = dockableState.getDockable();

                    // (JAF), 20110118, this line avoids a NPE when dockable state is null at invoked close method
                    dockingDesktop.getContext().setDockableState(dockable, dockableState);

                    // Unregister dockable to and allow GC free references
                    dockingDesktop.unregisterDockable(dockableState.getDockable());
                }
            }
        } catch (Exception e) {
            final String description = (theUserLayout != null) ? theUserLayout.getDescription() : StringUtils.EMPTY;
            final String message = BbVLDockingApplicationPage.PAGE_CLOSING_FAILED_FMT.format(//
                    new String[] { this.getPageDescriptor().getId(), description });

            BbVLDockingApplicationPage.LOGGER.error(message, e);
        } finally {
            this.setInitialLayout(this.getInitialLayout());
        }

        return success;
    }

    /**
     * Sets the layout.
     * 
     * @param layout
     *            the layout to set.
     */
    private void setLayout(Resource layout) {

        // May be null
        // Assert.notNull(layout, "layout");

        this.layout = layout;
    }
}

// /**
// * Modifica el comportamiento de
// * {@link
// org.springframework.richclient.application.support.AbstractApplicationPage#createPageComponent
// * con el objetivo de cachear los componentes de página creados en función del
// descriptor.
// *
// * @param descriptor
// * el descriptor.
// * @return el componente de página.
// *
// * @see
// org.springframework.richclient.application.support.AbstractApplicationPage#createPageComponent
// */
// @Override
// protected PageComponent createPageComponent(PageComponentDescriptor
// descriptor) {
//
// // Obtener el componente de la caché
// PageComponent pageComponent = this.cachedPageComponents.get(descriptor);
//
// // Si el componente es nulo crearlo y registrarlo
// if (pageComponent == null) {
// // TODO, ¿por qué es necesario esto, revisarlo, documentarlo y notificarlo?
// pageComponent = super.createPageComponent(descriptor);
// this.cachedPageComponents.put(descriptor, pageComponent);
// }
//
// return pageComponent;
// }
// TODO, ¿por qué es necesario esto, revisarlo, documentarlo y notificarlo?

//
// /**
// * Sobreescribe {@link VLDockingApplicationPage#doAddPageComponent} para que:
// * <ol>
// * <li>Sincronice los componentes de la página.
// * <li>No añada el <em>dockable</em> al <em>desktop</em> si el dockable ya
// está creado. Este comportamiento es
// * erróneo ya que pudiera ser que la página esté cerrada.
// * </ol>
// *
// * @param pageComponent
// * el componente de la página.
// *
// * @see #configurePageComponent(FormBackedView)
// */
// @Override
// protected void doAddPageComponent(PageComponent pageComponent) {
//
// super.doAddPageComponent(pageComponent);
//
// // HACK, (JAF), 20080417, continue just only if dockable is different from
// null. In such a case super does not
// // call layout builder
// final Dockable dockable = this.getDockable(pageComponent);
// if (dockable != null) {
// this.getLayoutManager().addDockable(((DockingDesktop) this.getControl()),
// dockable);
// }
// }
