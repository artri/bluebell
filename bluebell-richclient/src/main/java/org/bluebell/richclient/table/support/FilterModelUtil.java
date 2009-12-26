package org.bluebell.richclient.table.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.richclient.table.support.GlazedTableModel;

import ca.odell.glazedlists.EventList;

/**
 * Clase <em>helper</em> que simplifica la gestión de indices en un <code>TableModel</code> siempre y cuando se utilicen
 * filtrados.
 * 
 * @see FilterModel
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public final class FilterModelUtil {

    /**
     * Constructor privado ya que es una clase de utilidad.
     */
    private FilterModelUtil() {

        super();
    }

    /**
     * Obtiene los índices de los elementos seleccionados de una tabla tal y como los visualiza el usuario.
     * 
     * @param jTable
     *            la tabla.
     * 
     * @return los índices.
     */
    public static List<Integer> getFilteredSelectedIdxs(JTable jTable) {

        final List<Integer> filteredIdxs = new ArrayList<Integer>();

        final ListSelectionModel sm = jTable.getSelectionModel();

        // Comprobar que exista al menos un elemento seleccionado.
        if (sm.isSelectionEmpty()) {
            return filteredIdxs;
        }

        // Iterar sobre los items seleccionados
        final int min = sm.getMinSelectionIndex();
        final int max = sm.getMaxSelectionIndex();

        for (int idx = max; idx >= min; idx--) {
            if (sm.isSelectedIndex(idx)) {
                filteredIdxs.add(idx);
            }
        }

        return filteredIdxs;
    }

    /**
     * Obtiene los índices de los elementos seleccionados de una tabla relativos al modelo original y los devuelve en
     * orden descendente.
     * 
     * @param jTable
     *            la tabla.
     * 
     * @return los índices.
     */
    @SuppressWarnings("unchecked")
    public static List<Integer> getOriginalSelectedIdxs(final JTable jTable) {

        final List<Integer> filteresIdxs = FilterModelUtil.getFilteredSelectedIdxs(jTable);
        final List<Integer> originalIdxs = (List<Integer>) CollectionUtils.collect(//
                filteresIdxs, new Transformer() {

                    public Object transform(Object input) {

                        return FilterModelUtil.getRealRowNumber(//
                                jTable.getModel(), (Integer) input);
                    }

                });

        Collections.sort(originalIdxs, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {

                return o1.compareTo(o2) * -1;
            }
        });

        return originalIdxs;
    }

    /**
     * Obtiene el índice real a partir del índice respectivo a la relación de entidades visualizadas por el usuario
     * (modelo filtrado).
     * 
     * @param tableModel
     *            el modelo objeto de consulta.
     * @param indexInFilterModel
     *            el índice en el modelo de filtrado.
     * @return el índice en el modelo original.
     */
    public static int getRealRowNumber(TableModel tableModel, int indexInFilterModel) {

        if (indexInFilterModel < 0) {
            return indexInFilterModel;
        }
        // else if (tableModel instanceof FilterModel) {
        // return ((FilterModel) tableModel).getSourceRow(indexInFilterModel);
        // }

        return indexInFilterModel;
    }

    /**
     * Obtiene los índices reales a partir de los índices respectivos a la relación de entidades visualizadas por el
     * usuario (modelo filtrado).
     * 
     * @param tableModel
     *            el modelo objeto de consulta.
     * @param indexesInFilterModel
     *            los índice en el modelo de filtrado.
     * @return el índice en el modelo original.
     */
    public static int[] getRealRowNumbers(TableModel tableModel, int[] indexesInFilterModel) {

        final int[] indexesInOriginalModel = new int[indexesInFilterModel.length];

        for (int i = 0; i < indexesInFilterModel.length; ++i) {
            indexesInOriginalModel[i] = FilterModelUtil.getRealRowNumber(//
                    tableModel, indexesInFilterModel[i]);
        }

        return indexesInOriginalModel;
    }

    /**
     * Obtiene la relación de entidades que el usuario está visualizando.
     * 
     * @param <T>
     *            el tipo de las entidades.
     * @param tableModel
     *            el modelo objeto de consulta.
     * @param masterEventList
     *            la lista con todas las entidades.
     * @return las entidades visualizadas por el usuario.
     */
    public static <T> List<T> getRowsAsViewed(TableModel tableModel, EventList<T> masterEventList) {

        // TODO, (JAF), 20080611, el hecho de pasar como parámetro
        // masterEventList quizás rompa la encapsulación, pero se ha hecho así
        // por simplicidad. Quizás sea posible obtenerlas a partir del
        // FilterModel.

        // if (tableModel instanceof FilterModel) {
        // final List<T> rows = new ArrayList<T>();
        //
        // final FilterModel filterModel = (FilterModel) tableModel;
        // for (int i = 0; i < filterModel.getRowCount(); ++i) {
        // final int sourceRow = filterModel.getSourceRow(i);
        // final T t = masterEventList.get(sourceRow);
        // rows.add(t);
        // }
        //
        // return rows;
        // }

        return masterEventList;
    }

    /**
     * Reemplaza las entidades listadas en una tabla.
     * <p>
     * Es conveniento utilizar este método ya que cuando se trabaja con <code>EventList</code> el coste de las
     * operaciones es realmente importante. Este método desactiva la gestión de eventos mientras dure el reemplazo, una
     * vez finalizado notifica de que la tabla ha cambiado.
     * 
     * @param <T>
     *            el tipo de las entidades.
     * @param tableModel
     *            el modelo de la tabla.
     * @param masterEventList
     *            la lista sobre la que se construye el modelo de la tabla.
     * @param entities
     *            las entidades a mostrar en la tabla.
     */
    public static <T> void replace(TableModel tableModel, EventList<T> masterEventList, Collection<T> entities) {

        // TODO, (JAF), 20081002, el hecho de pasar como parámetro
        // masterEventList quizás rompa la encapsulación, pero se ha hecho así
        // por simplicidad. Quizás sea posible obtenerlas a partir del
        // FilterModel.

        // Obtener el table model envuelto si es que lo hubiera
        final GlazedTableModel glazedTableModel = FilterModelUtil.unwrapTableModel(tableModel);

        if (glazedTableModel != null) {
            final TableModelListener[] listeners = glazedTableModel.getTableModelListeners();

            // Desinstalar los listeners del modelo de la tabla.
            for (final TableModelListener listener : listeners) {
                glazedTableModel.removeTableModelListener(listener);
            }

            // Reemplazar las entidades de la tabla.
            // Estas dos operaciones son casi igual de pesadas pero no es
            // posible fusionarlas.
            masterEventList.clear();
            masterEventList.addAll(entities);

            // Instalar de nuevo los listeners.
            for (final TableModelListener listener : listeners) {
                glazedTableModel.addTableModelListener(listener);
            }

            // Notificar que se ha producido un cambio.
            glazedTableModel.fireTableDataChanged();
        }
    }

    /**
     * Permite seleccionar una serie de entidades en una tabla.
     * <p>
     * Si la entidad no pertenece a la tabla entonces la añade si sólo si el parámetro <code>addIfNotFound</code> es
     * <code>true</code> y si el modelo no la filtra, entonces la selecciona.
     * 
     * @param <Q>
     *            el tipo de las entidades contenidas en la tabla.
     * @param eventList
     *            la lista sobre la que se construye el modelo de la tabla.
     * @param table
     *            la tabla.
     * @param addIfNotFound
     *            indica si se ha de añadir una entidad a la tabla si no estaba siendo visualizada.
     * @param entities
     *            las entidades a seleccionar.
     */
    @SuppressWarnings("unchecked")
    public static <Q> void setSelectedEntities(EventList<Q> eventList, JTable table, Collection<Q> entities,
            Boolean addIfNotFound) {

        // Si la entidad no pertenece a la event list entonces añadirla.
        for (final Q entity : entities) {
            final int foundAt = eventList.indexOf(entity);
            if (foundAt != -1) {
                eventList.set(foundAt, entity);
            } else if (addIfNotFound) {
                eventList.add(entity);
            }
        }

        // Obtener la posición de la entidad relativa al orden con el que el
        // usuario las visualiza.
        final List viewedRows = FilterModelUtil.getRowsAsViewed(table.getModel(), eventList);

        // Limpiar la selección
        table.clearSelection();
        for (final Q entity : entities) {
            final int indexToSelect = viewedRows.indexOf(entity);

            // Seleccionar la fila referenciada por el índice obtenido
            if (indexToSelect >= 0) {
                table.addRowSelectionInterval(indexToSelect, indexToSelect);
            }
        }
    }

    /**
     * Permite seleccionar una entidad en una tabla.
     * <p>
     * Si la entidad no pertenece a la tabla entonces la añade y si el modelo no la filtra, entonces la selecciona.
     * 
     * @param <Q>
     *            el tipo de las entidades contenidas en la tabla.
     * @param eventList
     *            la lista sobre la que se construye el modelo de la tabla.
     * @param table
     *            la tabla.
     * @param entity
     *            la entidad a seleccionar.
     */
    @SuppressWarnings("unchecked")
    public static <Q> void setSelectedEntity(EventList<Q> eventList, JTable table, Q entity) {

        // Si la entidad no pertenece a la event list entonces añadirla.
        final int foundAt = eventList.indexOf(entity);
        if (foundAt != -1) {
            // JAF, 20090325, si la encuentra la reestablece
            eventList.set(foundAt, entity);
        } else {
            eventList.add(entity);
        }

        // Obtener la posición de la entidad relativa al orden con el que el
        // usuario las visualiza.
        final List viewedRows = FilterModelUtil.getRowsAsViewed(table.getModel(), eventList);
        final int indexToSelect = viewedRows.indexOf(entity);

        // Seleccionar la fila referenciada por el índice obtenido.
        if (indexToSelect >= 0) {
            table.setRowSelectionInterval(indexToSelect, indexToSelect);
        }
    }

    /**
     * Dado un <code>TableModel</code> comprueba si es de tipo {@link FilterModel}, en cuyo caso retorna el modelo que
     * envuelve si sólo si es de tipo {@link GlazedTableModel}.
     * 
     * @param tableModel
     *            el modelo.
     * @return el modelo envuelto si existe y es de tipo <code>GlazedTableModel</code>, si no se cumple ninguna de estas
     *         condiciones devuelve <code>null</code>.
     */
    private static GlazedTableModel unwrapTableModel(TableModel tableModel) {

        // TODO delete dependency to FilterModel and improve this code
        return (GlazedTableModel) tableModel;

        // if (!(tableModel instanceof FilterModel)) {
        // return null;
        // }
        //
        // // Obtener y retornar el modelo envuelto si es del tipo apropiado
        // final TableModel wrappedTableModel = ((FilterModel)
        // tableModel).getModel();
        //
        // return wrappedTableModel instanceof GlazedTableModel ?
        // (GlazedTableModel) wrappedTableModel : null;
    }
}
