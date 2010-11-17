package org.bluebell.richclient.application.docking.vldocking.substance;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import javax.swing.UIManager;

import junit.framework.TestCase;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.skin.DustSkin;

/**
 * This class demonstrate there is a little bug on Substante Look and Feel versión 6.1 that force to call
 * {@link SubstanceLookAndFeel#setSkin(String)} twice.
 * <p>
 * <a href="mailto:kirillcool@yahoo.com">Kirill Grouchnikov</a> (Substance project leader) required a test case to
 * ensure it and here it is ;-)
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestSubstanceLookAndFeel extends TestCase {

    /**
     * Tests Substance bug according to email sent to Kirill:
     * 
     * <quote> Hi Kirill,
     * 
     * Congratulations for this great project!!
     * 
     * I am the team leader of Bluebell (aka BB), http://saber.b2b2000.com/display/BLUE/Bluebell and BB employs (of
     * course) Substance.
     * 
     * I don't know how to contact you regarding Substance... so that's why this e-mail..
     * 
     * org.pushingpixels.substance.api.SubstanceLookAndFeel javadoc says: <li>Call
     * {@link SubstanceLookAndFeel#setSkin(String)} or {@link SubstanceLookAndFeel#setSkin(SubstanceSkin)} static
     * methods. These
     * 
     * methods do not require Substance to be the current look-and-feel.</li>
     * 
     * That's true but in such a case SubstanceLookAndFeel#currentSkin is not set after calling #setSkin(...). That's
     * the reason why a NPE is raised later :-(
     * 
     * In my case I have fixed the problem just calling this method twice ;-) (See it here
     * http://code.google.com/p/bluebell
     * /source/browse/trunk/bluebell-substance/src/main/java/org/bluebell/richclient/application
     * /config/substance/SubstanceLookAndFeelConfigurer.java)
     * 
     * Thank you very much!! </quote>
     * 
     */
    public void testSetSubstanceSkin() {

        final SubstanceSkin substanceSkin = new DustSkin();

        // 0.At the beginning Substance look and feel is not set
        TestCase.assertFalse(UIManager.getLookAndFeel() instanceof SubstanceLookAndFeel);

        // 1.First time "currentSkin" is null
        TestSubstanceLookAndFeel.setSkinOnEDT(substanceSkin);
        TestCase.assertTrue(UIManager.getLookAndFeel() instanceof SubstanceLookAndFeel);
        TestCase.assertNull(SubstanceLookAndFeel.getCurrentSkin());

        // 2.Second time "currentSkin" is nice!!
        TestSubstanceLookAndFeel.setSkinOnEDT(substanceSkin);
        TestCase.assertTrue(UIManager.getLookAndFeel() instanceof SubstanceLookAndFeel);
        TestCase.assertNotNull(SubstanceLookAndFeel.getCurrentSkin());
    }

    /**
     * Sets a Substance skin into the Event Dispatcher Thread (EDT)
     * 
     * @param substanceSkin
     *            the skin to set.
     * 
     * @see SubstanceLookAndFeel#setSkin(SubstanceSkin)
     */
    private static void setSkinOnEDT(final SubstanceSkin substanceSkin) {

        try {
            EventQueue.invokeAndWait(new Runnable() {

                @Override
                public void run() {

                    SubstanceLookAndFeel.setSkin(substanceSkin);
                }
            });
        } catch (InterruptedException e) {
            TestCase.fail(e.getMessage());
        } catch (InvocationTargetException e) {
            TestCase.fail(e.getMessage());
        }
    }
}
