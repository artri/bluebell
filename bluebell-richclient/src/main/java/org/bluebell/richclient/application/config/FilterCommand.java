package org.bluebell.richclient.application.config;

import javax.swing.JTable;

import org.springframework.richclient.command.ToggleCommand;
import org.springframework.util.Assert;

/**
 * Comando que muestra u oculta la cabecera de filtrado de una tabla de tipo {@link VLJTable}.
 * <p>
 * Si la tabla pasada como parámetro no es del tipo esperado entonces el comando permanece deshabilitado.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class FilterCommand extends ToggleCommand {

    /**
     * La tabla a filtrar.
     */
    private JTable table;

    /**
     * Construye el comando a partir de la tabla a filtrar.
     * 
     * @param table
     *            la tabla a filtrar.
     */
    public FilterCommand(JTable table) {

        this(null, table);
    }

    /**
     * Construye el comando a partir de su identificador y de la tabla a filtrar.
     * 
     * 
     * @param commandId
     *            el identificador del comando.
     * @param table
     *            la tabla a filtrar.
     */
    public FilterCommand(String commandId, JTable table) {

        super(commandId);

        this.setTable(table);
        // this.setEnabled(this.getTable() instanceof VLJTable);
    }

    /**
     * Obtiene la tabla a filtrar.
     * 
     * @return la tabla a filtrar.
     */
    public JTable getTable() {

        return this.table;
    }

    /**
     * Establece la tabla a filtrar.
     * 
     * @param table
     *            la tabla a filtrar.
     */
    public void setTable(JTable table) {

        this.table = table;
    }

    /**
     * Cambia la visibilidad de la cabecera de filtrado de la tabla.
     * 
     * @param selected
     *            <em>flag</em> indicando si se ha de seleccionar o deseleccionar el comando.
     * 
     * @return el valor devuelto por {@link ToggleCommand#onSelection(boolean)}.
     */
    @Override
    protected boolean onSelection(boolean selected) {

        Assert.isTrue(this.isEnabled());

        // Llegados a este punto el comando está habilitado y por tanto la
        // tabla es una VLJTable
        // ((VLJTable) this.table).setFilterHeaderVisible(selected);

        return super.onSelection(selected);
    }
}
