package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import javafx.scene.text.Text;
import javafx.scene.chart.NumberAxis;
import java.io.IOException;
import javafx.scene.text.Font;
import javafx.geometry.Insets;
import static settings.AppPropertyTypes.SCREENSHOT_ICON;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

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
    private LineChart<Number, Number> chart;          // the chart where data will be displayed
    private Button displayButton;  // workspace button to display data on the chart
    private TextArea textArea;       // text area for new data input
    private boolean hasNewText;// whether or not the text area has any new data since last display
    private String scrnpath;
    CheckBox checkBox;

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join("/", manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnpath = String.join("/", iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnpath,
                manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                true);
        toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> {
            try {
                applicationTemplate.getActionComponent().handleNewRequest();
                scrnshotButton.setDisable(true);
            } catch (IOException e1) {
                ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                dialog.show(AppPropertyTypes.DATA_RESOURCE_PATH.RESOURCE_SUBDIR_NOT_FOUND.name(), e1.getMessage());
            } catch (NullPointerException n) {
                ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                dialog.show("Null Pointer Exception", "No response chosen");
            }
        });
            saveButton.setOnAction(e ->
            {
                if(checkText(textArea.getText()) && checkDuplicates()) {
                    applicationTemplate.getActionComponent().handleSaveRequest();
                    if (!((AppActions) applicationTemplate.getActionComponent()).getIsUnsavedProperty())
                        saveButton.setDisable(true);
                }
            });
            loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions) applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException e1) {
                ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                dialog.show(AppPropertyTypes.DATA_RESOURCE_PATH.RESOURCE_SUBDIR_NOT_FOUND.name(), e1.getMessage());
            }
        });
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
    }

    public String getTextArea() {
        return textArea.getText();
    }

    public void setTextArea(String str)
    {
        textArea.setText(str);
    }

    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));

        VBox leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));

        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.345);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.3);

        Text leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        textArea = new TextArea();

        HBox processButtonsBox = new HBox();
        displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_BUTTON_TEXT.name()));
        checkBox = new CheckBox();
        checkBox.setText("Read Only");
        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.setSpacing(20);
        checkBox.setSelected(false);
        processButtonsBox.getChildren().addAll(checkBox,displayButton);

        leftPanel.getChildren().addAll(leftPanelTitle, textArea, processButtonsBox);

        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);
        appPane.getStylesheets().add("cse219.css");
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setDisplayButtonActions();
        setCheckBoxActions();
    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    if (!newValue.isEmpty()) {
                        ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                        if (newValue.charAt(newValue.length() - 1) == '\n'  || newValue.matches("@\\S+\\t\\S+\\t\\d+,\\d+")) {
                            newButton.setDisable(false);
                            saveButton.setDisable(false);
                        }
                    } else {
                        newButton.setDisable(true);
                        saveButton.setDisable(true);
                    }
                    hasNewText =true;
                }
                else
                    hasNewText = false;
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });
    }

    private void setDisplayButtonActions() {
        displayButton.setOnAction(event -> {
            if(textArea.getText().equals("")) {
                chart.getData().clear();
                scrnshotButton.setDisable(true);
            }
            else if (hasNewText) {
                try {
                    if(checkText(textArea.getText()) && checkDuplicates()) {
                        chart.getData().clear();
                        AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                        dataComponent.clear();
                        dataComponent.loadData(textArea.getText());
                        dataComponent.displayData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(chart.getData().size() == 0)
                scrnshotButton.setDisable(true);
            else
                scrnshotButton.setDisable(false);
        });
    }

    private void setCheckBoxActions(){
        checkBox.selectedProperty().addListener(e -> {
            if(checkBox.selectedProperty().get()) {
                textArea.setEditable(false);
                textArea.setStyle( "-fx-text-fill: gray");
            }
            else {
                textArea.setEditable(true);
                textArea.setStyle( "-fx-text-fill: black");
            }
        });
    }

    public boolean checkText(String str)
    {
        Boolean bool = true;
        String line = "";
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String lines[] = str.split("\\r?\\n");
        for(int i=0;i<lines.length;i++)
        {
            if (!lines[i].matches("@\\S+\\t\\S+\\t-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?")) {
               line += (i+1) + "  ";
               bool = false;
            }
        }
        if(!bool) {
            String errTitle = manager.getPropertyValue(AppPropertyTypes.INVALID_FORMAT_TITLE.name());
            String errMsg = manager.getPropertyValue(AppPropertyTypes.INVALID_FORMAT.name());
            dialog.show(errTitle, errMsg + " " + line);
        }
        return bool;
    }

   private boolean checkDuplicates()
   {
       ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
       PropertyManager manager  = applicationTemplate.manager;
       String str = textArea.getText();
       String lines[] = str.split("\\r?\\n");
       String[] first = new String[lines.length];
       for(int i=0;i<lines.length;i++)
           first[i] = lines[i].substring(0, lines[i].indexOf("\t"));

       for (int i = 0; i < first.length; i++) {
           for (int j = i+1; j < first.length; j++) {
               if(first[i].equals(first[j])) {
                   String errTitle = manager.getPropertyValue(AppPropertyTypes.DUPLICATES_TITLE.name());
                   String errMsg = manager.getPropertyValue(AppPropertyTypes.DUPLICATES.name());
                   dialog.show(errTitle, errMsg + "\nLines " + (i+1) + " and " + (j+1) + " have identicals names.");
                   return false;
               }
           }
       }
       return true;
   }
}