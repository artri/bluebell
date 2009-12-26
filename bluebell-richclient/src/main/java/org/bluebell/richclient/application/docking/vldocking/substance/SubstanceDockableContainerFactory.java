package org.bluebell.richclient.application.docking.vldocking.substance;

import java.awt.Window;

import javax.swing.JDialog;

import org.bluebell.richclient.application.docking.vldocking.BbDockableContainerFactory;

import com.vlsolutions.swing.docking.FloatingDockableContainer;

/**
 * Substance specific implementation of the <code>DockableContainerFactory</code>.
 * <p>
 * Fixes a known Substance bug working with detached dockables.
 * 
 * @see <a href="https://substance.dev.java.net/issues/show_bug.cgi?id=284">Related bug</a>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SubstanceDockableContainerFactory extends BbDockableContainerFactory {

    /**
     * {@inheritDoc}
     */
    public FloatingDockableContainer createFloatingDockableContainer(Window owner) {

        final Boolean defaultLookAndFeelDecorated = JDialog.isDefaultLookAndFeelDecorated();

        JDialog.setDefaultLookAndFeelDecorated(Boolean.FALSE);
        final FloatingDockableContainer floatingDockableContainer = super.createFloatingDockableContainer(owner);
        JDialog.setDefaultLookAndFeelDecorated(defaultLookAndFeelDecorated);

        return floatingDockableContainer;
    }
}
