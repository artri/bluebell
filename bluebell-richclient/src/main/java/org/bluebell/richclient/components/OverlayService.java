/**
 * 
 */
package org.bluebell.richclient.components;

import java.awt.Insets;

import javax.swing.JComponent;

/**
 * This interface provide a way for installing and uninstalling overlay components.
 * <p>
 * It's also a way for decoupling this functionality initially provided by <code>OverlayHelper</code>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public interface OverlayService {

    /**
     * Installs an overlay on top of an overlayable component.
     * 
     * @param overlayable
     *            the target component.
     * @param overlay
     *            the overlay.
     */
    void installOverlay(JComponent overlayable, JComponent overlay);

    /**
     * Installs an overlay on top of an overlayable component.
     * 
     * @param overlayable
     *            the target component.
     * @param overlay
     *            the overlay.
     * @param position
     *            the overlay position.
     * @param insets
     *            the overlay location insets
     */
    void installOverlay(JComponent overlayable, JComponent overlay, int position, Insets insets);

    /**
     * Uninstalls an overlay.
     * 
     * @param overlayable
     *            the target component.
     * @param overlay
     *            the overlay.
     */
    void uninstallOverlay(JComponent overlayable, JComponent overlay);
}
