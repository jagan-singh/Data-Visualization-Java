package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;
import javafx.scene.Node;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private Map<String, String>  dataLabels;
    private Map<String, Point2D> dataPoints;
    private String ptname;

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
              .forEach(list -> {
                  try {
                      String   name  = checkedname(list.get(0));
                      String   label = list.get(1);
                      String[] pair  = list.get(2).split(",");
                      Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                      dataLabels.put(name, label);
                      dataPoints.put(name, point);
                  } catch (Exception e) {
                      errorMessage.setLength(0);
                      errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                      hadAnError.set(true);
                  }
              });
        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());
    }

    /**
     * Exports the data to the specified 2-D chart
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        Double yval = 0.0;
        int counter = 0;
        Set<String> labels = new HashSet<>(dataLabels.values());
        Set<String> keys = new HashSet<>(dataLabels.keySet());
        String[] keyarray = keys.toArray(new String[dataLabels.size()]);
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
            });
            chart.getData().add(series);
            for (int i = 0; i < series.getData().size(); i++) {
                Tooltip.install(series.getData().get(i).getNode(), new Tooltip(keyarray[i].substring(1)));
                series.getData().get(i).getNode().setOnMouseEntered((MouseEvent event) -> {
                    ((Node) (event.getSource())).setCursor(Cursor.HAND);
                });
                series.getData().get(i).getNode().setOnMouseExited((MouseEvent event) -> {
                    ((Node) (event.getSource())).setCursor(Cursor.DEFAULT);
                });
                yval += (Double) series.getData().get(i).getYValue();
                counter++;
            }
        }
        final Double yavg = yval / counter;
        XYChart.Series<Number, Number> avgser = new XYChart.Series<>();
        avgser.setName("Average Y");
        for (String label : labels) {
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                avgser.getData().add(new XYChart.Data<>(point.getX(), yavg));
            });
        }
        chart.getData().add(avgser);

        for(int i=0;i<avgser.getData().size();i++)
           avgser.getData().get(i).getNode().setStyle(" visibility: hidden");
        avgser.getNode().setStyle("-fx-stroke: blue");
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }
}

