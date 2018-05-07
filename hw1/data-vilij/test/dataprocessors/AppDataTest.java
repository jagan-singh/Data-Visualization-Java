package dataprocessors;

import org.junit.Assert;
import org.junit.Test;
import vilij.templates.ApplicationTemplate;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class AppDataTest {

    //checking the saving of data from textArea
    @Test
    public void saveData() throws IOException {
        Path testPath = Paths.get("@instance\tlabel\t2,3");
        File testFile = new File(String.valueOf(testPath));

        Path expectedPath = Paths.get("@instance\tlabel\t2,3");
        File expectedFile = new File(String.valueOf(expectedPath));

        FileOutputStream stream = new FileOutputStream("file.txt");
        OutputStreamWriter streamWriter  = new OutputStreamWriter(stream);

        Writer writer = new BufferedWriter(streamWriter);
        writer.write("@instance\tlabel\t2,3");
        writer.close();

        List<String> str = Files.readAllLines(new File("file.txt").toPath());
        Assert.assertEquals("@instance\tlabel\t2,3", str.get(0));
        Assert.assertEquals(expectedFile,testFile);
    }
}
