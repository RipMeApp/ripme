package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.XcartxRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

public class XcartxRipperTest extends RippersTest {
    @Test
    @Disabled("Broken ripper")
    public void testAlbum() throws IOException {
        XcartxRipper ripper = new XcartxRipper(new URL("http://xcartx.com/4937-tokimeki-nioi.html"));
        testRipper(ripper);
    }
}
