package jibble;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebServerConfigTest {
    private final ByteArrayOutputStream outStream = new
            ByteArrayOutputStream();
    private File testFile;
    @Before
    public void setUp() {
        testFile = mock(File.class);
        System.setOut(new PrintStream(outStream));
    }
    @Test
    public void test() {
        when(testFile.getName()).thenReturn("index.html");
        Assert.assertEquals(".html", WebServerConfig.getExtension(testFile));

        when(testFile.getName()).thenReturn("index.min.js");
        Assert.assertEquals(".js", WebServerConfig.getExtension(testFile));

    }
    @After
    public void cleanUp() {
        System.setOut(null);
    }
}