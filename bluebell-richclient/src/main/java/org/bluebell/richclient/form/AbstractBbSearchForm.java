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

package org.bluebell.richclient.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bluebell.richclient.command.support.CommandUtils;
import org.bluebell.richclient.form.util.BbFormModelHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.binding.form.FormModel;
import org.springframework.binding.form.ValidatingFormModel;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.CommandGroup;
import org.springframework.richclient.command.ToggleCommand;
import org.springframework.richclient.core.DefaultMessage;
import org.springframework.richclient.core.Severity;
import org.springframework.richclient.dialog.MessageDialog;
import org.springframework.richclient.dialog.TitlePane;
import org.springframework.richclient.form.FormGuard;
import org.springframework.richclient.form.SimpleValidationResultsReporter;
import org.springframework.richclient.form.builder.support.ConfigurableFormComponentInterceptorFactory;
import org.springframework.richclient.util.GuiStandardUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Especialización de {@link org.springframework.richclient.form.AbstractForm} especializado en la realización de
 * búsquedas.
 * <p>
 * Para ello incorpora un comando de búsqueda y requiere de un formulario maestro en el que reflejar los resultados de
 * la búsqueda.
 * <p>
 * Este formulario por defecto no valida y tampoco tiene control de <em>dirty tracking</em>.
 * 
 * @param <T>
 *            el tipo de las entidades a buscar.
 * @param <U>
 *            el tipo de la clase con los parámetros de la búsqueda.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public abstract class AbstractBbSearchForm<T extends Object, U extends Object> extends ApplicationWindowAwareForm {

    /**
     * El identificador por defecto del comando para añadir o no resultados.
     */
    private static final String ATTACH_RESULTS_COMMAND_ID = "attachResultsCommand";

    /**
     * El identificador del grupo de comandos.
     */
    private static final String COMMAND_GROUP_ID = "searchFormCommandGroup";

    /**
     * El identificador por defecto del comando de refresco de la última búsqueda.
     */
    private static final String REFRESH_LAST_SEARCH_COMMAND_ID = "refreshLastSearchCommand";

    /**
     * El identificador por defecto del comando para resetear los parámetros de búsqueda.
     */
    private static final String RESET_COMMAND_ID = "resetCommand";

    /**
     * El identificador por defecto del comando de búsqueda.
     */
    private static final String SEARCH_COMMAND_ID = "searchCommand";

    /**
     * El identificador por defecto del formulario de búsqueda.
     */
    private static final String SEARCH_FORM_ID = "searchForm";

    /**
     * <em>Flag</em> indicando si los resultados de la última búsqueda se han de añadir a los anteriores (
     * <code>true</code>) o si por el contrario deben sustituirlos (<code>true</code>).
     */
    private boolean attachResults = Boolean.FALSE;

    /**
     * El comando para añadir o no resultados.
     */
    private ActionCommand attachResultsCommand;

    /**
     * EL grupo de comandos con el comando de búsqueda.
     */
    private CommandGroup commandGroup;

    /**
     * El interceptor de <em>dirty</em>.
     */
    private final ConfigurableFormComponentInterceptorFactory dirtyIndicatorInterceptorFactory;

    /**
     * Los últimos parámetros de búsqueda introducidos.
     */
    private U lastSearchParams;

    /**
     * El formulario maestro en el que mostrar los resultados de la búsqueda.
     */
    private AbstractBbMasterForm<T> masterForm;

    /**
     * Un diálogo que informa que la última búsqueda no arroja ningún resultado.
     */
    private MessageDialog noResultsMessageDialog;

    /**
     * El comando responsable de refrescar.
     */
    private ActionCommand refreshLastSearchCommand;

    /**
     * El comando para resetear el formulario.
     */
    private ActionCommand resetCommand;

    /**
     * El comando responsable de realizar la búsqueda.
     */
    private ActionCommand searchCommand;

    /**
     * Un <code>TitlePane</code> que se sitúa (opcionalmente) en la parte superior del formulario.
     */
    private TitlePane titlePane;

    /**
     * Construye el formulario a partir de su identificador.
     * 
     * @param formId
     *            el identificador del formulario.
     */
    public AbstractBbSearchForm(String formId) {

        super(formId);
        this.setClearFormOnCommit(Boolean.FALSE);

        // TODO (JAF), 20080713, quizás sea mejor recuperar el interceptor de otro modo...
        this.dirtyIndicatorInterceptorFactory = (ConfigurableFormComponentInterceptorFactory) //
        this.getApplicationContext().getBean("dirtyIndicatorInterceptorFactory");

        this.setFormModel(this.createFormModel());
    }

    /**
     * Gets the search params type.
     * 
     * @return the type.
     */
    public abstract Class<U> getSearchParamsType();

    /**
     * Gets the search results type.
     * 
     * @return the type.
     */
    public abstract Class<T> getSearchResultsType();

    /**
     * Obtiene el comando para añadir o no resultados.
     * 
     * @return el comando para añadir o no resultados.
     * 
     * @see #createAttachResultsCommand()
     */
    public final ActionCommand getAttachResultsCommand() {

        if (this.attachResultsCommand == null) {
            this.attachResultsCommand = this.createAttachResultsCommand();
        }
        return this.attachResultsCommand;
    }

    /**
     * Obtiene el formulario maestro en el que volcar los resultados de la búsqueda.
     * 
     * @return el formulario maestro.
     */
    public final AbstractBbMasterForm<T> getMasterForm() {

        return this.masterForm;
    }

    /**
     * Establece el formulario maestro en el que volcar los resultados de la búsqueda.
     * 
     * @param masterForm
     *            el formulario maestro.
     */
    public final void setMasterForm(AbstractBbMasterForm<T> masterForm) {

        this.masterForm = masterForm;
    }

    /**
     * Obtiene el comando de refresco y si no existe entonces lo crea.
     * 
     * @return el comando de refresco.
     * 
     * @see #createRefreshLastSearchCommand()
     */
    public final ActionCommand getRefreshLastSearchCommand() {

        if (this.refreshLastSearchCommand == null) {
            this.refreshLastSearchCommand = this.createRefreshLastSearchCommand();
        }
        return this.refreshLastSearchCommand;
    }

    /**
     * Obtiene el comando para resetear los parámetros de la búsqueda.
     * 
     * @return el comando para resetear.
     * 
     * @see #createResetCommand()
     */
    public final ActionCommand getResetCommand() {

        if (this.resetCommand == null) {
            this.resetCommand = this.createResetCommand();
        }
        return this.resetCommand;
    }

    /**
     * Obtiene el comando de búsqueda y si no existe entonces lo crea.
     * 
     * @return el comando de búsqueda.
     * 
     * @see #createSearchCommand()
     */
    public final ActionCommand getSearchCommand() {

        if (this.searchCommand == null) {
            this.searchCommand = this.createSearchCommand();
        }
        return this.searchCommand;
    }

    /**
     * Método ejecutado después de <em>commitear</em> este formulario.
     * <p>
     * Activa el comando de refresco de la última búsqueda.
     * 
     * @param formModel
     *            el modelo.
     */
    @Override
    public void postCommit(FormModel formModel) {

        this.getRefreshLastSearchCommand().setEnabled(Boolean.TRUE);
    }

    /**
     * Resetea el contenido de este formulario y limpia los resultados de la búsqueda.
     * <p>
     * Desactiva el comando de refresco de la última búsqueda.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void reset() {

        super.reset();
        this.getMasterForm().showEntities(ListUtils.EMPTY_LIST);
        this.getRefreshLastSearchCommand().setEnabled(Boolean.FALSE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", this.getId()).toString();
    }

    /**
     * Obtiene los resultados de la búsqueda.
     * 
     * @param searchParams
     *            un objeto con los parámetros de la búsqueda obtenido al <em>commitear</em> el formulario.
     * @return los resultados de la búsqueda.
     */
    protected abstract List<T> doSearch(U searchParams);

    /**
     * Crea el control donde introducir los parámetros de búsqueda.
     * <p>
     * Las subclases pueden implementar este método si quieren conservar la distribución propuesta por
     * {@link #createFormControl()}.
     * <p>
     * Esta implementación devuelve un <code>JPanel</code> vacío.
     * 
     * @return el control donde introducir los parámetros de búsqueda.
     */
    protected abstract JComponent createSearchParamsControl();

    /**
     * Método plantilla que crea el control de este formulario.
     * <p>
     * La distribución propuesta consiste en:
     * 
     * <pre>
     *  +-------------------------------------------+
     *  |               TITLE PANE                  |
     *  |(Errores de validación y nº de resultados) |
     *  |+=========================================+|
     *  ||                                         ||
     *  ||           SEARCH_TYPE PARAMS CONTROL    ||
     *  ||         (Parámetros de búsqueda)        ||            
     *  ||                                         ||
     *  |+=========================================+|
     *  |    RETAIN | SEARCH_TYPE | REFRESH | RESET |
     *  +-------------------------------------------+
     * </pre>
     * 
     * @return el control con el formulario.
     * 
     */
    @Override
    protected final JComponent createFormControl() {

        final int preferredWidth = 400;
        final int preferredHeight = 10;

        // Hacer que los resultados de validación se muestren en el titlePane
        new SimpleValidationResultsReporter(//
                this.getFormModel().getValidationResults(), this.getTitlePane());

        // Crear el control donde introducir los parámetros de búsqueda.
        final JComponent searchParamsControl = this.createSearchParamsControl();
        searchParamsControl.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder()));

        // Crear el control del formulario
        final JPanel pageControl = new JPanel(new BorderLayout());
        pageControl.add(this.titlePane.getControl(), BorderLayout.NORTH);
        pageControl.add(searchParamsControl, BorderLayout.CENTER);
        pageControl.add(this.createButtonBar(), BorderLayout.SOUTH);

        // HACK, (SHT), 20081027, establecer la dimensión para que el formulario
        // no se adapte al tamaño del mensaje de validación.
        pageControl.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

        // HACK, (SHT), 20081027, si no se pone esta línea entonces no saca el
        // icono correcto en caso de que el formulario tenga activada la
        // validación.
        this.getFormModel().validate();

        return pageControl;
    }

    /**
     * Creates the form model.
     * 
     * @return the created form model.
     */
    protected ValidatingFormModel createFormModel() {

        Assert.notNull(this.getSearchParamsType(), "this.getSearchParamsType()");

        final U formObject = BeanUtils.instantiate(this.getSearchParamsType());

        final ValidatingFormModel formModel = BbFormModelHelper.createValidatingFormModel(formObject, this.getId());

        return formModel;
    }

    /**
     * Retorna una barra de botones con el grupo de comandos {@link #commandGroup}, justificada a la derecha y todos del
     * mismo tamaño.
     * 
     * @return la barra de botones.
     * 
     * @see #getCommandGroup()
     */
    protected JComponent createButtonBar() {

        final JComponent buttonBar = this.getCommandGroup().createButtonBar();
        GuiStandardUtils.attachDialogBorder(buttonBar);

        return buttonBar;
    }

    /**
     * Obtiene el identificador del comando para añadir o no resultados.
     * <p>
     * Por defecto devuelve {@link #ATTACH_RESULTS_COMMAND_ID}, las subclases pueden sobreescribir este método para
     * particularizar la configuración del comando.
     * 
     * @return el identificador.
     */
    protected String getAttachResultsCommandFaceDescriptorId() {

        return AbstractBbSearchForm.ATTACH_RESULTS_COMMAND_ID;
    }

    /**
     * Retorna el grupo de comandos, que incluye el comando de búsqueda. En caso de que no exista la crea.
     * 
     * @return el grupo de comandos.
     */
    protected CommandGroup getCommandGroup() {

        if (this.commandGroup == null) {
            this.commandGroup = CommandGroup.createCommandGroup(
                    AbstractBbSearchForm.COMMAND_GROUP_ID,
                    new Object[] { this.getAttachResultsCommand(), this.getSearchCommand(),
                            this.getRefreshLastSearchCommand(), this.getResetCommand() });
        }

        return this.commandGroup;
    }

    /**
     * Obtiene el identificador del comando de refresco.
     * <p>
     * Por defecto devuelve {@link #REFRESH_LAST_SEARCH_COMMAND_ID}, las subclases pueden sobreescribir este método para
     * particularizar la configuración del comando.
     * 
     * @return el identificador.
     */
    protected String getRefreshLastSearchCommandFaceDescriptorId() {

        return AbstractBbSearchForm.REFRESH_LAST_SEARCH_COMMAND_ID;
    }

    /**
     * Obtiene el identificador del comando para resetear los parámetros de la búsqueda.
     * <p>
     * Por defecto devuelve {@link #RESET_COMMAND_ID}, las subclases pueden sobreescribir este método para
     * particularizar la configuración del comando.
     * 
     * @return el identificador.
     */
    protected String getResetCommandFaceDescriptorId() {

        return AbstractBbSearchForm.RESET_COMMAND_ID;
    }

    /**
     * Obtiene el identificador del comando de búsqueda.
     * <p>
     * Por defecto devuelve {@link #SEARCH_COMMAND_ID}, las subclases pueden sobreescribir este método para
     * particularizar la configuración del comando.
     * 
     * @return el identificador.
     */
    protected String getSearchCommandFaceDescriptorId() {

        return AbstractBbSearchForm.SEARCH_COMMAND_ID;
    }

    /**
     * Modifica {@link org.springframework.richclient.form.AbstractForm#setFormModel(ValidatingFormModel)} para que por
     * defecto los formularios de búsqueda no deberían validar.
     * 
     * @param formModel
     *            el modelo.
     */
    @Override
    protected void setFormModel(ValidatingFormModel formModel) {

        super.setFormModel(formModel);
        formModel.setValidating(Boolean.FALSE);

        final String[] excludedFormModelIds = this.dirtyIndicatorInterceptorFactory.getExcludedFormModelIds();
        this.dirtyIndicatorInterceptorFactory.setExcludedFormModelIds(//
                (String[]) ArrayUtils.add(excludedFormModelIds, formModel.getId()));
    }

    /**
     * Crea y configura el comando para añadir o no resultados.
     * 
     * @return el comando para añadir o no resultados.
     * 
     * @see org.springframework.richclient.command.config.CommandConfigurer#configure
     */
    private ActionCommand createAttachResultsCommand() {

        final String commandId = this.getAttachResultsCommandFaceDescriptorId();
        if (!StringUtils.hasText(commandId)) {
            return null;
        }

        final ToggleCommand attachResultsCmd = new ToggleCommand(commandId) {
            /**
             * @see AbstractBbSearchForm#setAttachResults(boolean)
             */
            @Override
            protected void onDeselection() {

                AbstractBbSearchForm.this.setAttachResults(Boolean.FALSE);
            }

            /**
             * @see AbstractBbSearchForm#setAttachResults(boolean)
             */
            @Override
            protected void onSelection() {

                AbstractBbSearchForm.this.setAttachResults(Boolean.TRUE);
            }
        };

        // Securizar el comando
        final String scid = this.constructSecurityControllerId(commandId);
        attachResultsCmd.setSecurityControllerId(scid);

        // Configurar el comando
        return CommandUtils.configureCommand(attachResultsCmd, this.getFormModel());
    }

    /**
     * Crea y configura el comando de refresco.
     * <p>
     * Ejecuta de nuevo la última búsqueda realizada.
     * 
     * @return el comando de refresco.
     * 
     * @see org.springframework.richclient.command.config.CommandConfigurer#configure
     */
    private ActionCommand createRefreshLastSearchCommand() {

        final String commandId = this.getRefreshLastSearchCommandFaceDescriptorId();
        if (!StringUtils.hasText(commandId)) {
            return null;
        }

        final ActionCommand refreshLastSearchCmd = new ActionCommand(commandId) {

            @Override
            protected void doExecuteCommand() {

                // Los últimos parámetros de búsqueda.
                final U searchParams = AbstractBbSearchForm.this.getLastSearchParams();

                // Obtener los resultados de la búsqueda.
                final List<T> searchResults = AbstractBbSearchForm.this.doSearch(searchParams);

                // Notificar el número de resultados devuelto
                AbstractBbSearchForm.this.showNumberOfResults(searchResults.size());

                // Establecer los resultados de la búsqueda en el formulario maestro.
                AbstractBbSearchForm.this.getMasterForm().showEntities(//
                        searchResults, AbstractBbSearchForm.this.isAttachResults());
            }
        };

        // El comando por defecto está deshabilitado.
        refreshLastSearchCmd.setEnabled(Boolean.FALSE);

        // Securizar el comando
        final String scid = this.constructSecurityControllerId(commandId);
        refreshLastSearchCmd.setSecurityControllerId(scid);

        // Configurar el comando
        return CommandUtils.configureCommand(refreshLastSearchCmd, this.getFormModel(), Boolean.TRUE);
    }

    /**
     * Crea y configura el comando para resetear los parámetros de la búsqueda.
     * 
     * @return el comando para resetear.
     * 
     * @see org.springframework.richclient.command.config.CommandConfigurer#configure
     */
    private ActionCommand createResetCommand() {

        final String commandId = this.getResetCommandFaceDescriptorId();
        if (!StringUtils.hasText(commandId)) {
            return null;
        }

        final ActionCommand resetCmd = new ActionCommand(commandId) {

            /**
             * Resetea el formModel del formulario.
             */
            @Override
            protected void doExecuteCommand() {

                AbstractBbSearchForm.this.reset();
                AbstractBbSearchForm.this.setEnabled(Boolean.TRUE);
            }
        };

        // Añadirle una guardia al comando
        final FormGuard formGuard = new FormGuard(AbstractBbSearchForm.this.getFormModel());
        formGuard.addGuarded(resetCmd, FormGuard.ON_ENABLED);

        // Securizar el comando
        final String scid = this.constructSecurityControllerId(commandId);
        resetCmd.setSecurityControllerId(scid);

        // Configurar el comando
        return CommandUtils.configureCommand(resetCmd, this.getFormModel());
    }

    /**
     * Crea y configura el comando de búsqueda.
     * 
     * @return el comando de búsqueda.
     * 
     * @see org.springframework.richclient.command.config.CommandConfigurer#configure
     */
    private ActionCommand createSearchCommand() {

        final String commandId = this.getSearchCommandFaceDescriptorId();
        if (!StringUtils.hasText(commandId)) {
            return null;
        }
        final ActionCommand searchCmd = new ActionCommand(commandId) {
            @Override
            @SuppressWarnings("unchecked")
            protected void doExecuteCommand() {

                final AbstractBbMasterForm<T> theMasterForm = AbstractBbSearchForm.this.getMasterForm();

                // Commitear el formulario
                AbstractBbSearchForm.this.commit();

                // (JAF), 20110111, from now on no search will be performed without having requested user confirmation
                if (theMasterForm.shouldProceed()) {

                    // Obtain and remmember search parameters
                    final U formObject = (U) AbstractBbSearchForm.this.getFormObject();
                    AbstractBbSearchForm.this.setLastSearchParams(formObject);

                    // Obtain search results and notify the number of results
                    final List<T> results = AbstractBbSearchForm.this.doSearch(formObject);
                    AbstractBbSearchForm.this.showNumberOfResults(results.size());

                    // Show entities on master form
                    theMasterForm.showEntities(results, AbstractBbSearchForm.this.isAttachResults(), Boolean.TRUE);
                }
            }
        };

        // Añadirle una guardia al comando
        final FormGuard formGuard = new FormGuard(AbstractBbSearchForm.this.getFormModel());
        formGuard.addGuarded(searchCmd, FormGuard.ON_NOERRORS);

        // Securizar el comando
        final String scid = this.constructSecurityControllerId(commandId);
        searchCmd.setSecurityControllerId(scid);

        // Configurar el comando
        return CommandUtils.configureCommand(searchCmd, this.getFormModel(), Boolean.TRUE);
    }

    /**
     * Obtiene los últimos parámetros de búsqueda.
     * 
     * @return los últimos parámetros de búsqueda.
     */
    private U getLastSearchParams() {

        return this.lastSearchParams;
    }

    /**
     * Obtiene el panel con el título y si no existe lo crea.
     * 
     * @return el panel con el título.
     */
    private TitlePane getTitlePane() {

        if (this.titlePane == null) {
            final String id = AbstractBbSearchForm.SEARCH_FORM_ID;
            final String title = AbstractBbSearchForm.this.getMessage(id + ".title");

            final int lines = 3;
            this.titlePane = new TitlePane(lines);
            this.titlePane.setTitle(title);
        }

        return this.titlePane;
    }

    /**
     * Indica si los resultados de la última búsqueda se han de añadir a los anteriores (<code>true</code>) o si por el
     * contrario deben sustituirlos ( <code>true</code>).
     * 
     * @return el <em>flag</em>
     */
    private boolean isAttachResults() {

        return this.attachResults;
    }

    /**
     * Establece si los resultados de la última búsqueda se han de añadir a los anteriores (<code>true</code>) o si por
     * el contrario deben sustituirlos ( <code>true</code>).
     * 
     * @param attachResults
     *            el <em>flag</em>.
     */
    private void setAttachResults(boolean attachResults) {

        this.attachResults = attachResults;
    }

    /**
     * Establece los últimos parámetros de búsqueda.
     * 
     * @param lastSearchParams
     *            los últimos parámetros de búsqueda.
     */
    private void setLastSearchParams(U lastSearchParams) {

        this.lastSearchParams = lastSearchParams;
    }

    /**
     * Refresca el <em>title pane</em> con el número de resultados devueltos por la última búsqueda. De no haber
     * resultados muestra además un diálogo indicando tal circunstancia.
     * <p>
     * En su primera ejecución instancia el diálogo {@link #noResultsMessageDialog}.
     * 
     * @param resultsCount
     *            el número de resultados devueltos por la última búsqueda.
     */
    private void showNumberOfResults(int resultsCount) {

        final String id = AbstractBbSearchForm.SEARCH_FORM_ID;

        // Construir de forma perezosa el diálogo que informa que no hay resultados para la búsqueda.
        if (this.noResultsMessageDialog == null) {
            final String title = AbstractBbSearchForm.this.getMessage(id + ".noResultsDialog.title");
            final String message = AbstractBbSearchForm.this.getMessage(id + ".noResultsDialog.message");
            this.noResultsMessageDialog = new MessageDialog(title, message);
        }

        // Si la búsqueda no arroja resultados notificarlo
        if (resultsCount == 0) {
            this.noResultsMessageDialog.showDialog();
        }

        // Modificar el mensaje del titlePane mostrando el número de resultados
        final String text = AbstractBbSearchForm.this.getMessage(//
                id + ".numberOfResults.caption", new Integer[] { resultsCount });
        this.getTitlePane().setMessage(new DefaultMessage(text, Severity.INFO));
    }
}
