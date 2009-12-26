package org.bluebell.richclient.application.support;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.ApplicationWindowFactory;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.support.DefaultApplicationWindow;
import org.springframework.richclient.factory.ComponentFactory;

/**
 * Factoría para la creación de ventanas conformadas por pestañas de páginas.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbApplicationWindowFactory implements ApplicationWindowFactory {

    // TODO, quizás lo mejor sea sacar algunos métodos a una clase padre.
    // TODO, esta implementación no es muy buena, mejorarla

    /**
     * El <em>logger</em>.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BbApplicationWindowFactory.class);

    /**
     * {@inheritDoc}
     */
    public ApplicationWindow createApplicationWindow() {

	BbApplicationWindowFactory.LOGGER.info("Creating new DefaultApplicationWindow");

	return new TabbedApplicationWindow();
    }

    /**
     * Ventana que distribuye sus páginas en ventanas.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class TabbedApplicationWindow extends DefaultApplicationWindow {

	/**
	 * Registro de las páginas mostradas en este panel.
	 */
	private final List<String> pageIds = new ArrayList<String>();

	/**
	 * El panel con las pestañas.
	 */
	private JTabbedPane tabbedPane;

	/**
	 * Obtiene el registro de las páginas mostradas en este panel.
	 * 
	 * @return el registro.
	 */
	public List<String> getPageIds() {

	    return this.pageIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent createToolBarControl() {

	    // Improve tool bar usability
	    final JToolBar toolBar = (JToolBar) super.createToolBarControl();
	    toolBar.setFloatable(Boolean.TRUE);
	    toolBar.setEnabled(Boolean.TRUE);

	    return toolBar;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent createWindowContentPane() {

	    // final JComponent windowContentPane = this.getTabbedPane();
	    final JComponent windowContentPane = super.createWindowContentPane();	  
	    
	    return windowContentPane;
	}

	/**
	 * Obtiene el panel con las pestañas y si no existe lo crea.
	 * 
	 * @return el panel.
	 */
	protected JTabbedPane getTabbedPane() {

	    if (this.tabbedPane == null) {
		final ComponentFactory componentFactory = (ComponentFactory) //
		this.getServices().getService(ComponentFactory.class);

		final JTabbedPane tabbedPane = componentFactory.createTabbedPane();
		tabbedPane.setTabPlacement(SwingConstants.TOP);
		tabbedPane.getModel().addChangeListener(new TrackPageChangesListener());

		this.setTabbedPane(tabbedPane);
	    }

	    return this.tabbedPane;
	}

	/**
	 * {@inheritDoc}
	 */
	// @Override
	protected void setActivePage2(ApplicationPage page) {

	    final JComponent control = page.getControl();
	    final int selectedIndex = this.getTabbedPane().getSelectedIndex();

	    int index = ArrayUtils.indexOf(//
		    this.getTabbedPane().getComponents(), control);

	    if ((selectedIndex == index) && (index != -1)) {
		// Evitar invocaciones recursivas
		return;
	    }

	    // Añadir el tab
	    if (index == -1) {
		index = this.getTabbedPane().getTabCount();
		this.getPageIds().add(index, page.getId());
		this.insertTab(page);
	    }

	    // Seleccionar y refrescar el tab
	    this.getTabbedPane().setSelectedIndex(index);
	    this.getTabbedPane().validate();
	}

	/**
	 * Inserta una nueva página en la ventana.
	 * 
	 * @param page
	 *            la página.
	 */
	private void insertTab(ApplicationPage page) {

	    final PageDescriptor pageDescriptor = this.getPageDescriptor(page.getId());

	    this.getTabbedPane().insertTab(//
		    pageDescriptor.getDisplayName(), //
		    pageDescriptor.getIcon(), //
		    page.getControl(), //
		    pageDescriptor.getDescription(), //
		    this.getTabbedPane().getTabCount());
	}

	/**
	 * Establece el panel con las pestañas.
	 * 
	 * @param tabbedPane
	 *            el panel con las pestañas.
	 * @return el panel con las pestañas.
	 */
	private JTabbedPane setTabbedPane(JTabbedPane tabbedPane) {

	    this.tabbedPane = tabbedPane;

	    return this.tabbedPane;
	}

	/**
	 * <em>Listener</em> que gestiona los cambios en la página seleccionada.
	 * 
	 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
	 */
	private class TrackPageChangesListener implements ChangeListener {

	    /**
	     * Notifica a la ventana cada vez que cambie la página seleccionada.
	     * 
	     * @param e
	     *            el evento de cambio.
	     */
	    public void stateChanged(ChangeEvent e) {

		final int size = BbApplicationWindowFactory.TabbedApplicationWindow.this.getPageIds().size();
		final int idx = BbApplicationWindowFactory.TabbedApplicationWindow.this.//
			getTabbedPane().getSelectedIndex();

		if (size > idx) {
		    final String pageId = BbApplicationWindowFactory.TabbedApplicationWindow.this.getPageIds().get(idx);
		    BbApplicationWindowFactory.TabbedApplicationWindow.this.showPage(pageId);
		}
	    }
	}
    }
}
