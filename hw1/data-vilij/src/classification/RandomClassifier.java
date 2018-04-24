package classification;

import algorithms.Classifier;
import data.DataSet;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private ApplicationTemplate applicationTemplate;
    private DataSet dataset;
    private List<List<Integer>> bigList= new ArrayList<>();
    private final int maxIterations;
    private final int updateInterval;
    private double xmax;
    private double xmin;
    private double ymin;
    private double ymax;
    XYChart.Series<Number, Number> ser;
    AppData appData;
    // currently, this value does not change after instantiation
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
    public void run() {

        for (int i = 1; i <= maxIterations && tocontinue(); i++) {
            int xCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int yCoefficient = new Double(RAND.nextDouble() * 100).intValue();
            int constant     = new Double(RAND.nextDouble() * 100).intValue();

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            bigList.add(output);
            ymin =  -(constant + (xCoefficient * xmin)) / yCoefficient;
            ymax =  -(constant + (xCoefficient * xmax)) / yCoefficient;
            ser = new XYChart.Series<>();
            ser.getData().add(new XYChart.Data<>(xmin, ymin));
            ser.getData().add(new XYChart.Data<>(xmax, ymax));

            try {
                Platform.runLater(()->  ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().add(ser)
                );
                Thread.sleep(400);
                Platform.runLater(()-> ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().remove(ser));
                Thread.sleep(400);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }

            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            if (i % updateInterval == 0) {
                //System.out.printf("Iteration number %d: ", i); //
                flush();
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                //System.out.printf("Iteration number %d: ", i);
                flush();
                break;
            }
        }
    }

    public List<List<Integer>> getBigList()
    {
        return bigList;
    }

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }


    /** A placeholder main method to just make sure this code runs smoothly */
    public static void main(String... args) throws IOException {
        //DataSet          dataset    = DataSet.fromTSDFile(Paths.get("/Users/jagandeep/IdeaProjects/cse219homework/out/production/data-vilij/data/sample-data.tsd"));
       // RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true,);
        //classifier.run(); // no multithreading yet

    }
}