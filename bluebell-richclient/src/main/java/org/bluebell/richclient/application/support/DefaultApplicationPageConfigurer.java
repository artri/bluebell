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
package org.bluebell.richclient.application.support;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ClassUtils;
import org.bluebell.richclient.application.ApplicationPageConfigurer;
import org.bluebell.richclient.form.AbstractBbChildForm;
import org.bluebell.richclient.form.AbstractBbMasterForm;
import org.bluebell.richclient.form.AbstractBbSearchForm;
import org.bluebell.richclient.form.BbDispatcherForm;
import org.bluebell.richclient.form.BbValidationForm;
import org.bluebell.richclient.form.GlobalCommandsAccessor;
import org.bluebell.richclient.form.MultipleValidationResultsReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.PropertyAccessor;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageComponentDescriptor;
import org.springframework.richclient.application.View;
import org.springframework.richclient.application.config.ApplicationWindowAware;
import org.springframework.richclient.application.support.DefaultViewDescriptor;
import org.springframework.richclient.form.Form;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link ApplicationPageConfigurer}.
 * <p>
 * It's also an aspect responsible for configuring page components with a structure similar to the figure below:
 * 
 * <pre>
 *     +=============================================+
 *     +=============================================+
 * +---+-------------------------------+-------------+-+
 * | T |                               |             | |
 * | r |          Master View          |Search Views |-+
 * | e |                               |             | |
 * | e +-------------------------------+-------------+-+
 * |   |_____|     |_____|_____|_____________________|
 * | V |                                             |
 * | i |                Child Views                  |
 * | e |                                             |
 * | w |                                             |
 * +---+---------------------------------------------+
 *     | Validation View |                           |
 *     +=============================================+
 * </pre>
 * <p>
 * Configuration consists on linking every well known page component in the way specified by <code>#processXXX</code>
 * methods.
 * <p>
 * As this class is a POJO it can be used in an isolated way in order to retrieve page components by type.
 * 
 * @param <T>
 *            the type of the entities to be managed.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
// @Aspect
public class DefaultApplicationPageConfigurer<T> implements ApplicationPageConfigurer<T> {

    /*
     * 20101002: Declaring this class as an aspect raises the following compile time exception:
     * 
     * The generic aspect 'org.bluebell.richclient.application.support.DefaultApplicationPageConfigurer' must be
     * declared abstract
     * 
     * However it works at runtime!! Anyway, this class has been refactored, splitting it into an aspect and the
     * application page configurer itself.
     * 
     * See org.bluebell.richclient.application.support.ApplicationPageConfigurerAspect
     */

    /**
     * The view types acording to the figure above.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static enum BbViewType {
        /**
         * Tree view.
         */
        TREE_TYPE,
        /**
         * Master view.
         */
        MASTER_TYPE,
        /**
         * Child view.
         */
        CHILD_TYPE,
        /**
         * Search view.
         */
        SEARCH_TYPE,
        /**
         * Validation view.
         */
        VALIDATION_TYPE,
        /**
         * Unknown view.
         */
        UNKNOWN_TYPE
    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultApplicationPageConfigurer.class);

    /**
     * The form class string key employed in <code>DefaultViewDescriptor#viewProperties</code>.
     */
    private static final String FORM_CLASS_KEY = "formClass";

    /**
     * Message format required for debug information during page configuration.
     */
    private static final MessageFormat UNKNOWN_VIEW_FMT = new MessageFormat(
            "View  processing ignored for view id \"{0}\" ");

    /**
     * Message format required for debug information during form class retrieval.
     */
    private static final MessageFormat UNKNOWN_FORM_CLASS_FMT = new MessageFormat(
            "Form class \"{0}\" not found. Employing default \"org.springframework.richclient.form.Form\"");

    /**
     * Constructs the configurer.
     */
    public DefaultApplicationPageConfigurer() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    public void configureApplicationPage(ApplicationPage applicationPage) {

        this.doConfigureApplicationPage(applicationPage);
    }

    /**
     * Classify page components according to Bluebell criteria.
     * 
     * @param applicationPage
     *            the page to be classified.
     * 
     * @return the page components classification.
     * 
     * @see #configureApplicationPage(ApplicationPage)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, List<? extends PageComponent>> classifyApplicationPage(ApplicationPage applicationPage) {

        Assert.notNull(applicationPage, "applicationPage");

        // Configuration is repeatable, idempotent and fast.
        final State<T> state = this.doConfigureApplicationPage(applicationPage);

        //
        List<? extends PageComponent> masterViews = new ArrayList<PageComponent>();
        List<? extends PageComponent> validationViews = new ArrayList<PageComponent>();
        if (state.masterView != null) {
            masterViews = Arrays.asList(state.masterView);
        }
        if (state.validationView != null) {
            validationViews = Arrays.asList(state.validationView);
        }

        final Map<String, List<? extends PageComponent>> res = new HashMap<String, List<? extends PageComponent>>();
        res.put(BbViewType.MASTER_TYPE.name(), masterViews);
        res.put(BbViewType.SEARCH_TYPE.name(), ListUtils.unmodifiableList(state.searchViews));
        res.put(BbViewType.CHILD_TYPE.name(), ListUtils.unmodifiableList(state.childViews));
        res.put(BbViewType.VALIDATION_TYPE.name(), validationViews);
        res.put(BbViewType.UNKNOWN_TYPE.name(), ListUtils.unmodifiableList(state.unknownPageComponents));

        return res;
    }

    /**
     * {@inheritDoc}
     * 
     * @see #getViewType(Class)
     */
    @Override
    public String getPageComponentType(PageComponentDescriptor pageComponentDescriptor) {

        Assert.notNull(pageComponentDescriptor, "pageComponentDescriptor");

        // If argument is a DefaultViewDescriptor for a FormBackedView then obtain the form class
        if (pageComponentDescriptor instanceof DefaultViewDescriptor) {

            final DefaultViewDescriptor defaultViewDescriptor = (DefaultViewDescriptor) pageComponentDescriptor;
            final Class<?> viewClass = defaultViewDescriptor.getViewClass();

            if (FormBackedView.class.isAssignableFrom(viewClass)) {

                final Class<? extends Form> formClass = this.getFormClass(defaultViewDescriptor);
                return this.getViewType(formClass).name();
            }
        }

        return BbViewType.UNKNOWN_TYPE.name();
    }

    /**
     * Gets the view type.
     * 
     * @param view
     *            the view.
     * @return the type.
     */
    public BbViewType getViewType(View view) {

        Assert.notNull(view, "view");

        if (view instanceof FormBackedView<?>) {

            final FormBackedView<?> formBackedView = (FormBackedView<?>) view;
            final Form form = DefaultApplicationPageConfigurer.backingForm(formBackedView);

            return this.getViewType(form.getClass());
        }

        return BbViewType.UNKNOWN_TYPE;
    }

    /**
     * Gets the view type.
     * 
     * @param formClass
     *            the form class of the backing form view.
     * @return the type.
     */
    protected BbViewType getViewType(Class<? extends Form> formClass) {

        Assert.notNull(formClass, FORM_CLASS_KEY);

        if (AbstractBbMasterForm.class.isAssignableFrom(formClass)) {
            return BbViewType.MASTER_TYPE;
        } else if (BbValidationForm.class.isAssignableFrom(formClass)) {
            return BbViewType.VALIDATION_TYPE;
        } else if (AbstractBbChildForm.class.isAssignableFrom(formClass)) {
            return BbViewType.CHILD_TYPE;
        } else if (AbstractBbSearchForm.class.isAssignableFrom(formClass)) {
            return BbViewType.SEARCH_TYPE;
        }

        return BbViewType.UNKNOWN_TYPE;
    }

    /**
     * Configura un componente de página.
     * 
     * @param pageComponent
     *            el componente de página a configurar.
     * @param state
     *            the processing state.
     */
    protected void processPageComponent(PageComponent pageComponent, State<T> state) {

        if (pageComponent instanceof FormBackedView<?>) {
            this.processFormBackedView((FormBackedView<?>) pageComponent, state);
        } else {
            this.processUnknownPageComponent(pageComponent, state);
        }
    }

    /**
     * Configura una vista sincronizándola con el resto de componentes de la página siempre.
     * 
     * @param view
     *            la vista a configurar.
     * @param state
     *            the processing state.
     */
    @SuppressWarnings("unchecked")
    protected void processFormBackedView(FormBackedView<?> view, State<T> state) {

        /*
         * Form processing by type
         */
        final BbViewType viewType = this.getViewType(view);
        switch (viewType) {
            case MASTER_TYPE:
                this.processMasterView((FormBackedView<AbstractBbMasterForm<T>>) view, state);
                break;
            case CHILD_TYPE:
                this.processChildView((FormBackedView<AbstractBbChildForm<T>>) view, state);
                break;
            case SEARCH_TYPE:
                this.processSearchView((FormBackedView<AbstractBbSearchForm<T, ?>>) view, state);
                break;
            case VALIDATION_TYPE:
                this.processValidatingView((FormBackedView<BbValidationForm<T>>) view, state);
                break;
            default:
                if (DefaultApplicationPageConfigurer.LOGGER.isDebugEnabled()) {
                    DefaultApplicationPageConfigurer.LOGGER.debug(//
                            DefaultApplicationPageConfigurer.UNKNOWN_VIEW_FMT.format(new String[] { view.getId() }));
                }
        }

        final Form form = view.getBackingForm();

        /*
         * Global commands accessor processing
         */
        if (form instanceof GlobalCommandsAccessor) {
            this.processGlobalCommandsAccessor((GlobalCommandsAccessor) form, state);
        }
        if (state.globalCommandsAccessor != null) {
            view.setGlobalCommandsAccessor(state.globalCommandsAccessor);
        }

        /*
         * Application window aware processing
         */
        if (form instanceof ApplicationWindowAware) {
            // During window creation, ApplicationServicesAccessor#getActiveWindow() may return last opened window
            // instead of target window. So forms need to know the window they belong to.
            ((ApplicationWindowAware) form).setApplicationWindow(view.getContext().getWindow());
        }
    }

    /**
     * Configura una vista sincronizándola con el resto de componentes de la página siempre y cuando su formulario sea
     * de tipo <code>AbstractBbTableMasterForm</code>.
     * <p>
     * Lleva a cabo las siguientes acciones:
     * <ul>
     * <li>Establece el último formulario maestro de la página.
     * <li>Registra un <em>listener</em> que devuelve el foco al componente activo (para que los comandos globales se
     * recarguen correctamente).
     * <li>Añade un interceptor al comando <code>newFormObject</code> del formulario maestro para que devuelva el foco
     * al primer formulario hijo.
     * </ul>
     * 
     * @param masterView
     *            la vista a configurar.
     * @param state
     *            the processing state.
     */
    protected void processMasterView(FormBackedView<AbstractBbMasterForm<T>> masterView, State<T> state) {

        // Validation checks
        Assert.notNull(masterView, "masterView");

        final AbstractBbMasterForm<T> targetMasterForm = DefaultApplicationPageConfigurer.backingForm(masterView);

        this.assertNotAlreadySet(state.masterView, masterView);

        // Attach a "change active component" command interceptor and set the master view
        if (state.masterView == null) {
            // final ActionCommand newFormObjectCommand =targetMasterForm.getNewFormObjectCommand();
            // final ApplicationPage applicationPage = masterView.getContext().getPage();
            // newFormObjectCommand.addCommandInterceptor(new ChangeActiveComponentCommandInterceptor(applicationPage));

            state.masterView = masterView;
            state.dispatcherForm = targetMasterForm.getDispatcherForm();
        }
    }

    /**
     * Configura una vista sincronizándola con el resto de componentes de la página siempre y cuando su formulario sea
     * de tipo <code>AbstractBbChildForm</code>.
     * <p>
     * Vincula el formulario hijo con su padre y recuerda el primer formulario hijo.
     * 
     * @param childView
     *            la vista a configurar.
     * @param state
     *            the processing state.
     */
    protected void processChildView(final FormBackedView<AbstractBbChildForm<T>> childView, State<T> state) {

        // Validation checks
        Assert.notNull(childView, "childView");

        final AbstractBbChildForm<T> targetChildForm = DefaultApplicationPageConfigurer.backingForm(childView);
        final AbstractBbMasterForm<T> masterForm = DefaultApplicationPageConfigurer.backingForm(state.masterView);
        final AbstractBbMasterForm<T> targetMasterForm = targetChildForm.getMasterForm();

        this.assertNotAlreadySet(targetMasterForm, masterForm);

        // Link master form and new child form
        if ((masterForm != null) && (targetMasterForm == null)) {
            masterForm.addChildForm(targetChildForm);
        }

        // Add a new child view
        if (!state.childViews.contains(childView)) {
            state.childViews.add(childView);
        }
    }

    /**
     * Configura una vista sincronizándola con el resto de componentes de la página siempre y cuando su formulario sea
     * de tipo <code>AbstractBbSearchForm</code>.
     * <p>
     * Establece el formulario maestro donde mostrar los resultados de la búsqueda.
     * 
     * @param searchView
     *            la vista a configurar.
     * @param state
     *            the processing state.
     */
    protected void processSearchView(final FormBackedView<AbstractBbSearchForm<T, ?>> searchView, State<T> state) {

        // Validation checks
        Assert.notNull(searchView, "searchView");

        final AbstractBbMasterForm<T> masterForm = DefaultApplicationPageConfigurer.backingForm(state.masterView);
        final AbstractBbSearchForm<T, ?> targetSearchForm = DefaultApplicationPageConfigurer.backingForm(searchView);
        final AbstractBbMasterForm<T> targetMasterForm = targetSearchForm.getMasterForm();

        this.assertNotAlreadySet(targetMasterForm, masterForm);

        // Link master form and new search form
        if ((masterForm != null) && (targetMasterForm == null)) {
            ((AbstractBbMasterForm<T>) masterForm).addSearchForm(targetSearchForm);
        }

        if (!state.searchViews.contains(searchView)) {
            state.searchViews.add(searchView);
        }
    }

    /**
     * Configura una vista sincronizándola con el resto de componentes de la página siempre y cuando su formulario sea
     * de tipo <code>BbProblemsForm</code>.
     * <p>
     * Establece al último formulario maestro su formulario de problemas y un nuevo <em>results reporter</em>.
     * 
     * @param validationView
     *            la vista a configurar.
     * @param state
     *            the processing state.
     */
    protected void processValidatingView(final FormBackedView<BbValidationForm<T>> validationView, State<T> state) {

        // Validation checks
        Assert.notNull(validationView, "validationView");

        final AbstractBbMasterForm<T> masterForm = DefaultApplicationPageConfigurer.backingForm(state.masterView);
        final BbDispatcherForm<T> theDispatcherForm = state.dispatcherForm;
        final BbValidationForm<T> targetValidationForm = DefaultApplicationPageConfigurer.backingForm(validationView);
        final AbstractBbMasterForm<T> targetMasterForm = targetValidationForm.getMasterForm();

        this.assertNotAlreadySet(targetMasterForm, masterForm);

        // Link master form and validation form
        if ((theDispatcherForm != null) && (targetMasterForm == null) && (masterForm != null)) {

            // TODO vincular el maestro y el validation form
            theDispatcherForm.addValidationResultsReporter(new MultipleValidationResultsReporter(theDispatcherForm
                    .getFormModel(), targetValidationForm.getMessagable()));

            targetValidationForm.setMasterForm((AbstractBbMasterForm<T>) masterForm);
        }

        // Subscribe for validation events and set the validation view
        if (state.validationView == null) {
            state.validationView = validationView;
        }
    }

    /**
     * Configura un componente de página de caracter desconocido.
     * 
     * @param pageComponent
     *            el componente de página a configurar.
     * @param state
     *            the processing state.
     */
    protected void processUnknownPageComponent(PageComponent pageComponent, State<T> state) {

        // Validation checks
        Assert.notNull(pageComponent, "pageComponent");

        if (!state.unknownPageComponents.contains(pageComponent)) {
            state.unknownPageComponents.add(pageComponent);
        }
    }

    /**
     * Configura una vista sincronizándola con el resto de componentes de la página siempre y cuando su formulario sea
     * de tipo <code>BbProblemsForm</code>.
     * <p>
     * Establece al último formulario maestro su formulario de problemas y un nuevo <em>results reporter</em>.
     * 
     * @param globalCommandsAccessor
     *            la vista a configurar.
     * @param state
     *            the processing state.
     */
    protected void processGlobalCommandsAccessor(GlobalCommandsAccessor globalCommandsAccessor, State<T> state) {

        // Validation checks
        Assert.notNull(globalCommandsAccessor, "globalCommandsAccesor");

        this.assertNotAlreadySet(state.globalCommandsAccessor, globalCommandsAccessor);

        // Sets the globalCommandAccessor
        if (state.globalCommandsAccessor == null) {
            state.globalCommandsAccessor = globalCommandsAccessor;
        }
    }

    /**
     * Configures an application page, iterating all over its page components in two consecutives steps.
     * <p>
     * As result of this method every page component should be aware of its respectives "neighbours".
     * 
     * @param applicationPage
     *            the page to be configured.
     * 
     * @return the page state.
     */
    private State<T> doConfigureApplicationPage(ApplicationPage applicationPage) {

        final State<T> state = new State<T>();

        // 1st pass: recognition
        for (final PageComponent pageComponent : applicationPage.getPageComponents()) {
            this.processPageComponent(pageComponent, state);
        }

        // 2nd pass: association
        for (final PageComponent pageComponent : applicationPage.getPageComponents()) {
            this.processPageComponent(pageComponent, state);
        }

        return state;
    }

    /**
     * Asserts that the target object is null or different from the candidate one.
     * 
     * @param current
     *            the target object
     * @param candidate
     *            the candidate
     */
    private void assertNotAlreadySet(Object current, Object candidate) {

        if ((current != null) && (candidate != null) && !current.equals(candidate)) {
            // candidate may be null after multiple processings depending on view descriptors order
            throw new IllegalStateException(); // TODO crear una excepción para esto.
        }
    }

    /**
     * Gets the form class for a given view descriptor.
     * 
     * @param viewDescriptor
     *            the view descriptor.
     * @return the form class.
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Form> getFormClass(DefaultViewDescriptor viewDescriptor) {

        Assert.notNull(viewDescriptor, "viewDescriptor");

        Class<Form> formClass = Form.class;

        final PropertyAccessor propertyAccessor = new DirectFieldAccessor(viewDescriptor);
        final Map<String, Object> viewProps = (Map<String, Object>) propertyAccessor.getPropertyValue("viewProperties");
        final String formClassName = (String) viewProps.get(DefaultApplicationPageConfigurer.FORM_CLASS_KEY);

        if (formClassName != null) {
            try {
                formClass = ClassUtils.getClass(formClassName);
            } catch (ClassNotFoundException e) {
                if (DefaultApplicationPageConfigurer.LOGGER.isDebugEnabled()) {
                    DefaultApplicationPageConfigurer.LOGGER.debug(//
                            DefaultApplicationPageConfigurer.UNKNOWN_FORM_CLASS_FMT.format(//
                                    new String[] { formClassName }));
                }
            }
        }

        return formClass;
    }

    /**
     * Returns the backing form of the target view.
     * <p>
     * This method ensures the backing form is not <code>null</code> when the target view neither is.
     * 
     * @param <Q>
     *            the form class.
     * @param view
     *            the view.
     * @return the backing form.
     */
    public static final <Q extends Form> Q backingForm(FormBackedView<Q> view) {

        final Q form = (view != null) ? view.getBackingForm() : null;

        // TODO crear una excepción y lanzarla
        Assert.state((view == null) || (form != null), "Backing form is null");

        return form;
    }

    /**
     * Returns the backing form model of the target view.
     * 
     * @param <Q>
     *            the form class.
     * @param view
     *            the view.
     * @return the backing form model.
     * 
     * @see #backingForm(FormBackedView)
     */
    public static final <Q extends Form> FormModel backingFormModel(FormBackedView<Q> view) {

        return (view != null) ? DefaultApplicationPageConfigurer.backingForm(view).getFormModel() : null;
    }

    /**
     * VO that stores internal processing state.
     * 
     * @param <Q>
     *            the type of the entities to be managed.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private static final class State<Q> {

        /**
         * The master view.
         */
        private FormBackedView<AbstractBbMasterForm<Q>> masterView;

        /**
         * Page child views.
         */
        private List<FormBackedView<AbstractBbChildForm<Q>>> childViews;

        /**
         * The search views.
         */
        private List<FormBackedView<AbstractBbSearchForm<Q, ?>>> searchViews;

        /**
         * The validation view.
         */
        private FormBackedView<BbValidationForm<Q>> validationView;

        /**
         * Other page components used on the page different from well knows page components.
         */
        private List<PageComponent> unknownPageComponents;

        /**
         * The global commands accessor.
         * <p>
         * Normally will be the same than <code>masterView#getBackingForm()</code>.
         */
        private GlobalCommandsAccessor globalCommandsAccessor;

        /**
         * The dispatcher form.
         */
        private BbDispatcherForm<Q> dispatcherForm;

        /**
         * Constructs the VO.
         */
        private State() {

            this.childViews = new ArrayList<FormBackedView<AbstractBbChildForm<Q>>>();
            this.searchViews = new ArrayList<FormBackedView<AbstractBbSearchForm<Q, ?>>>();
            this.unknownPageComponents = new ArrayList<PageComponent>();
        }
    }
}
