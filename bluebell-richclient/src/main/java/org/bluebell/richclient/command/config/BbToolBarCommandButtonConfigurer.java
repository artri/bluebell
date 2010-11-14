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

package org.bluebell.richclient.command.config;

import java.awt.Insets;

import javax.swing.AbstractButton;

import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.config.CommandFaceDescriptor;
import org.springframework.richclient.command.config.ToolBarCommandButtonConfigurer;
import org.springframework.util.Assert;

/**
 * Custom <code>CommandButtonConfigurer</code> for buttons on the toolbar.
 * <p>
 * Allows using large icons for toolbar.
 * 
 * @see http://forum.springsource.org/showthread.php?p=265779
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbToolBarCommandButtonConfigurer extends ToolBarCommandButtonConfigurer {

    /**
     * Indicates if large icons should be used.
     */
    private Boolean useLargeIcons;

    /**
     * Creates this command button configurer.
     */
    public BbToolBarCommandButtonConfigurer() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    public void configure(AbstractButton button, AbstractCommand command, CommandFaceDescriptor faceDescriptor) {

        super.configure(button, command, faceDescriptor);
        faceDescriptor.configureIconInfo(button, this.getUseLargeIcons());

        if (this.isTextBelowIcon() && this.isShowText()) {
            final Insets margin = button.getMargin();
            button.setMargin(new Insets(margin.top, 0, margin.bottom, 0));
        }
    }

    /**
     * Gets the useLargeIcons.
     * 
     * @return the useLargeIcons
     */
    public Boolean getUseLargeIcons() {

        if (this.useLargeIcons == null) {
            this.setUseLargeIcons(Boolean.TRUE);
        }

        return this.useLargeIcons;
    }

    /**
     * Sets the useLargeIcons.
     * 
     * @param useLargeIcons
     *            the useLargeIcons to set
     */
    public void setUseLargeIcons(Boolean useLargeIcons) {

        Assert.notNull(useLargeIcons, "useLargeIcons");

        this.useLargeIcons = useLargeIcons;
    }
}
