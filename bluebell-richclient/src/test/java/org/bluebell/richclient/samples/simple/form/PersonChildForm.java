/**
 * 
 */
package org.bluebell.richclient.samples.simple.form;

import javax.swing.JComponent;

import org.bluebell.richclient.form.AbstractBbChildForm;
import org.bluebell.richclient.form.util.BbFormModelHelper;
import org.bluebell.richclient.samples.simple.bean.Person;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.form.binding.swing.SwingBindingFactory;
import org.springframework.richclient.form.builder.TableFormBuilder;

/**
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class PersonChildForm extends AbstractBbChildForm<Person> {

    /**
     * 
     */
    private static final String FORM_ID = "personDetailForm";

    /**
     */
    public PersonChildForm() {

        this(PersonChildForm.FORM_ID);
    }

    /**
     * Creates the form given its id.
     * 
     * @param formId
     *            the form id.
     */
    public PersonChildForm(String formId) {

        super(formId);

        final ValidatingFormModel formModel = BbFormModelHelper.createValidatingFormModel(new Person());
        formModel.setId(formId);
        this.setFormModel(formModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFormModel(ValidatingFormModel formModel) {

        super.setFormModel(formModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValidatingFormModel createFormModel(ValidatingFormModel parentFormModel) {

        return BbFormModelHelper.createValidatingFormModel(parentFormModel.getFormObject(), this.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent createFormControl() {

        final SwingBindingFactory bindingFactory = (SwingBindingFactory) this.getBindingFactory();
        final TableFormBuilder formBuilder = new TableFormBuilder(bindingFactory);

        formBuilder.add("name");
        formBuilder.row();
        formBuilder.add("age");
        formBuilder.row();

        return formBuilder.getForm();
    }
}
