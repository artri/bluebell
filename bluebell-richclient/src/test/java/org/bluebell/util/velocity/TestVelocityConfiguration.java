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
package org.bluebell.util.velocity;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.velocity.app.VelocityEngine;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.ui.velocity.VelocityEngineUtils;

/**
 * Tests the correct behaviour of Velocity template engine configuration.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration
public class TestVelocityConfiguration extends AbstractJUnit4SpringContextTests {

    /**
     * The velocity engine implementation to be tested.
     */
    @Autowired
    private VelocityEngine velocityEngine;

    /**
     * Tests the correct behaviour of dependency injection.
     */
    @Test
    public void testDependencyInjection() {

        TestCase.assertNotNull("velocityEngine", velocityEngine);
    }

    /**
     * Simple test that evaluates the correct behaviour of velocity configuration.
     */
    @Test
    public void testVelocityConfiguration() {

        final String expectedValue = "expectedValue";
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("var", expectedValue);

        final String str = VelocityEngineUtils.mergeTemplateIntoString(//
                this.velocityEngine, "template.vm", "UTF-8", model);

        TestCase.assertEquals(expectedValue, str);
    }
}
