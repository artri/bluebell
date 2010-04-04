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
package org.bluebell.richclient.util;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * Tests the correct behaviour of {@link ObjectUtil}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestObjectUtil extends TestCase {

    /**
     * Tests the correct behaviour of shallow copy.
     */
    @Test
    public void testShallowCopy() {

        // Null source and null target
        try {
            ObjectUtil.shallowCopy(null, null);
            TestCase.fail("Null source and null target");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        // Null source and not null target
        try {
            ObjectUtil.shallowCopy(null, StringUtils.EMPTY);
            TestCase.fail("Null source");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        // Not null source and null target
        try {
            ObjectUtil.shallowCopy(StringUtils.EMPTY, null);
            TestCase.fail("Null target");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        // Primitive source and primitive target
        ObjectUtil.shallowCopy(5, 6);
        TestCase.assertTrue("Shallow copy between primitive types does nothing", Boolean.TRUE);

        // Shallow copy from objects of different classes: source is A and target is B
        final A a1 = new A("a1", "c1");
        final B b1 = new B("b1", "x!$%&_)(");

        ObjectUtil.shallowCopy(a1, b1);
        TestCase.assertEquals("a1", a1.a);
        TestCase.assertEquals("c1", a1.c);
        TestCase.assertEquals("b1", b1.b);
        TestCase.assertEquals("c1", b1.c);
        TestCase.assertTrue(a1.c == b1.c);

        // Shallow copy from objects of different classes: source is B and target is A
        // Note getter methods are ignored for copying internal state.
        final B b2 = new B("b2", "c2");
        final A a2 = new A("a2", "x!$%&_)(");

        ObjectUtil.shallowCopy(b2, a2);
        TestCase.assertEquals("b2", b2.b);
        TestCase.assertEquals("c2", b2.c);
        TestCase.assertEquals("a2", a2.a);
        TestCase.assertEquals("c2", a2.c);
        TestCase.assertTrue(a2.c == b2.c);
    }

    /**
     * A DTO useful for testing, without property accessors.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static final class A {

        /**
         * A exclusive property.
         */
        protected String a;

        /**
         * A shared property between <code>A</code> and <code>B</code>.
         */
        protected String c;

        /**
         * Creates the DTO given its property values.
         * 
         * @param a
         *            the <code>a</code> property.
         * @param c
         *            the <code>c</code> property.
         */
        public A(String a, String c) {

            this.a = a;
            this.c = c;
        }
    }

    /**
     * A DTO useful for testing, without property accessors.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static final class B {

        /**
         * A exclusive property.
         */
        protected String b;

        /**
         * A shared property between <code>A</code> and <code>B</code>.
         */
        protected String c;

        /**
         * Creates the DTO given its property values.
         * 
         * @param b
         *            the <code>b</code> property.
         * @param c
         *            the <code>c</code> property.
         */
        public B(String b, String c) {

            this.b = b;
            this.c = c;
        }

        /**
         * A fake getter method for property <code>c</code> that always returns the empty string.
         * 
         * @return the empty string.
         */
        public String getC() {

            return StringUtils.EMPTY;
        }
    }
}
