package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rarchives.ripme.ripper.rippers.MeituriRipper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class MeituriRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testMeituriRip() throws IOException, URISyntaxException {
        MeituriRipper ripper = new MeituriRipper(new URI("https://www.tujigu.com/a/14449/").toURL());
        testRipper(ripper);
    }

    @Test
    public void testGetGID() throws IOException, URISyntaxException {
        URL url = new URI("https://www.tujigu.com/a/14449/").toURL();
        MeituriRipper ripper = new MeituriRipper(url);
        Assertions.assertEquals("14449", ripper.getGID(url));
    }
}
