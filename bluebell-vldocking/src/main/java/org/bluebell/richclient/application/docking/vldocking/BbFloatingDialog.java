/*
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
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

package org.bluebell.richclient.application.docking.vldocking;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.lang.reflect.Field;

import org.springframework.util.ReflectionUtils;

import com.vlsolutions.swing.docking.FloatingDialog;

/**
 * Floating dialog implementation that employs a single color for the resizer. Activation simply changes widget title
 * pane and borders, not the background itself.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbFloatingDialog extends FloatingDialog {

    /**
     * This is a <code>Serializable</code> class.
     */
    private static final long serialVersionUID = 1567088727707662859L;

    /**
     * The <code>activeCaptionColor</code> field from {@link FloatingDialog}.
     */
    private static final Field ACTIVE_CAPTION_COLOR_FIELD = ReflectionUtils.findField(//
            FloatingDialog.class, "activeCaptionColor");
    /**
     * The <code>inactiveCaptionColor</code> field from {@link FloatingDialog}.
     */
    private static final Field INACTIVE_CAPTION_COLOR_FIELD = ReflectionUtils.findField(//
            FloatingDialog.class, "inactiveCaptionColor");

    static {
        ReflectionUtils.makeAccessible(BbFloatingDialog.ACTIVE_CAPTION_COLOR_FIELD);
        ReflectionUtils.makeAccessible(BbFloatingDialog.INACTIVE_CAPTION_COLOR_FIELD);
    }

    /**
     * Creates the dialog given its parent dialog.
     * 
     * @param parent
     *            the parent dialog.
     */
    public BbFloatingDialog(Dialog parent) {

        super(parent);
    }

    /**
     * Creates the dialog given its parent frame.
     * 
     * @param parent
     *            the parent frame.
     */
    public BbFloatingDialog(Frame parent) {

        super(parent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Changes the active caption color field before calling super.
     */
    public void init() {

        final Color backgroundColor = VLDockingUtils.DockingColor.BACKGROUND.getColor();
        ReflectionUtils.setField(BbFloatingDialog.ACTIVE_CAPTION_COLOR_FIELD, this, backgroundColor);
        ReflectionUtils.setField(BbFloatingDialog.INACTIVE_CAPTION_COLOR_FIELD, this, backgroundColor);

        super.init();
    }
}
