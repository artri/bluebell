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

/**
 * 
 */
package org.bluebell.richclient.application.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.PriorityOrdered;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.MultiValueMap;

/**
 * Tests the correct behaviour of {@link BbApplicationConfig}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration()
public class TestBbApplicationConfig extends AbstractJUnit4SpringContextTests {

    /**
     * Placeholder values retrieved by this application context test.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public enum PropertyValues {
        /**
         * The <b>L</b>owest value.
         */
        L,
        /**
         * The <b>I</b>ntermediate value.
         */
        I,
        /**
         * The <b>H</b>ighest value.
         */
        H
    };

    /**
     * The order of the highest precedence PPC.
     */
    public static final Integer HIGHEST_PRECEDENCE = PriorityOrdered.HIGHEST_PRECEDENCE + 1;

    /**
     * The order of an intermediate precedence PPC.
     */
    public static final Integer INTERMEDIATE_PRECEDENCE = 2;

    /**
     * The order of the lowest precedence PPC.
     */
    public static final Integer LOWEST_PRECEDENCE = PriorityOrdered.LOWEST_PRECEDENCE;

    /**
     * The name of the first bean.
     */
    private static final String BEAN_NAME_1 = "bean1";

    /**
     * The name of the second bean.
     */
    private static final String BEAN_NAME_2 = "bean2";

    /**
     * The name of the third bean.
     */
    private static final String BEAN_NAME_3 = "bean3";

    /**
     * The retrieved application applicationConfig.
     */
    @Resource
    private MultiValueMap<String, String[]> applicationConfig;

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
     * A placeholder to be tested.
     */
    @Autowired
    private String bean4;

    /**
     * A placeholder to be tested.
     */
    @Autowired
    private String bean5;

    /**
     * Test the correct behaviour of dependency injection.
     */
    @Test
    public void testDependencyInjection() {

        TestCase.assertNotNull(this.bean1);
        TestCase.assertNotNull(this.bean2);
        TestCase.assertNotNull(this.bean3);
        TestCase.assertNotNull(this.bean4);
        TestCase.assertNotNull(this.bean5);

        TestCase.assertNotNull(this.applicationConfig);
    }

    /**
     * Tests the correct behaviour of PPC's priority ordering.
     */
    @Test
    public void testPPCOrder() {

        TestCase.assertEquals(PropertyValues.H.name(), this.bean1);
        TestCase.assertEquals(PropertyValues.I.name(), this.bean2);
        TestCase.assertEquals(PropertyValues.L.name(), this.bean3);
        TestCase.assertEquals(this.bean1.concat(this.bean2), this.bean4);
        TestCase.assertEquals(PropertyValues.H.name(), this.bean5);
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
     * Tests the correct behaviour of {@link BbApplicationConfig#getValue(Map, String, Integer)}.
     */
    @Test
    public void testGetValue() {

        int pos = 0;

        // Bean1
        TestCase.assertEquals(PropertyValues.H.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, pos++));
        TestCase.assertEquals(PropertyValues.H.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, pos++));
        TestCase.assertEquals(PropertyValues.I.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, pos++));
        TestCase.assertEquals(PropertyValues.I.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, pos++));
        TestCase.assertEquals(PropertyValues.L.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, pos++));
        TestCase.assertEquals(PropertyValues.L.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, pos++));
        TestCase.assertEquals(null, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, pos++));

        // Bean2
        pos = 0;
        TestCase.assertEquals(PropertyValues.I.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_2, pos++));
        TestCase.assertEquals(PropertyValues.I.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_2, pos++));
        TestCase.assertEquals(PropertyValues.L.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_2, pos++));
        TestCase.assertEquals(PropertyValues.L.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_2, pos++));
        TestCase.assertEquals(null, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_2, pos++));

        // Bean3
        pos = 0;
        TestCase.assertEquals(PropertyValues.L.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_3, pos++));
        TestCase.assertEquals(PropertyValues.L.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_3, pos++));
        TestCase.assertEquals(null, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_3, pos++));

        // Bean3
        pos = 0;
        TestCase.assertEquals(PropertyValues.L.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_3, pos++));
        TestCase.assertEquals(PropertyValues.L.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_3, pos++));
        TestCase.assertEquals(null, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_3, pos++));
    }

    /**
     * Tests the correct behaviour of {@link BbApplicationConfig#debugPrint(Map)}.
     */
    @Test
    public void testDebugPrint() {

        final Set<Map.Entry<String, List<String[]>>> entrySetBefore = this.applicationConfig.entrySet();
        BbApplicationConfig.debugPrint(this.applicationConfig);
        final Set<Map.Entry<String, List<String[]>>> entrySetAfter = this.applicationConfig.entrySet();

        // Checks map is not changed during printing
        SetUtils.isEqualSet(entrySetBefore, entrySetAfter);
    }

    /**
     * Test the correct behaviour of {@link BbApplicationConfig} when there are duplicated placeholders.
     */
    @Test
    public void testDuplicatedPlaceholder() {

        final int noPpc = 6;

        // ${bean1} appears three times
        TestCase.assertEquals(PropertyValues.L.name(), //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, noPpc - 1));
        TestCase.assertEquals(null, //
                BbApplicationConfig.getValue(this.applicationConfig, TestBbApplicationConfig.BEAN_NAME_1, noPpc));
    }

    /**
     * Test the correct behaviour of {@link BbApplicationConfig} when there are duplicated placeholders.
     */
    @Test
    public void testFoo() {

        this.applicationConfig.getFirst(TestBbApplicationConfig.BEAN_NAME_1);
        this.applicationConfig.getFirst(TestBbApplicationConfig.BEAN_NAME_2);
        this.applicationConfig.getFirst(TestBbApplicationConfig.BEAN_NAME_3);
    }

}
