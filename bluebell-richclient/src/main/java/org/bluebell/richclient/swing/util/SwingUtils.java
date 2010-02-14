/*
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
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

/**
 * 
 */
package org.bluebell.richclient.swing.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import org.bluebell.richclient.application.RcpMain;
import org.springframework.util.Assert;

/**
 * Low level swing related utils.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class SwingUtils {

    /**
     * Utility classes should not have a public or default constructor.
     */
    private SwingUtils() {

        super();
    }

    /**
     * Ensures the given runnable is executed in the event dispatcher thread and waits until executin is completed.
     * 
     * @param runnable
     *            the runnable.
     * 
     * @see #runInEventDispatcherThread(Runnable, Boolean)
     */
    public static void runInEventDispatcherThread(Runnable runnable) {

        SwingUtils.runInEventDispatcherThread(runnable, Boolean.TRUE);
    }

    /**
     * Ensures the given runnable is executed into the event dispatcher thread.
     * 
     * @param runnable
     *            the runnable.
     * @param wait
     *            whether should wait until execution is completed.
     */
    public static void runInEventDispatcherThread(Runnable runnable, Boolean wait) {

        Assert.notNull(runnable, "runnable");
        Assert.notNull(wait, "wait");

        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else if (wait) {
            try {
                EventQueue.invokeAndWait(runnable);
            } catch (InterruptedException e) {
                RcpMain.handleException(e);
            } catch (InvocationTargetException e) {
                RcpMain.handleException(e);
            }
        } else {
            EventQueue.invokeLater(runnable);
        }
    }

    /**
     * Does a pre-order search of a component with a given name.
     * 
     * @param name
     *            the name.
     * @param parent
     *            the root component in hierarchy.
     * @return the found component (may be null).
     */
    public static Component getDescendantNamed(String name, Component parent) {

        Assert.notNull(name, "name");
        Assert.notNull(parent, "parent");

        if (name.equals(parent.getName())) { // Base case
            return parent;
        } else if (parent instanceof Container) { // Recursive case
            for (final Component component : ((Container) parent).getComponents()) {

                final Component foundComponent = SwingUtils.getDescendantNamed(name, component);

                if (foundComponent != null) {
                    return foundComponent;
                }
            }
        }

        return null;
    }
}
