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

package org.bluebell.richclient.application.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.bluebell.richclient.swing.util.SwingUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.richclient.application.config.UIManagerConfigurer;
import org.springframework.util.Assert;

/**
 * Bluebell look and feel configurer. Install look and feel in the event dispatcher thread, also installs the image
 * locations into the <code>UIManager</code>.
 * <p>
 * This is the simplest configuration:
 * 
 * <pre>
 *      &lt;bean id=&quot;bbLookAndFeelConfigurer&quot; 
 *              class=&quot;org.bluebell.richclient.application.config.BbLookAndFeelConfigurer&quot; &gt;             
 *      &lt;/bean&gt;
 * </pre>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbLookAndFeelConfigurer extends UIManagerConfigurer implements InitializingBean, PropertyChangeListener {

    /**
     * The image locations.
     */
    private Map<String, Resource> imageLocations;

    /**
     * Creates the look and feel configurer.
     * <p>
     * Note this doesn't install pre packaged UI managed defaults.
     */
    public BbLookAndFeelConfigurer() {

        super(Boolean.FALSE);

        UIManager.addPropertyChangeListener(this);
        // SwingUtils.weakPropertyChangeListener(this));
    }

    /**
     * Creates the look and feel configurer.
     * <p>
     * Specifying look and feel name in constructor ensures look and feel is stablished before splash screen is shown.
     * 
     * @param lookAndFeelName
     *            the look and feel name to be used.
     */
    public BbLookAndFeelConfigurer(String lookAndFeelName) {

        this();
        this.setLookAndFeel(lookAndFeelName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLookAndFeel(final String className) {

        SwingUtils.runInEventDispatcherThread(new Runnable() {
            public void run() {

                BbLookAndFeelConfigurer.super.setLookAndFeel(className);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLookAndFeelWithName(final String lookAndFeelName) {

        SwingUtils.runInEventDispatcherThread(new Runnable() {
            public void run() {

                BbLookAndFeelConfigurer.super.setLookAndFeelWithName(lookAndFeelName);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {

        for (Map.Entry<String, Resource> entry : this.getImageLocations().entrySet()) {
            final String imageKey = entry.getKey();

            try {
                final ImageIcon icon = new ImageIcon(entry.getValue().getURL());
                UIManager.put(imageKey, icon);
            } catch (Exception e) {
                new String("Avoid CS warnings");
                // Nothing to do
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void propertyChange(PropertyChangeEvent evt) {

        if ("lookAndFeel".equals(evt.getPropertyName())) {
            this.onLookAndFeelChange((LookAndFeel) evt.getOldValue(), (LookAndFeel) evt.getNewValue());
        }
    }

    /**
     * Gets the image locations.
     * 
     * @return the imageLocations to set.
     */
    public Map<String, Resource> getImageLocations() {

        return this.imageLocations;
    }

    /**
     * Sets the image locations.
     * 
     * @param imageLocations
     *            the image locations to set.
     */
    public void setImageLocations(Map<String, Resource> imageLocations) {

        Assert.notNull(imageLocations, "imageLocations");

        this.imageLocations = imageLocations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInstallCustomDefaults() throws Exception {

        System.setProperty("sun.awt.noerasebackground", "true");
    }

    /**
     * Allow subclasses be aware of look and feel changes.
     * 
     * @param oldLookAndFeel
     *            the old look and feel.
     * @param newLookAndFeel
     *            the new look and feel.
     */
    protected void onLookAndFeelChange(LookAndFeel oldLookAndFeel, LookAndFeel newLookAndFeel) {

    }
}
