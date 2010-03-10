/**
 * 
 */
package org.bluebell.richclient.components.swing;

import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import org.bluebell.richclient.components.OverlayService;
import org.springframework.richclient.form.builder.support.InterceptorOverlayHelper;

/**
 * Default overlay service implementation based on Spring RCP <code>OverlayHelper</code>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class DefaultOverlayService implements OverlayService, SwingConstants {

    /**
     * The default insets to be used.
     */
    private static final Insets DEFAULT_INSETS = new Insets(0, 0, 0, 0);

    /**
     * {@inheritDoc}
     */
    @Override
    public void installOverlay(JComponent overlayable, JComponent overlay) {

        this.installOverlay(overlayable, overlay, SwingConstants.NORTH_WEST, DefaultOverlayService.DEFAULT_INSETS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void installOverlay(JComponent overlayable, JComponent overlay, int position, Insets insets) {

        InterceptorOverlayHelper.attachOverlay(overlay, overlayable, position, insets.left, insets.top);
        overlay.setVisible(Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     */
    public void uninstallOverlay(JComponent overlayable, JComponent overlay) {

        overlay.setVisible(Boolean.FALSE);
    }
}
