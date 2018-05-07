package algFiles;

import algorithms.Clusterer;
import data.DataSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RandomClusterer extends Clusterer {

    private static final Random RAND = new Random();

    private DataSet dataset;
    private final int maxIterations;
    private final int updateInterval;
    private final AtomicBoolean tocontinue;

    public RandomClusterer(DataSet dataset,
                           int maxIterations,
                           int updateInterval,
                           boolean tocontinue,
                           int numberOfClusters) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
    }


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

    @Override
    public synchronized void run() {
       for (int i = 0; i <= maxIterations; i++) {
            dataset.getLocations().forEach((instanceName, location) -> dataset.getLabels().put(instanceName, Integer.toString(RAND.nextInt(numberOfClusters))));

           if(!(i == maxIterations)  && ((i % updateInterval) == 0)  ) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
