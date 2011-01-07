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
package org.bluebell.richclient.util;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.Assert;

/**
 * Tests the correct behaviour of {@link ObjectUtils}.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestObjectUtils extends TestCase {

    /**
     * Tests the correct behaviour of shallow copy.
     */
    @Test
    public void testShallowCopy() {

        // Null source and null target
        try {
            ObjectUtils.shallowCopy(null, null);
            TestCase.fail("Null source and null target");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        // Null source and not null target
        try {
            ObjectUtils.shallowCopy(null, StringUtils.EMPTY);
            TestCase.fail("Null source");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        // Not null source and null target
        try {
            ObjectUtils.shallowCopy(StringUtils.EMPTY, null);
            TestCase.fail("Null target");
        } catch (IllegalArgumentException e) {
            TestCase.assertTrue(e.getMessage(), Boolean.TRUE);
        }

        // Primitive source and primitive target
        ObjectUtils.shallowCopy(0, 1);
        TestCase.assertTrue("Shallow copy between primitive types does nothing", Boolean.TRUE);

        // Shallow copy from objects of different classes: source is A and target is B
        final A a1 = new A("a1", "c1");
        final B b1 = new B("b1", "x!$%&_)(");

        ObjectUtils.shallowCopy(a1, b1);
        TestCase.assertEquals("a1", a1.a);
        TestCase.assertEquals("c1", a1.c);
        TestCase.assertEquals("b1", b1.b);
        TestCase.assertEquals("c1", b1.c);
        TestCase.assertTrue(a1.c == b1.c);

        // Shallow copy from objects of different classes: source is B and target is A
        // Note getter methods are ignored for copying internal state.
        final B b2 = new B("b2", "c2");
        final A a2 = new A("a2", "x!$%&_)(");

        ObjectUtils.shallowCopy(b2, a2);
        TestCase.assertEquals("b2", b2.b);
        TestCase.assertEquals("c2", b2.c);
        TestCase.assertEquals("a2", a2.a);
        TestCase.assertEquals("c2", a2.c);
        TestCase.assertTrue(a2.c == b2.c);

        // Shallow copy excluding a property
        final A a3 = new A("a3", "c3");
        final B b3 = new B("b3", "x!$%&_)(");

        ObjectUtils.shallowCopy(a3, b3, "x");
        TestCase.assertEquals("a3", a3.a);
        TestCase.assertEquals("c3", a3.c);
        TestCase.assertEquals("b3", b3.b);
        TestCase.assertEquals("x!$%&_)(", b3.c);
        TestCase.assertFalse(a3.c == b3.c);
    }

    /**
     * Tests the correct behaviour of unwrapping proxies.
     */
    @Test
    public void testUnwrapProxy() {

        final Integer maxIter = 10;
        final String string = StringUtils.EMPTY;

        // 1. Test recursive proxy unwrapping (one parameter method)
        for (int i = 0; i < maxIter; ++i) {
            final Object proxy = TestObjectUtils.createProxy(string, i);
            TestCase.assertSame(string, ObjectUtils.unwrapProxy(proxy));
        }

        // 2. Test recursive proxy unwrapping (two parameters method)
        for (int i = 0; i < maxIter; ++i) {
            final Object proxy = TestObjectUtils.createProxy(string, i);
            TestCase.assertSame(string, ObjectUtils.unwrapProxy(proxy, Boolean.TRUE));
        }

        // 3. Test non recursive proxy unwrapping
        final Object proxy = TestObjectUtils.createProxy(string, maxIter);
        Object unwrappedProxy = proxy;
        for (int i = 0; i < maxIter - 1; ++i) {
            unwrappedProxy = ObjectUtils.unwrapProxy(unwrappedProxy, Boolean.FALSE);
        }

        // 3.1. At this point proxies have been unwrapped maxIter - 1 times, so work is not yet done
        TestCase.assertNotSame(string, unwrappedProxy);

        unwrappedProxy = ObjectUtils.unwrapProxy(unwrappedProxy, Boolean.FALSE);

        // 3.2. At this point proxies have been unwrapped maxIter times, so work is already done
        TestCase.assertSame(string, unwrappedProxy);

        // 4. Null parameter
        TestCase.assertNull(ObjectUtils.unwrapProxy(null));
        TestCase.assertNull(ObjectUtils.unwrapProxy(null, Boolean.TRUE));
        TestCase.assertNull(ObjectUtils.unwrapProxy(null, Boolean.FALSE));
    }

    /**
     * Creates a chain of proxies starting with the given parameter and stopping when depth has been reached.
     * 
     * @param bean
     *            the target bean.
     * @param depth
     *            the expected depth.
     * @return the most outer proxy.
     */
    private static Object createProxy(Object bean, Integer depth) {

        Assert.notNull(bean, "bean");
        Assert.notNull(depth, "deep");

        return (depth > 0) ? TestObjectUtils.createProxy(new ProxyFactory(bean).getProxy(), --depth) : bean;
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
        private String a;

        /**
         * A shared property between <code>A</code> and <code>B</code>.
         */
        private String c;

        /**
         * Creates the DTO given its property values.
         * 
         * @param a
         *            the <code>a</code> property.
         * @param c
         *            the <code>c</code> property.
         */
        private A(String a, String c) {

            this();
            this.a = a;
            this.c = c;
        }

        /**
         * Default constructor.
         */
        private A() {

            this.foo();
        }

        /**
         * ObjectToFieldValueTransformer method to avoid checkstyle warning.
         */
        public void foo() {

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
        private String b;

        /**
         * A shared property between <code>A</code> and <code>B</code>.
         */
        private String c;

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
