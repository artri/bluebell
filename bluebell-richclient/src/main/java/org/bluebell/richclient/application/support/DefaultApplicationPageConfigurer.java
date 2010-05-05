/*
 * Copyright (C) 2009 Julio Argüello <julio.arguello@gmail.com>
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
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.bluebell.richclient.application.ApplicationPageConfigurer;
import org.bluebell.richclient.form.AbstractB2TableMasterForm;
import org.bluebell.richclient.form.AbstractBbChildForm;
import org.bluebell.richclient.form.AbstractBbSearchForm;
import org.bluebell.richclient.form.AbstractBbTableMasterForm;
import org.bluebell.richclient.form.BbDispatcherForm;
import org.bluebell.richclient.form.BbValidationForm;
import org.bluebell.richclient.form.GlobalCommandsAccessor;
import org.bluebell.richclient.form.MultipleValidationResultsReporter;
import org.bluebell.richclient.swing.util.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.PropertyAccessor;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageComponentDescriptor;
import org.springframework.richclient.application.View;
import org.springframework.richclient.application.config.ApplicationWindowAware;
import org.springframework.richclient.application.support.DefaultViewDescriptor;
import org.springframework.richclient.application.support.MultiViewPageDescriptor;
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
 * | i |                Detail Views                 |
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
@Aspect
public class DefaultApplicationPageConfigurer<T> implements ApplicationPageConfigurer<T> {

    /**
     * The view types acording to the figure above.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    public static enum BbViewType {
        /**
         * Tree view.
         */
        TREE,
        /**
         * Master view.
         */
        MASTER,
        /**
         * Detail view.
         */
        DETAIL,
        /**
         * Search view.
         */
        SEARCH,
        /**
         * Validation view.
         */
        VALIDATION,
        /**
         * Unknown view.
         */
        UNKNOWN
    }

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultApplicationPageConfigurer.class);

    /**
     * The form class string key employed in <code>DefaultViewDescriptor#viewProperties</code>.
     */
    private static final String FORM_CLASS_KEY = "formClass";

    /**
     * Message format required for debug information during page creation.
     */
    private static final MessageFormat PAGE_CREATION_FMT = new MessageFormat(
            "{0, choice, 0#Before|0<After} creating page \"{1}\" in window \"{2}\"");

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
     * Pointcut that intercepts page creation operations.
     * <p>
     * This method does nothing.
     */
    @Pointcut("execution(* org.springframework.richclient.application.ApplicationPageFactory."
            + "createApplicationPage(..))")
    public final void pageCreationOperation() {

    }

    /**
     * Advise that acts just begore intercepting page creation operations.
     * <p>
     * This implementation ensures application window and page descriptor are not null and then writes a debug message.
     * 
     * @param window
     *            the target window.
     * @param pageDescriptor
     *            the page descriptor.
     */
    @Before("pageCreationOperation() && args(window,pageDescriptor)")
    public final void beforePageCreationOperation(ApplicationWindow window, MultiViewPageDescriptor pageDescriptor) {

        Assert.notNull(window, "window");
        Assert.notNull(pageDescriptor, "pageDescriptor");

        if (DefaultApplicationPageConfigurer.LOGGER.isDebugEnabled()) {
            DefaultApplicationPageConfigurer.LOGGER.debug(DefaultApplicationPageConfigurer.PAGE_CREATION_FMT.format(//
                    new Object[] { Integer.valueOf(0), pageDescriptor.getId(), Integer.valueOf(window.getNumber()) }));
        }
    }

    /**
     * Process a page just inmediatly after creating it.
     * <p>
     * Note that at this point the page <b>has no components</b>, so we need to add them before.
     * <p>
     * This method operates in 3 phases:
     * <ol>
     * <li>Triggers page control creation. This <b>DOES NOT</b> include page components controls creation.
     * <li>Add all described views to the page.
     * <li>Process the page.
     * </ol>
     * <p>
     * There is an invariant consisting on "page components control are not created at all at this method".
     * 
     * @param window
     *            the application window where the page is about to show.
     * @param pageDescriptor
     *            the page descriptor.
     * @param page
     *            the created page.
     * 
     * @see #configureApplicationPage(ApplicationPage)
     */
    @AfterReturning(pointcut = "pageCreationOperation() && args(window,pageDescriptor)", returning = "page")
    public final void afterReturningPageCreationOperation(ApplicationWindow window,
            final MultiViewPageDescriptor pageDescriptor, final ApplicationPage page) {

        Assert.notNull(window, "window");
        Assert.notNull(pageDescriptor, "pageDescriptor");
        Assert.notNull(page, "page");

        if (DefaultApplicationPageConfigurer.LOGGER.isDebugEnabled()) {
            DefaultApplicationPageConfigurer.LOGGER.debug(DefaultApplicationPageConfigurer.PAGE_CREATION_FMT.format(//
                    new Object[] { Integer.valueOf(1), pageDescriptor.getId(), Integer.valueOf(window.getNumber()) }));
        }

        // Page components creation must be done in the event dispatcher thread
        SwingUtils.runInEventDispatcherThread(new Runnable() {

            @SuppressWarnings("unchecked")
            public void run() {

                // 1) Trigger page control creation
                page.getControl();

                // 2) Add all described views to the page
                final List<String> viewDescriptorIds = pageDescriptor.getViewDescriptors();
                for (final String viewDescriptorId : viewDescriptorIds) {
                    page.showView(viewDescriptorId);
                }

                // 3) Process page
                DefaultApplicationPageConfigurer.this.configureApplicationPage(page);
            }
        });
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
        res.put(BbViewType.MASTER.name(), masterViews);
        res.put(BbViewType.SEARCH.name(), ListUtils.unmodifiableList(state.searchViews));
        res.put(BbViewType.DETAIL.name(), ListUtils.unmodifiableList(state.detailViews));
        res.put(BbViewType.VALIDATION.name(), validationViews);
        res.put(BbViewType.UNKNOWN.name(), ListUtils.unmodifiableList(state.unknownPageComponents));

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

        return BbViewType.UNKNOWN.name();
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

        return BbViewType.UNKNOWN;
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

        if (AbstractBbTableMasterForm.class.isAssignableFrom(formClass)) {
            return BbViewType.MASTER;
        } else if (BbValidationForm.class.isAssignableFrom(formClass)) {
            return BbViewType.VALIDATION;
        } else if (AbstractBbChildForm.class.isAssignableFrom(formClass)) {
            return BbViewType.DETAIL;
        } else if (AbstractBbSearchForm.class.isAssignableFrom(formClass)) {
            return BbViewType.SEARCH;
        }

        return BbViewType.UNKNOWN;
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
            case MASTER:
                this.processMasterView((FormBackedView<AbstractB2TableMasterForm<T>>) view, state);
                break;
            case DETAIL:
                this.processDetailView((FormBackedView<AbstractBbChildForm<T>>) view, state);
                break;
            case SEARCH:
                this.processSearchView((FormBackedView<AbstractBbSearchForm<T, ?>>) view, state);
                break;
            case VALIDATION:
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
    protected void processMasterView(FormBackedView<AbstractB2TableMasterForm<T>> masterView, State<T> state) {

        // Validation checks
        Assert.notNull(masterView, "masterView");

        final AbstractB2TableMasterForm<T> targetMasterForm = DefaultApplicationPageConfigurer.backingForm(masterView);

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
     * @param detailView
     *            la vista a configurar.
     * @param state
     *            the processing state.
     */
    protected void processDetailView(final FormBackedView<AbstractBbChildForm<T>> detailView, State<T> state) {

        // Validation checks
        Assert.notNull(detailView, "detailView");

        final AbstractBbChildForm<T> targetDetailForm = DefaultApplicationPageConfigurer.backingForm(detailView);
        final AbstractB2TableMasterForm<T> masterForm = DefaultApplicationPageConfigurer.backingForm(state.masterView);
        final AbstractB2TableMasterForm<T> targetMasterForm = targetDetailForm.getMasterForm();

        this.assertNotAlreadySet(targetMasterForm, masterForm);

        // Link master form and new detail form
        if ((masterForm != null) && (targetMasterForm == null)) {
            masterForm.addChildForm(targetDetailForm);
        }

        // Add a new detail view
        if (!state.detailViews.contains(detailView)) {
            state.detailViews.add(detailView);
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

        final AbstractBbTableMasterForm<T> masterForm = DefaultApplicationPageConfigurer.backingForm(state.masterView);
        final AbstractBbSearchForm<T, ?> targetSearchForm = DefaultApplicationPageConfigurer.backingForm(searchView);
        final AbstractBbTableMasterForm<T> targetMasterForm = targetSearchForm.getMasterForm();

        this.assertNotAlreadySet(targetMasterForm, masterForm);

        // Link master form and new search form
        if ((masterForm != null) && (targetMasterForm == null)) {
            ((AbstractB2TableMasterForm<T>) masterForm).addSearchForm(targetSearchForm);
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

        final AbstractBbTableMasterForm<T> masterForm = DefaultApplicationPageConfigurer.backingForm(state.masterView);
        final BbDispatcherForm<T> theDispatcherForm = state.dispatcherForm;
        final BbValidationForm<T> targetValidationForm = DefaultApplicationPageConfigurer.backingForm(validationView);
        final AbstractBbTableMasterForm<T> targetMasterForm = targetValidationForm.getMasterForm();

        this.assertNotAlreadySet(targetMasterForm, masterForm);

        // Link master form and validation form
        if ((theDispatcherForm != null) && (targetMasterForm == null) && (masterForm != null)) {

            // TODO vincular el maestro y el validation form
            theDispatcherForm.addValidationResultsReporter(new MultipleValidationResultsReporter(theDispatcherForm
                    .getFormModel(), targetValidationForm.getMessagable()));

            targetValidationForm.setMasterForm((AbstractB2TableMasterForm<T>) masterForm);
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
        private FormBackedView<AbstractB2TableMasterForm<Q>> masterView;

        /**
         * Page detail views.
         */
        private List<FormBackedView<AbstractBbChildForm<Q>>> detailViews;

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

            this.detailViews = new ArrayList<FormBackedView<AbstractBbChildForm<Q>>>();
            this.searchViews = new ArrayList<FormBackedView<AbstractBbSearchForm<Q, ?>>>();
            this.unknownPageComponents = new ArrayList<PageComponent>();
        }
    }
}

// TODO revisar
// /**
// * Gives back the focus to the first detail view.
// *
// * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello
// (JAF)</a>
// */
// private class ChangeActiveComponentCommandInterceptor implements
// ActionCommandInterceptor {
//
// private final ApplicationPage applicationPage;
//
// /**
// *
// */
// public ChangeActiveComponentCommandInterceptor(ApplicationPage
// applicationPage) {
//
// super();
// this.applicationPage = applicationPage;
// }
//
// /**
// * {@inheritDoc}
// */
// public void postExecution(ActionCommand command) {
//
// if (BbPageComponentsConfigurer.this.getDetailViews().isEmpty()) {
//
// final PageComponent activeComponent =
// BbPageComponentsConfigurer.this.getDetailViews().iterator()
// .next();
// this.applicationPage.setActiveComponent(activeComponent);
// }
//
// }
//
// /**
// * {@inheritDoc}
// */
// public boolean preExecution(ActionCommand command) {
//
// return Boolean.TRUE;
// }
// }

// TODO revisar
// /**
// * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello
// (JAF)</a>
// */
// private final class PageComponentActivation implements PageListener {
// /**
// *
// */
// private final ApplicationPage applicationPage;
//
// /**
// * @param applicationPage
// */
// private PageComponentActivation(ApplicationPage applicationPage) {
//
// this.applicationPage = applicationPage;
// }
//
// public void pageClosed(ApplicationPage page) {
//
// // Nothing to do
// }
//
// public void pageOpened(ApplicationPage page) {
//
// if (page == this.applicationPage) {
// final PageComponent activePageComponent = page.getActiveComponent();
//
// if ((activePageComponent == null) && !page.getPageComponents().isEmpty())
// {
// // TODO change
// page.setActiveComponent(page.getPageComponents().get(0));
// }
// // Intrusive code...
// // else {
// // BbVLDockingApplicationPage.this.giveFocusTo(activePageComponent);
// // BbVLDockingApplicationPage.this.fireFocusGained(activePageComponent);
// // }
// }
// }
// }

// /**
// * Da el foco al primer formulario hijo.
// *
// * FIXME, (JAF), 20081013, esto es aún provisional.
// */
// private void giveFocusToFirstChildForm() {
//
// if (this.getFirstChildForm() == null) {
// return;
// }
//
// final SingleDockableContainer sdc =
// DockingUtilities.findSingleDockableContainerAncestor(//
// this.getFirstChildForm().getControl());
//
// if (sdc == null) {
// return;
// }
//
// final Dockable dockable = sdc.getDockable();
// final TabbedDockableContainer tdc =
// DockingUtilities.findTabbedDockableContainer(dockable);
//
// if (tdc != null) {
// tdc.setSelectedDockable(dockable);
// }
