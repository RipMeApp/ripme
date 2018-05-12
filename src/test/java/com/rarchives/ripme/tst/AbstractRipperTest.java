package com.rarchives.ripme.tst;

import com.rarchives.ripme.ripper.AbstractRipper;
import junit.framework.TestCase;

import java.io.IOException;
import java.net.URL;



public class AbstractRipperTest extends TestCase {

    public void testGetFileName() throws IOException {
       String fileName = AbstractRipper.getFileName(new URL("http://www.tsumino.com/Image/Object?name=U1EieteEGwm6N1dGszqCpA%3D%3D"), "test", "test");
       assertEquals("test.test", fileName);

       fileName = AbstractRipper.getFileName(new URL("http://www.tsumino.com/Image/Object?name=U1EieteEGwm6N1dGszqCpA%3D%3D"), "test", null);
       assertEquals("test", fileName);

       fileName = AbstractRipper.getFileName(new URL("http://www.tsumino.com/Image/Object?name=U1EieteEGwm6N1dGszqCpA%3D%3D"), null, null);
       assertEquals("Object", fileName);

       fileName = AbstractRipper.getFileName(new URL("http://www.test.com/file.png"), null, null);
       assertEquals("file.png", fileName);

       fileName = AbstractRipper.getFileName(new URL("http://www.test.com/file."), null, null);
       assertEquals("file.", fileName);
    }

}
