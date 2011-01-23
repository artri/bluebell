/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell Substance.
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
package org.bluebell.richclient.application.config.substance;

import java.util.Map;

import org.bluebell.richclient.application.config.vldocking.WidgetDesktopStyle;
import org.bluebell.richclient.application.docking.vldocking.substance.ui.SubstanceDockViewTitleBarUI;

/**
 * Substance specialization of <code>WidgetDesktopStyle</code>.
 * 
 * <p>
 * This implementation is based on <a href="http://forum.springsource.org/showthread.php?t=73183">Spring Forum
 * comments</a>.
 */
public class SubstanceWidgetDesktopStyle extends WidgetDesktopStyle {

    /**
     * Creates the style.
     */
    public SubstanceWidgetDesktopStyle() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> getDefaultsMap() {

        final Map<String, Object> defaults = super.getDefaultsMap();
        defaults.put("DockViewTitleBarUI", SubstanceDockViewTitleBarUI.class.getName());

        return defaults;
    }

}
