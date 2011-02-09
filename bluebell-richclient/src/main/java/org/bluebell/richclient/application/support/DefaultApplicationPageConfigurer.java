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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.functors.InstanceofPredicate;
import org.apache.commons.collections.functors.NotNullPredicate;
import org.apache.commons.lang.ClassUtils;
import org.bluebell.richclient.application.ApplicationPageConfigurer;
import org.bluebell.richclient.form.AbstractBbChildForm;
import org.bluebell.richclient.form.AbstractBbMasterForm;
import org.bluebell.richclient.form.AbstractBbSearchForm;
import org.bluebell.richclient.form.BbDispatcherForm;
import org.bluebell.richclient.form.BbValidationForm;
import org.bluebell.richclient.form.FormUtils;
import org.bluebell.richclient.form.GlobalCommandsAccessor;
import org.bluebell.richclient.form.MultipleValidationResultsReporter;
import org.bluebell.richclient.swing.util.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.PropertyAccessor;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.application.ApplicationPage;
import org.springframework.richclient.application.PageComponent;
import org.springframework.richclient.application.PageComponentDescriptor;
import org.springframework.richclient.application.View;
import org.springframework.richclient.application.ViewDescriptorRegistry;
import org.springframework.richclient.application.config.ApplicationWindowAware;
import org.springframework.richclient.application.support.ApplicationServicesAccessor;
import org.springframework.richclient.application.support.DefaultViewDescriptor;
import org.springframework.richclient.form.Form;
import org.springframework.richclient.form.ValidationResultsReporter;
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
public class DefaultApplicationPageConfigurer<T> extends ApplicationServicesAccessor implements
        ApplicationPageConfigurer<T> {

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
    public void configureApplicationPage(final ApplicationPage applicationPage) {

        Assert.notNull(applicationPage);

        // Page components creation must be done in the event dispatcher thread
        SwingUtils.runInEventDispatcherThread(new Runnable() {

            public void run() {

                // 1) Trigger page control creation, this will attach page components to application page
                applicationPage.getControl();

                // 2) Add all view descriptors to the page
                // final List<String> viewDescriptorIds = pageDescriptor.getViewDescriptors();
                // for (final String viewDescriptorId : viewDescriptorIds) {
                // // We just need to add the page componente but API force us to call showView
                // if (applicationPage.getView(viewDescriptorId) != null) {
                // applicationPage.showView(viewDescriptorId);
                // }}

                /*
                 * 3) Process page,
                 * 1st pass: disassociation
                 * 2nd pass: recognition
                 * 3rd pass: association
                 * 4th pass: validation
                 */
                DefaultApplicationPageConfigurer.this.doConfigureApplicationPage(applicationPage,
                        ProcessingMode.DISASSOCIATE, // 1st
                        ProcessingMode.RECOGNIZE, // 2nd
                        ProcessingMode.ASSOCIATE, // 3rd
                        ProcessingMode.VALIDATE); // 4th
            }
        });
    }

    /**
     * Classify page components according to Bluebell criteria.
     * <p>
     * <b>Note</b> only current page components will be processed. To classify closed undeclared page components also
     * then write the following code before:
     * 
     * <pre>
     * final List&lt;String&gt; pageComponentIds = ApplicationUtils.getDeclaredPageComponentDescriptors(//
     *         applicationPage);
     * for (String pageComponentId : pageComponentIds) {
     *     applicationPage.showView(pageComponentId);
     * }
     * </pre>
     * 
     * </p>
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
        final State<T> state = this.doConfigureApplicationPage(applicationPage, ProcessingMode.RECOGNIZE);

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
    public String getPageComponentType(String pageComponentDescriptorId) {

        Assert.notNull(pageComponentDescriptorId, "pageComponentDescriptorId");

        final ViewDescriptorRegistry viewDescriptorRegistry = (ViewDescriptorRegistry) this
                .getService(ViewDescriptorRegistry.class);
        final PageComponentDescriptor descriptor = viewDescriptorRegistry.getViewDescriptor(pageComponentDescriptorId);

        // If argument is a DefaultViewDescriptor for a FormBackedView then obtain the form class
        if (descriptor instanceof DefaultViewDescriptor) {

            final DefaultViewDescriptor defaultViewDescriptor = (DefaultViewDescriptor) descriptor;
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
            final Form form = FormUtils.getBackingForm(formBackedView);

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
     * @param processingMode
     *            the processing mode.
     */
    protected void processPageComponent(PageComponent pageComponent, State<T> state, ProcessingMode processingMode) {

        Assert.notNull(pageComponent, "pageComponent");
        Assert.notNull(state, "state");
        Assert.notNull(processingMode, "processingMode");

        if (pageComponent instanceof FormBackedView<?>) {
            this.processFormBackedView((FormBackedView<?>) pageComponent, state, processingMode);
        } else {
            this.processUnknownPageComponent(pageComponent, state, processingMode);
        }
    }

    /**
     * Configura una vista sincronizándola con el resto de componentes de la página siempre.
     * 
     * @param view
     *            la vista a configurar.
     * @param state
     *            the processing state.
     * @param processingMode
     *            the processing mode.
     */
    @SuppressWarnings("unchecked")
    protected void processFormBackedView(FormBackedView<?> view, State<T> state, ProcessingMode processingMode) {

        Assert.notNull(view, "view");
        Assert.notNull(state, "state");
        Assert.notNull(processingMode, "processingMode");

        /*
         * Form processing by type
         */
        final BbViewType viewType = this.getViewType(view);
        switch (viewType) {
            case MASTER_TYPE:
                this.processMasterView((FormBackedView<AbstractBbMasterForm<T>>) view, state, processingMode);
                break;
            case CHILD_TYPE:
                this.processChildView((FormBackedView<AbstractBbChildForm<T>>) view, state, processingMode);
                break;
            case SEARCH_TYPE:
                this.processSearchView((FormBackedView<AbstractBbSearchForm<T, ?>>) view, state, processingMode);
                break;
            case VALIDATION_TYPE:
                this.processValidatingView((FormBackedView<BbValidationForm<T>>) view, state, processingMode);
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
            this.processGlobalCommandsAccessor((GlobalCommandsAccessor) form, state, processingMode);
        }

        switch (processingMode) {
            case RECOGNIZE:
                break;
            case ASSOCIATE:
                if (state.globalCommandsAccessor != null) {
                    view.setGlobalCommandsAccessor(state.globalCommandsAccessor);
                }
                /*
                 * Application window aware processing
                 */
                if (form instanceof ApplicationWindowAware) {
                    // During window creation, ApplicationServicesAccessor#getActiveWindow() may return last opened
                    // window instead of target window. So forms need to know the window they belong to.
                    final ApplicationWindowAware applicationWindowAware = (ApplicationWindowAware) form;
                    applicationWindowAware.setApplicationWindow(view.getContext().getWindow());
                }
                break;
            case DISASSOCIATE:
                view.setGlobalCommandsAccessor(null);
                // A form belongs to a window and just one window
                break;
            case VALIDATE:
                break;
            default:
                throw new IllegalStateException("Unknown processing mode");
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
     * @param processingMode
     *            the processing mode.
     */
    protected void processMasterView(FormBackedView<AbstractBbMasterForm<T>> masterView, State<T> state,
            ProcessingMode processingMode) {

        Assert.notNull(masterView, "masterView");
        Assert.notNull(state, "state");
        Assert.notNull(processingMode, "processingMode");

        final AbstractBbMasterForm<T> targetMasterForm = FormUtils.getBackingForm(masterView);

        this.assertNotAlreadySet(state.masterView, masterView);

        switch (processingMode) {
            case RECOGNIZE:
                if (state.masterView == null) {
                    state.masterView = masterView;
                    state.dispatcherForm = targetMasterForm.getDispatcherForm();
                }
                break;
            case ASSOCIATE:
                break;
            case DISASSOCIATE:
                // Child forms
                final List<AbstractBbChildForm<T>> childForms = new ArrayList<AbstractBbChildForm<T>>(//
                        targetMasterForm.getChildForms());
                for (AbstractBbChildForm<T> childForm : childForms) {
                    targetMasterForm.removeChildForm(childForm);
                }

                // Search forms
                final List<AbstractBbSearchForm<T, ?>> searchForms = new ArrayList<AbstractBbSearchForm<T, ?>>(//
                        targetMasterForm.getSearchForms());
                for (AbstractBbSearchForm<T, ?> searchForm : searchForms) {
                    targetMasterForm.removeSearchForm(searchForm);
                }

                break;
            case VALIDATE:
                this.assertSamePage(masterView, state.searchViews);
                this.assertSamePage(masterView, state.childViews);
                break;
            default:
                throw new IllegalStateException("Unknown processing mode");
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
     * @param processingMode
     *            the processing mode.
     */
    @SuppressWarnings("unchecked")
    protected void processChildView(final FormBackedView<AbstractBbChildForm<T>> childView, State<T> state,
            ProcessingMode processingMode) {

        Assert.notNull(childView, "childView");
        Assert.notNull(state, "state");
        Assert.notNull(processingMode, "processingMode");

        final AbstractBbChildForm<T> targetChildForm = FormUtils.getBackingForm(childView);
        final AbstractBbMasterForm<T> masterForm = FormUtils.getBackingForm(state.masterView);
        final AbstractBbMasterForm<T> targetMasterForm = targetChildForm.getMasterForm();

        this.assertNotAlreadySet(targetMasterForm, masterForm);

        switch (processingMode) {
            case RECOGNIZE:
                // Add a new child view
                if (!state.childViews.contains(childView)) {
                    state.childViews.add(childView);
                }
                break;
            case ASSOCIATE:
                // Associate master form and new child form
                if ((masterForm != null) && (targetMasterForm == null)
                        && this.isCompatible(masterForm, targetChildForm)) {
                    masterForm.addChildForm(targetChildForm);
                }
                break;
            case DISASSOCIATE:
                // Disassociate master form and child form
                if (targetMasterForm != null) {
                    targetMasterForm.removeChildForm(targetChildForm);
                }
                break;
            case VALIDATE:
                this.assertSamePage(childView, Arrays.asList(state.masterView));
                break;
            default:
                throw new IllegalStateException("Unknown processing mode");
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
     * @param processingMode
     *            the processing mode.
     */
    @SuppressWarnings("unchecked")
    protected void processSearchView(final FormBackedView<AbstractBbSearchForm<T, ?>> searchView, State<T> state,
            ProcessingMode processingMode) {

        Assert.notNull(searchView, "searchView");
        Assert.notNull(state, "state");
        Assert.notNull(processingMode, "processingMode");

        final AbstractBbMasterForm<T> masterForm = FormUtils.getBackingForm(state.masterView);
        final AbstractBbSearchForm<T, ?> targetSearchForm = FormUtils.getBackingForm(searchView);
        final AbstractBbMasterForm<T> targetMasterForm = targetSearchForm.getMasterForm();

        this.assertNotAlreadySet(targetMasterForm, masterForm);

        switch (processingMode) {
            case RECOGNIZE:
                if (!state.searchViews.contains(searchView)) {
                    state.searchViews.add(searchView);
                }
                break;
            case ASSOCIATE:
                // Associate master form and new search form
                if ((masterForm != null) && (targetMasterForm == null)
                        && this.isCompatible(masterForm, targetSearchForm)) {
                    masterForm.addSearchForm(targetSearchForm);
                }
                break;
            case DISASSOCIATE:
                // Disassociate master form and search form
                if (targetMasterForm != null) {
                    targetMasterForm.removeSearchForm(targetSearchForm);
                }
                break;
            case VALIDATE:
                this.assertSamePage(searchView, Arrays.asList(state.masterView));
                break;
            default:
                throw new IllegalStateException("Unknown processing mode");
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
     * @param processingMode
     *            the processing mode.
     * 
     * @see #nullSafeGetMVRR(Form)
     */
    @SuppressWarnings("unchecked")
    protected void processValidatingView(final FormBackedView<BbValidationForm<T>> validationView, State<T> state,
            ProcessingMode processingMode) {

        Assert.notNull(validationView, "validationView");
        Assert.notNull(state, "state");
        Assert.notNull(processingMode, "processingMode");

        final AbstractBbMasterForm<T> masterForm = FormUtils.getBackingForm(state.masterView);
        final BbDispatcherForm<T> theDispatcherForm = state.dispatcherForm;
        final BbValidationForm<T> targetValidationForm = FormUtils.getBackingForm(validationView);
        final AbstractBbMasterForm<T> targetMasterForm = targetValidationForm.getMasterForm();

        this.assertNotAlreadySet(targetMasterForm, masterForm);

        switch (processingMode) {
            case RECOGNIZE:
                // Subscribe for validation events and set the validation view
                if (state.validationView == null) {
                    state.validationView = validationView;
                }
                break;
            case ASSOCIATE:
                // Link master form and validation form
                if ((theDispatcherForm != null) && (targetMasterForm == null) && (masterForm != null)) {

                    final List<MultipleValidationResultsReporter> reporters = this.nullSafeGetMVRR(theDispatcherForm);
                    if (reporters.isEmpty()) {
                        final ValidationResultsReporter reporter = new MultipleValidationResultsReporter(
                                theDispatcherForm.getFormModel(), targetValidationForm.getMessagable());

                        theDispatcherForm.addValidationResultsReporter(reporter);
                    }

                    targetValidationForm.setMasterForm((AbstractBbMasterForm<T>) masterForm);
                }
                break;
            case DISASSOCIATE:

                final List<MultipleValidationResultsReporter> reporters = this.nullSafeGetMVRR(theDispatcherForm);
                for (ValidationResultsReporter reporter : reporters) {
                    theDispatcherForm.removeValidationResultsReporter(reporter);
                }

                targetValidationForm.setMasterForm((AbstractBbMasterForm<T>) masterForm);
                break;
            case VALIDATE:
                this.assertSamePage(validationView, Arrays.asList(state.masterView));
                break;
            default:
                throw new IllegalStateException("Unknown processing mode");
        }
    }

    /**
     * Configura un componente de página de caracter desconocido.
     * 
     * @param pageComponent
     *            el componente de página a configurar.
     * @param state
     *            the processing state.
     * @param processingMode
     *            the processing mode.
     */
    protected void processUnknownPageComponent(PageComponent pageComponent, State<T> state,
            ProcessingMode processingMode) {

        // Validation checks
        Assert.notNull(pageComponent, "pageComponent");
        Assert.notNull(state, "state");
        Assert.notNull(processingMode, "state");

        switch (processingMode) {
            case RECOGNIZE:
                if (!state.unknownPageComponents.contains(pageComponent)) {
                    state.unknownPageComponents.add(pageComponent);
                }
                break;
            case ASSOCIATE:
                break;
            case DISASSOCIATE:
                break;
            case VALIDATE:
                break;
            default:
                throw new IllegalStateException("Unknown processing mode");
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
     * @param processingMode
     *            the processing mode.
     */
    protected void processGlobalCommandsAccessor(GlobalCommandsAccessor globalCommandsAccessor, State<T> state,
            ProcessingMode processingMode) {

        Assert.notNull(globalCommandsAccessor, "globalCommandsAccesor");
        Assert.notNull(state, "state");
        Assert.notNull(processingMode, "processingMode");

        this.assertNotAlreadySet(state.globalCommandsAccessor, globalCommandsAccessor);

        switch (processingMode) {
            case RECOGNIZE:
                // Sets the globalCommandAccessor
                if (state.globalCommandsAccessor == null) {
                    state.globalCommandsAccessor = globalCommandsAccessor;
                }
                break;
            case ASSOCIATE:
                break;
            case DISASSOCIATE:
                break;
            case VALIDATE:
                break;
            default:
                throw new IllegalStateException("Unknown processing mode");
        }
    }

    /**
     * Configures an application page, iterating all over its page components in two consecutives steps.
     * <p>
     * As result of this method every page component should be aware of its respectives "neighbours".
     * 
     * @param applicationPage
     *            the page to be configured.
     * @param processingModes
     *            the ordered relation of processing modes to apply.
     * 
     * @return the page state.
     */
    private State<T> doConfigureApplicationPage(ApplicationPage applicationPage, ProcessingMode... processingModes) {

        Assert.notNull(applicationPage, "applicationPage");

        final State<T> state = new State<T>();

        for (ProcessingMode processingMode : processingModes) {
            for (final PageComponent pageComponent : applicationPage.getPageComponents()) {
                this.processPageComponent(pageComponent, state, processingMode);
            }
        }

        return state;
    }

    /**
     * Validates a page component in order to ensure its associations are in fact contained in the same page.
     * 
     * @param targetPageComponent
     *            the page component.
     * @param associations
     *            its associations.
     */
    private void assertSamePage(PageComponent targetPageComponent, Collection<? extends PageComponent> associations) {
    
        Assert.notNull(targetPageComponent, "targetPageComponent");
        Assert.notNull(associations, "associtations");
    
        final ApplicationPage applicationPage = targetPageComponent.getContext().getPage();
        final List<Form> pageForms = new ArrayList<Form>();
        final List<Form> associationsForms = new ArrayList<Form>();
    
        Assert.notNull(applicationPage, "applicationPage");
    
        for (PageComponent pageComponent : applicationPage.getPageComponents()) {
            pageForms.add(FormUtils.getBackingForm(pageComponent));
        }
        for (PageComponent pageComponent : associations) {
            associationsForms.add(FormUtils.getBackingForm(pageComponent));
        }
        
        CollectionUtils.filter(associationsForms, NotNullPredicate.getInstance());
    
        Assert.isTrue(CollectionUtils.isSubCollection(associationsForms, pageForms),
                "CollectionUtils.isSubCollection(associationsForms, pageForms)");
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
     * Returns whether to forms are compatible.
     * 
     * @param masterForm
     *            the master form to associate.
     * @param form
     *            the target form, may be a child form or a search form.
     * @return <code>true</code> if compatible and <code>false</code> in other case.
     */
    private Boolean isCompatible(AbstractBbMasterForm<T> masterForm, Form form) {

        Assert.notNull(masterForm, "masterForm");
        Assert.notNull(form, "form");

        final Class<?> masterFormType = masterForm.getManagedType();
        final Class<?> targetFormType;
        if (form instanceof AbstractBbChildForm<?>) {
            targetFormType = ((AbstractBbChildForm<?>) form).getManagedType();
        } else if (form instanceof AbstractBbSearchForm<?, ?>) {
            targetFormType = ((AbstractBbSearchForm<?, ?>) form).getSearchResultsType();
        } else {
            targetFormType = form.getFormObject().getClass();
        }

        Assert.notNull(masterFormType, "masterFormType");
        Assert.notNull(targetFormType, "targetFormType");

        final Boolean compatible = ClassUtils.isAssignable(masterFormType, targetFormType);

        return compatible;
    }

    /**
     * Gets the reports of type <code>MultipleValidationResultsReporter</code> (aka MVRR) installed on a form. Never
     * minds if form
     * is <code>null</code>.
     * <p/>
     * Usefull to:
     * <ul>
     * <li>Avoid adding more than once instance of <code>MVRR</code> to the same form.
     * <li>Remove installed instances.
     * </ul>
     * 
     * @param form
     *            the target form.
     * @return the reporters.
     */
    private List<MultipleValidationResultsReporter> nullSafeGetMVRR(Form form) {

        final List<MultipleValidationResultsReporter> reporters = new ArrayList<MultipleValidationResultsReporter>();
        if (form != null) {
            CollectionUtils.select(form.getValidationResultsReporters(),
                    InstanceofPredicate.getInstance(MultipleValidationResultsReporter.class), reporters);
        }

        return reporters;
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

        return (view != null) ? FormUtils.getBackingForm(view).getFormModel() : null;
    }

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
     * The process mode.
     * 
     * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
     */
    private static enum ProcessingMode {
        /**
         * Proceessing just recognizes page components.
         */
        RECOGNIZE,
        /**
         * Processing associates page components.
         */
        ASSOCIATE,
        /**
         * Processing disassociates page components.
         */
        DISASSOCIATE,
        /**
         * Processing validates everything is OK.
         */
        VALIDATE
    }
}
