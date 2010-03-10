/**
 * 
 */
package org.bluebell.richclient.components.jideoss;

import java.awt.Container;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import org.bluebell.richclient.components.OverlayService;

import com.jidesoft.swing.DefaultOverlayable;

/**
 * Jide OSS implementation of overlay service. Expects the target component (AKA "overlayable") be an instance of
 * <code>DefaultOverlayable</code>, in other case this will not work.
 * <p>
 * For this reason this implementation should be used together with <code>JideBindingFactory</code> in order to find
 * <code>DefaultOverlayable</code> in the overlayable component hierarchy.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 * 
 * @see DefaultOverlayable
 * @see org.bluebell.richclient.form.binding.jideoss.JideBindingFactory
 */
public class JideOverlayService implements OverlayService, SwingConstants {

    /**
     * Return value for searchs with no result.
     */
    private static final int NOT_FOUND = -1;

    /**
     * The default insets to be used.
     */
    private static final Insets DEFAULT_INSETS = new Insets(0, 0, 0, 0);

    /**
     * {@inheritDoc}
     */
    @Override
    public void installOverlay(JComponent overlayable, JComponent overlay) {

        final Insets insets = new Insets(0, overlay.getWidth(), 0, 0);

        this.installOverlay(overlayable, overlay, SwingConstants.NORTH_WEST, insets);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void installOverlay(JComponent overlayable, JComponent overlay, int position, Insets insets) {

        final Container parent = overlayable.getParent();

        if ((parent != null) && (parent instanceof DefaultOverlayable)) {

            final DefaultOverlayable defaultOverlayable = (DefaultOverlayable) parent;

            if (defaultOverlayable.getOverlayLocation(overlay) == JideOverlayService.NOT_FOUND) {
                defaultOverlayable.addOverlayComponent(overlay, position, -1);
                defaultOverlayable.setOverlayLocationInsets(insets);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void uninstallOverlay(JComponent overlayable, JComponent overlay) {

        final Container parent = overlayable.getParent();

        if ((parent != null) && (parent instanceof DefaultOverlayable)) {

            final DefaultOverlayable defaultOverlayable = (DefaultOverlayable) parent;

            defaultOverlayable.removeOverlayComponent(overlay);
            defaultOverlayable.setOverlayLocationInsets(JideOverlayService.DEFAULT_INSETS);
        }
    }
}
