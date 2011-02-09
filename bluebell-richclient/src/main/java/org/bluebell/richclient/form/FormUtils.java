/*
 * Copyright (C) 2009 Julio Arg\u00fcello <julio.arguello@gmail.com>
 *
 * This file is part of Bluebell Rich Client.
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
package org.bluebell.richclient.form;

import org.bluebell.richclient.application.support.FormBackedView;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.util.Assert;

/**
 * Utility class for dealing with forms.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class FormUtils {

    /**
     * The field with name {@value #EDITING_FORM_OBJECT_INDEX_HOLDER} from {@link AbstractForm}.
     */
    private static final String EDITING_FORM_OBJECT_INDEX_HOLDER = "editingFormObjectIndexHolder";

    /**
     * The field with name {@value #EDITABLE_FORM_OBJECTS} from {@link AbstractForm}.
     */
    private static final String EDITABLE_FORM_OBJECTS = "editableFormObjects";

    /**
     * The field with name {@value #NEW_FORM_OBJECT_COMMAND} from {@link AbstractForm}.
     */
    private static final String NEW_FORM_OBJECT_COMMAND = "newFormObjectCommand";

    /**
     * Utility classes should not have a public or default constructor.
     */
    private FormUtils() {

    }

    /**
     * Returns the view backing form.
     * 
     * @param <T>
     *            the form class.
     * @param pageComponent
     *            the page component.
     * @return the backing form.
     * 
     * @see #getBackingForm(FormBackedView)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Form> T getBackingForm(PageComponent pageComponent) {

        T form = null;

        if (pageComponent instanceof FormBackedView) {
            form = FormUtils.getBackingForm((FormBackedView<T>) pageComponent);
        }

        return form;
    }

    /**
     * Returns the view backing form.
     * 
     * @param <T>
     *            the form class.
     * @param view
     *            the view.
     * @return the backing form.
     */
    public static <T extends Form> T getBackingForm(FormBackedView<T> view) {

        final T form = (view != null) ? view.getBackingForm() : null;

        Assert.state((view == null) || (form != null), "Backing form is null");

        return form;
    }

    /**
     * Returns the view backing form model.
     * 
     * @param <T>
     *            the form class.
     * @param view
     *            the view.
     * @return the backing form.
     */
    public static <T extends Form> ValidatingFormModel getBackingFormModel(FormBackedView<T> view) {

        return (view != null) ? FormUtils.getBackingForm(view).getFormModel() : null;
    }

    /**
     * Gets the list of editable form objects of a given form.
     * 
     * @param form
     *            the form.
     * @return the list of editable form objects.
     */
    public static ObservableList getEditableFormObjects(AbstractForm form) {

        Assert.notNull(form, "form");

        final PropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(form);
        final ObservableList observableList = (ObservableList) propertyAccessor.getPropertyValue(//
                FormUtils.EDITABLE_FORM_OBJECTS);

        return observableList;
    }

    /**
     * Gets the selected index of a given form.
     * 
     * @param form
     *            the form.
     * @return the editing index.
     */
    public static int getSelectedIndex(AbstractForm form) {

        Assert.notNull(form, "form");

        final PropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(form);
        final ValueModel indexHolder = (ValueModel) propertyAccessor.getPropertyValue(//
                FormUtils.EDITING_FORM_OBJECT_INDEX_HOLDER);
        final Integer index = (Integer) indexHolder.getValue();

        return index;
    }

    /**
     * Gets the new form object command of a given form.
     * 
     * @param form
     *            the form.
     * @return the list of editable form objects.
     */
    public static ActionCommand getNewFormObjectCommand(AbstractForm form) {

        Assert.notNull(form, "form");

        final PropertyAccessor propertyAccessor = PropertyAccessorFactory.forDirectFieldAccess(form);
        final ActionCommand actionCommand = (ActionCommand) propertyAccessor.getPropertyValue(//
                FormUtils.NEW_FORM_OBJECT_COMMAND);

        return actionCommand;
    }
}
