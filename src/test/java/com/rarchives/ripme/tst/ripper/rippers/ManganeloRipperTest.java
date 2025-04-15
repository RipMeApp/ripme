package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ManganeloRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ManganeloRipperTest extends RippersTest {
    @Test
    @Disabled("no images found, test or ripper broken")
    public void testRip() throws IOException, URISyntaxException {
        ManganeloRipper ripper = new ManganeloRipper(new URI("https://manganelo.com/manga/demonic_housekeeper").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://manganelo.com/manga/demonic_housekeeper").toURL();
        ManganeloRipper ripper = new ManganeloRipper(url);
        Assertions.assertEquals("demonic_housekeeper", ripper.getGID(url));
    }
}
