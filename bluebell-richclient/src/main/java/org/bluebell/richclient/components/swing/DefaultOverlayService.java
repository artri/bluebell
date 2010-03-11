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
     * 
     * @see #installOverlay(JComponent, JComponent, int, Insets)
     */
    @Override
    public Boolean installOverlay(JComponent targetComponent, JComponent overlay) {

        this.installOverlay(targetComponent, overlay, SwingConstants.NORTH_WEST, DefaultOverlayService.DEFAULT_INSETS);

        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean installOverlay(JComponent targetComponent, JComponent overlay, int position, Insets insets) {

        InterceptorOverlayHelper.attachOverlay(overlay, targetComponent, position, insets.left, insets.top);
        overlay.setVisible(Boolean.TRUE);

        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     * 
     * @see #uninstallOverlay(JComponent, JComponent, Insets)
     */
    public Boolean uninstallOverlay(JComponent targetComponent, JComponent overlay) {

        this.uninstallOverlay(targetComponent, overlay, null);

        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean uninstallOverlay(JComponent targetComponent, JComponent overlay, Insets insets) {

        overlay.setVisible(Boolean.FALSE);

        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean hideOverlay(JComponent targetComponent, JComponent overlay) {

        // TODO

        return Boolean.TRUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean showOverlay(JComponent targetComponent, JComponent overlay) {

        // TODO

        return Boolean.TRUE;
    }
}
