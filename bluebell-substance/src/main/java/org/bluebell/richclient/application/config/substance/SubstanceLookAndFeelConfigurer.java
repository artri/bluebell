/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
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

import java.awt.Color;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.bluebell.richclient.application.config.vldocking.VLDockingLookAndFeelConfigurer;
import org.bluebell.richclient.application.docking.vldocking.VLDockingUtils;
import org.bluebell.richclient.swing.util.SwingUtils;
import org.pushingpixels.lafwidget.LafWidget;
import org.pushingpixels.lafwidget.animation.AnimationConfigurationManager;
import org.pushingpixels.lafwidget.animation.AnimationFacet;
import org.pushingpixels.lafwidget.preview.DefaultPreviewPainter;
import org.pushingpixels.lafwidget.tabbed.DefaultTabPreviewPainter;
import org.pushingpixels.lafwidget.utils.LafConstants.TabOverviewKind;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceConstants.TabContentPaneBorderKind;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.skin.DustSkin;
import org.pushingpixels.substance.api.skin.SkinChangeListener;
import org.springframework.util.Assert;

/**
 * Substance look and feel configurer.
 * <p>
 * The simplest configuration requires the following bean in the startup aplication context:
 * 
 * <pre>
 *      <bean id="substanceLookAndFeelConfigurer" parent="vldockingLookAndFeelConfigurer"
 *                 class="org.bluebell.richclient.application.config.substance.SubstanceLookAndFeelConfigurer" lazy-init="true">
 *                 <constructor-arg index="0" value="${richclient.substanceSkinName}" />
 *         </bean>
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
 *        <bean id="substanceLookAndFeelConfigurer" parent="vldockingLookAndFeelConfigurer"
 *                 class="org.bluebell.richclient.application.config.substance.SubstanceLookAndFeelConfigurer" lazy-init="true">
 *                 <constructor-arg index="0" value="${richclient.substanceSkinName}" />
 *                 <property name="progressMonitorProxyBean">
 *                         <util:property-path id="progressMonitor" path="splashScreen.progressMonitor" />
 *                 </property>
 *         </bean>
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
public class SubstanceLookAndFeelConfigurer extends VLDockingLookAndFeelConfigurer implements SkinChangeListener {

    // TODO, (JAF), 20101123, aspect related methods should be externalized into other class and registered in a
    // bluebell-aspects-context.xml like XML file

    /**
     * The progress monitor proxy bean.
     */
    private Object progressMonitorProxyBean;

    /**
     * The default Substance skin to be set if failure or not provided.
     */
    public static final String DEFAULT_SUBSTANCE_SKIN = DustSkin.class.getName();

    /**
     * Creates the look and feel configurer given a valid Substance skin name.
     * <p>
     * Specifying skin name in constructor ensures look and feel is stablished before splash screen is shown.
     * <p>
     * Since Substance 6.1 version the skin must be set compulsory. This class proceeds according to
     * <code>SubstanceLookAndFeel</code> javadoc:
     * 
     * <quote>Call {@link SubstanceLookAndFeel#setSkin(String)} or {@link SubstanceLookAndFeel#setSkin(SubstanceSkin)}
     * static methods. These methods do not require Substance to be the current look-and-feel.</quote>
     * <p>
     * <b>Note</b> this constructor receives a <code>String</code> with the Substance skin name instead of look and feel
     * class name as usual (in parent class).
     * 
     * @param skinName
     *            the skin name to be used.
     */
    public SubstanceLookAndFeelConfigurer(final String skinName) {

        super();

        Assert.notNull(skinName, "skinName");

        SwingUtils.runInEventDispatcherThread(new Runnable() {
            public void run() {

                SubstanceLookAndFeel.setSkin(skinName);

                /*
                 * See TestSubstanceLookAndFeel that demonstrate there is a little bug on Substante Look and Feel
                 * versión 6.1 that force to call {@link SubstanceLookAndFeel#setSkin(String)} twice.
                 */
                // (JAF), 20101122, false alarm, substance-jide (v5.0) have to be excluded from classpath since it is
                // not compatible
                // SubstanceLookAndFeel.setSkin(skinName);
            }
        });

        SubstanceLookAndFeel.registerSkinChangeListener(this);
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
     * {@inheritDoc}
     */
    public final void skinChanged() {

        this.installColors();

        this.installWidgetDesktopStyle();
    }

    /**
     * Pointcut that intercepts getting progress monitor from monitoring splash screen.
     * <p>
     * This method does nothing.
     */
    @Pointcut("execution(* org.springframework.richclient.application.splash.MonitoringSplashScreen."
            + "getProgressMonitor(..))")
    protected void monitoringSplashScreenGetProgressMonitorOperation() {

    }

    /**
     * Pointcut that intercepts progress monitor <code>taskStarted</code> events.
     * <p>
     * This method does nothing.
     */
    @Pointcut("execution(* org.springframework.richclient.progress.ProgressMonitor.*(..))")
    protected void progressMonitorOperation() {

    }

    /**
     * Pointcut that intercepts splash screen <code>splash</code> invocations.
     * <p>
     * This method does nothing.
     */
    @Pointcut("execution(* org.springframework.richclient.application.splash.SplashScreen.*(..))")
    protected void splashScreenOperation() {

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
     * Also proceeds according to <a href="https://substance.dev.java.net/docs/faq.html">Substance FAQ</a>:
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

        super.doInstallCustomDefaults();

        // https://substance.dev.java.net/docs/faq.html
        JFrame.setDefaultLookAndFeelDecorated(Boolean.TRUE);
        JDialog.setDefaultLookAndFeelDecorated(Boolean.TRUE);

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

        /*
         * (JAF), 20101115, with Substance v5.x:
         */
        // FadeConfigurationManager.getInstance().allowFades(FadeKind.GHOSTING_BUTTON_PRESS);
        // FadeConfigurationManager.getInstance().allowFades(FadeKind.GHOSTING_ICON_ROLLOVER);
        // FadeConfigurationManager.getInstance().allowFades(FadeKind.SELECTION);

        /*
         * (JAF), 20101115, with Substance v6.x
         */
        AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.ARM);
        AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.FOCUS);
        AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.FOCUS_LOOP_ANIMATION);
        // FIXME, (JAF), 20101125, Ghosting_Button_Press and Ghosting_Icon_Rollover does not work fine when jide-oss is
        // in classpath (http://jirabluebell.b2b2000.com/browse/BLUE-33)
        // AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.GHOSTING_BUTTON_PRESS);
        // AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.GHOSTING_ICON_ROLLOVER);
        AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.ICON_GLOW);
        AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.PRESS);
        AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.ROLLOVER);
        AnimationConfigurationManager.getInstance().allowAnimations(AnimationFacet.SELECTION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void installColors() {

        final SubstanceSkin skin = SubstanceLookAndFeel.getCurrentSkin();

        if (skin != null) {
            this.installColors(skin);
        }
    }

    /**
     * Installs the VLDocking colors for the given skin.
     * 
     * @param skin
     *            the substance skin.
     * 
     * @see VLDockingUtils.DockingColor
     */
    private void installColors(SubstanceSkin skin) {

        Assert.notNull(skin, "skin");

        /*
         * (JAF), 20101115, Substance version updated to 6.1.
         * 
         * Background compatibility has been broken
         */
        // final Color shadow = skin.getMainDefaultColorScheme().getDarkColor();
        final Color shadow = skin.getEnabledColorScheme(DecorationAreaType.NONE).getDarkColor();
        // final Color highlight = skin.getMainDefaultColorScheme().getLightColor();
        final Color highlight = skin.getEnabledColorScheme(DecorationAreaType.NONE).getLightColor();
        // final Color background = skin.getMainDefaultColorScheme().getBackgroundFillColor();
        final Color background = skin.getEnabledColorScheme(DecorationAreaType.NONE).getBackgroundFillColor();
        // final Color active = skin.getMainActiveColorScheme().getMidColor();
        final Color active = skin.getActiveColorScheme(DecorationAreaType.NONE).getMidColor();
        // final Color inactive = skin.getMainDefaultColorScheme().getMidColor();
        final Color inactive = skin.getEnabledColorScheme(DecorationAreaType.NONE).getMidColor();

        // Widget style colors
        UIManager.put(VLDockingUtils.DockingColor.BACKGROUND.getKey(), background);
        UIManager.put(VLDockingUtils.DockingColor.SHADOW.getKey(), shadow);
        UIManager.put(VLDockingUtils.DockingColor.HIGHLIGHT.getKey(), highlight);
        UIManager.put(VLDockingUtils.DockingColor.ACTIVE_WIDGET.getKey(), active);
        UIManager.put(VLDockingUtils.DockingColor.INACTIVE_WIDGET.getKey(), inactive);
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
