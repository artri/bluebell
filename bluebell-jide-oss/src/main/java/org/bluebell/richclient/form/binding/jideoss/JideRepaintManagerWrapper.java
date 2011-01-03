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

import java.applet.Applet;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.RepaintManager;

import org.springframework.richclient.util.Assert;

import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.Overlayable;
import com.jidesoft.swing.OverlayableUtils;

/**
 * Repaint manager to be employed when using <code>JideBindingFactory</code> in order to make overlay support work.
 * <p>
 * Employs a non invasive approach based on delegate methods.
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
 * @deprecated (JAF), 20101228, wrapper approach is invalid due to some tricky effects discovered when dealing with <a
 *             href="http://jirabluebell.b2b2000.com/browse/BLUE-41">BLUE-41</a>. So, extension approach is nevertheless
 *             preferred ({@link org.bluebell.richclient.form.binding.jideoss.JideRepaintManager}).
 * 
 * @see org.bluebell.richclient.form.binding.jideoss.JideRepaintManager
 */
@Deprecated
public class JideRepaintManagerWrapper extends RepaintManager {

    /**
     * The singleton instance.
     */
    private static JideRepaintManagerWrapper instance;

    /**
     * The delegate repaint manager.
     */
    private RepaintManager delegate;

    /**
     * Creates the repaint manager given its delegate.
     * 
     * @param delegate
     *            the delegate.
     */
    public JideRepaintManagerWrapper(RepaintManager delegate) {

        super();
        this.setDelegate(delegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDirtyRegion(Applet applet, int x, int y, int w, int h) {

        this.getDelegate().addDirtyRegion(applet, x, y, w, h);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {

        this.getDelegate().addDirtyRegion(c, x, y, w, h);

        // Aditional behaviour
        this.repaintOverlayable(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDirtyRegion(Window window, int x, int y, int w, int h) {

        this.getDelegate().addDirtyRegion(window, x, y, w, h);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addInvalidComponent(JComponent invalidComponent) {

        this.getDelegate().addInvalidComponent(invalidComponent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {

        return this.getDelegate().equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle getDirtyRegion(JComponent aComponent) {

        return this.getDelegate().getDirtyRegion(aComponent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getDoubleBufferMaximumSize() {

        return this.getDelegate().getDoubleBufferMaximumSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {

        return this.getDelegate().getOffscreenBuffer(c, proposedWidth, proposedHeight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getVolatileOffscreenBuffer(Component c, int proposedWidth, int proposedHeight) {

        return this.getDelegate().getVolatileOffscreenBuffer(c, proposedWidth, proposedHeight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        return this.getDelegate().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompletelyDirty(JComponent aComponent) {

        return this.getDelegate().isCompletelyDirty(aComponent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDoubleBufferingEnabled() {

        return this.getDelegate().isDoubleBufferingEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markCompletelyClean(JComponent aComponent) {

        this.getDelegate().markCompletelyClean(aComponent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markCompletelyDirty(JComponent aComponent) {

        this.getDelegate().markCompletelyDirty(aComponent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintDirtyRegions() {

        this.getDelegate().paintDirtyRegions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeInvalidComponent(JComponent component) {

        this.getDelegate().removeInvalidComponent(component);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDoubleBufferingEnabled(boolean aFlag) {

        this.getDelegate().setDoubleBufferingEnabled(aFlag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDoubleBufferMaximumSize(Dimension d) {

        this.getDelegate().setDoubleBufferMaximumSize(d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return this.getDelegate().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInvalidComponents() {

        this.getDelegate().validateInvalidComponents();
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
     * Gets the delegate repaint manager.
     * 
     * @return the delegate.
     */
    protected final RepaintManager getDelegate() {

        return this.delegate;
    }

    /**
     * Sets the delegate repaint manager.
     * 
     * @param delegate
     *            the delegate to set.
     */
    private void setDelegate(RepaintManager delegate) {

        Assert.notNull(delegate, "delegate");

        this.delegate = delegate;
    }

    /**
     * Gets the singleton instance initialized in a lazy mode (useful for reducing race conditions probability).
     * 
     * @return the jide repaint manager.
     */
    public static JideRepaintManagerWrapper getInstance() {

        if (JideRepaintManagerWrapper.instance == null) {
            final RepaintManager current = RepaintManager.currentManager(null);
            JideRepaintManagerWrapper.instance = new JideRepaintManagerWrapper(current);
        }

        return JideRepaintManagerWrapper.instance;
    }

    /**
     * Installs this repaint manager if not already set.
     */
    public static void installJideRepaintManagerIfNeeded() {

        final RepaintManager current = RepaintManager.currentManager(null);
        if (current != JideRepaintManagerWrapper.instance) {
            RepaintManager.setCurrentManager(JideRepaintManagerWrapper.getInstance());
        }
    }
}
