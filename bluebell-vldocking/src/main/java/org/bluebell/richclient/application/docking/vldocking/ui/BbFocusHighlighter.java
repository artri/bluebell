/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell VLDocking.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bluebell.richclient.application.docking.vldocking.ui;

import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import org.bluebell.richclient.application.docking.vldocking.VLDockingUtils.FocusGainedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.vlsolutions.swing.docking.DockViewTitleBar;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * A property change listener with similar behaviour as
 * <code>com.vlsolutions.swing.docking.DockViewTitleBar$FocusHighlighter</code> that resolves an activation bug
 * consisting on:
 * 
 * <pre>
 * When dealing with multiple desktops if the user tries to change the active one, old focus listener does not 
 * propagate activation changes correctly to other desktop dockables (page components).
 * </pre>
 * 
 * It's also important to take into account the following line at <code>WidgetDesktopStyle</code>:
 * 
 * <pre>
 *      (JAF), 20101205, Very important!!!
 * 
 *      Should request focus on tab selection be activated? On one hand in case negative activation will not be 
 *      triggered correctly. On the other hand (affirmative case) every time a tabbed container is shown request focus,
 *      so this is a problem since is better to keep old dockable focused (activated!)
 * 
 *      Decission is to keep value as FALSE since is prior to retain old selection.
 * 
 *      this.defaults.put("TabbedContainer.requestFocusOnTabSelection", Boolean.FALSE);
 * </pre>
 * 
 * @see Associated <a href="http://jirabluebell.b2b2000.com/browse/BLUE-31">JIRA issue</a>
 * @see Javalobby <a href="http://www.javalobby.org/java/forums/t43667.html">article</a> regarding to focus traversal on
 *      a tabbed pane.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbFocusHighlighter implements PropertyChangeListener {

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BbFocusHighlighter.class);

    /**
     * The singleton instance.
     */
    private static final PropertyChangeListener INSTANCE = new BbFocusHighlighter();

    /**
     * Since original <em>focus highlighter</em> is private, class name is useful to do the trick.
     * 
     * @see #replaceFocusHighlighterIfNeeded()
     */
    private static final String FOCUS_HIGHLIGHTER_CLASS = //
    "com.vlsolutions.swing.docking.DockViewTitleBar$FocusHighlighter";

    /**
     * The {@value #FOCUS_OWNER} property name.
     */
    private static final String FOCUS_OWNER = "focusOwner";

    /**
     * Remembers the last active title bar.
     */
    private DockViewTitleBar lastActiveTitleBar = null;

    /**
     * {@inheritDoc}
     * 
     * @see sun.swing.SwingUtilities2#tabbedPaneChangeFocusTo(java.awt.Component)
     * @see sun.swing.SwingUtilities2#compositeRequestFocus(java.awt.Component)
     */
    public void propertyChange(PropertyChangeEvent e) {

        final FocusGainedBean bean = new FocusGainedBean().setEvent(e).setLastTitleBar(this.lastActiveTitleBar);

        /*
         * (JAF), 20101205, note this method MUST work with "title bar" instead of "dockable" due to after some actions
         * titleBar identity is replaced - while dockable keeps unchanged (i.e.: detaching a dockable or reseting
         * application page)
         */

        if ((bean.getLastTitleBar() == null) && (bean.getNewTitleBar() == null)) {

            /* CASE 0: never mind */
            bean.setWhatsHappening("CASE 0 - "
                    + "Inconsiderable event: neither last active dockable nor a newer one are involved");

        } else if (bean.getLastTitleBar() == bean.getNewTitleBar()) {

            /* CASE 1: neither newer nor older are null */
            bean.setWhatsHappening("CASE 1 - New active dockable is the remembered, "
                    + "however it may have being de-activated previously");

        } else if ((bean.getNewTitleBar() == null) && (bean.getOldTitleBar() != null)) {

            /* CASE 2: temporary focus lost */
            bean.setWhatsHappening("CASE 2 - Last active dockable has lost focus (may be temporary)");

        } else if (bean.getNewTitleBar() != null) {

            /* CASE 3: deactivate old dockable and activate newer */
            bean.setWhatsHappening("CASE 3 - A new dockable has been selected");
            BbDockViewTitleBarUI.nullSafeRepaintDockable(bean.getLastTitleBar(), Boolean.FALSE);
            BbDockViewTitleBarUI.nullSafeRepaintDockable(bean.getNewTitleBar(), Boolean.TRUE);

            /*
             * (JAF), 20101205, this line is... ...magic:
             * 
             * According to this great Javalobby article: http://www.javalobby.org/java/forums/t43667.html
             * 
             * a) - When changing selected tab on a tabbed pane, focusTraversalPolicy tries to find the default
             * component for a given container, this is frustrant...
             * 
             * b) - Setting docking desktop as NON focusable, does the trick!!
             * ----------------------------------------------------------------
             * 
             * If docking desktop were focusable then
             * sun.swing.SwingUtilities2#tabbedPaneChangeFocusTo(java.awt.Component) would invoke
             * SwingUtilities2#compositeRequestFocus(java.awt.Component) and that is not so cool...
             */
            final DockingDesktop dockingDesktop = bean.getNewTitleBar().getDesktop();
            if (dockingDesktop != null) {
                dockingDesktop.setFocusable(Boolean.FALSE);
            }

            this.lastActiveTitleBar = bean.getNewTitleBar();

        } else {

            /* CASE 4: never mind */
            bean.setWhatsHappening("CASE 4 - Something not very meaningful");
        }

        if (BbFocusHighlighter.LOGGER.isDebugEnabled()) {
            BbFocusHighlighter.LOGGER.debug("Focus change event " + bean.toString());
        }
    }

    /**
     * Replaces the <em>buggy</em> focus manager listener (
     * <code>com.vlsolutions.swing.docking.DockViewTitleBar.FocusHighlighter</code>) installed by
     * {@link DockViewTitleBar} with Bluebell one.
     * 
     * <b>Note</b> this method acts just the first time.
     * 
     * @return <code>true</code> if success.
     */
    public static Boolean replaceFocusHighlighterIfNeeded() {

        Boolean success = Boolean.FALSE;

        final KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

        // a) Remove focus highlighter listener (com.vlsolutions.swing.docking.DockViewTitleBar.FocusHighlighter)
        final List<PropertyChangeListener> listeners = Arrays.asList(focusManager.getPropertyChangeListeners());
        for (PropertyChangeListener listener : listeners) {

            final EventListener unproxiedListener = BbFocusHighlighter.unproxy(listener);
            final String unproxiedListenerClass = unproxiedListener.getClass().getName();

            final Boolean isHighlighter = BbFocusHighlighter.FOCUS_HIGHLIGHTER_CLASS.equals(unproxiedListenerClass);
            if (isHighlighter) {
                focusManager.removePropertyChangeListener(listener);
                success = Boolean.TRUE;
            }
        }

        // b) Install BB focus highlighter.
        if (success) {
            focusManager.addPropertyChangeListener(BbFocusHighlighter.FOCUS_OWNER, BbFocusHighlighter.INSTANCE);
        }

        return success;
    }

    /**
     * Unproxy a <code>PropertyChangeListener</code> looking for nested {@link PropertyChangeListenerProxy}'s (aka
     * PCLP).
     * 
     * @param listener
     *            the target listener.
     * @return the most deep (non PCLP) listener on target hierarchy.
     */
    private static EventListener unproxy(PropertyChangeListener listener) {

        Assert.notNull(listener, "listener");

        EventListener iterator = listener;

        while (iterator instanceof PropertyChangeListenerProxy) {
            iterator = ((PropertyChangeListenerProxy) iterator).getListener();
        }

        return iterator;
    }
}
