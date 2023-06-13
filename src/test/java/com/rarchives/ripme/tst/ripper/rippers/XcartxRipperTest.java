package com.rarchives.ripme.tst.ripper.rippers;

import com.rarchives.ripme.ripper.rippers.XcartxRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class XcartxRipperTest extends RippersTest {
    @Test
    @Disabled("Broken ripper")
    public void testAlbum() throws IOException, URISyntaxException {
        XcartxRipper ripper = new XcartxRipper(new URI("http://xcartx.com/4937-tokimeki-nioi.html").toURL());
        testRipper(ripper);
    }
}
