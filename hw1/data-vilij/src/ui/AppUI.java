package ui;

import actions.AppActions;
import classification.RandomClassifier;
import data.DataSet;
import dataprocessors.AppData;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.chart.NumberAxis;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.geometry.Insets;
import settings.AppPropertyTypes;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
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
    private boolean cluster;
    private int classiIterations = 100;
    private int classiInterval = 5;
    private boolean classiRun = false;
    private int clusterIterations = 100;
    private int clusterInterval = 5;
    private boolean clusterRun = false;
    private int labels = 0;
    private boolean play = true;
    private Stage configStage;
    private DataSet set;
    private RandomClassifier classifier;
    private boolean firstTime = true;
    private boolean algRunning = false;
    private Label iteration;
    public AtomicInteger iterations;

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
        scrnpath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(SCREENSHOT_ICON.name()));
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
                iteration.setText("");
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
                if(checkText(textArea.getText()) && checkDuplicates(textArea.getText())) {
                    applicationTemplate.getActionComponent().handleSaveRequest();
                    if (!((AppActions) applicationTemplate.getActionComponent()).getIsUnsavedProperty())
                        saveButton.setDisable(true);
                }
            });
            loadButton.setOnAction(e ->
            {
                applicationTemplate.getActionComponent().handleLoadRequest();
                chart.getData().clear();
                applicationTemplate.getDataComponent().clear();
                displayButton.setVisible(false);
                if(((AppData)applicationTemplate.getDataComponent()).numLabels() != 2)
                    classification.setDisable(true);
                else
                    classification.setDisable(false);
                iteration.setText("");
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
        chart.setAnimated(false);

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
        String iconsPath = SEPARATOR + String.join("/", manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String playPath = String.join("/", iconsPath, manager.getPropertyValue(PLAY_ICON.name()));
        displayButton = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(playPath))));

        info = new Label();
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

        //Buttons for algorithms
        rclassBox = new HBox();
        rclusBox = new HBox();
        String configPath = String.join(SEPARATOR, iconsPath, manager.getPropertyValue(CONFIG_ICON.name()));
        classConfig = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(configPath))));
        clusConfig = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(configPath))));

        rclass = new RadioButton(applicationTemplate.manager.getPropertyValue(RANDOM_CLASSIFICATION.name()));
        rclus = new RadioButton(applicationTemplate.manager.getPropertyValue(RANDOM_CLUSTERING.name()));

        Tooltip.install(clusConfig,new Tooltip(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONFIG_TOOLTIP.name())));
        Tooltip.install(classConfig,new Tooltip(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONFIG_TOOLTIP.name())));
        classConfig.setDisable(true);
        clusConfig.setDisable(true);

        rclusBox.getChildren().addAll(rclus,clusConfig);
        rclassBox.getChildren().addAll(rclass,classConfig);

        rclusBox.setSpacing(10);
        rclassBox.setSpacing(10);

        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.setSpacing(20);

        leftPanel.getChildren().addAll(leftPanelTitle, textArea,info, processButtonsBox,ed,algv,displayButton);
        algv.getChildren().clear();

        iteration = new Label();
        iteration.setTranslateY(-220);
        iteration.setTranslateX(270);

        StackPane rightPanel = new StackPane(chart,iteration);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.75);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.75);
        StackPane.setAlignment(rightPanel, Pos.CENTER);


        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);
        appPane.getStylesheets().add("cse219.css");

        leftVisiblity(false);
        newButton.setDisable(false);

        displayButton.setVisible(false);
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setDisplayButtonActions();
        setAlgTypeAction();
        toggleActions();
        algActions();
        configAction();
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
        PropertyManager manager = applicationTemplate.manager;
        displayButton.setOnAction(event -> {
            chart.getData().clear();
            AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
            dataComponent.clear();
            if(loaded){
                dataComponent.loadData(fileData);
                dataComponent.displayData();
            }
            else
            {
                dataComponent.loadData(textArea.getText());
                dataComponent.displayData();
            }

            if(chart.getData().size() == 0)
                scrnshotButton.setDisable(true);
            else
                scrnshotButton.setDisable(false);

            if(classiRun) {
                algorithmRun();
                firstTime = true;
            } else {
                if(firstTime) {
                    algorithmRun();
                    firstTime = false;
                }
                else {
                    synchronized (classifier) {
                        classifier.notify();
                    }
                }
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

   public boolean checkDuplicates(String str)
   {
       ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
       PropertyManager manager  = applicationTemplate.manager;
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
            displayButton.setVisible(false);
        });

        done.setOnAction(e -> {
            if (checkText(textArea.getText()) && checkDuplicates(textArea.getText()))
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
                try {
                    final File file = File.createTempFile("temp",".tsd");
                    PrintWriter out = new PrintWriter("filename.txt");
                    out.println(file);
                    setSet(file.toPath());
                    file.deleteOnExit();
                } catch (IOException e1) {
                    System.err.println(e1.getMessage());
                }
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
       radioSelection(false);
    }

    public void newAct()
    {
       if(loaded)
          ed.setVisible(true);
       textArea.setEditable(true);
       displayButton.setVisible(false);
       textArea.setStyle( "-fx-text-fill: black");
       edit.setDisable(true);
       done.setDisable(false);
       leftVisiblity(true);
       info.setText("");
       algv.getChildren().clear();
       radioSelection(false);
    }

    private void radioSelection(boolean bool){
        rclass.setSelected(bool);
        rclus.setSelected(bool);
    }

    public void leftVisiblity(boolean bool)
    {
       leftPanelTitle.setVisible(bool);
       textArea.setVisible(bool);
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
            cluster = true;
        });

        classification.setOnAction( e -> {
            algv.getChildren().clear();
            algv.getChildren().addAll(new Label(applicationTemplate.manager.getPropertyValue(CLASSIFICATION.name())),rclassBox);
            cluster = false;
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

    private void configAction()
    {
        classConfig.setOnAction( e -> {
            configDialog();
        });

        clusConfig.setOnAction( e -> {
            configDialog();
        });
    }

    private void configError(String title,String msg)
    {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        dialog.setAlwaysOnTop(true);
        dialog.show(title,msg);
    }

    private void configDialog()
    {
        PropertyManager manager = applicationTemplate.manager;
        GridPane pane = new GridPane();
        configStage = new Stage();
        Scene scene = new Scene(pane,400,400);
        VBox vbox = new VBox();

        HBox iterationBox = new HBox();
        Label iterationLabel = new Label(applicationTemplate.manager.getPropertyValue(MAX_ITERATIONS.name()));
        TextField area1 = new TextField();

        iterationBox.getChildren().addAll(iterationLabel,area1);
        iterationBox.setSpacing(15);

        HBox intervalBox = new HBox();
        Label intervalLabel = new Label(applicationTemplate.manager.getPropertyValue(UPDATE_INTERVAL.name()));
        TextField area2 = new TextField();

        intervalBox.getChildren().addAll(intervalLabel,area2);
        intervalBox.setSpacing(15);

        HBox clusterBox = new HBox();
        TextField area3 = new TextField();
        area3.setText(Integer.toString(labels));

        HBox crunBox = new HBox();
        CheckBox check = new CheckBox();

        if(cluster) {
            area1.setText(Integer.toString(clusterIterations));
            area2.setText(Integer.toString(clusterInterval));
            Label clusterLabel = new Label(applicationTemplate.manager.getPropertyValue(LABELS.name()));
            clusterBox.getChildren().addAll(clusterLabel,area3);
            clusterBox.setSpacing(25);
            if(clusterRun)
                check.setSelected(true);
            else
                check.setSelected(false);
        }
        else {
            area1.setText(Integer.toString(classiIterations));
            area2.setText(Integer.toString(classiInterval));
            if(classiRun)
                check.setSelected(true);
            else
                check.setSelected(false);
        }

        Label crunLabel = new Label(applicationTemplate.manager.getPropertyValue(CONTINUE.name()));
        crunBox.getChildren().addAll(crunLabel,check);
        crunBox.setSpacing(15);

        Button donne = new Button(applicationTemplate.manager.getPropertyValue(DONE.name()));
        donne.setOnAction( e -> {
            if(cluster) {
                if (area1.getText().matches("-?\\d+") && area2.getText().matches("-?\\d+") && area3.getText().matches("-?\\d+")) {

                    if(Integer.parseInt(area1.getText()) < 0 || Integer.parseInt(area2.getText()) < 0 || Integer.parseInt(area3.getText()) < 0 )
                        configError(manager.getPropertyValue(AppPropertyTypes.NEGATIVE_TITLE.name()),manager.getPropertyValue(AppPropertyTypes.NEGATIVE_MSG.name()));

                    if(Integer.parseInt(area1.getText()) < 0){
                        area1.setText("1");
                        clusterIterations = 1;
                    }
                    else
                        clusterIterations = Integer.parseInt(area1.getText());

                    if(Integer.parseInt(area2.getText()) <= 0){
                        area2.setText("1");
                        clusterInterval = 1;
                    }
                    else
                        clusterInterval = Integer.parseInt(area2.getText());

                    if(Integer.parseInt(area3.getText()) < 0){
                        area3.setText("1");
                        labels = 1;
                    }
                    else
                        labels = Integer.parseInt(area2.getText());

                    if (check.isSelected())
                        clusterRun = true;
                    else
                        clusterRun = false;
                    displayButton.setVisible(true);
                    configStage.close();
                }
                else{
                    configError(manager.getPropertyValue(AppPropertyTypes.CONFIG_ERROR_TITLE.name()),manager.getPropertyValue(AppPropertyTypes.CONFIG_ERROR.name()));
                }
            }
            else
            {
                if (area1.getText().matches("-?\\d+") && area2.getText().matches("-?\\d+")) {
                    if(Integer.parseInt(area1.getText()) < 0 || Integer.parseInt(area2.getText()) < 0)
                        configError(manager.getPropertyValue(AppPropertyTypes.NEGATIVE_TITLE.name()),manager.getPropertyValue(AppPropertyTypes.NEGATIVE_MSG.name()));

                    if(Integer.parseInt(area1.getText()) < 0){
                        area1.setText("1");
                        classiIterations = 1;
                    }
                    else
                        classiIterations = Integer.parseInt(area1.getText());

                    if(Integer.parseInt(area2.getText()) <= 0){
                        area2.setText("1");
                        classiInterval = 1;
                    }
                    else
                        classiInterval = Integer.parseInt(area2.getText());


                    if (check.isSelected())
                        classiRun = true;
                    else {
                        classiRun = false;
                        alert();
                    }

                    displayButton.setVisible(true);
                    displayButton.setDisable(false);
                    configStage.close();
                }
                else
                    configError(manager.getPropertyValue(AppPropertyTypes.CONFIG_ERROR_TITLE.name()),manager.getPropertyValue(AppPropertyTypes.CONFIG_ERROR.name()));
            }
        });

        vbox.setSpacing(20);
        vbox.getChildren().addAll(iterationBox,intervalBox,clusterBox,crunBox,donne);
        pane.getChildren().add(vbox);
        configStage.setScene(scene);
        pane.setAlignment(Pos.CENTER);
        configStage.setAlwaysOnTop(true);
        firstTime = true;
        configStage.show();
    }

    public void setSet(Path filePath) throws IOException {
           set =  DataSet.fromTSDFile(filePath);
    }

    public void algorithmRun(){
        if(!cluster) {
            AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
            classifier = new RandomClassifier(set, classiIterations, classiInterval, classiRun, applicationTemplate);
            classifier.setXmax(dataComponent.forXmax());
            classifier.setXmin(dataComponent.forXmin());
            Thread thread = new Thread(classifier);
            thread.start();
        }
    }

    public void disableScreenshot(boolean bool)
    {
        scrnshotButton.setDisable(bool);
    }

    public void disableDisplay(boolean bool)
    {
        displayButton.setDisable(bool);
    }

    public void disableNew(boolean bool)
    {
        newButton.setDisable(bool);
    }

    public void disableLoad(boolean bool)
    {
        loadButton.setDisable(bool);
    }

    private void alert()
    {
        PropertyManager manager = applicationTemplate.manager;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(manager.getPropertyValue(AppPropertyTypes.ALERT.name()));
        alert.setHeaderText(manager.getPropertyValue(AppPropertyTypes.ALERT_HEADER.name()));
        alert.setContentText(manager.getPropertyValue(AppPropertyTypes.ALERT_CONTEXT.name()));
        alert.initOwner(configStage);
        alert.showAndWait();
    }

   public void setAlgRunning(boolean bool)
   {
       algRunning = bool;
   }

   public boolean getAlgRunning()
   {
       return algRunning;
   }

    public synchronized void updateIterationLabel(AtomicInteger iterations)
    {
        iteration.setText("Iterations = " + iterations);
    }

}