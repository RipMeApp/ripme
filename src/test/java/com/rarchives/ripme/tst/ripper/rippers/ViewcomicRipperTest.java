package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.ViewcomicRipper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ViewcomicRipperTest extends RippersTest {
    @Test @Disabled("Ripper broken")
    public void testViewcomicRipper() throws IOException {
        ViewcomicRipper ripper = new ViewcomicRipper(new URL("https://view-comic.com/batman-no-mans-land-vol-1/"));
        testRipper(ripper);
    }
}