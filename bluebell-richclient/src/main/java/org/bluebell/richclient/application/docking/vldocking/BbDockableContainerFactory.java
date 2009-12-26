package org.bluebell.richclient.application.docking.vldocking;

import com.vlsolutions.swing.docking.DefaultDockableContainerFactory;
import com.vlsolutions.swing.docking.TabbedDockableContainer;

/**
 * 
 * Improved implementation of the <code>DockableContainerFactory</code>.
 * 
 * @see BbDockTabbedPane
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbDockableContainerFactory extends DefaultDockableContainerFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public TabbedDockableContainer createTabbedDockableContainer() {

        return new BbDockTabbedPane();
    }
}
