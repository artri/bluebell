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

import java.util.HashMap;
import java.util.Map;

import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.support.DefaultApplicationPage;
import org.springframework.richclient.application.support.DefaultApplicationPageFactory;
import org.springframework.util.Assert;

/**
 * Application page factory implementation for <code>DefaultApplicationPage</code> with caching capabilities.
 * <p>
 * This implementation is based on <code>VLDockingApplicationPageFactory</code>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbApplicationPageFactory extends DefaultApplicationPageFactory {

    /**
     * Whether to reuse pages.
     */
    private Boolean reusePages = Boolean.TRUE;

    /**
     * The page cache.
     */
    private Map<ApplicationWindow, Map<String, ApplicationPage>> pageCache = //
    new HashMap<ApplicationWindow, Map<String, ApplicationPage>>();

    /**
     * {@inheritDoc}
     */
    public ApplicationPage createApplicationPage(ApplicationWindow window, PageDescriptor descriptor) {

        ApplicationPage page;

        if (this.isReusePages()) {
            page = this.findPage(window, descriptor);
            if (page != null) {
                return page;
            }
        }

        page = new DefaultApplicationPage(window, descriptor);       
        if (this.isReusePages()) {
            this.cachePage(page);
        }

        return page;
    }

    /**
     * Sets whether to reuse pages.
     * 
     * @param reusePages
     *            the flag to set.
     */
    public void setReusePages(Boolean reusePages) {

        Assert.notNull(reusePages, "reusePages");

        this.reusePages = reusePages;
    }

    /**
     * Tries to find a page in the cache given its descriptor.
     * 
     * @param window
     *            the application window the page belongs to.
     * @param descriptor
     *            the page descriptor.
     * @return the page, may be <code>null</code> if not found.
     */
    protected final ApplicationPage findPage(ApplicationWindow window, PageDescriptor descriptor) {

        Assert.notNull(window, "window");
        Assert.notNull(descriptor, "descriptor");

        final Map<String, ApplicationPage> pages = (Map<String, ApplicationPage>) this.pageCache.get(window);

        return (pages != null) ? pages.get(descriptor.getId()) : null;
    }

    /**
     * Caches a page.
     * 
     * @param page
     *            the page.
     */
    protected final void cachePage(ApplicationPage page) {

        Assert.notNull(page, "page");

        Map<String, ApplicationPage> pages = (Map<String, ApplicationPage>) this.pageCache.get(page.getWindow());
        if (pages == null) {
            pages = new HashMap<String, ApplicationPage>();
            this.pageCache.put(page.getWindow(), pages);
        }

        pages.put(page.getId(), page);
    }

    /**
     * Gets whether to reuse pages.
     * 
     * @return the flag.
     */
    private Boolean isReusePages() {

        return this.reusePages;
    }
}
