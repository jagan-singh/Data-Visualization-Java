package ui;

import com.sun.media.sound.InvalidDataException;
import org.junit.Test;
import static org.junit.Assert.*;

public class AppUITest {

    //checking by assigning some positive integers to the variables
    @Test
    public void test1() throws InvalidDataException {
        int maxIterations = 0;
        int updateInterval = 0;
        int labels = 0;

        int testVal1 = 3;
        int testVal2  = 5;
        int testVal3 = 3;

        if (testVal1 > 0)
            maxIterations = testVal2;
        else
            throw new InvalidDataException();

        if (testVal2 > 0)
            updateInterval = testVal2;
        else
            throw new InvalidDataException();

        if (testVal3 > 0)
            labels = testVal3;
        else
            throw new InvalidDataException();
    }


    //checking by assigning negative integers to the configuration variables
    @Test(expected = InvalidDataException.class)
    public void test2() throws InvalidDataException {
        int maxIterations = 0;
        int updateInterval = 0;
        int labels = 0;

        int testVal1 = -3;
        int testVal2  = -5;
        int testVal3 = -3;

        if (testVal1 > 0)
            maxIterations = testVal2;
        else
            throw new InvalidDataException();

        if (testVal2 > 0)
            updateInterval = testVal2;
        else
            throw new InvalidDataException();

        if (testVal3 > 0)
            labels = testVal3;
        else
            throw new InvalidDataException();
    }

    //checking for 0 values for all variables
    @Test(expected = InvalidDataException.class)
    public void test3() throws InvalidDataException {
        int maxIterations = 0;
        int updateInterval = 0;
        int labels = 0;

        int testVal1 = 0;
        int testVal2  = 0;
        int testVal3 = 0;

        if (testVal1 > 0)
            maxIterations = testVal2;
        else
            throw new InvalidDataException();

        if (testVal2 > 0)
            updateInterval = testVal2;
        else
            throw new InvalidDataException();

        if (testVal3 > 0)
            labels = testVal3;
        else
            throw new InvalidDataException();
    }

    //checking with assigning non integers to the variables
    //if user enters strings in the textArea exceptions will be thrown
    @Test(expected = NumberFormatException.class )
    public void test4()  {
        int maxIterations = 0;
        int updateInterval = 0;
        int labels = 0;

        String test1 = "Hello world";
        String test2 = "Hello world!";
        String test3 = "Hello world!!";

        maxIterations = Integer.parseInt(test1);
        updateInterval = Integer.parseInt(test2);
        labels = Integer.parseInt(test3);
    }


}