package org.bluebell.richclient.application.docking.vldocking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bluebell.richclient.application.ApplicationPageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.docking.vldocking.VLDockingApplicationPage;
import org.springframework.richclient.application.docking.vldocking.VLDockingPageDescriptor;
import org.springframework.util.Assert;
import org.xml.sax.SAXException;

import com.vlsolutions.swing.docking.DockingContext;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * VLDocking application page implementation with support for saving user layouts.
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
    private static final MessageFormat PAGE_BUILDING_FAILED_FMT = new MessageFormat(
            "Failed to build page with id \"{0}\" using layout \"{1}\"");

    /**
     * Message for page closing exception.
     */
    private static final MessageFormat PAGE_CLOSING_FAILED_FMT = new MessageFormat(
            "Failed to close page with id \"{0}\" using layout \"{1}\"");

    /**
     * Message format for building user layout locations.
     * <p>
     * Follows this convention:
     * 
     * <pre>
     * ${user_home}/.${application}/${filename}
     * </pre>
     * 
     * <dl>
     * <dt>user_home
     * <dd>The user home.
     * <dt>application
     * <dd>The application name.
     * <dt>filename
     * <dd>The filename.
     * </dl>
     */
    private static final MessageFormat USER_LAYOUT_LOCATION_FMT = new MessageFormat("{0}/.{1}/{2}");

    /**
     * The identifier of the page to be used when page descriptor identifier is null.
     */
    private static final String PAGE_ID_IF_NULL = "emptyPage";

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BbVLDockingApplicationPage.class);

    /**
     * The initial layout resource.
     */
    private Resource initialLayout;

    /**
     * The user layout resource.
     */
    private Resource userLayout;

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
     * Resets the page layout saving actual state and restoring the initial one.
     */
    public void resetLayout() {

        Assert.state(this.isControlCreated(), "this.isControlCreated() should be true");

        final DockingDesktop dockingDesktop = (DockingDesktop) this.getControl();

        this.saveLayout(dockingDesktop, this.getUserLayout());
        this.buildLayout(dockingDesktop, this.getInitialLayout());
    }

    /**
     * Builds a desktop layout given the appropiate resource configuration.
     * <p />
     * If an exception is raised then its envolved and rethrown using an <code>ApplicationPageException</code>.
     * 
     * @param dockingDesktop
     *            the docking desktop.
     * @param layout
     *            the layout to be used.
     */
    public void buildLayout(DockingDesktop dockingDesktop, Resource layout) {

        Assert.notNull(dockingDesktop, "dockingDesktop");

        if (layout == null) {
            this.getPageDescriptor().buildInitialLayout(this);
            return;
        }

        try {
            // Force an exception if file doesn't exist
            layout.getFile();

            // Read layout file
            final InputStream in = layout.getInputStream();
            dockingDesktop.getContext().readXML(in);
            in.close();

        } catch (IOException e) {
            throw new ApplicationPageException("Error reading workspace layout \"" + layout + "\"", e, this.getId());
        } catch (SAXException e) {
            throw new ApplicationPageException("Error reading workspace layout \"" + layout + "\"", e, this.getId());
        } catch (ParserConfigurationException e) {
            throw new ApplicationPageException("Error reading workspace layout \"" + layout + "\"", e, this.getId());
        } finally {
            if (ArrayUtils.isEmpty(dockingDesktop.getDockables())) {
                this.getPageDescriptor().buildInitialLayout(this);
            }
        }
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
     * {@inheritDoc}
     */
    @Override
    public String getId() {

        final String id = super.getId();

        return (id != null) ? id : BbVLDockingApplicationPage.PAGE_ID_IF_NULL;
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
     * Creates the control using three attempts.
     * <p />
     * It follows this order:
     * <ol>
     * <li>{@link #getLayout()}
     * <li>{@link #getInitialLayout()}
     * <li><code>null</code>
     * </ol>
     * 
     * @return the page control.
     * 
     * @see VLDockingApplicationPage#createControl
     */
    @Override
    protected JComponent createControl() {

        final List<Exception> errors = new ArrayList<Exception>();

        DockingDesktop dockingDesktop = this.doCreateControl(this.getLayout(), errors);
        if (dockingDesktop == null) {
            dockingDesktop = this.doCreateControl(this.getInitialLayout(), errors);
        }
        if (dockingDesktop == null) {
            dockingDesktop = this.doCreateControl(null, errors);
        }
        if (dockingDesktop == null) {
            if (!errors.isEmpty()) {
                throw new ApplicationPageException(errors.get(0), this.getId());
            }
        }

        return dockingDesktop;
    }

    /**
     * If user layout exists then returns it, else returns the initial layout.
     * 
     * @return the layout to be used, may be <code>null</code> if initial layout is not set.
     */
    protected Resource getLayout() {

        final Resource theUserLayout = this.getUserLayout();
        if ((theUserLayout != null) && theUserLayout.exists()) {
            return theUserLayout;
        }

        return this.getInitialLayout();
    }

    /**
     * Gets the initial layout.
     * 
     * @return the initial layout.
     */
    protected Resource getInitialLayout() {

        if ((this.initialLayout == null) && (this.getPageDescriptor() instanceof VLDockingPageDescriptor)) {
            this.initialLayout = ((VLDockingPageDescriptor) this.getPageDescriptor()).getInitialLayout();
        }

        return this.initialLayout;
    }

    /**
     * Gets the user layout.
     * 
     * @return the user layout.
     * 
     * @see #USER_LAYOUT_LOCATION_FMT
     */
    protected Resource getUserLayout() {

        if ((this.userLayout == null) && (this.getInitialLayout() != null)) {
            final String userHome = SystemUtils.getUserHome().getAbsolutePath();
            final String applicationName = this.getApplication().getName();
            String filename = StringUtils.EMPTY;
            if (this.getInitialLayout() != null) {
                filename = this.getInitialLayout().getFilename();
            }

            final String userLayoutLocation = BbVLDockingApplicationPage.USER_LAYOUT_LOCATION_FMT.format(//
                    new String[] { userHome, applicationName, filename });

            this.userLayout = new FileSystemResource(userLayoutLocation);
        }

        return this.userLayout;
    }

    /**
     * Creates the page control with the given layout.
     * 
     * @param layout
     *            the layout.
     * @param exceptions
     *            the exceptions registered from previous invocations.
     * 
     * @return the page control.
     */
    @SuppressWarnings("unchecked")
    private DockingDesktop doCreateControl(Resource layout, List<Exception> exceptions) {

        // Recover from previous failures
        final DockingContext dockingContext = this.getDockingContext();
        final List<DockingDesktop> dockingDesktops = new ArrayList<DockingDesktop>(dockingContext.getDesktopList());
        for (final DockingDesktop dockingDesktop : dockingDesktops) {
            dockingContext.removeDesktop(dockingDesktop);
        }

        // 20091223, HACK, set temporally a null initial layout before calling
        // super and restore the value later.
        // This will avoid reading the same layout twice.
        super.setInitialLayout(null);

        DockingDesktop dockingDesktop = null;
        try {
            dockingDesktop = (DockingDesktop) super.createControl();
            this.buildLayout(dockingDesktop, this.getLayout());
        } catch (Exception e) {

            final String resourceDescription = (layout != null) ? layout.getDescription() : StringUtils.EMPTY;
            final String message = BbVLDockingApplicationPage.PAGE_BUILDING_FAILED_FMT.format(//
                    new String[] { this.getPageDescriptor().getId(), resourceDescription });

            BbVLDockingApplicationPage.LOGGER.error(message, e);

            // (JAF), 20090720, register exception to be handled later
            exceptions.add(e);
        } finally {
            this.setInitialLayout(this.getInitialLayout());
        }

        return dockingDesktop;
    }

    /**
     * Saves this page layout and close it respectively.
     * 
     * @return <code>true</code> if success and <code>false</code> in any other case.
     */
    private Boolean doClose() {

        // 20090913, (JAF), prevents attemps to close a non existing control
        // (i.e.: on exit command execution after page
        // creation exception)
        if (!this.isControlCreated()) {
            return Boolean.TRUE;
        }

        // HACK, set temporally a null initial layout before calling super#close
        // and restore the value later.
        // This will avoid writing the layout twice.
        super.setInitialLayout(null);

        final Resource layout = this.getUserLayout();
        Boolean success = null;
        try {
            this.saveLayout((DockingDesktop) this.getControl(), layout);
            success = super.close();
        } catch (Exception e) {
            final String resourceDescription = (layout != null) ? layout.getDescription() : StringUtils.EMPTY;
            final String message = BbVLDockingApplicationPage.PAGE_CLOSING_FAILED_FMT.format(//
                    new String[] { this.getPageDescriptor().getId(), resourceDescription });

            BbVLDockingApplicationPage.LOGGER.error(message, e);
        } finally {
            this.setInitialLayout(this.getInitialLayout());
        }

        return success;
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
