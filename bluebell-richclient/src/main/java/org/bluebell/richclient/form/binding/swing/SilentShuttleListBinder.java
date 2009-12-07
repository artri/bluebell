/**
 * org.springframework.richclient.components.ExtendedShuttleListBinder
 */
package org.bluebell.richclient.form.binding.swing;

import java.util.Map;

import javax.swing.JComponent;

import org.bluebell.richclient.form.binding.swing.SilentShuttleListBinding.SilentShuttleList;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.binding.Binding;
import org.springframework.richclient.form.binding.swing.ShuttleListBinder;
import org.springframework.richclient.form.binding.swing.ShuttleListBinding;
import org.springframework.util.Assert;

/**
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SilentShuttleListBinder extends ShuttleListBinder {

    /**
     * 
     */
    public SilentShuttleListBinder() {

	super();
    }

    /**
     * Constructor allowing the specification of additional/alternate context keys. This is for use by derived classes.
     * 
     * @param supportedContextKeys
     *            Context keys supported by subclass
     */
    protected SilentShuttleListBinder(final String[] supportedContextKeys) {

	super(supportedContextKeys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected JComponent createControl(Map context) {

	return new SilentShuttleListBinding.SilentShuttleList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Binding doBind(JComponent control, FormModel formModel, String formPropertyPath, Map context) {

	Assert.isTrue(control instanceof SilentShuttleList, formPropertyPath);

	final ShuttleListBinding binding = new SilentShuttleListBinding(//
		(SilentShuttleList) control, formModel, formPropertyPath);
	this.applyContext(binding, context);

	return binding;
    }
}
