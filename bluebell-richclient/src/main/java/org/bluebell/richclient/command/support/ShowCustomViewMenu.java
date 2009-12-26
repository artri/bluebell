package org.bluebell.richclient.command.support;

import org.apache.commons.lang.StringUtils;
import org.springframework.richclient.application.ApplicationServicesLocator;
import org.springframework.richclient.application.ApplicationWindow;
import org.springframework.richclient.application.ViewDescriptor;
import org.springframework.richclient.application.ViewDescriptorRegistry;
import org.springframework.richclient.application.config.ApplicationWindowAware;
import org.springframework.richclient.command.CommandGroup;

/**
 * Menú con la capacidad de visualizar las vistas previamente configuradas a través de sus descriptores.
 * 
 * @see org.springframework.richclient.command.support.ShowViewMenu
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ShowCustomViewMenu extends CommandGroup implements ApplicationWindowAware {

    /**
     * El identificador del menú.
     */
    public static final String ID = "showCustomViewMenu";

    /**
     * Los identificadores de los descriptores de las vistas a incluir en el menú.
     */
    private String[] viewDescriptors;

    /**
     * La ventana en la que se renderiza el menú.
     */
    private ApplicationWindow window;

    /**
     * Crea un nuevo menú de tipo {@code ShowCustomViewMenu} con el identificador {@value #ID}.
     */
    public ShowCustomViewMenu() {

        this(ShowCustomViewMenu.ID);
    }

    /**
     * Crea un nuevo menú de tipo {@code ShowCustomViewMenu} con el identificador dado.
     * 
     * @param id
     *            el identificador del menú.
     */
    public ShowCustomViewMenu(String id) {

        super(id);
        this.viewDescriptors = new String[0];
    }

    /**
     * Crea los comandos que conforman este menú una vez fijadas las dependencias.
     */
    @Override
    public void afterPropertiesSet() {

        super.afterPropertiesSet();
        this.populate();
    }

    /**
     * Obtiene los identificadores de los descriptores de las vistas a utilizar.
     * 
     * @return los identificadores de los descriptores.
     */
    public String[] getViewDescriptors() {

        return this.viewDescriptors;
    }

    /**
     * {@inheritDoc}
     */
    public void setApplicationWindow(ApplicationWindow window) {

        this.window = window;
    }

    /**
     * Establece los identificadores de los descriptores de las vistas a utilizar.
     * 
     * @param viewDescriptors
     *            los identificadores de los descriptores.
     */
    public void setViewDescriptors(String[] viewDescriptors) {

        this.viewDescriptors = viewDescriptors;
    }

    /**
     * Crea el menú.
     */
    private void populate() {

        // El servicio "ViewDescriptorRegistry"
        final ViewDescriptorRegistry viewDescriptorRegistry = (ViewDescriptorRegistry) ApplicationServicesLocator
                .services().getService(ViewDescriptorRegistry.class);

        // Iterar sobre los descriptores y añadir los comandos al menú
        for (final String viewDescriptorId : this.getViewDescriptors()) {
            if ((viewDescriptorId != null) && (!viewDescriptorId.equals(StringUtils.EMPTY))) {
                final ViewDescriptor viewDescriptor = viewDescriptorRegistry.//
                        getViewDescriptor(viewDescriptorId.trim());
                this.addInternal(viewDescriptor.createShowViewCommand(this.window));
            }
        }
    }
}
