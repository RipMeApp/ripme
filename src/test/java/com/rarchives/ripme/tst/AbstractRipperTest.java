package com.rarchives.ripme.tst;

import com.rarchives.ripme.ripper.AbstractRipper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class AbstractRipperTest {

   @Test
    public void testGetFileName() throws IOException, URISyntaxException {
       String fileName = AbstractRipper.getFileName(new URI("http://www.tsumino.com/Image/Object?name=U1EieteEGwm6N1dGszqCpA%3D%3D").toURL(),null,  "test", "test");
       assertEquals("test.test", fileName);

       fileName = AbstractRipper.getFileName(new URI("http://www.tsumino.com/Image/Object?name=U1EieteEGwm6N1dGszqCpA%3D%3D").toURL(), null,"test", null);
       assertEquals("test", fileName);

       fileName = AbstractRipper.getFileName(new URI("http://www.tsumino.com/Image/Object?name=U1EieteEGwm6N1dGszqCpA%3D%3D").toURL(), null,null, null);
       assertEquals("Object", fileName);

       fileName = AbstractRipper.getFileName(new URI("http://www.test.com/file.png").toURL(), null,null, null);
       assertEquals("file.png", fileName);

       fileName = AbstractRipper.getFileName(new URI("http://www.test.com/file.").toURL(), null,null, null);
       assertEquals("file.", fileName);
    }

}
