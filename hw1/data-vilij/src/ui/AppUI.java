package ui;

import actions.AppActions;
import classification.RandomClassifier;
import data.DataSet;
import dataprocessors.AppData;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.chart.NumberAxis;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.geometry.Insets;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import settings.AppPropertyTypes;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import javafx.stage.Popup;
import javafx.stage.Window;
import static settings.AppPropertyTypes.*;
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
    private static final String SEPARATOR = "/";
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
    private Button classConfig;
    private Button clusConfig;
    private RadioButton rclass;
    private RadioButton rclus;
    private Label algoTitle;
    private HBox rclassBox;
    private HBox rclusBox;
    DataSet set;



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
        String iconsPath = SEPARATOR + String.join("/", manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
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
            loadButton.setOnAction(e ->
            {
                applicationTemplate.getActionComponent().handleLoadRequest();
                if(((AppData)applicationTemplate.getDataComponent()).numLabels() != 2)
                    classification.setDisable(true);
                else
                    classification.setDisable(false);
            });
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
        edit = new ToggleButton(applicationTemplate.manager.getPropertyValue(EDIT.name()));
        done = new ToggleButton(applicationTemplate.manager.getPropertyValue(DONE.name()));
        ToggleGroup group = new ToggleGroup();
        edit.setToggleGroup(group);
        done.setToggleGroup(group);
        edit.setDisable(true);

        //HBOX for toggle buttons
        ed = new HBox();
        ed.getChildren().addAll(edit,done);
        ed.setSpacing(10);

        //VBOX for algorithm types
        algv = new VBox();
        algv.setPadding(new Insets(10));
        algv.setSpacing(10);
        clustering = new Button(applicationTemplate.manager.getPropertyValue(CLUSTERING.name()));
        classification = new Button(applicationTemplate.manager.getPropertyValue(CLASSIFICATION.name()));
        algoTitle = new Label(applicationTemplate.manager.getPropertyValue(ALGO_VBOX_TITLE.name()));
        algv.getChildren().addAll(algoTitle,clustering,classification);


        rclassBox = new HBox();
        rclusBox = new HBox();
        String iconsPath = SEPARATOR + String.join("/", manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        scrnpath = String.join("/", iconsPath, manager.getPropertyValue(CONFIG_ICON.name()));
        classConfig = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(scrnpath))));
        clusConfig = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(scrnpath))));

        rclass = new RadioButton(applicationTemplate.manager.getPropertyValue(RANDOM_CLASSIFICATION.name()));
        rclus = new RadioButton(applicationTemplate.manager.getPropertyValue(RANDOM_CLUSTERING.name()));

        classConfig.setDisable(true);
        clusConfig.setDisable(true);

        rclusBox.getChildren().addAll(rclus,clusConfig);
        rclassBox.getChildren().addAll(rclass,classConfig);

        rclusBox.setSpacing(10);
        rclassBox.setSpacing(10);

        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.setSpacing(20);
        //checkBox.setSelected(false);
        //processButtonsBox.getChildren().addAll(checkBox);

        leftPanel.getChildren().addAll(leftPanelTitle, textArea,info, processButtonsBox,ed,algv,displayButton);
        algv.getChildren().clear();


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
        algActions();
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

    private void toggleActions()
    {
        edit.setOnAction( e -> {
            edit.setDisable(true);
            done.setDisable(false);
            textArea.setEditable(true);
            textArea.setStyle( "-fx-text-fill: black");
            algv.getChildren().clear();
        });

        done.setOnAction(e -> {
            if (checkText(textArea.getText()) && checkDuplicates())
            {
                done.setDisable(true);
                edit.setDisable(false);
                textArea.setEditable(false);
                textArea.setStyle("-fx-text-fill: gray");
                infoMsg("TextArea");
                algv.getChildren().clear();
                algv.getChildren().addAll(algoTitle,classification,clustering);
                if(((AppData)applicationTemplate.getDataComponent()).numLabels() != 2)
                    classification.setDisable(true);
                else
                    classification.setDisable(false);
            }
        });
    }


    public void loaded()
   {
       textArea.setEditable(false);
       textArea.setStyle( "-fx-text-fill: gray");
       ed.setVisible(false);
       saveButton.setDisable(true);
       algv.getChildren().clear();
       algv.getChildren().addAll(algoTitle,classification,clustering);
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
       algv.getChildren().clear();
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

       if(loaded) {
           applicationTemplate.getDataComponent().clear();
           ((AppData) applicationTemplate.getDataComponent()).loadData(fileData);
       }
       else{
           applicationTemplate.getDataComponent().clear();
           ((AppData)applicationTemplate.getDataComponent()).loadData(textArea.getText());
       }

       info.setText(((AppData)applicationTemplate.getDataComponent()).forDone(str));
   }

    private void setAlgTypeAction()
    {
        clustering.setOnAction( e -> {
            algv.getChildren().clear();
            algv.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(CLUSTERING.name())),rclusBox);
        });

        classification.setOnAction( e -> {

            algv.getChildren().clear();
            algv.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(CLASSIFICATION.name())),rclassBox);
        });
    }

    private void algActions()
    {

        rclass.setOnAction( e -> {
            rclass.setSelected(true);
            classConfig.setDisable(false);

        });

        rclus.setOnAction( e -> {
            rclus.setSelected(true);
            clusConfig.setDisable(false);
        });
    }


    private DataSet something(String str)
    {
        set = new DataSet();
        if(loaded)
          str = fileData;
        else
          str = textArea.getText();

        String lines[] = str.split("\\r?\\n");
        for(int i=0;i<lines.length;i++)
        {
                //set.addInstance(lines[i]);

        }
        return set;
    }

    private void configAction()
    {
        classConfig.setOnAction( e -> {
            configDialog();
        });

    }

    private void configDialog()
    {
        Stage stage = new Stage();
        final Popup popup = new Popup();
        popup.setX(300);
        popup.setY(200);
        popup.getContent().addAll(new TextArea("hello"));
        popup.show(stage);
        stage.show();
    }

}