package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ErotivRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ErotivRipperTest extends RippersTest {
    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://erotiv.io/e/1568314255").toURL();
        ErotivRipper ripper = new ErotivRipper(url);
        assert("1568314255".equals(ripper.getGID(url)));
    }

    public void testRip() throws IOException, URISyntaxException {
        URL url = new URI("https://erotiv.io/e/1568314255").toURL();
        ErotivRipper ripper = new ErotivRipper(url);
        testRipper(ripper);
    }

    @Test
    @Disabled("test or ripper broken")
    public void testGetURLsFromPage() throws IOException, URISyntaxException {
        URL url = new URI("https://erotiv.io/e/1568314255").toURL();
        ErotivRipper ripper = new ErotivRipper(url);
        assert(1 == ripper.getURLsFromPage(ripper.getFirstPage()).size());
    }
}
