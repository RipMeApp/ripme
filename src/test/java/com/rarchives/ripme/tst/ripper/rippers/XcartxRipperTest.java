package com.rarchives.ripme.tst.ripper.rippers;


import com.rarchives.ripme.ripper.rippers.XcartxRipper;

import java.io.IOException;
import java.net.URL;

public class XcartxRipperTest extends RippersTest {
    public void testAlbum() throws IOException {
        XcartxRipper ripper = new XcartxRipper(new URL("http://xcartx.com/4937-tokimeki-nioi.html"));
        testRipper(ripper);
    }
}
