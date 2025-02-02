package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.NewgroundsRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class NewgroundsRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testNewgroundsRip() throws IOException, URISyntaxException {
        NewgroundsRipper ripper = new NewgroundsRipper(new URI("https://zone-sama.newgrounds.com/art").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://zone-sama.newgrounds.com/art").toURL();
        NewgroundsRipper ripper = new NewgroundsRipper(url);
        Assertions.assertEquals("zone-sama", ripper.getGID(url));
    }


}
