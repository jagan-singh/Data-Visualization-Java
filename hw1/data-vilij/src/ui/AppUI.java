package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.chart.NumberAxis;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.geometry.Insets;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import settings.AppPropertyTypes;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
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
    private String displayPath;
    private CheckBox checkBox;
    private boolean loaded = false;
    private String fileData = new String();
    private ToggleButton edit;
    private ToggleButton done;
    private VBox leftPanel;
    private Text leftPanelTitle;
    private VBox algv;
    private HBox ed;
    private Label info;
    private Button clustering;
    private Button classification;
    private VBox algorithms;


    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    public LineChart<Number, Number> getChart() {
        return chart;
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

        leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));

        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        //leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.50);
        //leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.3);

        leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        textArea = new TextArea();

        HBox processButtonsBox = new HBox();
       // Image cameraIcon = new Image(getClass().getClassLoader().getResourceAsStream("@display.png"));
        //ImageView cameraIconView = new ImageView(cameraIcon);
       displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_BUTTON_TEXT.name()));

         info = new Label();
         //info.setMaxSize(windowWidth * 0.29, windowHeight * 0.50);
        info.setWrapText(true);

       //toggle buttons
        edit = new ToggleButton("Edit");
        done = new ToggleButton("Done");
        ToggleGroup group = new ToggleGroup();
        edit.setToggleGroup(group);
        done.setToggleGroup(group);
        edit.setDisable(true);

         ed = new HBox();
         ed.getChildren().addAll(edit,done);
         ed.setSpacing(10);

        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.setSpacing(20);
        //checkBox.setSelected(false);
        //processButtonsBox.getChildren().addAll(checkBox);

        leftPanel.getChildren().addAll(leftPanelTitle, textArea,info, processButtonsBox,ed,displayButton);

        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);
        appPane.getStylesheets().add("cse219.css");

        leftVisiblity(false);
        newButton.setDisable(false);
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setDisplayButtonActions();
        setAlgTypeAction();
        toggleActions();
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
            if(loaded){
                chart.getData().clear();
                AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                dataComponent.clear();
                dataComponent.loadData(fileData);
                dataComponent.displayData();
            }
            else
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

   /* private void setCheckBoxActions(){
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
    }*/

    private void toggleActions()
    {
        algv = new VBox();
        clustering = new Button("Clustering");
        classification = new Button("Classification");
        algv.getChildren().addAll(clustering,classification);

        edit.setOnAction( e -> {
            edit.setDisable(true);
            done.setDisable(false);
            textArea.setEditable(true);
            textArea.setStyle( "-fx-text-fill: black");
            leftPanel.getChildren().remove(algv);
        });

        done.setOnAction(e -> {
            if (checkText(textArea.getText()) && checkDuplicates())
            {
              done.setDisable(true);
              edit.setDisable(false);
              textArea.setEditable(false);
              textArea.setStyle("-fx-text-fill: gray");
              leftPanel.getChildren().add(algv);
              infoMsg("TextArea");
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

   public void setLoaded(boolean bool)
   {
       loaded = bool;
   }

   public void setFileData(String str)
   {
       fileData = str;
   }

   public void loaded()
   {
       textArea.setEditable(false);
       textArea.setStyle( "-fx-text-fill: gray");
      // leftPanel.getChildren().removeAll(ed);
       ed.setVisible(false);
   }

   public void newAct()
   {
       if(loaded)
          ed.setVisible(true);
       textArea.setEditable(true);
       textArea.setStyle( "-fx-text-fill: black");
       edit.setDisable(true);
       done.setDisable(false);
       leftVisiblity(true);
       info.setText("");
   }

   public void leftVisiblity(boolean bool)
   {
       leftPanelTitle.setVisible(bool);
       textArea.setVisible(bool);
       displayButton.setVisible(bool);
       edit.setVisible(bool);
       done.setVisible(bool);
   }

   public void infoMsg(String str)
   {
       if(loaded)
           ((AppData)applicationTemplate.getDataComponent()).loadData(fileData);
       else
           ((AppData)applicationTemplate.getDataComponent()).loadData(textArea.getText());

       info.setText(((AppData)applicationTemplate.getDataComponent()).forDone(str));
   }

    private void setAlgTypeAction()
    {
        /*Button config = new Button();
        Button rclass = new Button("Random Classification");
        Button rclus = new Button("Random Clustering");
        clustering.setOnAction( e -> {
          algorithms.getChildren().add(rclus);
            leftPanel.getChildren().remove(algv);
            leftPanel.getChildren().add(rclass);
        });

        classification.setOnAction( e -> {
            leftPanel.getChildren().remove(algv);
            leftPanel.getChildren().add(rclus);
        });*/
    }



}