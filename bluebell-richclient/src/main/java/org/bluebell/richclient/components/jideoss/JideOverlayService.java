/**
 * 
 */
package org.bluebell.richclient.components.jideoss;

import java.awt.Container;
import java.awt.Insets;
import java.text.MessageFormat;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import org.bluebell.richclient.components.OverlayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.richclient.util.Assert;

import com.jidesoft.swing.DefaultOverlayable;

/**
 * Jide OSS implementation of overlay service.
 * <p>
 * Expects the target component to get attached an instance of <code>DefaultOverlayable</code> (AKA "overlayable"), in
 * other case this will not work.
 * <p>
 * For that reason this implementation should be used together with <code>JideBindingFactory</code> in order to find
 * <code>DefaultOverlayable</code> in the overlayable component hierarchy.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 * 
 * @see DefaultOverlayable
 * @see org.bluebell.richclient.form.binding.jideoss.JideBindingFactory
 */
public class JideOverlayService implements OverlayService, SwingConstants {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JideOverlayService.class);

    /**
     * Return value for searchs with no result.
     */
    private static final int NOT_FOUND = -1;

    /**
     * Message when overlay is already installed.
     */
    private static final MessageFormat NOT_FOUND_FMT = new MessageFormat("Overlayable not found for \"{0}\"");

    /**
     * Message when overlay is already installed.
     */
    private static final MessageFormat ALREADY_INSTALLED_FMT = new MessageFormat(
            "Overlay \"{0}\" already installed into \"{1}\"");

    /**
     * Message when overlay is not installed yet.
     */
    private static final MessageFormat NOT_INSTALLED_FMT = new MessageFormat(
            "Overlay \"{0}\" not installed into \"{1}\"");

    /**
     * {@inheritDoc}
     * <p>
     * Employs a default position <code>SwingConstants.NORTH_WEST</code> and <code>null</code> insets to avoid changes.
     * 
     * @see #installOverlay(JComponent, JComponent, int, Insets)
     */
    @Override
    public Boolean installOverlay(JComponent targetComponent, JComponent overlay) {

        return this.installOverlay(targetComponent, overlay, SwingConstants.NORTH_WEST, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean installOverlay(JComponent targetComponent, JComponent overlay, int position, Insets insets) {

        Assert.notNull(targetComponent, "targetComponent");
        Assert.notNull(overlay, "overlay");

        final DefaultOverlayable overlayable = this.findOverlayable(targetComponent);

        if (overlayable != null) {

            // If overlay is not installed...
            if (overlayable.getOverlayLocation(overlay) == JideOverlayService.NOT_FOUND) {

                // Install overlay
                overlayable.addOverlayComponent(overlay, position, -1);

                // If location insets are not null then change them
                if (insets != null) {
                    overlayable.setOverlayLocationInsets(insets);
                }

                return Boolean.TRUE;
            } else {
                JideOverlayService.LOGGER.warn(JideOverlayService.ALREADY_INSTALLED_FMT.format(//
                        new String[] { overlay.getName(), targetComponent.getName() }));
            }
        }

        return Boolean.FALSE;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Employs <code>null</code> insets to avoid changes.
     * 
     * @see #uninstallOverlay(JComponent, JComponent, Insets)
     */
    public Boolean uninstallOverlay(JComponent targetComponent, JComponent overlay) {

        return this.uninstallOverlay(targetComponent, overlay, null);
    }

    /**
     * {@inheritDoc}
     */
    public Boolean uninstallOverlay(JComponent targetComponent, JComponent overlay, Insets insets) {

        Assert.notNull(targetComponent, "targetComponent");
        Assert.notNull(overlay, "overlay");

        final DefaultOverlayable overlayable = this.findOverlayable(targetComponent);

        if (overlayable != null) {

            // If overlay is installed...
            if (overlayable.getOverlayLocation(overlay) != JideOverlayService.NOT_FOUND) {

                // Uninstall overlay
                overlayable.removeOverlayComponent(overlay);

                // If location insets are not null then change them
                if (insets != null) {
                    overlayable.setOverlayLocationInsets(insets);
                }

                return Boolean.TRUE;

            } else {
                JideOverlayService.LOGGER.warn(JideOverlayService.NOT_INSTALLED_FMT.format(//
                        new String[] { overlay.getName(), targetComponent.getName() }));
            }
        }

        return Boolean.FALSE;
    }

    /**
     * {@inheritDoc}
     * 
     * @see #setVisible(JComponent, JComponent, Insets, Boolean)
     */
    @Override
    public Boolean showOverlay(JComponent targetComponent, JComponent overlay) {

        return this.setVisible(targetComponent, overlay, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     * 
     * @see #setVisible(JComponent, JComponent, Insets, Boolean)
     */
    @Override
    public Boolean hideOverlay(JComponent targetComponent, JComponent overlay) {

        return this.setVisible(targetComponent, overlay, Boolean.FALSE);
    }

    /**
     * Tries to find a <code>DefaultOverlayable</code> given a target component.
     * 
     * @param targetComponent
     *            the target component.
     * @return the associated <code>Overlayable</code> if found and <code>null</code> in other case.
     */
    protected final DefaultOverlayable findOverlayable(JComponent targetComponent) {

        // Find overlayable in tart component container
        final Container parent = targetComponent.getParent();

        if ((parent != null) && (parent instanceof DefaultOverlayable)) {

            return (DefaultOverlayable) parent;
        }

        // Unexpected situation
        JideOverlayService.LOGGER.warn(JideOverlayService.NOT_FOUND_FMT.format(//
                new String[] { targetComponent.getName() }));

        return null;
    }

    /**
     * Show or hide the given overlay depending on the given <code>show</code> parameter.
     * 
     * @param targetComponent
     *            the target component.
     * @param overlay
     *            the overlay component.
     * @param show
     *            whether to show or hide the given overlay (<code>true</code> for showing).
     * 
     * @return <code>true</code> if success and <code>false</code> in other case.
     */
    protected final Boolean setVisible(JComponent targetComponent, JComponent overlay, Boolean show) {

        Assert.notNull(targetComponent, "targetComponent");
        Assert.notNull(overlay, "overlay");
        Assert.notNull(show, "show");

        final DefaultOverlayable overlayable = this.findOverlayable(targetComponent);

        if (overlayable != null) {

            // If overlay is installed...
            if (overlayable.getOverlayLocation(overlay) != JideOverlayService.NOT_FOUND) {

                // Definitely show or hide overlay
                overlay.setVisible(show);
                overlay.repaint();

                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }
}
