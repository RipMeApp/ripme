package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.KingcomixRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class KingcomixRipperTest extends RippersTest {

    @Test
    @Disabled("test or ripper broken")
    public void testRip() throws IOException {
        KingcomixRipper ripper = new KingcomixRipper(new URL("https://kingcomix.com/aunt-cumming-tracy-scops/"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://kingcomix.com/aunt-cumming-tracy-scops/");
        KingcomixRipper ripper = new KingcomixRipper(url);
        Assertions.assertEquals("aunt-cumming-tracy-scops", ripper.getGID(url));
    }

}
