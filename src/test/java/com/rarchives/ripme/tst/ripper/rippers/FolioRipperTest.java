package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;
import com.rarchives.ripme.ripper.rippers.FolioRipper;

import org.junit.jupiter.api.Test;

public class FolioRipperTest extends RippersTest {
    /**
     * Test for folio.ink ripper
     * @throws IOException
     */
    @Test
    public void testFolioRip() throws IOException {
        FolioRipper ripper = new FolioRipper(new URL("https://folio.ink/DmBe6i"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://folio.ink/DmBe6i");
        FolioRipper ripper = new FolioRipper(url);
        assertEquals("DmBe6i", ripper.getGID(url));
    }
}
