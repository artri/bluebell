package org.bluebell.richclient.command.support;

import org.bluebell.richclient.application.docking.vldocking.BbVLDockingApplicationPage;
import org.springframework.richclient.application.Application;
import org.springframework.richclient.command.ActionCommand;

/**
 * Command responsible for resetting current page layout.
 * 
 * Para su utilización es necesario crear el comando en el contexto de aplicación y referenciarlo, por ejemplo, desde la
 * <em>toolbar</em>:
 * 
 * <pre>
 * &lt;bean id=&quot;resetPerspectiveCommand&quot;
 *  class=&quot;org.bluebell.richclient.command.support.ResetPerspectiveCommand&quot; /&gt;
 * </pre>
 * 
 * @author <a href = "mailto:julio.arguello@gmail.com" >Julio Argüello (JAF)</a>
 */
public class ResetPerspectiveCommand extends ActionCommand {

    /**
     * The command id.
     */
    private static final String COMMAND_ID = "resetPerspectiveCommand";

    /**
     * Constructs the command using the default command id.
     */
    public ResetPerspectiveCommand() {

        super(ResetPerspectiveCommand.COMMAND_ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doExecuteCommand() {

        final BbVLDockingApplicationPage<?> page = (BbVLDockingApplicationPage<?>) //
        Application.instance().getActiveWindow().getPage();

        page.resetLayout();
    }
}
