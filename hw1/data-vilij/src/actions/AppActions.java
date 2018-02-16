package actions;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.templates.ApplicationTemplate;
import javafx.stage.FileChooser;
import java.io.IOException;
import java.nio.file.Path;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent  {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    Path dataFilePath;

    public AppActions(ApplicationTemplate applicationTemplate)
    {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void handleNewRequest()   {

        applicationTemplate.getUIComponent().clear();
    }

    @Override
    public void handleSaveRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleLoadRequest() {
        // TODO: NOT A PART OF HW 1
    }

    @Override
    public void handleExitRequest() {
        AppUI ui = (AppUI)applicationTemplate.getUIComponent();
        if(!ui.getTextArea().equals("")) {
            ConfirmationDialog confirm = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
            confirm.show("EXIT_WHILE_RUNNING_WARNING", "An algorithm is running. If you exit now, all unsaved changes will be lost. Are you sure?");
            if (confirm.getSelectedOption() == ConfirmationDialog.Option.YES)
                System.exit(0);
            else
                confirm.close();
        }else
            System.exit(0);
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        // TODO: NOT A PART OF HW 1
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        ConfirmationDialog confirm = (ConfirmationDialog) applicationTemplate.getDialog(Dialog.DialogType.CONFIRMATION);
        confirm.show("SAVE","Do you want to save the text? ");
        if(confirm.getSelectedOption() == ConfirmationDialog.Option.CANCEL)
        {
            confirm.close();
            return false;
        }
        else {
            if(confirm.getSelectedOption() == ConfirmationDialog.Option.YES)
            {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TSD files (*.tsd)", "*.tsd");
                fileChooser.getExtensionFilters().add(extFilter);

                File file = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

                FileWriter writer = new FileWriter(file);
                AppUI ui = (AppUI)applicationTemplate.getUIComponent();
                writer.write(ui.getTextArea());
            }
            else
                {

                }
                return true;
        }
    }
}
