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

    private Map<String, String> dataLabels;
    private Map<String, Point2D> dataPoints;
    private int numOfLabels;
    private double xMax;
    private double xMin;
    LinkedList<Double> xList = new LinkedList();

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
        numOfLabels = 0;
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        AtomicBoolean hadAnError = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        xList.clear();
        Stream.of(tsdString.split("\n"))
                .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    try {
                        String name = checkedName(list.get(0));
                        String label = list.get(1);
                        String[] pair = list.get(2).split(",");
                        Point2D point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                        dataLabels.put(name, label);
                        dataPoints.put(name, point);
                        xList.add(point.getX());
                    } catch (Exception e) {
                        errorMessage.setLength(0);
                        errorMessage.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
                        hadAnError.set(true);
                    }
                });

        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());

        xMin = Collections.min(xList);
        xMax = Collections.max(xList);
    }

    /**
     * Exports the data to the specified 2-D chart
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        Map<XYChart.Data<Number, Number>, String> toolTipMap = new HashMap<>();
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                XYChart.Data<Number, Number> pt = new XYChart.Data<>(point.getX(), point.getY());
                series.getData().add(pt);
                toolTipMap.put(pt, entry.getKey());
            });
            chart.getData().add(series);
            series.getNode().setVisible(false);
            for (XYChart.Data<Number, Number> temp : series.getData()) {
                Tooltip.install(temp.getNode(), new Tooltip(toolTipMap.get(temp).substring(1)));
                temp.getNode().setOnMouseEntered((MouseEvent event) -> {
                    ((Node) (event.getSource())).setCursor(Cursor.HAND);
                });
                temp.getNode().setOnMouseExited((MouseEvent event) -> {
                    ((Node) (event.getSource())).setCursor(Cursor.DEFAULT);
                });
            }
        }
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedName(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }

    public String instances(String path) {
        String str = String.valueOf(dataLabels.entrySet().size());
        String labels = "";
        Map<String, String> map = new HashMap();
        for (Map.Entry<String, String> temp : dataLabels.entrySet())
            if (!temp.getValue().equals("null"))
                map.put(temp.getValue(), temp.getKey());
        for (Map.Entry<String, String> ent : map.entrySet())
            labels += "-" + ent.getKey() + "\n";
        str += " instances with " + map.entrySet().size() + " labels loaded from " + path + " .The labels are:\n" + labels;
        numOfLabels = map.size();
        return str;
    }

    public int getNumOfLabels() {
        return numOfLabels;
    }

    public double getxMax() {
        return xMax;
    }

    public double getxMin() {
        return xMin;
    }

    public Map<String, String> getDataLabels() {
        return dataLabels;
    }

    public Map<String, Point2D> getDataPoints() {
        return dataPoints;
    }



}
