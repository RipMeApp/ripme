package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ShesFreakyRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ShesFreakyRipperTest extends RippersTest {
    @Test
    @Disabled("https://github.com/RipMeApp/ripme/issues/254")
    public void testShesFreakyRip() throws IOException, URISyntaxException {
        ShesFreakyRipper ripper = new ShesFreakyRipper(
                new URI("http://www.shesfreaky.com/gallery/nicee-snow-bunny-579NbPjUcYa.html").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("http://www.shesfreaky.com/gallery/nicee-snow-bunny-579NbPjUcYa.html").toURL();
        ShesFreakyRipper ripper = new ShesFreakyRipper(url);
        Assertions.assertEquals("nicee-snow-bunny-579NbPjUcYa", ripper.getGID(url));
    }
}
