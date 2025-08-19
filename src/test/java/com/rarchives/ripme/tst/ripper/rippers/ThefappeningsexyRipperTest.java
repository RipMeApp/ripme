package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.ThefappeningsexyRipper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class ThefappeningsexyRipperTest extends RippersTest {

    @Test
    public void testThefappeningsexyRip() throws IOException {
        ThefappeningsexyRipper ripper = new ThefappeningsexyRipper(new URL("https://thefappening.sexy/albums/index.php?/category/1"));
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException {
        URL url = new URL("https://thefappening.sexy/albums/index.php?/category/1");
        ThefappeningsexyRipper ripper = new ThefappeningsexyRipper(url);
        assertEquals("Ali Michael", ripper.getGID(url));
    }

}
