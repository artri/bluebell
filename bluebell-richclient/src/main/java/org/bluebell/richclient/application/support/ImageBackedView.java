/**
 * 
 */
package org.bluebell.richclient.application.support;

import java.awt.Image;

import javax.swing.JComponent;

import org.bluebell.richclient.swing.util.SwingUtils;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.util.Assert;

/**
 * A page consisting on an image.
 * <p>
 * If no image is specified then employs the application default image <code>Application.instance().getImage()</code>.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ImageBackedView extends AbstractView {

    /**
     * The image.
     */
    private Image image;

    /**
     * Sets the image.
     * 
     * @param image
     *            the image to set.
     */
    public void setImage(Image image) {

        Assert.notNull(image, "image");

        this.image = image;
    }

    /**
     * Gets the image.
     * 
     * @return the image.
     */
    public Image getImage() {

        if (image == null) {
            this.setImage(Application.instance().getImage());
        }

        return this.image;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent createControl() {

        return SwingUtils.generateComponent(this.getImage());
    }
}
