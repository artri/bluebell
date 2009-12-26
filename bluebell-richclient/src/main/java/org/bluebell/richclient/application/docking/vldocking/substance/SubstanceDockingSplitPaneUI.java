/**
 * 
 */
package org.bluebell.richclient.application.docking.vldocking.substance;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

import org.bluebell.richclient.application.docking.vldocking.VLDockingUtil.DockingColor;

import com.vlsolutions.swing.docking.ui.DockingSplitPaneUI;

/**
 * A UI for the docking split pane that uses Substance color scheme.
 * <p />
 * Extends {@link DockingSplitPaneUI} in the following way:
 * <ul>
 * <li>Re-installs, if needed, the border of the pane.
 * <li>Default divider creation also sets the color.
 * </ul>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SubstanceDockingSplitPaneUI extends DockingSplitPaneUI {

    /**
     * Creates the UI.
     */
    public SubstanceDockingSplitPaneUI() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uninstallUI(JComponent c) {

        super.uninstallUI(c);

        c.setBorder(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g, JComponent jc) {

        super.paint(g, jc);

        // Change border
        final Border border = UIManager.getBorder("SplitContainer.border");
        if (!this.existBorderInHierarchy(jc, border)) {
            jc.setBorder(border);
        }
    }

    /**
     * Creates the default divider.
     * 
     * @return the divider.
     */
    public BasicSplitPaneDivider createDefaultDivider() {

        final BasicSplitPaneDivider divider = super.createDefaultDivider();

        // Change background color
        final Color backgroundColor = DockingColor.BACKGROUND.getColor();
        if (backgroundColor != null) {
            divider.setBackground(backgroundColor);
        }

        return divider;
    }

    /**
     * Founds a border into a component or any other of its parents hierarchy.
     * 
     * @param jc
     *            the supplied component.
     * @param border
     *            the supplied border.
     * @return <code>true</code> if found or <code>false</code> in other case.
     */
    private Boolean existBorderInHierarchy(JComponent jc, Border border) {

        final Container parent = jc.getParent();

        if (jc.getBorder() == border) {
            return Boolean.TRUE;
        } else if (parent instanceof JComponent) {
            return this.existBorderInHierarchy((JComponent) parent, border);
        } else {
            return Boolean.FALSE;
        }
    }

    /**
     * Factory method for creating the UI.
     * 
     * @param c
     *            the split pane.
     * @return the UI.
     */
    public static ComponentUI createUI(JComponent c) {

        return new SubstanceDockingSplitPaneUI();
    }
}
