package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import com.rarchives.ripme.ripper.rippers.FolioRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FolioRipperTest extends RippersTest {
    /**
     * Test for folio.ink ripper
     */
    @Test
    @Disabled("test or ripper broken")
    public void testFolioRip() throws IOException, URISyntaxException {
        FolioRipper ripper = new FolioRipper(new URI("https://folio.ink/DmBe6i").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://folio.ink/DmBe6i").toURL();
        FolioRipper ripper = new FolioRipper(url);
        Assertions.assertEquals("DmBe6i", ripper.getGID(url));
    }
}
