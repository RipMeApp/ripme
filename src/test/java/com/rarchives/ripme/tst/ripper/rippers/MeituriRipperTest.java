package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MeituriRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MeituriRipperTest extends RippersTest {
    @Test
    @Disabled("Broken ripper")
    public void testMeituriRip() throws IOException {
        MeituriRipper ripper = new MeituriRipper(new URL("https://www.meituri.com/a/14449/"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://www.meituri.com/a/14449/");
        MeituriRipper ripper = new MeituriRipper(url);
        assertEquals("14449", ripper.getGID(url));
    }
}
