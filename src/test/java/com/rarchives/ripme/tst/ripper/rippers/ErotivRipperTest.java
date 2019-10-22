package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ErotivRipper;
import org.junit.jupiter.api.Test;

public class ErotivRipperTest extends RippersTest {
    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://erotiv.io/e/1568314255");
        ErotivRipper ripper = new ErotivRipper(url);
        assert("1568314255".equals(ripper.getGID(url)));
    }

    public void testRip() throws IOException {
        URL url = new URL("https://erotiv.io/e/1568314255");
        ErotivRipper ripper = new ErotivRipper(url);
        testRipper(ripper);
    }

    @Test
    public void testGetURLsFromPage() throws IOException {
        URL url = new URL("https://erotiv.io/e/1568314255");
        ErotivRipper ripper = new ErotivRipper(url);
        assert(1 == ripper.getURLsFromPage(ripper.getFirstPage()).size());
    }
}
