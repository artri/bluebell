/*
 * Copyright 2002-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CachingMapDecorator;
import org.springframework.util.ReflectionUtils;

/**
 * Helper implementation for a reflective visitor. Mainly for internal use within the framework.
 * 
 * <p>
 * To use, call <code>invokeVisit</code>, passing a Visitor object and the data argument to accept (double-dispatch).
 * For example:
 * 
 * <pre>
 *   public String styleValue(Object value) {
 *     reflectiveVistorSupport.invokeVisit(this, value)
 *   }
 *  
 *   // visit call back will be invoked via reflection
 *   String visit(&lt;valueType&gt; arg) {
 *     // process argument of type &lt;valueType&gt;
 *   }
 * </pre>
 * 
 * See the {@link org.springframework.core.style.DefaultValueStyler} class for a concrete usage of this visitor helper.
 * 
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>: copied from
 *         org.springframework:org.springframework.core:2.5.6.A since from 3.0.0-RELEASE this file has intentionally
 *         been removed.
 * 
 * @since 1.2.2
 * @deprecated as of Spring 2.5, to be removed in Spring 3.0
 */
@Deprecated
public class ReflectiveVisitorHelper {

    /**
     * The visit method name.
     */
    private static final String VISIT_METHOD = "visit";

    /**
     * The visit method name if parameter is null.
     */
    private static final String VISIT_NULL = "visitNull";

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectiveVisitorHelper.class);

    /**
     * A cache of visited methods.
     */
    private final CachingMapDecorator<Class<?>, ClassVisitMethods> visitMethods = new BbCachingMapDecorator();

    /**
     * Use reflection to call the appropriate <code>visit</code> method on the provided visitor, passing in the
     * specified argument.
     * 
     * @param visitor
     *            the visitor encapsulating the logic to process the argument
     * @param argument
     *            the argument to dispatch
     * @return the value returned by <code>visit</code>.
     */
    public Object invokeVisit(Object visitor, Object argument) {

        Assert.notNull(visitor, "The visitor to visit is required");
        // Perform call back on the visitor through reflection.
        Method method = this.getMethod(visitor.getClass(), argument);
        if (method == null) {
            if (ReflectiveVisitorHelper.LOGGER.isWarnEnabled()) {
                ReflectiveVisitorHelper.LOGGER.warn("No method found by reflection for visitor class ["
                        + visitor.getClass().getName() + "] and argument of type ["
                        + (argument != null ? argument.getClass().getName() : "") + "]");
            }
            return null;
        }
        try {
            Object[] args = null;
            if (argument != null) {
                args = new Object[] { argument };
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                method.setAccessible(true);
            }
            return method.invoke(visitor, args);
        } catch (Exception ex) {
            ReflectionUtils.handleReflectionException(ex);
            throw new IllegalStateException("Should never get here");
        }
    }

    /**
     * Determines the most appropriate visit method for the given visitor class and argument.
     * 
     * @param visitorClass
     *            the visitor class.
     * @param argument
     *            the argument.
     * 
     * @return the most appropiate visit method.
     */
    private Method getMethod(Class<?> visitorClass, Object argument) {

        ClassVisitMethods classVisitMethods = this.visitMethods.get(visitorClass);
        return classVisitMethods.getVisitMethod(argument != null ? argument.getClass() : null);
    }

    /**
     * Converted from anonymous to inner class.
     * 
     * <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private static final class BbCachingMapDecorator extends CachingMapDecorator<Class<?>, ClassVisitMethods> {
        /**
         * It's a <code>Serializable</code> class.
         */
        private static final long serialVersionUID = -1422147070362246795L;

        @Override
        public ClassVisitMethods create(Class<?> key) {

            return new ClassVisitMethods(key);
        }
    }

    /**
     * Internal class caching visitor methods by argument class.
     */
    private static final class ClassVisitMethods implements Serializable {

        /**
         *  It's a <code>Serializable</code> class.
         */
        private static final long serialVersionUID = -5820091475402486972L;

        /**
         * The visitor class.
         */
        private final Class<?> visitorClass;

        /**
         * The cache of visited methods.
         */
        private final CachingMapDecorator<Class<?>, Method> visitMethodCache = //
        new CachingMapDecorator<Class<?>, Method>() {
            /**
             * It's a <code>Serializable</code> class.
             */
            private static final long serialVersionUID = -1690397441136615094L;

            /**
             * {@inheritDoc}
             */
            @Override
            public Method create(Class<?> argumentClazz) {

                if (argumentClazz == null) {
                    return ClassVisitMethods.this.findNullVisitorMethod();
                }
                Method method = ClassVisitMethods.this.findVisitMethod(argumentClazz);
                if (method == null) {
                    method = ClassVisitMethods.this.findDefaultVisitMethod();
                }
                return method;
            }
        };

        /**
         * A constructor.
         * 
         * @param visitorClass
         *            the visitor class.
         */
        private ClassVisitMethods(Class<?> visitorClass) {

            this.visitorClass = visitorClass;
        }

        /**
         * Finds the null visitor method.
         * 
         * @return the method.
         */
        private Method findNullVisitorMethod() {

            for (Class<?> clazz = this.visitorClass; clazz != null; clazz = clazz.getSuperclass()) {
                try {
                    return clazz.getDeclaredMethod(ReflectiveVisitorHelper.VISIT_NULL, (Class[]) null);
                } catch (NoSuchMethodException ex) {
                    ex.getMessage(); // "Avoid CS warnings"
                }
            }
            return this.findDefaultVisitMethod();
        }

        /**
         * Finds the default visit method.
         * 
         * @return the method.
         */
        private Method findDefaultVisitMethod() {

            final Class<?>[] args = { Object.class };
            for (Class<?> clazz = this.visitorClass; clazz != null; clazz = clazz.getSuperclass()) {
                try {
                    return clazz.getDeclaredMethod(ReflectiveVisitorHelper.VISIT_METHOD, args);
                } catch (NoSuchMethodException ex) {
                    ex.getMessage(); // "Avoid CS warnings"
                }
            }
            if (ReflectiveVisitorHelper.LOGGER.isWarnEnabled()) {
                ReflectiveVisitorHelper.LOGGER.warn("No default '" + ReflectiveVisitorHelper.VISIT_METHOD
                        + "' method found. Returning <null>.");
            }
            return null;
        }

        /**
         * Gets a cached visitor method for the specified argument type.
         * 
         * @param argumentClass
         *            the argument class.
         * @return a cached visitor method.
         */
        private Method getVisitMethod(Class<?> argumentClass) {

            return this.visitMethodCache.get(argumentClass);
        }

        /**
         * Traverses class hierarchy looking for applicable visit() method.
         * 
         * @param rootArgumentType
         *            the root argument type.
         * 
         * @return an applicable visit() method.
         */
        private Method findVisitMethod(Class<?> rootArgumentType) {

            if (rootArgumentType == Object.class) {
                return null;
            }
            final LinkedList<Class<?>> classQueue = new LinkedList<Class<?>>();
            classQueue.addFirst(rootArgumentType);

            while (!classQueue.isEmpty()) {
                final Class<?> argumentType = classQueue.removeLast();
                // Check for a visit method on the visitor class matching this
                // argument type.
                try {
                    if (ReflectiveVisitorHelper.LOGGER.isTraceEnabled()) {
                        ReflectiveVisitorHelper.LOGGER.trace("Looking for method "
                                + ReflectiveVisitorHelper.VISIT_METHOD + "(" + argumentType + ")");
                    }
                    return this.findVisitMethod(this.visitorClass, argumentType);
                } catch (NoSuchMethodException e) {
                    // Queue up the argument super class if it's not of type
                    // Object.
                    if (!argumentType.isInterface() && (argumentType.getSuperclass() != Object.class)) {
                        classQueue.addFirst(argumentType.getSuperclass());
                    }
                    // Queue up argument's implemented interfaces.
                    final Class<?>[] interfaces = argumentType.getInterfaces();
                    for (int i = 0; i < interfaces.length; i++) {
                        classQueue.addFirst(interfaces[i]);
                    }
                }
            }
            // No specific method found -> return the default.
            return this.findDefaultVisitMethod();
        }

        /**
         * Finds the more suitable visit method for an argument type.
         * 
         * @param visitorClass
         *            the visitor method.
         * @param argumentType
         *            the argument type.
         * @return the method.
         * @throws NoSuchMethodException
         *             if method not found.
         */
        private Method findVisitMethod(Class<?> visitorClass, Class<?> argumentType) throws NoSuchMethodException {

            try {
                return visitorClass.getDeclaredMethod(ReflectiveVisitorHelper.VISIT_METHOD,
                        new Class<?>[] { argumentType });
            } catch (NoSuchMethodException ex) {
                // Try visitorClass superclasses.
                if (visitorClass.getSuperclass() != Object.class) {
                    return this.findVisitMethod(visitorClass.getSuperclass(), argumentType);
                } else {
                    throw ex;
                }
            }
        }
    }

}
