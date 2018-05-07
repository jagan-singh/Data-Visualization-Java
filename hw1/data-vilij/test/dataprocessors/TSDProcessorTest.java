package dataprocessors;

import data.DataSet;
import javafx.geometry.Point2D;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class TSDProcessorTest {

    //checking with valid tsd line with doubles in point
    @Test
    public void test1() throws Exception {
        TSDProcessor processor = new TSDProcessor();
        HashMap<String,String> expectedLabels = new HashMap<>();
        expectedLabels.put("@instance","label");
        HashMap<String,Point2D> expectedPoints = new HashMap<>();
        expectedPoints.put("@instance",new Point2D(2.3,3.5));

        processor.processString("@instance\tlabel\t2.3,3.5");
        HashMap labels = (HashMap) processor.getDataLabels();
        HashMap points = (HashMap) processor.getDataPoints();

        Assert.assertEquals(expectedLabels,labels);
        Assert.assertEquals(expectedPoints,points);
    }

    //checking with two tabs between label and point
    @Test(expected = Exception.class)
    public void test2() throws Exception {
        TSDProcessor processor = new TSDProcessor();
        HashMap<String,String> expectedLabels = new HashMap<>();
        expectedLabels.put("@instance","label");
        HashMap<String,Point2D> expectedPoints = new HashMap<>();
        expectedPoints.put("@instance",new Point2D(2.3,3.5));

        processor.processString("@instance\tlabel\t\t2.3,3.5");
        HashMap labels = (HashMap) processor.getDataLabels();
        HashMap points = (HashMap) processor.getDataPoints();

        Assert.assertEquals(expectedLabels,labels);
        Assert.assertEquals(expectedPoints,points);
    }

    //checking with two tabs between instance and label
    @Test(expected = Exception.class)
    public void test3() throws Exception {
        TSDProcessor processor = new TSDProcessor();
        HashMap<String,String> expectedLabels = new HashMap<>();
        expectedLabels.put("@instance","label");
        HashMap<String,Point2D> expectedPoints = new HashMap<>();
        expectedPoints.put("@instance",new Point2D(2.3,3.5));

        processor.processString("@instance\t\tlabel\t2.3,3.5");
        HashMap labels = (HashMap) processor.getDataLabels();
        HashMap points = (HashMap) processor.getDataPoints();

        Assert.assertEquals(expectedLabels,labels);
        Assert.assertEquals(expectedPoints,points);
    }

    //checking with no tabs in data
    @Test(expected = Exception.class)
    public void test4() throws Exception {
        TSDProcessor processor = new TSDProcessor();
        HashMap<String,String> expectedLabels = new HashMap<>();
        expectedLabels.put("@instance","label");
        HashMap<String,Point2D> expectedPoints = new HashMap<>();
        expectedPoints.put("@instance",new Point2D(2.3,3.5));

        processor.processString("@instancelabel2.3,3.5");
        HashMap labels = (HashMap) processor.getDataLabels();
        HashMap points = (HashMap) processor.getDataPoints();

        Assert.assertEquals(expectedLabels,labels);
        Assert.assertEquals(expectedPoints,points);
    }

    //Checking without any instance name
    @Test(expected = Exception.class)
    public void test5() throws Exception{
        TSDProcessor processor = new TSDProcessor();
        HashMap<String,String> expectedLabels = new HashMap<>();
        expectedLabels.put("@instance","label");
        HashMap<String,Point2D> expectedPoints = new HashMap<>();
        expectedPoints.put("@instance",new Point2D(2.3,3.5));

        processor.processString("\tlabel\t2.3,3.5");
        HashMap labels = (HashMap) processor.getDataLabels();
        HashMap points = (HashMap) processor.getDataPoints();

        Assert.assertEquals(expectedLabels,labels);
        Assert.assertEquals(expectedPoints,points);
    }
}