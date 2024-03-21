package com.rarchives.ripme.tst.ripper.rippers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.rarchives.ripme.ripper.rippers.Hentai2readRipper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class Hentai2readRipperTest extends RippersTest {
    @Test
    @Tag("flaky")
    public void testHentai2readAlbum() throws IOException, URISyntaxException {
        Hentai2readRipper ripper = new Hentai2readRipper(new URI("https://hentai2read.com/sm_school_memorial/1/").toURL());
        testRipper(ripper);
    }
}