package org.bluebell.richclient.application.docking.vldocking.substance;

import java.awt.Color;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.bluebell.richclient.application.docking.vldocking.DockingPreferencesWidgetExtension;
import org.bluebell.richclient.application.docking.vldocking.VLDockingUtil;
import org.pushingpixels.lafplugin.LafComponentPlugin;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.skin.SkinChangeListener;
import org.springframework.util.Assert;

import com.vlsolutions.swing.docking.DockableContainerFactory;

/**
 * <a href="http://www.vlsolutions.com/en/products/docking/">VLDocking</a> plugin for Substance.
 * <p>
 * This implementation is initially based on <a href="http://forum.springsource.org/showthread.php?t=73183">Spring Forum
 * comments</a>.
 * <p>
 * Basically fix some errors with title bars and detached dockables.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SubstanceVLDockingPlugin implements LafComponentPlugin, SkinChangeListener {

    /**
     * The default margin size.
     */
    public static final Integer MARGIN_SIZE = 5;

    // TODO, explicar el tamaño de los borders

    /**
     * The default border size.
     */
    public static final Integer BORDER_SIZE = 1;

    /**
     * Creates the plugin.
     */
    public SubstanceVLDockingPlugin() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    public void skinChanged() {

        DockingPreferencesWidgetExtension.uninstallWidgetDesktopStyle();

        // Initialize default VLDocking setup
        // http://www.vlsolutions.com/en/documentation/docking/tutorial/tutorial8.php
        DockingPreferencesWidgetExtension.setWidgetDesktopStyle();

        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }

    }

    /**
     * {@inheritDoc}
     * 
     * @see SubstanceDockViewTitleBarUI
     */
    public Object[] getDefaults(Object mSkin) {

        final SubstanceSkin skin = (SubstanceSkin) mSkin;

        this.reset(skin);

        final Map<String, Object> defaultsMap = this.getDefaultsMap(skin);
        final ArrayList<Object> defaults = new ArrayList<Object>(defaultsMap.size() * 2);
        for (Map.Entry<String, Object> entry : defaultsMap.entrySet()) {
            if (entry.getKey() != null) {
                defaults.add(entry.getKey());
                defaults.add(entry.getValue());
            }
        }

        return defaults.toArray(new Object[defaults.size()]);
    }

    /**
     * Proceeds according to <a href= "http://www.vlsolutions.com/en/documentation/docking/tutorial/tutorial8.php"
     * >VLDocking Tutorial</a>:
     * <p>
     * <div> Please note that to avoid having your own UI settings beeing erased by the default ones, you will have to
     * follow the pattern :
     * <ol>
     * <li>pre-install the default ui settings in you main() method, or any method prior DockingDesktop usage
     * <li>put your new settings as UIManager properties
     * </ol>
     * 
     * <pre>
     *  public static void main(String[] args){ // first, preload the UI to avoid erasing your own customizations
     * 
     *          DockingUISettings.getInstance().installUI(); 
     * 
     *          // declare your border
     *          Border myBorder = ... 
     * 
     *          // and start customizing...
     *          UIManager.put(&quot;DockView.maximizedDockableBorder&quot;, myBorder); ... }
     * </pre>
     * 
     * </div>
     * 
     * <p>
     * <b>Note</b> this method is called once so we need to make extra actions at every skin change.
     * 
     * @see SubstanceDockableContainerFactory
     * @see DockingPreferencesWidgetExtension
     * @see #skinChanged()
     */
    public void initialize() {

        // Change dockable container factory
        DockableContainerFactory.setFactory(new SubstanceDockableContainerFactory());

        // Initialize default VLDocking setup
        // http://www.vlsolutions.com/en/documentation/docking/tutorial/tutorial8.php
        DockingPreferencesWidgetExtension.setWidgetDesktopStyle();

        // Register the skin change listener
        SubstanceLookAndFeel.registerSkinChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    public void uninitialize() {

        this.reset(null);

        // Unregister the skin change listener
        SubstanceLookAndFeel.unregisterSkinChangeListener(this);
    }

    /**
     * Gets the property values to be installed.
     * 
     * @param skin
     *            the skin to be used. Id a <code>null</code> value is provided this implementation falls back to the
     *            LookAndFeel current skin. An exception is thrown if skin cannot been obtained.
     * @return a map indexed by property name.
     */
    protected Map<String, Object> getDefaultsMap(SubstanceSkin skin) {

        if (skin == null) {
            skin = SubstanceLookAndFeel.getCurrentSkin();
        }
        Assert.notNull(skin, "skin"); // Invariant

        final Map<String, Object> defaults = new HashMap<String, Object>();

        final Color shadow = skin.getMainDefaultColorScheme().getDarkColor();
        final Color highlight = skin.getMainDefaultColorScheme().getLightColor();
        final Color background = skin.getMainDefaultColorScheme().getBackgroundFillColor();
        final Color active = skin.getMainActiveColorScheme().getMidColor();
        final Color inactive = skin.getMainDefaultColorScheme().getMidColor();

        // Widget style colors
        defaults.put(VLDockingUtil.DockingColor.BACKGROUND.getKey(), background);
        defaults.put(VLDockingUtil.DockingColor.SHADOW.getKey(), shadow);
        defaults.put(VLDockingUtil.DockingColor.HIGHLIGHT.getKey(), highlight);
        defaults.put(VLDockingUtil.DockingColor.ACTIVE_WIDGET.getKey(), active);
        defaults.put(VLDockingUtil.DockingColor.INACTIVE_WIDGET.getKey(), inactive);

        // Color properties for detached dockables:
        // TODO cambiar esto
        defaults.put("activeCaption", background);
        defaults.put("inactiveCaption", background);
        defaults.put("activeCaptionBorder", active);
        defaults.put("inactiveCaptionBorder", inactive);

        // TabbedContainer
        // Request focus on tab selection, otherwise activation will not be
        // triggered correctly
        defaults.put("TabbedContainer.requestFocusOnTabSelection", Boolean.TRUE);

        // UI Delegates: must be the latest
        defaults.put("AutoHideButtonUI", SubstanceAutoHideButtonUI.class.getName());
        defaults.put("AutoHideExpandPanelUI", SubstanceAutoHideExpandPanelUI.class.getName());
        defaults.put("DetachedDockViewUI", SubstanceDockViewUI.class.getName());
        defaults.put("DockViewUI", SubstanceDockViewUI.class.getName());
        defaults.put("DockViewTitleBarUI", SubstanceDockViewTitleBarUI.class.getName());
        defaults.put("DockingSplitPaneUI", SubstanceDockingSplitPaneUI.class.getName());

        return defaults;
    }

    /**
     * Reset changes.
     * 
     * @param skin
     *            the skin previous changes applies to.
     */
    private void reset(SubstanceSkin skin) {

        for (String key : this.getDefaultsMap(skin).keySet()) {
            UIManager.put(key, null);
        }
    }
}
