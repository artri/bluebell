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

/**
 * 
 */
package org.bluebell.richclient.application.docking.vldocking;

import junit.framework.TestCase;

import org.bluebell.richclient.application.support.DefaultApplicationPageConfigurer;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestBbVLDockingApplicationPage extends AbstractBbSamplesTests {

    /**
     * The default application page configurer implementation.
     */
    @Autowired
    public DefaultApplicationPageConfigurer<?> defaultApplicationPageConfigurer;

    /**
     * {@inheritDoc}
     */
    @Test
    public void testDependencyInjection() {

        super.testDependencyInjection();

        TestCase.assertNotNull(this.defaultApplicationPageConfigurer);
    }

    /**
     * TODO test perspective saving.
     */
    @Test
    public void todo() {

    }

    @Test
    public void testVelocity() {

        // TODO, (JAF), 20100410, review this test

        // this.initializeVariables(this.getPersonPageDescriptor());
        //        
        // final Map<String, List<? extends PageComponent>> classification = //
        // this.defaultApplicationPageConfigurer.classifyApplicationPage(this.getApplicationPage());
        //
        // final Map<String, Object> context = new HashMap<String, Object>();
        // context.put("classification", classification);
        // context.put("MASTER_TYPE", DefaultApplicationPageConfigurer.BbViewType.MASTER.name());
        // context.put("DETAIL_TYPE", DefaultApplicationPageConfigurer.BbViewType.DETAIL.name());
        // context.put("SEARCH_TYPE", DefaultApplicationPageConfigurer.BbViewType.SEARCH.name());
        // context.put("VALIDATION_TYPE", DefaultApplicationPageConfigurer.BbViewType.VALIDATION.name());
        // context.put("UNKNOWN_TYPE", DefaultApplicationPageConfigurer.BbViewType.UNKNOWN.name());
        //        
        //
        // final StringBuffer sb = BbVLDockingApplicationPage.velocityTest(context);
        // System.out.println(sb);
    }

}
