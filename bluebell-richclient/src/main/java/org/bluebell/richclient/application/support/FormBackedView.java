package org.bluebell.richclient.application.support;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bluebell.richclient.form.GlobalCommandsAccessor;
import org.springframework.richclient.application.PageComponentContext;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.command.support.GlobalCommandIds;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.AbstractMasterForm;
import org.springframework.richclient.form.Form;
import org.springframework.util.Assert;

/**
 * Implementación de una vista que admite un formulario para ser visualizada.
 * <p>
 * Utiliza un formulario (<code>Form</code>) para crear su <code>control</code> y un {@link GlobalCommandsAccessor} para
 * registrar los <em>local executors</em> de los comandos globales.
 * 
 * @param <T>
 *            el tipo del formulario.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class FormBackedView<T extends Form> extends AbstractView {

    /**
     * Un borde vacío con espacios.
     */
    private static final Border EMPTY_BORDER_WITH_GAPS = BorderFactory.createEmptyBorder(3, 3, 3, 3);

    /**
     * El formulario sobre el que se construye la vista.
     */
    private T backingForm;

    /**
     * El borde a aplicar sobre el formulario.
     */
    private Border border;

    /**
     * El <em>accesor</em> para la obtención de los comandos globales.
     */
    private GlobalCommandsAccessor globalCommandsAccessor;

    /**
     * Construye la vista.
     */
    public FormBackedView() {

        super();
    }

    /**
     * Obtiene el formulario.
     * 
     * @return el formulario.
     */
    public T getBackingForm() {

        return this.backingForm;
    }

    /**
     * Obtiene el borde a aplicar sobre el formulario y si no existe lo crea con valor {@link #EMPTY_BORDER_WITH_GAPS}.
     * 
     * @return el borde.
     */
    public Border getBorder() {

        // TODO, (JAF), study if this method should be configurable (UIManager)
        if (this.border == null) {
            this.setBorder(FormBackedView.EMPTY_BORDER_WITH_GAPS);
        }

        return this.border;
    }

    /**
     * Gets the globalCommandsAccessor.
     * 
     * @return the globalCommandsAccessor
     */
    public GlobalCommandsAccessor getGlobalCommandsAccessor() {

        return this.globalCommandsAccessor;
    }

    /**
     * Establece el formulario.
     * 
     * @param backingForm
     *            el formulario.
     */
    public void setBackingForm(T backingForm) {

        this.backingForm = backingForm;
    }

    /**
     * Establece el borde a aplicar sobre el formulario.
     * 
     * @param border
     *            el borde.
     */
    public void setBorder(Border border) {

        this.border = border;
    }

    /**
     * Establece el <em>accesor</em> para los comandos globales.
     * <p>
     * Además comprueba que en el momento de la invocación se haya establecido el contexto de la página, en caso
     * contrario elevará una excepción.
     * 
     * @param globalCommandsAccessor
     *            el <em>accesor</em>.
     */
    public void setGlobalCommandsAccessor(GlobalCommandsAccessor globalCommandsAccessor) {

        Assert.notNull(globalCommandsAccessor, "globalCommandsAccessor");
        Assert.notNull(this.getContext(), "this.getContext()");

        this.globalCommandsAccessor = globalCommandsAccessor;

        this.registerLocalCommandExecutors(this.getContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("id", this.getId()).toString();
    }

    /**
     * Crea el control a partir del formulario.
     * <p>
     * Produce una excepción en el caso de que el <em>backing form</em> sea nulo.
     * 
     * @return el control del formulario.
     */
    @Override
    protected JComponent createControl() {

        Assert.notNull(this.getBackingForm());
        // this.registerLocalCommandExecutors(this.getContext());

        // Envolver el control en un JScrollPane
        final JComponent control = this.getBackingForm().getControl();
        final JScrollPane scrollPane = new JScrollPane(control);

        // TODO, this code is substance dependent!!
        scrollPane.putClientProperty("substancelaf.componentFlat", Boolean.TRUE);
        control.putClientProperty("substancelaf.componentFlat", Boolean.FALSE);

        // Añadir los bordes
        // FIXME, (JAF), with substance this border can take a different color
        // from form one.
        control.setBorder(this.getBorder());
        scrollPane.setBorder(this.getBorder());

        return scrollPane;
    }

    /**
     * Registra los <em>command executor</em> locales para los comandos globales.
     * <p>
     * Para ello obtiene los comandos a partir de un {@link GlobalCommandsAccessor}.
     * 
     * @param context
     *            el contexto de la página.
     * 
     * @see AbstractView#registerLocalCommandExecutors(PageComponentContext)
     * @see GlobalCommandsAccessor
     */
    @Override
    protected void registerLocalCommandExecutors(PageComponentContext context) {

        // FIXME, no tengo claro que el registro de comandos globales funcione
        // sólo con esto, aunque puede que sí!!

        // TODO, (JAF), 20090919, esto debería de ir a la nueva clase que
        // sincroniza los componentes de la página.
        // Eso no está claro!!!!
        if (this.getGlobalCommandsAccessor() != null) {
            context.register(GlobalCommandIds.PROPERTIES, this.getGlobalCommandsAccessor().getNewFormObjectCommand());
            context.register(GlobalCommandIds.SAVE, this.getGlobalCommandsAccessor().getSaveCommand());
            context.register(GlobalCommandsAccessor.CANCEL, this.getGlobalCommandsAccessor().getCancelCommand());
            context.register(GlobalCommandIds.DELETE, this.getGlobalCommandsAccessor().getDeleteCommand());
            context.register(GlobalCommandsAccessor.REFRESH, this.getGlobalCommandsAccessor().getRefreshCommand());
            context.register(GlobalCommandsAccessor.REVERT, this.getRevertCommand(this.getBackingForm()));
            context.register(GlobalCommandsAccessor.REVERT_ALL, this.getGlobalCommandsAccessor().getRevertAllCommand());

            // (JAF), 20090630, este comando se deshabilita ya que puede causar
            // más problemas que beneficios...
            // context.register(GlobalCommandsAccesor.SELECT_ALL_ENTITIES, //
            // this.globalCommandsAccesor.getSelectAllCommand());

            // (JAF), 20090113, "TextComponentPopup" registra sus propios global
            // command executors
            context.register(GlobalCommandIds.CUT, null);
            context.register(GlobalCommandIds.COPY, null);
            context.register(GlobalCommandIds.PASTE, null);
            context.register(GlobalCommandIds.UNDO, null);
            context.register(GlobalCommandIds.REDO, null);
            // context.register(GlobalCommandIds.SELECT_ALL, null);
        }
    }

    /**
     * Obtiene la implementación del comando global {@value GlobalCommandIds#UNDO} dado el <em>backing form</em>.
     * 
     * @param backingForm
     *            el formulario.
     * @return la implementación del comando, <code>null</code> para los formularios maestros y formularios que no
     *         implementen <code>AbstractForm</code>.
     */
    private ActionCommand getRevertCommand(Form backingForm) {

        if (backingForm instanceof AbstractMasterForm) {
            return null;
        } else if (backingForm instanceof AbstractForm) {
            return ((AbstractForm) backingForm).getRevertCommand();
        }

        return null;
    }
}
