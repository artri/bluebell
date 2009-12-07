package org.bluebell.richclient.command.support;

import org.springframework.richclient.command.ActionCommand;

/**
 * Comando para resetear el layout de una página y volver al inicial.
 * 
 * Para su utilización es necesario crear el comando en el contexto de aplicación y referenciarlo, por ejemplo, desde la
 * <em>toolbar</em>:
 * 
 * <pre>
 * &lt;bean id=&quot;resetPerspectiveCommand&quot;
 *  class=&quot;org.bluebell.richclient.test.command.ResetPerspectiveCommand&quot; /&gt;
 * </pre>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ResetPerspectiveCommand extends ActionCommand {

    /**
     * The command id.
     */
    private static final String COMMAND_ID = "resetPerspectiveCommand";

    // /**
    // * The error code for this command.
    // */
    // private static final String ERROR_CODE = ResetPerspectiveCommand.COMMAND_ID + "Exception";

    /**
     * El constructor por defecto.
     */
    public ResetPerspectiveCommand() {

	super(ResetPerspectiveCommand.COMMAND_ID);
    }

    /**
     * Cambia el layout de una página mostrándola con su layout inicial.
     */
    // @SuppressWarnings("unchecked")
    @Override
    protected void doExecuteCommand() {

	// final BbVLDockingApplicationPage page = (BbVLDockingApplicationPage) Application.instance().getActiveWindow()
	// .getPage();

	// try {
	// ResourceUtils.getFile(page.getUserDefinedLayoutLocation()).delete();
	// if (page.isControlCreated()) {
	// final DockingDesktop dockingDesktop = (DockingDesktop) page.getControl();
	// final Resource defaultLayout = page.getDefaultLayout();
	// if (defaultLayout != null) {
	// final InputStream in = defaultLayout.getInputStream();
	// dockingDesktop.readXML(in);
	// in.close();
	// }
	// }
	//
	// } catch (final FileNotFoundException e) {
	// throw new BbApplicationException(new MessageFormat("No se ha encontrado el fichero {0}").format(//
	// new String[] { page.getUserDefinedLayoutLocation() }), e, ResetPerspectiveCommand.ERROR_CODE);
	// } catch (final IOException e) {
	// throw new BbApplicationException("Error E/S", e, ResetPerspectiveCommand.ERROR_CODE);
	// } catch (final ParserConfigurationException e) {
	// throw new BbApplicationException("Error in  VLDocking parser configuration", e,
	// ResetPerspectiveCommand.ERROR_CODE);
	// } catch (final SAXException e) {
	// throw new BbApplicationException("Error parsing XML file", e, ResetPerspectiveCommand.ERROR_CODE);
	// }
    }
}
