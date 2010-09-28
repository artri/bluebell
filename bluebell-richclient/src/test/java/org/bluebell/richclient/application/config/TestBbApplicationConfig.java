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
package org.bluebell.richclient.application.config;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.PriorityOrdered;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Tests the correct behaviour of {@link BbApplicationConfig}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration()
public class TestBbApplicationConfig extends AbstractJUnit4SpringContextTests {

    /**
     * 
     */
    private static final String LOWEST_PRECEDENCE_PPC_VALUE = "lowest_precedence";

    /**
     * 
     */
    private static final String INTERMEDIATE_PRECEDENCE_PPC_VALUE = "intermediate_precedence";

    /**
     * 
     */
    private static final String HIGHEST_PRECEDENCE_PPC_VALUE = "highest_precedence";

    /**
     * The order of the highest precedence PPC.
     */
    public static Integer HIGHEST_PRECEDENCE = PriorityOrdered.HIGHEST_PRECEDENCE + 1;

    /**
     * The order of an intermediate precedence PPC.
     */
    public static Integer INTERMEDIATE_PRECEDENCE = 2;

    /**
     * The order of the lowest precedence PPC.
     */
    public static Integer LOWEST_PRECEDENCE = PriorityOrdered.LOWEST_PRECEDENCE;

    /**
     * The name of the first bean.
     */
    private static String BEAN_NAME_1 = "bean1";

    /**
     * The name of the second bean.
     */
    private static String BEAN_NAME_2 = "bean2";

    /**
     * The name of the third bean.
     */
    private static String BEAN_NAME_3 = "bean3";

    /**
     * The retrieved application applicationConfig.
     */
    @Resource
    private Map<String, List<String[]>> applicationConfig;

    /**
     * A placeholder to be tested.
     */
    @Autowired
    private String bean1;

    /**
     * A placeholder to be tested.
     */
    @Autowired
    private String bean2;

    /**
     * A placeholder to be tested.
     */
    @Autowired
    private String bean3;

    /**
     * Test the correct behaviour of dependency injection.
     */
    @Test
    public void testDependencyInjection() {

        TestCase.assertNotNull(this.bean1);
        TestCase.assertNotNull(this.bean2);
        TestCase.assertNotNull(this.bean3);
        TestCase.assertNotNull(this.applicationConfig);
    }

    /**
     * Tests the correct behaviour of PPC's priority ordering.
     */
    @Test
    public void testPPCOrder() {

        TestCase.assertEquals(TestBbApplicationConfig.HIGHEST_PRECEDENCE_PPC_VALUE, bean1);
        TestCase.assertEquals(TestBbApplicationConfig.INTERMEDIATE_PRECEDENCE_PPC_VALUE, bean2);
        TestCase.assertEquals(TestBbApplicationConfig.LOWEST_PRECEDENCE_PPC_VALUE, bean3);
    }

    /**
     * Tests the correct behaviour of the validations done by {@link BbApplicationConfig#getValue(Map, String, Integer)}
     * .
     */
    @Test
    public void testGetValueInput() {

        // Application config is null
        try {
            BbApplicationConfig.getValue(null, "something", 0);
            TestCase.fail("BbApplicationConfig.getValue(null, \"something\", 0);");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        // Placeholder is null
        try {
            BbApplicationConfig.getValue(this.applicationConfig, null, 0);
            TestCase.fail("BbApplicationConfig.getValue(this.applicationConfig, null, 0);");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        // Placeholder is empty
        try {
            BbApplicationConfig.getValue(this.applicationConfig, StringUtils.EMPTY, 0);
            TestCase.fail("BbApplicationConfig.getValue(this.applicationConfig, StringUtils.EMPTY, 0);");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        // Precedence is less than 0
        try {
            BbApplicationConfig.getValue(this.applicationConfig, StringUtils.EMPTY, -1);
            TestCase.fail("BbApplicationConfig.getValue(this.applicationConfig, StringUtils.EMPTY, -1);");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }
    }

    /**
     * Tests the correct behaviour of{@link BbApplicationConfig#getValue(Map, String, Integer)}.
     */
    @Test
    public void testGetValue() {

        // Bean1
        TestCase.assertEquals(TestBbApplicationConfig.HIGHEST_PRECEDENCE_PPC_VALUE, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, 0));
        TestCase.assertEquals(TestBbApplicationConfig.INTERMEDIATE_PRECEDENCE_PPC_VALUE, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, 1));
        TestCase.assertEquals(TestBbApplicationConfig.LOWEST_PRECEDENCE_PPC_VALUE, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, 2));
        TestCase.assertEquals(null,//
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, 3));

        // Bean2
        TestCase.assertEquals(TestBbApplicationConfig.INTERMEDIATE_PRECEDENCE_PPC_VALUE, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, 0));
        TestCase.assertEquals(TestBbApplicationConfig.LOWEST_PRECEDENCE_PPC_VALUE, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, 1));
        TestCase.assertEquals(null, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, 2));

        // Bean2
        TestCase.assertEquals(TestBbApplicationConfig.LOWEST_PRECEDENCE_PPC_VALUE, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, 0));
        TestCase.assertEquals(null, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, 1));
    }

    /**
     * Tests the correct behaviour of the factory bean retrieved map.
     */
    @Test
    public void testApplicationConfig() {

    }

    @Test
    public void testFoo() {

        System.out.println(BbApplicationConfig.debugPrint(this.applicationConfig));
    }

}
