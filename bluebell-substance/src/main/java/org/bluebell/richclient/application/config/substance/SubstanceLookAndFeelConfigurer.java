/*
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell Substance.
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
package org.bluebell.richclient.application.config.substance;

import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.bluebell.richclient.swing.util.SwingUtils;
import org.jvnet.lafwidget.LafWidget;
import org.jvnet.lafwidget.animation.FadeConfigurationManager;
import org.jvnet.lafwidget.animation.FadeKind;
import org.jvnet.lafwidget.preview.DefaultPreviewPainter;
import org.jvnet.lafwidget.tabbed.DefaultTabPreviewPainter;
import org.jvnet.lafwidget.utils.LafConstants.TabOverviewKind;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceConstants.TabContentPaneBorderKind;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.richclient.application.config.UIManagerConfigurer;
import org.springframework.util.Assert;

/**
 * Substance look and feel configurer.
 * <p>
 * The simplest configuration requires the following bean in the startup aplication context:
 * 
 * <pre>
 *      &lt;bean id=&quot;substanceLookAndFeelConfigurer&quot; 
 *              class=&quot;org.bluebell.richclient.application.config.SubstanceLookAndFeelConfigurer&quot; &gt;
 *              &lt;constructor-arg index=&quot;0&quot; 
 *              value=&quot;org.jvnet.substance.skin.SubstanceCremeCoffeeLookAndFeel&quot;/&gt;
 *      &lt;/bean&gt;
 * </pre>
 * 
 * However is better to define the following beans in the startup application context:
 * 
 * <pre>
 *      &lt;aop:aspectj-autoproxy/&gt; 
 * 
 *      &lt;bean id=&quot;splashScreen&quot;
 *              class=&quot;org.springframework.richclient.application.splash.ProgressSplashScreen&quot; /&gt;
 * 
 *      &lt;util:property-path id=&quot;progressMonitor&quot; path=&quot;splashScreen.progressMonitor&quot;/&gt;
 * 
 *      &lt;bean id=&quot;substanceLookAndFeelConfigurer&quot; 
 *              class=&quot;org.bluebell.richclient.application.config.SubstanceLookAndFeelConfigurer&quot;
 *              p:proxy-bean-ref=&quot;progressMonitor&quot;&gt;
 *              &lt;constructor-arg index=&quot;0&quot; 
 *              value=&quot;org.jvnet.substance.skin.SubstanceCremeCoffeeLookAndFeel&quot;/&gt;
 *      &lt;/bean&gt;
 * </pre>
 * <p>
 * The extra beans are needed to intercept progress monitor operations and execute them into the Event Dispatcher
 * Thread. Since progress monitor is not a bean an additional work is needed in order to intercept invocations:
 * <ol>
 * <li>Declare it as a bean: <code>progressMonitor</code> bean.
 * <li>Once it's already a bean intecept required operations: {@link #progressMonitorOperation()}.
 * <li>Replace the original collaborator with the proxied one:
 * {@link #replaceProgressMonitorAdvice(ProceedingJoinPoint)}.
 * </ol>
 * 
 * @see <a href="http://forum.springsource.org/showthread.php?p=263453">Post this class is based on</a>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
@Aspect
public class SubstanceLookAndFeelConfigurer extends UIManagerConfigurer implements InitializingBean {

    /**
     * The progress monitor proxy bean.
     */
    private Object progressMonitorProxyBean;

    /**
     * The image locations.
     */
    private Map<String, Resource> imageLocations;

    /**
     * Creates the look and feel configurer.
     * <p>
     * Specifying look and feel name in constructor ensures look and feel is stablished before splash screen is shown.
     * 
     * @param lookAndFeelName
     *            the look and feel name to be used.
     */
    public SubstanceLookAndFeelConfigurer(String lookAndFeelName) {

        super();
        this.setLookAndFeel(lookAndFeelName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLookAndFeel(final String className) {

        SwingUtils.runInEventDispatcherThread(new Runnable() {
            public void run() {

                SubstanceLookAndFeelConfigurer.super.setLookAndFeel(className);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLookAndFeelWithName(final String lookAndFeelName) {

        SwingUtils.runInEventDispatcherThread(new Runnable() {
            public void run() {

                SubstanceLookAndFeelConfigurer.super.setLookAndFeelWithName(lookAndFeelName);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {

        for (Map.Entry<String, Resource> entry : this.getImageLocations().entrySet()) {
            final String imageKey = entry.getKey();

            try {
                final ImageIcon icon = new ImageIcon(entry.getValue().getURL());
                UIManager.put(imageKey, icon);
            } catch (Exception e) {
                new String("Avoid CS warnings");
                // Nothing to do
            }
        }
    }

    /**
     * Gets the imageLocations.
     * 
     * @return the imageLocations
     */
    public Map<String, Resource> getImageLocations() {

        return this.imageLocations;
    }

    /**
     * Sets the imageLocations.
     * 
     * @param imageLocations
     *            the imageLocations to set
     */
    public void setImageLocations(Map<String, Resource> imageLocations) {

        Assert.notNull(imageLocations, "imageLocations");

        this.imageLocations = imageLocations;
    }

    /**
     * Sets the progress monitor proxy bean.
     * 
     * @param progressMonitorProxyBean
     *            the proxy bean to set.
     */
    public void setProgressMonitorProxyBean(Object progressMonitorProxyBean) {

        Assert.notNull(progressMonitorProxyBean, "progressMonitorProxyBean");

        this.progressMonitorProxyBean = progressMonitorProxyBean;
    }

    /**
     * Pointcut that intercepts progress monitor <code>taskStarted</code> events.
     * <p>
     * This method does nothing.
     */
    @Pointcut("execution(* org.springframework.richclient.progress.ProgressMonitor.*(..))")
    protected final void progressMonitorOperation() {

    }

    /**
     * Pointcut that intercepts getting progress monitor from monitoring splash screen.
     * <p>
     * This method does nothing.
     */
    @Pointcut("execution(* org.springframework.richclient.application.splash.MonitoringSplashScreen."
            + "getProgressMonitor(..))")
    protected final void monitoringSplashScreenGetProgressMonitorOperation() {

    }

    /**
     * Pointcut that intercepts splash screen <code>splash</code> invocations.
     * <p>
     * This method does nothing.
     */
    @Pointcut("execution(* org.springframework.richclient.application.splash.SplashScreen.*(..))")
    protected final void splashScreenOperation() {

    }

    /**
     * This advice ensures the intercepted method is executed in the EDT.
     * 
     * @param pjp
     *            the proceeding join point.
     * @return the value returned by the intercepted method.
     * @throws Throwable
     *             in case of error.
     */
    @Around("progressMonitorOperation()")
    public final Object doInEventDispatcherThread(final ProceedingJoinPoint pjp) throws Throwable {

        final Object[] returnValue = new Object[1];
        final Throwable[] throwable = new Throwable[1];

        SwingUtils.runInEventDispatcherThread(new Runnable() {
            public void run() {

                try {
                    returnValue[0] = pjp.proceed();
                } catch (final Throwable e) {
                    throwable[0] = e;
                }
            }
        });

        // Re-throw throwable
        if (throwable[0] != null) {
            throw throwable[0];
        }

        return returnValue[0];
    }

    /**
     * This advice replaces the object returned by the target method with the proxy bean instantiated by the bean
     * factory, capable of being intercepted using AOP mechanism.
     * 
     * @param pjp
     *            the proceeding join point.
     * @return the appropiate object.
     * @throws Throwable
     *             in case of error.
     * 
     * @see #getProgressMonitorProxyBean()
     */
    @Around("monitoringSplashScreenGetProgressMonitorOperation()")
    public final Object replaceProgressMonitorAdvice(final ProceedingJoinPoint pjp) throws Throwable {

        final Object returnValue = this.doInEventDispatcherThread(pjp);

        return (this.getProgressMonitorProxyBean() != null) ? this.getProgressMonitorProxyBean() : returnValue;
    }

    /**
     * This advice adapts Substance to splash screen displaying.
     * 
     * @param pjp
     *            the proceeding join point.
     * @throws Throwable
     *             in case of error.
     */
    @Around("splashScreenOperation()")
    public final void splashAdvice(final ProceedingJoinPoint pjp) throws Throwable {

        final Boolean defaultLookAndFeelDecorated = JFrame.isDefaultLookAndFeelDecorated();
        JFrame.setDefaultLookAndFeelDecorated(Boolean.FALSE);
        pjp.proceed();
        JFrame.setDefaultLookAndFeelDecorated(defaultLookAndFeelDecorated);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Proceeds according to <a href="https://substance.dev.java.net/docs/faq.html">Substance FAQ</a>:
     * <p>
     * <dl>
     * <dt>Question 16: How do i make Substance to paint the title panes?
     * <dd>In case you wish to use cross-platform frame and dialog decorations, use the following before you instantiate
     * your first top-level window: <code>JFrame.setDefaultLookAndFeelDecorated(true);</code> and
     * <code>JDialog.setDefaultLookAndFeelDecorated(true);</code>. This, however, causes flicker on resize due to a
     * known Swing bug. For Windows, there is a workaround, using
     * <code>System.setProperty("sun.awt.noerasebackground", "true");</code>. You can set this property along with the
     * above two lines.
     * </dl>
     */
    @Override
    protected void doInstallCustomDefaults() throws Exception {

        // https://substance.dev.java.net/docs/faq.html
        JFrame.setDefaultLookAndFeelDecorated(Boolean.TRUE);
        JDialog.setDefaultLookAndFeelDecorated(Boolean.TRUE);
        System.setProperty("sun.awt.noerasebackground", "true");

        // LafWidget
        UIManager.put(LafWidget.AUTO_SCROLL, Boolean.TRUE);
        UIManager.put(LafWidget.COMPONENT_PREVIEW_PAINTER, new DefaultPreviewPainter());
        UIManager.put(LafWidget.TABBED_PANE_PREVIEW_PAINTER, new DefaultTabPreviewPainter() {
            @Override
            public TabOverviewKind getOverviewKind(JTabbedPane tabPane) {

                return TabOverviewKind.ROUND_CAROUSEL;
            }
        });

        // Substance
        UIManager.put(SubstanceLookAndFeel.SHOW_EXTRA_WIDGETS, Boolean.TRUE);
        UIManager.put(SubstanceLookAndFeel.PASSWORD_ECHO_PER_CHAR, 2);
        UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_PLACEMENT);
        UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CLOSE_BUTTONS_PROPERTY, Boolean.FALSE);

        // Fade
        FadeConfigurationManager.getInstance().allowFades(FadeKind.GHOSTING_BUTTON_PRESS);
        FadeConfigurationManager.getInstance().allowFades(FadeKind.GHOSTING_ICON_ROLLOVER);
        FadeConfigurationManager.getInstance().allowFades(FadeKind.SELECTION);
    }

    /**
     * Gets the progress monitor proxy bean.
     * 
     * @return the progress monitor proxy bean.
     */
    private Object getProgressMonitorProxyBean() {

        return this.progressMonitorProxyBean;
    }
}
