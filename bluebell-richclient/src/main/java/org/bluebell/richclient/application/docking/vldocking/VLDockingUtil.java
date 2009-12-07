/**
 * 
 */
package org.bluebell.richclient.application.docking.vldocking;

import java.awt.Color;

import javax.swing.UIManager;

import org.apache.commons.lang.StringUtils;
import org.springframework.richclient.util.Assert;

/**
 * Utility class for dealing with VLDocking.
 * 
 * @see DockingPreferencesWidgetExtension
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class VLDockingUtil {

    /**
     * The activation infix for <em>active</em> UI property names.
     */
    private static String ACTIVE_INFIX = ".active";

    /**
     * The '{@value #DOT}' character.
     */
    private static char DOT = '.';

    /**
     * The activation infix for <em>inactive</em> UI property names.
     */
    private static String INACTIVE_INFIX = ".inactive";

    /**
     * Transforms a UI object key into an activation aware key name.
     * 
     * @param key
     *            the key.
     * @param active
     *            <code>true</code> for <em>active</em> UI keys and <code>false</code> for inactive.
     * @return the transformed key.
     */
    public static String activationKey(String key, Boolean active) {

	Assert.notNull(key, "key");
	Assert.notNull(active, "active");

	final int index = StringUtils.lastIndexOf(key, VLDockingUtil.DOT);
	final String overlay = active ? VLDockingUtil.ACTIVE_INFIX : VLDockingUtil.INACTIVE_INFIX;

	return StringUtils.overlay(key, overlay, index, index);
    }

    /**
     * A set of colors with its own semantic.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static enum DockingColor {

	ACTIVE_WIDGET("VLDocking.activeWidgetColor", Color.DARK_GRAY), //
	BACKGROUND("DockingDesktop.backgroundColor", Color.WHITE), // "controlShadow", "VLDocking.shadow"
	HIGHLIGHT("controlLtHighlight", Color.BLACK), // "VLDocking.highlight"
	INACTIVE_WIDGET("VLDocking.inactiveWidgetColor", Color.LIGHT_GRAY), //
	SHADOW("controlDkShadow", Color.GRAY);

	/**
	 * The default color to be used if other is not found.
	 */
	private Color defaultColor;

	/**
	 * The key to query the UIManager for getting the associated color.
	 */
	private String key;

	/**
	 * Creates the enumerated type.
	 * 
	 * @param key
	 *            the key to query the UIManager.
	 */
	private DockingColor(String key, Color defaultColor) {

	    this.setKey(key);
	    this.setDefaultColor(defaultColor);
	}

	/**
	 * Gets the related color.
	 * 
	 * @return the related color.
	 */
	public Color getColor() {

	    return UIManager.getColor(this.getKey());
	}

	/**
	 * Gets the default color.
	 * 
	 * @return the default color.
	 */
	public Color getDefaultColor() {

	    return this.defaultColor;
	}

	/**
	 * Gets the key.
	 * 
	 * @return the key
	 */
	public String getKey() {

	    return this.key;
	}

	/**
	 * Sets the default color.
	 * 
	 * @param defaultColor
	 *            the default color to set.
	 */
	private void setDefaultColor(Color defaultColor) {

	    Assert.notNull(defaultColor, "defaultColor");

	    this.defaultColor = defaultColor;
	}

	/**
	 * Sets the key.
	 * 
	 * @param key
	 *            the key to set.
	 */
	private void setKey(String key) {

	    Assert.notNull(key, "key");

	    this.key = key;
	}
    }

    /**
     * An enum to distinguish between dock view types.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static enum DockViewType {
	CLOSED, DOCKED, FLOATING, HIDDEN, MAXIMIZED, TABBED
    }
}