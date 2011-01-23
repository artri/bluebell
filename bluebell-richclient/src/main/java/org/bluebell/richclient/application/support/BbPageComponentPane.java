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

package org.bluebell.richclient.application.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.bluebell.richclient.swing.util.SwingUtils;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageComponentPane;
import org.springframework.richclient.components.SimpleInternalFrame;
import org.springframework.richclient.factory.AbstractControlFactory;
import org.springframework.util.Assert;

/**
 * A <code>PageComponentPane</code> that puts the <code>PageComponent</code> inside a <code>SimpleInternalFrame</code>.
 * <p />
 * Original implementation has been modified in order to prevent memory leaks after registering listeners.
 * 
 * @author Peter De Bruycker
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>: original implementation adapted to
 *         employ a weak property change listener and follow Bluebell code conventions.
 * 
 * @see org.springframework.richclient.application.support.DefaultPageComponentPane
 */
public class BbPageComponentPane extends AbstractControlFactory implements PageComponentPane, PropertyChangeListener {

    /**
     * The page component.
     */
    private PageComponent pageComponent;

    /**
     * Creates the pane given its associated page component.
     * 
     * @param pageComponent
     *            the page componet.
     */
    public BbPageComponentPane(PageComponent pageComponent) {

        this.setPageComponent(pageComponent);
        this.getPageComponent().addPropertyChangeListener(SwingUtils.weakPropertyChangeListener(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final PageComponent getPageComponent() {

        return this.pageComponent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        final SimpleInternalFrame frame = (SimpleInternalFrame) this.getControl();
        frame.setTitle(this.getPageComponent().getDisplayName());
        frame.setFrameIcon(this.getPageComponent().getIcon());
        frame.setToolTipText(this.getPageComponent().getCaption());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent createControl() {

        final SimpleInternalFrame control = new SimpleInternalFrame(//
                this.getPageComponent().getIcon(), //
                this.getPageComponent().getDisplayName(), //
                this.createViewToolBar(), //
                this.getPageComponent().getControl());

        return control;
    }

    /**
     * Creates the tool bar for this page component.
     * <p />
     * This implementation returns <code>null</code>.
     * 
     * @return the created tool bar.
     */
    protected JToolBar createViewToolBar() {

        return null;
    }

    /**
     * Sets the pageComponent.
     * 
     * @param pageComponent
     *            the pageComponent to set.
     */
    private void setPageComponent(PageComponent pageComponent) {

        Assert.notNull(pageComponent, "pageComponent");

        this.pageComponent = pageComponent;
    }
}
