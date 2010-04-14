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
package org.bluebell.richclient.application.support;

import java.awt.Image;

import javax.swing.JComponent;

import org.bluebell.richclient.swing.util.SwingUtils;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.util.Assert;

/**
 * A page consisting on an image.
 * <p>
 * If no image is specified then employs the application default image <code>Application.instance().getImage()</code>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ImageBackedView extends AbstractView {

    /**
     * The image.
     */
    private Image image;

    /**
     * Sets the image.
     * 
     * @param image
     *            the image to set.
     */
    public void setImage(Image image) {

        Assert.notNull(image, "image");

        this.image = image;
    }

    /**
     * Gets the image.
     * 
     * @return the image.
     */
    public Image getImage() {

        if (image == null) {
            this.setImage(Application.instance().getImage());
        }

        return this.image;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent createControl() {

        return SwingUtils.generateComponent(this.getImage());
    }
}
