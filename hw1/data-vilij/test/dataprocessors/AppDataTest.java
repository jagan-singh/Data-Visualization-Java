package dataprocessors;

import org.junit.Assert;
import org.junit.Test;
import vilij.templates.ApplicationTemplate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppDataTest {

    //checking the saving of data from textArea
    @Test(expected = ExceptionInInitializerError.class)
    public void saveData() throws IOException {
        Path expectedPath = Paths.get("@instance\tlabel\t2,3");
        File expectedFile = new File(String.valueOf(expectedPath));

        ApplicationTemplate applicationTemplate = new ApplicationTemplate();
        AppData appData = new AppData(applicationTemplate);

        Path testPath = Paths.get("@instance\tlabel\t2,3");
        File testFile = new File(String.valueOf(testPath));

        appData.saveData(testPath);

        Assert.assertEquals(expectedFile,testFile);
    }
}