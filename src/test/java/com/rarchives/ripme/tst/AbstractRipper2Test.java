package com.rarchives.ripme.tst;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;


import com.rarchives.ripme.ripper.AbstractRipper2;

public class AbstractRipper2Test {

    @Test
   public void testGetFileName() throws IOException, URISyntaxException {
      String fileName = AbstractRipper2.getFileName(
            new URI("http://www.tsumino.com/Image/Object?name=U1EieteEGwm6N1dGszqCpA%3D%3D").toURL(),
            null, "test", "test");
      assertEquals("test.test", fileName);

      fileName = AbstractRipper2.getFileName(
            new URI("http://www.tsumino.com/Image/Object?name=U1EieteEGwm6N1dGszqCpA%3D%3D").toURL(),
            null, "test", null);
      assertEquals("test", fileName);

      fileName = AbstractRipper2.getFileName(
            new URI("http://www.tsumino.com/Image/Object?name=U1EieteEGwm6N1dGszqCpA%3D%3D").toURL(),
            null, null, null);
      assertEquals("Object", fileName);

      fileName = AbstractRipper2.getFileName(new URI("http://www.test.com/file.png").toURL(),
            null, null, null);
      assertEquals("file.png", fileName);

      fileName = AbstractRipper2.getFileName(new URI("http://www.test.com/file.").toURL(),
            null, null, null);
      assertEquals("file.", fileName);
   }
    
}
