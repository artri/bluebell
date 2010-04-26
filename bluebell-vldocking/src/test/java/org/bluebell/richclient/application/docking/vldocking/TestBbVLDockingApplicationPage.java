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

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.swing.JComponent;

import junit.framework.TestCase;

import org.bluebell.richclient.application.ApplicationPageException;
import org.bluebell.richclient.application.support.DefaultApplicationPageConfigurer;
import org.bluebell.richclient.application.support.DefaultApplicationPageConfigurer.BbViewType;
import org.bluebell.richclient.samples.simple.form.PersonChildForm;
import org.bluebell.richclient.samples.simple.form.PersonSearchForm;
import org.bluebell.richclient.test.AbstractBbSamplesTests;
import org.junit.Before;
import org.junit.Test;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageDescriptor;
import org.springframework.richclient.application.support.MultiViewPageDescriptor;
import org.springframework.richclient.exceptionhandling.AbstractLoggingExceptionHandler;
import org.springframework.richclient.exceptionhandling.RegisterableExceptionHandler;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.test.context.ContextConfiguration;

/**
 * 
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@ContextConfiguration
public class TestBbVLDockingApplicationPage extends AbstractBbSamplesTests {

    /**
     * The default application page configurer implementation.
     */
    @Resource
    private DefaultApplicationPageConfigurer<?> defaultApplicationPageConfigurer;

    /**
     * A page descriptor containing a failed view descriptor.
     * 
     * @see #testCreateControlThrowsException()
     */
    @Resource
    private PageDescriptor failedViewPageDescriptor;

    /**
     * A page descriptor with a valid explicit layout set.
     * 
     * @see #testBuildValidExplicitInitialLayout()
     */
    @Resource
    private PageDescriptor validExplicitLayoutPageDescriptor;

    /**
     * A page descriptor with an invalid explicit layout set.
     * 
     * @see #testBuildInvalidExplicitInitialLayout()
     */
    @Resource
    private PageDescriptor invalidExplicitLayoutPageDescriptor;

    /**
     * A page descriptor with a valid implicit layout.
     * 
     * @see #testBuildValidImplicitInitialLayout()
     */
    @Resource
    private PageDescriptor validImplicitLayoutPageDescriptor;

    /**
     * A page descriptor with an invalid implicit layout.
     * 
     * @see #testBuildInvalidImplicitInitialLayout()
     */
    @Resource
    private PageDescriptor invalidImplicitLayoutPageDescriptor;

    /**
     * A page descriptor with no components.
     * 
     * @see #testBuildEmptyPageAutoLayout()
     */
    @Resource
    private PageDescriptor emptyPageDescriptor;

    /**
     * A page descriptor with multiple components, including to detail view descriptors and two search vies descriptors.
     * 
     * @see #testBuildWholePageAutoLayout()
     */
    @Resource
    private PageDescriptor wholePageDescriptor;

    /**
     * {@inheritDoc}
     */
    @Test
    public void testDependencyInjection() {

        super.testDependencyInjection();

        TestCase.assertNotNull(this.defaultApplicationPageConfigurer);
        TestCase.assertNotNull(this.failedViewPageDescriptor);
        TestCase.assertNotNull(this.validExplicitLayoutPageDescriptor);
        TestCase.assertNotNull(this.invalidExplicitLayoutPageDescriptor);
        TestCase.assertNotNull(this.validImplicitLayoutPageDescriptor);
        TestCase.assertNotNull(this.invalidImplicitLayoutPageDescriptor);
        TestCase.assertNotNull(this.emptyPageDescriptor);
        TestCase.assertNotNull(this.wholePageDescriptor);
    }

    /**
     * TODO test perspective saving.
     */

    /**
     * Tests page control creation throws an exception when there is a failure during any page component control
     * creation.
     */
    @Test
    public void testCreateControlFailed() {

        final String pageId = this.failedViewPageDescriptor.getId();

        // Raise page control creation
        this.initializeVariables(this.failedViewPageDescriptor);

        // Ensure expected exception is thrown
        final Throwable cause = RememberExceptionHandler.getLastThrowable();
        TestCase.assertNotNull("cause", cause);
        TestCase.assertTrue(cause instanceof ApplicationPageException);
        TestCase.assertEquals(((ApplicationPageException) cause).getPageId(), pageId);

        // Ensure employed layout is the expected one
        final BbVLDockingApplicationPage<?> vlPage = (BbVLDockingApplicationPage<?>) this.getApplicationPage();
        TestCase.assertEquals(vlPage.getAutoLayout(), vlPage.getLayout());
    }

    /**
     * Tests everything works fine after trying to build a page with a valid explicit layout.
     */
    @Test
    public void testBuildValidExplicitInitialLayout() {

        // Raise page control creation
        this.initializeVariables(this.validExplicitLayoutPageDescriptor);

        // Ensure no exception is thrown
        final Throwable cause = RememberExceptionHandler.getLastThrowable();
        TestCase.assertNull("cause", cause);

        // Ensure employed layout is the expected one
        final BbVLDockingApplicationPage<?> vlPage = (BbVLDockingApplicationPage<?>) this.getApplicationPage();
        TestCase.assertEquals(vlPage.getInitialLayout(), vlPage.getLayout());
    }

    /**
     * Tests non exception is thrown after trying to build a non existing explicit layout.
     */
    @Test
    public void testBuildInvalidExplicitInitialLayout() {

        // Raise page control creation
        this.initializeVariables(this.invalidExplicitLayoutPageDescriptor);

        // Ensure no exception is thrown
        final Throwable cause = RememberExceptionHandler.getLastThrowable();
        TestCase.assertNull("cause", cause);

        // Ensure employed layout is the expected one
        final BbVLDockingApplicationPage<?> vlPage = (BbVLDockingApplicationPage<?>) this.getApplicationPage();
        TestCase.assertEquals(vlPage.getAutoLayout(), vlPage.getLayout());
    }

    /**
     * Tests everything works fine after trying to build a page with a valid implicit layout.
     */
    @Test
    public void testBuildValidImplicitInitialLayout() {

        // Raise page control creation
        this.initializeVariables(this.validImplicitLayoutPageDescriptor);

        // Ensure no exception is thrown
        final Throwable cause = RememberExceptionHandler.getLastThrowable();
        TestCase.assertNull("cause", cause);

        // Ensure employed layout is the expected one
        final BbVLDockingApplicationPage<?> vlPage = (BbVLDockingApplicationPage<?>) this.getApplicationPage();
        TestCase.assertEquals(vlPage.getInitialLayout(), vlPage.getLayout());
    }

    /**
     * Tests everything works fine after trying to build a page with an invalid implicit layout.
     */
    @Test
    public void testBuildInvalidImplicitInitialLayout() {

        // Raise page control creation
        this.initializeVariables(this.invalidImplicitLayoutPageDescriptor);

        // Ensure no exception is thrown
        final Throwable cause = RememberExceptionHandler.getLastThrowable();
        TestCase.assertNull("cause", cause);

        // Ensure employed layout is the expected one
        final BbVLDockingApplicationPage<?> vlPage = (BbVLDockingApplicationPage<?>) this.getApplicationPage();
        TestCase.assertEquals(vlPage.getAutoLayout(), vlPage.getLayout());
    }

    /**
     * Tests everything works fine after trying to build an empty page with an auto layout.
     */
    @Test
    public void testBuildEmptyPageAutoLayout() {

        // Raise page control creation
        this.initializeVariables(this.emptyPageDescriptor);

        // Ensure this page description is really empty
        final MultiViewPageDescriptor multiViewPageDescriptor = (MultiViewPageDescriptor) this.emptyPageDescriptor;
        TestCase.assertTrue(multiViewPageDescriptor.getViewDescriptors().isEmpty());

        // Ensure no exception is thrown
        final Throwable cause = RememberExceptionHandler.getLastThrowable();
        TestCase.assertNull("cause", cause);

        // Ensure employed layout is the expected one
        final BbVLDockingApplicationPage<?> vlPage = (BbVLDockingApplicationPage<?>) this.getApplicationPage();
        TestCase.assertEquals(vlPage.getAutoLayout(), vlPage.getLayout());
    }

    /**
     * Tests everything works fine after trying to build an whole page with an auto layout.
     */
    @Test
    public void testBuildWholePageAutoLayout() {

        // Raise page control creation
        this.initializeVariables(this.wholePageDescriptor);

        // Ensure this page description is really whole
        final Map<String, List<? extends PageComponent>> classification = //
        this.defaultApplicationPageConfigurer.classifyApplicationPage(this.getApplicationPage());

        TestCase.assertTrue(classification.get(BbViewType.DETAIL.name()).size() == 2);
        TestCase.assertTrue(classification.get(BbViewType.SEARCH.name()).size() == 2);

        // Ensure no exception is thrown
        final Throwable cause = RememberExceptionHandler.getLastThrowable();
        TestCase.assertNull("cause", cause);

        // Ensure employed layout is the expected one
        final BbVLDockingApplicationPage<?> vlPage = (BbVLDockingApplicationPage<?>) this.getApplicationPage();
        TestCase.assertEquals(vlPage.getAutoLayout(), vlPage.getLayout());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        super.afterPropertiesSet();

        final RegisterableExceptionHandler exceptionHandler = //
        Application.instance().getLifecycleAdvisor().getRegisterableExceptionHandler();

        TestCase.assertTrue("exceptionHandler instanceof RethrowRegisterableExceptionHandler", //
                exceptionHandler instanceof RememberExceptionHandler);
    }

    /**
     * Ensures last exception thrown is <code>null</code> before test execution.
     */
    @Before
    public final void resetLastThrowable() {

        TestBbVLDockingApplicationPage.RememberExceptionHandler.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeVariables(PageDescriptor pageDescriptor) {

        super.initializeVariables(pageDescriptor);
    }

    /**
     * Form implementation equals to {@link PersonChildForm} except on identifier.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class PersonDetailFormBis extends PersonChildForm {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getId() {

            return PersonDetailFormBis.class.getName();
        }
    }

    /**
     * Form implementation equals to {@link PersonSearchForm} except on identifier.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class PersonSearchFormBis extends PersonSearchForm {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getId() {

            return PersonSearchFormBis.class.getName();
        }
    }

    /**
     * Form implementation that raises an exception during form control creation.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class FailedForm extends AbstractForm {

        /**
         * {@inheritDoc}
         */
        @Override
        protected JComponent createFormControl() {

            throw new IllegalStateException("IllegalStateException forced");
        }
    }

    /**
     * Registerable exception handler that remembers last exception thrown.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static class RememberExceptionHandler extends AbstractLoggingExceptionHandler {

        /**
         * The last throwable thrown.
         */
        private static Throwable lastThrowable;

        /**
         * {@inheritDoc}
         */
        @Override
        public void notifyUserAboutException(Thread thread, Throwable throwable) {

            RememberExceptionHandler.lastThrowable = throwable;
        }

        /**
         * Gets the last throwable thrown.
         * 
         * @return the last throwable thrown.
         */
        public static Throwable getLastThrowable() {

            return RememberExceptionHandler.lastThrowable;
        }

        /**
         * Resets the last throwable thrown.
         */
        public static void reset() {

            RememberExceptionHandler.lastThrowable = null;
        }
    }
}
