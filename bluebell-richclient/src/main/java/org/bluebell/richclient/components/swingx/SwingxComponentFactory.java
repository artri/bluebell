/**
 * 
 */
package org.bluebell.richclient.components.swingx;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;
import org.springframework.richclient.factory.DefaultComponentFactory;

/**
 * Swingx based component factory implementation.
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class SwingxComponentFactory extends DefaultComponentFactory {

    /**7
     * Creates a table.
     * 
     * @return the table.
     * 
     * @see org.springframework.richclient.factory.ComponentFactory#createTable()
     * @see JXTable
     */
    @Override
    public JTable createTable() {

	final JXTable table = new JXTable();
	
	table.setColumnControlVisible(Boolean.TRUE);

	// Sizes the scrollpane to be the same size as the table.
	// jTable.setPreferredScrollableViewportSize(jTable.getPreferredSize());

	return table;
    }

    /**
     * Creates a table given its model.
     * 
     * @param tableModel
     *            the table model.
     * @return the table.
     * 
     * @see org.springframework.richclient.factory.ComponentFactory#createTable(TableModel)
     * @see #createTable()
     * @see JXTable
     */
    @Override
    public JTable createTable(TableModel tableModel) {

	final JTable table = this.createTable();
	table.setModel(tableModel);

	return table;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent createToolBar() {

	final JToolBar toolBar = (JToolBar) super.createToolBar();
	toolBar.setFloatable(Boolean.TRUE);
	toolBar.setEnabled(Boolean.TRUE);

	return toolBar;
    }
}
