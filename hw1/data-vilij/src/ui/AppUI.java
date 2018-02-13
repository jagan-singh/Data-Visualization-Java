package ui;

import actions.AppActions;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import vilij.components.DataComponent;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate  {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private ScatterChart<Number, Number> chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display

    private HBox hbox ;
    private  VBox vbox;
    private VBox vbox2;

    public ScatterChart<Number, Number> getChart() { return chart; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        chart = new ScatterChart<Number, Number>(xAxis,yAxis);
        chart.setTitle("Data Visualization");
        textArea = new TextArea();
        displayButton = new Button("Display");
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
       super.setToolBar(applicationTemplate);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
       textArea.clear();
    }

    private void layout() {
         hbox  = new HBox(10);
         vbox = new VBox(10);
         Text text = new Text("Data File");
         vbox.getChildren().addAll(text,textArea,displayButton);
         vbox.setAlignment(Pos.TOP_CENTER);
         hbox.getChildren().addAll(vbox, chart);
         appPane.getChildren().add(hbox);
         textArea.textProperty().addListener(e -> {
                     newButton.setDisable(false);
                     saveButton.setDisable(false);
                 }
             );
    }

    private void setWorkspaceActions() {
       displayButton.setOnAction(e -> {
           //XYChart.Series series1 = new XYChart.Series();

           //series1.getData().add(new XYChart.Data(4.2, 5.6));
           //series1.setName("hi");

           //chart.getData().add(series1);
          String str = textArea.getText();
           String lines[] = str.split("\\r?\\n");
           for(int i=0;i<lines.length;i++)
           {
              // if(lines[i].matches() )
               String[] strr = lines[i].split("\\s+");
               XYChart.Series series = new XYChart.Series();
               //if()
               series.setName(strr[1]);
               Double x = Double.parseDouble(strr[2].substring(0,strr[2].indexOf(',')));
               Double y = Double.parseDouble(strr[2].substring(strr[2].indexOf(',') +1));
               series.getData().add(new XYChart.Data(x,y));
               chart.getData().add(series);

               }



       });
    }





}
