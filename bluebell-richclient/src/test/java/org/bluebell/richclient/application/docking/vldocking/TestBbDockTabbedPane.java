package org.bluebell.richclient.application.docking.vldocking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.UIManager;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.bluebell.richclient.application.docking.vldocking.BbDockTabbedPane;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockablePanel;

/**
 * Clase que prueba el correcto funcionamiento de {@link BbDockTabbedPane} .
 * 
 *@author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class TestBbDockTabbedPane extends TestCase {

    /**
     * El número original de pestañas de {@link #tabs}.
     */
    private static final int SIZE = 5;

    static {
	// Esta propiedad es requerida para la prueba
	UIManager.put("TabbedDockableContainer.tabPlacement", 1);
    }

    /**
     * El panel contenedor de pestañas objeto de la prueba.
     */
    private BbDockTabbedPane tabbedPane;

    /**
     * Las pestañas de {@link #tabs}.
     */
    private final Dockable[] tabs = new Dockable[TestBbDockTabbedPane.SIZE];

    /**
     * Caso que prueba el correcto funcionamiento de la inserción de pestañas en su forma más básica.
     * 
     * <ul>
     * <li>[]
     * <li>[0,1,2,3,4]
     * </ul>
     */
    public void testSimplestTabInsertion() {

	this.checkCorrectness();
    }

    /**
     * Caso que prueba que después de haber eliminado en orden ascendente todas las pestañas el sistema es capaz de
     * insertarlas en orden ascendente recuperando su posición original.
     * 
     * <ul>
     * <li>[]
     * <li>[0,1,2,3,4]
     * <li>[1,2,3,4]
     * <li>[2,3,4]
     * <li>[3,4]
     * <li>[4]
     * <li>[]
     * <li>[0]
     * <li>[0,1]
     * <li>[0,1,2]
     * <li>[0,1,2,3]
     * <li>[0,1,2,3,4]
     * </ul>
     */
    public void testTabRemovalAndInsertingForeward() {

	// Eliminar las pestañas en orden ascendente
	for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
	    this.tabbedPane.remove(0);
	}

	// Insertar las pestañas en orden ascendente
	for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {

	    TestCase.assertEquals(i, this.getIndexForDockable(i));
	    this.insertTab(this.tabs[i], this.getIndexForDockable(i));
	}

	// Comprobar que se ha recuperado la situación inicial
	this.checkCorrectness();
    }

    /**
     * Caso que prueba que después de haber eliminado en orden descendente todas las pestañas el sistema es capaz de
     * insertarlas en orden descendente recuperando su posición original.
     * 
     * <ul>
     * <li>[]
     * <li>[0,1,2,3,4]
     * <li>[0,1,2,3]
     * <li>[0,1,2]
     * <li>[0,1]
     * <li>[0]
     * <li>[]
     * <li>[4]
     * <li>[3,4]
     * <li>[2,3,4]
     * <li>[1,2,3,4]
     * <li>[0,1,2,3,4]
     * </ul>
     */
    public void testTabRemovalAndInsertingBackward() {

	// Eliminar las pestañas en orden descendente
	for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
	    this.tabbedPane.remove(this.tabbedPane.getTabCount() - 1);
	}

	// Insertar las pestañas en orden descendente
	for (int i = TestBbDockTabbedPane.SIZE - 1; i >= 0; --i) {
	    TestCase.assertEquals(0, this.getIndexForDockable(i));
	    this.insertTab(this.tabs[i], this.getIndexForDockable(i));
	}

	// Comprobar que se ha recuperado la situación inicial
	this.checkCorrectness();
    }

    /**
     * Caso que prueba que después de haber eliminado en orden ascendente todas las pestañas el sistema es capaz de
     * insertar pestañas adicionales antes de reinsertarlas en orden ascendente.
     * 
     * <ul>
     * <li>[]
     * <li>[0,1,2,3,4]
     * <li>[1,2,3,4]
     * <li>[2,3,4]
     * <li>[3,4]
     * <li>[4]
     * <li>[]
     * <li>[0bis]
     * <li>[0bis,1bis]
     * <li>[0bis,1bis,2bis]
     * <li>[0bis,1bis,2bis,3bis]
     * <li>[0bis,1bis,2bis,3bis,4bis]
     * <li>[0bis,1bis,2bis,3bis,4bis,0]
     * <li>[0bis,1bis,2bis,3bis,4bis,0,1]
     * <li>[0bis,1bis,2bis,3bis,4bis,0,1,2]
     * <li>[0bis,1bis,2bis,3bis,4bis,0,1,2,3]
     * <li>[0bis,1bis,2bis,3bis,4bis,0,1,2,3,4]
     * </ul>
     */
    public void testAditionalInsertion() {

	// Las pestañas adicionales
	final Dockable[] aditionalTabs = new Dockable[TestBbDockTabbedPane.SIZE];
	for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
	    aditionalTabs[i] = this.createDockable(i + "bis");
	}

	// Eliminar las pestañas en orden ascendente
	for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
	    this.tabbedPane.remove(0);
	}

	// Insertar las pestañas adicionales en orden ascendente
	for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
	    this.insertTab(aditionalTabs[i], i);
	}

	// Insertar las pestañas en orden ascendente
	for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
	    TestCase.assertEquals(i + TestBbDockTabbedPane.SIZE, //
		    this.getIndexForDockable(i));
	    this.insertTab(this.tabs[i], this.getIndexForDockable(i));
	}

	// Comprobar que todas las pestañas estén en su posición correcta.
	for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
	    TestCase.assertEquals(aditionalTabs[i], //
		    this.tabbedPane.getComponentAt(i));

	    TestCase.assertEquals(this.tabs[i], //
		    this.tabbedPane.getComponentAt(//
			    i + TestBbDockTabbedPane.SIZE));
	}
    }

    /**
     * Caso que prueba múltiples borrados e inserciones desordenados.
     */
    public void testTabRemovalAndInsertingRandom() {

	final int iterations = 10000;
	for (int i = 0; i < iterations; ++i) {
	    this.doTestTabRemovalAndInsertingRandom();
	}
    }

    /**
     * Caso que prueba borrados e inserciones desordenados.
     */
    protected void doTestTabRemovalAndInsertingRandom() {

	final List<Integer> removalOrder = new ArrayList<Integer>();
	final List<Integer> insertionOrder = Arrays.asList(new Integer[] { 0, 1, 2, 3, 4 });

	// Eliminar las pestañas en orden aleatorio
	for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
	    final int maxValue = TestBbDockTabbedPane.SIZE - i;
	    removalOrder.add(i, maxValue != 0 ? RandomUtils.nextInt(maxValue) //
		    : 0);
	    this.tabbedPane.remove(removalOrder.get(i));
	}

	// Insertar las pestañas en orden aleatorio
	Collections.shuffle(insertionOrder);
	for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
	    this.insertTab(this.tabs[insertionOrder.get(i)], //
		    this.getIndexForDockable(insertionOrder.get(i)));
	}

	// System.out.println(removalOrder);
	// System.out.println(insertionOrder);

	// Comprobar que se ha recuperado la situación inicial
	this.checkCorrectness();
    }

    /**
     * Crea un nuevo panel contenedor de pestañas antes de cada test.
     * <p>
     * El panel contiene {@value #SIZE} pestañas, cada una de las cuales es una <code>JLabel</code> con texto su
     * posición inicial (partiendo de 0).
     * 
     * @throws Exception
     *             en caso de error.
     */
    @Override
    protected void setUp() throws Exception {

	this.tabbedPane = new BbDockTabbedPane();

	for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
	    this.tabs[i] = this.createDockable(((Integer) i).toString());
	    this.insertTab(this.tabs[i], i);
	}
    }

    /**
     * Obtiene el índice más apropiado donde insertar la pestaña i-esima (relativa a {@link #tabs}).
     * 
     * @param i
     *            el índice relativo a <code>tabs</code>.
     * @return el índice relativo al panel.
     */
    private int getIndexForDockable(int i) {

	final int indexForDockable = this.tabbedPane.getIndexForDockable(this.tabs[i]);

	return indexForDockable != -1 ? indexForDockable //
		: 0;
    }

    /**
     * Inserta una pestaña en {@link #tabbedPane} en una posición dada.
     * 
     * @param tab
     *            la pestaña.
     * @param position
     *            la posición.
     */
    private void insertTab(Dockable tab, int position) {

	this.tabbedPane.insertTab(//
		tab.getDockKey().getName(), //
		null, //
		tab.getComponent(), //
		StringUtils.EMPTY, //
		position);
    }

    /**
     * Prueba que todas las pestañas estén en su posición correcta.
     */
    private void checkCorrectness() {

	for (int i = 0; i < TestBbDockTabbedPane.SIZE; ++i) {
	    TestCase.assertEquals(i, this.getIndexForDockable(i));
	}
    }

    /**
     * Crea un <code>Dockable</code> dado su nombre.
     * 
     * @param name
     *            el nombre del <code>Dockable</code>.
     * @return el <code>Dockable</code>.
     */
    private Dockable createDockable(String name) {

	final DockKey dockKey = new DockKey(name, name);

	return new DockablePanel(new JLabel(name), dockKey);
    }
}
