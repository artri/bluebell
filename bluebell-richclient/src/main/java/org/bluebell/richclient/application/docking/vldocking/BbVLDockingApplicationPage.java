package org.bluebell.richclient.application.docking.vldocking;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.docking.vldocking.VLDockingApplicationPage;
import org.springframework.richclient.application.docking.vldocking.VLDockingPageDescriptor;

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
     * Message for page creation errors.
     */
    private static final MessageFormat PAGE_CREATION_FAILED_FMT = new MessageFormat(
	    "Failed to create page with id \"{0}\" using layout \"{1}\"");

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
     * The logger.
     */
    private static final Log LOGGER = LogFactory.getLog(BbVLDockingApplicationPage.class);

    /**
     * The identifier of the page to be used when page descriptor identifier is null.
     */
    private static final String PAGE_ID_IF_NULL = "emptyPage";

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
     * Closes this page.
     * <p>
     * This implementation tries to save the user layout into a standarized system location.
     * 
     * @return <code>true</code> is success.
     */
    @Override
    public boolean close() {

	// 20090913, (JAF), prevents attemps to close a non existing control (i.e.: on exit command execution after page
	// creation exception)
	if (!this.isControlCreated()) {
	    return Boolean.TRUE;
	}

	// Create layout folder and file
	try {
	    // final String userLayoutFolderLocation = FilenameUtils.getFullPath(this.getUserLayout().);
	    // final File userDefinedLayoutFolder = ResourceUtils.getFile(userLayoutFolderLocation);
	    // if (!userDefinedLayoutFolder.exists()) {
	    // FileUtils.forceMkdir(userDefinedLayoutFolder);
	    // }

	    final File userLayoutFile = new File(this.getUserLayout().getURI().toString());
	    if (!userLayoutFile.exists()) {
		userLayoutFile.createNewFile();
	    }

	    final OutputStream out = new FileOutputStream(userLayoutFile);
	    ((DockingDesktop) this.getControl()).getContext().writeXML(out);

	    out.close();
	} catch (final IOException ioe) {
	    BbVLDockingApplicationPage.LOGGER.warn("Cannot save user defined layout", ioe);
	}

	// HACK, set temporally a null initial layout before calling super and restore the value later. This will skip
	// writing the layout again.
	this.setInitialLayout(null);
	final Boolean success = super.close();
	this.setInitialLayout(this.getInitialLayout());

	return success;
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
     * Gets the initial layout.
     * 
     * @return the initial layout.
     */
    public Resource getInitialLayout() {

	if (this.initialLayout == null) {
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
    public Resource getUserLayout() {

	if ((this.userLayout == null) && (this.getInitialLayout() != null)) {
	    final String userHome = System.getProperty("user.home", "/");
	    final String applicationName = this.getApplication().getName();
	    final String filename = this.getInitialLayout().getFilename();

	    final String userLayoutLocation = BbVLDockingApplicationPage.USER_LAYOUT_LOCATION_FMT.format(//
		    new String[] { userHome, applicationName, filename });

	    this.userLayout = this.getApplication().getApplicationContext().getResource(userLayoutLocation);
	}

	return this.userLayout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

	return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", this.getId()).toString();
    }

    /**
     * Creates the control using three attempts with different layouts priorizing the user one.
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
		throw new PageCreationException(errors.get(0), this.getId());
	    }
	}

	return dockingDesktop;
    }

    // /**
    // * Modifica el comportamiento de
    // * {@link
    // org.springframework.richclient.application.support.AbstractApplicationPage#createPageComponent(PageComponentDescriptor)}
    // * con el objetivo de cachear los componentes de página creados en función del descriptor.
    // *
    // * @param descriptor
    // * el descriptor.
    // * @return el componente de página.
    // *
    // * @see
    // org.springframework.richclient.application.support.AbstractApplicationPage#createPageComponent(PageComponentDescriptor)
    // */
    // @Override
    // protected PageComponent createPageComponent(PageComponentDescriptor descriptor) {
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
    // * <li>No añada el <em>dockable</em> al <em>desktop</em> si el dockable ya está creado. Este comportamiento es
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
    // // HACK, (JAF), 20080417, continue just only if dockable is different from null. In such a case super does not
    // // call layout builder
    // final Dockable dockable = this.getDockable(pageComponent);
    // if (dockable != null) {
    // this.getLayoutManager().addDockable(((DockingDesktop) this.getControl()), dockable);
    // }
    // }

    /**
     * Sobreescribe {@link VLDockingApplicationPage#giveFocusTo} para evitar tener que seleccionar dos veces un
     * componente para darle el foco.
     * 
     * @param pageComponent
     *            el componente de la página.
     * @return <code>true</code> si el componente de la página es un <code>Dockable</code> y <code>false</code> en caso
     *         contrario.
     */
    @Override
    protected boolean giveFocusTo(PageComponent pageComponent) {

	// TODO, revisar si sigue haciendo falta esto y por qué va a esta clase, todo lo relativo al foco lo veo en otro
	// sitio

	// Obtener el propiertario del foco.
	final Component focusOwner = FocusManager.getCurrentManager().getFocusOwner();

	// Si esta vista ya tiene el foco retornar true salvo que el dockable sea nulo
	if (pageComponent.getControl().isAncestorOf(focusOwner)) {
	    return (this.getDockable(pageComponent) == null) ? Boolean.FALSE : Boolean.TRUE;
	}

	// Delegar en la clase base
	return super.giveFocusTo(pageComponent);
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

	DockingDesktop dockingDesktop = null;

	// Create the control using the specified layout
	this.setInitialLayout(layout);
	try {
	    if (layout != null) {
		// Force an exception if file does not exist
		layout.getFile();
	    }

	    dockingDesktop = (DockingDesktop) super.createControl();
	} catch (Exception e) {

	    final String resourceDescription = (layout != null) ? layout.getDescription() : StringUtils.EMPTY;
	    final String message = BbVLDockingApplicationPage.PAGE_CREATION_FAILED_FMT.format(//
		    new String[] { this.getPageDescriptor().getId(), resourceDescription });

	    // (JAF), 20090720, register exception to be handled later
	    BbVLDockingApplicationPage.LOGGER.error(message, e);
	    exceptions.add(e);
	} finally {
	    this.setInitialLayout(this.getInitialLayout());
	}
	
	// 
	final Border border = UIManager.getBorder("DockingDesktop.border");
	if (border != null) {
	    dockingDesktop.setBorder(border);
	}

	return dockingDesktop;
    }

    /**
     * If user layout exists then returns it, else returns the initial layout.
     * 
     * @return the layout to be used, may be <code>null</code> if initial layout is not set.
     */
    private Resource getLayout() {

	final Resource userLayout = this.getUserLayout();
	if ((userLayout != null) && userLayout.exists()) {
	    return userLayout;
	}

	return this.getInitialLayout();
    }
}
