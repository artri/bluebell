/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.NOPTransformer;
import org.apache.commons.collections.map.TransformedMap;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * FactoryBean that creates a Map with String keys and Resource values from properties, interpreting passed-in String
 * values as resource locations.
 * 
 * <p>
 * Extends PropertiesFactoryBean to inherit the capability of defining local properties and loading from properties
 * files.
 * 
 * <p>
 * Implements the ResourceLoaderAware interface to automatically use the context ResourceLoader if running in an
 * ApplicationContext. Uses DefaultResourceLoader else.
 * 
 * @author Juergen Hoeller
 * @author Keith Donald
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>: copied from
 *         org.springframework:org.springframework.context:2.5.6.A since 3.0.0-RELEASE has intentionally forgotten this
 *         file.
 * @since 1.0.2
 * @see org.springframework.core.io.DefaultResourceLoader
 * @see <a href="http://forum.springsource.org/showthread.php?t=75807">Spring Forum Issue Report</a>
 */
public class ResourceMapFactoryBean extends PropertiesFactoryBean implements ResourceLoaderAware, Transformer {

    /**
     * A base path to prepend to each resource location value in the properties file.
     */
    private String resourceBasePath = "";

    /**
     * The resource loader to be used.
     */
    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    /**
     * Set a base path to prepend to each resource location value in the properties file.
     * <p>
     * E.g.: resourceBasePath="/images", value="/test.gif" -> location="/images/test.gif"
     */
    public void setResourceBasePath(String resourceBasePath) {

	this.resourceBasePath = (resourceBasePath != null ? resourceBasePath : "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {

	this.resourceLoader = (resourceLoader != null) ? resourceLoader : new DefaultResourceLoader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Properties> getObjectType() {

	return Properties.class;
    }

    /**
     * Fetch the Resource handle for the given location, prepeding the resource base path.
     * 
     * @param location
     *            the resource location
     * @return the Resource handle
     * @see org.springframework.core.io.ResourceLoader#getResource(String)
     */
    @Override
    public Object transform(Object location) {
    
        return this.resourceLoader.getResource(this.resourceBasePath + location);
    }

    /**
     * Create the Map instance, populated with keys and Resource values.
     */
    @Override
    protected Properties mergeProperties() throws IOException {

	return MapUtils.toProperties(//
		TransformedMap.decorateTransform(super.mergeProperties(), NOPTransformer.getInstance(), this));

    }
}
