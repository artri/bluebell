/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell Rich Client.
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

package org.bluebell.richclient.swing.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * Property change listener wrapper that reduces the risk of memory leaks.
 * <p/>
 * How to use this: <code>
 * <pre>
 * KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 * <p/>
 * // instead of registering directly use weak listener
 * // focusManager.addPropertyChangeListener(focusOwnerListener);
 * <p/>
 * focusManager.addPropertyChangeListener(new WeakPropertyChangeListener(focusOwnerListener, focusManager));
 * </pre>
 * </code>
 * <p/>
 * How does this work:
 * <p/>
 * Instead of registering propertyChangeListener directly to keyboardFocusManager, we wrap it inside
 * WeakPropertyChangeListener and register this weak listener to keyboardFocusManager. This weak listener acts a
 * delegate. It receives the propertyChangeEvents from keyboardFocusManager and delegates it the wrapped listener.
 * <p/>
 * The interesting part of this weak listener, it hold a weakReference to the original propertyChangeListener. so this
 * delegate is eligible for garbage collection which it is no longer reachable via references. When it gets garbage
 * collection, the weakReference will be pointing to null. On next propertyChangeEvent notification from
 * keyboardFocusManager, it find that the weakReference is pointing to null, and unregisters itself from
 * keyboardFocusManager. Thus the weak listener will also become eligible for garbage collection in next gc cycle.
 * <p/>
 * This concept is not something new. If you have a habit of looking into swing sources, you will find that
 * AbstractButton actually adds a weak listener to its action. The weak listener class used for this is :
 * javax.swing.AbstractActionPropertyChangeListener; This class is package-private, so you don't find it in javadoc.
 * <p/>
 * The full-fledged, generic implementation of weak listeners is available in Netbeans OpenAPI: WeakListeners.java . It
 * is worth to have a look at it.
 * 
 * @see <a href="http://www.jroller.com/santhosh/entry/use_weak_listeners_to_avoid">Weak listeners</a>
 * 
 * @author Santhosh Kumar T - santhosh@in.fiorano.com
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>: little modifications following a Java5
 *         approach, Bluebell code conventions and utility apis.
 */
public class WeakPropertyChangeListener implements PropertyChangeListener {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WeakPropertyChangeListener.class);

    /**
     * The weak reference to the wrapped listener.
     */
    final WeakReference<PropertyChangeListener> listenerRef;

    /**
     * Creates the listener given the wrapped instance.
     * 
     * @param listener
     *            the wrapped listener.
     */
    public WeakPropertyChangeListener(PropertyChangeListener listener) {

        this.listenerRef = new WeakReference<PropertyChangeListener>(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void propertyChange(PropertyChangeEvent evt) {

        final PropertyChangeListener listener = this.listenerRef.get();
        if (listener == null) {
            this.removeListener(evt.getSource());
        } else {
            listener.propertyChange(evt);
        }
    }

    /**
     * Uninstalls this from the target source when needed.
     * 
     * @param src
     *            the owner of the listener.
     */
    private void removeListener(Object src) {

        if (WeakPropertyChangeListener.LOGGER.isDebugEnabled()) {
            WeakPropertyChangeListener.LOGGER.debug("Removing unused listener " + this);
        }

        try {
            final String methodName = "removePropertyChangeListener";
            final Method method = ReflectionUtils.findMethod(src.getClass(), methodName, PropertyChangeListener.class);
            ReflectionUtils.invokeMethod(method, src, this);
        } catch (Throwable e) {
            if (WeakPropertyChangeListener.LOGGER.isDebugEnabled()) {
                WeakPropertyChangeListener.LOGGER.debug("Cannot remove listener: " + e);
            }
        }
    }
}
