/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell Jide OSS.
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

/**
 * 
 */
package org.bluebell.richclient.form.binding.jideoss;

import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.RepaintManager;

import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.Overlayable;
import com.jidesoft.swing.OverlayableUtils;

/**
 * Repaint manager to be employed when using <code>JideBindingFactory</code> in order to make overlay support work.
 * <p>
 * Employs an <em>invasive</em> approach since extends from {@link RepaintManager}. Employing delegate methods would be
 * preferred but there are some tricky effects discovered when dealing with <a
 * href="http://jirabluebell.b2b2000.com/browse/BLUE-41">BLUE-41 issue</a>.
 * 
 * <p>
 * Overrides {@link #addDirtyRegion(JComponent, int, int, int, int)} in order to repaint overlayables just after
 * invoking delegate method. This is a way to avoid rewriting <code>repaint</code> for every component class!! ...like
 * in <code>OverlayTextArea</code>:
 * 
 * <pre>
 * &#064;Override
 * public void repaint(long tm, int x, int y, int width, int height) {
 * 
 *     super.repaint(tm, x, y, width, height);
 *     OverlayableUtils.repaintOverlayable(this);
 * }
 * </pre>
 * <p>
 * According to <a href="http://forums.sun.com/thread.jspa?threadID=725127">this thread</a> the "unique" way to listen
 * for repaint changes is overriding <code>RepaintManager</code>. This is not a recommended practice but works anyway...
 * <p>
 * <b>Note</b> this class implements singleton.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 * 
 * @see org.bluebell.richclient.form.binding.jideoss.JideRepaintManagerWrapper
 * 
 * @since 20101228
 */
public class JideRepaintManager extends RepaintManager {

    /**
     * The singleton instance.
     */
    private static JideRepaintManager instance;

    /**
     * Creates the repaint manager given its delegate.
     * 
     * @param delegate
     *            the delegate.
     */
    public JideRepaintManager() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {

        super.addDirtyRegion(c, x, y, w, h);

        // Aditional behaviour
        this.repaintOverlayable(c);
    }

    /**
     * Repaints the parent container of the target component if that is a <code>DefaultOverlayable</code> instance.
     * 
     * @param c
     *            the child component.
     */
    protected void repaintOverlayable(JComponent c) {

        final Container parent = c.getParent();
        if ((!(c instanceof Overlayable)) && (parent != null) && (parent instanceof DefaultOverlayable)) {
            OverlayableUtils.repaintOverlayable(c);
        }
    }

    /**
     * Gets the singleton instance initialized in a lazy mode (useful for reducing race conditions probability).
     * 
     * @return the jide repaint manager.
     */
    public static JideRepaintManager getInstance() {

        if (JideRepaintManager.instance == null) {
            JideRepaintManager.instance = new JideRepaintManager();
        }

        return JideRepaintManager.instance;
    }

    /**
     * Installs this repaint manager if not already set.
     */
    public static void installJideRepaintManagerIfNeeded() {

        final RepaintManager current = RepaintManager.currentManager(null);
        if (current != JideRepaintManager.instance) {
            RepaintManager.setCurrentManager(JideRepaintManager.getInstance());
        }
    }
}
