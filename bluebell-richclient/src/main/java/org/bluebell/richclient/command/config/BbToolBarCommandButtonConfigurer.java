package org.bluebell.richclient.command.config;

import java.awt.Insets;

import javax.swing.AbstractButton;

import org.springframework.richclient.command.AbstractCommand;
import org.springframework.richclient.command.config.CommandFaceDescriptor;
import org.springframework.richclient.command.config.ToolBarCommandButtonConfigurer;
import org.springframework.util.Assert;

/**
 * Custom <code>CommandButtonConfigurer</code> for buttons on the toolbar.
 * <p>
 * Allows using large icons for toolbar.
 * 
 * @see http://forum.springsource.org/showthread.php?p=265779
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbToolBarCommandButtonConfigurer extends ToolBarCommandButtonConfigurer {

    /**
     * Indicates if large icons should be used.
     */
    private Boolean useLargeIcons;

    /**
     * Creates this command button configurer.
     */
    public BbToolBarCommandButtonConfigurer() {

	super();
    }

    /**
     * {@inheritDoc}
     */
    public void configure(AbstractButton button, AbstractCommand command, CommandFaceDescriptor faceDescriptor) {

	super.configure(button, command, faceDescriptor);
	faceDescriptor.configureIconInfo(button, this.getUseLargeIcons());

	if (this.isTextBelowIcon() && this.isShowText()) {
	    final Insets margin = button.getMargin();
	    button.setMargin(new Insets(margin.top, 0, margin.bottom, 0));
	}
    }

    /**
     * Gets the useLargeIcons.
     * 
     * @return the useLargeIcons
     */
    public Boolean getUseLargeIcons() {

	if (this.useLargeIcons == null) {
	    this.setUseLargeIcons(Boolean.TRUE);
	}

	return this.useLargeIcons;
    }

    /**
     * Sets the useLargeIcons.
     * 
     * @param useLargeIcons
     *            the useLargeIcons to set
     */
    public void setUseLargeIcons(Boolean useLargeIcons) {

	Assert.notNull(useLargeIcons, "useLargeIcons");

	this.useLargeIcons = useLargeIcons;
    }
}
