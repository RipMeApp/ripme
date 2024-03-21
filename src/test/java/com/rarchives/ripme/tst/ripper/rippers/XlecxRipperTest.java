package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.XlecxRipper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class XlecxRipperTest extends RippersTest {
    @Test
    @Disabled("Broken ripper")
    public void testAlbum() throws IOException, URISyntaxException {
        XlecxRipper ripper = new XlecxRipper(new URI("http://xlecx.com/4274-black-canary-ravished-prey.html").toURL());
        testRipper(ripper);
    }
}
