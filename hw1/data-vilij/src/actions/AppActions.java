package actions;

import java.io.File;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.templates.ApplicationTemplate;
import javafx.stage.FileChooser;
import java.io.IOException;
import java.nio.file.Path;
import javafx.beans.property.SimpleBooleanProperty;
import vilij.propertymanager.PropertyManager;
import javafx.stage.FileChooser.ExtensionFilter;
import vilij.components.ErrorDialog;
import vilij.settings.PropertyTypes;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;
import java.net.URL;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;

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
    SimpleBooleanProperty isUnsaved;
    private int numsave = 0;
    private ArrayList<String> list;

    public AppActions(ApplicationTemplate applicationTemplate)
    {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
    }

    public void setIsUnsavedProperty(boolean property) { isUnsaved.set(property); }

    public boolean getIsUnsavedProperty(){ return isUnsaved.get(); }

    @Override
    public void handleNewRequest() {
        AppUI ui = (AppUI)applicationTemplate.getUIComponent();
        try {
            if (!isUnsaved.get() || promptToSave()) {
                applicationTemplate.getDataComponent().clear();
                applicationTemplate.getUIComponent().clear();
                isUnsaved.set(false);
                dataFilePath = null;
                numsave = 0;
                ui.newAct();
                ui.setLoaded(false);
            }
        } catch (IOException e) { errorHandlingHelper(); }

    }

    @Override
    public void handleSaveRequest() {
        if(numsave == 0) {
            File selected = chooser().showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
            if (selected != null) {
                dataFilePath = selected.toPath();
                save();
            }
        }
        else if(numsave >= 1)
                save();
        numsave++;
        }


    @Override
    public void handleLoadRequest() {
        AppUI ui = (AppUI)applicationTemplate.getUIComponent();
        try {
            if (!ui.getTextArea().equals("")) {
                promptToSave();
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }

        File file = chooser().showOpenDialog(null);
        if (file != null) {
            try {
            Scanner scanner = new Scanner(file);
            String lines = "";
            list = new ArrayList<String>();
            while (scanner.hasNextLine()) {
                list.add(scanner.nextLine());
            }
            for(int i =0; i < list.size();i++)
            {
                lines += list.get(i)+ '\n';
            }
                if (ui.checkText(lines) && ui.checkDuplicates(lines)) {
                if(list.size() <= 10) {
                    ui.setTextArea(lines);
                }else {
                    ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    PropertyManager manager = applicationTemplate.manager;
                    dialog.show("Loaded Data", "Loaded data consists of " + list.size() + " lines. Showing the first 10 lines in the text area.");
                    String ten = new String();
                    for(int i=0;i<10;i++) {
                        ten += list.get(i) + '\n';
                    }
                    ui.setTextArea(ten);
                }
                    ui.setFileData(lines);
                    ui.setLoaded(true);
                    ui.loaded();
                    ui.leftVisiblity(true);
                    ui.infoMsg(file.getPath());
                    try {
                        ui.setSet(file.toPath());
                    }
                    catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }
            }
            scanner.close();
            } catch (FileNotFoundException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    @Override
    public void handleExitRequest() {
        AppUI ui = (AppUI)applicationTemplate.getUIComponent();
        try {
            if(ui.getAlgRunning())
                exitDialog();
            else if (!isUnsaved.get() || promptToSave())
                System.exit(0);
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        WritableImage image = ((AppUI)applicationTemplate.getUIComponent()).getChart().snapshot(new SnapshotParameters(), null);
        PropertyManager manager = applicationTemplate.manager;
        FileChooser fileChooser = new FileChooser();
        String      dataDirPath = "/" + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        URL         dataDirURL  = getClass().getResource(dataDirPath);
        fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
        File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if (selected != null) {
            dataFilePath = selected.toPath();
            save();
        }
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", selected);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
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
        PropertyManager manager = applicationTemplate.manager;
        ConfirmationDialog dialog = ConfirmationDialog.getDialog();
        dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));

        if(dialog.getSelectedOption() == null) return false;

        if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            if (dataFilePath == null) {
                FileChooser fileChooser = new FileChooser();
                String      dataDirPath = "/" + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
                URL         dataDirURL  = getClass().getResource(dataDirPath);

                if (dataDirURL == null)
                    throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));

                fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
                fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

                String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
                String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
                ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                        String.format("*.%s", extension));

                fileChooser.getExtensionFilters().add(extFilter);
                File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                if (selected != null) {
                    dataFilePath = selected.toPath();
                    save();
                } else return false; // if user presses escape after initially selecting 'yes'
            } else
                save();
        }
        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);
    }

    private void save(){
        applicationTemplate.getDataComponent().saveData(dataFilePath);
        isUnsaved.set(false);
    }

    private void errorHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }

   private FileChooser chooser()
   {
       PropertyManager manager = applicationTemplate.manager;
       FileChooser fileChooser = new FileChooser();
       String      dataDirPath = "/" + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
       URL         dataDirURL  = getClass().getResource(dataDirPath);
       fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
       String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
       String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
       ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
               String.format("*.%s", extension));
       fileChooser.getExtensionFilters().addAll(extFilter);
       return fileChooser;
   }

   private void exitDialog() {
       PropertyManager manager = applicationTemplate.manager;
       Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
       alert.setTitle(manager.getPropertyValue(AppPropertyTypes.ALERT.name()));
       alert.setHeaderText(manager.getPropertyValue(AppPropertyTypes.ALGORITHM_RUNNING_TITLE.name()));
       alert.setContentText(manager.getPropertyValue(AppPropertyTypes.EXIT_WHILE_RUNNING_WARNING.name()));

       ButtonType yesBtn = new ButtonType("Yes");
       ButtonType noBtn = new ButtonType("No");

       alert.getButtonTypes().setAll(yesBtn, noBtn);

       Optional<ButtonType> result = alert.showAndWait();
       if (result.get() == yesBtn)
          System.exit(1);
       else
          alert.close();

   }

}

