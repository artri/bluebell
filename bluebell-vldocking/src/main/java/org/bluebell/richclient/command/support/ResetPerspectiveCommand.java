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

package org.bluebell.richclient.command.support;

import org.bluebell.richclient.application.docking.vldocking.BbVLDockingApplicationPage;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.command.ActionCommand;

/**
 * Command responsible for resetting current page layout.
 * <p>
 * To be used requires creating the command into the application context and reference it, for instance at the tool bar.
 * 
 * <pre>
 * &lt;bean id=&quot;resetPerspectiveCommand&quot;
 *  class=&quot;org.bluebell.richclient.command.support.ResetPerspectiveCommand&quot; /&gt;
 * </pre>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ResetPerspectiveCommand extends ActionCommand {

    /**
     * The command id.
     */
    private static final String COMMAND_ID = "resetPerspectiveCommand";

    /**
     * Constructs the command using the default command id.
     */
    public ResetPerspectiveCommand() {

        super(ResetPerspectiveCommand.COMMAND_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doExecuteCommand() {

        // TODO (JAF), 20100325, may rename this class and package

        final ApplicationPage page = Application.instance().getActiveWindow().getPage();

        if (page instanceof BbVLDockingApplicationPage<?>) {
            ((BbVLDockingApplicationPage<?>) page).resetLayout();
        }
    }
}
