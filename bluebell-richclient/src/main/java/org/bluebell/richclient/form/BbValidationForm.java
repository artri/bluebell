package org.bluebell.richclient.form;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.bluebell.richclient.form.util.BbFormModelHelper;
import org.springframework.binding.validation.ValidationMessage;
import org.springframework.richclient.core.Message;
import org.springframework.richclient.dialog.DefaultMessageAreaModel;
import org.springframework.richclient.dialog.Messagable;
import org.springframework.util.Assert;

/**
 * Formulario genérico para la visualización de errores y conflictos.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class BbValidationForm<T> extends ApplicationWindowAwareForm {

    /**
     * Nombre del formulario.
     */
    private static final String FORM_NAME = "problemsForm";

    private Messagable messagable;

    private List<ValidationMessage> validationMessages = new ArrayList<ValidationMessage>();

    private AbstractBb2TableMasterForm<T> masterForm;

    /**
     * Gets the masterForm.
     * 
     * @return the masterForm
     */
    public AbstractBb2TableMasterForm<T> getMasterForm() {

	return this.masterForm;
    }

    /**
     * Sets the masterForm.
     * 
     * @param masterForm
     *            the masterForm to set
     */
    public void setMasterForm(AbstractBb2TableMasterForm<T> masterForm) {

	Assert.notNull(masterForm, "masterForm");

	this.masterForm = masterForm;
    }

    /**
     * Gets the validationMessages.
     * 
     * @return the validationMessages
     */
    public List<ValidationMessage> getValidationMessages() {

	return this.validationMessages;
    }

    /**
     * Sets the validationMessages.
     * 
     * @param validationMessages
     *            the validationMessages to set
     */
    public void setValidationMessages(List<ValidationMessage> validationMessages) {

	Assert.notNull(validationMessages, "validationMessages");

	this.validationMessages = validationMessages;
    }

    /**
     * Constructor por defecto.
     */
    public BbValidationForm() {

	super(BbValidationForm.FORM_NAME);
	this.setFormModel(BbFormModelHelper.createFormModel(new String())); // TODO
	this.setMessagable(new DefaultMessageAreaModel() {

	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public void setMessage(Message message) {

		// TODO Auto-generated method stub
		super.setMessage(message);

		if (message == null) {
		    BbValidationForm.this.validationMessages.clear();
		} else if (message instanceof ValidationMessage) {
		    BbValidationForm.this.validationMessages.add((ValidationMessage) message);
		}
	    }

	});
    }

    /**
     * Gets the messagable.
     * 
     * @return the messagable
     */
    public Messagable getMessagable() {

	return this.messagable;
    }

    /**
     * Sets the messagable.
     * 
     * @param messagable
     *            the messagable to set
     */
    public void setMessagable(Messagable messagable) {

	Assert.notNull(messagable, "messagable");

	this.messagable = messagable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent createFormControl() {

	return new JPanel();
    }
}
