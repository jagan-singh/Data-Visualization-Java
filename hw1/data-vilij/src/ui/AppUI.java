package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.geometry.Pos;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.chart.NumberAxis;
import java.io.IOException;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */

public final class AppUI extends UITemplate {

    /**
     * The application to which this class of actions belongs.
     */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button scrnshotButton; // toolbar button to take a screenshot of the data
    private ScatterChart<Number, Number> chart;          // the chart where data will be displayed
    private Button displayButton;  // workspace button to display data on the chart
    private TextArea textArea;       // text area for new data input
    private boolean hasNewText;// whether or not the text area has any new data since last display
    private String scrnpath;
    private HBox hbox;
    private VBox vbox;
    AppData app;

    public ScatterChart<Number, Number> getChart() {
        return chart;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new ScatterChart<Number, Number>(xAxis, yAxis);
        chart.setTitle("Data Visualization");
        textArea = new TextArea();
        displayButton = new Button("Display");
        app = new AppData(applicationTemplate);
        scrnshotButton = new Button();
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
        //String iconsPath = "/" + String.join("/", AppPropertyTypes.DATA_RESOURCE_PATH.SCREENSHOT_ICON.name()) ;
      //scrnpath = "/"+  String.join("/", AppPropertyTypes.DATA_RESOURCE_PATH.SCREENSHOT_ICON.name());

    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        //scrnshotButton = setToolbarButton(scrnpath, AppPropertyTypes.DATA_RESOURCE_PATH.SCREENSHOT_TOOLTIP.name() , true);
        //toolBar.getItems().add(scrnshotButton);

    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> {
            try {
                applicationTemplate.getActionComponent().handleNewRequest();
            } catch (IOException e1) {
                ErrorDialog dialog =  (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                dialog.show(AppPropertyTypes.DATA_RESOURCE_PATH.RESOURCE_SUBDIR_NOT_FOUND.name(),e1.getMessage());
            }catch (NullPointerException n){
                ErrorDialog dialog =  (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                dialog.show("Null Pointer Exception","No response chosen");
            }
        });
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
       /*scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions)applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });*/
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        app.clear();
        textArea.clear();
        chart.getData().clear();
    }

    public String getTextArea()
    {
        return textArea.getText();
    }

    private void layout() {
        hbox = new HBox(10);
        vbox = new VBox(10);
        Text text = new Text("Data File");
        vbox.getChildren().addAll(text, textArea, displayButton);
        vbox.setAlignment(Pos.TOP_CENTER);
        hbox.getChildren().addAll(vbox, chart);
        appPane.getChildren().add(hbox);
        applicationTemplate.getDialog(Dialog.DialogType.ERROR).init(primaryStage);
        textArea.textProperty().addListener(e -> {
                    newButton.setDisable(false);
                    saveButton.setDisable(false);
                    hasNewText = true;
                    if(textArea.getText().equals("")) {
                        newButton.setDisable(true);
                        saveButton.setDisable(true);
                    }
                }
        );
    }

    private void setWorkspaceActions() {
        displayButton.setOnAction(e ->
                {
                    if(hasNewText) {
                        try {
                            chart.getData().clear();
                            app.loadData(textArea.getText());
                        } catch (Exception ex) {
                            applicationTemplate.getDialog(Dialog.DialogType.ERROR).show("ERROR", "Invalid format");
                        }
                    }
                    hasNewText = false;
                }
        );
    }
}
