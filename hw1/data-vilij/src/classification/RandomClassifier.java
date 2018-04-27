package classification;

import algorithms.Classifier;
import data.DataSet;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private ApplicationTemplate applicationTemplate;
    private DataSet dataset;
    private final int maxIterations;
    private final int updateInterval;
    private double xmax;
    private double xmin;
    private double ymin;
    private double ymax;
    private XYChart.Series<Number, Number> ser;
    private final AtomicBoolean tocontinue;

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue, ApplicationTemplate app) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.applicationTemplate = app;
    }

    public void setXmin(double xmin) {
        this.xmin = xmin;
    }

    public void setXmax(double xmax) {
        this.xmax = xmax;
    }

    @Override
    public synchronized void run() {
        ((AppUI) applicationTemplate.getUIComponent()).disableScreenshot(true);
        ((AppUI) applicationTemplate.getUIComponent()).disableDisplay(true);
        ((AppUI) applicationTemplate.getUIComponent()).setAlgRunning(true);
        ((AppUI) applicationTemplate.getUIComponent()).disableLoad(true);
        ((AppUI) applicationTemplate.getUIComponent()).disableNew(true);
        for (int i = 1; i <= maxIterations; i++) {
            int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant = RAND.nextInt(11);

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            ymin = -(constant + (xCoefficient * xmin)) / yCoefficient;
            ymax = -(constant + (xCoefficient * xmax)) / yCoefficient;

            final AtomicInteger n = new AtomicInteger(i);
            Platform.runLater(() ->  ((AppUI) applicationTemplate.getUIComponent()).updateIterationLabel(n));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if ( (i == maxIterations) || ((i % updateInterval) == 0)) {
                try {
                    Platform.runLater(() -> ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().remove(ser));
                    Platform.runLater(() -> {
                        ser = new XYChart.Series<>();
                        ser.setName("Classification");
                        ser.getData().add(new XYChart.Data<>(xmin, ymin));
                        ser.getData().add(new XYChart.Data<>(xmax, ymax));
                        ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().add(ser);
                        for (int j = 0; j < ser.getData().size(); j++)
                            ser.getData().get(j).getNode().setStyle(" visibility: hidden");
                    });
                    Thread.sleep(700);

                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }

                if (!tocontinue()) {
                    try {
                        ((AppUI) applicationTemplate.getUIComponent()).disableScreenshot(false);
                        ((AppUI) applicationTemplate.getUIComponent()).disableDisplay(false);
                        ((AppUI) applicationTemplate.getUIComponent()).setAlgRunning(false);
                        ((AppUI) applicationTemplate.getUIComponent()).disableLoad(false);
                        ((AppUI) applicationTemplate.getUIComponent()).disableNew(false);
                        if(i != maxIterations)
                           wait();
                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                    }
                    ((AppUI) applicationTemplate.getUIComponent()).disableScreenshot(true);
                    ((AppUI) applicationTemplate.getUIComponent()).disableDisplay(true);
                    ((AppUI) applicationTemplate.getUIComponent()).setAlgRunning(true);
                    ((AppUI) applicationTemplate.getUIComponent()).disableLoad(true);
                    ((AppUI) applicationTemplate.getUIComponent()).disableNew(true);
                }
            }
        }
                ((AppUI) applicationTemplate.getUIComponent()).disableScreenshot(false);
                ((AppUI) applicationTemplate.getUIComponent()).setAlgRunning(false);
                ((AppUI) applicationTemplate.getUIComponent()).disableLoad(false);
                ((AppUI) applicationTemplate.getUIComponent()).disableNew(false);

                if (tocontinue())
                   ((AppUI) applicationTemplate.getUIComponent()).disableDisplay(false);
    }

    /** A placeholder main method to just make sure this code runs smoothly */
    public static void main(String... args) throws IOException {
        //DataSet          dataset    = DataSet.fromTSDFile(Paths.get("/Users/jagandeep/IdeaProjects/cse219homework/out/production/data-vilij/data/sample-data.tsd"));
       // RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true,);
        //classifier.run(); // no multithreading yet

    }
}