package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.SmuttyRipper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class SmuttyRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testRip() throws IOException, URISyntaxException {
        SmuttyRipper ripper = new SmuttyRipper(new URI("https://smutty.com/user/QUIGON/").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://smutty.com/user/QUIGON/").toURL();
        SmuttyRipper ripper = new SmuttyRipper(url);
        Assertions.assertEquals("QUIGON", ripper.getGID(url));
    }
}
