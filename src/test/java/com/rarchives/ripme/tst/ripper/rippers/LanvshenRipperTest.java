package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.LanvshenRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class LanvshenRipperTest extends RippersTest {
    @Test
    @Disabled("Broken ripper")
    public void testMeituriRip() throws IOException {
        LanvshenRipper ripper = new LanvshenRipper(new URL("https://www.lanvshen.com/a/14449/"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://www.lanvshen.com/a/14449/");
        LanvshenRipper ripper = new LanvshenRipper(url);
        assertEquals("14449", ripper.getGID(url));
    }
}
